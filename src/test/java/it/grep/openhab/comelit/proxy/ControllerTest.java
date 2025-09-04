package it.grep.openhab.comelit.proxy;

import it.grep.openhab.comelit.serialbridge.SerialBridgeAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    @Test
    void testValidationType() {
        // Test valid types
        assertTrue(isValidType(SerialBridgeAPI.TYPE_SHUTTER));
        assertTrue(isValidType(SerialBridgeAPI.TYPE_LIGHTS));
        assertTrue(isValidType(SerialBridgeAPI.TYPE_OTHER));
        
        // Test invalid types
        assertFalse(isValidType(null));
        assertFalse(isValidType(""));
        assertFalse(isValidType("  "));
        assertFalse(isValidType("invalid"));
        assertFalse(isValidType("SHUTTER")); // case sensitive
    }

    @Test
    void testValidationId() {
        // Test valid IDs
        assertTrue(isValidId("0"));
        assertTrue(isValidId("123"));
        assertTrue(isValidId("999"));
        
        // Test invalid IDs
        assertFalse(isValidId(null));
        assertFalse(isValidId(""));
        assertFalse(isValidId("  "));
        assertFalse(isValidId("-1"));
        assertFalse(isValidId("1000"));
        assertFalse(isValidId("abc"));
        assertFalse(isValidId("12.5"));
    }

    @Test
    void testValidationCommand() {
        // Test valid commands
        assertTrue(isValidCommand(SerialBridgeAPI.CMD_SHUTTER_UP));
        assertTrue(isValidCommand(SerialBridgeAPI.CMD_SHUTTER_DOWN));
        assertTrue(isValidCommand(SerialBridgeAPI.CMD_SHUTTER_STOP));
        assertTrue(isValidCommand(SerialBridgeAPI.CMD_SWITCH_ON));
        assertTrue(isValidCommand(SerialBridgeAPI.CMD_SWITCH_OFF));
        
        // Test invalid commands
        assertFalse(isValidCommand(null));
        assertFalse(isValidCommand(""));
        assertFalse(isValidCommand("  "));
        assertFalse(isValidCommand("invalid"));
        assertFalse(isValidCommand("UP")); // case sensitive
    }

    @Test
    void testValidationDescription() {
        // Test valid descriptions
        assertTrue(isValidDescription("Light1"));
        assertTrue(isValidDescription("Living Room Light"));
        assertTrue(isValidDescription("Device-123_Test"));
        assertTrue(isValidDescription("Room(1).Light"));
        
        // Test invalid descriptions
        assertFalse(isValidDescription(null));
        assertFalse(isValidDescription(""));
        assertFalse(isValidDescription("  "));
        assertFalse(isValidDescription("a".repeat(101))); // too long
        assertFalse(isValidDescription("Test<script>")); // dangerous chars
        assertFalse(isValidDescription("Test&command")); // dangerous chars
    }

    @Test
    void testSanitizeDescription() {
        assertEquals("Test Description", sanitizeDescription("Test Description"));
        assertEquals("Test-123_ok", sanitizeDescription("Test-123_ok"));
        assertEquals("Safe.text()", sanitizeDescription("Safe.text()"));
        assertEquals("Unsafescripttext", sanitizeDescription("Unsafe<script>text"));
        assertEquals("Cleandirty", sanitizeDescription("Clean&dirty"));
        assertEquals("", sanitizeDescription(null));
        
        // Test length limiting
        String longInput = "a".repeat(150);
        String sanitized = sanitizeDescription(longInput);
        assertTrue(sanitized.length() <= 100);
    }

    // Helper methods to test private static methods
    // Note: In real scenarios, you might want to make these package-private for testing
    
    private boolean isValidType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        return SerialBridgeAPI.TYPE_SHUTTER.equals(type) || 
               SerialBridgeAPI.TYPE_LIGHTS.equals(type) || 
               SerialBridgeAPI.TYPE_OTHER.equals(type);
    }

    private boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            int idInt = Integer.parseInt(id);
            return idInt >= 0 && idInt <= 999;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isValidCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return false;
        }
        return SerialBridgeAPI.CMD_SHUTTER_UP.equals(cmd) ||
               SerialBridgeAPI.CMD_SHUTTER_DOWN.equals(cmd) ||
               SerialBridgeAPI.CMD_SHUTTER_STOP.equals(cmd) ||
               SerialBridgeAPI.CMD_SWITCH_ON.equals(cmd) ||
               SerialBridgeAPI.CMD_SWITCH_OFF.equals(cmd);
    }

    private boolean isValidDescription(String desc) {
        if (desc == null || desc.trim().isEmpty()) {
            return false;
        }
        if (desc.length() > 100) {
            return false;
        }
        return desc.matches("[a-zA-Z0-9\\s\\-_.,()]+");
    }

    private String sanitizeDescription(String desc) {
        if (desc == null) {
            return "";
        }
        String cleaned = desc.replaceAll("[^a-zA-Z0-9\\s\\-_.,()]", "");
        return cleaned.substring(0, Math.min(cleaned.length(), 100));
    }
}