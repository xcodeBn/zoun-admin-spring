# Zoun Admin Spring Boot Starter

A **zero-code** Spring Boot starter that automatically generates a complete, production-ready admin panel for all your JPA entities.

## Features

✨ **Zero Configuration** - Just add the dependency and enable it
🔍 **Auto-Discovery** - Automatically finds all JPA entities in your application
📝 **Full CRUD** - Create, Read, Update, Delete operations out-of-the-box
🎨 **Modern UI** - Beautiful Tailwind CSS interface with dark mode support
🔒 **Secure** - Built-in role-based access control
✅ **Validation** - Respects all Bean Validation annotations
🔗 **Relationships** - Handles @ManyToOne, @OneToMany, @ManyToMany
📎 **File Uploads** - Support for @Lob byte[] fields
📄 **Pagination** - Paginated list views

## Quick Start

### 1. Add Dependency

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("io.xcodebn:zoun-admin-spring-boot-starter:0.0.1-SNAPSHOT")
}
```

**Gradle (Groovy):**
```groovy
dependencies {
    implementation 'io.xcodebn:zoun-admin-spring-boot-starter:0.0.1-SNAPSHOT'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.xcodebn</groupId>
    <artifactId>zoun-admin-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Configure

Add these properties to your `application.properties` or `application.yml`:

```properties
# Enable the admin panel
zoun.admin.ui.enabled=true

# Required role to access admin panel (default: ADMIN)
zoun.admin.ui.required-role=ADMIN
```

### 3. Run

Start your application and navigate to:

```
http://localhost:8080/zoun-admin
```

**That's it!** You now have a complete admin panel for all your JPA entities.

## Configuration

All configuration properties are prefixed with `zoun.admin.ui`:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `false` | Enable/disable the admin UI (must be explicitly enabled) |
| `base-path` | String | `/zoun-admin` | Base URL path for the admin panel |
| `required-role` | String | `ADMIN` | Spring Security role required to access the panel |
| `page-size` | int | `20` | Number of items per page in list views |
| `app-title` | String | `Zoun Admin Panel` | Application title displayed in the UI |
| `dark-mode` | boolean | `false` | Enable dark mode by default |
| `max-file-size-mb` | int | `10` | Maximum file upload size for @Lob fields (in MB) |

### Example Configuration

```yaml
zoun:
  admin:
    ui:
      enabled: true
      base-path: /admin
      required-role: SUPER_ADMIN
      page-size: 50
      app-title: My Application Admin
      dark-mode: true
      max-file-size-mb: 20
```

## Security Setup

The admin panel requires Spring Security. You must configure a user with the required role (default: `ADMIN`).

### Example In-Memory Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}admin") // Use proper password encoding in production!
            .roles("ADMIN")
            .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
```

### Example Database Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public UserDetailsService userDetailsService() {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Supported Field Types

Zoun Admin automatically renders appropriate UI controls for different field types:

| Java Type | UI Control | Notes |
|-----------|------------|-------|
| `String` | Text input | |
| `Integer`, `Long` | Number input | |
| `Double`, `Float`, `BigDecimal` | Number input (decimal) | |
| `Boolean` | Checkbox | |
| `Date`, `LocalDate` | Date picker | |
| `LocalDateTime`, `Timestamp` | DateTime picker | |
| `Enum` | Dropdown select | All enum values auto-populated |
| `@ManyToOne`, `@OneToOne` | Dropdown select | Related entities auto-loaded |
| `@OneToMany`, `@ManyToMany` | (Future: Multi-select) | Currently view-only |
| `@Lob byte[]` | File upload | With download link for existing files |

## Validation Support

Zoun Admin respects all standard Bean Validation annotations:

- `@NotNull`, `@NotBlank`, `@NotEmpty` - Makes field required
- `@Size(min=, max=)` - Shows min/max hints
- `@Min`, `@Max` - Number range validation
- `@Email` - Email format validation
- `@Pattern` - Regex validation
- `@Past`, `@Future`, `@PastOrPresent`, `@FutureOrPresent` - Date validation
- `@Positive`, `@PositiveOrZero`, `@Negative`, `@NegativeOrZero` - Number sign validation

### Example Entity with Validation

```java
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @Min(18)
    @Max(120)
    private Integer age;

    @Past
    private LocalDate birthDate;

    @ManyToOne
    @NotNull
    private Department department;

    // Getters and setters...
}
```

## Example Entities

### Basic Entity

```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Size(max = 500)
    private String description;

    @Positive
    private BigDecimal price;

    private Integer stock;

    private Boolean active;

    // Getters and setters...
}
```

### Entity with Relationships

```java
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private Customer customer;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Getters and setters...
}

public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}
```

### Entity with File Upload

```java
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Lob
    private byte[] fileData;

    private String fileName;

    private String mimeType;

    private LocalDateTime uploadDate;

    // Getters and setters...
}
```

## Customization (Future Features)

Future versions will support custom annotations for advanced control:

```java
@Entity
public class User {

    @Id
    private Long id;

    private String name;

    @AdminHidden  // Exclude from admin UI
    private String internalCode;

    @AdminReadOnly  // Show but disable editing
    private LocalDateTime createdAt;

    @AdminLabel("Full Name")  // Custom field label
    private String name;

    @AdminOrder(1)  // Control field order
    private String email;
}
```

## Requirements

- Java 21+
- Spring Boot 3.3.0+
- Spring Data JPA
- At least one JPA repository in your application
- Spring Security configured with at least one user having the required role

## How It Works

1. **Auto-Discovery**: At startup, Zoun Admin scans for all `JpaRepository` beans
2. **Metadata Extraction**: Uses reflection to inspect entity classes, fields, relationships, and validation rules
3. **Dynamic Controllers**: A single generic controller handles all CRUD operations for all entities
4. **Dynamic Views**: Thymeleaf templates render appropriate UI controls based on field metadata
5. **Type Safety**: Runtime type conversion and validation ensure data integrity

## Security Considerations

- **Disabled by Default**: The admin panel is disabled by default for safety (`zoun.admin.ui.enabled=false`)
- **Role-Based Access**: Only users with the configured role can access the panel
- **CSRF Protection**: All forms include CSRF tokens
- **No Data Exposure**: Only accessible entities via JPA repositories are shown
- **Production Use**: Always use proper authentication, HTTPS, and password encoding in production

## Troubleshooting

### "No entities found" on dashboard

**Cause**: No `JpaRepository` beans were discovered.

**Solution**: Ensure you have at least one repository interface:

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

### "Access Denied" or redirect to login

**Cause**: No user with the required role is authenticated.

**Solution**: Configure Spring Security with a user having the `ADMIN` role (or your configured role).

### Field not showing in form

**Causes**:
- Field is marked `@Transient`
- Field is marked with `@AdminHidden` (future feature)
- Field type is not supported

**Solution**: Check field annotations and type. Refer to supported types above.

## Roadmap

- [ ] Multi-select for `@OneToMany` and `@ManyToMany` relationships
- [ ] Advanced search and filtering
- [ ] Bulk operations (bulk delete, export)
- [ ] Custom actions per entity
- [ ] Audit logging
- [ ] Export to CSV/Excel
- [ ] Custom field renderers
- [ ] Dashboard widgets with metrics
- [ ] Multi-tenancy support

## License

This project is licensed under the MIT License.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## Support

For issues, questions, or feature requests, please open an issue on GitHub.

---

**Made with ❤️ by [xcodebn](https://github.com/xcodebn)**
