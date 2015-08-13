package com.grishberg.interfaces;

import com.grishberg.data.model.MqPolicyResponse;
import com.grishberg.data.model.MqRequestDeviceResponse;
import com.grishberg.data.rest.RestConst;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by g on 13.08.15.
 */
public interface LinkingPolicyService {
    @GET(RestConst.MqPolicy.GET_POLICY)
    void getPolicy(Callback<MqPolicyResponse> response);

    @GET(RestConst.MqPolicy.REGISTER_DEVICE)
    MqRequestDeviceResponse requestDevice(@Query(RestConst.field.KEY) String key);

    @GET(RestConst.MqPolicy.REGISTER_DEVICE)
    MqRequestDeviceResponse registerDevice(@Query(RestConst.field.KEY) String key);

}
