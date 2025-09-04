package it.grep.openhab.comelit.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SerialBridgeConfigTest {

    @Test
    void testDefaultConstructor() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        
        assertEquals("http://192.168.1.244/user", config.getUrl());
        assertFalse(config.isTrustAll());
    }

    @Test
    void testParameterizedConstructor() {
        String customUrl = "https://192.168.1.100/api";
        SerialBridgeConfig config = new SerialBridgeConfig(customUrl);
        
        assertEquals(customUrl, config.getUrl());
        assertFalse(config.isTrustAll()); // default value
    }

    @Test
    void testSettersAndGetters() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        
        String newUrl = "https://example.com/bridge";
        config.setUrl(newUrl);
        config.setTrustAll(true);
        
        assertEquals(newUrl, config.getUrl());
        assertTrue(config.isTrustAll());
    }

    @Test
    void testUrlValidation_Http() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        config.setUrl("http://192.168.1.100/user");
        
        assertTrue(config.getUrl().startsWith("http://"));
    }

    @Test
    void testUrlValidation_Https() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        config.setUrl("https://192.168.1.100/user");
        
        assertTrue(config.getUrl().startsWith("https://"));
    }

    @Test
    void testTrustAllToggle() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        
        // Default is false
        assertFalse(config.isTrustAll());
        
        // Toggle to true
        config.setTrustAll(true);
        assertTrue(config.isTrustAll());
        
        // Toggle back to false
        config.setTrustAll(false);
        assertFalse(config.isTrustAll());
    }

    @Test
    void testNullUrl() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        config.setUrl(null);
        
        assertNull(config.getUrl());
    }

    @Test
    void testEmptyUrl() {
        SerialBridgeConfig config = new SerialBridgeConfig();
        config.setUrl("");
        
        assertEquals("", config.getUrl());
    }
}