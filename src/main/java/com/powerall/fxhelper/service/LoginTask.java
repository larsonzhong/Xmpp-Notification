package com.powerall.fxhelper.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.powerall.fxhelper.manager.XmppConnectionManager;
import com.powerall.fxhelper.models.UserInfo;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.PreferenceUtil;
import com.powerall.fxhelper.utils.SysUtil;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.carbons.CarbonManager;

/**
 * 登录异步任务.
 *
 * @author shimiso
 */
public class LoginTask extends AsyncTask<UserInfo, Void, Integer> {
    private Context context;
    public static PreferenceUtil sp;
    UserInfo user;

    public LoginTask(Context context) {
        sp = PreferenceUtil.getInstance(context);
        this.context = context;
    }

    @Override
    protected Integer doInBackground(UserInfo... params) {
        user = params[0];
        return login(user);
    }

    @Override
    protected void onPostExecute(Integer result) {
        switch (result) {
            case Constant.Login_Result.LOGIN_SECCESS: // 登录成功
                saveLoginConfig();// 保存用户配置信息
                startService(); // 初始化各项服务
                Toast.makeText(
                        context, "登录成功",
                        Toast.LENGTH_SHORT).show();
                break;
            case Constant.Login_Result.LOGIN_ERROR_ACCOUNT_PASS:// 账户或者密码错误
                Toast.makeText(
                        context, "帐户名或密码错误",
                        Toast.LENGTH_SHORT).show();
                break;
            case Constant.Login_Result.SERVER_UNAVAILABLE:// 服务器连接失败
                Toast.makeText(context, "服务器不可用",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        super.onPostExecute(result);
    }


    /**
     * 登录
     */
    private int login(UserInfo user) {
        try {
            XMPPConnection mXMPPConnection = XmppConnectionManager.getIntance(context).getConnection();
            if (mXMPPConnection.isConnected()) {
                try {
                    mXMPPConnection.disconnect();
                } catch (Exception e) {
                    SysUtil.print("conn.disconnect() failed: " + e);
                }
            }
            SmackConfiguration.setPacketReplyTimeout(XmppConnectionManager.PACKET_TIMEOUT);
            SmackConfiguration.setDefaultPingInterval(0);
            mXMPPConnection.connect();
            if (!mXMPPConnection.isConnected()) {
                return Constant.Login_Result.SERVER_UNAVAILABLE;
            }

            // SMACK auto-login if we were authenticated before
            if (!mXMPPConnection.isAuthenticated()) {
                mXMPPConnection.login(user.getjId(), user.getPassword(), user.getPlatform());
                setStatusFromConfig(mXMPPConnection);// 更新在线状态
            }

            return mXMPPConnection.isAuthenticated() ? Constant.Login_Result.LOGIN_SECCESS : Constant.Login_Result.LOGIN_ERROR_ACCOUNT_PASS;
        } catch (XMPPException xee) {
            final XMPPError error = xee.getXMPPError();
            int errorCode = 0;
            if (error != null) {
                errorCode = error.getCode();
            }
            if (errorCode == 401) {
                return Constant.Login_Result.LOGIN_ERROR_ACCOUNT_PASS;
            } else if (errorCode == 403) {
                return Constant.Login_Result.LOGIN_ERROR_ACCOUNT_PASS;
            } else {
                return Constant.Login_Result.SERVER_UNAVAILABLE;
            }
        }
    }


    /**
     * 发送状态信息
     */
    public void setStatusFromConfig(XMPPConnection mXMPPConnection) {
        boolean messageCarbons = sp.getPrefBoolean(
                Constant.Preference.MESSAGE_CARBONS, true);
        String statusMode = sp.getPrefString(
                Constant.Preference.STATUS_MODE, Constant.Status.AVAILABLE);
        String statusMessage = sp.getPrefString(
                Constant.Preference.STATUS_MESSAGE, Constant.Status.online);
        int priority = sp.getPrefInt(
                Constant.Preference.PRIORITY, 0);
        if (messageCarbons)
            CarbonManager.getInstanceFor(mXMPPConnection).sendCarbonsEnabled(
                    true);

        Presence presence = new Presence(Presence.Type.available);
        Presence.Mode mode = Presence.Mode.valueOf(statusMode);
        presence.setMode(mode);
        presence.setStatus(statusMessage);
        presence.setPriority(priority);
        mXMPPConnection.sendPacket(presence);
    }

    /**
     * 开启服务
     */
    private void startService() {
        //1.开启服务监听网络状态
        Intent recService = new Intent(context, ReconnectService.class);
        context.startService(recService);
        //2.开启服务监听消息推送和ping
        Intent packetService = new Intent(context, PacketService.class);
        context.startService(packetService);
    }

    /**
     * 保存登录信息
     */
    private void saveLoginConfig() {
        sp.setPrefString(Constant.Preference.FX_USER, user.getjId());
        sp.setPrefString(Constant.Preference.FX_PASSWORD, user.getPassword());
        sp.setPrefString(Constant.Preference.FX_PLATFORM, user.getPlatform());
    }

    public static interface ConnCallback {
        public void onConnectionFailed(String reason);
    }
}
