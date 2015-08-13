package com.grishberg.stb;

import com.grishberg.interfaces.IInput;
import com.grishberg.interfaces.IPairing;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by g on 13.08.15.
 */
public class Input implements IInput , RequestHandler {
    private Player mPlayer;
    private IPairing mParing;
    private final String COMMAND_MUTE = "mute";
    private final String COMMAND_SUBS = "subtitle";
    private final String COMMAND_PLAY_PAUSE = "playPause";
    private final String COMMAND_VOLUME_DOWN =  "volumeDown";
    private final String COMMAND_VOLUME_UP = "volumeUp";
    private final String COMMAND_SELECT = "select";
    private final String COMMAND_RIGHT = "right";
    private final String COMMAND_LEFT = "left";
    private final String COMMAND_HOME = "home";
    private final String COMMAND_UP= "up";
    private final String COMMAND_AUDIO = "audio";
    private final String COMMAND_FULLSCREEN = "fullscreen";
    private final String COMMAND_PREV = "prev";
    private final String COMMAND_NEXT = "next";
    private final String COMMAND_MENU = "menu";
    private String mResultRPC;

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
    public JSONRPC2Response process(JSONRPC2Request req, MessageContext messageContext) {
        switch (req.getMethod()){
            case COMMAND_AUDIO:  audio();break;
            case COMMAND_FULLSCREEN: fullscreen();break;
            case COMMAND_HOME: home();break;
            case COMMAND_LEFT: left();break;
            case COMMAND_MENU: menu();break;
            case COMMAND_MUTE: mute();break;
            case COMMAND_NEXT: next();break;
            case COMMAND_PLAY_PAUSE: playPause();break;
            case COMMAND_PREV: prev();break;
            case COMMAND_RIGHT: right();break;
            case COMMAND_SELECT: select();break;
            case COMMAND_SUBS: subtitle();break;
            case COMMAND_UP: up();break;
            case COMMAND_VOLUME_DOWN: volumeDown();break;
            case COMMAND_VOLUME_UP: volumeUp();break;
            default:  return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
        return new JSONRPC2Response(mResultRPC, req.getID());
    }
}
