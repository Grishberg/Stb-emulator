package com.grishberg.stb;
// JSON-RPC 2.0 protocol messages

import com.google.gson.Gson;
import com.grishberg.data.api.MqServer;
import com.grishberg.data.model.MqOutMessage;
import com.grishberg.data.model.MqPolicyResponse;
import com.grishberg.data.model.PairingInfo;
import com.grishberg.data.model.QueueInfo;
import com.grishberg.data.rest.RestConst;
import com.grishberg.interfaces.*;

// The JSON-RPC 2.0 server framework package
import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.server.*;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.zip.CRC32;

import com.grishberg.framework.*;

/**
 * Created by g on 13.08.15.
 */
public class Stb implements MqServer.IMqObserver, ITokenLObserver, IPlayerObserver {
    private static final String DIGITS_DEC = "0123456789";
    private static final String DIGITS_HEX = "0123456789abcdef";
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
    private IView mView;
    private QueueInfo mLastQueue;
    private ILogger mLogger;

    public Stb(IView view, ILogger logger) {
        mLogger = logger;
        readSettings();
        mPairing = new Pairing(this);
        mPlayer = new Player(view, mPairing, this, mLogger);
        mInput = new Input(mPlayer, mPairing, mLogger);
        mToken = null;
        mPolicyService = buildRestAdapter().create(LinkingPolicyService.class);
        mDispatcher = new Dispatcher();
        mDispatcher.register(mInput);
        mDispatcher.register(mPlayer);
        mDispatcher.register(mPairing);
        mView = view;
    }

    public void release() {
        if (mMqServer != null) {
            mMqServer.release();
        }
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
        mMqServer = new MqServer(mId, mHost, mMac, this, mLogger);
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
                if (mView != null) {
                    mView.onRegistered(mKey);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                System.out.println("error register device");
            }
        });
    }

    @Override
    public void onDeviceConnected(String name) {
        mView.onDeviceConnected(name);
    }

    /**
     * receive message from mobile device event
     *
     * @param queue client's queue name for reply
     * @param msg
     * @return
     */
    @Override
    public JSONRPC2Response onMessage(QueueInfo queue, String msg) {
        System.out.println("on new message msg = " + msg);
        mLastQueue = queue;
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

    @Override
    public void onNotify(String msg) {
        mMqServer.sendMqMessage(new MqOutMessage(mLastQueue.getName(), msg, mLastQueue.getCorrelationId()));
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

    private boolean readSettings() {
        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(cwd+"/config.txt");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            mMac = prop.getProperty("mac");
            mId = prop.getProperty("device_id");
            mLogger.log("read mac from config");
            mLogger.log("    mac = " + mMac);
            mLogger.log("    dev_id = " + mId);
            if (mMac == null || mMac.length() == 0) {
                mMac = generateMac();
            }
            if (mId == null || mId.length() == 0){
                mId = generateId();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            mMac = generateMac();
            mId = generateId();
            mLogger.log("can't read config.properties, generating mac and dev_id");
            mLogger.log("    mac = " + mMac);
            mLogger.log("    dev_id = " + mId);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private static String generateMac() {
        StringBuilder builder = new StringBuilder("a8f94b20");
        final int N = DIGITS_HEX.length();
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            builder.append(DIGITS_HEX.charAt(r.nextInt(N)));
        }
        return builder.toString();
    }

    private static String generateId() {
        StringBuilder builder = new StringBuilder();
        final int N = DIGITS_HEX.length();
        Random r = new Random();
        for (int i = 0; i < 32; i++) {
            builder.append(DIGITS_HEX.charAt(r.nextInt(N)));
        }
        return builder.toString();
    }

    private static String generateSecretKey() {
        StringBuilder builder = new StringBuilder();
        final int N = DIGITS_DEC.length();

        Random r = new Random();

        for (int i = 0; i < 9; i++) {
            builder.append(DIGITS_DEC.charAt(r.nextInt(N)));
        }
        return builder.toString();
    }

    private static String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }
}
