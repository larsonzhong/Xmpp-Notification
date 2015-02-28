package com.powerall.fxhelper.manager;

import android.content.Context;

import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.PreferenceUtil;
import com.powerall.fxhelper.utils.SysUtil;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * 负责openFire的服务类
 * <p/>
 * Created by larson on 05/02/15.
 */
public class XmppConnectionManager {
    public static final int PACKET_TIMEOUT = 30000;
    private XMPPConnection mXMPPConnection;
    public static XmppConnectionManager connMan;
    public static PreferenceUtil sp;
    private Context context;

    public static XmppConnectionManager getIntance(Context context) {
        if (connMan == null)
            connMan = new XmppConnectionManager(context);
        return connMan;
    }

    public XMPPConnection getConnection() {
        if (mXMPPConnection == null)
            mXMPPConnection = initXMPPConnection(context);
        return mXMPPConnection;
    }

    public XmppConnectionManager(Context context) {
        this.context = context;
        mXMPPConnection = initXMPPConnection(context);
    }

    private XMPPConnection initXMPPConnection(Context context) {
        sp = PreferenceUtil.getInstance(context);
        String customServer = sp.getPrefString(
                Constant.Preference.CUSTOM_SERVER, Constant.Server.DEFAULT_HOST);
        int port = sp.getPrefInt(
                Constant.Preference.PORT, Constant.Server.DEFAULT_PORT_INT);
        String server = sp.getPrefString(
                Constant.Preference.Server, Constant.Server.DEFAULT_SERVER_NAME);
        boolean smackDebug = sp.getPrefBoolean(
                Constant.Preference.SMACKDEBUG, Constant.Server.DEFAULT_DEBUG);
        boolean requireSsl = sp.getPrefBoolean(
                Constant.Preference.REQUIRE_TLS, Constant.Server.DEFAULT_REQUIRE_TLS);
        ConnectionConfiguration mXMPPConfig;
        if (customServer.length() > 0
                || port != Constant.Server.DEFAULT_PORT_INT)
            mXMPPConfig = new ConnectionConfiguration(customServer, port,
                    server);
        else
            mXMPPConfig = new ConnectionConfiguration(server); // use SRV

        mXMPPConfig.setReconnectionAllowed(true);
        mXMPPConfig.setSendPresence(false);
        mXMPPConfig.setCompressionEnabled(false); // disable for now
        mXMPPConfig.setDebuggerEnabled(smackDebug);
        if (requireSsl)
            mXMPPConfig
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.required);

        return new XMPPConnection(mXMPPConfig);
    }



    public boolean isAuthenticated() {
        return mXMPPConnection != null && (mXMPPConnection.isConnected() && mXMPPConnection.isAuthenticated());
    }


    /**
     * 创建一个账户
     *
     * @param jid      重新组装的用户名
     * @param password 密码
     */
    public static boolean createAccount(String jid, String password) throws XMPPException {
        XMPPConnection conn = MyDefaultConn.getInstance().getConn();
        if (!conn.isConnected()) {
            SysUtil.print("没有连接");
            conn.connect();
        }
        org.jivesoftware.smack.AccountManager accMan = conn.getAccountManager();
//        Map<String, String> map = new LinkedHashMap<String, String>();
//        int gender = user.getGender();
//        map.put("gender", gender + "");
//        String icon = user.getUserIcon();
//        map.put("icon", icon);
//        String name = user.getName();
//        map.put("name", name);
//        String note = user.getUserNote();
//        map.put("note", note);
        accMan.createAccount(jid, password);
        return true;
    }

    /**
     * 获取不要密码的连接
     *
     * @author larson
     */
    public static class MyDefaultConn {
        private static MyDefaultConn md;
        private XMPPConnection conn;

        public static MyDefaultConn getInstance() {
            if (md == null)
                md = new MyDefaultConn();
            return md;
        }

        public XMPPConnection getConn() {
//            if (conn == null)
            conn = makeConn();
            return conn;
        }

        private XMPPConnection makeConn() {
            ConnectionConfiguration connectionConfig = new ConnectionConfiguration(
                    Constant.Server.DEFAULT_HOST, Constant.Server.DEFAULT_PORT_INT,
                    Constant.Server.DEFAULT_SERVER_NAME);
            connectionConfig.setSASLAuthenticationEnabled(false);// 不使用SASL验证，设置为false
            connectionConfig
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            connectionConfig.setReconnectionAllowed(true);
            conn = new XMPPConnection(connectionConfig);
            return conn;
        }

        private MyDefaultConn() {
            makeConn();
        }
    }
}
