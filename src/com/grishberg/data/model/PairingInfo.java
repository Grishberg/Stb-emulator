package com.grishberg.data.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by g on 13.08.15.
 */
public class PairingInfo {
    String key = "";
    String token = "";
    String mac = "";
    long deviceId = 0;

    public PairingInfo(String key, String token, String mac, long deviceId) {
        this.key = key;
        this.token = token;
        this.mac = mac;
        this.deviceId = deviceId;
    }

    public String toJson() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(this);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }
}
