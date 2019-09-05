package com.duofei.message;

import com.duofei.handler.MsgHandle;
import com.duofei.utils.JsonUtils;

import java.util.Map;

/**
 * 消息中心
 * @author duofei
 * @date 2019/9/3
 */
public interface MsgDispatcher {

    /**
     * 存放消息处理器
     * @author duofei
     * @date 2019/9/3
     * @return Map<String, MsgHandle> 消息id 对应处理器
     */
    Map<String, MsgHandle> msgHandles();

    /**
     * 根据消息id不同分配给不同的消息处理器
     * @author duofei
     * @date 2019/9/3
     * @param msg
     */
    default void dispatch(String msg) throws Exception{
        UserMessage userMessage = JsonUtils.fromJson(msg, UserMessage.class);
        if(msgHandles().containsKey(userMessage.getId())){
            msgHandles().get(userMessage.getId()).handle(userMessage);
        }
    }
}
