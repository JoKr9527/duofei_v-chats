package com.duofei.message.dispatcher;

import com.duofei.constant.RespStatus;
import com.duofei.context.UserContext;
import com.duofei.message.MsgHandle;
import com.duofei.message.MsgHandler;
import com.duofei.message.model.SystemMessage;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.factory.PeopleRoomScopeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duofei
 * @date 2019/9/19
 */
@Component
public class PeopleRoomMsgDispatcher implements MsgDispatcher {

    @Autowired
    private UserContext userContext;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private PeopleRoomScopeFactory peopleRoomScopeFactory;

    private static Map<String, MsgHandle> msgHandles = new HashMap<>();

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandles;
    }

    /**
     * 邀请他人加入多人聊天
     * @author duofei
     * @date 2019/9/19
     */
    @MsgHandler
    public MsgHandle inviteJoinPeopleRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) ->{
            // 根据被邀请人创建域
            String scopeId = peopleRoomScopeFactory.create(userMessage.getFrom(), ((List<String>) userMessage.getContent()));
            // 给域中成员发送邀请信息
            UserMessage inviteMsg = new UserMessage();
            inviteMsg.setFrom(userMessage.getFrom());
            inviteMsg.setContent(userMessage.getContent());
            inviteMsg.setOther(scopeId);
            inviteMsg.setId("inviteJoinPeopleRoom");
            peopleRoomScopeFactory.notifyMembers(scopeId,userMessage.getFrom(),inviteMsg);
        };
        return result;
    }

    /**
     * 响应加入多人聊天的请求
     * @author duofei
     * @date 2019/9/19
     */
    @MsgHandler
    public MsgHandle respInviteJoinPeopleRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) ->{
            if(userMessage.getContent().equals(RespStatus.ACCEPT)){
                peopleRoomScopeFactory.join(userMessage.getTo(),userMessage.getFrom());
                // 发送创建成功通知
                SystemMessage peopleRoomSuccessMsg = new SystemMessage();
                peopleRoomSuccessMsg.setId("peopleRoomSuccess");
                peopleRoomSuccessMsg.setContent(userMessage.getTo());
                peopleRoomScopeFactory.notifyMember(userMessage.getTo(),userMessage.getFrom(),1,peopleRoomSuccessMsg);
                peopleRoomScopeFactory.notifyMember(userMessage.getTo(),null,1,peopleRoomSuccessMsg);
            }else if(userMessage.getContent().equals(RespStatus.REFUSE)){
                peopleRoomScopeFactory.refuse(userMessage.getTo(),userMessage.getFrom());
            }
        };
        return result;
    }

    /**
     * 接受客户端激活请求
     * @author duofei
     * @date 2019/9/19
     */
    @MsgHandler
    public MsgHandle active(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) ->{
            peopleRoomScopeFactory.active(userMessage.getTo(),userMessage.getFrom());
        };
        return result;
    }

}
