package com.powerall.fxhelper.utils;

/**
 * 字符串处理类
 * Created by larson on 06/02/15.
 */
public class StringUtil {
    private static String PLEASE_SELECT = "请选择";

    public static boolean empty(Object o) {
        return o == null || "".equals(o.toString().trim())
                || "null".equalsIgnoreCase(o.toString().trim())
                || "undefined".equalsIgnoreCase(o.toString().trim())
                || PLEASE_SELECT.equals(o.toString().trim());
    }
}
