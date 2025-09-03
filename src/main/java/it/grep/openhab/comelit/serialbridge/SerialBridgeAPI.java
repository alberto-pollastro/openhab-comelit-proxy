package it.grep.openhab.comelit.serialbridge;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
        
        ItemsState is = parseItemsStateFromJson(contentStr, type);
        if (is == null) {
            long endTsMillis = System.currentTimeMillis();
            log.error("SB GET {} Failed: Invalid JSON response (took {}ms)", type, endTsMillis - startTsMillis);
            return null;
        }
        
        log.trace("SB GET {} State parsed successfully", type);
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
        if (!isValidUrlInputs(baseUrl, type, item, command)) {
            log.error("Invalid URL inputs: baseUrl={}, type={}, item={}, command={}", 
                baseUrl, type, item, command);
            return null;
        }
        
        if (command == null || command.isEmpty() || command.isBlank()) {
            // It's a state request
            String sanitizedType = sanitizeUrlParameter(type);
            return String.format(STATE_URL_FORMAT, baseUrl, sanitizedType);
        } else {
            // It's an action request
            try {
                int itemId = Integer.parseInt(item);
                if (itemId < 0 || itemId > 999) {
                    log.error("Item ID out of range: {}", itemId);
                    return null;
                }
                Integer commandInt = parseCommand(type, command, itemId);
                if (commandInt == null) return null;
                String sanitizedType = sanitizeUrlParameter(type);
                return String.format(ACTION_URL_FORMAT, baseUrl, sanitizedType, commandInt, itemId);
            } catch (NumberFormatException ex) {
                log.error("Invalid item ID format: {}", item);
                return null;
            }
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

    private ItemsState parseItemsStateFromJson(String json, String type) {
        if (json == null || json.trim().isEmpty()) {
            log.error("Empty JSON content for type: {}", type);
            return null;
        }
        
        if (json.length() > 1024 * 1024) { // 1MB limit
            log.error("JSON response too large: {} bytes for type: {}", json.length(), type);
            return null;
        }
        
        try {
            ItemsState itemsState = new Gson().fromJson(json, ItemsState.class);
            if (validateItemsState(itemsState, type)) {
                return itemsState;
            } else {
                return null;
            }
        } catch (JsonSyntaxException ex) {
            log.error("Invalid JSON syntax for type {}: {}", type, ex.getMessage());
            return null;
        } catch (Exception ex) {
            log.error("Unexpected error parsing JSON for type {}: {}", type, ex.getMessage());
            return null;
        }
    }
    
    private boolean validateItemsState(ItemsState itemsState, String type) {
        if (itemsState == null) {
            log.error("Parsed ItemsState is null for type: {}", type);
            return false;
        }
        
        if (itemsState.getNum() < 0 || itemsState.getNum() > 1000) { // reasonable limit
            log.error("Invalid num value: {} for type: {}", itemsState.getNum(), type);
            return false;
        }
        
        // Validate array lengths match num
        if (itemsState.getDesc() != null && itemsState.getDesc().length != itemsState.getNum()) {
            log.error("Desc array length {} does not match num {} for type: {}", 
                itemsState.getDesc().length, itemsState.getNum(), type);
            return false;
        }
        
        if (itemsState.getStatus() != null && itemsState.getStatus().length != itemsState.getNum()) {
            log.error("Status array length {} does not match num {} for type: {}", 
                itemsState.getStatus().length, itemsState.getNum(), type);
            return false;
        }
        
        // Validate status values are within expected range
        if (itemsState.getStatus() != null) {
            for (int status : itemsState.getStatus()) {
                if (status < 0 || status > 10) { // reasonable range
                    log.error("Invalid status value: {} for type: {}", status, type);
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean isValidUrlInputs(String baseUrl, String type, String item, String command) {
        // Validate base URL
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return false;
        }
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            return false;
        }
        if (baseUrl.length() > 255) { // reasonable limit
            return false;
        }
        
        // Validate type
        if (type == null || (!TYPE_SHUTTER.equals(type) && !TYPE_LIGHTS.equals(type) && !TYPE_OTHER.equals(type))) {
            return false;
        }
        
        // Validate item (if provided)
        if (item != null && !item.isEmpty()) {
            try {
                int itemId = Integer.parseInt(item);
                if (itemId < 0 || itemId > 999) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        
        // Validate command (if provided)
        if (command != null && !command.isEmpty()) {
            if (!CMD_SHUTTER_UP.equals(command) && !CMD_SHUTTER_DOWN.equals(command) && 
                !CMD_SHUTTER_STOP.equals(command) && !CMD_SWITCH_ON.equals(command) && 
                !CMD_SWITCH_OFF.equals(command)) {
                return false;
            }
        }
        
        return true;
    }
    
    private String sanitizeUrlParameter(String param) {
        if (param == null) {
            return "";
        }
        // Remove any characters that could be used for URL injection
        return param.replaceAll("[^a-zA-Z0-9\\-_]", "");
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
