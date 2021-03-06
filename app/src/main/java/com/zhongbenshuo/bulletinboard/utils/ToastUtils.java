package com.zhongbenshuo.bulletinboard.utils;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zhongbenshuo.bulletinboard.R;

import java.util.Stack;

/**
 * 自定义的Toast，避免用户未授予系统Toast权限而不显示吐司
 * Created at 2018/02/30 0030 8:33
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ToastUtils {

    public static final int LENGTH_SHORT = 0x00;
    public static final int LENGTH_LONG = 0x01;

    private static final int ANIMATION_DURATION = 600;

    public Context mContext;
    public String msg;
    private int HIDE_DELAY = 2000;

//    public static boolean isRunning = false;

    private Handler mHandler = new Handler();

    public static Stack<ToastUtils> stack = new Stack();

    // 表示吐司里显示的文字
    public static ToastUtils makeText(Context context, String message, int HIDE_DELAY) {
        ToastUtils utils = new ToastUtils();
        utils.mContext = context;
        utils.msg = message;

        if (HIDE_DELAY == LENGTH_LONG) {
            utils.HIDE_DELAY = 2500;
        } else {
            utils.HIDE_DELAY = 1500;
        }

        return utils;
    }

    private static void wakeUp() {
//        isRunning = true;
        if (!stack.empty()) {
            ToastUtils util = stack.pop();
            util.doshow();

        } else {
//            isRunning = false;
        }


    }

    private void doshow() {
        ViewGroup container = ((AppCompatActivity) mContext).findViewById(android.R.id.content);
        View mView = ((AppCompatActivity) mContext).getLayoutInflater().inflate(R.layout.toast_layout, null);
        container.addView(mView);

        LinearLayout mContainer = mView.findViewById(R.id.mbContainer);
        mContainer.setVisibility(View.GONE);
        TextView mTextView = mView.findViewById(R.id.mbMessage);
        mTextView.setText(msg);

        // 显示动画
        AlphaAnimation mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        // 消失动画
        final AlphaAnimation mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFadeOutAnimation.setDuration(ANIMATION_DURATION);
        mFadeOutAnimation
                .setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // 消失动画后更改状态为 未显示
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // 隐藏布局，不使用remove方法为防止多次创建多个布局
                        mContainer.setVisibility(View.GONE);
                        container.removeView(mView);
                        wakeUp();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
        mContainer.setVisibility(View.VISIBLE);

        mFadeInAnimation.setDuration(ANIMATION_DURATION);

        mContainer.startAnimation(mFadeInAnimation);
        mHandler.postDelayed(() -> mContainer.startAnimation(mFadeOutAnimation), HIDE_DELAY);
    }

    public void show() {
        stack.push(this);
//        if (!isRunning) {
        wakeUp();
//        }
    }

}
