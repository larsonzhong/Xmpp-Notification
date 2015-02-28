package com.powerall.fxhelper.manager;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.powerall.fxhelper.utils.SysUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取手机信息
 * Created by larson on 05/02/15.
 */
public class PhoneInfoManager {

    /**
     * 获取IMEI号，IESI号，手机型号
     */
    public static Map getInfo(Context context) {
        TelephonyManager mTm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        String imsi = mTm.getSubscriberId();
        String mtype = android.os.Build.MODEL; // 手机型号
        String mtyb = android.os.Build.BRAND;//手机品牌
        String numer = mTm.getLine1Number(); // 手机号码，有的可得，有的不可得
        Map map = new HashMap();
        map.put("imei", imei);
        map.put("imsi", imsi);
        map.put("mtype", mtype);
        map.put("mtyb", mtyb);
        map.put("numer", numer);
        SysUtil.print("手机IMEI号：" + imei + "手机IESI号：" + imsi + "手机型号：" + mtype + "手机品牌：" + mtyb + "手机号码" + numer);
        return map;
    }
}
