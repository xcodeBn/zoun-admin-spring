package io.xcodebn.zounadminspring.core;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Metadata about a JPA entity discovered in the host application.
 * Contains the entity class, ID class, and the repository bean for CRUD operations.
 */
public record ModelMetadata(
    String modelName,
    Class<?> entityClass,
    Class<?> idClass,
    JpaRepository<?, ?> repository
) {

    /**
     * Get the simple name of the entity for display purposes.
     */
    public String getDisplayName() {
        return modelName;
    }

    /**
     * Get the repository cast to the correct generic type.
     * Use with caution - type safety is not guaranteed at compile time.
     */
    @SuppressWarnings("unchecked")
    public <T, ID> JpaRepository<T, ID> getTypedRepository() {
        return (JpaRepository<T, ID>) repository;
    }
}
