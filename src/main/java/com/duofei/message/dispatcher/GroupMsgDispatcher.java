package com.duofei.message.dispatcher;

import com.duofei.message.MsgHandler;
import com.duofei.context.UserContext;
import com.duofei.event.MeetRoomEvent;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.MsgHandle;
import com.duofei.message.model.SystemMessage;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.GroupScopeFactory;
import com.duofei.user.BaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 群组聊天消息分发器
 * @author duofei
 * @date 2019/9/3
 */
@Component
public class GroupMsgDispatcher implements MsgDispatcher {

    @Autowired
    private UserContext userContext;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GroupScopeFactory groupScopeFactory;

    private static Map<String, MsgHandle> msgHandles = new HashMap<>();

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandles;
    }

    /**
     * 创建会议室
     * @author duofei
     * @date 2019/9/4
     */
    @MsgHandler
    public MsgHandle createMeetRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) ->{
            String scopeId = groupScopeFactory.create(userMessage.getFrom(), (String) userMessage.getContent());
            // 生成响应信息
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setType("success");
            systemMessage.setId("createMeetRoomResp");
            systemMessage.setContent(scopeId);
            BaseUser fromUser = userContext.getE(userMessage.getFrom());
            applicationContext.publishEvent(new MsgSendEvent(fromUser.getSession(),systemMessage));
            // 发送在线会议室通知
            applicationContext.publishEvent(new MeetRoomEvent(new Object()));
        };
        return result;
    }

    /**
     * 接收加入会议室请求
     * @author duofei
     * @date 2019/9/5
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle joinMeetRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            BaseUser baseUser = userContext.getE(userMessage.getFrom());
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setId("joinManyToManyScopeSuccess");
            systemMessage.setContent(userMessage.getTo());
            applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),systemMessage));
        };
        return result;
    }

    /**
     * 激活
     * @author duofei
     * @date 2019/9/4
     */
    @MsgHandler
    public MsgHandle active(){
        MsgHandle<UserMessage> result = (UserMessage msg) ->
            groupScopeFactory.active(msg.getTo(), msg.getFrom());
        return result;
    }

    /**
     * 请求接收指定成员的流媒体数据
     * @author duofei
     * @date 2019/9/4
     */
    @MsgHandler
    public MsgHandle recvMemberMedia(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> groupScopeFactory.recvMemberMedia(userMessage.getFrom(),userMessage.getTo());
        return result;
    }

    /**
     * 退出会议室
     * @author duofei
     * @date 2019/9/4
     */
    @MsgHandler
    public MsgHandle quitMeetRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            // 给域中其它成员发送成员退出消息
            groupScopeFactory.notifyMembers(userMessage.getTo(),userMessage.getFrom(),userMessage);
            groupScopeFactory.quitMeetRoom(userMessage.getTo(),userMessage.getFrom());
        };
        return result;
    }

    /**
     * 关闭会议室
     * @author duofei
     * @date 2019/9/4
     */
    @MsgHandler
    public MsgHandle closeMeetRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            // 给域中其它成员发送会议室关闭消息
            groupScopeFactory.notifyMembers(userMessage.getTo(),userMessage.getFrom(),userMessage);
            groupScopeFactory.closeMeetRoom(userMessage.getTo(),userMessage.getFrom());
        };
        return result;
    }

}
