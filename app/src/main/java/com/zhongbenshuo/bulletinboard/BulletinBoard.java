package com.zhongbenshuo.bulletinboard;

import android.app.Application;
import android.util.SparseArray;

import com.zhongbenshuo.bulletinboard.contentprovider.SPHelper;
import com.zhongbenshuo.bulletinboard.utils.CrashHandler;
import com.zhongbenshuo.bulletinboard.utils.encrypt.RSAUtils;

/**
 * Application类
 * Created by Li Yuliang on 2019/03/26.
 *
 * @author LiYuliang
 * @version 2019/03/26
 */

public class BulletinBoard extends Application {

    private static BulletinBoard instance;
    public static String publicKeyString, privateKeyString;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SPHelper.init(this);
        // 捕捉异常
        CrashHandler.getInstance().init(this);
        // 初始化加密秘钥
        initKey();
    }

    private void initKey() {
        new Thread(() -> {
            // 生成RSA密钥对
            SparseArray<String> keyMap = null;
            try {
                keyMap = RSAUtils.genKeyPair(1024);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (keyMap != null) {
                publicKeyString = keyMap.get(0);
                privateKeyString = keyMap.get(1);
            }
        }).start();
    }

    /**
     * 单例模式中获取唯一的MyApplication实例
     *
     * @return application实例
     */
    public static BulletinBoard getInstance() {
        if (instance == null) {
            instance = new BulletinBoard();
        }
        return instance;
    }

}
