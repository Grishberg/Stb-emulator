package com.grishberg.interfaces;

import com.grishberg.data.model.PlayerStatus;

/**
 * Created by g on 13.08.15.
 */
public interface IPlayer {
    void playContent(String id, int episode, String studio, int startSec);
    void playStream(String idStream, int startSec);
    PlayerStatus getStatus();
}
