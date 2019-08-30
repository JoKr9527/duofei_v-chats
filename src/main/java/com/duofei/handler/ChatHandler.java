package com.duofei.handler;

import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.BroadcastRoomEvent;
import com.duofei.event.MeetRoomEvent;
import com.duofei.event.OnlineUsersEvent;
import com.duofei.message.MessageHandleCenter;
import com.duofei.user.BaseUser;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;

/**
 * 聊天处理的文本handler
 * @author duofei
 * @date 2019/8/19
 */
public class ChatHandler extends TextWebSocketHandler {

    @Resource
    private UserContext userContext;
    @Resource
    private MessageHandleCenter messageHandleCenter;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private WebRtcEndpointContext webRtcEndpointContext;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        final String userName = (String) session.getAttributes().get("userName");
        BaseUser baseUser = new BaseUser(userName,session);
        userContext.putE(userName,baseUser);
        // 发布在线用户消息
        applicationContext.publishEvent(new OnlineUsersEvent(userContext));
        // 房间消息
        applicationContext.publishEvent(new BroadcastRoomEvent(new Object()));
        // 发送在线会议室通知
        applicationContext.publishEvent(new MeetRoomEvent(new Object()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        messageHandleCenter.handleMessage(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        final String userName = (String) session.getAttributes().get("userName");
        userContext.removeE(userName);
        webRtcEndpointContext.removeE(userName);
        // 发布在线用户消息
        applicationContext.publishEvent(new OnlineUsersEvent(userContext));

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final String userName = (String) session.getAttributes().get("userName");
        userContext.removeE(userName);
        webRtcEndpointContext.removeE(userName);
        // 发布在线用户消息
        applicationContext.publishEvent(new OnlineUsersEvent(userContext));
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }
}
