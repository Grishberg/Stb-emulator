package com.grishberg.stb;
// JSON-RPC 2.0 protocol messages

import com.google.gson.Gson;
import com.grishberg.data.api.MqServer;
import com.grishberg.data.model.MqOutMessage;
import com.grishberg.data.model.MqPolicyResponse;
import com.grishberg.data.model.PairingInfo;
import com.grishberg.data.rest.RestConst;
import com.grishberg.interfaces.ITokenLObserver;
import com.grishberg.interfaces.LinkingPolicyService;

// The JSON-RPC 2.0 server framework package
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.*;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.CRC32;

import com.grishberg.framework.*;

/**
 * Created by g on 13.08.15.
 */
public class Stb implements MqServer.IMqObserver, ITokenLObserver {

    private static final String TAG = Stb.class.getSimpleName();
    private String mId = "01234567890";
    private String mMac;
    private String mKey;
    private LinkingPolicyService mPolicyService;
    private Dispatcher mDispatcher;
    private Input mInput;
    private Player mPlayer;
    private Pairing mPairing;
    private MqServer mMqServer;
    private String mHost;
    private String mToken;
    private IOnRegisteredObserver mRegisteredObserver;

    public Stb(IOnRegisteredObserver observer) {
        mMac = generateMac();
        mId = generateId();
        mPairing = new Pairing(this);
        mInput = new Input(mPlayer, mPairing);
        mPlayer = new Player(mPairing);
        mToken = null;
        mPolicyService = buildRestAdapter().create(LinkingPolicyService.class);
        mDispatcher = new Dispatcher();
        mDispatcher.register(mInput);
        mDispatcher.register(mPlayer);
        mDispatcher.register(mPairing);
        mRegisteredObserver = observer;
    }

    public Player getMediaPlayer() {
        return mPlayer;
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
        mMqServer = new MqServer(mId, mHost, mMac, this);
    }

    @Override
    public void onBoundOk() {
        System.out.println("on bound ok");
        //register device
        registerDevice();
    }

    @Override
    public void registerDevice() {
        mKey = generateSecretKey();
        final String token = generateToken();
        PairingInfo pairingInfo = new PairingInfo(mKey, token, mMac, mId);
        mPolicyService.registerDevice(pairingInfo, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                System.out.println("registered, mac = " + mMac);
                mToken = token;
                if (mRegisteredObserver != null) {
                    mRegisteredObserver.onRegistered(mKey);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                System.out.println("error register device");
            }
        });

    }

    @Override
    public JSONRPC2Response onMessage(String queueName, String msg) {
        System.out.println("on new message msg = " + msg);
        JSONRPC2Request request = null;
        try {

            request = JSONRPC2Request.parse(msg);
        } catch (JSONRPC2ParseException e) {
            System.out.println("parse exception");
        }
        //TODO: send rpc to dispatcher
        if (request != null) {
            return mDispatcher.process(request, null);
        }
        return new JSONRPC2Response(JSONRPC2Error.INTERNAL_ERROR);
    }

    @Override
    public String getLastToken() {
        return mToken;
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

    private static String generateMac() {
        StringBuilder builder = new StringBuilder("a8f94b20");
        final String alphabet = "0123456789abcde";
        final int N = alphabet.length();

        Random r = new Random();

        for (int i = 0; i < 4; i++) {
            builder.append(alphabet.charAt(r.nextInt(N)));
        }
        return builder.toString();
    }

    private static String generateId() {
        StringBuilder builder = new StringBuilder();
        final String alphabet = "0123456789abcde";
        final int N = alphabet.length();

        Random r = new Random();

        for (int i = 0; i < 32; i++) {
            builder.append(alphabet.charAt(r.nextInt(N)));
        }
        return builder.toString();
    }

    private static String generateSecretKey() {
        StringBuilder builder = new StringBuilder();
        final String alphabet = "0123456789";
        final int N = alphabet.length();

        Random r = new Random();

        for (int i = 0; i < 9; i++) {
            builder.append(alphabet.charAt(r.nextInt(N)));
        }
        return builder.toString();
    }

    private static String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public interface IOnRegisteredObserver {
        void onRegistered(String secretKey);
    }
}
