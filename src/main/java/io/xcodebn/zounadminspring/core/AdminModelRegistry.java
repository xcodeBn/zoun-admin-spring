package io.xcodebn.zounadminspring.core;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Central registry that discovers and manages all JPA entities in the host application.
 * Scans for all JpaRepository beans and extracts entity/ID class information via reflection.
 */
@Service
public class AdminModelRegistry {

    private static final Logger log = LoggerFactory.getLogger(AdminModelRegistry.class);

    private final ApplicationContext applicationContext;
    private final Map<String, ModelMetadata> modelRegistry = new LinkedHashMap<>();

    public AdminModelRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Scan and register all JPA repositories after bean initialization.
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing Zoun Admin Model Registry...");

        Map<String, JpaRepository> repositories = applicationContext.getBeansOfType(JpaRepository.class);

        for (Map.Entry<String, JpaRepository> entry : repositories.entrySet()) {
            String beanName = entry.getKey();
            JpaRepository<?, ?> repository = entry.getValue();

            try {
                registerRepository(beanName, repository);
            } catch (Exception e) {
                log.warn("Failed to register repository '{}': {}", beanName, e.getMessage());
            }
        }

        log.info("Registered {} entities: {}", modelRegistry.size(), modelRegistry.keySet());
    }

    /**
     * Register a single repository and extract its entity/ID types.
     */
    private void registerRepository(String beanName, JpaRepository<?, ?> repository) {
        // Use ResolvableType to extract generic parameters from JpaRepository<Entity, ID>
        ResolvableType repositoryType = ResolvableType.forClass(repository.getClass())
                                                       .as(JpaRepository.class);

        ResolvableType[] generics = repositoryType.getGenerics();

        if (generics.length != 2) {
            log.warn("Repository '{}' does not have exactly 2 generic parameters", beanName);
            return;
        }

        Class<?> entityClass = generics[0].resolve();
        Class<?> idClass = generics[1].resolve();

        if (entityClass == null || idClass == null) {
            log.warn("Could not resolve entity or ID class for repository '{}'", beanName);
            return;
        }

        String modelName = entityClass.getSimpleName();

        ModelMetadata metadata = new ModelMetadata(
            modelName,
            entityClass,
            idClass,
            repository
        );

        modelRegistry.put(modelName, metadata);
        log.debug("Registered entity '{}' with ID type '{}' from repository '{}'",
                  modelName, idClass.getSimpleName(), beanName);
    }

    /**
     * Get metadata for a specific entity by name.
     */
    public Optional<ModelMetadata> getModelMetadata(String modelName) {
        return Optional.ofNullable(modelRegistry.get(modelName));
    }

    /**
     * Get all registered entities.
     */
    public Map<String, ModelMetadata> getAllModels() {
        return Collections.unmodifiableMap(modelRegistry);
    }

    /**
     * Check if an entity is registered.
     */
    public boolean hasModel(String modelName) {
        return modelRegistry.containsKey(modelName);
    }

    /**
     * Get the total number of registered entities.
     */
    public int getModelCount() {
        return modelRegistry.size();
    }
}
