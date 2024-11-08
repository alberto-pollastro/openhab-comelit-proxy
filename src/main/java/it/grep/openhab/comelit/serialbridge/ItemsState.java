package it.grep.openhab.comelit.serialbridge;


import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class ItemsState {

    private int num;
    private String[] desc;
    private int[] env;
    private int[] type;
    private int[] val;
    private int[] status;
    @SerializedName("protected")
    private int[] protectedField;
    private String[] envdesc;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String[] getDesc() {
        return desc;
    }

    public void setDesc(String[] desc) {
        this.desc = desc;
    }

    public int[] getEnv() {
        return env;
    }

    public void setEnv(int[] env) {
        this.env = env;
    }

    public int[] getType() {
        return type;
    }

    public void setType(int[] type) {
        this.type = type;
    }

    public int[] getProtectedField() {
        return protectedField;
    }

    public void setProtectedField(int[] protectedField) {
        this.protectedField = protectedField;
    }

    public String[] getEnvdesc() {
        return envdesc;
    }

    public void setEnvdesc(String[] envdesc) {
        this.envdesc = envdesc;
    }

    public int[] getStatus() { return status; }

    public void setStatus(int[] status) { this.status = status; }

    public Integer getStateById(int arrayIndex) {
        if(arrayIndex < 0 || arrayIndex >= this.getNum()) return null;
        return this.getStatus()[arrayIndex];
    }

    public Integer getStateByDesc(String desc) {
        if (desc == null || desc.isEmpty()) return null;
        int arrayIndex = Arrays.asList(this.getDesc()).indexOf(desc);
        return getStateById(arrayIndex);
    }
}
