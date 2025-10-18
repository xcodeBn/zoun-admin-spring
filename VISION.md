# Zoun-Admin-Spring - Complete Vision & Requirements

## Project Overview
A Spring Boot starter library that automatically generates a fully-functional, production-ready web admin panel for all JPA entities in a host application with **zero code required**.

## Core Philosophy
- **Convention over Configuration**: Works out-of-the-box with sensible defaults
- **Plug-and-Play**: Add dependency + 2 config lines = complete admin panel
- **Zero Boilerplate**: No need to write controllers, services, or views
- **Type-Safe**: Leverages Java reflection and generics for type safety
- **Production-Ready**: Includes validation, security, error handling, and professional UI

---

## Feature Set

### 1. Auto-Discovery & Model Registry
- [x] Scan all `JpaRepository` beans at startup
- [x] Extract entity class and ID class via reflection
- [x] Build runtime registry: `Map<String, ModelMetadata>`
- [x] Support for composite keys (`@IdClass`, `@EmbeddedId`)
- [x] Detect and map entity relationships (`@ManyToOne`, `@OneToMany`, `@ManyToMany`)

### 2. Entity Introspection
- [x] Reflect on entity fields to extract metadata
- [x] Field types: primitives, wrappers, String, Date, Enum, Associations
- [x] Identify `@Id` fields (primary keys)
- [x] Detect relationship annotations and cardinality
- [x] Extract validation annotations (`@NotNull`, `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern`)
- [x] Identify `@Lob` fields for file/blob handling
- [x] Support for `@Transient` fields (exclude from forms)
- [x] Custom annotation: `@AdminHidden` - exclude field from admin UI
- [x] Custom annotation: `@AdminReadOnly` - show in forms but disable editing

### 3. CRUD Operations
- [x] **List View**: Paginated table with sorting
  - Configurable page size
  - Search/filter capability
  - Bulk actions (delete selected)
- [x] **Create**: Form with proper input types based on field metadata
- [x] **Read**: Detail view showing all field values
- [x] **Update**: Pre-populated form with validation
- [x] **Delete**: Confirmation modal before deletion
- [x] Cascade handling for relationships

### 4. Validation
- [x] Server-side validation using Bean Validation API
- [x] Respect all standard validation annotations:
  - `@NotNull`, `@NotBlank`, `@NotEmpty`
  - `@Size(min=, max=)`
  - `@Min`, `@Max`
  - `@Email`, `@Pattern`
  - `@Past`, `@Future`, `@PastOrPresent`, `@FutureOrPresent`
  - `@Positive`, `@PositiveOrZero`, `@Negative`, `@NegativeOrZero`
- [x] Display validation errors inline in forms
- [x] Prevent invalid data submission

### 5. Relationship Handling
- [x] **@ManyToOne**: Dropdown/select with all available entities
- [x] **@OneToMany**: Show related entities in detail view, with add/remove actions
- [x] **@ManyToMany**: Multi-select or tag-style input
- [x] **@OneToOne**: Similar to @ManyToOne
- [x] Lazy loading support (fetch relationships on-demand)
- [x] Nested relationship display (e.g., User -> Address -> Country)

### 6. File Upload Support
- [x] Detect `@Lob byte[]` and `@Lob Byte[]` fields
- [x] File upload input in forms
- [x] Store binary data in database
- [x] Download/view uploaded files
- [x] Support for common file types (images, PDFs, documents)
- [x] File size validation

### 7. Security
- [x] Role-based access control (RBAC)
- [x] Configurable required role (default: `ADMIN`)
- [x] Security filter chain with high precedence
- [x] CSRF protection enabled
- [x] Login mechanism (form-based or HTTP Basic)
- [x] Session management
- [x] Logout functionality
- [x] Optional: Entity-level permissions (future enhancement)

### 8. User Interface (Tailwind CSS)
- [x] Modern, responsive design using Tailwind CSS
- [x] Dark mode support (optional toggle)
- [x] Professional dashboard with entity cards
- [x] Data tables with:
  - Sortable columns
  - Pagination controls
  - Search/filter bar
  - Row actions (edit, delete)
  - Bulk actions
- [x] Forms with:
  - Proper input types (text, number, email, date, checkbox, select, file, etc.)
  - Inline validation errors
  - Clear labels and placeholders
  - Submit/cancel buttons
- [x] Modals for confirmations
- [x] Toast notifications for success/error messages
- [x] Loading states and spinners
- [x] Accessible (ARIA labels, keyboard navigation)

### 9. Configuration Properties
```properties
# Enable/disable the admin panel
zoun.admin.ui.enabled=true

# Base URL path for admin panel
zoun.admin.ui.base-path=/admin

# Required role to access admin panel
zoun.admin.ui.required-role=ADMIN

# Page size for list views
zoun.admin.ui.page-size=20

# Application title shown in UI
zoun.admin.ui.app-title=Admin Panel

# Enable dark mode by default
zoun.admin.ui.dark-mode=false

# Max file upload size for @Lob fields (in MB)
zoun.admin.ui.max-file-size=10
```

### 10. Error Handling
- [x] Global exception handler (`@ControllerAdvice`)
- [x] Custom error pages (404, 403, 500)
- [x] Friendly error messages for users
- [x] Detailed error logging for developers
- [x] Validation error aggregation and display

### 11. Custom Annotations (Optional Enhancements)
```java
@AdminHidden        // Exclude field from admin UI entirely
@AdminReadOnly      // Show in forms but disable editing
@AdminLabel("...")  // Custom label for field (instead of field name)
@AdminOrder(1)      // Control field order in forms
```

