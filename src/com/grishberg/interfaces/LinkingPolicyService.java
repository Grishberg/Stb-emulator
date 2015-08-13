package com.grishberg.interfaces;

import com.grishberg.data.model.MqPolicyResponse;
import com.grishberg.data.model.MqRequestDeviceResponse;
import com.grishberg.data.model.PairingInfo;
import com.grishberg.data.rest.RestConst;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by g on 13.08.15.
 */
public interface LinkingPolicyService {
    @GET(RestConst.MqPolicy.GET_POLICY)
    void getPolicy(Callback<MqPolicyResponse> response);

    @POST(RestConst.MqPolicy.REGISTER_DEVICE)
    void registerDevice(@Body PairingInfo pairingInfo, Callback<Response> responseCallback);

}
