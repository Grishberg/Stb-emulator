package com.grishberg.stb;

import com.grishberg.data.model.Profile;
import com.grishberg.interfaces.IPairing;
import com.grishberg.interfaces.ITokenLObserver;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by g on 13.08.15.
 */
public class Pairing implements RequestHandler, IPairing {

    private ITokenLObserver mTokenObserver;
    private Map<String, Profile> mTokens;

    public Pairing(ITokenLObserver observer) {
        mTokens = new HashMap<>();
        mTokenObserver = observer;
    }

    //Зарегистрировать МП на УП
    public String addDevice(int id, String name) {
        String token = mTokenObserver.getLastToken();
        if (token == null) return "";
        Profile profile = new Profile(id, name);
        mTokens.put(token, profile);
        mTokenObserver.registerDevice();
        mTokenObserver.onDeviceConnected(name);
        return "";
    }

    public Profile getProfile(String token) {
        return mTokens.get(token);
    }

    //--------------- JSONRPC2------------------

    @Override
    public String[] handledRequests() {
        return new String[]{"Pairing.addDevice"};
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext messageContext) {
        if (req.getMethod().equals("Pairing.addDevice")) {
            List params = (List) req.getParams();
            try {
                int id = (int) ((long) params.get(0));
                String name = (String) params.get(1);
                return new JSONRPC2Response(addDevice(id,name), req.getID());

            } catch (Exception e){
                System.out.println("rpc error "+e.toString());
                return new JSONRPC2Response(new JSONRPC2Error(-1,"error"), req.getID());
            }
        }
        return null;
    }
}
