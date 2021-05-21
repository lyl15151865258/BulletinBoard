package com.zhongbenshuo.bulletinboard.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.activity.MainActivity;
import com.zhongbenshuo.bulletinboard.constant.ApkInfo;
import com.zhongbenshuo.bulletinboard.contentprovider.SPHelper;
import com.zhongbenshuo.bulletinboard.interfaces.DownloadProgress;
import com.zhongbenshuo.bulletinboard.network.download.DownloadCallBack;
import com.zhongbenshuo.bulletinboard.network.download.RetrofitHttp;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import java.io.File;

/**
 * 文件下载服务
 * Created at 2019/9/28 15:19
 *
 * @author LiYuliang
 * @version 1.0
 */

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";
    private Context mContext;
    private NotificationManager mNotifyManager;
    private Notification mNotification;
    private DownloadProgress downloadProgress = MainActivity.downloadProgress;

    public DownloadService() {
        super("InitializeService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String downloadUrl = intent.getExtras().getString("download_url");
        final int downloadId = intent.getExtras().getInt("download_id");
        String mDownloadFileName = intent.getExtras().getString("download_file");

        LogUtils.d(TAG, "download_url --" + downloadUrl + "，download_file --" + mDownloadFileName);

        File file = new File(ApkInfo.APP_ROOT_PATH + ApkInfo.DOWNLOAD_DIR, mDownloadFileName);
        long range = 0;
        int progress = 0;
        if (file.exists()) {
            range = SPHelper.getLong(downloadUrl, 0);
            progress = (int) (range * 100 / file.length());
            if (range == file.length()) {
                LogUtils.d(TAG, "文件已存在且完整");
                downloadProgress.downloadFinish();
                return;
            } else {
                LogUtils.d(TAG, "文件已存在但是不完整");
            }
        }

        LogUtils.d(TAG, "range = " + range);
        downloadProgress.downloadStart();

        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notify_download);
        remoteViews.setProgressBar(R.id.pb_progress, 100, progress, false);
        remoteViews.setTextViewText(R.id.tv_progress, "已下载" + progress + "%");

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification(remoteViews, mNotifyManager);
        mNotifyManager.notify(downloadId, mNotification);

        RetrofitHttp.getInstance().downloadFile(range, downloadUrl, mDownloadFileName, new DownloadCallBack() {
            @Override
            public void onProgress(int progress) {
                remoteViews.setProgressBar(R.id.pb_progress, 100, progress, false);
                remoteViews.setTextViewText(R.id.tv_progress, "已下载" + progress + "%");
                mNotifyManager.notify(downloadId, mNotification);
                LogUtils.d(TAG, "已下载" + progress + "%");
            }

            @Override
            public void onCompleted() {
                LogUtils.d(TAG, "下载完成");
                mNotifyManager.cancel(downloadId);
                downloadProgress.downloadFinish();
            }

            @Override
            public void onError(String msg) {
                mNotifyManager.cancel(downloadId);
                LogUtils.d(TAG, "下载发生错误--" + msg);
            }
        });
    }


    /**
     * 前台Service
     */
    private void showNotification(RemoteViews remoteViews, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("135", "更新软件", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(true);                                         //设置提示灯
            notificationChannel.enableVibration(false);                                     //设置震动
            notificationChannel.setSound(null, null);               //设置声音
            notificationChannel.setLightColor(Color.RED);                                   //设置提示灯颜色
            notificationChannel.setShowBadge(true);                                         //显示logo
            notificationChannel.setDescription("软件更新异步线程");                         //设置描述
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);    //设置锁屏不可见 VISIBILITY_SECRET=不可见
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext, "135");
            notification.setContentTitle(getString(R.string.app_name));
            notification.setContentText("正在下载新版本");
            notification.setWhen(System.currentTimeMillis());
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            notification.setContent(remoteViews);
            notification.setDefaults(Notification.DEFAULT_LIGHTS);
            notification.setOnlyAlertOnce(true);
            mNotification = notification.build();
        } else {
            mNotification = new Notification.Builder(mContext)
                    .setContentTitle(getString(R.string.app_name))                                      //设置标题
                    .setContentText("正在下载新版本")                                                   //设置内容
                    .setWhen(System.currentTimeMillis())                                                //设置创建时间
                    .setSmallIcon(R.mipmap.ic_launcher)                                                 //设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))   //设置通知栏图标
                    .setContent(remoteViews)
                    .build();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "下载服务已关闭");
    }
}