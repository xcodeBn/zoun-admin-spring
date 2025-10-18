package io.xcodebn.zounadminspring.config;

import io.xcodebn.zounadminspring.core.AdminModelRegistry;
import io.xcodebn.zounadminspring.core.EntityReflectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for the Zoun Admin UI.
 * Activates only when zoun.admin.ui.enabled=true in application properties.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "zoun.admin.ui", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AdminUIProperties.class)
@ComponentScan(basePackages = "io.xcodebn.zounadminspring")
public class AdminAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AdminAutoConfiguration.class);

    public AdminAutoConfiguration() {
        log.info("Zoun Admin UI is enabled and initializing...");
    }

    /**
     * Register the AdminModelRegistry bean.
     * This is already a @Service, but we can explicitly declare it here if needed.
     */
    @Bean
    public AdminModelRegistry adminModelRegistry() {
        return new AdminModelRegistry(null); // Will be injected by Spring
    }

    /**
     * Register the EntityReflectionService bean.
     * This is already a @Service, but we can explicitly declare it here if needed.
     */
    @Bean
    public EntityReflectionService entityReflectionService() {
        return new EntityReflectionService();
    }
}
