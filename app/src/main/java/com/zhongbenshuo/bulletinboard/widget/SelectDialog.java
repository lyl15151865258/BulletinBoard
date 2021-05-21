package com.zhongbenshuo.bulletinboard.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zhongbenshuo.bulletinboard.R;

/**
 * 选择Dialog
 * Created at 2018/11/28 13:55
 *
 * @author LiYuliang
 * @version 1.0
 */

public class SelectDialog extends Dialog {
    private Context context;
    private String text;
    private TextView okBtn, cancelBtn;
    private OnDialogClickListener dialogClickListener;

    public SelectDialog(Context context, String text) {
        super(context);
        this.context = context;
        this.text = text;
        initView();
    }

    //初始化View
    private void initView() {
        setContentView(R.layout.dialog_select);
        initWindow();
        okBtn = findViewById(R.id.btn_ok);
        cancelBtn = findViewById(R.id.btn_cancel);
        okBtn.requestFocus();
        TextView tvWarning = findViewById(R.id.tv_warning);
        tvWarning.setText(text);
        okBtn.setOnClickListener((v) -> {
            dismiss();
            if (dialogClickListener != null) {
                dialogClickListener.onOKClick();
            }
        });
        cancelBtn.setOnClickListener((v) -> {
            dismiss();
            if (dialogClickListener != null) {
                dialogClickListener.onCancelClick();
            }
        });

        // 去掉Android4.4及以下版本出现的顶部横线
        try {
            int dividerID = context.getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = findViewById(dividerID);
            divider.setBackgroundColor(Color.TRANSPARENT);
        } catch (Exception e) {
            //上面的代码，是用来去除Holo主题的蓝色线条
            e.printStackTrace();
        }
    }

    /**
     * 设置按钮上的文字
     *
     * @param leftText  左边按钮文字
     * @param rightText 右边按钮文字
     */
    public void setButtonText(String leftText, String rightText) {
        cancelBtn.setText(leftText);
        okBtn.setText(rightText);
    }

    /**
     * 添加黑色半透明背景
     */
    private void initWindow() {
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));//设置window背景
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//设置输入法显示模式
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = context.getResources().getDisplayMetrics();//获取屏幕尺寸
            lp.width = (int) (d.widthPixels * 0.9); //宽度为屏幕80%
            lp.gravity = Gravity.CENTER;  //中央居中
            dialogWindow.setAttributes(lp);
        }
    }

    public void setOnDialogClickListener(OnDialogClickListener clickListener) {
        dialogClickListener = clickListener;
    }

    /**
     * 添加按钮点击事件
     */
    public interface OnDialogClickListener {
        void onOKClick();

        void onCancelClick();
    }
}