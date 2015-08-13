package com.grishberg.stb;

import com.grishberg.interfaces.IInput;
import com.grishberg.interfaces.IPairing;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

/**
 * Created by g on 13.08.15.
 */
public class Input implements IInput , RequestHandler {
    private Player mPlayer;
    private IPairing mParing;

    public Input(Player player,IPairing pairing) {
        mPlayer = player;
        mParing = pairing;
    }

    @Override
    public void back() {

    }

    @Override
    public void up() {

    }

    @Override
    public void down() {

    }

    @Override
    public void home() {

    }

    @Override
    public void left() {

    }

    @Override
    public void right() {

    }

    @Override
    public void select() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void volumeUp() {

    }

    @Override
    public void volumeDown() {

    }

    @Override
    public void playPause() {

    }

    @Override
    public void subtitle() {

    }

    @Override
    public void mute() {

    }

    @Override
    public void audio() {

    }

    @Override
    public void fullscreen() {

    }

    @Override
    public void prev() {

    }

    @Override
    public void next() {

    }

    @Override
    public void menu() {

    }

    //--------------- JSONRPC2------------------

    @Override
    public String[] handledRequests() {
        return new String[]{"back", "up", "down", "home", "left", "right", "select", "stop","volumeUp",
        "volumeDown", "playPause", "subtitle", "mute", "audio", "fullscreen", "prev" , "next" , "menu"};
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request jsonrpc2Request, MessageContext messageContext) {
        return null;
    }
}
