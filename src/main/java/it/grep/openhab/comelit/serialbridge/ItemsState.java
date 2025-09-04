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
        if (num < 0 || num > 1000) { // reasonable limit
            throw new IllegalArgumentException("Invalid num value: " + num);
        }
        this.num = num;
    }

    public String[] getDesc() {
        return desc;
    }

    public void setDesc(String[] desc) {
        if (desc != null && desc.length > 1000) { // reasonable limit
            throw new IllegalArgumentException("Desc array too large: " + desc.length);
        }
        this.desc = desc;
    }

    public int[] getEnv() {
        return env;
    }

    public void setEnv(int[] env) {
        if (env != null && env.length > 1000) { // reasonable limit
            throw new IllegalArgumentException("Env array too large: " + env.length);
        }
        this.env = env;
    }

    public int[] getType() {
        return type;
    }

    public void setType(int[] type) {
        if (type != null && type.length > 1000) { // reasonable limit
            throw new IllegalArgumentException("Type array too large: " + type.length);
        }
        this.type = type;
    }

    public int[] getProtectedField() {
        return protectedField;
    }

    public void setProtectedField(int[] protectedField) {
        if (protectedField != null && protectedField.length > 1000) { // reasonable limit
            throw new IllegalArgumentException("Protected field array too large: " + protectedField.length);
        }
        this.protectedField = protectedField;
    }

    public String[] getEnvdesc() {
        return envdesc;
    }

    public void setEnvdesc(String[] envdesc) {
        if (envdesc != null && envdesc.length > 1000) { // reasonable limit
            throw new IllegalArgumentException("Envdesc array too large: " + envdesc.length);
        }
        this.envdesc = envdesc;
    }

    public int[] getStatus() { 
        return status; 
    }

    public void setStatus(int[] status) { 
        if (status != null && status.length > 1000) { // reasonable limit
            throw new IllegalArgumentException("Status array too large: " + status.length);
        }
        this.status = status; 
    }

    public Integer getStateById(int arrayIndex) {
        if (arrayIndex < 0 || arrayIndex >= this.getNum()) {
            return null;
        }
        
        int[] statusArray = this.getStatus();
        if (statusArray == null || arrayIndex >= statusArray.length) {
            return null;
        }
        
        return statusArray[arrayIndex];
    }

    public Integer getStateByDesc(String desc) {
        if (desc == null || desc.isEmpty()) {
            return null;
        }
        
        String[] descArray = this.getDesc();
        if (descArray == null || descArray.length == 0) {
            return null;
        }
        
        // Safe array search without converting to List
        int arrayIndex = -1;
        for (int i = 0; i < descArray.length; i++) {
            if (desc.equals(descArray[i])) {
                arrayIndex = i;
                break;
            }
        }
        
        if (arrayIndex == -1) {
            return null; // Description not found
        }
        
        return getStateById(arrayIndex);
    }
}
