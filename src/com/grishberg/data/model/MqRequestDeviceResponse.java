package com.grishberg.data.model;


import com.google.gson.annotations.SerializedName;
import com.grishberg.data.rest.RestConst;


/**
 * Created on 31.07.15.
 *
 * @author g
 */
public class MqRequestDeviceResponse {
    @SerializedName(RestConst.responseField.TOKEN)
    private String token;
    @SerializedName(RestConst.responseField.MAC)
    private String mac;
    @SerializedName(RestConst.responseField.DEVICE_ID)
    private String deviceId;
    @SerializedName(RestConst.responseField.ERROR_MESSAGE)
    private String errorMessage;
    @SerializedName(RestConst.responseField.ERROR_CODE)
    private int errorCode;

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
