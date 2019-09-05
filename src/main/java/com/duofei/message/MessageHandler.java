package com.duofei.message;

import com.duofei.message.model.BaseMessage;
import com.duofei.scope.Scope;
import com.duofei.user.BaseUser;

import java.util.List;

/**
 * 消息处理器
 * @author duofei
 * @date 2019/8/20
 */
public interface MessageHandler {

    /**
     * 处理消息
     * @author duofei
     * @date 2019/8/20
     * @param scope 消息涉及到的域
     * @param baseUsers 消息涉及到的用户
     * @param baseMessage 消息
     */
    <T> void handle(Scope scope, List<BaseUser> baseUsers, BaseMessage<T> baseMessage);
}
