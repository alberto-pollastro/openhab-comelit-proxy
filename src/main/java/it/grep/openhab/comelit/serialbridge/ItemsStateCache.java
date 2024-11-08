package it.grep.openhab.comelit.serialbridge;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.grep.openhab.comelit.config.CacheConfig;
import it.grep.openhab.comelit.config.Config;
import it.grep.openhab.comelit.config.MainConfig;
import it.grep.openhab.comelit.config.SerialBridgeConfig;
import it.grep.openhab.comelit.proxy.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class ItemsStateCache {
    private static ItemsStateCache instance;
    private LoadingCache<String, Optional<ItemsState>> cache;

    private final static Logger logger = LogManager.getLogger(ItemsStateCache.class.getName());

    private ItemsStateCache() {
    }

    public void init() {

        MainConfig mainConfig = Config.getInstance().getConfig();
        CacheConfig cacheConfig = mainConfig.getCacheConfig();
        SerialBridgeConfig serialBridgeConfig = mainConfig.getSerialBridgeConfig();
        cache = CacheBuilder.newBuilder()
                //.maximumSize(100)
                .expireAfterWrite(cacheConfig.getExpireMillis(5000), TimeUnit.MILLISECONDS)
                .build(
                        new CacheLoader<String, Optional<ItemsState>>() {
                            @Override
                            public Optional<ItemsState> load(String type) {
                                ItemsState is = null;
                                try {
                                    is = new SerialBridgeAPI(serialBridgeConfig).getItemsState(type);
                                } catch (Exception ex) {
                                    logger.error("Something went wrong: {}", ex.getMessage());
                                }
                                if (is != null) {
                                    return Optional.of(is);
                                } else {
                                    return Optional.absent();
                                }
                            }
                        }
                );
    }

    public static ItemsStateCache getInstance() {
        if (instance == null) {
            synchronized (ItemsStateCache.class) {
                if (instance == null) {
                    instance = new ItemsStateCache();
                }
            }
        }
        return instance;
    }

    public ItemsState getClientState(String type) {
        return cache.getUnchecked(type).orNull();
    }

    public ItemsState getCurrentClientState(String type) {
        cache.invalidate(type);
        return cache.getUnchecked(type).orNull();
    }

    public void invalidateClientState(String type) {
        cache.invalidate(type);
    }
}
