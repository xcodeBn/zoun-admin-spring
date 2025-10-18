package io.xcodebn.zounadminspring.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Service that uses reflection to inspect JPA entities and extract field metadata.
 * Identifies field types, relationships, validation annotations, and other attributes.
 */
@Service
public class EntityReflectionService {

    /**
     * Inspect an entity class and return metadata for all its fields.
     */
    public List<FieldMetadata> inspect(Class<?> entityClass) {
        List<FieldMetadata> fieldMetadataList = new ArrayList<>();

        // Get all declared fields including inherited ones
        List<Field> allFields = getAllFields(entityClass);

        for (Field field : allFields) {
            field.setAccessible(true);
            FieldMetadata metadata = inspectField(field);
            fieldMetadataList.add(metadata);
        }

        // Sort by order annotation if present, otherwise preserve declaration order
        fieldMetadataList.sort(Comparator.comparing(
            fm -> fm.order() != null ? fm.order() : Integer.MAX_VALUE
        ));

        return fieldMetadataList;
    }

    /**
     * Get all fields from a class including inherited fields.
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    /**
     * Inspect a single field and extract its metadata.
     */
    private FieldMetadata inspectField(Field field) {
        String name = field.getName();
        Class<?> type = field.getType();

        // Determine field type
        FieldMetadata.FieldType fieldType = determineFieldType(field, type);

        // Check for annotations
        boolean isId = field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
        boolean isTransient = field.isAnnotationPresent(Transient.class) ||
                              java.lang.reflect.Modifier.isTransient(field.getModifiers());
        boolean isLob = field.isAnnotationPresent(Lob.class);

        // Custom annotations (we'll create these later)
        boolean isHidden = false; // field.isAnnotationPresent(AdminHidden.class);
        boolean isReadOnly = false; // field.isAnnotationPresent(AdminReadOnly.class);
        String label = null; // Extract from @AdminLabel if present
        Integer order = null; // Extract from @AdminOrder if present

        // Extract relationship metadata if applicable
        RelationshipMetadata relationshipMetadata = extractRelationshipMetadata(field, fieldType);

        // Extract validation annotations
        Map<Class<? extends Annotation>, Annotation> validationAnnotations = extractValidationAnnotations(field);

        return new FieldMetadata(
            name,
            type,
            fieldType,
            isId,
            isTransient,
            isHidden,
            isReadOnly,
            label,
            order,
            relationshipMetadata,
            validationAnnotations,
            isLob
        );
    }

    /**
     * Determine the field type for rendering appropriate UI controls.
     */
    private FieldMetadata.FieldType determineFieldType(Field field, Class<?> type) {
        // Check for relationships first
        if (field.isAnnotationPresent(ManyToOne.class)) {
            return FieldMetadata.FieldType.MANY_TO_ONE;
        }
        if (field.isAnnotationPresent(OneToMany.class)) {
            return FieldMetadata.FieldType.ONE_TO_MANY;
        }
        if (field.isAnnotationPresent(ManyToMany.class)) {
            return FieldMetadata.FieldType.MANY_TO_MANY;
        }
        if (field.isAnnotationPresent(OneToOne.class)) {
            return FieldMetadata.FieldType.ONE_TO_ONE;
        }

        // Check for LOB
        if (field.isAnnotationPresent(Lob.class)) {
            return FieldMetadata.FieldType.LOB;
        }

        // Primitive and wrapper types
        if (type == String.class) {
            return FieldMetadata.FieldType.STRING;
        }
        if (type == Integer.class || type == int.class) {
            return FieldMetadata.FieldType.INTEGER;
        }
        if (type == Long.class || type == long.class) {
            return FieldMetadata.FieldType.LONG;
        }
        if (type == Double.class || type == double.class) {
            return FieldMetadata.FieldType.DOUBLE;
        }
        if (type == Float.class || type == float.class) {
            return FieldMetadata.FieldType.FLOAT;
        }
        if (type == Boolean.class || type == boolean.class) {
            return FieldMetadata.FieldType.BOOLEAN;
        }
        if (type.isEnum()) {
            return FieldMetadata.FieldType.ENUM;
        }

        // Date/time types
        if (type == Date.class || type == java.sql.Date.class || type == LocalDate.class) {
            return FieldMetadata.FieldType.DATE;
        }
        if (type == LocalDateTime.class || type == java.sql.Timestamp.class || Temporal.class.isAssignableFrom(type)) {
            return FieldMetadata.FieldType.TIMESTAMP;
        }

        // BigDecimal (treat as double for now)
        if (type == BigDecimal.class) {
            return FieldMetadata.FieldType.DOUBLE;
        }

        return FieldMetadata.FieldType.UNKNOWN;
    }

