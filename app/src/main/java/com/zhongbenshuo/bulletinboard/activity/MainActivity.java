package com.zhongbenshuo.bulletinboard.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.zhongbenshuo.bulletinboard.BuildConfig;
import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.adapter.StatusAdapter;
import com.zhongbenshuo.bulletinboard.bean.BoardData;
import com.zhongbenshuo.bulletinboard.bean.EventMsg;
import com.zhongbenshuo.bulletinboard.bean.ProjectAnnouncement;
import com.zhongbenshuo.bulletinboard.bean.Result;
import com.zhongbenshuo.bulletinboard.bean.VersionInfo;
import com.zhongbenshuo.bulletinboard.bean.VersionResult;
import com.zhongbenshuo.bulletinboard.bean.Weather;
import com.zhongbenshuo.bulletinboard.bean.ShowData;
import com.zhongbenshuo.bulletinboard.constant.ApkInfo;
import com.zhongbenshuo.bulletinboard.constant.Constants;
import com.zhongbenshuo.bulletinboard.constant.ErrorCode;
import com.zhongbenshuo.bulletinboard.contentprovider.SPHelper;
import com.zhongbenshuo.bulletinboard.interfaces.DownloadProgress;
import com.zhongbenshuo.bulletinboard.interfaces.OnDoubleClickListener;
import com.zhongbenshuo.bulletinboard.network.ExceptionHandle;
import com.zhongbenshuo.bulletinboard.network.NetClient;
import com.zhongbenshuo.bulletinboard.network.NetworkObserver;
import com.zhongbenshuo.bulletinboard.service.DownloadService;
import com.zhongbenshuo.bulletinboard.service.TimeTaskService;
import com.zhongbenshuo.bulletinboard.utils.ActivityController;
import com.zhongbenshuo.bulletinboard.utils.ApkUtils;
import com.zhongbenshuo.bulletinboard.utils.FileUtil;
import com.zhongbenshuo.bulletinboard.utils.GsonUtils;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;
import com.zhongbenshuo.bulletinboard.utils.NetworkUtil;
import com.zhongbenshuo.bulletinboard.widget.SelectDialog;
import com.zhongbenshuo.bulletinboard.widget.UpgradeVersionDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.zhongbenshuo.bulletinboard.constant.Screen.ANNOUNCEMENT_SHOW_ROW;
import static com.zhongbenshuo.bulletinboard.constant.Screen.USER_SHOW_ROW;

/**
 * 主页面，环境监测页面
 * Created at 2019/9/24 18:21
 *
 * @author LiYuliang
 * @version 1.0
 */

public class MainActivity extends BaseActivity {

    private Context mContext;
    private ImageView ivWeather;
    private TextView tvCity, tvWeather, tvTemperature, tvWind, tvHumidity, tvAnnouncement;
    private List<ShowData> allDataList, showDataList;
    private int announcementShowRow, userShowRow, totalPage = 0, currentPage = 1;
    private List<ProjectAnnouncement> projectAnnouncementList;
    // 原始内容
    private final StringBuffer originContent = new StringBuffer();
    // 上一次原始内容
    private final StringBuffer allContent = new StringBuffer();
    // 当前显示内容
    private final StringBuffer currentContent = new StringBuffer();
    // 剩余内容
    private final StringBuffer leftContent = new StringBuffer();
    private StatusAdapter statusAdapter;

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

