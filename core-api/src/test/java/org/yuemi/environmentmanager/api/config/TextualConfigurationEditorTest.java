package org.yuemi.environmentmanager.api.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;

public class TextualConfigurationEditorTest {

    private String readResource(String name) throws Exception {
        URL url = getClass().getClassLoader().getResource(name);
        if (url == null) throw new IllegalArgumentException("Resource not found: " + name);
        // Normalize line endings to \n for consistent testing across OSes
        return Files.readString(Paths.get(url.toURI())).replace("\r\n", "\n");
    }

    @Test
    public void testFormatValue() throws Exception {
        Method formatValue = TextualConfigurationEditor.class.getDeclaredMethod("formatValue", String.class);
        formatValue.setAccessible(true);

        // Normal values
        assertEquals("normalValue", formatValue.invoke(null, "normalValue"));
        assertEquals("123456", formatValue.invoke(null, "123456"));

        // Special starting characters (YAML)
        assertEquals("\"@password123\"", formatValue.invoke(null, "@password123"));
        assertEquals("\"!important\"", formatValue.invoke(null, "!important"));
        assertEquals("\"&reference\"", formatValue.invoke(null, "&reference"));
        assertEquals("\"*anchor\"", formatValue.invoke(null, "*anchor"));
        assertEquals("\"- list\"", formatValue.invoke(null, "- list"));
        assertEquals("\"? search\"", formatValue.invoke(null, "? search"));
        assertEquals("\"{key: value}\"", formatValue.invoke(null, "{key: value}"));
        assertEquals("\"[item1, item2]\"", formatValue.invoke(null, "[item1, item2]"));
        assertEquals("\"> block\"", formatValue.invoke(null, "> block"));
        assertEquals("\"| literal\"", formatValue.invoke(null, "| literal"));
        assertEquals("\"% directive\"", formatValue.invoke(null, "% directive"));
        assertEquals("\"`backtick`\"", formatValue.invoke(null, "`backtick`"));

        // Special characters anywhere
        assertEquals("\"value with spaces\"", formatValue.invoke(null, "value with spaces"));
        assertEquals("\"#comment\"", formatValue.invoke(null, "#comment"));
        assertEquals("\"key:value\"", formatValue.invoke(null, "key:value"));
        assertEquals("\"key=value\"", formatValue.invoke(null, "key=value"));
        assertEquals("\"value'with'quote\"", formatValue.invoke(null, "value'with'quote"));
        assertEquals("\"value\\\"with\\\"doublequote\"", formatValue.invoke(null, "value\"with\"doublequote"));

        // Already quoted
        assertEquals("\"already quoted\"", formatValue.invoke(null, "\"already quoted\""));
        assertEquals("'already quoted'", formatValue.invoke(null, "'already quoted'"));
        
        // Null and Empty
        assertEquals("null", formatValue.invoke(null, (Object) null));
        assertEquals("''", formatValue.invoke(null, ""));
    }

    @Test
    public void testUpdateWithSpecialChars() throws Exception {
        String content = readResource("testUpdateWithSpecialChars_input.yml");
        String updated = TextualConfigurationEditor.update(content, "database.password", "@SriwijayaRootDB1234");
        String expected = readResource("testUpdateWithSpecialChars_output.yml");
        assertEquals(expected, updated);
    }

    @Test
    public void testUpdateWithEmptyValue() throws Exception {
        String content = readResource("testUpdateWithEmptyValue_input.yml");
        String updated = TextualConfigurationEditor.update(content, "database.password", "");
        String expected = readResource("testUpdateWithEmptyValue_output.yml");
        assertEquals(expected, updated);
    }

    @Test
    public void testUpdateJsonPreservesTrailingComma() throws Exception {
        String content = readResource("testUpdateJsonPreservesTrailingComma_input.json");
        String updated = TextualConfigurationEditor.update(content, "bindPort", "25565");
        String expected = readResource("testUpdateJsonPreservesTrailingComma_output.json");
        assertEquals(expected, updated);
    }

    @Test
    public void testUpdateJsonLastKeyNoComma() throws Exception {
        String content = readResource("testUpdateJsonLastKeyNoComma_input.json");
        String updated = TextualConfigurationEditor.update(content, "port", "5432");
        String expected = readResource("testUpdateJsonLastKeyNoComma_output.json");
        
        assertEquals(expected, updated);
    }
}
