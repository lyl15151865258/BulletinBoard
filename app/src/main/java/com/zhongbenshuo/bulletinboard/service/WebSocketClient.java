package com.zhongbenshuo.bulletinboard.service;

import com.zhongbenshuo.bulletinboard.bean.Environment;
import com.zhongbenshuo.bulletinboard.bean.EventMsg;
import com.zhongbenshuo.bulletinboard.bean.Websocket;
import com.zhongbenshuo.bulletinboard.constant.Constants;
import com.zhongbenshuo.bulletinboard.interfaces.IMsgWebSocket;
import com.zhongbenshuo.bulletinboard.utils.GsonUtils;
import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

    private String TAG = "WebSocketClient";
    private IMsgWebSocket iMsgWebSocket;

    public WebSocketClient(String url, IMsgWebSocket iMsgWebSocket) throws URISyntaxException {
        super(new URI(url));
        this.iMsgWebSocket = iMsgWebSocket;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        //通道打开
        LogUtils.d(TAG, "建立连接");
        iMsgWebSocket.openSuccess();
    }

    @Override
    public void onMessage(String message) {
        LogUtils.d(TAG, message);
        if (message.equals("PingPong")) {
            // 收到心跳包，原文返回
            send(message);
        } else {
            Websocket websocket = null;
            try {
                websocket = GsonUtils.parseJSON(message, Websocket.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (websocket != null) {
                LogUtils.d(TAG, "服务器传来的信息：" + message);
                if (getObject(websocket) instanceof Environment) {
                    EventMsg msg = new EventMsg();
                    msg.setTag(Constants.SHOW_DATA_WEBSOCKET);
                    msg.setMsg(GsonUtils.convertJSON(websocket.getMessage()));
                    EventBus.getDefault().post(msg);
                }else if (getObject(websocket) instanceof String){
                    EventMsg msg = new EventMsg();
                    msg.setTag(Constants.SHOW_USER_PHOTO);
                    msg.setMsg((String)websocket.getMessage());
                    EventBus.getDefault().post(msg);
                }
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // 通道关闭
        LogUtils.d(TAG, "连接断开");
        iMsgWebSocket.closed();
    }

    @Override
    public void onError(Exception ex) {
        //发生错误
        LogUtils.d(TAG, "发生错误：" + ex.getMessage());
        ex.printStackTrace();
    }

    private Object getObject(Websocket websocket) {
        String object = GsonUtils.convertJSON(websocket.getMessage());
        switch (websocket.getKey()) {
            case 20000://环境监测数据
                try {
                    return GsonUtils.parseJSON(object, Environment.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 20001://有人按门铃，在大屏展示
                return websocket.getMessage();
            default:
                return false;
        }
        return false;
    }

}