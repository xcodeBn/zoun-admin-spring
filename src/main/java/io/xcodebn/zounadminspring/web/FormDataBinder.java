package io.xcodebn.zounadminspring.web;

import io.xcodebn.zounadminspring.core.AdminModelRegistry;
import io.xcodebn.zounadminspring.core.EntityReflectionService;
import io.xcodebn.zounadminspring.core.FieldMetadata;
import io.xcodebn.zounadminspring.core.ModelMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Binds form data (Map of field names to values) to entity objects.
 * Handles type conversion, relationships, and file uploads.
 */
@Component
public class FormDataBinder {

    private static final Logger log = LoggerFactory.getLogger(FormDataBinder.class);

    private final TypeConverter typeConverter;
    private final EntityReflectionService reflectionService;
    private final AdminModelRegistry modelRegistry;

    public FormDataBinder(TypeConverter typeConverter,
                          EntityReflectionService reflectionService,
                          AdminModelRegistry modelRegistry) {
        this.typeConverter = typeConverter;
        this.reflectionService = reflectionService;
        this.modelRegistry = modelRegistry;
    }

    /**
     * Bind form data to an entity instance.
     * Creates a new instance if entity is null.
     */
    public <T> T bind(Map<String, String> formData,
                      Map<String, MultipartFile> files,
                      T entity,
                      Class<T> entityClass) throws Exception {

        if (entity == null) {
            entity = entityClass.getDeclaredConstructor().newInstance();
        }

        List<FieldMetadata> fields = reflectionService.inspect(entityClass);

        for (FieldMetadata fieldMetadata : fields) {
            String fieldName = fieldMetadata.name();

            // Skip transient, hidden, and ID fields (ID is usually auto-generated)
            if (fieldMetadata.isTransient() || fieldMetadata.isHidden()) {
                continue;
            }

            // Handle file uploads for @Lob fields
            if (fieldMetadata.isLob() && files != null && files.containsKey(fieldName)) {
                bindFileField(entity, entityClass, fieldName, files.get(fieldName));
                continue;
            }

            // Handle relationships
            if (fieldMetadata.isRelationship()) {
                bindRelationshipField(entity, entityClass, fieldName, fieldMetadata, formData);
                continue;
            }

            // Handle regular fields
            if (formData.containsKey(fieldName)) {
                bindSimpleField(entity, entityClass, fieldName, formData.get(fieldName), fieldMetadata.type());
            }
        }

        return entity;
    }

    /**
     * Bind a simple (non-relationship) field.
     */
    private <T> void bindSimpleField(T entity, Class<T> entityClass, String fieldName, String value, Class<?> fieldType) {
        try {
            Field field = findField(entityClass, fieldName);
            if (field == null) {
                log.warn("Field '{}' not found in class '{}'", fieldName, entityClass.getName());
                return;
            }

            field.setAccessible(true);

            Object convertedValue = typeConverter.convert(value, fieldType);
            field.set(entity, convertedValue);

        } catch (Exception e) {
            log.error("Failed to bind field '{}': {}", fieldName, e.getMessage());
            throw new RuntimeException("Failed to bind field '" + fieldName + "'", e);
        }
    }

    /**
     * Bind a relationship field (@ManyToOne, @OneToOne, etc.).
     */
    private <T> void bindRelationshipField(T entity, Class<T> entityClass, String fieldName,
                                           FieldMetadata fieldMetadata, Map<String, String> formData) {
        try {
            // For to-one relationships, expect the ID of the related entity
            if (fieldMetadata.relationshipMetadata().isToOne()) {
                String relatedIdValue = formData.get(fieldName);

                if (relatedIdValue == null || relatedIdValue.isBlank()) {
                    // No related entity selected, set to null
                    Field field = findField(entityClass, fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(entity, null);
                    }
                    return;
                }

                // Fetch the related entity by ID
                String targetEntityName = fieldMetadata.relationshipMetadata().targetEntityName();
                ModelMetadata targetModel = modelRegistry.getModelMetadata(targetEntityName)
                        .orElseThrow(() -> new IllegalStateException("Target entity '" + targetEntityName + "' not found"));

                JpaRepository<?, ?> targetRepository = targetModel.repository();
                Object relatedId = typeConverter.convert(relatedIdValue, targetModel.idClass());

                @SuppressWarnings("unchecked")
                JpaRepository<Object, Object> repo = (JpaRepository<Object, Object>) targetRepository;
                Object relatedEntity = repo.findById(relatedId).orElse(null);

                Field field = findField(entityClass, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(entity, relatedEntity);
                }
            }

            // TODO: Handle to-many relationships (OneToMany, ManyToMany)
            // For now, we'll skip these as they require more complex UI handling

        } catch (Exception e) {
            log.error("Failed to bind relationship field '{}': {}", fieldName, e.getMessage());
            throw new RuntimeException("Failed to bind relationship field '" + fieldName + "'", e);
        }
    }

    /**
     * Bind a file upload to a @Lob byte[] field.
     */
    private <T> void bindFileField(T entity, Class<T> entityClass, String fieldName, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return;
            }

            Field field = findField(entityClass, fieldName);
            if (field == null) {
                log.warn("Field '{}' not found in class '{}'", fieldName, entityClass.getName());
                return;
            }

            field.setAccessible(true);

            // Convert to byte array
            byte[] fileData = file.getBytes();
            field.set(entity, fileData);

            log.debug("Uploaded file '{}' ({} bytes) to field '{}'",
                    file.getOriginalFilename(), fileData.length, fieldName);

        } catch (Exception e) {
            log.error("Failed to bind file field '{}': {}", fieldName, e.getMessage());
            throw new RuntimeException("Failed to bind file field '" + fieldName + "'", e);
        }
    }

    /**
     * Find a field by name in a class (including inherited fields).
     */
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
