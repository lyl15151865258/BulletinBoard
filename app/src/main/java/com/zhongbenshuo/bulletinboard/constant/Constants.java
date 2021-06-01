package com.zhongbenshuo.bulletinboard.constant;

/**
 * 部分常量值，用于解决提示魔法值的提示和接口返回数据解析用
 * Created by LiYuliang on 2017/10/25.
 *
 * @author LiYuliang
 * @version 2017/11/21
 */

public class Constants {

    public static final String EMPTY = "";
    public static final String FAIL = "fail";
    public static final String NEW_LINE = "\n";
    public static final String POINT = ".";
    public static final String HYPHEN = "-";
    public static final String SUCCESS = "success";

    public static final String CITY_DATA = "china_city_data.json";

    /**
     * 退出程序点击两次返回键的间隔时间
     */
    public static final int EXIT_DOUBLE_CLICK_TIME = 2000;


    public static final String APP_ID = "FxsTMG6wwVwtL4dChvPz11snR6gZgUHHivjDaB4EJEn9";
    public static final String SDK_KEY = "GPNiuSinenHLZMoYqFhphTJF6KXh4Q7UUokBn4GdTvNf";

    /**
     * IR预览数据相对于RGB预览数据的横向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int HORIZONTAL_OFFSET = 0;
    /**
     * IR预览数据相对于RGB预览数据的纵向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int VERTICAL_OFFSET = 0;


    /**
     * EventBus标记
     */
    public static final String CONNECT_SUCCESS_SOCKET = "connectSuccess_socket";
    public static final String CONNECT_FAIL_WEBSOCKET = "connectFail_webSocket";
    public static final String CONNECT_OPEN_WEBSOCKET = "connectOpen_webSocket";
    public static final String CONNECT_CLOSE_WEBSOCKET = "connectClose_webSocket";
    public static final String SHOW_TOAST_WEBSOCKET = "showToast_webSocket";
    public static final String SHOW_WEATHER = "showWeather";
    public static final String SHOW_USER_STATUS = "showUserStatus";
    public static final String CHANGE_PAGE = "changePage";
}
