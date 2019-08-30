package com.duofei.message;

import com.duofei.constant.MessageProcessMode;
import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.BroadcastRoomEvent;
import com.duofei.event.MeetRoomEvent;
import com.duofei.event.MsgSendEvent;
import com.duofei.handler.MessageHandler;
import com.duofei.scope.*;
import com.duofei.user.BaseUser;
import com.duofei.utils.JsonUtils;
import com.google.gson.JsonObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 消息处理中心
 * @author duofei
 * @date 2019/8/20
 */
@Component
public class MessageHandleCenter {

    @Resource
    private UserContext userContext;
    @Resource
    private ScopeContext scopeContext;
    @Resource(name="redirectMessageHandler")
    private MessageHandler redirectMessageHandler;
    @Resource
    private OneToOneScopeFactory oneToOneScopeFactory;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private WebRtcEndpointContext webRtcEndpointContext;
    @Resource
    private OneToManyScopeFactory oneToManyScopeFactory;
    @Resource
    private GroupScopeFactory groupScopeFactory;
    
    /**
     * 处理消息
     * @author duofei
     * @date 2019/8/20
     * @param message
     */
    public void handleMessage(String message){
        JsonObject jsonObject = JsonUtils.fromJson(message);

        // 根据消息类型转换为具体对象
        final String messageType = jsonObject.get("messageType").getAsString();
        switch (messageType){
            case "system":
                handleSysMsg(JsonUtils.fromJson(message,SystemMessage.class));
                break;
            case "user":
                handleUserMsg(JsonUtils.fromJson(message, UserMessage.class));
                break;
            case "onIceCandidate":
                handleIceCandidateUserMsg(JsonUtils.fromJson(message,IceCandidateUserMessage.class));
                break;
            default:
        }

    }

    /**
     * 处理系统消息
     * @author duofei
     * @date 2019/8/20
     * @param systemMessage 系统消息
     */
    private void handleSysMsg(SystemMessage systemMessage){

    }

