package org.yuemi.environmentmanager.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnvironmentManagerExpansionTest {

    private EnvironmentManager manager;
    private Method expandMethod;
    private Map<String, String> environmentKeys;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        manager = new EnvironmentManager(null, Logger.getLogger("Test"));
        
        expandMethod = EnvironmentManager.class.getDeclaredMethod("expandVariables", String.class);
        expandMethod.setAccessible(true);
        
        Field envField = EnvironmentManager.class.getDeclaredField("environmentKeys");
        envField.setAccessible(true);
        environmentKeys = (Map<String, String>) envField.get(manager);
    }

    @Test
    void testStandardExpansion() throws Exception {
        environmentKeys.put("HOST", "localhost");
        environmentKeys.put("PORT", "3306");
        
        String input = "${HOST}:${PORT}";
        String result = (String) expandMethod.invoke(manager, input);
        assertEquals("localhost:3306", result);
    }

    @Test
    void testAlternativeExpansion() throws Exception {
        environmentKeys.put("HOST", "127.0.0.1");
        
        String input = "{$HOST}";
        String result = (String) expandMethod.invoke(manager, input);
        assertEquals("127.0.0.1", result);
    }

    @Test
    void testMixedExpansion() throws Exception {
        environmentKeys.put("PROTOCOL", "mysql");
        environmentKeys.put("DB", "testdb");
        
        String input = "{$PROTOCOL}://localhost/${DB}";
        String result = (String) expandMethod.invoke(manager, input);
        assertEquals("mysql://localhost/testdb", result);
    }

    @Test
    void testUnresolvedExpansion() throws Exception {
        String input = "${MISSING}";
        String result = (String) expandMethod.invoke(manager, input);
        assertEquals("${MISSING}", result);
    }

    @Test
    void testResolveMappingValueLiteral() {
        // Not in environmentKeys, should be literal
        String result = manager.resolveMappingValue("MariaDB");
        assertEquals("MariaDB", result);
    }

    @Test
    void testResolveMappingValueVariable() {
        environmentKeys.put("DB_USER", "admin");
        // In environmentKeys, should be variable
        String result = manager.resolveMappingValue("DB_USER");
        assertEquals("admin", result);
    }

    @Test
    void testResolveMappingValueForcedLiteral() {
        environmentKeys.put("DB_USER", "admin");
        // Quoted, should be forced literal even if in environmentKeys
        String resultDouble = manager.resolveMappingValue("\"DB_USER\"");
        assertEquals("DB_USER", resultDouble);
        
        String resultSingle = manager.resolveMappingValue("'DB_USER'");
        assertEquals("DB_USER", resultSingle);
    }
}
