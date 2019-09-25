package com.duofei.event;

import com.duofei.bean.ScopeData;
import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.message.model.SystemMessage;
import com.duofei.scope.GroupScope;
import com.duofei.scope.OneToManyScope;
import com.duofei.scope.Scope;
import com.duofei.user.BaseUser;
import com.duofei.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统通知事件
 *
 * @author duofei
 * @date 2019/8/20
 */
@Component
public class EventsListener {

    private static Logger logger = LoggerFactory.getLogger(EventsListener.class);

    @Resource
    private ScopeContext scopeContext;
    @Resource
    private UserContext userContext;

    @EventListener
    @Async("eventExecutor")
    public void onOnlineUsersEvent(OnlineUsersEvent onlineUsersEvent) {
        SystemMessage systemMessage = new SystemMessage();
        Object source = onlineUsersEvent.getSource();
        if (source instanceof UserContext) {
            UserContext userContext = (UserContext) source;
            systemMessage.setId("onlineUsers");
            userContext.holdE().forEach(baseUser -> {
                WebSocketSession session = baseUser.getSession();
                try {
                    Set<String> holds = userContext.holds().stream().collect(Collectors.toSet());
                    // 移除自己
                    holds.remove(baseUser.getUserName());
                    systemMessage.setContent(holds);
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(JsonUtils.toJSON(systemMessage)));
                        }
                    }
                } catch (IOException e) {
                    logger.error(" onOnlineUsersEvent error,to={}", baseUser.getUserName(), e);
                }

            });
        }
    }

    @EventListener
    @Async("eventExecutor")
    public void onMsgSendEvent(MsgSendEvent msgSendEvent) {
        Object source = msgSendEvent.getSource();
        if (source instanceof WebSocketSession) {
            WebSocketSession session = (WebSocketSession) source;
            if (msgSendEvent.getBaseMessage() != null) {
                try {
                    synchronized (session) {
                        if(session.isOpen()){
                            session.sendMessage(new TextMessage(JsonUtils.toJSON(msgSendEvent.getBaseMessage())));
                        }
                    }
                } catch (IOException e) {
                    logger.error(" onMsgSendEvent error,msgSendEvent={}", msgSendEvent, e);
                }
            }
        }
    }

    @EventListener
    @Async("eventExecutor")
    public void onBroadcastRoomEvent(BroadcastRoomEvent broadcastRoomEvent){
        Set<Scope> scopes = scopeContext.holdE();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setId("onlineBroadcastRoom");
        List<ScopeData> scopeDatas = new ArrayList<>();
        scopes.forEach(scope -> {
            if(scope instanceof OneToManyScope){
                OneToManyScope oneToManyScope = (OneToManyScope)scope;
                ScopeData scopeData = new ScopeData();
                scopeData.setId(oneToManyScope.getId());
                scopeData.setUserName(oneToManyScope.getUserName());
                scopeData.setName(oneToManyScope.getName());
                scopeDatas.add(scopeData);
            }
        });
        systemMessage.setContent(scopeDatas);
        String jsonMsg = JsonUtils.toJSON(systemMessage);
        sendMsg(jsonMsg);
    }

    @EventListener
    @Async("eventExecutor")
    public void onMeetRoomEvent(MeetRoomEvent meetRoomEvent){
        Set<Scope> scopes = scopeContext.holdE();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setId("onlineMeetRoom");
        List<ScopeData> scopeDatas = new ArrayList<>();
        scopes.forEach(scope -> {
            if(scope instanceof GroupScope){
                GroupScope groupScope = (GroupScope)scope;
                ScopeData scopeData = new ScopeData();
                scopeData.setId(groupScope.getId());
                scopeData.setUserName(groupScope.getUserName());
                scopeData.setName(groupScope.getName());
                scopeDatas.add(scopeData);
            }
        });
        systemMessage.setContent(scopeDatas);
        String jsonMsg = JsonUtils.toJSON(systemMessage);
        sendMsg(jsonMsg);
    }

    /**
     * 给所有在线用户发送通知消息
     * @author duofei
     * @date 2019/8/29
     * @param jsonMsg json消息
     */
    private void sendMsg(String jsonMsg){
        userContext.holds().forEach(userName -> {
            BaseUser baseUser = userContext.getE(userName);
            WebSocketSession session = baseUser.getSession();
            try {
                synchronized (session){
                    if(session.isOpen()){
                        session.sendMessage(new TextMessage(jsonMsg));
                    }
                }
            } catch (IOException e) {
                logger.error("onBroadcastRoomEvent error,userName={}",userName,e);
            }
        });
    }
}
