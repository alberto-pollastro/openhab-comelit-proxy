package it.grep.openhab.comelit.config;

/**
 *
 * @author alber
 */
public class MainConfig {

    private int port = 8008;
    private int minThread = 2;
    private int maxThread = 8;
    private int timeOutMillis = 30000;

    private CacheConfig cacheConfig;

    private SerialBridgeConfig serialBridgeConfig;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMinThread() {
        return minThread;
    }

    public void setMinThread(int minThread) {
        this.minThread = minThread;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }

    public int getTimeOutMillis() {
        return timeOutMillis;
    }

    public void setTimeOutMillis(int timeOutMillis) {
        this.timeOutMillis = timeOutMillis;
    }

    public CacheConfig getCacheConfig() {
        if(cacheConfig == null) return new CacheConfig();
        else return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    public SerialBridgeConfig getSerialBridgeConfig() {
        if(serialBridgeConfig == null) return new SerialBridgeConfig();
        else return serialBridgeConfig;
    }

    public void setSerialBridgeConfig(SerialBridgeConfig serialBridgeConfig) {
        this.serialBridgeConfig = serialBridgeConfig;
    }
}
