package com.grishberg.interfaces;

/**
 * Created by g on 13.08.15.
 */
public interface IPlayer {
    void playContent(int id, int episode, String studio, int startSec, String accessToken);
    void playStream(int idStream, int startSec, String accessToken);
}