        ivWeather = findViewById(R.id.ivWeather);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.app_title_mingzhi);
        tvCity = findViewById(R.id.tvCity);
        tvWeather = findViewById(R.id.tvWeather);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWind = findViewById(R.id.tvWind);
        tvHumidity = findViewById(R.id.tvHumidity);
        TextView tvYearWeek = findViewById(R.id.tvYearWeek);
        TextView tvTime = findViewById(R.id.tvTime);
        Typeface typeFaceBlack = Typeface.createFromAsset(getAssets(), "fonts/fzzzhf.TTF");
        tvYearWeek.setTypeface(typeFaceBlack);
        tvTime.setTypeface(typeFaceBlack);

        allDataList = new ArrayList<>();
        showDataList = new ArrayList<>();
        projectAnnouncementList = new ArrayList<>();

        findViewById(R.id.leftTop).setOnTouchListener(onTouchListener1);
        findViewById(R.id.leftBottom).setOnTouchListener(onTouchListener2);
        findViewById(R.id.rightTop).setOnTouchListener(onTouchListener3);
        findViewById(R.id.rightBottom).setOnTouchListener(onTouchListener4);

        announcementShowRow = SPHelper.getInt("ANNOUNCEMENT_SHOW_ROW", ANNOUNCEMENT_SHOW_ROW);
        userShowRow = SPHelper.getInt("USER_SHOW_ROW", USER_SHOW_ROW);

        // 公告
        tvAnnouncement = findViewById(R.id.tvAnnouncement);

        // 员工状态
        RecyclerView rvStatus = findViewById(R.id.rvStatus);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(mContext);
        linearLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvStatus.setLayoutManager(linearLayoutManager2);
        statusAdapter = new StatusAdapter(this, showDataList);
        rvStatus.setAdapter(statusAdapter);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initTimeTaskService();

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

    private final View.OnTouchListener onTouchListener1 = new OnDoubleClickListener(this::addLines);

    private final View.OnTouchListener onTouchListener2 = new OnDoubleClickListener(this::minusLines);

    private final View.OnTouchListener onTouchListener3 = new OnDoubleClickListener(this::addRows);

    private final View.OnTouchListener onTouchListener4 = new OnDoubleClickListener(this::minusRows);

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
        if (msg.getTag().equals(Constants.SHOW_USER_STATUS)) {
            //接收到这个消息说明需要展示数据
            BoardData boardData = GsonUtils.parseJSON(msg.getMsg(), BoardData.class);

            // ************************************* 公告栏部分 *************************************
            projectAnnouncementList.clear();
            projectAnnouncementList.addAll(boardData.getProjectAnnouncementList());

            // 清空公告内容
            originContent.delete(0, originContent.length());
            for (int i = 0; i < projectAnnouncementList.size(); i++) {
                originContent.append(i + 1);
                originContent.append("、");
                originContent.append(projectAnnouncementList.get(i).getQuotation());
                if (i != projectAnnouncementList.size() - 1) {
                    originContent.append("\n");
                }
            }
            LogUtils.d(TAG, "原始内容是：" + originContent.toString());
            LogUtils.d(TAG, "页面内容是：" + allContent.toString());
            if (!originContent.toString().equals(allContent.toString())) {
                // 如果内容发生变化，重新展示最新的内容
                LogUtils.d(TAG, "内容发生变化：" + originContent);
                allContent.delete(0, allContent.length());
                allContent.append(originContent);

                tvAnnouncement.setMaxLines(announcementShowRow);
                tvAnnouncement.setText(allContent);
                currentContent.delete(0, currentContent.length());
                currentContent.append(tvAnnouncement.getText().subSequence(0, tvAnnouncement.getLayout().getLineEnd(Math.min(announcementShowRow - 1, tvAnnouncement.getLineCount() - 1))));
                LogUtils.d(TAG, "当前显示的内容：" + currentContent.toString());
                // 计算剩余内容
                leftContent.delete(0, leftContent.length());
                leftContent.append(allContent.delete(0, currentContent.toString().length()));
                // 重新赋值原始内容
                allContent.delete(0, allContent.length());
                allContent.append(originContent);
                LogUtils.d(TAG, "剩余内容：" + leftContent.toString());
            }
            // ************************************* 公告栏部分 *************************************

            // ************************************* 人员动态部分 *************************************
            if (boardData.getShowDataList().size() != allDataList.size() || !boardData.getShowDataList().containsAll(allDataList)) {
                // 内容不相同时才更新
                allDataList.clear();
                allDataList.addAll(boardData.getShowDataList());
                // 更改总页面数量
                if (allDataList.size() % userShowRow == 0) {
                    totalPage = allDataList.size() / userShowRow;
                } else {
                    totalPage = allDataList.size() / userShowRow + 1;
                }
                LogUtils.d(TAG, "人员页面总数：" + totalPage + "，当前页面：" + currentPage);
                refreshUsers();
            }
            // ************************************* 人员动态部分 *************************************
        }
        if (msg.getTag().equals(Constants.CHANGE_PAGE)) {
            //接收到这个消息说明需要翻页
            // 人员动态翻页
            if (currentPage < totalPage) {
                currentPage++;
            } else {
                currentPage = 1;
            }
            LogUtils.d(TAG, "人员页面总数：" + totalPage + "，当前页面：" + currentPage);
            refreshUsers();
            // 公告栏翻页
            if (leftContent.toString().length() > 0) {
                LogUtils.d(TAG, "剩余内容长度大于0");
                tvAnnouncement.setMaxLines(announcementShowRow);
                tvAnnouncement.setText(leftContent);
                currentContent.delete(0, currentContent.length());
                LogUtils.d(TAG, "当前显示的行数：" + tvAnnouncement.getLineCount());

                currentContent.append(tvAnnouncement.getText().subSequence(0, tvAnnouncement.getLayout().getLineEnd(Math.min(announcementShowRow - 1, tvAnnouncement.getLineCount() - 1))));

                LogUtils.d(TAG, "翻页显示的内容：" + currentContent.toString());
                leftContent.delete(0, currentContent.toString().length());
                LogUtils.d(TAG, "翻页，剩余内容：" + leftContent);
            } else {
                LogUtils.d(TAG, "没有剩余内容了，重新展示第一页");
                // 重新展示第一页内容
                refreshAnnouncement();
            }
        }
        if (msg.getTag().equals(Constants.SHOW_WEATHER)) {
            //接收到实时天气信息
            LogUtils.d(TAG, "获取到实时天气信息");
            Weather weather = GsonUtils.parseJSON(msg.getMsg(), Weather.class);
            if (weather.getStatus().equals("1") && weather.getInfocode().equals("10000") && Integer.parseInt(weather.getCount()) > 0) {
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
                //向下键，公告行数减一
                /*    实际开发中有时候会触发两次，所以要判断一下按下时触发 ，松开按键时不触发
                 *    exp:KeyEvent.ACTION_UP
                 */
                LogUtils.d(TAG, "点击了下键");
                minusLines();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                //向上键，公告行数加一
                LogUtils.d(TAG, "点击了上键");
                addLines();
                break;
            case KeyEvent.KEYCODE_0:
                //数字键0
                LogUtils.d(TAG, "点击了数字键0");
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //向左键，人员条数减一
                LogUtils.d(TAG, "点击了左键");
                minusRows();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                //向右键，人员条数加一
                LogUtils.d(TAG, "点击了右键");
                addRows();
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

    /**
     * 每列行数增加
     */
    private void addRows() {
        userShowRow++;
        SPHelper.save("USER_SHOW_ROW", userShowRow);
        showToast("当前人员显示" + userShowRow + "行");
        // 更改总页面数量
        if (allDataList.size() % userShowRow == 0) {
            totalPage = allDataList.size() / userShowRow;
        } else {
            totalPage = allDataList.size() / userShowRow + 1;
        }
        LogUtils.d(TAG, "页面总数：" + totalPage + "，当前页面：" + currentPage);
        refreshUsers();
    }

    /**
     * 每列行数减少
     */
    private void minusRows() {
        if (userShowRow > 1) {
            userShowRow--;
            SPHelper.save("USER_SHOW_ROW", userShowRow);
            showToast("当前人员显示" + userShowRow + "行");
            // 更改总页面数量
            if (allDataList.size() % userShowRow == 0) {
                totalPage = allDataList.size() / userShowRow;
            } else {
                totalPage = allDataList.size() / userShowRow + 1;
            }
            LogUtils.d(TAG, "页面总数：" + totalPage + "，当前页面：" + currentPage);
            refreshUsers();
        } else {
            showToast("每列不能小于1行");
        }
    }

    /**
     * 增加文本行数
     */
    private void addLines() {
        announcementShowRow++;
        SPHelper.save("ANNOUNCEMENT_SHOW_ROW", announcementShowRow);
        showToast("当前公告显示" + announcementShowRow + "行");
        refreshAnnouncement();
    }

    /**
     * 减少文本行数
     */
    private void minusLines() {
        if (announcementShowRow > 1) {
            announcementShowRow--;
            SPHelper.save("ANNOUNCEMENT_SHOW_ROW", announcementShowRow);
            showToast("当前公告显示" + announcementShowRow + "行");
            refreshAnnouncement();
        } else {
            showToast("公告显示不能小于1行");
        }
    }

    /**
     * 刷新人员页面
     */
    private void refreshUsers() {
        showDataList.clear();
        showDataList.addAll(allDataList.subList((currentPage - 1) * userShowRow, Math.min(allDataList.size(), (currentPage * userShowRow))));
        statusAdapter.notifyDataSetChanged();
    }

    /**
     * 刷新公告页面
     */
    private void refreshAnnouncement() {
        tvAnnouncement.setMaxLines(announcementShowRow);
        tvAnnouncement.setText(allContent);
        currentContent.delete(0, currentContent.length());
        currentContent.append(tvAnnouncement.getText().subSequence(0, tvAnnouncement.getLayout().getLineEnd(Math.min(announcementShowRow - 1, tvAnnouncement.getLineCount() - 1))));
        LogUtils.d(TAG, "刷新公告页面，当前显示的内容：" + currentContent.toString());

        leftContent.delete(0, leftContent.length());
        leftContent.append(allContent.toString().substring(currentContent.toString().length()));
        LogUtils.d(TAG, "刷新公告页面，剩余内容：" + leftContent.toString());
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
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
        params.addProperty("apkTypeId", ApkInfo.APK_TYPE_ID_BulletinBoard_MingZhi);

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
            public void onNext(@NotNull Result result) {
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
        if (requestCode == INSTALL_PACKAGES_REQUEST_CODE) {
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GET_UNKNOWN_APP_SOURCES) {// 从安装未知来源文件的设置页面返回
                checkIsAndroidO();
            }
        }
    }

}
