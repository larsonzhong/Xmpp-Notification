package com.powerall.fxhelper.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;

import com.powerall.fxhelper.manager.XmppConnectionManager;
import com.powerall.fxhelper.models.UserInfo;
import com.powerall.fxhelper.receiver.FXBroadcastReceiver;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.NetUtil;
import com.powerall.fxhelper.utils.PreferenceUtil;
import com.powerall.fxhelper.utils.SysUtil;

/**
 * 自动重连的服务
 * Created by larson on 05/02/15.
 */
public class ReconnectService extends Service implements FXBroadcastReceiver.EventHandler, LoginTask.ConnCallback {
    //    public static final int CONNECTED = 0;
    //    public static final int CONNECTING = 1;
    public static final int DISCONNECTED = -1;

    private int mReconnectTimeout = 5;
    private static final int RECONNECT_MAXIMUM = 10 * 60;// 最大重连时间间隔
    private int mConnectedState = DISCONNECTED; // 是否已经连接

    public static final String PONG_TIMEOUT = "pong timeout";// 连接超时
    public static final String NETWORK_ERROR = "network error";// 网络错误
    private PendingIntent mPAlarmIntent;
    private PreferenceUtil sp;
    private XmppConnectionManager connMan;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        FXBroadcastReceiver.mListeners.add(this);//添加网络状态回调
        sp = PreferenceUtil.getInstance(this);
        connMan = XmppConnectionManager.getIntance(this);

        Intent mAlarmIntent = new Intent(Constant.ReceiverCode.RECONNECT_ALARM);
        mPAlarmIntent = PendingIntent.getBroadcast(this, 0, mAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        BroadcastReceiver mAlarmReceiver = new ReconnectAlarmReceiver();
        registerReceiver(mAlarmReceiver, new IntentFilter(Constant.ReceiverCode.RECONNECT_ALARM));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onNetChange() {
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE) {// 如果是网络断开，不作处理
            connectionFailed(NETWORK_ERROR);
            return;
        }
        if (connMan.isAuthenticated())// 如果已经连接上，直接返回
            return;
        reLogin();
    }


    // 自动重连广播
    private class ReconnectAlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context ctx, Intent i) {
            SysUtil.print("Alarm received.");
            if (!sp.getPrefBoolean(
                    Constant.Preference.AUTO_RECONNECT, true)) {
                return;
            }
            if (mConnectedState != DISCONNECTED) {
                SysUtil.print("Reconnect attempt aborted: we are connected again!");
                return;
            }
            reLogin();
        }
    }

    /**
     * 重新登陆
     */
    private void reLogin() {
        PreferenceUtil sp = PreferenceUtil.getInstance(this);
        String account = sp.getPrefString(Constant.Preference.FX_USER, "");
        String password = sp.getPrefString(Constant.Preference.FX_PASSWORD, "");
        String plat = sp.getPrefString(
                Constant.Preference.FX_PLATFORM, Constant.PlatForm.PlatForm_VCHAT);
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password))// 如果没有帐号，也直接返回
            return;
        if (!sp.getPrefBoolean(Constant.Preference.AUTO_RECONNECT, true))// 不需要重连
            return;
        UserInfo user = new UserInfo(account, password, plat);
        new LoginTask(ReconnectService.this).execute(user);
    }

    /**
     * 连接失败的处理
     *
     * @param reason 连接失败的原因代码
     */
    public void onConnectionFailed(final String reason) {
        new Runnable() {
            public void run() {
                connectionFailed(reason);
            }
        }.run();
    }

    /**
     * 连接失败,开启重连闹钟
     *
     * @param reason 连接失败的原因代码
     */
    private void connectionFailed(String reason) {
        SysUtil.print("FXService.class,--   connectionFailed: " + reason);
        mConnectedState = DISCONNECTED;// 更新当前连接状态
        if (TextUtils.equals(reason, Constant.Login_Failed.FAILED_MANUEL)) {// 如果是手动退出
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                    .cancel(mPAlarmIntent);
            return;
        }
        // 回调
        boolean isAppInit = sp.getPrefBoolean(Constant.Preference.FX_INIT, false);
        if (!isAppInit)// 如果是第一次登录,就算登录失败也不需要继续
            return;//TODO

        // 无网络连接时,直接返回
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE) {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                    .cancel(mPAlarmIntent);
            return;
        }

        String account = sp.getPrefString(
                Constant.Preference.FX_USER, "");
        String password = sp.getPrefString(
                Constant.Preference.FX_PASSWORD, "");
        // 无保存的帐号密码时，也直接返回
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            SysUtil.print("account = null || password = null");
            return;
        }
        // 如果不是手动退出并且需要重新连接，则开启重连闹钟
        if (sp.getPrefBoolean(
                Constant.Preference.AUTO_RECONNECT, true)) {
            SysUtil.print("connectionFailed(): registering reconnect in "
                    + mReconnectTimeout + "s");
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
                    AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                            + mReconnectTimeout * 1000, mPAlarmIntent);
            mReconnectTimeout = mReconnectTimeout * 2;
            if (mReconnectTimeout > RECONNECT_MAXIMUM)
                mReconnectTimeout = RECONNECT_MAXIMUM;
        } else {
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                    .cancel(mPAlarmIntent);
        }

    }


}
