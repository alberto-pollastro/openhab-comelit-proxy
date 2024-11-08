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
        MainConfig mainConfig = Config.getInstance().getConfig();
        SerialBridgeConfig serialBridgeConfig = mainConfig.getSerialBridgeConfig();
        if (!new SerialBridgeAPI(serialBridgeConfig).setItemState(type, id, cmd)) {
            notFound("");
        }
        ItemsStateCache.getInstance().invalidateClientState(type);
        long endTsMillis = System.currentTimeMillis();
        logger.info("CTRL CMD-BY-ID {} {} {} took {}ms", type, id, cmd, endTsMillis - startTsMillis);
        return "\n";
    };

    public static Route statusById = (Request request, Response response) -> {
        long startTsMillis = System.currentTimeMillis();
        String type = request.params(":type");
        String id = request.params(":id");
        ItemsState is = ItemsStateCache.getInstance().getClientState(type);
        Integer state = is.getStateById(Integer.parseInt(id));
        long endTsMillis = System.currentTimeMillis();
        String status = SerialBridgeAPI.convertState(type, state);
        logger.debug("CTRL STS-BY-ID {} {} {} took {}ms", type, id, status, endTsMillis - startTsMillis);
        return status;
    };

    public static Route statusByDesc = (Request request, Response response) -> {
        long startTsMillis = System.currentTimeMillis();
        String type = request.params(":type");
        String desc = request.params(":desc");
        ItemsState is = ItemsStateCache.getInstance().getClientState(type);
        Integer state = is.getStateByDesc(desc);
        long endTsMillis = System.currentTimeMillis();
        String status = SerialBridgeAPI.convertState(type, state);
        logger.debug("CTRL STS-BY-DE {} {} {} took {}ms", type, desc, status, endTsMillis - startTsMillis);
        return status;
    };

    public static Route statusAll = (Request request, Response response) -> {

        long startTsMillis = System.currentTimeMillis();

        String type = request.params(":type");

        try {
            ItemsState is = ItemsStateCache.getInstance().getClientState(type);
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (String d : is.getDesc()) {
                String itemStateString = String.format("%2d - %d - '%s'", index, is.getStatus()[index], d);
                sb.append(itemStateString).append('\n');
                index++;
            }
            sb.append('\n');
            long endTsMillis = System.currentTimeMillis();
            logger.debug("CTRL STS---ALL {} took {}ms", type, endTsMillis - startTsMillis);
            return sb.toString();
        } catch (Exception ex) {
            long endTsMillis = System.currentTimeMillis();
            logger.error("CTRL STS---ALL {} Unable to get status: {} (took {}ms)", type, ex.getMessage(), endTsMillis - startTsMillis);
        }

        halt(404);
        return null;
    };

}
