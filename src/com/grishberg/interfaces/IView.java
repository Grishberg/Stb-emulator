package com.grishberg.interfaces;

import com.grishberg.stb.Player;
import javafx.scene.media.MediaPlayer;

/**
 * Created by g on 14.08.15.
 * видео контейнейр, реагирующий на события плеера
 */
public interface IView {
    void setMediaPlayer(MediaPlayer mediaPlayer);
    void onChangedTimePosition(double currentPosition, String caption);
    void onChangedVolume(double volume);
    void onChangedState(Player.PlayerState state);
    void onRegistered(String secretKey);
    void onFullScreen();
    void onRewindStateChanged(boolean isRewind);
}
