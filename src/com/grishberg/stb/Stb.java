package com.grishberg.stb;
// JSON-RPC 2.0 protocol messages

import com.thetransactioncompany.jsonrpc2.*;

// The JSON-RPC 2.0 server framework package
import com.thetransactioncompany.jsonrpc2.server.*;

/**
 * Created by g on 13.08.15.
 */
public class Stb {
    private String mId = "01234567890";
    private String mMac;
    private Dispatcher mDispatcher;

    public Stb() {
        mDispatcher = new Dispatcher();
        dispatcher.register(new EchoHandler());
    }

}
