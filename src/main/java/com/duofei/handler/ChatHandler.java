package com.duofei.handler;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.BroadcastRoomEvent;
import com.duofei.event.MeetRoomEvent;
import com.duofei.event.OnlineUsersEvent;
import com.duofei.message.dispatcher.MsgDispatcher;
import com.duofei.message.dispatcher.PeopleRoomMsgDispatcher;
import com.duofei.message.dispatcher.RecordMsgDispatcher;
import com.duofei.message.model.SystemMessage;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.*;
import com.duofei.scope.factory.GroupScopeFactory;
import com.duofei.scope.factory.OneToManyScopeFactory;
import com.duofei.scope.factory.OneToOneScopeFactory;
import com.duofei.user.BaseUser;
import com.duofei.utils.JsonUtils;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天处理的文本handler
 * @author duofei
 * @date 2019/8/19
 */
public class ChatHandler extends TextWebSocketHandler {

    private static Map<String, MsgDispatcher> msgDispatcherMap = new HashMap<>();

    @Resource
    private UserContext userContext;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private WebRtcEndpointContext webRtcEndpointContext;
    @Resource
    private ScopeContext scopeContext;
    @Autowired
    private OneToManyScopeFactory oneToManyScopeFactory;
    @Autowired
    private OneToOneScopeFactory oneToOneScopeFactory;
    @Autowired
    private GroupScopeFactory groupScopeFactory;
    @Resource
    private MsgDispatcher sdpMsgDispatcher;
    @Resource
    private MsgDispatcher oneToOneMsgDispatcher;
    @Resource
    private MsgDispatcher oneToManyMsgDispatcher;
    @Resource
    private MsgDispatcher groupMsgDispatcher;
    @Resource
    private PeopleRoomMsgDispatcher peopleRoomMsgDispatcher;
    @Resource
    private RecordMsgDispatcher recordMsgDispatcher;

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
        JsonObject jsonObject = JsonUtils.fromJson(message.getPayload());
        // 根据消息类型转换为具体对象
        final String messageType = jsonObject.get("messageType").getAsString();
        MsgDispatcher msgDispatcher = msgDispatcherMap.get(messageType);
        /*if(msgDispatcher != null){
            msgDispatcher.dispatch(message.getPayload());
        }*/
        switch (messageType){
            case "SdpMsg":
                sdpMsgDispatcher.dispatch(message.getPayload());
                break;
            case "OneToOneMsg":
                oneToOneMsgDispatcher.dispatch(message.getPayload());
                break;
            case "OneToManyMsg":
                oneToManyMsgDispatcher.dispatch(message.getPayload());
                break;
            case "GroupMsg":
                groupMsgDispatcher.dispatch(message.getPayload());
                break;
            case "PeopleRoomMsg":
                peopleRoomMsgDispatcher.dispatch(message.getPayload());
                break;
            case "RecordMsg":
                recordMsgDispatcher.dispatch(message.getPayload());
                break;
            default:
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        final String userName = (String) session.getAttributes().get("userName");
        userScopeClean(userName);
        userContext.removeE(userName);
        webRtcEndpointContext.removeE(userName);
        // 发布在线用户消息
        applicationContext.publishEvent(new OnlineUsersEvent(userContext));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final String userName = (String) session.getAttributes().get("userName");
        userScopeClean(userName);
        userContext.removeE(userName);
        webRtcEndpointContext.removeE(userName);
        // 发布在线用户消息
        applicationContext.publishEvent(new OnlineUsersEvent(userContext));
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }

    /**
     * 新增消息分发器
     * @author duofei
     * @date 2019/9/23
     * @param name 消息分发器名称
     * @param msgDispatcher 消息分发器
     */
    public void addMsgDispatcher(String name, MsgDispatcher msgDispatcher){
        msgDispatcherMap.put(name, msgDispatcher);
    }

    /**
     * 当用户退出时，需要进行的域清理以及通知
     * @author duofei
     * @date 2019/9/5
     * @param userName
     */
    private void userScopeClean(String userName){
        BaseUser base = userContext.getE(userName);
        if(base != null && base.getScopeId() != null){
            Scope scope = scopeContext.getE(base.getScopeId());
            if(scope instanceof OneToOneScope){
                OneToOneScope oneToOneScope = (OneToOneScope) scope;
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setContent("网络出现错误，连接失败");
                systemMessage.setId("hangup");
                oneToOneScopeFactory.notifyMembers(oneToOneScope.getId(),userName,systemMessage);
                oneToOneScopeFactory.dispose(oneToOneScope.getId());
            }else if(scope instanceof OneToManyScope){
                OneToManyScope oneToManyScope = (OneToManyScope) scope;
                // 如果是直播间创建者
                if(userName.equals(oneToManyScope.getPresenterUserName())){
                    // 发送异常信息
                    SystemMessage systemMessage = new SystemMessage();
                    systemMessage.setContent("网络出现错误，直播间已关闭");
                    systemMessage.setId("closeBroadcastRoom");
                    oneToManyScopeFactory.notifyMembers(oneToManyScope.getId(),null,systemMessage);
                    oneToManyScopeFactory.dispose(oneToManyScope.getId());
                    // 房间消息
                    applicationContext.publishEvent(new BroadcastRoomEvent(new Object()));
                }else {
                    // 参观者异常退出直播间，发送提醒
                    SystemMessage systemMessage = new SystemMessage();
                    systemMessage.setContent(userName + "退出直播间");
                    systemMessage.setId("quitBroadcastRoom");
                    oneToManyScopeFactory.notifyMembers(oneToManyScope.getId(),userName,systemMessage);
                    oneToManyScopeFactory.notifyPresenter(oneToManyScope.getId(), systemMessage);
                    // 销毁参观者
                    oneToManyScopeFactory.removeMember(oneToManyScope.getId(),userName);
                }
            }else if(scope instanceof GroupScope){
                GroupScope groupScope = (GroupScope) scope;
                // 异常退出会议室，给域中其它成员发送成员退出消息
                UserMessage userMessage = new UserMessage();
                userMessage.setFrom(userName);
                userMessage.setId("quitMeetRoom");
                groupScopeFactory.notifyMembers(groupScope.getId(),userName,userMessage);
                groupScopeFactory.quitMeetRoom(groupScope.getId(),userName);
            }
        }
    }
}
