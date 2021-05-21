package com.zhongbenshuo.bulletinboard.bean;

/**
 * 远程开关门实体类
 * Created at 2019/9/20 15:43
 *
 * @author LiYuliang
 * @version 1.0
 */

public class OpenAndCloseDoorRecord {

    private int id;//开关门记录
    private int user_id;//谁
    private int status;//状态1是开，2是关
    private String createTime;//时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
