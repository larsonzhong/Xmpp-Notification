package com.powerall.fxhelper.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 配置工具类
 * Created by larson on 05/02/15.
 */
public class PreferenceUtil {

    public static PreferenceUtil sp;
    private SharedPreferences settings;
    private String spName = "fxhelper";
    private Context context;

    public void setSpName(String spName) {
        this.spName = spName;
    }

    private PreferenceUtil(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (sp == null)
            sp = new PreferenceUtil(context);
        return sp;
    }


    public boolean getPrefBoolean(final String key,
                                  final boolean defaultValue) {
        return settings.getBoolean(key, defaultValue);
    }

    public String getPrefString(String key,
                                final String defaultValue) {
        return settings.getString(key, defaultValue);
    }

    public int getPrefInt(final String key,
                          final int defaultValue) {
        return settings.getInt(key, defaultValue);
    }


    public void setPrefString(final String key,
                              final String value) {
        settings.edit().putString(key, value).commit();
    }

    public void setPrefBoolean(final String key,
                               final boolean value) {
        settings.edit().putBoolean(key, value).commit();
    }

    public void setPrefInt(final String key,
                           final int value) {
        settings.edit().putInt(key, value).commit();
    }

}
