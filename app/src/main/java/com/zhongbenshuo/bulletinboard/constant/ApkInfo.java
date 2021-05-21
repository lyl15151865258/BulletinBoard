package com.zhongbenshuo.bulletinboard.constant;

import android.os.Environment;

import com.zhongbenshuo.bulletinboard.BulletinBoard;

/**
 * 软件信息类
 * Created at 2019/9/27 19:04
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ApkInfo {
    /**
     * 软件类型
     */
    public static final String APK_TYPE_ID_ZBSAttendance = "1";
    public static final String APK_TYPE_ID_FaceRecognize = "2";
    public static final String APK_TYPE_ID_AirCondition = "3";
    public static final String APK_TYPE_ID_EmployeeStatus = "4";

    // 文件路径
    public final static String APP_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + BulletinBoard.getInstance().getPackageName();
    public final static String DOWNLOAD_DIR = "/downlaod/";
}