---

## Implementation Phases

### Phase 1: Core Foundation (Baseline Functionality)
**Goal**: Get basic auto-configuration and model discovery working

1.1. Project setup (build.gradle.kts already done)
1.2. Configuration properties class (`AdminUIProperties`)
1.3. Auto-configuration class (`AdminAutoConfiguration`)
1.4. Model registry service (`AdminModelRegistry`)
1.5. Entity reflection service (`EntityReflectionService`)
1.6. Register starter in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Deliverable**: Library can discover all JPA entities and their metadata at runtime

---

### Phase 2: Web Layer (CRUD UI)
**Goal**: Build generic controller and Tailwind-based views

2.1. Create data models:
   - `ModelMetadata` (entity class, ID class, repository)
   - `FieldMetadata` (name, type, validation rules, relationship info)

2.2. Generic admin controller (`GenericAdminController`):
   - `GET /` - Dashboard (list all entities)
   - `GET /models/{modelName}` - List view (paginated table)
   - `GET /models/{modelName}/new` - Create form
   - `GET /models/{modelName}/edit/{id}` - Edit form
   - `GET /models/{modelName}/{id}` - Detail view
   - `POST /models/{modelName}/save` - Save/update handler
   - `POST /models/{modelName}/delete/{id}` - Delete handler
   - `GET /models/{modelName}/file/{id}/{fieldName}` - File download

2.3. Thymeleaf templates with Tailwind CSS:
   - `layout.html` - Base layout with sidebar navigation
   - `index.html` - Dashboard with entity cards
   - `list.html` - Data table with sorting, pagination, search
   - `form.html` - Generic form with dynamic input types
   - `detail.html` - Read-only view of entity
   - `error.html` - Error page

2.4. Form handling utilities:
   - Type conversion (String → primitives, Date, Enum, etc.)
   - Relationship resolution (fetch related entities by ID)
   - File upload handling
   - Validation error mapping

**Deliverable**: Full CRUD interface for any entity with professional UI

---

### Phase 3: Security & Validation
**Goal**: Secure the admin panel and add validation

3.1. Security configuration (`AdminSecurityConfiguration`):
   - Security filter chain with `@Order` for precedence
   - Restrict access to `basePath/**`
   - Require role specified in config
   - Enable CSRF protection
   - Form login page

3.2. Validation integration:
   - Server-side validation using `Validator`
   - Display validation errors in forms
   - Extract validation rules from annotations to show hints in UI

**Deliverable**: Secured admin panel with comprehensive validation

---

### Phase 4: Polish & Testing
**Goal**: Production-ready library with documentation

4.1. Error handling:
   - Global exception handler
   - Custom error pages
   - User-friendly error messages

4.2. Demo application:
   - Create test module (`zoun-admin-spring-demo`)
   - Sample entities: `User`, `Product`, `Category`, `Order`
   - Sample relationships and validation annotations
   - In-memory H2 database
   - Test user with ADMIN role

4.3. Documentation:
   - README.md with quickstart guide
   - Configuration reference
   - Examples and screenshots
   - Troubleshooting guide

4.4. Testing:
   - Unit tests for reflection service
   - Integration tests for controller
   - Security tests

**Deliverable**: Fully tested, documented, production-ready library

---

## Technical Architecture

### Package Structure
```
io.xcodebn.zounadminspring
├── config
│   ├── AdminAutoConfiguration.java
│   ├── AdminSecurityConfiguration.java
│   └── AdminUIProperties.java
├── core
│   ├── AdminModelRegistry.java
│   ├── EntityReflectionService.java
│   ├── FieldMetadata.java
│   └── ModelMetadata.java
├── web
│   ├── GenericAdminController.java
│   ├── FormDataBinder.java
│   ├── TypeConverter.java
│   └── FileDownloadController.java
├── validation
│   ├── ValidationService.java
│   └── ValidationErrorHandler.java
├── annotations (optional)
│   ├── AdminHidden.java
│   ├── AdminReadOnly.java
│   ├── AdminLabel.java
│   └── AdminOrder.java
└── exception
    └── AdminExceptionHandler.java
```

### Key Technologies
- **Spring Boot 3.3.0**: Core framework
- **Spring Data JPA**: Entity and repository management
- **Thymeleaf**: Server-side templating
- **Tailwind CSS**: UI styling (via CDN)
- **Bean Validation API**: Server-side validation
- **Spring Security**: Authentication and authorization

---

## Success Criteria

The library is considered complete when a developer can:

1. Add the dependency to their Spring Boot app
2. Add 2 lines to `application.properties`:
   ```properties
   zoun.admin.ui.enabled=true
   zoun.admin.ui.required-role=ADMIN
   ```
3. Start the app
4. Navigate to `/admin`
5. See all their JPA entities listed
6. Perform full CRUD operations on any entity
7. Upload files to `@Lob` fields
8. See validation errors for invalid input
9. Navigate entity relationships
10. All without writing a single line of code

---

## Future Enhancements (Post-MVP)

- [ ] Export data (CSV, JSON, Excel)
- [ ] Import data from files
- [ ] Advanced search with multiple filters
- [ ] Audit logging (who changed what, when)
- [ ] Custom actions/buttons per entity
- [ ] Theming support (custom CSS/colors)
- [ ] REST API alongside web UI
- [ ] GraphQL support
- [ ] Multi-tenancy support
- [ ] Soft delete support
- [ ] Field-level permissions
- [ ] Custom validators
- [ ] Webhooks on CRUD events
- [ ] Email notifications
- [ ] Dashboard widgets with metrics
