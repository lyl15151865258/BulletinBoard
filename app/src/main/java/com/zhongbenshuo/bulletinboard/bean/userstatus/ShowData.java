package com.zhongbenshuo.bulletinboard.bean.userstatus;

/**
 * 人员状态板真正展示的内容
 * Created at 2019/9/30 9:30
 *
 * @author LiYuliang
 * @version 1.0
 */

public class ShowData {

    private int type;       // 数据类型，POSITION：部门，EMPLOYEE：员工
    private Object object;  // 数据内容

    public static final int POSITION = 1;
    public static final int EMPLOYEE = 2;

    public ShowData(int type, Object object) {
        this.type = type;
        this.object = object;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
