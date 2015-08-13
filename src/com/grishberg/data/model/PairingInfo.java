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
    String deviceId = "";

    public PairingInfo() {
        key = "";
        token = "";
        mac = "";
        deviceId = "";
    }

    public PairingInfo(String key, String token, String mac, String deviceId) {
        this.key = key;
        this.token = token;
        this.mac = mac;
        this.deviceId = deviceId;
    }

    public String toJson(){
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
