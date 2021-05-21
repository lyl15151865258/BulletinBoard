package com.zhongbenshuo.bulletinboard.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.ywl5320.wlmedia.WlMedia;
import com.ywl5320.wlmedia.enums.WlCodecType;
import com.ywl5320.wlmedia.enums.WlMute;
import com.ywl5320.wlmedia.enums.WlPlayModel;
import com.ywl5320.wlmedia.enums.WlTransportModel;
import com.ywl5320.wlmedia.listener.WlOnPcmDataListener;
import com.ywl5320.wlmedia.listener.WlOnVideoViewListener;
import com.ywl5320.wlmedia.surface.WlSurfaceView;
import com.zhongbenshuo.bulletinboard.BuildConfig;
import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.adapter.HistoryDataAdapter;
import com.zhongbenshuo.bulletinboard.adapter.RealDataAdapter;
import com.zhongbenshuo.bulletinboard.adapter.StationAdapter;
import com.zhongbenshuo.bulletinboard.bean.Environment;
import com.zhongbenshuo.bulletinboard.bean.EventMsg;
import com.zhongbenshuo.bulletinboard.bean.OpenAndCloseDoorRecord;
import com.zhongbenshuo.bulletinboard.bean.RealData;
import com.zhongbenshuo.bulletinboard.bean.Result;
import com.zhongbenshuo.bulletinboard.bean.Station;
import com.zhongbenshuo.bulletinboard.bean.VersionInfo;
import com.zhongbenshuo.bulletinboard.bean.VersionResult;
import com.zhongbenshuo.bulletinboard.bean.Weather;
import com.zhongbenshuo.bulletinboard.constant.ApkInfo;
import com.zhongbenshuo.bulletinboard.constant.Constants;
import com.zhongbenshuo.bulletinboard.constant.ErrorCode;
import com.zhongbenshuo.bulletinboard.glide.RoundedCornersTransformation;
import com.zhongbenshuo.bulletinboard.interfaces.DownloadProgress;
import com.zhongbenshuo.bulletinboard.network.ExceptionHandle;
import com.zhongbenshuo.bulletinboard.network.NetClient;
import com.zhongbenshuo.bulletinboard.network.NetworkObserver;
import com.zhongbenshuo.bulletinboard.service.DownloadService;
import com.zhongbenshuo.bulletinboard.service.TimeTaskService;
import com.zhongbenshuo.bulletinboard.service.WebSocketService;
import com.zhongbenshuo.bulletinboard.utils.ActivityController;
import com.zhongbenshuo.bulletinboard.utils.ApkUtils;
import com.zhongbenshuo.bulletinboard.utils.FileUtil;
import com.zhongbenshuo.bulletinboard.utils.GsonUtils;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;
import com.zhongbenshuo.bulletinboard.utils.NetworkUtil;
import com.zhongbenshuo.bulletinboard.utils.TimeUtils;
import com.zhongbenshuo.bulletinboard.widget.ClockView;
import com.zhongbenshuo.bulletinboard.widget.SelectDialog;
import com.zhongbenshuo.bulletinboard.widget.UpgradeVersionDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.zhongbenshuo.bulletinboard.glide.RoundedCornersTransformation.CENTER_CROP;
import static com.zhongbenshuo.bulletinboard.glide.RoundedCornersTransformation.CORNER_ALL;

