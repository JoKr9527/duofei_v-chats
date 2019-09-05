package com.duofei.event;

import com.duofei.message.BaseMessage;
import org.springframework.context.ApplicationEvent;

/**
 * 消息发送事件
 * @author duofei
 * @date 2019/8/21
 */
public class MsgSendEvent extends ApplicationEvent {

    private BaseMessage baseMessage;

    public MsgSendEvent(Object source, BaseMessage baseMessage) {
        super(source);
        this.baseMessage = baseMessage;
    }

    public BaseMessage getBaseMessage() {
        return baseMessage;
    }

}
