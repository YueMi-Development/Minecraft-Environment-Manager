package org.yuemi.environmentmanager.api.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.lang.reflect.Method;

public class TextualConfigurationEditorTest {

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
        
        // Null
        assertEquals("null", formatValue.invoke(null, (Object) null));
    }

    @Test
    public void testUpdateWithSpecialChars() {
        String content = "database:\n  host: localhost\n  password: oldpassword";
        String updated = TextualConfigurationEditor.update(content, "database.password", "@SriwijayaRootDB1234");
        
        String expected = "database:\n  host: localhost\n  password: \"@SriwijayaRootDB1234\"";
        assertEquals(expected, updated);
    }
}
