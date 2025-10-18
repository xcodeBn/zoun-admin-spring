package io.xcodebn.zounadminspring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Zoun Admin UI.
 * Allows customization of the admin panel behavior and appearance.
 */
@ConfigurationProperties(prefix = "zoun.admin.ui")
public class AdminUIProperties {

    /**
     * Enable or disable the admin UI.
     * Default: false (for safety - must be explicitly enabled)
     */
    private boolean enabled = false;

    /**
     * Base URL path for the admin panel.
     * Default: /zoun-admin
     */
    private String basePath = "/zoun-admin";

    /**
     * The Spring Security role required to access the admin panel.
     * Default: ADMIN
     */
    private String requiredRole = "ADMIN";

    /**
     * Number of items per page in list views.
     * Default: 20
     */
    private int pageSize = 20;

    /**
     * Application title displayed in the admin UI.
     * Default: Zoun Admin Panel
     */
    private String appTitle = "Zoun Admin Panel";

    /**
     * Enable dark mode by default.
     * Default: false
     */
    private boolean darkMode = false;

    /**
     * Maximum file upload size for @Lob fields (in megabytes).
     * Default: 10 MB
     */
    private int maxFileSizeMb = 10;

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(int maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }
}
