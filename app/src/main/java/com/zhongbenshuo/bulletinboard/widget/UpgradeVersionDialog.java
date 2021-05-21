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
 * 版本升级的dialog
 * Created at 2019/3/1 14:23
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class UpgradeVersionDialog extends Dialog {

    private Context context;
    private OnDialogClickListener dialogClickListener;

    public UpgradeVersionDialog(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    private void initView() {
        setContentView(R.layout.dialog_upgrade);
        initWindow();
        TextView okBtn = findViewById(R.id.btn_ok);
        TextView cancelBtn = findViewById(R.id.btn_cancel);
        okBtn.requestFocus();
        okBtn.setOnClickListener(v -> {
            if (dialogClickListener != null) {
                dialogClickListener.onOKClick();
            }
            dismiss();
        });
        cancelBtn.setOnClickListener(v -> {
            if (dialogClickListener != null) {
                dialogClickListener.onCancelClick();
            }
            dismiss();
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
     * 添加黑色半透明背景
     */
    private void initWindow() {
        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
            dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = context.getResources().getDisplayMetrics();
            lp.width = (int) (d.widthPixels * 0.9);
            lp.gravity = Gravity.CENTER;
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

        /**
         * 更新按钮点击事件
         */
        void onOKClick();

        /**
         * 下次再说按钮点击事件
         */
        void onCancelClick();
    }
}

