package com.zhongbenshuo.bulletinboard.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhongbenshuo.bulletinboard.activity.LogoActivity;

/**
 * 开机自启广播
 * Created at 2019/8/22 14:06
 *
 * @author LiYuliang
 * @version 1.0
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            Intent mainActivityIntent = new Intent(context, LogoActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}