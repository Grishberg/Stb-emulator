package com.grishberg.interfaces;

/**
 * Created by g on 13.08.15.
 */
public interface IInput {
    void back();
    void up(boolean state);
    void down(boolean state);
    void home();
    void left(boolean state);
    void right(boolean state);
    void select();
    void stop();
    void volumeUp();
    void volumeDown();
    void playPause();
    void subtitle();
    void mute();
    void audio();
    void fullscreen();
    void prev();
    void next();
    void menu();
}
