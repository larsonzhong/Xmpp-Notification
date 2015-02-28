package com.powerall.fxhelper.manager;

import android.content.Context;

import com.powerall.fxhelper.models.UserInfo;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.PreferenceUtil;
import com.powerall.fxhelper.utils.StringUtil;
import com.powerall.fxhelper.utils.SysUtil;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import java.util.concurrent.Callable;

public class AccountManager {
    private Context context;
    private static AccountManager manager;

    private AccountManager(Context context) {
        this.context = context;
    }

    public static AccountManager getInstance(Context context) {
        if (manager == null)
            manager = new AccountManager(context);
        return manager;
    }


    /**
     * 检查这个平台的这个用户是否在本地已经生成了配置文件
     *
     * @param userID
     */
    public UserInfo checkUser(String userID) {
        PreferenceUtil sp = PreferenceUtil.getInstance(context);
        sp.setSpName(userID);//关联这个用户的配置

        boolean isExist = sp.getPrefBoolean(Constant.Preference.FX_INIT, false);
        if (isExist) {
            String fxUser = sp.getPrefString(Constant.Preference.FX_USER, null);
            String fxPassword = sp.getPrefString(Constant.Preference.FX_PASSWORD, null);
            String platform = sp.getPrefString(Constant.Preference.FX_PLATFORM, null);
            String nick = sp.getPrefString(Constant.Preference.FX_NICKNAME, null);
            if (!StringUtil.empty(fxUser) && !StringUtil.empty(fxPassword) && !StringUtil.empty(platform)) {
                UserInfo userInfo = new UserInfo();
                userInfo.setjId(fxUser);
                userInfo.setNickName(nick);
                userInfo.setPassword(fxPassword);
                userInfo.setPlatform(platform);
                return userInfo;
            }
        }
        return null;
    }


    /**
     * 创建账户
     *
     * @param userID
     * @param password
     * @param platform
     */
    public UserInfo createAccount(String userID, final String password, String nickName, String platform) throws Exception {
        final String jid = getFXAccount(userID, platform);
        UserInfo user = null;
        try {
            boolean result = new Callable<Boolean>(){
                @Override
                public Boolean call() throws Exception {
                    return XmppConnectionManager.createAccount(jid, password);
                }
            }.call();
            if (result) {
                user = new UserInfo();
                user.setPlatform(platform);
                user.setPassword(password);
                user.setNickName(nickName);
                user.setjId(jid);
            }
        } catch (XMPPException e) {
            XMPPError error = e.getXMPPError();
            int errorCode;
            if (error != null) {
                errorCode = error.getCode();
                if (errorCode == 409) {
                    SysUtil.print("message.already.exists");//参照Spark源码
                    user = new UserInfo();
                    user.setPlatform(platform);
                    user.setPassword(password);
                    user.setNickName(nickName);
                    user.setjId(jid);//如果账户已经存在则返回一样的数据
                }
            }
            e.printStackTrace();
        }
        return user;
    }

    public String getFXAccount(String userID, String platform) {
        String jid = userID + "_" + platform;
        return jid;
    }
}
