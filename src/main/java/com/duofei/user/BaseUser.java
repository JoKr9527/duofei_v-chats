package com.duofei.user;

import org.springframework.web.socket.WebSocketSession;

/**
 * 普通用户
 * @author duofei
 * @date 2019/8/16
 */
public class BaseUser {

    /**
     * 用户名
     */
    private String userName;
    /**
     * websocket session
     */
    private final WebSocketSession session;

    public BaseUser(String userName,WebSocketSession webSocketSession){
        this.userName = userName;
        this.session = webSocketSession;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public WebSocketSession getSession() {
        return session;
    }
}
