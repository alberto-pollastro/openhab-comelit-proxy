package it.grep.openhab.comelit.config;

/**
 *
 * @author alber
 */
public class CacheConfig {

    private int expireMillis;

    public CacheConfig() {
        this.expireMillis = 1000;
    }

    public int getExpireMillis() {
        return expireMillis;
    }

    public int getExpireMillis(int def) {
        if (expireMillis == 0) {
            return def;
        }
        return expireMillis;
    }

    public void setExpireMillis(int expireMillis) {
        this.expireMillis = expireMillis;
    }

}
