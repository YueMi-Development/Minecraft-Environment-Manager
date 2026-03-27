package org.yuemi.environmentmanager.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Surgically replaces values in configuration files while preserving all formatting and comments.
 */
public final class TextualConfigurationEditor {

    private TextualConfigurationEditor() {}

    /**
     * Updates a value in the configuration content string.
     *
     * @param content  the original file content
     * @param keyPath  the dot-separated key path (e.g., "database.host")
     * @param newValue the new value to set
     * @return the updated content string, or original if key path not found
     */
    public static String update(String content, String keyPath, String newValue) {
        String[] path = keyPath.split("\\.");
        // Split while preserving empty trailing lines
        List<String> lines = new ArrayList<>(Arrays.asList(content.split("\\R", -1)));
        
        int startLine = 0;
        int endLine = lines.size();
        int currentIndent = -1;

        for (int i = 0; i < path.length; i++) {
            String targetKey = path[i];
            int foundLine = -1;
            
            for (int j = startLine; j < endLine; j++) {
                String line = lines.get(j);
                String trimmed = line.trim();
                
                // Skip comments and empty lines
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;

                int indent = getIndent(line);
                if (currentIndent != -1 && indent <= currentIndent) {
                    // We've moved out of the previous block's scope
                    break;
                }

                if (matchesKey(trimmed, targetKey)) {
                    foundLine = j;
                    currentIndent = indent;
                    break;
                }
            }

            if (foundLine == -1) {
                // Key not found, return original
                return content;
            }

            if (i == path.length - 1) {
                // Last key in path: replace its value
                String originalLine = lines.get(foundLine);
                lines.set(foundLine, replaceValue(originalLine, targetKey, newValue));
            } else {
                // Intermediate key: update range to search within this block
                startLine = foundLine + 1;
                endLine = findBlockEnd(lines, startLine, currentIndent);
            }
        }

        return String.join("\n", lines); // Use \n as standard internal separator
    }

    private static int getIndent(String line) {
        int count = 0;
        while (count < line.length() && (line.charAt(count) == ' ' || line.charAt(count) == '\t')) {
            count++;
        }
        return count;
    }

    private static boolean matchesKey(String trimmed, String targetKey) {
        String quoted = "\"" + targetKey + "\"";
        String singleQuoted = "'" + targetKey + "'";
        
        return trimmed.startsWith(targetKey + ":") || 
               trimmed.startsWith(targetKey + " :") ||
               trimmed.startsWith(targetKey + "=") ||
               trimmed.startsWith(targetKey + " =") ||
               trimmed.startsWith(quoted + ":") ||
               trimmed.startsWith(quoted + " :") ||
               trimmed.startsWith(quoted + "=") ||
               trimmed.startsWith(quoted + " =") ||
               trimmed.startsWith(singleQuoted + ":") ||
               trimmed.startsWith(singleQuoted + " =");
    }

    private static String replaceValue(String line, String key, String newValue) {
        // Regex to isolate key prefix and trailing comments
        // Group 1: Leading spaces and key name with separator
        // Group 4: The value itself (to be replaced)
        // Group 5: Trailing comments
        String keyEsc = Pattern.quote(key);
        Pattern pattern = Pattern.compile("^([ \t]*(([\"']?)" + keyEsc + "([\"']?))[ \t]*[:=][ \t]*)([^#\n\r]*)(.*)$");
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            String prefix = matcher.group(1);
            String comment = matcher.group(6);
            return prefix + formatValue(newValue) + comment;
        }
        return line;
    }
    
    private static String formatValue(String value) {
        // Basic escaping for strings with special characters
        if (value.contains(" ") || value.contains("#") || value.contains(":") || value.contains("=") || value.contains("\"")) {
            if (!value.startsWith("\"")) {
                return "\"" + value.replace("\"", "\\\"") + "\"";
            }
        }
        return value;
    }

    private static int findBlockEnd(List<String> lines, int startLine, int parentIndent) {
        for (int i = startLine; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
            
            if (getIndent(line) <= parentIndent) {
                return i;
            }
        }
        return lines.size();
    }
}
