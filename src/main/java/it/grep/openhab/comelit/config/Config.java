package it.grep.openhab.comelit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author alber
 */
public class Config {

    private static Config instance;

    private MainConfig config;

    private final String DEFAULT_ConfigString = "{}";
    private Logger log;

    private Config() {
    }

    public void init() {

        log = LogManager.getLogger(Config.class.getName());

        Gson gson = new Gson();
        try {
            String configContent = readConfigFile("/etc/openhab-comelit-proxy.conf");
            if (configContent != null && isValidJson(configContent)) {
                config = gson.fromJson(configContent, MainConfig.class);
                validateConfig(config);
            } else {
                throw new RuntimeException("Invalid configuration file");
            }
        } catch (Exception ex) {
            log.warn("Failed to load config file: {}", ex.getMessage());
            config = gson.fromJson(DEFAULT_ConfigString, MainConfig.class);
            log.error("Using default config: {}", gson.toJson(config));
        }

        SerialBridgeConfig sbConfig = config.getSerialBridgeConfig();
        if (sbConfig == null) {
            config.setSerialBridgeConfig(new SerialBridgeConfig());
        }

        String prettyConfig = new GsonBuilder().setPrettyPrinting().create().toJson(config);
        System.err.println("Running config: \n" + prettyConfig);

        log.info("Running config: \n{}", gson.toJson(config));
    }

    public static Config getInstance() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    public MainConfig getConfig() {
        return config;
    }

    public void setConfig(MainConfig config) {
        this.config = config;
    }

    private String readConfigFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Config file not found: " + filePath);
        }
        
        if (Files.size(path) > 1024 * 1024) { // 1MB limit
            throw new IOException("Config file too large: " + Files.size(path) + " bytes");
        }
        
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            new Gson().fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException ex) {
            log.error("Invalid JSON syntax: {}", ex.getMessage());
            return false;
        }
    }

    private void validateConfig(MainConfig config) throws RuntimeException {
        if (config == null) {
            throw new RuntimeException("Config cannot be null");
        }
        
        if (config.getPort() < 1 || config.getPort() > 65535) {
            throw new RuntimeException("Invalid port number: " + config.getPort());
        }
        
        if (config.getMinThread() < 1 || config.getMaxThread() < 1 || 
            config.getMinThread() > config.getMaxThread()) {
            throw new RuntimeException("Invalid thread configuration");
        }
        
        if (config.getTimeOutMillis() < 0) {
            throw new RuntimeException("Invalid timeout value: " + config.getTimeOutMillis());
        }
        
        SerialBridgeConfig sbConfig = config.getSerialBridgeConfig();
        if (sbConfig != null && sbConfig.getUrl() != null) {
            String url = sbConfig.getUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new RuntimeException("Invalid SerialBridge URL: " + url);
            }
        }
    }

}
