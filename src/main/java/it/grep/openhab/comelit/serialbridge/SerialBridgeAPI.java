package it.grep.openhab.comelit.serialbridge;

import com.google.gson.Gson;
import it.grep.openhab.comelit.config.SerialBridgeConfig;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;


public class SerialBridgeAPI {
    private final static org.apache.logging.log4j.Logger log = LogManager.getLogger(SerialBridgeAPI.class.getName());

    public final static String TYPE_SHUTTER = "shutter";
    public final static String TYPE_LIGHTS = "lights";
    public final static String TYPE_OTHER = "other";

    public final static String CMD_SHUTTER_UP = "up";
    public final static String CMD_SHUTTER_DOWN = "down";
    public final static String CMD_SHUTTER_STOP = "stop";
    public final static String CMD_SWITCH_ON = "on";
    public final static String CMD_SWITCH_OFF = "off";

    public final static String STATUS_SHUTTER_UP = "up";
    public final static String STATUS_SHUTTER_DOWN = "down";
    public final static String STATUS_SHUTTER_IDLE = "stop";
    public final static String STATUS_SWITCH_ON = "on";
    public final static String STATUS_SWITCH_OFF = "off";

    private final SerialBridgeConfig serialBridgeConfig;
    private HttpClient httpClient;

    private final String ACTION_URL_FORMAT = "%s/action.cgi?type=%s&num%d=%d"; // <base-url>, [light,shutter,other], [0,1,2], <item-id>
    private final String STATE_URL_FORMAT = "%s/icon_desc.json?type=%s";       // <base-url>, [light,shutter,other]

    public SerialBridgeAPI(SerialBridgeConfig serialBridgeConfig) throws Exception {
        this.serialBridgeConfig = serialBridgeConfig;
        initHttpClient();
    }

    public boolean setItemState(String type, String id, String state) {
        long startTsMillis = System.currentTimeMillis();
        String url = null;
        try {
            url = composeCompleteUrl(serialBridgeConfig.getUrl(), type, id, state);
            if (url == null) return false;
            log.trace("SB SET {} {} {} GET : {}", type, id, state, url);
            Request request = initRequest(url, HttpMethod.GET);
            ContentResponse response = request.send();
            log.trace("SB SET {} {} {} RES : {}", type, id, state, response.getStatus());
            if (response.getStatus() == 200) {
                long endTsMillis = System.currentTimeMillis();
                log.trace("SB SET {} {} {} took {}ms", type, id, state, endTsMillis-startTsMillis);
                return true;
            } else {
                long endTsMillis = System.currentTimeMillis();
                log.error("SB SET {} {} {} Failed: {} (took {}ms)", type, id, state, response.getStatus(), endTsMillis-startTsMillis);
                return false;
            }
        } catch (Exception ex) {
            long endTsMillis = System.currentTimeMillis();
            log.error("SB SET {} {} {} Failed: {} (took {}ms)", type, id, state, ex.getMessage(), endTsMillis-startTsMillis);
        } finally {
            stopHttpClient();
        }
        return false;
    }

    public ItemsState getItemsState(String type) {

        long startTsMillis = System.currentTimeMillis();
        String contentStr = null;
        String url = null;
        try {
            url = composeCompleteUrl(serialBridgeConfig.getUrl(), type, null, null);
            log.trace("SB GET {} GET : {}", type, url);
            Request request = initRequest(url, HttpMethod.GET);
            ContentResponse response = request.send();
            log.trace("SB GET {} RES : {}", type, response.getStatus());
            if (response.getStatus() == 200) {
                contentStr = response.getContentAsString();
                if (contentStr == null || contentStr.isEmpty()) {
                    log.error("SB GET {} Failed: {}", type, "Empty response");
                }
            } else {
                log.warn("SB GET {} Failed: status {}", type, response.getStatus());
            }
        } catch (Exception ex) {
            log.error("SB GET {} Failed: {}", type, ex.getMessage());
        } finally {
            stopHttpClient();
        }
        if (contentStr == null || contentStr.isEmpty()) {
            long endTsMillis = System.currentTimeMillis();
            log.debug("SB GET {} Failed took {}ms", type, endTsMillis - startTsMillis);
            return null;
        }
        ItemsState is = new Gson().fromJson(contentStr, ItemsState.class);
        log.trace("SB GET {} State: {}", type, new Gson().toJson(is));
        long endTsMillis = System.currentTimeMillis();
        log.debug("SB GET {} took {}ms", type, endTsMillis - startTsMillis);
        return is;
    }

    private void initHttpClient() throws Exception {
        // Instantiate and configure the SslContextFactory
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(serialBridgeConfig.isTrustAll());

        // Instantiate HttpClient with the SslContextFactory
        httpClient = new HttpClient(sslContextFactory);

        // Configure HttpClient, for example:
        httpClient.setFollowRedirects(false);
        httpClient.setConnectTimeout(500);

        // Start HttpClient
        httpClient.start();
    }

    private void stopHttpClient() {
        try {
            // Stop HttpClient
            httpClient.stop();
        } catch (Exception ex) {
        }
    }

    private Request initRequest(String url, HttpMethod method) throws Exception {

        Request request = httpClient.newRequest(url).method(method);
        return request;
    }

    private String composeCompleteUrl(String baseUrl, String type, String item, String command) {
        if (command == null || command.isEmpty() || command.isBlank()) {
            // It's a state request
            return String.format(STATE_URL_FORMAT, baseUrl, type);
        } else {
            // It's an action request
            int itemId = Integer.parseInt(item);
            Integer commandInt = parseCommand(type, command, itemId);
            if (commandInt == null) return null;
            else return String.format(ACTION_URL_FORMAT, baseUrl, type, commandInt, itemId);
        }
    }

    private Integer parseCommand(String type, String cmd, int id) {
        switch (type) {
            case TYPE_SHUTTER:
                switch (cmd) {
                    case CMD_SHUTTER_UP:
                        return 1;
                    case CMD_SHUTTER_DOWN:
                        return 0;
                    case CMD_SHUTTER_STOP:
                        // Virtual command, not directly supported by SimpleHome
                        ItemsState is = ItemsStateCache.getInstance().getCurrentClientState(type);
                        String currentState = convertState(type, is.getStateById(id));
                        switch (currentState) {
                            case STATUS_SHUTTER_DOWN:
                                return 1;
                            case STATUS_SHUTTER_UP:
                                return 0;
                            default:
                                return null;
                        }
                    default:
                        return null;
                }
            case TYPE_LIGHTS:
            case TYPE_OTHER:
                switch (cmd) {
                    case CMD_SWITCH_ON:
                        return 1;
                    case CMD_SWITCH_OFF:
                        return 0;
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    public static String convertState(String type, Integer state) {
        switch (type) {
            case TYPE_SHUTTER:
                switch (state) {
                    case 0:
                        return STATUS_SHUTTER_IDLE;
                    case 1:
                        return STATUS_SHUTTER_UP;
                    case 2:
                        return STATUS_SHUTTER_DOWN;
                    default:
                        return null;
                }
            case TYPE_LIGHTS:
            case TYPE_OTHER:
                switch (state) {
                    case 0:
                        return STATUS_SWITCH_OFF;
                    case 1:
                        return STATUS_SWITCH_ON;
                    default:
                        return null;
                }
            default:
                return null;
        }
    }
}
