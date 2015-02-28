package com.powerall.fxhelper.utils;

/**
 * 以下字段已被使用
 * Created by larson on 05/02/15.
 */
public class Constant {

    public class Preference {
        public final static String FX_INIT = "isFXSDKInit";

        public final static String FX_USER = "fxUser";
        public final static String FX_PASSWORD = "fxPassword";
        public static final String FX_PLATFORM = "fxPlatform";//客户端标示
        public static final String FX_NICKNAME = "fxNickName";//客户端标示

        public final static String SMACKDEBUG = "smackdebug";
        public final static String REQUIRE_TLS = "require_tls";

        public final static String AUTO_RECONNECT = "reconnect";

        public final static String CUSTOM_SERVER = "account_customserver";
        public final static String PORT = "account_port";
        public final static String PRIORITY = "account_prio";

        public final static String MESSAGE_CARBONS = "carbons";
        public final static String Server = "server";

        public static final String RESOURCE = "account_resource";


        public final static String STATUS_MODE = "status_mode";

        public final static String STATUS_MESSAGE = "status_message";
        public final static String AUTO_START = "auto_start";
    }

    public static class Server {
        public final static int DEFAULT_PORT_INT = 5222;
        public static final String DEFAULT_HOST = "118.186.241.177";
        public static final String DEFAULT_SERVER_NAME = "118.186.241.177:5222";
        public static final boolean DEFAULT_DEBUG = true;
        public static final boolean DEFAULT_REQUIRE_TLS = false;
    }

    public class Status {
        public final static String online = "在线";
        public final static String AVAILABLE = "available";
    }

    public class Login_Result {
        public static final int LOGIN_SECCESS = 0;// 成功
        public static final int LOGIN_ERROR_ACCOUNT_PASS = 3;// 账号或者密码错误
        public static final int SERVER_UNAVAILABLE = 4;// 无法连接到服务器
        public static final int LOGIN_ERROR = 5;// 连接失败
    }

    public class PlatForm {
        public static final String PlatForm_VCHAT = "SDK";// 成功
    }

    /**
     * 登陆失败代码
     */
    public class Login_Failed {
        public static final String FAILED_MANUEL = "logout";
    }

    public class ReceiverCode {
        public static final String PONG_TIMEOUT_ALARM = "com.powerall.notice.PONG_TIMEOUT_ALARM";
        public static final String PING_ALARM = "com.powerall.notice.PING_ALARM";
        public static final String RECONNECT_ALARM = "com.powerall.fxhelper.RECONNECT_ALARM";
    }
}
