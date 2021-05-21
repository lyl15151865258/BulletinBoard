package com.zhongbenshuo.bulletinboard.network;

import android.content.Context;
import android.text.TextUtils;

import com.zhongbenshuo.bulletinboard.activity.BaseActivity;
import com.zhongbenshuo.bulletinboard.bean.Result;
import com.zhongbenshuo.bulletinboard.constant.ErrorCode;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 自定义Observer类,可以在这里处理client网络连接状况（比如没有wifi，没有4g，没有联网等）
 * Created at 2018/11/28 13:48
 *
 * @author LiYuliang
 * @version 1.0
 */

public abstract class NetworkObserver<T> implements Observer<T> {

    private static final String TAG = "NetworkObserver";
    private Context mContext;
    private Disposable disposable;

    public NetworkObserver(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        LogUtils.d(TAG, "NetworkObserver.onSubscribe()");
    }

    @Override
    public void onNext(T t) {
        if (t instanceof Result) {

            LogUtils.d(TAG, "NetworkObserver.onNext(),result:" + t);
            int code = ((Result) t).getCode();
            LogUtils.d(TAG, "本次请求的返回码为：" + code);

            String log = "";
            switch (code) {
                case ErrorCode.SUCCESS:
                    log = "请求成功";
                    break;
                case ErrorCode.KEY_RSA_SERVER_EXPIRED:
                    log = "服务端RSA公钥过期";
                    break;
                case ErrorCode.KEY_RSA_CLIENT_EXPIRED:
                    log = "客户端RSA公钥过期";
                    break;
                case ErrorCode.DECRYPT_AES_KEY_FAILED:
                    log = "RSA解密AES密钥失败";
                    break;
                case ErrorCode.DECRYPT_REQUEST_BODY_FAILED:
                    log = "AES解密请求体失败";
                    break;
                case ErrorCode.DECRYPT_RESPONSE_BODY_FAILED:
                    log = "AES解密返回体失败";
                    break;
                case ErrorCode.VERIFY_HASH_VALUE_FAILED:
                    log = "请求体Hash值验证失败";
                    break;
                case ErrorCode.SERVER_ERROR:
                    log = "服务器出错";
                    break;
                case ErrorCode.ENCRYPT_AES_KEY_FAILED:
                    log = "服务端Aes密钥使用客户端RSA公钥加密失败";
                    break;
                case ErrorCode.GET_REQUEST_BODY_FAILED:
                    log = "获取请求体错误";
                    break;
                case ErrorCode.UNKNOWN_ERROR:
                    log = "未知错误";
                    break;
                case ErrorCode.USER_ALREADY_EXITS:
                    log = "用户已存在，注册失败";
                    break;
                case ErrorCode.ENCRYPT_PASSWORD_FAILED:
                    log = "服务端加密用户密码失败";
                    break;
                case ErrorCode.USER_DOES_NOT_EXITS:
                    log = "用户名或密码错误";
                    break;
                case ErrorCode.PHONE_NUMBER_OCCUPIED:
                    log = "该手机号已被使用";
                    break;
                case ErrorCode.PHONE_NUMBER_IS_NOT_REGISTERED:
                    log = "该手机号没有注册";
                    break;
                case ErrorCode.ORIGINAL_PASSWORD_WRONG:
                    log = "原密码错误";
                    break;
                case ErrorCode.MAIL_OCCUPIED:
                    log = "该邮箱已被使用";
                    break;
                case ErrorCode.OVER_FACE_NUM:
                    log = "最多保留一条人脸信息";
                    break;
                default:
                    break;
            }

            LogUtils.d(TAG, log);
            if (mContext instanceof BaseActivity) {
                // 请求成功的话不显示提示，交给对应的页面处理，其他异常在这里显示
                if (ErrorCode.SUCCESS != code && !TextUtils.isEmpty(log)) {
                    ((BaseActivity) mContext).showToast(log);
                }
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        LogUtils.e("NetworkObserver.throwable =" + e.toString());
        LogUtils.e("NetworkObserver.throwable =" + e.getMessage());

        if (e instanceof Exception) {
            //访问获得对应的Exception
            onError(ExceptionHandle.handleException(e));
        } else {
            //将Throwable 和 未知错误的status code返回
            onError(new ExceptionHandle.ResponseThrowable(e, ExceptionHandle.ERROR.UNKNOWN));
        }
    }

    public abstract void onError(ExceptionHandle.ResponseThrowable responseThrowable);

    @Override
    public void onComplete() {
        LogUtils.d(TAG, "NetworkObserver.onComplete()");
    }

}
