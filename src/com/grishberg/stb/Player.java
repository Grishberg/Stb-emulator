package com.grishberg.stb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.grishberg.data.model.PlayerStatus;
import com.grishberg.data.rest.RestConst;
import com.grishberg.interfaces.IPairing;
import com.grishberg.interfaces.IPlayer;
import com.grishberg.interfaces.IPlayerObserver;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.*;
import javafx.util.Duration;

import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends BorderPane implements IPlayer, RequestHandler {

    private static final String COMMAND_PLAY_STREAM = "Player.playStream";
    private static final String COMMAND_PLAY_CONTENT = "Player.playContent";
    private static final String COMMAND_GET_STATUS = "Player.getStatus";

    private static final String NOTIFY_END_PLAYING = "onPlayEndNotify";
    private static final String NOTIFY_START_PLAYING = "onStartPlaying";

    private enum PlayerState {
        NONE, PLAYING, PAUSED, STOPPED
    }

    private MediaPlayer mp;
    private MediaView mMediaView;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Duration duration;
    private Slider timeSlider;
    private Label playTime;
    private Slider volumeSlider;
    private HBox mediaBar;

    private PlayerState mState = PlayerState.NONE;
    private String mContentTitle;
    private int mEkId;
    private int mEpId;
    private int mIdStream;
    private IPairing mPairing;
    private IPlayerObserver mPlayerObserver;
    private double mVolume = 100;
    private double mVolumeDelta = 5;

    private static final String[] CONTENT = {
            "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"
            ,"http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
            ,"http://vevoplaylist-live.hls.adaptive.level3.net/vevo/ch1/appleman.m3u8"
            ,"http://srv6.zoeweb.tv:1935/z330-live/stream/playlist.m3u8"
    };
    private static final String[] CONTENT_TITLE = {
            "jobs"
            ,"bip bop"
            ,"live tv 1"
            ,"live tv 2"
    };

    public Player(IPairing pairing, IPlayerObserver playerObserver) {
        mPairing = pairing;
        mPlayerObserver = playerObserver;
        setStyle("-fx-background-color: #bfc2c7;");
        mMediaView = new MediaView();


        Pane mvPane = new Pane() {
        };
        mvPane.getChildren().add(mMediaView);
        mvPane.setStyle("-fx-background-color: black;");
        setCenter(mvPane);

        mediaBar = new HBox();
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);

        final Button playButton = new Button(">");

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {

              /*  MediaPlayer.Status status = mp.getStatus();

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
                } else {
                    mp.pause();
                }*/
            }
        });


        mediaBar.getChildren().add(playButton);
        // Add spacer
        Label spacer = new Label("   ");
        mediaBar.getChildren().add(spacer);

        // Add Time label
        Label timeLabel = new Label("Time: ");
        mediaBar.getChildren().add(timeLabel);

        // Add time slider
        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging()) {
                    // multiply duration by percentage calculated by slider position
                    mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                }
            }
        });
        mediaBar.getChildren().add(timeSlider);

        // Add Play label
        playTime = new Label();
        playTime.setPrefWidth(130);
        playTime.setMinWidth(50);
        mediaBar.getChildren().add(playTime);

        // Add the volume label
        Label volumeLabel = new Label("Vol: ");
        mediaBar.getChildren().add(volumeLabel);

        // Add Volume slider
        volumeSlider = new Slider();
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(30);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (volumeSlider.isValueChanging()) {
                    mp.setVolume(volumeSlider.getValue() / 100.0);
                    mVolume = volumeSlider.getValue() / 100.0;
                }
            }
        });
        mediaBar.getChildren().add(volumeSlider);

        setBottom(mediaBar);
    }

    private void initMediaPlayer() {
        mp.currentTimeProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                updateValues();
            }
        });

        mp.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mp.pause();
                    mState = PlayerState.PAUSED;
                    stopRequested = false;
                } else {
                    //playButton.setText("||");
                }
            }
        });

        mp.setOnPaused(new Runnable() {
            public void run() {
                System.out.println("onPaused");
                mState = PlayerState.PAUSED;
                //playButton.setText(">");
            }
        });

        mp.setOnReady(new Runnable() {
            public void run() {
                duration = mp.getMedia().getDuration();
                updateValues();
                mPlayerObserver.onNotify(new JSONRPC2Notification(NOTIFY_START_PLAYING).toJSONString());
            }
        });

        mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mp.setOnEndOfMedia(new Runnable() {
            public void run() {
                //on end of video
                //TODO: send
                if (!repeat) {
                    //playButton.setText(">");
                    stopRequested = true;
                    atEndOfMedia = true;
                    mState = PlayerState.STOPPED;
                    if(mPlayerObserver != null){
                        mPlayerObserver.onNotify(new JSONRPC2Notification(NOTIFY_END_PLAYING).toJSONString());
                    }
                }
            }
        });
    }

    public void volumeUp(){
        mVolume -= mVolumeDelta;
        if( mVolume < 0){
            mVolume = 0;
        }
        mp.setVolume(mVolume);
        volumeSlider.setValue(mVolume);
    }

    public void volumeDown(){
        mVolume += mVolumeDelta;
        if(mVolume > 100){
            mVolume = 100;
        }
        mp.setVolume(mVolume);
        volumeSlider.setValue(mVolume);
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

    protected void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mp.getCurrentTime();
                    playTime.setText(formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentTime.divide(duration).toMillis()
                                * 100.0);
                    }
                    if (!volumeSlider.isValueChanging()) {
                        volumeSlider.setValue((int) Math.round(mp.getVolume()
                                * 100));
                    }
                }
            });
        }
    }

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

    @Override
    public void playContent(int id, int episode, String studio, int startSec) {
        // создаем медиаплеер
        mEkId = id;
        mEpId = episode;
        int index = id % CONTENT.length;
        String content = CONTENT[index];
        mContentTitle = CONTENT_TITLE[index];

        Media media = new Media(content);
        mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        mMediaView.setMediaPlayer(mp);
    }

    @Override
    public void playStream(int idStream, int startSec) {
        // создаем медиаплеер
        mIdStream = idStream;
        int index = idStream % CONTENT.length;
        String content = CONTENT[index];
        mContentTitle = CONTENT_TITLE[index];

        Media media = new Media(content);
        mp = new MediaPlayer(media);
        mp.setAutoPlay(true);
        mMediaView.setMediaPlayer(mp);
    }

    @Override
    public Map<String,Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put(RestConst.field.EK_ID, mEkId);
        result.put(RestConst.field.EP_ID, mEpId);
        result.put(RestConst.field.EK_TITLE, mContentTitle);
        result.put(RestConst.field.STATE, mState.ordinal());
        return result;
    }

    //--------------- JSONRPC2------------------

    @Override
    public String[] handledRequests() {
        return new String[]{COMMAND_PLAY_CONTENT, COMMAND_PLAY_STREAM, COMMAND_GET_STATUS};
    }

    @Override
    public JSONRPC2Response process(JSONRPC2Request jsonrpc2Request, MessageContext messageContext) {
        List params = (List) jsonrpc2Request.getParams();
        String token = null;
        int id;
        int startSec;
        switch (jsonrpc2Request.getMethod()) {
            case COMMAND_GET_STATUS:
                return new JSONRPC2Response(getStatus(), jsonrpc2Request.getID());
            case COMMAND_PLAY_CONTENT:
                //TODO: check token
                id = (int) ((long) params.get(0));
                int episode = (int) ((long) params.get(1));
                String studio = (String) params.get(2);
                startSec = (int) ((long) params.get(3));
                playContent(id, episode, studio, startSec);
                return new JSONRPC2Response("", jsonrpc2Request.getID());

            case COMMAND_PLAY_STREAM:
                //TODO: check token
                id = (int) ((long) params.get(0));
                startSec = (int) ((long) params.get(1));
                playStream(id, startSec);
                return new JSONRPC2Response("", jsonrpc2Request.getID());
            default:
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, jsonrpc2Request.getID());
        }
        //return new JSONRPC2Response(new JSONRPC2Error(-1, "error"), jsonrpc2Request.getID());
    }
}