package com.powerall.fxhelper.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.google.gson.Gson;
import com.powerall.fxhelper.R;
import com.powerall.fxhelper.activities.ShowUrlActivity;
import com.powerall.fxhelper.manager.XmppConnectionManager;
import com.powerall.fxhelper.models.NoticeBean;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.SysUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ping.packet.Ping;

import java.util.Calendar;

/**
 * 监听服务器推送和向服务器定时发送pingPong请求的service
 * Created by larson on 09/02/15.
 */
public class PacketService extends Service {

    private NotificationManager notificationManager;
    private PacketListener noticeListener;
    private PacketListener mPongListener;
    private String mPingID;
    private long mPingTimestamp;
    private Intent mPongTimeoutAlarmIntent;
    private PendingIntent mPongTimeoutAlarmPendIntent;
    private PendingIntent mPingAlarmPendIntent;
    private BroadcastReceiver mPingAlarmReceiver;
    private Intent mPingAlarmIntent;
    private PongTimeoutAlarmReceiver mPongTimeoutAlarmReceiver;

    private Context context;
    private XMPPConnection mXmppConnection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        init();
    }

    private void init() {
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mXmppConnection = XmppConnectionManager.getIntance(context).getConnection();

        mPingAlarmIntent = new Intent(Constant.ReceiverCode.PING_ALARM);
        mPingAlarmReceiver = new PingAlarmReceiver();

        mPongTimeoutAlarmIntent = new Intent(Constant.ReceiverCode.PONG_TIMEOUT_ALARM);
        mPongTimeoutAlarmReceiver = new PongTimeoutAlarmReceiver();
    }

    @Override
    public ComponentName startService(Intent service) {
        registerAllListener();
        return super.startService(service);
    }

    private void registerAllListener() {
        if (mXmppConnection.isAuthenticated()) {
            registerNoticeListener();//注册推送消息监听
            registerPongListener();//注册ping监听
            setPingTongAlarm();
        }
    }

    /**
     * 监听消息推送
     */
    private void registerNoticeListener() {

        if (noticeListener != null)
            mXmppConnection.removePacketListener(noticeListener);

        noticeListener = new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                if (packet instanceof Message) {
                    Message message = (Message) packet;
                    String sub = message.getSubject();
                    SysUtil.print("sub is --" + sub);

                    if (message.getBody() != null && !message.getBody().equals("null")) { // 判断消息是否为推送消息
                        if (sub != null && sub.equals("null") && sub.equalsIgnoreCase(SysUtil.Notice_Subject)) {
                            NoticeBean notice;
                            String time = SysUtil.date2Str(Calendar.getInstance().getTime(),
                                    SysUtil.MS_FORMART);
                            notice = new Gson().fromJson(message.getBody(), NoticeBean.class);
                            notice.setTime(time);
                            showNotification(R.drawable.icon, notice.getTitle(), notice.getText(), ShowUrlActivity.class, notice);
                        }
                    }
                }
            }
        };

        MessageTypeFilter filter = new MessageTypeFilter(Message.Type.normal);
        mXmppConnection.addPacketListener(noticeListener, filter);
    }


    /**
     * ************** start 处理ping服务器消息 **********************
     */
    private void registerPongListener() {
        // reset ping expectation on new connection
        mPingID = null;

        if (mPongListener != null)
            mXmppConnection.removePacketListener(mPongListener);

        mPongListener = new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                if (packet == null)
                    return;
                if (packet.getPacketID().equals(mPingID)) {//监听发出去的iq包
                    SysUtil.print(String.format(
                            "Ping: server latency %1.3fs",
                            (System.currentTimeMillis() - mPingTimestamp) / 1000.));
                    mPingID = null;
                    ((AlarmManager) context//如果iq包被服务器收到并返回，说明没有掉线，取消超时广播
                            .getSystemService(Context.ALARM_SERVICE))
                            .cancel(mPongTimeoutAlarmPendIntent);
                }
            }

        };

        mXmppConnection.addPacketListener(mPongListener, new PacketTypeFilter(
                IQ.class));

    }

    /**
     * 设置pingTong闹钟(初始化)
     */
    private void setPingTongAlarm() {
        mPingAlarmPendIntent = PendingIntent.getBroadcast(//发送一个ping服务器的广播
                context, 0, mPingAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mPongTimeoutAlarmPendIntent = PendingIntent.getBroadcast(//发送一个超时广播
                context, 0, mPongTimeoutAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        context.registerReceiver(mPingAlarmReceiver, new IntentFilter(//注册ping服务器的广播接收者
                Constant.ReceiverCode.PING_ALARM));
        context.registerReceiver(mPongTimeoutAlarmReceiver, new IntentFilter(//注册超时广播接收者
                Constant.ReceiverCode.PONG_TIMEOUT_ALARM));
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))//开启重复ping的广播，隔五分钟发送一个
                .setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis()
                                + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        mPingAlarmPendIntent);
    }


    /**
     * BroadcastReceiver to trigger reconnect on pong timeout.
     */
    private class PongTimeoutAlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context ctx, Intent i) {
            SysUtil.print("Ping: timeout for " + mPingID);
            logout();// 超时就断开连接
        }
    }


    /**
     * 当接受到5分钟一次的ping服务器的广播就开始ping服务器
     */
    private class PingAlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context ctx, Intent i) {
            if (mXmppConnection.isAuthenticated()) {
                sendServerPing();
            } else
                SysUtil.print("Ping: alarm received, but not connected to server.");
        }
    }

    public void sendServerPing() {
        if (mPingID != null) {
            SysUtil.print("Ping: requested, but still waiting for " + mPingID);
            return; // a ping is still on its way
        }
        Ping ping = new Ping();
        ping.setType(IQ.Type.GET);
        ping.setTo(Constant.Server.DEFAULT_HOST);
        mPingID = ping.getPacketID();
        mPingTimestamp = System.currentTimeMillis();
        SysUtil.print("Ping: sending ping " + mPingID);
        mXmppConnection.sendPacket(ping);

        // register ping timeout handler: PACKET_TIMEOUT(30s) + 3s
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + XmppConnectionManager.PACKET_TIMEOUT + 3000, mPongTimeoutAlarmPendIntent);
    }

    public boolean logout() {
        SysUtil.print("unRegisterCallback()");
        try {
            mXmppConnection.removePacketListener(noticeListener);
            mXmppConnection.removePacketListener(mPongListener);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                    .cancel(mPingAlarmPendIntent);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                    .cancel(mPongTimeoutAlarmPendIntent);
            context.unregisterReceiver(mPingAlarmReceiver);
            context.unregisterReceiver(mPongTimeoutAlarmReceiver);
        } catch (Exception e) {
            // ignore it!
            return false;
        }
        if (mXmppConnection.isConnected()) {


            new Thread() {
                public void run() {
                    SysUtil.print("shutDown thread started");
                    mXmppConnection.disconnect();
                    SysUtil.print("shutDown thread finished");
                }
            }.start();
        }
        this.context = null;
        return true;
    }

    @Override
    public boolean stopService(Intent name) {
        logout();
        return super.stopService(name);
    }


    /**
     * 发出通知
     */
    private void showNotification(int iconId, String contentTitle,
                                  String contentText, Class activity, NoticeBean notice) {
        /*
         * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
        Intent notifyIntent = new Intent(context, activity);
        notifyIntent.putExtra("url", notice.getUrl());
        // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		/* 创建PendingIntent作为设置递延运行的Activity */

        PendingIntent appIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, 0);

		/* 创建Notication，并设置相关参数 */
        Notification myNoti = new Notification();
        // 点击自动消失
        myNoti.flags = Notification.FLAG_AUTO_CANCEL;
        /* 设置statusbar显示的icon */
        myNoti.icon = iconId;
        /* 设置statusbar显示的文字信息 */
        myNoti.tickerText = contentTitle;
        /* 设置notification发生时同时发出默认声音 */
        myNoti.defaults = Notification.DEFAULT_SOUND;
        /* 设置Notification留言条的参数 */
        myNoti.setLatestEventInfo(context, contentTitle, contentText, appIntent);
        /* 送出Notification */
        notificationManager.notify(0, myNoti);
    }

}
