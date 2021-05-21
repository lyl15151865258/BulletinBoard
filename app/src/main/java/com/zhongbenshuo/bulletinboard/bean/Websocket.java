package com.zhongbenshuo.bulletinboard.bean;

/**
 * WebSocket接受数据实体类
 * Created at 2018/5/16 0016 16:46
 *
 * @author LiYuliang
 * @version 1.0
 */

public class Websocket {
    /**
     * 数据类型
     */
    private int key;
    /**
     * 数据具体内容
     */
    private Object message;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
