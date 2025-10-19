package io.xcodebn.zounadminspring.util;

import io.xcodebn.zounadminspring.core.FieldMetadata;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Thymeleaf utility bean for accessing entity field values via reflection.
 */
@Component("reflectionHelper")
public class  ReflectionHelper {

    /**
     * Get the value of a field from an entity object.
     *
     * @param entity The entity object
     * @param fieldName The name of the field
     * @return The field value, or null if not found
     */
    public Object getFieldValue(Object entity, String fieldName) {
        if (entity == null || fieldName == null) {
            return null;
        }

        try {
            Field field = findField(entity.getClass(), fieldName);
            if (field == null) {
                return null;
            }

            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if a field has a specific validation annotation.
     */
    public boolean hasValidationAnnotation(FieldMetadata field, String annotationName) {
        if (field == null || field.validationAnnotations() == null) {
            return false;
        }

        return switch (annotationName) {
            case "NotNull" -> field.validationAnnotations().containsKey(NotNull.class);
            case "NotBlank" -> field.validationAnnotations().containsKey(NotBlank.class);
            case "NotEmpty" -> field.validationAnnotations().containsKey(NotEmpty.class);
            case "Size" -> field.validationAnnotations().containsKey(Size.class);
            case "Min" -> field.validationAnnotations().containsKey(Min.class);
            case "Max" -> field.validationAnnotations().containsKey(Max.class);
            case "Email" -> field.validationAnnotations().containsKey(Email.class);
            case "Pattern" -> field.validationAnnotations().containsKey(Pattern.class);
            default -> false;
        };
    }

    /**
     * Get a Size annotation from a field.
     */
    public Size getSizeAnnotation(FieldMetadata field) {
        if (field == null || field.validationAnnotations() == null) {
            return null;
        }
        return (Size) field.validationAnnotations().get(Size.class);
    }

    /**
     * Find a field by name in a class hierarchy.
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
