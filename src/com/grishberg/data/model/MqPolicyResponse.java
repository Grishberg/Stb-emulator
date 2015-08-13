package com.grishberg.data.model;

import com.google.gson.annotations.SerializedName;
import com.grishberg.data.rest.RestConst;

import java.util.List;


/**
 * Created on 31.07.15.
 *
 * @author g
 */
public class MqPolicyResponse {
    @SerializedName(RestConst.responseField.EXPIRE)
    private int expire;
    @SerializedName(RestConst.responseField.MQ)
    private List<String> mq;
    @SerializedName(RestConst.responseField.ERROR_MESSAGE)
    private String errorMessage;
    @SerializedName(RestConst.responseField.ERROR_CODE)
    private int errorCode;

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public List<String> getMq() {
        return mq;
    }

    public void setMq(List<String> mq) {
        this.mq = mq;
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
