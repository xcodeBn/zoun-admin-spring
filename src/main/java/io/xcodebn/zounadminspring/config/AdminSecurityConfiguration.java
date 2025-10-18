package io.xcodebn.zounadminspring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration for the Zoun Admin UI.
 * Restricts access to the admin panel based on the configured role.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(prefix = "zoun.admin.ui", name = "enabled", havingValue = "true")
@Order(1) // High precedence to run before application's security config
public class AdminSecurityConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AdminSecurityConfiguration.class);

    private final AdminUIProperties properties;

    public AdminSecurityConfiguration(AdminUIProperties properties) {
        this.properties = properties;
        log.info("Zoun Admin UI Security is enabled. Required role: {}", properties.getRequiredRole());
    }

    /**
     * Configure security for the admin panel.
     * Only applies to URLs under the configured base path.
     */
    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        String basePath = properties.getBasePath();
        String requiredRole = properties.getRequiredRole();

        http
            // Only apply this security configuration to admin URLs
            .securityMatcher(basePath + "/**")

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(basePath + "/**")
                .hasRole(requiredRole)
            )

            // Form-based login
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )

            // Logout configuration
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher(basePath + "/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // Enable CSRF protection
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(basePath + "/models/*/delete/*") // Allow DELETE via POST
            );

        log.debug("Admin security filter chain configured for path: {}", basePath);

        return http.build();
    }
}
