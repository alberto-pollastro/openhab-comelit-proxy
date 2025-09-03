package it.grep.openhab.comelit.proxy;

import it.grep.openhab.comelit.config.Config;
import it.grep.openhab.comelit.config.MainConfig;
import it.grep.openhab.comelit.config.SerialBridgeConfig;
import it.grep.openhab.comelit.serialbridge.ItemsState;
import it.grep.openhab.comelit.serialbridge.ItemsStateCache;
import it.grep.openhab.comelit.serialbridge.SerialBridgeAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.*;

public class Controller {

    private final static Logger logger = LogManager.getLogger(Controller.class.getName());

    public static Route commandById = (Request request, Response response) -> {
        long startTsMillis = System.currentTimeMillis();
        String type = request.params(":type");
        String id = request.params(":id");
        String cmd = request.params(":cmd");
        
        if (!isValidType(type) || !isValidId(id) || !isValidCommand(cmd)) {
            halt(400, "Invalid parameters");
            return null;
        }
        
        try {
            MainConfig mainConfig = Config.getInstance().getConfig();
            SerialBridgeConfig serialBridgeConfig = mainConfig.getSerialBridgeConfig();
            if (!new SerialBridgeAPI(serialBridgeConfig).setItemState(type, id, cmd)) {
                halt(404, "Command failed");
                return null;
            }
            ItemsStateCache.getInstance().invalidateClientState(type);
            long endTsMillis = System.currentTimeMillis();
            logger.info("CTRL CMD-BY-ID {} {} {} took {}ms", type, id, cmd, endTsMillis - startTsMillis);
            return "\n";
        } catch (Exception ex) {
            long endTsMillis = System.currentTimeMillis();
            logger.error("CTRL CMD-BY-ID {} {} {} Failed: {} (took {}ms)", type, id, cmd, ex.getMessage(), endTsMillis - startTsMillis);
            halt(500, "Internal server error");
            return null;
        }
    };

    public static Route statusById = (Request request, Response response) -> {
        long startTsMillis = System.currentTimeMillis();
        String type = request.params(":type");
        String id = request.params(":id");
        
        if (!isValidType(type) || !isValidId(id)) {
            halt(400, "Invalid parameters");
            return null;
        }
        
        try {
            ItemsState is = ItemsStateCache.getInstance().getClientState(type);
            if (is == null) {
                halt(404, "Type not found");
                return null;
            }
            Integer state = is.getStateById(Integer.parseInt(id));
            if (state == null) {
                halt(404, "ID not found");
                return null;
            }
            long endTsMillis = System.currentTimeMillis();
            String status = SerialBridgeAPI.convertState(type, state);
            logger.debug("CTRL STS-BY-ID {} {} {} took {}ms", type, id, status, endTsMillis - startTsMillis);
            return status;
        } catch (NumberFormatException ex) {
            logger.warn("Invalid ID format: {}", id);
            halt(400, "Invalid ID format");
            return null;
        }
    };

    public static Route statusByDesc = (Request request, Response response) -> {
        long startTsMillis = System.currentTimeMillis();
        String type = request.params(":type");
        String desc = request.params(":desc");
        
        if (!isValidType(type) || !isValidDescription(desc)) {
            halt(400, "Invalid parameters");
            return null;
        }
        
        try {
            ItemsState is = ItemsStateCache.getInstance().getClientState(type);
            if (is == null) {
                halt(404, "Type not found");
                return null;
            }
            Integer state = is.getStateByDesc(desc);
            if (state == null) {
                halt(404, "Description not found");
                return null;
            }
            long endTsMillis = System.currentTimeMillis();
            String status = SerialBridgeAPI.convertState(type, state);
            logger.debug("CTRL STS-BY-DE {} {} {} took {}ms", type, desc, status, endTsMillis - startTsMillis);
            return status;
        } catch (Exception ex) {
            long endTsMillis = System.currentTimeMillis();
            logger.error("CTRL STS-BY-DE {} {} Failed: {} (took {}ms)", type, desc, ex.getMessage(), endTsMillis - startTsMillis);
            halt(500, "Internal server error");
            return null;
        }
    };

    public static Route statusAll = (Request request, Response response) -> {
        long startTsMillis = System.currentTimeMillis();
        String type = request.params(":type");
        
        if (!isValidType(type)) {
            halt(400, "Invalid type parameter");
            return null;
        }

        try {
            ItemsState is = ItemsStateCache.getInstance().getClientState(type);
            if (is == null) {
                halt(404, "Type not found");
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            String[] descriptions = is.getDesc();
            int[] statuses = is.getStatus();
            
            if (descriptions == null || statuses == null) {
                halt(404, "No data available");
                return null;
            }
            
            // Ensure arrays are consistent and within safe bounds
            int maxLength = Math.min(descriptions.length, statuses.length);
            maxLength = Math.min(maxLength, is.getNum());
            maxLength = Math.min(maxLength, 1000); // absolute safety limit
            
            for (int index = 0; index < maxLength; index++) {
                if (index >= descriptions.length || index >= statuses.length) {
                    break; // Extra safety check
                }
                
                String desc = descriptions[index];
                if (desc == null) {
                    desc = ""; // Handle null descriptions
                }
                
                String sanitizedDesc = sanitizeDescription(desc);
                String itemStateString = String.format("%2d - %d - '%s'", index, statuses[index], sanitizedDesc);
                sb.append(itemStateString).append('\n');
            }
            sb.append('\n');
            long endTsMillis = System.currentTimeMillis();
            logger.debug("CTRL STS---ALL {} took {}ms", type, endTsMillis - startTsMillis);
            return sb.toString();
        } catch (Exception ex) {
            long endTsMillis = System.currentTimeMillis();
            logger.error("CTRL STS---ALL {} Unable to get status: {} (took {}ms)", type, ex.getMessage(), endTsMillis - startTsMillis);
            halt(500, "Internal server error");
            return null;
        }
    };

    private static boolean isValidType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        return SerialBridgeAPI.TYPE_SHUTTER.equals(type) || 
               SerialBridgeAPI.TYPE_LIGHTS.equals(type) || 
               SerialBridgeAPI.TYPE_OTHER.equals(type);
    }

    private static boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        try {
            int idInt = Integer.parseInt(id);
            return idInt >= 0 && idInt <= 999; // reasonable range
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean isValidCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return false;
        }
        return SerialBridgeAPI.CMD_SHUTTER_UP.equals(cmd) ||
               SerialBridgeAPI.CMD_SHUTTER_DOWN.equals(cmd) ||
               SerialBridgeAPI.CMD_SHUTTER_STOP.equals(cmd) ||
               SerialBridgeAPI.CMD_SWITCH_ON.equals(cmd) ||
               SerialBridgeAPI.CMD_SWITCH_OFF.equals(cmd);
    }

    private static boolean isValidDescription(String desc) {
        if (desc == null || desc.trim().isEmpty()) {
            return false;
        }
        if (desc.length() > 100) { // reasonable limit
            return false;
        }
        // Allow only alphanumeric, spaces, and common punctuation
        return desc.matches("[a-zA-Z0-9\\s\\-_.,()]+");
    }

    private static String sanitizeDescription(String desc) {
        if (desc == null) {
            return "";
        }
        // Remove any potentially dangerous characters and limit length
        return desc.replaceAll("[^a-zA-Z0-9\\s\\-_.,()]", "")
                  .substring(0, Math.min(desc.length(), 100));
    }

}
