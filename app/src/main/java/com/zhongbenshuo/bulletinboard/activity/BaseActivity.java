package com.zhongbenshuo.bulletinboard.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhongbenshuo.bulletinboard.R;
import com.zhongbenshuo.bulletinboard.utils.ActivityController;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;
import com.zhongbenshuo.bulletinboard.utils.MyLifecycleHandler;
import com.zhongbenshuo.bulletinboard.utils.NotificationsUtils;
import com.zhongbenshuo.bulletinboard.utils.ToastUtils;
import com.zhongbenshuo.bulletinboard.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 父类activity
 * Created by Li Yuliang on 2017/2/13 0013.
 *
 * @author LiYuliang
 * @version 2017/10/27
 */

public abstract class BaseActivity extends AppCompatActivity {

    public String TAG = getClass().getName();
    private Toast toast;
    private LoadingDialog loadingDialog;
    protected int mWidth;
    protected int mHeight;
    protected float mDensity;
    protected int mDensityDpi;
    protected int mAvatarSize;
    protected float mRatio;
    protected Vibrator vibrator;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onCreate() ");
        ActivityController.addActivity(this);
        // 保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 隐藏状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏底部虚拟按键
        hideBottomUIMenu();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDensity = dm.density;
        mDensityDpi = dm.densityDpi;
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mRatio = Math.min((float) mWidth / 720, (float) mHeight / 1280);
        mAvatarSize = (int) (50 * mDensity);
        loadingDialog = new LoadingDialog(this, R.style.loading_dialog);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        EventBus.getDefault().register(this);
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStringEvent(String msg) {

    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (mInputMethodManager != null) {
                return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onStart() ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onRestart() ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 隐藏底部虚拟按键
        hideBottomUIMenu();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onResume() ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //如果toast在显示则取消显示
        if (toast != null) {
            toast.cancel();
        }
        //取消显示dialog
        cancelDialog();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onPause() ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onStop() ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityController.removeActivity(this);
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onDestroy() ");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onConfigurationChanged() ");
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    /**
     * 自定义的Toast，避免重复出现
     *
     * @param msg 需要显示的文本
     */
    public void showToast(String msg) {
        if (NotificationsUtils.isNotificationEnabled(this) && MyLifecycleHandler.isApplicationInForeground()) {
            //如果授予了App系统通知权限，则使用系统Toast
            View view = LayoutInflater.from(this).inflate(R.layout.toast_layout, findViewById(android.R.id.content), false);
            TextView tvMessage = view.findViewById(R.id.mbMessage);
            tvMessage.setText(msg);
            if (toast == null) {
                toast = new Toast(this);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
            } else {
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
            }
            toast.show();
        } else {
            //否则使用自定义的View（模仿Toast，存在瑕疵）
            ToastUtils.makeText(this, msg, ToastUtils.LENGTH_SHORT).show();
        }
    }

    public void openActivity(Class<?> pClass) {
        openActivity(pClass, null, null);
    }

    public void openActivity(Class<?> pClass, Bundle bundle) {
        openActivity(pClass, bundle, null);
    }

    public void openActivity(Class<?> pClass, Bundle bundle, Uri uri) {
        Intent intent = new Intent(this, pClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
    }

    public void openActivity(String action) {
        openActivity(action, null, null);
    }

    public void openActivity(String action, Bundle bundle) {
        openActivity(action, bundle, null);
    }

    public void openActivity(String action, Bundle bundle, Uri uri) {
        Intent intent = new Intent(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
    }

    /**
     * 点击除了EditText以外的所有地方都可以隐藏软键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                AppCompatActivity currentActivity = (AppCompatActivity) ActivityController.getInstance().getCurrentActivity();
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (null != im) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 显示加载的dialogs
     *
     * @param context    Context对象
     * @param msg        显示的信息
     * @param cancelable 是否可取消
     */
    public void showLoadingDialog(Context context, String msg, boolean cancelable) {
        if (!loadingDialog.isShowing()) {
            loadingDialog = new LoadingDialog(this, R.style.loading_dialog);
            loadingDialog.setCancelable(cancelable);

            if (!((AppCompatActivity) context).isFinishing()) {
                //显示dialog
                loadingDialog.show();
                loadingDialog.setMessage(msg);
            }
        }
    }

    /**
     * 显示加载的dialogs
     *
     * @param context    Context对象
     * @param cancelable 是否可取消
     */
    public void showLoadingDialog(Context context, boolean cancelable) {
        if (!loadingDialog.isShowing()) {
            loadingDialog = new LoadingDialog(this, R.style.loading_dialog);
            loadingDialog.setCancelable(cancelable);

            if (!((AppCompatActivity) context).isFinishing()) {
                //显示dialog
                loadingDialog.show();
            }
        }
    }

    /**
     * 取消dialog显示
     */
    public void cancelDialog() {
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this instanceof LogoActivity || this instanceof MainActivity) {
                // RgbRecognizeActivity和IrRecognizeActivity页面不执行退出， 改返回键为Home键，返回手机主界面，不退出APP
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
            } else {
                //如果activity队列还包含本activity以外的activity，则删除本activity，回退到前一个activity
                ActivityController.finishActivity(this);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
