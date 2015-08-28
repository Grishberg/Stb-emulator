package com.grishberg.stb;

import com.grishberg.data.rest.RestConst;
import com.grishberg.interfaces.*;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.*;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player implements IPlayer, RequestHandler {
    private static final String COMMAND_PLAY_STREAM = "Player.playStream";
    private static final String COMMAND_PLAY_CONTENT = "Player.playContent";
    private static final String COMMAND_PLAY_YOUTUBE = "Player.playYoutube";
    private static final String COMMAND_GET_STATUS = "Player.getStatus";
    private static final int STATE_NONE = 0;
    private static final int STATE_PLAYING = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_STOPPED = 3;
    private static final int SEEK_DURATION_SEC = 30;

    public enum PlayerState {
        NONE, PLAYING, PAUSED, STOPPED, BUFFERING, ERROR, READY, END_PLAYING
    }

    private MediaPlayer mp;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Duration mDuration;
    private Duration mCurrentPosition;
    private int mDurationSeconds;
    private int mPositionSeconds;
    private int mSeekDurationSec;
    private int mState = 0;
    private String mContentTitle;
    private int mEkId;
    private int mEpId;
    private int mIdStream;
    private String mYoutubeId;
    private IPairing mPairing; // for checking token
    private IPlayerObserver mPlayerObserver;
    private double mVolume = 100;
    private double mPrevVolume = mVolume;
    private double mVolumeDelta = 1;
    private IView mView;
    private boolean mIsPositionChanging;
    private Map<String, Object> mResult;
    private ILogger mLogger;
    private String mType;
    /**
     * test media content
     */
    private static final String[] SRC_CONTENT = {
            "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"
    };
    private static final String[] SRC_CONTENT_STREAM = {
            "http://3.hls.bolshoe.tv/1/streaming/discovery/tvrec/playlist.m3u8"
    };
    /**
     * media content title
     */
    private static final String[] CONTENT_TITLE = {
            "jobs"
    };
    private static final String[] CONTENT_STREAM_TITLE = {
            "discovery channel"
    };

    public Player(IView view, IPairing pairing, IPlayerObserver playerObserver, ILogger logger) {
        mView = view;
        mPairing = pairing;
        mPlayerObserver = playerObserver;
        mLogger = logger;
    }

    /**
     * send notifications to IView and if need to mobile device
     */
    private void onStateChanged() {
        mView.onChangedState(mState);
        JSONRPC2Notification notification = null;
        Map<String, Object> result = new HashMap<>();
        switch (mState) {
            case PLAYING:
                notification = new JSONRPC2Notification(RestConst.Notifications.PLAYER_ON_START);
                break;
            case END_PLAYING:
                notifyOnStop((int) mCurrentPosition.toSeconds(), RestConst.Values.PLAYER_STOP);
                mState = PlayerState.NONE;
                break;
            case ERROR:
                result.put(RestConst.Fields.CODE, 2);
                notification = new JSONRPC2Notification(RestConst.Notifications.PLAYER_ON_ERROR, result);
                mState = PlayerState.NONE;
                break;
        }
        if (notification != null) {
            mPlayerObserver.onNotify(notification.toJSONString());
        }
    }

    private void notifyOnStop(int endSec, String reason) {
        JSONRPC2Notification notification = null;
        Map<String, Object> result = new HashMap<>();
        result.put(RestConst.Fields.TYPE, mType);
        int epId = mEpId;
        String id = getMediaId();
        result.put(RestConst.Fields.ID, id);
        result.put(RestConst.Fields.EP_ID, epId);
        result.put(RestConst.Fields.END_SEC, endSec);
        result.put(RestConst.Fields.REASON, reason);

        mPlayerObserver.onNotify(notification.toJSONString());
    }

    /**
     * create and setup MediaPlayer
     */
    private void initMediaPlayer() {
        mCurrentPosition = Duration.seconds(0);
        updateValues();
        mp.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });

        mp.setVolume(mVolume / 100.0);
        mView.onChangedVolume(mVolume);
        mp.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mp.pause();
                    mState = PlayerState.PAUSED;
                    stopRequested = false;
                } else {
                    mState = PlayerState.PLAYING;
                }
                onStateChanged();
            }
        });
        mp.setOnPaused(new Runnable() {
            public void run() {
                System.out.println("onPaused");
                mState = PlayerState.PAUSED;
                onStateChanged();
            }
        });

        mp.setOnReady(new Runnable() {
            public void run() {
                System.out.println("----------Player onReady");
                mLogger.log("----------Player onReady");
                mDuration = mp.getMedia().getDuration();
                mDurationSeconds = (int) mDuration.toSeconds();
                mSeekDurationSec = mDurationSeconds / SEEK_DURATION_SEC;
                mState = PlayerState.READY;
                onStateChanged();
                updateValues();
            }
        });
        mp.setOnStopped(new Runnable() {
            @Override
            public void run() {
                System.out.println("----------Player onStopped");
                mLogger.log("----------Player onStopped");
                mState = PlayerState.STOPPED;
                onStateChanged();
            }
        });
        mp.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println("----------Player on error");
                mLogger.log("----------Player error");
                mState = PlayerState.ERROR;
                onStateChanged();
            }
        });

        mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                //on end of video
                //TODO: send
                System.out.println("----------Player on end of media");
                mLogger.log("----------Player on end of media");
                stopRequested = true;
                atEndOfMedia = true;
                mState = PlayerState.END_PLAYING;
                onStateChanged();
            }
        });
        mp.statusProperty().addListener(new ChangeListener<MediaPlayer.Status>() {
            @Override
            public void changed(ObservableValue<? extends MediaPlayer.Status> observable
                    , MediaPlayer.Status oldValue, MediaPlayer.Status newValue) {
                System.out.println("----------Player status changed value=" + newValue);
                mLogger.log("----------Player status changed value=" + newValue);
            }
        });
    }

    public void left() {
        if (mp == null) return;
        if (!mIsPositionChanging) {
            mView.onRewindStateChanged(true);
        }
        mIsPositionChanging = true;
        mCurrentPosition = mCurrentPosition.add(Duration.seconds(-SEEK_DURATION_SEC));
        if (mCurrentPosition.lessThan(Duration.seconds(0))) {
            mCurrentPosition = Duration.seconds(0);
        }
        updateValues();
    }

    public void right() {
        if (mp == null) return;
        if (!mIsPositionChanging) {
            mView.onRewindStateChanged(true);
        }
        mIsPositionChanging = true;
        mCurrentPosition = mCurrentPosition.add(Duration.seconds(SEEK_DURATION_SEC));
        if (mCurrentPosition.greaterThan(mDuration)) {
            mCurrentPosition = mDuration;
        }
        updateValues();
    }

    public void doSeek() {
        if (mIsPositionChanging) {
            mView.onRewindStateChanged(false);
        }
        mState = PlayerState.BUFFERING;
        mIsPositionChanging = false;
        if (mp != null) {
            mp.seek(mCurrentPosition);
        }
    }

    public void cancelRewind() {
        mIsPositionChanging = false;
        mView.onRewindStateChanged(false);
    }

    public void mute() {
        if (mp == null) return;
        if (mVolume != 0) {
            mPrevVolume = mVolume;
            mVolume = 0.0;
        } else {
            mVolume = mPrevVolume;
        }
        if (mp != null) {
            mp.setVolume(mVolume / 100.0);
        }
        mView.onChangedVolume(mVolume);
    }

    public void volumeDown() {
        mVolume -= mVolumeDelta;
        if (mVolume < 0) {
            mVolume = 0;
        }
        if (mp != null) {
            mp.setVolume(mVolume / 100.0);
        }
        mView.onChangedVolume(mVolume);
    }

    public void volumeUp() {
        mVolume += mVolumeDelta;
        if (mVolume > 100) {
            mVolume = 100;
        }
        if (mp != null) {
            mp.setVolume(mVolume / 100.0);
        }
        mView.onChangedVolume(mVolume);
    }

    public void stop() {
        if (mp != null) {
            mp.stop();
        }
    }

    public void playPause() {
        MediaPlayer.Status status = mp.getStatus();

        if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
            // don't do anything in these states
            return;
        }

        if (status == MediaPlayer.Status.PAUSED
                || status == MediaPlayer.Status.READY
                || status == MediaPlayer.Status.STOPPED) {
            // rewind the movie if we're sitting at the end
            if (atEndOfMedia) {
                mp.seek(mp.getStartTime());
                atEndOfMedia = false;
            }
            mp.play();
            mState = PlayerState.PLAYING;
        } else {
            mp.pause();
            mState = PlayerState.PAUSED;
        }
    }

    /**
     * update time line
     */
    protected void updateValues() {
        Platform.runLater(new Runnable() {
            public void run() {
                Duration currentTime = mp.getCurrentTime();
                if (!mIsPositionChanging) {
                    if (mState == PlayerState.BUFFERING) {
                        mState = PlayerState.PLAYING;
                        onStateChanged();
                    }
                    mCurrentPosition = currentTime;
                }

                double position = -1;
                String timeCaption = "";
                if (mDuration != null && !mDuration.isUnknown()) {
                    position = mCurrentPosition.divide(mDuration).toMillis() * 100.0;
                    timeCaption = formatTime(mCurrentPosition, mDuration);
                }
                mView.onChangedTimePosition(position, timeCaption);
            }
        });
    }

    @Override
    public void playContent(int id, int episode, String studio, int startSec) {
        mType = RestConst.Values.CONTENT;
        mEkId = id;
        mEpId = episode;
        int index = id % SRC_CONTENT.length;
        String content = SRC_CONTENT[index];
        mContentTitle = CONTENT_TITLE[index];
        //stop old media
        if (mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING) {
            mp.stop();
        }

        Media media = new Media(content);
        mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        initMediaPlayer();
        mView.setMediaPlayer(mp);
    }

    @Override
    public void playStream(int idStream, int startSec) {
        mType = RestConst.Values.STREAM;
        mIdStream = idStream;
        int index = idStream % SRC_CONTENT_STREAM.length;
        String content = SRC_CONTENT_STREAM[index];
        mContentTitle = CONTENT_STREAM_TITLE[index];
        //stop old media
        if (mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING) {
            mp.stop();
        }
        Media media = new Media(content);
        mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        initMediaPlayer();
        mView.setMediaPlayer(mp);
    }

    @Override
    public void playYoutube(String id, int startSec) {
        mType = RestConst.Values.YOUTUBE;
        //stop old media
        if (mp != null && mp.getStatus() == MediaPlayer.Status.PLAYING) {
            mp.stop();
        }
        mYoutubeId = id;
        mContentTitle = "youtube";
        mView.playYouTube(id);
    }

    @Override
    public Map<String, Object> getStatus(String[] params) {
        if (params != null && params.length == 0) {
            params = new String[]{
                    RestConst.Fields.TYPE
                    , RestConst.Fields.ID
                    , RestConst.Fields.EP_ID
                    , RestConst.Fields.TITLE
                    , RestConst.Fields.STATE
                    , RestConst.Fields.POSITION
                    , RestConst.Fields.VOLUME};
        }
        
        Map<String, Object> result = new HashMap<>();
        String id = "";
        for (String parameter : params) {
            switch (parameter) {
                case RestConst.Fields.ID:
                    result.put(parameter, getMediaId());
                    break;
                case RestConst.Fields.POSITION:
                    result.put(parameter, getPosition());
                    break;
                case RestConst.Fields.EP_ID:
                    result.put(parameter, mEpId);
                    break;
                case RestConst.Fields.STATE:
                    result.put(parameter, mState);
                    break;
                case RestConst.Fields.VOLUME:
                    result.put(parameter, mVolume);
                    break;
                case RestConst.Fields.TITLE:
                    result.put(parameter, mContentTitle);
                    break;
            }
        }
        return result;
    }

    private String getMediaId() {
        String id = "";
        switch (mType) {
            case RestConst.Values.CONTENT:
            case RestConst.Values.STREAM:
                id = String.valueOf(mEkId);
                break;
            case RestConst.Values.YOUTUBE:
                id = mYoutubeId;
                break;
        }
        return id;
    }

    private int getPosition() {
        if (mCurrentPosition != null) {
            return (int) mCurrentPosition.toSeconds();
        }
        return -1;
    }

    /**
     * get formatted time
     *
     * @param elapsed
     * @param duration
     * @return
     */
    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
    //--------------- JSONRPC2------------------

    @Override
    public String[] handledRequests() {
        return new String[]{COMMAND_PLAY_CONTENT
                , COMMAND_PLAY_STREAM
                , COMMAND_PLAY_YOUTUBE
                , COMMAND_GET_STATUS
        };
    }

    public void fullscreen() {
        mView.onFullScreen();
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request jsonrpc2Request, MessageContext messageContext) {
        mResult = null;
        List params = (List) jsonrpc2Request.getParams();
        String token = null;
        String youtubeId = null;
        int id;
        int startSec;
        try {

            switch (jsonrpc2Request.getMethod()) {
                case COMMAND_GET_STATUS:
                    mResult = getStatus(params);
                    break;
                case COMMAND_PLAY_YOUTUBE:
                    youtubeId = (String) params.get(0);
                    startSec = (int) ((long) params.get(1));
                    playYoutube(youtubeId, startSec);
                    break;
                case COMMAND_PLAY_CONTENT:
                    //TODO: check token
                    id = Math.abs((int) ((long) params.get(0)));
                    int episode = Math.abs((int) ((long) params.get(1)));
                    String studio = (String) params.get(2);
                    startSec = (int) ((long) params.get(3));
                    playContent(id, episode, studio, startSec);
                    break;

                case COMMAND_PLAY_STREAM:
                    //TODO: check token
                    id = Math.abs((int) ((long) params.get(0)));
                    startSec = Math.abs((int) ((long) params.get(1)));
                    playStream(id, startSec);
                    break;
                default:
                    return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, jsonrpc2Request.getID());
            }
            return new JSONRPC2Response(mResult, jsonrpc2Request.getID());
        } catch (Exception e) {
            e.printStackTrace();
            mLogger.log(e.getMessage());
            return new JSONRPC2Response(new JSONRPC2Error(-1, e.getMessage()), jsonrpc2Request.getID());
        }
    }
}