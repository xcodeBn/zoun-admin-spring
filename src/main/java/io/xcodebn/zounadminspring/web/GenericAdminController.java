package io.xcodebn.zounadminspring.web;

import io.xcodebn.zounadminspring.config.AdminUIProperties;
import io.xcodebn.zounadminspring.core.AdminModelRegistry;
import io.xcodebn.zounadminspring.core.EntityReflectionService;
import io.xcodebn.zounadminspring.core.FieldMetadata;
import io.xcodebn.zounadminspring.core.ModelMetadata;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generic controller that handles CRUD operations for all discovered JPA entities.
 * Dynamically generates views and handles form submissions using reflection.
 */
@Controller
@RequestMapping("${zoun.admin.ui.base-path:/zoun-admin}")
public class GenericAdminController {

    private static final Logger log = LoggerFactory.getLogger(GenericAdminController.class);

    private final AdminModelRegistry modelRegistry;
    private final EntityReflectionService reflectionService;
    private final FormDataBinder formDataBinder;
    private final AdminUIProperties properties;

    public GenericAdminController(AdminModelRegistry modelRegistry,
                                  EntityReflectionService reflectionService,
                                  FormDataBinder formDataBinder,
                                  AdminUIProperties properties) {
        this.modelRegistry = modelRegistry;
        this.reflectionService = reflectionService;
        this.formDataBinder = formDataBinder;
        this.properties = properties;
    }

