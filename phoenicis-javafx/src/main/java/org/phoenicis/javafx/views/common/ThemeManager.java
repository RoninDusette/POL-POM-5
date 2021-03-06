package org.phoenicis.javafx.views.common;

public class ThemeManager {
    private Theme currentTheme;
    private final String themeUrl = "/org/phoenicis/javafx/themes";

    public ThemeManager() {
        currentTheme = Theme.DEFAULT;
    }

    public ThemeManager(Theme theme) {
        currentTheme = theme;
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(Theme theme) {
        currentTheme = theme;
    }

    /**
     * checks if a theme resource exists
     * 
     * @param resource
     *            theme resource
     * @return
     */
    public boolean resourceExists(String resource) {
        return getClass().getResourceAsStream(
                String.format("%s/%s/%s", themeUrl, currentTheme.getShortName(), resource)) != null;
    }

    /**
     * returns the full resource URL for a given theme resource
     * 
     * @param resource
     *            theme resource
     * @return full resource URL, falls back to default if resource does not
     *         exist in theme
     */
    public String getResourceUrl(String resource) {
        // check if theme contains resource
        if (resourceExists(resource)) {
            return String.format("%s/%s/%s", themeUrl, currentTheme.getShortName(), resource);
        } else {
            return String.format("%s/%s/%s", themeUrl, Theme.DEFAULT.getShortName(), resource);
        }
    }
}
