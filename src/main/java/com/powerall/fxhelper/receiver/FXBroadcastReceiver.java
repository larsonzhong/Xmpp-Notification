package com.powerall.fxhelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.powerall.fxhelper.service.ReconnectService;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.PreferenceUtil;
import com.powerall.fxhelper.utils.SysUtil;

import java.util.ArrayList;

/**
 * 当asmack监听到网络改变，便会发出这个广播，所以发广播的动作是系统发的
 */
public class FXBroadcastReceiver extends BroadcastReceiver {
    public static final String BOOT_COMPLETED_ACTION = "com.powerall.action.BOOT_COMPLETED";
    public static ArrayList<EventHandler> mListeners = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SysUtil.print("action = " + action);
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (mListeners.size() > 0)// 通知接口完成加载
                for (EventHandler handler : mListeners) {
                    handler.onNetChange();
                }
        } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            SysUtil.print("System shutdown, stopping service.");
            Intent xmppServiceIntent = new Intent(context, ReconnectService.class);
            context.stopService(xmppServiceIntent);
        } else {
            if (!TextUtils.isEmpty(PreferenceUtil.getInstance(context).getPrefString(
                    Constant.Preference.FX_PASSWORD, ""))
                    && PreferenceUtil.getInstance(context).getPrefBoolean(
                    Constant.Preference.AUTO_START, true)) {
                Intent i = new Intent(context, ReconnectService.class);
                i.setAction(BOOT_COMPLETED_ACTION);
                context.startService(i);
            }
        }
    }

    public static abstract interface EventHandler {
        public abstract void onNetChange();
    }
}