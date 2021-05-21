package com.zhongbenshuo.bulletinboard.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.constant.NetWork;
import com.zhongbenshuo.bulletinboard.interfaces.IMsgWebSocket;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 实时数据同步服务
 * Created at 2019/9/17 17:14
 *
 * @author LiYuliang
 * @version 1.0
 */

public class WebSocketService extends Service {

    private String TAG = "WebSocketService";
    private WebSocketServiceBinder webSocketServiceBinder;

    private WebSocketClient msgWebSocketClient;
    private ExecutorService executorService;
    private boolean flag = true;

    @Override
    public IBinder onBind(Intent intent) {
        return webSocketServiceBinder;
    }

    public class WebSocketServiceBinder extends Binder {
        /**
         * WebSocketServiceBinder
         *
         * @return SocketService对象
         */
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), (r) -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
        showNotification();
        webSocketServiceBinder = new WebSocketServiceBinder();
        initWebSocket();
    }

    /**
     * 前台Service
     */
    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel Channel = new NotificationChannel("124", getString(R.string.RealTimeSyncService), NotificationManager.IMPORTANCE_NONE);
            Channel.enableLights(true);                                         //设置提示灯
            Channel.setLightColor(Color.RED);                                   //设置提示灯颜色
            Channel.setShowBadge(true);                                         //显示logo
            Channel.setDescription(getString(R.string.RealTimeSyncService));    //设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);    //设置锁屏不可见 VISIBILITY_SECRET=不可见
            manager.createNotificationChannel(Channel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "124");
            notification.setContentTitle(getString(R.string.app_name));
            notification.setContentText(getString(R.string.RealTimeSyncServiceRunning));
            notification.setWhen(System.currentTimeMillis());
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            startForeground(124, notification.build());
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))                                      //设置标题
                    .setContentText(getString(R.string.RealTimeSyncServiceRunning))                     //设置内容
                    .setWhen(System.currentTimeMillis())                                                //设置创建时间
                    .setSmallIcon(R.mipmap.ic_launcher)                                                 //设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))   //设置通知栏图标
                    .build();
            startForeground(124, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 初始化并启动启动WebSocket
     */
    public void initWebSocket() {
        try {
            msgWebSocketClient = new WebSocketClient("wss://" + NetWork.SERVER_DOMAIN_NAME + ":" + NetWork.PORT_WEBSOCKET + "/" + NetWork.NAME_WEBSOCKET, new IMsgWebSocket() {
                @Override
                public void openSuccess() {
                    // websocket连接成功

                }

                @Override
                public void closed() {
                    // 执行重连
                    reConnect();
                }
            });
            msgWebSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重连WebSocket
     */
    public void reConnect() {
        // 如果程序退出了就不要重连了
        Runnable runnable = () -> {
            try {
                if (flag) {
                    LogUtils.d(TAG, "Message————————————————WebSocket重连");
                    Thread.sleep(NetWork.WEBSOCKET_RECONNECT_RATE);
                    msgWebSocketClient.reconnectBlocking();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        executorService.submit(runnable);
    }

    /**
     * 关闭WebSocket
     */
    public void closeWebSocket() {
        if (msgWebSocketClient != null) {
            LogUtils.d(TAG, "手动关闭WebSocket");
            msgWebSocketClient.close();
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "WebSocketService——————生命周期——————:onDestroy");
        executorService.shutdown();
        flag = false;
        closeWebSocket();
        super.onDestroy();
    }

}