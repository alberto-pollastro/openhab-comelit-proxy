package it.grep.openhab.comelit.config;

public class SerialBridgeConfig {

    private String url;
    private boolean trustAll;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public SerialBridgeConfig(String url) {
        this.url = url;
    }

    public SerialBridgeConfig() {
        this.url = "http://192.168.1.244/user";
        this.trustAll = false;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }
}
