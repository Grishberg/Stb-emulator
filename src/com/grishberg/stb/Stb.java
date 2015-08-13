package com.grishberg.stb;
// JSON-RPC 2.0 protocol messages

import com.google.gson.Gson;
import com.grishberg.data.api.MqServer;
import com.grishberg.data.model.MqPolicyResponse;
import com.grishberg.data.rest.RestConst;
import com.grishberg.interfaces.LinkingPolicyService;

// The JSON-RPC 2.0 server framework package
import com.thetransactioncompany.jsonrpc2.server.*;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.CRC32;

import com.grishberg.framework.*;

/**
 * Created by g on 13.08.15.
 */
public class Stb {
    private static final String TAG = Stb.class.getSimpleName();
    private String mId = "01234567890";
    private String mMac = "77777777777";
    private LinkingPolicyService mPolicyService;
    private Dispatcher mDispatcher;
    private Input mInput;
    private Player mPlayer;
    private MqServer mMqServer;
    private String mHost;


    public Stb(Input input, Player player) {
        mInput = input;
        mPlayer = player;
        mPolicyService = buildRestAdapter().create(LinkingPolicyService.class);
        mDispatcher = new Dispatcher();
        mDispatcher.register(mInput);
        mDispatcher.register(mPlayer);
    }

    public void start() {
        getPolicy();
    }

    private void getPolicy() {
        mPolicyService.getPolicy(new Callback<MqPolicyResponse>() {
            @Override
            public void success(MqPolicyResponse mqPolicyResponse, Response response) {
                onGetPolicy(mqPolicyResponse);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                System.out.println("fail");
            }
        });
    }

    private void onGetPolicy(MqPolicyResponse response) {
        System.out.println("Stb get policy");
        mHost = getMqAddress(response.getMq(), mMac);
        System.out.println("host = " + mHost);
        //mMqServer = new MqServer(mId, mHost, mMac);
    }

    private void connectToMq() {

    }

    public static String getMqAddress(List<String> addresses, String mac) {
        if (addresses == null) return null;
        String md5Mac = FrameworkUtils.md5(mac);
        //your crc class
        ByteBuffer bbuffer = ByteBuffer.allocate(md5Mac.length());
        bbuffer.put(md5Mac.getBytes());

        CRC32 crc = new CRC32();
        crc.update(bbuffer.array());

        long crc32Value = crc.getValue();
        int index = (int) (crc32Value % addresses.size());
        return addresses.get(index);
    }

    private RestAdapter buildRestAdapter() {
        return new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(RestConst.MqPolicy.API)
                .setConverter(new GsonConverter(new Gson()))
                .build();
    }
}
