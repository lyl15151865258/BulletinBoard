package com.zhongbenshuo.bulletinboard.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhongbenshuo.bulletinboard.activity.LogoActivity;

/**
 * Sdcard接收者，可以接收四个广播
 * 当Sdcard的状态发生改变后，系统会自动的发出以下四种广播
 * 1.mount 挂载；
 * 2.unmount 卸载移除；
 * 3.start_scan 开始扫描；
 * 4.scan_finish 扫描完成；
 */
public class SdcardBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = SdcardBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            Log.d(TAG, "Sdcard挂载了...");
            Intent mainActivityIntent = new Intent(context, LogoActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            Log.d(TAG, "Sdcard卸载了 移除了...");
        } else if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            Log.d(TAG, "Sdcard开始扫描...");
        } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            Log.d(TAG, "Sdcard扫描完成✅...");
        }
    }
}
