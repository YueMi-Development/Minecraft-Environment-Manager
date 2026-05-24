package org.yuemi.environmentmanager.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Surgically replaces values in configuration files while preserving all formatting and comments.
 * Supports YAML, JSON, TOML, and HOCON formats via {@link FileFormat}.
 */
public final class TextualConfigurationEditor {

    private TextualConfigurationEditor() {}

    /**
     * Matches TOML table headers: {@code [table.path]} or {@code [[array.table.path]]}
     * Captures the inner path and allows trailing whitespace/comments.
     */
    private static final Pattern TOML_TABLE_HEADER = Pattern.compile(
        "^\\[\\[?\\s*([^\\]]+?)\\s*\\]\\]?\\s*(#.*)?$"
    );

    /**
     * Validates that a TOML header path contains only valid TOML key characters.
     * Prevents false positives from JSON array values like {@code [\"string\", ...]}.
     */
    private static final Pattern TOML_PATH_VALIDATOR = Pattern.compile(
        "^[A-Za-z0-9_\\-]+(\\.[A-Za-z0-9_\\-]+)*$"
    );

    /**
     * Matches a quoted key at the start of a line followed by a separator ({@code :} or {@code =}).
     */
    private static final Pattern QUOTED_KEY_PATTERN = Pattern.compile(
        "^([\"'])(.+?)\\1\\s*[:=]"
    );

    /**
     * Updates a value in the configuration content string using the specified file format.
     *
     * @param content  the original file content
     * @param keyPath  the dot-separated key path (e.g., "database.host" or "common.multiThreading.numberOfThreads")
     * @param newValue the new value to set
     * @param format   the file format to use for parsing rules
     * @return the updated content string, or original if key path not found
     */
    public static String update(String content, String keyPath, String newValue, FileFormat format) {
        List<String> targetPath = Arrays.asList(keyPath.split("\\."));
        List<String> lines = new ArrayList<>(Arrays.asList(content.split("\\R", -1)));

        List<String> currentTomlPath = new ArrayList<>();
        List<Integer> indents = new ArrayList<>();
        List<String> currentContextPath = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || format.isComment(trimmed)) continue;

            // Parse TOML table headers when format supports them
            if (format.usesTableHeaders()) {
                String tomlPath = parseTomlTableHeader(trimmed);
                if (tomlPath != null) {
                    currentTomlPath = new ArrayList<>(Arrays.asList(tomlPath.split("\\.")));
                    currentContextPath.clear();
                    indents.clear();
                    continue;
                }
            }

            int indent = getIndent(line);

            // Pop context entries that are at the same or deeper indentation
            if (format == FileFormat.TOML) {
                // TOML does not use indentation for hierarchy. 
                // All keys belong directly to the current table.
                indents.clear();
                currentContextPath.clear();
            } else {
                while (!indents.isEmpty() && indents.get(indents.size() - 1) >= indent) {
                    indents.remove(indents.size() - 1);
                    currentContextPath.remove(currentContextPath.size() - 1);
                }
            }

