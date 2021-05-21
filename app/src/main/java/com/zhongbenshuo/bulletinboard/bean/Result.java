package com.zhongbenshuo.bulletinboard.bean;

/**
 * 通用的接口请求加密结果
 * Created at 2019/6/20 17:12
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Result {

    private int code;

    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
