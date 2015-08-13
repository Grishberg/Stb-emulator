package com.grishberg.data.rest;

/**
 * Created by g on 13.08.15.
 */
public final class RestConst {

    private RestConst(){}

    public final class MqPolicy {
        public static final String POLICY_URL = "http://pair.bolshoe.tv";
        public static final String API = POLICY_URL + "/rc";
        public static final String GET_POLICY = "/getPolicy";
        public static final String REQUEST_DEVICE = "/requestDevice";
        public static final String REGISTER_DEVICE = "/registerDevice";

        private MqPolicy(){

        }
    }
    public final class field {
        public static final String DEV_ID = "dev_id";
        public static final String DEV_NAME = "dev_name";
        public static final String PAIR_TOKEN = "pair_token";
        public static final String KEY = "key";

        public static final String EK_ID = "EK_Id";
        public static final String EP_ID = "EP_Id";
        public static final String EK_TITLE = "EK-Title";
        public static final String STATE = "state";

        private field() {
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
