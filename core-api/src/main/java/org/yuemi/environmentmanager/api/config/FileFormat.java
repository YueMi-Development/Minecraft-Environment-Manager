package org.yuemi.environmentmanager.api.config;

/**
 * Represents the format of a configuration file.
 * Used by {@link TextualConfigurationEditor} to apply format-specific
 * parsing and replacement rules.
 */
public enum FileFormat {

    /** YAML format (.yml, .yaml) — indent-based nesting, ':' separator, '#' comments. */
    YAML("yml", "yaml"),

    /** JSON format (.json) — brace-based structure, ':' separator, quoted keys. */
    JSON("json"),

    /** TOML format (.toml) — [table] headers, '=' separator, '#' comments. */
    TOML("toml"),

    /** HOCON format (.conf, .hocon) — indent/brace nesting, '='/':', '#'/'//' comments. */
    HOCON("conf", "hocon");

    private final String[] extensions;

    FileFormat(String... extensions) {
        this.extensions = extensions;
    }

    /**
     * Whether this format uses TOML-style {@code [table.path]} headers for nesting.
     */
    public boolean usesTableHeaders() {
        return this == TOML;
    }

    /**
     * Whether this format uses braces ({@code {}}) and brackets ({@code []}) for
     * structural nesting, requiring those characters to be skipped during key extraction.
     */
    public boolean hasStructuralBraces() {
        return this == JSON || this == HOCON;
    }

    /**
     * Checks if a trimmed line is a comment in this format.
     *
     * @param trimmed the trimmed line content
     * @return true if the line is a comment
     */
    public boolean isComment(String trimmed) {
        if (this == YAML || this == TOML) {
            return trimmed.startsWith("#");
        } else if (this == HOCON) {
            return trimmed.startsWith("#") || trimmed.startsWith("//");
        }
        // JSON has no standard comment syntax
        return false;
    }

    /**
     * Resolves the file format from a file extension string.
     *
     * @param extension the file extension without the dot (e.g., "yml", "toml")
     * @return the matching FileFormat, or {@link #YAML} as default
     */
    public static FileFormat fromExtension(String extension) {
        String ext = extension.toLowerCase();
        for (FileFormat format : values()) {
            for (String e : format.extensions) {
                if (e.equals(ext)) return format;
            }
        }
        return YAML;
    }

    /**
     * Resolves the file format from a file path string.
     *
     * @param path the file path (e.g., "config/server.toml")
     * @return the matching FileFormat, or {@link #YAML} as default
     */
    public static FileFormat fromPath(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1) return YAML;
        return fromExtension(path.substring(lastDot + 1));
    }
}
