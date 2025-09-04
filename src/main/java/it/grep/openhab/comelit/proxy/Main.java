package it.grep.openhab.comelit.proxy;

import it.grep.openhab.comelit.config.Config;
import it.grep.openhab.comelit.config.MainConfig;
import it.grep.openhab.comelit.serialbridge.ItemsStateCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;

public class Main {

    public static void main(String[] args) {

        String configFilePath = System.getProperty("log4j.configurationFile");
        if(configFilePath == null || configFilePath.isEmpty()) {
            System.setProperty("log4j.configurationFile", "/etc/openhab-comelit-proxy_log.conf");
        }

        Logger logger = LogManager.getLogger(Main.class.getName());

        // Read config
        Config config = Config.getInstance();
        config.init();

        // Init Items State Cache
        ItemsStateCache.getInstance().init();

        // Configure Spark
        MainConfig mainConfig = config.getConfig();
        Spark.port(mainConfig.getPort());
        int maxThreads = mainConfig.getMaxThread();
        int minThreads = mainConfig.getMinThread();
        int timeOutMillis = mainConfig.getTimeOutMillis();
        Spark.threadPool(maxThreads, minThreads, timeOutMillis);

        // Set up routes
        Spark.get("/:type/id/:id/:cmd", Controller.commandById);
        Spark.get("/:type/id/:id", Controller.statusById);
        Spark.get("/:type/desc/:desc", Controller.statusByDesc);
        Spark.get("/:type/all", Controller.statusAll);

    }
}
