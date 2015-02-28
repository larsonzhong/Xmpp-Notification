package com.powerall.fxhelper.models;

/**
 * Created by larson on 05/02/15.
 */
public class NoticeBean {
    /**
     * 本来说时间不是notice必须，但是如果用户需要设置接收时段，可以通过这个判断是否显示
     */
    private String time;//收到notice的时间
    private String title;//显示的标题
    private String text;//消失的内容
    private String url;//跳转到的网址

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
