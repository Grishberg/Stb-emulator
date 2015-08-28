package com.grishberg.data.rest;

/**
 * Created by g on 13.08.15.
 */
public final class RestConst {
    private RestConst() {
    }

    public final class MqPolicy {
        public static final String POLICY_URL = "http://pair.bolshoe.tv";
        public static final String API = POLICY_URL + "/rc";
        public static final String GET_POLICY = "/getPolicy";
        public static final String REQUEST_DEVICE = "/requestDevice";
        public static final String REGISTER_DEVICE = "/registerDevice";

        private MqPolicy() {

        }
    }

    public final class api {
        public static final String PAIRING_ADD_DEVICE = "Pairing.addDevice";
        public static final String PLAYER_PLAY_CONTENT = "Player.playContent";
        public static final String PLAYER_PLAY_STREAM = "Player.playStream";
        public static final String PLAYER_PLAY_YOUTUBE = "Player.playYoutube";
        public static final String PLAYER_GET_STATUS = "Player.getStatus";
        public static final String INPUT_PAUSE = "Input.playPause";
        public static final String INPUT_VOLUME_UP = "Input.volumeUp";
        public static final String INPUT_VOLUME_DOWN = "Input.volumeDown";
        public static final String INPUT_STOP = "Input.stop";
        public static final String INPUT_MUTE = "Input.mute";
        public static final String INPUT_PLAY_PAUSE = "Input.playPause";
        public static final String INPUT_LEFT = "Input.left";
        public static final String INPUT_RIGHT = "Input.right";
        public static final String INPUT_UP = "Input.up";
        public static final String INPUT_DOWN = "Input.down";
        public static final String INPUT_SUBTITLE = "Input.subtitle";
        public static final String INPUT_AUDIO = "Input.audio";
        public static final String INPUT_FULLSCREEN = "Input.fullscreen";
        public static final String INPUT_PREV = "Input.prev";
        public static final String INPUT_NEXT = "Input.next";
        public static final String INPUT_MENU = "Input.menu";
        public static final String INPUT_BACK = "Input.back";
        public static final String INPUT_SELECT = "Input.select";

        private api() {
        }
    }

    public final class Notifications {
        public static final String PLAYER_ON_START = "Player.onStart";
        public static final String PLAYER_ON_PAUSE = "Player.onPause";
        public static final String PLAYER_ON_STOP = "Player.onStop";
        public static final String PLAYER_ON_BUFFERING = "Player.onBuffering";
        public static final String PLAYER_ON_ERROR = "Player.onError";
        public static final String PLAYER_ON_VOLUME_CHANGE = "Player.onVolumeChange";

        public final class Parameters {
            public static final String PLAYER_STOP = "PLAYER_STOP";

            private Parameters() {
            }
        }

        private Notifications() {
        }
    }

    public final class Values {
        public static final String PLAYER_NEXT = "PLAYER_NEXT";
        public static final String PLAYER_FWD = "PLAYER_FWD";
        public static final String PLAYER_STOP = "PLAYER_STOP";
        public static final String CONTENT = "CONTENT";
        public static final String STREAM = "STREAM";
        public static final String YOUTUBE = "YOUTUBE";

        private Values() {
        }
    }

    public final class Fields {
        public static final String DEV_ID = "dev_id";
        public static final String DEV_NAME = "dev_name";
        public static final String PAIR_TOKEN = "pair_token";
        public static final String KEY = "key";
        public static final String ID = "id";
        public static final String EP_ID = "epId";
        public static final String TITLE = "title";
        public static final String STATE = "state";
        public static final String POSITION = "position";
        public static final String VOLUME = "volume";
        public static final String TYPE = "type";
        public static final String END_SEC = "endSec";
        public static final String REASON = "reason";
        public static final String CODE = "code";
        public static final String VALUE = "value";

        private Fields() {
        }
    }

    public final class responseField {
        public static final String TOKEN = "token";
        public static final String MAC = "mac";
        public static final String IS_SUCCESS = "is_success";
        public static final String DEVICE_ID = "deviceId";
        public static final String EXPIRE = "expire";
        public static final String MQ = "mq";
        public static final String ERROR_MESSAGE = "error.message";
        public static final String ERROR_CODE = "error.code";

        private responseField() {
        }
    }
}
