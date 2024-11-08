package it.grep.openhab.comelit.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
            config = gson.fromJson(new FileReader("/etc/openhab-comelit-proxy.conf"), MainConfig.class);
        } catch (FileNotFoundException ex) {
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

}
