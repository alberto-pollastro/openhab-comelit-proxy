package it.grep.openhab.comelit.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainConfigTest {

    private MainConfig mainConfig;

    @BeforeEach
    void setUp() {
        mainConfig = new MainConfig();
    }

    @Test
    void testDefaultValues() {
        assertEquals(8008, mainConfig.getPort());
        assertEquals(2, mainConfig.getMinThread());
        assertEquals(8, mainConfig.getMaxThread());
        assertEquals(30000, mainConfig.getTimeOutMillis());
    }

    @Test
    void testSettersAndGetters() {
        mainConfig.setPort(9090);
        mainConfig.setMinThread(4);
        mainConfig.setMaxThread(16);
        mainConfig.setTimeOutMillis(60000);
        
        assertEquals(9090, mainConfig.getPort());
        assertEquals(4, mainConfig.getMinThread());
        assertEquals(16, mainConfig.getMaxThread());
        assertEquals(60000, mainConfig.getTimeOutMillis());
    }

    @Test
    void testCacheConfigDefaultCreation() {
        CacheConfig cacheConfig = mainConfig.getCacheConfig();
        assertNotNull(cacheConfig);
        
        // Test default cache config
        assertEquals(1000, cacheConfig.getExpireMillis());
    }

    @Test
    void testCacheConfigCustom() {
        CacheConfig customCache = new CacheConfig();
        customCache.setExpireMillis(5000);
        mainConfig.setCacheConfig(customCache);
        
        assertEquals(customCache, mainConfig.getCacheConfig());
        assertEquals(5000, mainConfig.getCacheConfig().getExpireMillis());
    }

    @Test
    void testSerialBridgeConfigDefaultCreation() {
        SerialBridgeConfig sbConfig = mainConfig.getSerialBridgeConfig();
        assertNotNull(sbConfig);
        
        // Test default serial bridge config
        assertEquals("http://192.168.1.244/user", sbConfig.getUrl());
        assertFalse(sbConfig.isTrustAll());
    }

    @Test
    void testSerialBridgeConfigCustom() {
        SerialBridgeConfig customSbConfig = new SerialBridgeConfig("https://192.168.1.100/api");
        customSbConfig.setTrustAll(true);
        mainConfig.setSerialBridgeConfig(customSbConfig);
        
        assertEquals(customSbConfig, mainConfig.getSerialBridgeConfig());
        assertEquals("https://192.168.1.100/api", mainConfig.getSerialBridgeConfig().getUrl());
        assertTrue(mainConfig.getSerialBridgeConfig().isTrustAll());
    }

    @Test
    void testThreadConfigurationLogic() {
        // Test valid thread configuration
        mainConfig.setMinThread(2);
        mainConfig.setMaxThread(8);
        assertTrue(mainConfig.getMinThread() <= mainConfig.getMaxThread());
        
        // Test edge case where min equals max
        mainConfig.setMinThread(4);
        mainConfig.setMaxThread(4);
        assertEquals(mainConfig.getMinThread(), mainConfig.getMaxThread());
    }

    @Test
    void testPortRange() {
        // Test common port numbers
        mainConfig.setPort(80);
        assertEquals(80, mainConfig.getPort());
        
        mainConfig.setPort(8080);
        assertEquals(8080, mainConfig.getPort());
        
        mainConfig.setPort(65535);
        assertEquals(65535, mainConfig.getPort());
    }
}