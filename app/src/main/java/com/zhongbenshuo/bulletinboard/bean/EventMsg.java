package com.zhongbenshuo.bulletinboard.bean;

/**
 * Created by LiYuliang on 2017/12/08.
 * 传递消息时使用，可以自己增加更多的参数
 *
 * @author LiYuliang
 * @version 1.1.0
 */

public class EventMsg {

    private String tag;
    private String msg;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
