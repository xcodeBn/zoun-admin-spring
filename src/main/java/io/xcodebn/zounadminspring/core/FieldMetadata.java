package io.xcodebn.zounadminspring.core;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Metadata about a single field in a JPA entity.
 * Contains information needed to render forms, validate input, and handle relationships.
 */
public record FieldMetadata(
    String name,
    Class<?> type,
    FieldType fieldType,
    boolean isId,
    boolean isTransient,
    boolean isHidden,
    boolean isReadOnly,
    String label,
    Integer order,
    RelationshipMetadata relationshipMetadata,
    Map<Class<? extends Annotation>, Annotation> validationAnnotations,
    boolean isLob
) {

    /**
     * Enumeration of field types for rendering appropriate UI controls.
     */
    public enum FieldType {
        STRING,
        INTEGER,
        LONG,
        DOUBLE,
        FLOAT,
        BOOLEAN,
        DATE,
        TIMESTAMP,
        ENUM,
        MANY_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY,
        ONE_TO_ONE,
        LOB,
        UNKNOWN
    }

    /**
     * Get a user-friendly display label for this field.
     */
    public String getDisplayLabel() {
        if (label != null && !label.isBlank()) {
            return label;
        }
        // Convert camelCase to Title Case
        return name.replaceAll("([a-z])([A-Z])", "$1 $2")
                   .substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * Check if this field represents a relationship to another entity.
     */
    public boolean isRelationship() {
        return fieldType == FieldType.MANY_TO_ONE ||
               fieldType == FieldType.ONE_TO_MANY ||
               fieldType == FieldType.MANY_TO_MANY ||
               fieldType == FieldType.ONE_TO_ONE;
    }

    /**
     * Check if this field should be included in forms.
     */
    public boolean isEditable() {
        return !isId && !isTransient && !isHidden && !isReadOnly;
    }

    /**
     * Check if this field should be displayed (read-only fields are displayed but disabled).
     */
    public boolean isVisible() {
        return !isTransient && !isHidden;
    }
}
