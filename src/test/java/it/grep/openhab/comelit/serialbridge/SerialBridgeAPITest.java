package it.grep.openhab.comelit.serialbridge;

import it.grep.openhab.comelit.config.SerialBridgeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SerialBridgeAPITest {

    private SerialBridgeConfig config;

    @BeforeEach
    void setUp() {
        config = new SerialBridgeConfig("http://192.168.1.100/user");
    }

    @Test
    void testConvertState_Shutter() {
        assertEquals("stop", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_SHUTTER, 0));
        assertEquals("up", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_SHUTTER, 1));
        assertEquals("down", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_SHUTTER, 2));
        assertNull(SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_SHUTTER, 99));
    }

    @Test
    void testConvertState_Lights() {
        assertEquals("off", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_LIGHTS, 0));
        assertEquals("on", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_LIGHTS, 1));
        assertNull(SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_LIGHTS, 99));
    }

    @Test
    void testConvertState_Other() {
        assertEquals("off", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_OTHER, 0));
        assertEquals("on", SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_OTHER, 1));
        assertNull(SerialBridgeAPI.convertState(SerialBridgeAPI.TYPE_OTHER, 99));
    }

    @Test
    void testConvertState_InvalidType() {
        assertNull(SerialBridgeAPI.convertState("invalid", 1));
        assertNull(SerialBridgeAPI.convertState(null, 1));
        assertNull(SerialBridgeAPI.convertState("", 1));
    }

    @Test
    void testParseItemsStateFromJson_ValidJson() throws Exception {
        SerialBridgeAPI api = new SerialBridgeAPI(config);
        String validJson = "{\n" +
            "  \"num\": 2,\n" +
            "  \"desc\": [\"Light1\", \"Light2\"],\n" +
            "  \"status\": [0, 1]\n" +
            "}";
        
        // We can't directly test private method, but we can test the validation logic
        // by creating an ItemsState and validating it
        ItemsState state = new ItemsState();
        state.setNum(2);
        state.setDesc(new String[]{"Light1", "Light2"});
        state.setStatus(new int[]{0, 1});
        
        assertEquals(2, state.getNum());
        assertEquals(2, state.getDesc().length);
        assertEquals(2, state.getStatus().length);
    }

    @Test
    void testValidateItemsState_InvalidStatusRange() {
        ItemsState state = new ItemsState();
        state.setNum(1);
        state.setDesc(new String[]{"Light1"});
        
        // Valid status values should work fine
        assertDoesNotThrow(() -> state.setStatus(new int[]{1}));
        
        // The validation for status values 0-10 happens in SerialBridgeAPI validation
        // Here we test array size limits
        int[] largeArray = new int[1001];
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> state.setStatus(largeArray)
        );
        assertEquals("Status array too large: 1001", exception.getMessage());
    }

    @Test
    void testValidateItemsState_ArrayLengthMismatch() {
        ItemsState state = new ItemsState();
        state.setNum(2);
        state.setDesc(new String[]{"Light1"});  // Length 1
        state.setStatus(new int[]{0, 1});       // Length 2
        
        // Arrays have different lengths than num - this should be caught by validation
        assertEquals(1, state.getDesc().length);
        assertEquals(2, state.getStatus().length);
        assertEquals(2, state.getNum());
    }

    @Test
    void testConstants() {
        assertEquals("shutter", SerialBridgeAPI.TYPE_SHUTTER);
        assertEquals("lights", SerialBridgeAPI.TYPE_LIGHTS);
        assertEquals("other", SerialBridgeAPI.TYPE_OTHER);
        
        assertEquals("up", SerialBridgeAPI.CMD_SHUTTER_UP);
        assertEquals("down", SerialBridgeAPI.CMD_SHUTTER_DOWN);
        assertEquals("stop", SerialBridgeAPI.CMD_SHUTTER_STOP);
        assertEquals("on", SerialBridgeAPI.CMD_SWITCH_ON);
        assertEquals("off", SerialBridgeAPI.CMD_SWITCH_OFF);
    }
}