            String key = extractKey(trimmed, format);
            if (key != null) {
                List<String> fullPath = new ArrayList<>(currentTomlPath.size() + currentContextPath.size() + 1);
                fullPath.addAll(currentTomlPath);
                fullPath.addAll(currentContextPath);
                fullPath.add(key);

                if (fullPath.equals(targetPath)) {
                    lines.set(i, replaceValue(line, key, newValue));
                    return String.join("\n", lines);
                }

                indents.add(indent);
                currentContextPath.add(key);
            }
        }

        return content;
    }

    /**
     * Convenience overload that determines the {@link FileFormat} from a file name or path string.
     *
     * @param content  file content
     * @param keyPath  dot‑separated key path
     * @param newValue new value to set
     * @param fileName name or relative path of the configuration file (e.g. "config.toml")
     * @return the updated content string
     */
    public static String update(String content, String keyPath, String newValue, String fileName) {
        FileFormat format = FileFormat.fromPath(fileName);
        return update(content, keyPath, newValue, format);
    }

    /**
     * Attempts to parse a TOML table header from a trimmed line.
     *
     * @return the dot-separated path (e.g., "server.experimental"), or null if not a valid header
     */
    private static String parseTomlTableHeader(String trimmed) {
        Matcher m = TOML_TABLE_HEADER.matcher(trimmed);
        if (!m.matches()) return null;

        String path = m.group(1).trim();
        // Validate the path contains only valid TOML key characters (reject JSON array values)
        if (!TOML_PATH_VALIDATOR.matcher(path).matches()) return null;

        return path;
    }

    private static int getIndent(String line) {
        int count = 0;
        while (count < line.length() && (line.charAt(count) == ' ' || line.charAt(count) == '\t')) {
            count++;
        }
        return count;
    }

    /**
     * Extracts the key name from a trimmed line.
     * Handles quoted keys ("key", 'key') and unquoted keys.
     * Skips structural characters ({, }, [, ]) for formats that use them.
     *
     * @return the key name, or null if the line doesn't contain a key-value pair
     */
    private static String extractKey(String trimmed, FileFormat format) {
        // Skip structural brace/bracket characters for JSON and HOCON
        if (format.hasStructuralBraces()) {
            if (trimmed.startsWith("{") || trimmed.startsWith("}") ||
                trimmed.startsWith("[") || trimmed.startsWith("]")) {
                return null;
            }
        }

        // Try quoted key first ("key": value or 'key' = value)
        Matcher quotedMatcher = QUOTED_KEY_PATTERN.matcher(trimmed);
        if (quotedMatcher.find()) {
            return quotedMatcher.group(2);
        }

        // Unquoted key: find first ':' or '='
        int colonIndex = trimmed.indexOf(':');
        int eqIndex = trimmed.indexOf('=');
        int sepIndex;

        if (colonIndex != -1 && eqIndex != -1) {
            sepIndex = Math.min(colonIndex, eqIndex);
        } else if (colonIndex != -1) {
            sepIndex = colonIndex;
        } else if (eqIndex != -1) {
            sepIndex = eqIndex;
        } else {
            return null;
        }

        String key = trimmed.substring(0, sepIndex).trim();
        if (key.isEmpty()) return null;

        return key;
    }

    /**
     * Replaces the value portion of a key-value line, preserving the key prefix,
     * trailing commas, and inline comments.
     * Correctly handles quoted string values that contain '#' or ',' characters.
     */
    private static String replaceValue(String line, String key, String newValue) {
        String keyEsc = Pattern.quote(key);
        // Value group handles three cases via alternation:
        //   1. Double-quoted strings (may contain #, commas, escaped chars)
        //   2. Single-quoted strings (may contain #, commas, escaped chars)
        //   3. Unquoted values (stops at , or #)
        Pattern pattern = Pattern.compile(
            "^([ \\t]*(([\"']?)" + keyEsc + "([\"']?))[ \\t]*[:=][ \\t]*)" +
            "(\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'|[^,#\\n\\r]*)" +
            "(,?)(\\s*#.*)?$"
        );
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String prefix = matcher.group(1);
            String trailingComma = matcher.group(6);
            String comment = matcher.group(7) != null ? matcher.group(7) : "";
            return prefix + formatValue(newValue) + trailingComma + comment;
        }
        return line;
    }

    static String formatValue(String value) {
        if (value == null) return "null";
        if (value.isEmpty()) return "''";

        boolean containsSpecial = value.contains(" ") || value.contains("#") ||
                                value.contains(":") || value.contains("=") ||
                                value.contains("\"") || value.contains("'");

        boolean startsWithSpecial = false;
        if (!value.isEmpty()) {
            char first = value.charAt(0);
            String specialStarts = "!&*-?{}[],#|>%@`";
            startsWithSpecial = specialStarts.indexOf(first) != -1;
        }

        if (containsSpecial || startsWithSpecial) {
            boolean isQuoted = (value.startsWith("\"") && value.endsWith("\"")) ||
                             (value.startsWith("'") && value.endsWith("'"));
            if (!isQuoted) {
                return "\"" + value.replace("\"", "\\\"") + "\"";
            }
        }
        return value;
    }
}