    /**
     * Dashboard - List all available entities.
     */
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        Map<String, ModelMetadata> models = modelRegistry.getAllModels();
        model.addAttribute("models", models);
        model.addAttribute("appTitle", properties.getAppTitle());
        return "zoun-admin-ui/index";
    }

    /**
     * List view - Paginated table of entities.
     */
    @GetMapping("/models/{modelName}")
    public String listEntities(@PathVariable String modelName,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "id") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               @RequestParam(required = false) String search,
                               Model model) {

        ModelMetadata metadata = modelRegistry.getModelMetadata(modelName)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

        // Get field metadata for table headers
        List<FieldMetadata> fields = reflectionService.inspect(metadata.entityClass());
        List<FieldMetadata> visibleFields = fields.stream()
                .filter(FieldMetadata::isVisible)
                .filter(f -> !f.isLob()) // Don't show LOB fields in table
                .limit(10) // Limit columns to avoid clutter
                .collect(Collectors.toList());

        // Fetch paginated data
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, properties.getPageSize(), sort);

        @SuppressWarnings("unchecked")
        JpaRepository<Object, Object> repository = (JpaRepository<Object, Object>) metadata.repository();

        Page<Object> entityPage = repository.findAll(pageable);

        model.addAttribute("modelName", modelName);
        model.addAttribute("fields", visibleFields);
        model.addAttribute("entities", entityPage.getContent());
        model.addAttribute("page", entityPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("appTitle", properties.getAppTitle());

        return "zoun-admin-ui/list";
    }

    /**
     * Create form - Empty form for new entity.
     */
    @GetMapping("/models/{modelName}/new")
    public String newEntityForm(@PathVariable String modelName, Model model) {
        ModelMetadata metadata = modelRegistry.getModelMetadata(modelName)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

        List<FieldMetadata> fields = reflectionService.inspect(metadata.entityClass());

        // For relationship fields, fetch available options
        Map<String, List<Object>> relationshipOptions = new HashMap<>();
        for (FieldMetadata field : fields) {
            if (field.isRelationship() && field.relationshipMetadata().isToOne()) {
                String targetEntityName = field.relationshipMetadata().targetEntityName();
                List<Object> options = fetchAllEntities(targetEntityName);
                relationshipOptions.put(field.name(), options);
            }
        }

        model.addAttribute("modelName", modelName);
        model.addAttribute("fields", fields);
        model.addAttribute("entity", null); // New entity
        model.addAttribute("relationshipOptions", relationshipOptions);
        model.addAttribute("appTitle", properties.getAppTitle());
        model.addAttribute("isEdit", false);

        return "zoun-admin-ui/form";
    }

    /**
     * Edit form - Pre-populated form for existing entity.
     */
    @GetMapping("/models/{modelName}/edit/{id}")
    public String editEntityForm(@PathVariable String modelName,
                                 @PathVariable String id,
                                 Model model) {

        ModelMetadata metadata = modelRegistry.getModelMetadata(modelName)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

        // Fetch the entity by ID
        Object entityId = convertId(id, metadata.idClass());

        @SuppressWarnings("unchecked")
        JpaRepository<Object, Object> repository = (JpaRepository<Object, Object>) metadata.repository();

        Object entity = repository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));

        List<FieldMetadata> fields = reflectionService.inspect(metadata.entityClass());

        // For relationship fields, fetch available options
        Map<String, List<Object>> relationshipOptions = new HashMap<>();
        for (FieldMetadata field : fields) {
            if (field.isRelationship() && field.relationshipMetadata().isToOne()) {
                String targetEntityName = field.relationshipMetadata().targetEntityName();
                List<Object> options = fetchAllEntities(targetEntityName);
                relationshipOptions.put(field.name(), options);
            }
        }

        model.addAttribute("modelName", modelName);
        model.addAttribute("fields", fields);
        model.addAttribute("entity", entity);
        model.addAttribute("relationshipOptions", relationshipOptions);
        model.addAttribute("appTitle", properties.getAppTitle());
        model.addAttribute("isEdit", true);

        return "zoun-admin-ui/form";
    }

    /**
     * Save/Update handler - Process form submission.
     */
    @PostMapping("/models/{modelName}/save")
    public String saveEntity(@PathVariable String modelName,
                            @RequestParam Map<String, String> formData,
                            @RequestParam(required = false) Map<String, MultipartFile> files,
                            RedirectAttributes redirectAttributes) {

        try {
            ModelMetadata metadata = modelRegistry.getModelMetadata(modelName)
                    .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

            @SuppressWarnings("unchecked")
            JpaRepository<Object, Object> repository = (JpaRepository<Object, Object>) metadata.repository();

            // Check if this is an update (ID present) or create (no ID)
            String idValue = formData.get("id");
            Object entity = null;

            if (idValue != null && !idValue.isBlank()) {
                // Update existing entity
                Object entityId = convertId(idValue, metadata.idClass());
                entity = repository.findById(entityId).orElse(null);
            }

            // Bind form data to entity
            entity = formDataBinder.bind(formData, files, entity, metadata.entityClass());

            // Save entity
            repository.save(entity);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully saved " + modelName);

            return "redirect:${zoun.admin.ui.base-path:/zoun-admin}/models/" + modelName;

        } catch (Exception e) {
            log.error("Failed to save entity: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to save: " + e.getMessage());
            return "redirect:${zoun.admin.ui.base-path:/zoun-admin}/models/" + modelName + "/new";
        }
    }

    /**
     * Delete handler - Delete an entity by ID.
     */
    @PostMapping("/models/{modelName}/delete/{id}")
    public String deleteEntity(@PathVariable String modelName,
                              @PathVariable String id,
                              RedirectAttributes redirectAttributes) {

        try {
            ModelMetadata metadata = modelRegistry.getModelMetadata(modelName)
                    .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

            Object entityId = convertId(id, metadata.idClass());

            @SuppressWarnings("unchecked")
            JpaRepository<Object, Object> repository = (JpaRepository<Object, Object>) metadata.repository();

            repository.deleteById(entityId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully deleted " + modelName);

        } catch (Exception e) {
            log.error("Failed to delete entity: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete: " + e.getMessage());
        }

        return "redirect:${zoun.admin.ui.base-path:/zoun-admin}/models/" + modelName;
    }

    /**
     * File download handler - Download @Lob byte[] fields.
     */
    @GetMapping("/models/{modelName}/file/{id}/{fieldName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String modelName,
                                               @PathVariable String id,
                                               @PathVariable String fieldName) {

        try {
            ModelMetadata metadata = modelRegistry.getModelMetadata(modelName)
                    .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

            Object entityId = convertId(id, metadata.idClass());

            @SuppressWarnings("unchecked")
            JpaRepository<Object, Object> repository = (JpaRepository<Object, Object>) metadata.repository();

            Object entity = repository.findById(entityId)
                    .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));

            // Get the field value
            Field field = findField(metadata.entityClass(), fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Field not found: " + fieldName);
            }

            field.setAccessible(true);
            byte[] fileData = (byte[]) field.get(entity);

            if (fileData == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fieldName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileData);

        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods

    private Object convertId(String idValue, Class<?> idClass) {
        if (idClass == Long.class || idClass == long.class) {
            return Long.parseLong(idValue);
        }
        if (idClass == Integer.class || idClass == int.class) {
            return Integer.parseInt(idValue);
        }
        if (idClass == String.class) {
            return idValue;
        }
        throw new IllegalArgumentException("Unsupported ID type: " + idClass.getName());
    }

    private List<Object> fetchAllEntities(String modelName) {
        return modelRegistry.getModelMetadata(modelName)
                .map(metadata -> {
                    @SuppressWarnings("unchecked")
                    JpaRepository<Object, Object> repository = (JpaRepository<Object, Object>) metadata.repository();
                    return repository.findAll();
                })
                .orElse(Collections.emptyList());
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
}
