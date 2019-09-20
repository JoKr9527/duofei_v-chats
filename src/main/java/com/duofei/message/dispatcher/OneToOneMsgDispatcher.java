package com.duofei.message.dispatcher;

import com.duofei.message.MsgHandler;
import com.duofei.constant.RespStatus;
import com.duofei.context.UserContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.MsgHandle;
import com.duofei.message.model.SystemMessage;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.OneToOneScope;
import com.duofei.scope.factory.OneToOneScopeFactory;
import com.duofei.user.BaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 一对一聊天消息分发器
 * @author duofei
 * @date 2019/9/3
 */
@Component
public class OneToOneMsgDispatcher implements MsgDispatcher {

    @Autowired
    private UserContext userContext;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private OneToOneScopeFactory oneToOneScopeFactory;

    private static Map<String, MsgHandle> msgHandles = new HashMap<>();

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandles;
    }

    /**
     * 发起呼叫请求
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle call(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            BaseUser baseUser = userContext.getE(userMessage.getTo());
            applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),userMessage));
        };
        return result;
    }

    /**
     * 响应呼叫请求
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle callResp(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            BaseUser baseUser = userContext.getE(userMessage.getTo());
            applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),userMessage));
            if (RespStatus.ACCEPT.equals(userMessage.getContent())) {
                OneToOneScope scope = oneToOneScopeFactory.create(userMessage.getTo());
                scope.setCallingToUserName(userMessage.getFrom());
                scope.setCallingFromUserName(userMessage.getTo());
                // 通知其他成员，域创建成功
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setId("oneToOneScopeSuccess");
                systemMessage.setContent(scope.getId());
                applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),systemMessage));
                BaseUser fromUser = userContext.getE(userMessage.getFrom());
                applicationContext.publishEvent(new MsgSendEvent(fromUser.getSession(),systemMessage));
            }else if(RespStatus.REFUSE.equals(userMessage.getContent())){
                //TODO HXF 不做任何事
            }
        };
        return result;
    }

    /**
     * 请求在域中激活
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle active(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->
            oneToOneScopeFactory.active(userMessage.getTo(), userMessage.getFrom(), userMessage.getOther());
        return result;
    }

    /**
     * 处理挂断请求
     * @author duofei
     * @date 2019/9/4
     * @return MsgHandle
     */
    @MsgHandler
    private MsgHandle hangup(){
        MsgHandle<UserMessage> result = (UserMessage userMessage)->{
            // 给域中其它用户发送提醒消息
            oneToOneScopeFactory.notifyMembers(userMessage.getTo(),userMessage.getFrom(),userMessage);
            oneToOneScopeFactory.dispose(userMessage.getTo());
        };
        return result;
    }
}
