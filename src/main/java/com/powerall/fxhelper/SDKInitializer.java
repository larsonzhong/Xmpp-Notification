package com.powerall.fxhelper;

import com.powerall.fxhelper.manager.AccountManager;
import com.powerall.fxhelper.models.UserInfo;
import com.powerall.fxhelper.service.LoginTask;
import com.powerall.fxhelper.utils.Constant;
import com.powerall.fxhelper.utils.PreferenceUtil;

/**
 * Created by larson on 05/02/15.
 * 使用前需要调用initialize 方法，否则接受不到消息
 */
public class SDKInitializer {

    /**
     * 初始化本接收器
     *
     * @param userID   开发者平台上的用户的用户名
     * @param context  上下文
     * @param platform 开发者的应用标示
     */
    public static void initialize(final String userID, final String password, final String nickName, final String platform, final android.content.Context context) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                //1.检查本地配置文件是否有openFire账户信息
                AccountManager manager = AccountManager.getInstance(context);
                UserInfo user = manager.checkUser(userID);

                //2.如果没有则创建一个
                if (user == null) {
                    try {
                        user = manager.createAccount(userID, password, nickName, platform);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //3.有的话就拿这个登录(只要user不是null就证明注册成功或者已经存在)
                new LoginTask(context).execute(user);
                PreferenceUtil.getInstance(context).setPrefString(Constant.Preference.FX_PLATFORM, user.getNickName());
            }
        }.start();


    }
}
