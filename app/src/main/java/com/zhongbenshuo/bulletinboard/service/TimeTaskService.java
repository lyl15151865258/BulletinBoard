package com.zhongbenshuo.bulletinboard.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.gson.JsonObject;
import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.bean.BoardData;
import com.zhongbenshuo.bulletinboard.bean.EventMsg;
import com.zhongbenshuo.bulletinboard.bean.ProjectAnnouncement;
import com.zhongbenshuo.bulletinboard.bean.Result;
import com.zhongbenshuo.bulletinboard.bean.Weather;
import com.zhongbenshuo.bulletinboard.bean.AllUserInfoStatus;
import com.zhongbenshuo.bulletinboard.bean.AllUserInfoStatusResult;
import com.zhongbenshuo.bulletinboard.bean.ShowData;
import com.zhongbenshuo.bulletinboard.constant.Constants;
import com.zhongbenshuo.bulletinboard.constant.ErrorCode;
import com.zhongbenshuo.bulletinboard.constant.NetWork;
import com.zhongbenshuo.bulletinboard.network.ExceptionHandle;
import com.zhongbenshuo.bulletinboard.network.NetClient;
import com.zhongbenshuo.bulletinboard.network.NetworkObserver;
import com.zhongbenshuo.bulletinboard.utils.ActivityController;
import com.zhongbenshuo.bulletinboard.utils.GsonUtils;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;
import com.zhongbenshuo.bulletinboard.utils.TimeUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.zhongbenshuo.bulletinboard.bean.ShowData.EMPLOYEE;
import static com.zhongbenshuo.bulletinboard.bean.ShowData.POSITION;

/**
 * 定时任务服务（定时同步服务器数据，定时重启）
 * Created at 2019/9/12 13:48
 *
 * @author LiYuliang
 * @version 1.0
 */

public class TimeTaskService extends Service {