/**
 * 主页面，环境监测页面
 * Created at 2019/9/24 18:21
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MainActivity extends BaseActivity {

    private Context mContext;
    private RecyclerView rvPosition;
    private SparseArray<List<Environment>> environmentListMap;
    private List<Station> stationList;
    private List<Environment> environmentList;
    private List<RealData> realDataList;
    private StationAdapter stationAdapter;
    private HistoryDataAdapter historyDataAdapter;
    private RealDataAdapter realDataAdapter;
    private int selectedStation = 0;
    private ClockView cvIlluminance;
    private ImageView ivWeather, ivUser;
    private TextView tvCity, tvWeather, tvTemperature, tvWind, tvHumidity;

    private static boolean flag = true;
    // 用于自动点击Item的定时任务
    private SyncTimeTask syncTimeTask;

    // 每个监测点停留时间
    private static final int WAIT_TIME_SECONDS = 10;
    // 门铃和照片显示时间
    private static final int PHOTO_SHOW_TIME_SECONDS = 8;
    // 视频显示时间
    private static final int VIDEO_SHOW_TIME_SECONDS = 16;

    // 定时任务执行时间
    private static volatile long seconds = 0;
    private static volatile int photoShowTime = 0;
    private static volatile int videoShowTime = 0;

    // 标记当前是否有人按门铃
    private boolean doorBellPressed = false;

    private MediaPlayer mediaPlayer;
    private WlMedia wlMedia;
    private WlSurfaceView wlSurfaceView;
    private LinearLayout llRealData;
    private ImageView ivNetWork;

    private NetBroadcastReceiver netBroadcastReceiver;

    // 版本更新相关信息
    private String latestVersionName, latestVersionMD5, latestVersionLog, apkDownloadPath, latestFileName;
    private int myVersionCode, latestVersionCode;

    private static final int GET_UNKNOWN_APP_SOURCES = 2;
    protected static final int INSTALL_PACKAGES_REQUEST_CODE = 103;

    public static DownloadProgress downloadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        environmentListMap = new SparseArray<>();

        rvPosition = findViewById(R.id.rvPosition);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvPosition.setLayoutManager(linearLayoutManager);
        stationList = new ArrayList<>();
        stationList.add(new Station(10000, "门厅监控", true, Station.TYPE_CAMERA));
        stationAdapter = new StationAdapter(this, stationList, selectedStation);
        stationAdapter.setOnItemClickListener(onItemClickListener);
        rvPosition.setAdapter(stationAdapter);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(mContext);
        linearLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvHistory.setLayoutManager(linearLayoutManager2);
        environmentList = new ArrayList<>();
        historyDataAdapter = new HistoryDataAdapter(this, environmentList);
        rvHistory.setAdapter(historyDataAdapter);

        RecyclerView rvRealTime = findViewById(R.id.rvRealTime);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
        rvRealTime.setLayoutManager(gridLayoutManager);
        realDataList = new ArrayList<>();
        realDataAdapter = new RealDataAdapter(this, realDataList);
        rvRealTime.setAdapter(realDataAdapter);

        cvIlluminance = findViewById(R.id.cvIlluminance);
        ivWeather = findViewById(R.id.ivWeather);
        ivUser = findViewById(R.id.ivUser);

        tvCity = findViewById(R.id.tvCity);
        tvWeather = findViewById(R.id.tvWeather);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 对于Android 8.0+
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setOnAudioFocusChangeListener(focusChangeListener).build();
            audioFocusRequest.acceptsDelayedFocusGain();
            audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            // 小于Android 8.0
            int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // could not get audio focus.
            }
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initWebSocketService();
        initTimeTaskService();

        syncTimeTask = new SyncTimeTask(this);
        syncTimeTask.execute();

        llRealData = findViewById(R.id.llRealData);
        wlSurfaceView = findViewById(R.id.wlsurfaceview);
        wlSurfaceView.setVisibility(View.VISIBLE);

        wlMedia = new WlMedia();
        wlMedia.setPlayModel(WlPlayModel.PLAYMODEL_AUDIO_VIDEO);        //声音视频都播放
        wlMedia.setCodecType(WlCodecType.CODEC_MEDIACODEC);             //优先使用硬解码
        wlMedia.setMute(WlMute.MUTE_CENTER);                            //立体声
        wlMedia.setVolume(100);                                         //100%音量
        wlMedia.setTransportModel(WlTransportModel.TRANSPORT_MODEL_TCP);//TCP模式
        wlSurfaceView.setWlMedia(wlMedia);                              //给视频surface设置播放器

        wlMedia.setOnPreparedListener(() -> {
            wlMedia.start();
        });

        wlMedia.setOnLoadListener(load -> {
            if (load) {
                LogUtils.d(TAG, "WlMedia：加载中");
            } else {
                LogUtils.d(TAG, "WlMedia：播放中");
            }
        });

        wlMedia.setOnErrorListener((code, msg) -> {
            LogUtils.d(TAG, "WlMedia：出错，code is :" + code + ", msg is :" + msg);
            change();
        });

        wlMedia.setOnCompleteListener(() -> {
            LogUtils.d(TAG, "WlMedia：播放完成");
            change();
        });

        wlMedia.setOnPcmDataListener(new WlOnPcmDataListener() {
            @Override
            public void onPcmInfo(int bit, int channel, int samplerate) {
                LogUtils.d(TAG, "WlMedia：pcm info samplerate :" + samplerate);
            }

            @Override
            public void onPcmData(int size, byte[] data) {
                LogUtils.d(TAG, "WlMedia：pcm data size :" + size);
            }
        });

        wlSurfaceView.setOnVideoViewListener(new WlOnVideoViewListener() {
            @Override
            public void initSuccess() {
                play();
            }

            @Override
            public void moveSlide(double value) {

            }

            @Override
            public void movdFinish(double value) {
                wlMedia.seek((int) value);
            }
        });

        ivNetWork = findViewById(R.id.ivNetWork);

        netBroadcastReceiver = new NetBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, intentFilter);

        // 当前版本
        myVersionCode = ApkUtils.getVersionCode(mContext);

        downloadProgress = new DownloadProgress() {
            @Override
            public void downloadStart() {
                runOnUiThread(() -> showToast("正在后台下载"));
            }

            @Override
            public void downloadFinish() {
                runOnUiThread(() -> checkIsAndroidO());
            }
        };
        searchNewVersion(false);
    }

    /**
     * 播放视频流
     */
    public void play() {
        wlMedia.setSource("rtsp://admin:fengyinhua504@192.168.2.254:554/h265/Streaming/Channels/101");
        wlMedia.prepared();
    }

    /**
     * 切换视频流
     */
    public void change() {
        wlMedia.setSource("rtsp://admin:fengyinhua504@192.168.2.254:554/h265/Streaming/Channels/101");
        wlMedia.next();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wlMedia != null) {
            wlMedia.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wlMedia != null) {
            wlMedia.pause();
        }
    }

    private StationAdapter.OnItemClickListener onItemClickListener = position -> {
        selectedStation = position;
        seconds = 0;
        // 平滑地将点击的item滚动到中间
        rvPosition.smoothScrollToPosition(position);
        stationAdapter.setSelectedPosition(position);
        refreshPage(null);
    };

    /**
     * 收到EventBus发来的消息并处理
     *
     * @param msg 消息对象
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(EventMsg msg) {
        if (msg.getTag().equals(Constants.CONNECT_OPEN_WEBSOCKET)) {
            //接收到这个消息说明连接成功
            LogUtils.d(TAG, "WebSocket连接成功");
        }
        if (msg.getTag().equals(Constants.CONNECT_CLOSE_WEBSOCKET)) {
            //接收到这个消息说明连接失败或者中断了
            LogUtils.d(TAG, "WebSocket关闭");
        }
        if (msg.getTag().equals(Constants.CONNECT_FAIL_WEBSOCKET)) {
            //接收到这个消息说明连接出错了
            LogUtils.d(TAG, "WebSocket连接错误");
        }
        if (msg.getTag().equals(Constants.SHOW_TOAST_WEBSOCKET)) {
            //接收到这个消息说明是非Websocket对象的数据
            LogUtils.d(TAG, msg.getMsg());
        }
        if (msg.getTag().equals(Constants.SHOW_DATA_WEBSOCKET)) {
            //接收到这个消息说明需要展示数据
            Environment environment = GsonUtils.parseJSON(msg.getMsg(), Environment.class);
            // 通过站号过滤无效的数据（station为0的时候，表示这条数据在服务端解析异常）
            if (environment.getStation() > 0) {
                if (environmentListMap.get(environment.getStation()) == null) {
                    List<Environment> environments = new ArrayList<>();
                    environments.add(0, environment);
                    environmentListMap.put(environment.getStation(), environments);
                } else {
                    environmentListMap.get(environment.getStation()).add(0, environment);
                }
                // 最多保留4条
                if (environmentListMap.get(environment.getStation()).size() > 4) {
                    environmentListMap.get(environment.getStation()).remove(environmentListMap.get(environment.getStation()).size() - 1);
                }
                // 刷新页面
                refreshPage(new Station(environment.getStation(), environment.getStation_name(), environment.isState(), Station.TYPE_AIR));
            }
        }
        if (msg.getTag().equals(Constants.SHOW_USER_PHOTO)) {
            //接收到这个消息说明有人按门铃
            String url = msg.getMsg();
            if (!TextUtils.isEmpty(url)) {
                ivUser.setVisibility(View.VISIBLE);
                refreshPage(null);
                LogUtils.d(TAG, "展示照片：" + NetClient.getBaseUrl() + url.replace("\\", "/"));
                RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(10, 0, CORNER_ALL, CENTER_CROP);
                RequestOptions options = new RequestOptions().dontAnimate().transform(roundedCornersTransformation);
                Glide.with(mContext).load(NetClient.getBaseUrl() + url.replace("\\", "/")).apply(options).into(ivUser);
            }
            // 播放门铃音乐
            mediaPlayer = MediaPlayer.create(mContext, R.raw.bell);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            photoShowTime = 0;
            videoShowTime = 0;
            doorBellPressed = true;
        }
        if (msg.getTag().equals(Constants.SHOW_WEATHER)) {
            //接收到实时天气信息
            LogUtils.d(TAG, "获取到实时天气信息");
            Weather weather = GsonUtils.parseJSON(msg.getMsg(), Weather.class);
            if (weather.getStatus().equals("1") && weather.getInfocode().equals("10000") && Integer.valueOf(weather.getCount()) > 0) {
                Weather.Lives lives = weather.getLives().get(0);

                tvCity.setText(lives.getCity());
                tvWeather.setText(lives.getWeather());
                tvTemperature.setText(lives.getTemperature() + "℃");
                if (lives.getWinddirection().equals("无风向") || lives.getWinddirection().equals("旋转不定")) {
                    tvWind.setText(lives.getWinddirection());
                } else {
                    tvWind.setText(lives.getWinddirection() + "风" + lives.getWindpower() + "级");
                }
                tvHumidity.setText("湿度" + lives.getHumidity() + "%");

                ivWeather.setVisibility(View.VISIBLE);
                if ("晴".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.qinw);
                } else if ("多云".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.duoyunw);
                } else if ("阴".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.yinw);
                } else if ("阵雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.zhenyuw);
                } else if ("雷阵雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.leizhenyuw);
                } else if ("雷阵雨伴有冰雹".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.leizhenyubanyoubinpaow);
                } else if ("雨夹雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.yujiaxuew);
                } else if ("小雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.xiaoyuw);
                } else if ("中雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.zhongyuw);
                } else if ("大雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.dayuw);
                } else if ("暴雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.baoyuw);
                } else if ("大暴雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.dabaoyuw);
                } else if ("特大暴雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.tedabaoyuw);
                } else if ("阵雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.zhenxuew);
                } else if ("小雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.xiaoxuew);
                } else if ("中雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.zhongxuew);
                } else if ("大雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.daxuew);
                } else if ("暴雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.baoxuew);
                } else if ("雾".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.wuw);
                } else if ("冻雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.dongyuw);
                } else if ("沙尘暴".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.shachenbaow);
                } else if ("小到中雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.zhongyuw);
                } else if ("中到大雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.dayuw);
                } else if ("大到暴雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.baoyuw);
                } else if ("暴雨到大暴雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.dabaoyuw);
                } else if ("大暴雨到特大暴雨".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.tedabaoyuw);
                } else if ("小到中雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.zhongxuew);
                } else if ("中到大雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.daxuew);
                } else if ("大到暴雪".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.baoxuew);
                } else if ("浮尘".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.fuchenw);
                } else if ("扬沙".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.yangshaw);
                } else if ("强沙尘暴".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.qiangshachenbaow);
                } else if ("霾".equals(lives.getWeather())) {
                    ivWeather.setImageResource(R.drawable.maiw);
                } else {
                    ivWeather.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 刷新页面
     *
     * @param station 环境监测站对象
     */
    private void refreshPage(Station station) {
        if (station != null && !stationList.contains(station)) {
            stationList.add(station);
        }
        // 刷新站点列表
        Collections.sort(stationList);
        stationAdapter.notifyDataSetChanged();

        if (stationList.get(selectedStation).getStationId() == 10000 || doorBellPressed) {
            // 如果当前选中的是视频监控页面有人按门铃
            llRealData.setVisibility(View.INVISIBLE);
            wlSurfaceView.setVisibility(View.VISIBLE);
        } else {
            // 隐藏视频监控页面
            llRealData.setVisibility(View.VISIBLE);
            wlSurfaceView.setVisibility(View.INVISIBLE);
        }

        // 如果当前选中的不是监控页面
        if (stationList.get(selectedStation).getStationId() != 10000) {
            // 刷新历史数据列表
            environmentList.clear();
            environmentList.addAll(environmentListMap.get(stationList.get(selectedStation).getStationId()));
            historyDataAdapter.notifyDataSetChanged();

            // 刷新实时数据页面
            if (environmentListMap.get(stationList.get(selectedStation).getStationId()).size() > 0) {
                realDataList.clear();
                Environment environment = environmentListMap.get(stationList.get(selectedStation).getStationId()).get(0);
                realDataList.add(new RealData("温度", RealData.DATA_TYPE.TYPE_TEMP, environment.getTemperature(), "℃"));
                realDataList.add(new RealData("湿度", RealData.DATA_TYPE.TYPE_HUMIDITY, environment.getHumidity(), "%"));
                realDataList.add(new RealData("PM2.5", RealData.DATA_TYPE.TYPE_PM25, environment.getPm25(), "μg/m³"));
                realDataList.add(new RealData("PM10", RealData.DATA_TYPE.TYPE_PM10, environment.getPm10(), "μg/m³"));
                realDataList.add(new RealData("甲醛", RealData.DATA_TYPE.TYPE_HCHO, environment.getFormaldehyde(), "mg/m³"));
                realDataList.add(new RealData("二氧化碳", RealData.DATA_TYPE.TYPE_CO2, environment.getCarbonDioxide(), "ppm"));
                realDataAdapter.notifyDataSetChanged();
                cvIlluminance.setTitle("光照度");
                cvIlluminance.setCompleteDegree(environment.getIlluminance(), "lux");
                cvIlluminance.setColor(mContext.getResources().getColor(R.color.value_low), mContext.getResources().getColor(R.color.value_low), mContext.getResources().getColor(R.color.value_low));
                cvIlluminance.setValue(0, 2000, 0, 0);
                cvIlluminance.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 初始化并绑定WebSocketService
     */
    private void initWebSocketService() {
        Intent intent = new Intent(mContext, WebSocketService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * 初始化并绑定TimeTaskService
     */
    private void initTimeTaskService() {
        Intent intent = new Intent(mContext, TimeTaskService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    // 无限循环的定时任务
    private static class SyncTimeTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<MainActivity> mainActivityWeakReference;

        private SyncTimeTask(MainActivity activity) {
            mainActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (flag) {
                if (isCancelled()) {
                    break;
                }
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                seconds++;
                photoShowTime++;
                videoShowTime++;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate();
            if (isCancelled()) {
                return;
            }
            MainActivity mainActivity = mainActivityWeakReference.get();
            LogUtils.d(mainActivity.TAG, "当前seconds为：" + seconds);
            LogUtils.d(mainActivity.TAG, "当前photoShowTime为：" + photoShowTime);
            LogUtils.d(mainActivity.TAG, "当前videpShowTime为：" + videoShowTime);
            // 如果当前选中的不是监控
            if (mainActivity.stationList.get(mainActivity.selectedStation).getStationId() != 10000) {
                if (mainActivity.stationList.size() != 0 && seconds % WAIT_TIME_SECONDS == 0) {
                    LogUtils.d(mainActivity.TAG, "跳转到下一个");
                    // 跳转到下一个监测点，如果是监测点的最后一个，则跳转到监测点的第一个
                    if (mainActivity.selectedStation == mainActivity.stationList.size() - 2) {
                        // 表示当前选中的是最后一个监测点
                        LogUtils.d(mainActivity.TAG, "当前在最后一个，跳转到第一个");
                        mainActivity.selectedStation = 0;
                    } else if (mainActivity.selectedStation < mainActivity.stationList.size() - 2) {
                        // 表示当前选中的不是最后一个
                        LogUtils.d(mainActivity.TAG, "当前不在最后一个，跳转到下一个");
                        mainActivity.selectedStation++;
                    }
                    // 平滑地将这个的item滚动到中间
                    mainActivity.rvPosition.smoothScrollToPosition(mainActivity.selectedStation);
                    mainActivity.stationAdapter.setSelectedPosition(mainActivity.selectedStation);
                    mainActivity.refreshPage(null);
                }
            }
            // 控制视频监控和实时数据显示的切换
            if (mainActivity.stationList.get(mainActivity.selectedStation).getStationId() == 10000 || mainActivity.doorBellPressed) {
                // 如果当前选中的是视频监控页面有人按门铃
                mainActivity.llRealData.setVisibility(View.INVISIBLE);
                mainActivity.wlSurfaceView.setVisibility(View.VISIBLE);
            } else {
                // 隐藏视频监控页面
                mainActivity.llRealData.setVisibility(View.VISIBLE);
                mainActivity.wlSurfaceView.setVisibility(View.INVISIBLE);
            }
            // 照片和铃声
            if (photoShowTime == PHOTO_SHOW_TIME_SECONDS) {
                mainActivity.ivUser.setVisibility(View.GONE);
                if (mainActivity.mediaPlayer != null) {
                    mainActivity.mediaPlayer.stop();
                    mainActivity.mediaPlayer.release();
                    mainActivity.mediaPlayer = null;
                }
                mainActivity.doorBellPressed = false;
            }
            // 视频显示
            if (videoShowTime == VIDEO_SHOW_TIME_SECONDS) {
                // 隐藏视频监控页面
                if (mainActivity.stationList.get(mainActivity.selectedStation).getStationId() != 10000) {
                    mainActivity.llRealData.setVisibility(View.VISIBLE);
                    mainActivity.wlSurfaceView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    //焦点问题
    private AudioManager.OnAudioFocusChangeListener focusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 长时间失去
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 短时间失去
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 暂时失去 audio focus，但是允许持续播放音频(以很小的声音)，不需要完全停止播放。
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.setVolume(0.1f, 0.1f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 获得音频焦点
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开关门
     *
     * @param status 1、开  2、关
     */
    private void openAndCloseDoorRecord(int status) {
        OpenAndCloseDoorRecord openAndCloseDoorRecord = new OpenAndCloseDoorRecord();
        openAndCloseDoorRecord.setUser_id(999999999);
        openAndCloseDoorRecord.setCreateTime(TimeUtils.getCurrentDateTime());
        openAndCloseDoorRecord.setStatus(status);

        Observable<Result> resultObservable = NetClient.getInstance(NetClient.getBaseUrlProject(), false, true).getZbsApi().openAndCloseDoorRecord(openAndCloseDoorRecord);
        resultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkObserver<Result>(this) {

            @Override
            public void onSubscribe(Disposable d) {
                //接下来可以检查网络连接等操作
                if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    showToast("当前网络不可用，请检查网络");
                }
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                showToast(responseThrowable.message);
            }

            @Override
            public void onNext(Result result) {
                super.onNext(result);
                if (result.getCode() == ErrorCode.SUCCESS) {
                    if (status == 1) {
                        showToast("开门成功");
                    } else {
                        showToast("关门成功");
                    }
                } else if (result.getCode() == ErrorCode.FAIL) {
                    if (status == 1) {
                        showToast("开门失败");
                    } else {
                        showToast("关门失败");
                    }
                }
            }
        });
    }

    /**
     * 网络监测
     */
    private class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtil.isNetworkAvailable(context)) {
                // 网络连接成功
                ivNetWork.setVisibility(View.VISIBLE);
            } else {
                // 网络断开
                ivNetWork.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                //确定键enter
                LogUtils.d(TAG, "点击了确定键");
                // 开门
                openAndCloseDoorRecord(1);
                break;
            case KeyEvent.KEYCODE_BACK:
                //返回键
                //这里由于break会退出，所以我们自己要处理掉 不返回上一层
                LogUtils.d(TAG, "点击了返回键");
                exitApp();
                return true;
            case KeyEvent.KEYCODE_SETTINGS:
                //设置键
                LogUtils.d(TAG, "点击了设置键");
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                //向下键
                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                LogUtils.d(TAG, "点击了下键");
                if (stationList.size() != 0) {
                    LogUtils.d(TAG, "跳转到下一个");
                    seconds = 0;
                    // 跳转到下一个监测点，如果是最后一个，则跳转到第一个
                    if (selectedStation == stationList.size() - 1) {
                        // 表示当前选中的是最后一个监测点
                        LogUtils.d(TAG, "当前在最后一个，跳转到第一个");
                        selectedStation = 0;
                    } else if (selectedStation < stationList.size() - 1) {
                        // 表示当前选中的不是最后一个
                        LogUtils.d(TAG, "当前不在最后一个，跳转到下一个");
                        selectedStation++;
                    }
                    // 平滑地将这个的item滚动到中间
                    rvPosition.smoothScrollToPosition(selectedStation);
                    stationAdapter.setSelectedPosition(selectedStation);
                    refreshPage(null);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                //向上键
                LogUtils.d(TAG, "点击了上键");
                if (stationList.size() != 0) {
                    LogUtils.d(TAG, "跳转到上一个");
                    seconds = 0;
                    // 跳转到上一个监测点，如果是第一个，则跳转到最后一个
                    if (selectedStation == 0) {
                        // 表示当前选中的是第一个监测点
                        LogUtils.d(TAG, "当前在第一一个，跳转到最后个");
                        selectedStation = stationList.size() - 1;
                    } else if (selectedStation > 0) {
                        // 表示当前选中的不是第一个
                        LogUtils.d(TAG, "当前不在第一个，跳转到上一个");
                        selectedStation--;
                    }
                    // 平滑地将这个的item滚动到中间
                    rvPosition.smoothScrollToPosition(selectedStation);
                    stationAdapter.setSelectedPosition(selectedStation);
                    refreshPage(null);
                }
                break;
            case KeyEvent.KEYCODE_0:
                //数字键0
                LogUtils.d(TAG, "点击了数字键0");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //向左键
                LogUtils.d(TAG, "点击了左键");
                searchNewVersion(true);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //向右键
                LogUtils.d(TAG, "点击了右键");
                searchNewVersion(true);
                break;
            case KeyEvent.KEYCODE_INFO:
                //info键
                LogUtils.d(TAG, "点击了Info键");
                break;
            case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                //向下翻页键
                LogUtils.d(TAG, "点击了向下翻页键");
                break;
            case KeyEvent.KEYCODE_PAGE_UP:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                //向上翻页键
                LogUtils.d(TAG, "点击了向上翻页键");
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                //调大声音键
                LogUtils.d(TAG, "点击了音量增大键");
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //降低声音键
                LogUtils.d(TAG, "点击了音量减小键");
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                //禁用声音
                LogUtils.d(TAG, "点击了禁音键");
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        flag = false;
        if (syncTimeTask != null) {
            syncTimeTask.cancel(true);
            syncTimeTask = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (netBroadcastReceiver != null) {
            unregisterReceiver(netBroadcastReceiver);
        }
        super.onDestroy();
    }

    /**
     * 显示确认退出的弹窗
     */
    private void exitApp() {
        SelectDialog selectDialog = new SelectDialog(mContext, getString(R.string.warning_to_exit));
        selectDialog.setButtonText(getString(R.string.Cancel), getString(R.string.Continue));
        selectDialog.setCancelable(false);
        selectDialog.setOnDialogClickListener(new SelectDialog.OnDialogClickListener() {
            @Override
            public void onOKClick() {
                ActivityController.exit(mContext);
            }

            @Override
            public void onCancelClick() {

            }
        });
        selectDialog.show();
    }

    /**
     * 查询最新版本
     */
    public void searchNewVersion(boolean showToast) {
        JsonObject params = new JsonObject();
        params.addProperty("apkTypeId", ApkInfo.APK_TYPE_ID_AirCondition);

        Observable<Result> resultObservable = NetClient.getInstance(NetClient.getBaseUrlProject(), false, true).getZbsApi().searchNewVersion(params);
        resultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new NetworkObserver<Result>(this) {

            @Override
            public void onSubscribe(Disposable d) {
                showLoadingDialog(mContext, "查询最新版本中", false);
            }

            @Override
            public void onError(ExceptionHandle.ResponseThrowable responseThrowable) {
                cancelDialog();
            }

            @Override
            public void onNext(Result result) {
                super.onNext(result);
                cancelDialog();
                VersionResult versionResult = GsonUtils.parseJSON(GsonUtils.convertJSON(result.getData()), VersionResult.class);
                if (result.getCode() == ErrorCode.SUCCESS) {
                    VersionInfo versionInfo = versionResult.getVersionInfo();
                    latestVersionName = versionInfo.getVersionName();
                    latestVersionMD5 = versionInfo.getMd5Value();
                    latestVersionLog = versionInfo.getVersionLog();
                    apkDownloadPath = versionInfo.getVersionUrl().replace("\\", "/");
                    latestFileName = versionInfo.getVersionFileName();
                    latestVersionCode = versionInfo.getVersionCode();
                    if (myVersionCode < latestVersionCode) {
                        showDialogUpdate();
                    } else {
                        if (showToast) {
                            showToast("您当前使用的是最新版本");
                        }
                    }
                } else if (result.getCode() == ErrorCode.FAIL) {
                    if (showToast) {
                        showToast("查询版本信息失败");
                    }
                }
            }
        });
    }

    /**
     * 提示版本更新的对话框
     */
    private void showDialogUpdate() {
        UpgradeVersionDialog upgradeVersionDialog = new UpgradeVersionDialog(mContext);
        upgradeVersionDialog.setCancelable(false);
        ((TextView) upgradeVersionDialog.findViewById(R.id.tv_versionLog)).setText(latestVersionLog);
        ((TextView) upgradeVersionDialog.findViewById(R.id.tv_currentVersion)).setText(ApkUtils.getVersionName(mContext));
        ((TextView) upgradeVersionDialog.findViewById(R.id.tv_latestVersion)).setText(latestVersionName);
        upgradeVersionDialog.setOnDialogClickListener(new UpgradeVersionDialog.OnDialogClickListener() {
            @Override
            public void onOKClick() {
                downloadApk(apkDownloadPath);
            }

            @Override
            public void onCancelClick() {
                upgradeVersionDialog.dismiss();
            }
        });
        upgradeVersionDialog.show();
    }

    /**
     * 下载新版本程序
     */
    public void downloadApk(String downloadUrl) {
        final int DOWNLOAD_APK_ID = 10;
        // 判断下载服务是不是已经在运行了
        if (isServiceRunning(DownloadService.class.getName())) {
            showToast("正在后台下载，请稍后");
        } else {
            Intent intent = new Intent(MainActivity.this, DownloadService.class);
            Bundle bundle = new Bundle();
            bundle.putString("download_url", downloadUrl);
            bundle.putInt("download_id", DOWNLOAD_APK_ID);
            bundle.putString("download_file", latestFileName);
            intent.putExtras(bundle);
            startService(intent);
            showToast("正在后台下载");
        }
    }

    /**
     * 用来判断服务是否运行.
     *
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    private boolean isServiceRunning(String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 安装apk
     *
     * @param file 需要安装的apk
     */
    private void installApk(File file) {
        //先验证文件的正确性和完整性（通过MD5值）
        LogUtils.d(TAG, "文件路径：" + file.getAbsolutePath());
        if (file.isFile() && latestVersionMD5.equals(FileUtil.getFileMD5(file))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileProvider", file);//在AndroidManifest中的android:authorities值
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            startActivity(intent);
        } else {
            showToast("File error");
        }
    }

    /**
     * Android8.0需要处理未知应用来源权限问题,否则直接安装
     */
    private void checkIsAndroidO() {
        LogUtils.d(TAG, "检查Android8.0安装软件的权限");
        File file = new File(ApkInfo.APP_ROOT_PATH + ApkInfo.DOWNLOAD_DIR, latestFileName);
        if (file.exists()) {
            LogUtils.d(TAG, "文件存在，准备安装");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LogUtils.d(TAG, "Android版本大于等于Android8.0，需要检查安装软件的权限");
                boolean b = mContext.getPackageManager().canRequestPackageInstalls();
                if (b) {
                    LogUtils.d(TAG, "有安装未知应用来源的权限，开始安装");
                    installApk(file);
                } else {
                    //请求安装未知应用来源的权限
                    LogUtils.d(TAG, "没有安装未知应用来源的权限，开始申请");
                    requestPermissions(new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGES_REQUEST_CODE);
                }
            } else {
                LogUtils.d(TAG, "Android版本小于Android8.0，直接安装");
                installApk(file);
            }
        } else {
            LogUtils.d(TAG, "文件不存在，安装失败");
            showToast("文件不存在");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case INSTALL_PACKAGES_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkIsAndroidO();
                } else {
                    //  Android8.0以上引导用户手动开启安装权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LogUtils.d(TAG, "需要引导用户手动开启安装权限");
                        Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                        startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GET_UNKNOWN_APP_SOURCES:
                    // 从安装未知来源文件的设置页面返回
                    checkIsAndroidO();
                    break;
                default:
                    break;
            }
        }
    }

}
