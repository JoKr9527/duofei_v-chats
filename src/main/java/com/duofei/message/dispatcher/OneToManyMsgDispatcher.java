package com.duofei.message.dispatcher;

import com.duofei.message.MsgHandler;
import com.duofei.context.UserContext;
import com.duofei.event.BroadcastRoomEvent;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.MsgHandle;
import com.duofei.message.model.SystemMessage;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.OneToManyScopeFactory;
import com.duofei.user.BaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 一对多聊天消息分发器
 * @author duofei
 * @date 2019/9/3
 */
@Component
public class OneToManyMsgDispatcher implements MsgDispatcher {

    @Autowired
    private UserContext userContext;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private OneToManyScopeFactory oneToManyScopeFactory;

    private static Map<String, MsgHandle> msgHandles = new HashMap<>();

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandles;
    }

    /**
     * 接收创建直播间请求
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle createBroadcastRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            BaseUser baseUser = userContext.getE(userMessage.getFrom());
            String scopeId = oneToManyScopeFactory.create(userMessage.getFrom(), ((String) userMessage.getContent()));
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setId("oneToManyScopeSuccess");
            systemMessage.setContent(scopeId);
            applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),systemMessage));
            applicationContext.publishEvent(new BroadcastRoomEvent(new Object()));
        };
        return result;
    }

    /**
     * 接收加入直播间请求
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle joinBroadcastRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            BaseUser baseUser = userContext.getE(userMessage.getFrom());
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setId("joinOneToManyScopeSuccess");
            systemMessage.setContent(userMessage.getTo());
            applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),systemMessage));
        };
        return result;
    }

    /**
     * 激活
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle active(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->
            oneToManyScopeFactory.active(userMessage.getTo(),userMessage.getFrom());
        return result;
    }

    /**
     * 关闭直播间
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle closeBroadcastRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            // 主持人关闭直播间，给参观者发送提醒
            oneToManyScopeFactory.notifyMembers(userMessage.getTo(),null,userMessage);
            oneToManyScopeFactory.dispose(userMessage.getTo());
            // 房间消息
            applicationContext.publishEvent(new BroadcastRoomEvent(new Object()));
        };
        return result;
    }

    /**
     * 退出直播间
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle quitBroadcastRoom(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            // 参观者退出直播间，发送提醒
            oneToManyScopeFactory.notifyMembers(userMessage.getTo(),userMessage.getFrom(),userMessage);
            oneToManyScopeFactory.notifyPresenter(userMessage.getTo(), userMessage);
            // 销毁参观者
            oneToManyScopeFactory.removeMember(userMessage.getTo(),userMessage.getFrom());
        };
        return result;
    }
}
