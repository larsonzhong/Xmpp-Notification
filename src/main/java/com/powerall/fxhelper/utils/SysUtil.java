package com.powerall.fxhelper.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by larson on 05/02/15.
 */
public class SysUtil {

    public static boolean DEBUG_MODE = true;
    public static String Notice_Subject = "notification";
    /**
     * 精确到毫秒
     */
//    public static final String MS_FORMART = "yyyy-MM-dd HH:mm:ss";
    public static final String MS_FORMART = "HH:mm:ss";

    /**
     * 统一打印，便于调试和发布
     *
     * @param str
     */
    public static void print(String str) {
        if (DEBUG_MODE)
            System.out.println(str);
    }


    public static String date2Str(Date d, String format) {
        if (d == null) {
            return null;
        }
        if (format == null || format.length() == 0) {
            format = MS_FORMART;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String s = sdf.format(d);
        return s;
    }
}