    /**
     * 处理用户消息
     * @author duofei
     * @date 2019/8/20
     * @param userMessage 用户消息
     */
    private void handleUserMsg(UserMessage userMessage){
        MessageProcessMode messageProcessMode = MessageProcessMode.getMessageProcessMode(userMessage.getMessageProcessMode());
        if(userMessage.getTo() != null || userMessage.getFrom() != null){
            BaseUser toUser = userContext.getE(userMessage.getTo());
            BaseUser fromUser = userContext.getE(userMessage.getFrom());
            if(messageProcessMode == MessageProcessMode.REDIRECT && toUser != null){
                if("reqVideoCall".equals(userMessage.getId())){
                    // 如果对方处于通话中，拒绝建立连接
                    if(webRtcEndpointContext.contains(userMessage.getTo())){
                        SystemMessage systemMessage = new SystemMessage();
                        systemMessage.setId("errorMsg");
                        systemMessage.setContent("对方忙");
                        if(fromUser != null){
                            applicationContext.publishEvent(new MsgSendEvent(fromUser.getSession(),systemMessage));
                        }
                    }else{
                        redirectMessageHandler.handle(null, Stream.of(toUser).collect(Collectors.toList()), userMessage);
                    }
                }else {
                    redirectMessageHandler.handle(null, Stream.of(toUser).collect(Collectors.toList()), userMessage);
                }
            }else if(messageProcessMode == MessageProcessMode.SDP_ONETOONE){
                if("call".equals(userMessage.getId())){
                    // 创建一对一域
                    String id = oneToOneScopeFactory.create(userMessage.getFrom(), (String) userMessage.getContent());
                    UserMessage temp = new UserMessage();
                    temp.setContent(id);
                    temp.setId("callee");
                    applicationContext.publishEvent(new MsgSendEvent(toUser.getSession(),temp));
                    // 通知其他成员，域创建成功
                    SystemMessage systemMessage = new SystemMessage();
                    systemMessage.setId("joinScope");
                    systemMessage.setContent(id);
                    applicationContext.publishEvent(new MsgSendEvent(fromUser.getSession(),systemMessage));
                }else if("callee".equals(userMessage.getId())){
                    oneToOneScopeFactory.enableScope(userMessage.getTo(),userMessage.getFrom(), (String) userMessage.getContent());
                }
            }else if(messageProcessMode == MessageProcessMode.MUTUAL){
                if("hangup".equals(userMessage.getId())){
                    // 给域中其它用户发送提醒消息
                    oneToOneScopeFactory.notifyMembers(userMessage.getTo(),userMessage.getFrom(),userMessage);
                    oneToOneScopeFactory.dispose(userMessage.getTo());
                }else if("createBroadcastRoom".equals(userMessage.getId())){
                    // 申请创建直播间
                    String scopeId = oneToManyScopeFactory.create(userMessage.getFrom(), ((String) userMessage.getContent()));
                    // 生成响应信息
                    SystemMessage systemMessage = new SystemMessage();
                    systemMessage.setType("success");
                    systemMessage.setId("createBroadcastRoomResp");
                    systemMessage.setContent(scopeId);
                    applicationContext.publishEvent(new MsgSendEvent(fromUser.getSession(),systemMessage));
                    // 发送在线直播间通知
                    applicationContext.publishEvent(new BroadcastRoomEvent(new Object()));
                }else if("closeBroadcastRoom".equals(userMessage.getId())){
                    // 主持人关闭直播间，给参观者发送提醒
                    oneToManyScopeFactory.notifyMembers(userMessage.getTo(),null,userMessage);
                    oneToManyScopeFactory.dispose(userMessage.getTo());
                    // 房间消息
                    applicationContext.publishEvent(new BroadcastRoomEvent(new Object()));
                }else if("quitBroadcastRoom".equals(userMessage.getId())){
                    // 参观者退出直播间，发送提醒
                    oneToManyScopeFactory.notifyMembers(userMessage.getTo(),userMessage.getFrom(),userMessage);
                    oneToManyScopeFactory.notifyPresenter(userMessage.getTo(), userMessage);
                    // 销毁参观者
                    Scope scope = scopeContext.getE(userMessage.getTo());
                    if(scope != null && scope instanceof OneToManyScope){
                        OneToManyScope oneToManyScope = (OneToManyScope) scope;
                        oneToManyScope.removeViewer(userMessage.getFrom());
                    }
                }else if ("createMeetRoom".equals(userMessage.getId())){
                    // 申请创建会议室
                    String scopeId = groupScopeFactory.create(((String) userMessage.getContent()),userMessage.getFrom());
                    // 生成响应信息
                    SystemMessage systemMessage = new SystemMessage();
                    systemMessage.setType("success");
                    systemMessage.setId("createMeetRoomResp");
                    systemMessage.setContent(scopeId);
                    applicationContext.publishEvent(new MsgSendEvent(fromUser.getSession(),systemMessage));
                    // 发送在线会议室通知
                    applicationContext.publishEvent(new MeetRoomEvent(new Object()));
                }else if("recvMemberMedia".equals(userMessage.getId())){
                    // 请求接收指定成员流媒体数据
                    groupScopeFactory.recvMemberMedia(userMessage.getFrom(), userMessage.getTo(), ((String) userMessage.getContent()));
                }
            }else if(messageProcessMode == MessageProcessMode.SDP_ONETOMANY){
                if("call".equals(userMessage.getId())){
                    oneToManyScopeFactory.activePresenter(userMessage.getTo(), ((String) userMessage.getContent()));
                }else if("callee".equals(userMessage.getId())){
                    oneToManyScopeFactory.activeViewer(userMessage.getTo(),userMessage.getFrom(), ((String) userMessage.getContent()));
                }
            }else if(messageProcessMode == MessageProcessMode.SDP_MANYTOMANY){
                if("call".equals(userMessage.getId())){
                    groupScopeFactory.activeMember(userMessage.getTo(), userMessage.getFrom(), ((String) userMessage.getContent()));
                }
            }
        }
    }

    /**
     * 处理 ice candidate 消息
     * @author duofei
     * @date 2019/8/20
     * @param iceCandidateUserMessage 消息
     */
    private void handleIceCandidateUserMsg(IceCandidateUserMessage iceCandidateUserMessage){
        webRtcEndpointContext.addIceCandidate(iceCandidateUserMessage.getFrom(),iceCandidateUserMessage.getContent());
    }

    /**
     * 发送系统消息
     * @author duofei
     * @date 2019/8/20
     * @param id
     * @param content
     * @param baseUsers
     */
    private <T> void sendSysMsg(String id, T content, List<BaseUser> baseUsers){

    }



}
