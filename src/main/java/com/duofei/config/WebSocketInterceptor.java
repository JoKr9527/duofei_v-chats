package com.duofei.config;

import com.duofei.constant.MessageIdConstant;
import com.duofei.handler.ChatHandler;
import com.duofei.message.SystemMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * websocket 握手拦截器
 * @author duofei
 * @date 2019/8/19
 */
public class WebSocketInterceptor implements HandshakeInterceptor {


    private static final Gson gson = new GsonBuilder().create();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 连接建立时，获取用户名
        if(request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String userName = servletRequest.getServletRequest().getParameter("userName");
            if (userName != null) {
                attributes.put("userName", userName);
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
