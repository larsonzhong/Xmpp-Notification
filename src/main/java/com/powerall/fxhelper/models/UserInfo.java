package com.powerall.fxhelper.models;

/**
 * 接受消息推送的用户信息
 * 格式：larson@vchat.com@pacloulduser
 * Created by larson on 05/02/15.
 */
public class UserInfo {
    private String jId;//开发者方用户账户
    private String nickName;//
    private String password;
    private String platform;

    public UserInfo() {
    }

    public UserInfo(String jId, String password, String platform) {
        this.jId = jId;
        this.password = password;
        this.platform = platform;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getjId() {
        return jId;
    }

    public void setjId(String jId) {
        this.jId = jId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

}
