package com.grishberg.data.model;

import com.google.gson.annotations.SerializedName;
import com.grishberg.data.rest.RestConst;

/**
 * Created by g on 13.08.15.
 */
public class PlayerStatus {
    @SerializedName(RestConst.field.EK_ID)
    private int ekId;
    @SerializedName(RestConst.field.EP_ID)
    private int epId;
    @SerializedName(RestConst.field.EK_TITLE)
    private String ekTitle;
    @SerializedName(RestConst.field.STATE)
    private int state;

    public PlayerStatus(int ekId, int epId, String ekTitle, int state) {
        this.ekId = ekId;
        this.epId = epId;
        this.ekTitle = ekTitle;
        this.state = state;
    }

    public int getEkId() {
        return ekId;
    }

    public int getEpId() {
        return epId;
    }

    public String getEkTitle() {
        return ekTitle;
    }

    public int getState() {
        return state;
    }
}