    /**
     * Extract relationship metadata from a field.
     */
    private RelationshipMetadata extractRelationshipMetadata(Field field, FieldMetadata.FieldType fieldType) {
        if (!isRelationshipType(fieldType)) {
            return null;
        }

        RelationshipMetadata.RelationshipType relationshipType = switch (fieldType) {
            case MANY_TO_ONE -> RelationshipMetadata.RelationshipType.MANY_TO_ONE;
            case ONE_TO_MANY -> RelationshipMetadata.RelationshipType.ONE_TO_MANY;
            case MANY_TO_MANY -> RelationshipMetadata.RelationshipType.MANY_TO_MANY;
            case ONE_TO_ONE -> RelationshipMetadata.RelationshipType.ONE_TO_ONE;
            default -> null;
        };

        if (relationshipType == null) {
            return null;
        }

        // Determine target entity
        Class<?> targetEntity = determineTargetEntity(field, fieldType);
        String targetEntityName = targetEntity != null ? targetEntity.getSimpleName() : "Unknown";

        // Extract mappedBy attribute
        String mappedBy = extractMappedBy(field, fieldType);

        // Check if lazy loaded
        boolean isLazy = isLazyLoaded(field, fieldType);

        return new RelationshipMetadata(
            targetEntity,
            targetEntityName,
            relationshipType,
            mappedBy,
            isLazy
        );
    }

    /**
     * Determine the target entity class for a relationship.
     */
    private Class<?> determineTargetEntity(Field field, FieldMetadata.FieldType fieldType) {
        Class<?> fieldClass = field.getType();

        // For to-many relationships, extract generic type from Collection
        if (fieldType == FieldMetadata.FieldType.ONE_TO_MANY ||
            fieldType == FieldMetadata.FieldType.MANY_TO_MANY) {

            if (Collection.class.isAssignableFrom(fieldClass)) {
                var genericType = field.getGenericType();
                if (genericType instanceof java.lang.reflect.ParameterizedType paramType) {
                    var typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> targetClass) {
                        return targetClass;
                    }
                }
            }
        }

        // For to-one relationships, the field type is the target entity
        return fieldClass;
    }

    /**
     * Extract the mappedBy attribute from a relationship annotation.
     */
    private String extractMappedBy(Field field, FieldMetadata.FieldType fieldType) {
        return switch (fieldType) {
            case ONE_TO_MANY -> {
                OneToMany annotation = field.getAnnotation(OneToMany.class);
                yield annotation != null && !annotation.mappedBy().isEmpty() ? annotation.mappedBy() : null;
            }
            case MANY_TO_MANY -> {
                ManyToMany annotation = field.getAnnotation(ManyToMany.class);
                yield annotation != null && !annotation.mappedBy().isEmpty() ? annotation.mappedBy() : null;
            }
            case ONE_TO_ONE -> {
                OneToOne annotation = field.getAnnotation(OneToOne.class);
                yield annotation != null && !annotation.mappedBy().isEmpty() ? annotation.mappedBy() : null;
            }
            default -> null;
        };
    }

    /**
     * Check if a relationship is lazy-loaded.
     */
    private boolean isLazyLoaded(Field field, FieldMetadata.FieldType fieldType) {
        return switch (fieldType) {
            case MANY_TO_ONE -> {
                ManyToOne annotation = field.getAnnotation(ManyToOne.class);
                yield annotation != null && annotation.fetch() == FetchType.LAZY;
            }
            case ONE_TO_MANY -> {
                OneToMany annotation = field.getAnnotation(OneToMany.class);
                yield annotation == null || annotation.fetch() == FetchType.LAZY; // Default is LAZY
            }
            case MANY_TO_MANY -> {
                ManyToMany annotation = field.getAnnotation(ManyToMany.class);
                yield annotation == null || annotation.fetch() == FetchType.LAZY; // Default is LAZY
            }
            case ONE_TO_ONE -> {
                OneToOne annotation = field.getAnnotation(OneToOne.class);
                yield annotation != null && annotation.fetch() == FetchType.LAZY;
            }
            default -> false;
        };
    }

    /**
     * Extract validation annotations from a field.
     */
    private Map<Class<? extends Annotation>, Annotation> extractValidationAnnotations(Field field) {
        Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

        // Standard Bean Validation annotations
        addIfPresent(annotations, field, NotNull.class);
        addIfPresent(annotations, field, NotBlank.class);
        addIfPresent(annotations, field, NotEmpty.class);
        addIfPresent(annotations, field, Size.class);
        addIfPresent(annotations, field, Min.class);
        addIfPresent(annotations, field, Max.class);
        addIfPresent(annotations, field, Email.class);
        addIfPresent(annotations, field, Pattern.class);
        addIfPresent(annotations, field, Past.class);
        addIfPresent(annotations, field, Future.class);
        addIfPresent(annotations, field, PastOrPresent.class);
        addIfPresent(annotations, field, FutureOrPresent.class);
        addIfPresent(annotations, field, Positive.class);
        addIfPresent(annotations, field, PositiveOrZero.class);
        addIfPresent(annotations, field, Negative.class);
        addIfPresent(annotations, field, NegativeOrZero.class);

        return annotations;
    }

    /**
     * Add an annotation to the map if present on the field.
     */
    private <T extends Annotation> void addIfPresent(
        Map<Class<? extends Annotation>, Annotation> map,
        Field field,
        Class<T> annotationClass
    ) {
        T annotation = field.getAnnotation(annotationClass);
        if (annotation != null) {
            map.put(annotationClass, annotation);
        }
    }

    /**
     * Check if a field type represents a relationship.
     */
    private boolean isRelationshipType(FieldMetadata.FieldType fieldType) {
        return fieldType == FieldMetadata.FieldType.MANY_TO_ONE ||
               fieldType == FieldMetadata.FieldType.ONE_TO_MANY ||
               fieldType == FieldMetadata.FieldType.MANY_TO_MANY ||
               fieldType == FieldMetadata.FieldType.ONE_TO_ONE;
    }
}
