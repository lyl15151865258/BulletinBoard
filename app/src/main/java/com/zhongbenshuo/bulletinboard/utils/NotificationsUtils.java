package com.zhongbenshuo.bulletinboard.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zhongbenshuo.bulletinboard.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationsUtils {

    public static boolean isNotificationEnabled(Context context) {
        //API 19以下的版本无法获得通知栏权限，该方法默认会返回true
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }

    /**
     * 显示一个状态栏通知
     *
     * @param context Context对象
     * @param title   通知栏标题
     * @param content 通知内容
     */
    public static void showNotification(Context context, String title, String content) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel Channel = new NotificationChannel("123", title, NotificationManager.IMPORTANCE_NONE);
            Channel.enableLights(true);                                             //设置提示灯
            Channel.setLightColor(Color.RED);                                       //设置提示灯颜色
            Channel.setShowBadge(true);                                             //显示logo
            Channel.setDescription(content);                                        //设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);        //设置锁屏不可见 VISIBILITY_SECRET=不可见
            manager.createNotificationChannel(Channel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "123");
            notification.setContentTitle(title);
            notification.setContentText(content);
            notification.setWhen(System.currentTimeMillis());
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));

            manager.notify(123, notification.build());
        } else {
            Notification notification = new Notification.Builder(context)
                    .setContentTitle(title)                                                                     //设置标题
                    .setContentText(content)                                                                    //设置内容
                    .setWhen(System.currentTimeMillis())                                                        //设置创建时间
                    .setSmallIcon(R.mipmap.ic_launcher)                                                         //设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))   //设置通知栏图标
                    .build();

            manager.notify(123, notification);
        }
    }
}