    private final String TAG = "TimeTaskService";
    private Context mContext;
    private TimeTaskServiceBinder timeTaskServiceBinder;
    private ScheduledExecutorService threadPool;
    private static final int betweenTime = 1;                //间隔1秒执行一次
    private List<ShowData> showDataList;
    private List<ProjectAnnouncement> projectAnnouncementList;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return timeTaskServiceBinder;
    }

    public class TimeTaskServiceBinder extends Binder {
        /**
         * TimeTaskServiceBinder
         *
         * @return SocketService对象
         */
        public TimeTaskService getService() {
            return TimeTaskService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "TimeTaskService:onCreate");
        mContext = this;
        threadPool = Executors.newScheduledThreadPool(3);
        showNotification();
        executeShutDown();
        showDataList = new ArrayList<>();
        projectAnnouncementList = new ArrayList<>();
        timeTaskServiceBinder = new TimeTaskServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG, "TimeTaskService:onStartCommand");
        searchWeather();
        getEmployeeStatusByDepartment();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 前台Service
     */
    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel Channel = new NotificationChannel("124", getString(R.string.TimeTaskService), NotificationManager.IMPORTANCE_NONE);
            Channel.enableLights(true);                                         //设置提示灯
            Channel.setLightColor(Color.RED);                                   //设置提示灯颜色
            Channel.setShowBadge(true);                                         //显示logo
            Channel.setDescription(getString(R.string.TimeTaskService));        //设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);    //设置锁屏不可见 VISIBILITY_SECRET=不可见
            manager.createNotificationChannel(Channel);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext, "124");
            notification.setContentTitle(getString(R.string.app_name));
            notification.setContentText(getString(R.string.TimeTaskServiceRunning));
            notification.setWhen(System.currentTimeMillis());
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            startForeground(124, notification.build());
        } else {
            Notification notification = new Notification.Builder(mContext)
                    .setContentTitle(getString(R.string.app_name))                                      //设置标题
                    .setContentText(getString(R.string.TimeTaskServiceRunning))                         //设置内容
                    .setWhen(System.currentTimeMillis())                                                //设置创建时间
                    .setSmallIcon(R.mipmap.ic_launcher)                                                 //设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))   //设置通知栏图标
                    .build();
            startForeground(124, notification);
        }
    }

    @SuppressLint("WrongConstant")
    public void executeShutDown() {
        threadPool.scheduleAtFixedRate(() -> {
            String currentTime = TimeUtils.getCurrentTime();
            LogUtils.d(TAG, "当前时间：" + currentTime);
            // 每5分钟查询一次实时天气
            if (currentTime.split(":")[1].endsWith("0") || currentTime.split(":")[1].endsWith("5")) {
                searchWeather();
            }
            // 每天06:00重启APP
            if (currentTime.equals("06:00:00")) {
                restartApp();
            }
            // 每10秒钟查询一次
            if (currentTime.endsWith("0")) {
                getEmployeeStatusByDepartment();
            }
            // 每30秒翻页一次
            if (currentTime.split(":")[2].equals("00")||currentTime.split(":")[2].equals("30")){
                EventMsg msg = new EventMsg();
                msg.setTag(Constants.CHANGE_PAGE);
                EventBus.getDefault().post(msg);
            }
        }, 0, betweenTime, TimeUnit.SECONDS);
    }

    /**
     * 查询天气
     */
    public void searchWeather() {
        LogUtils.d(TAG, "查询天气");
        Observable<Weather> resultObservable = NetClient.getInstance(NetClient.BASE_URL_WEATHER, false, false).getZbsApi().searchWeather(NetWork.GAODE_WEB, "440118", "base", "JSON");
        resultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkObserver<Weather>(mContext) {
            @Override
            public void onSubscribe(Disposable d) {
                LogUtils.d(TAG, "定时任务，开始查询实时天气");
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                LogUtils.d(TAG, "定时任务，实时天气查询失败");
            }

            @Override
            public void onNext(Weather weather) {
                super.onNext(weather);
                if (weather.getStatus().equals("1")) {
                    LogUtils.d(TAG, "定时任务，实时天气获取成功");
                    EventMsg msg = new EventMsg();
                    msg.setTag(Constants.SHOW_WEATHER);
                    msg.setMsg(GsonUtils.convertJSON(weather));
                    EventBus.getDefault().post(msg);
                } else {
                    LogUtils.d(TAG, "定时任务，实时天气获取失败");
                }
            }
        });
    }

    /**
     * 根据部门查询员工状态
     */
    private void getEmployeeStatusByDepartment() {
        JsonObject params = new JsonObject();
        params.addProperty("companyId", 1);
        params.addProperty("departmentName", "项目部—广州明治");
        Observable<Result> resultObservable = NetClient.getInstance(NetClient.getBaseUrlProject(), false, true).getZbsApi().getEmployeeStatusByDepartment(params);
        resultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkObserver<Result>(mContext) {

            @Override
            public void onSubscribe(Disposable d) {
                LogUtils.d(TAG, "定时任务，开始查询人员状态");
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                LogUtils.d(TAG, "定时任务，查询人员状态失败");
            }

            @Override
            public void onNext(Result result) {
                super.onNext(result);
                if (result.getCode() == ErrorCode.SUCCESS) {
                    AllUserInfoStatusResult allUserInfoStatusResult = GsonUtils.parseJSON(GsonUtils.convertJSON(result.getData()), AllUserInfoStatusResult.class);
                    List<AllUserInfoStatus> employeeStatusList = allUserInfoStatusResult.getEmployeeStatusList();
                    projectAnnouncementList.clear();
                    projectAnnouncementList.addAll(allUserInfoStatusResult.getProjectAnnouncementList());
                    showDataList.clear();
                    // 遍历部门以及部门里面的用户列表，删除用户数量为0的部门
                    Iterator<AllUserInfoStatus> iterable = employeeStatusList.iterator();
                    while ((iterable).hasNext()) {
                        AllUserInfoStatus value = iterable.next();
                        // 如果部门人员数量为0，则删除这个部门
                        if (value.getUsers().size() == 0) {
                            iterable.remove();
                        } else {
                            showDataList.add(new ShowData(POSITION, value.getPosition()));
                            for (int i = 0; i < value.getUsers().size(); i++) {
                                showDataList.add(new ShowData(EMPLOYEE, value.getUsers().get(i)));
                            }
                        }
                    }
                    // 构造显示对象
                    BoardData boardData = new BoardData();
                    boardData.setProjectAnnouncementList(projectAnnouncementList);
                    boardData.setShowDataList(showDataList);
                    // 发送给页面展示
                    EventMsg msg = new EventMsg();
                    msg.setTag(Constants.SHOW_USER_STATUS);
                    msg.setMsg(GsonUtils.convertJSON(boardData));
                    EventBus.getDefault().post(msg);
                    LogUtils.d(TAG, "定时任务，查询人员状态成功");
                } else if (result.getCode() == ErrorCode.FAIL) {
                    LogUtils.d(TAG, "定时任务，查询人员状态失败");
                }
            }
        });
    }

    /**
     * 重启APP
     */
    private void restartApp() {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 300, restartIntent);
            ActivityController.exit(mContext);
            ActivityManager am = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses("com.zhongbenshuo.bulletinboard");
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void onDestroy() {
        threadPool.shutdown();
        threadPool = null;
        super.onDestroy();
    }

}