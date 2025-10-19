package io.xcodebn.zounadminspring.core;

/**
 * Metadata about a relationship field (@ManyToOne, @OneToMany, etc.).
 * Contains information about the target entity and cardinality.
 */
public record RelationshipMetadata(
    Class<?> targetEntity,
    String targetEntityName,
    RelationshipType relationshipType,
    String mappedBy,
    boolean isLazy
) {

    /**
     * Types of JPA relationships.
     */
    public enum RelationshipType {
        MANY_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY,
        ONE_TO_ONE
    }

    /**
     * Check if this is a collection-based relationship (to-many).
     */
    public boolean isToMany() {
        return relationshipType == RelationshipType.ONE_TO_MANY ||
               relationshipType == RelationshipType.MANY_TO_MANY;
    }

    /**
     * Check if this is a singular relationship (to-one).
     */
    public boolean isToOne() {
        return relationshipType == RelationshipType.MANY_TO_ONE ||
               relationshipType == RelationshipType.ONE_TO_ONE;
    }
}
