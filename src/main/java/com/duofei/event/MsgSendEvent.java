package com.duofei.event;

import com.duofei.message.AbstractMessage;
import org.springframework.context.ApplicationEvent;

/**
 * 消息发送事件
 * @author duofei
 * @date 2019/8/21
 */
public class MsgSendEvent extends ApplicationEvent {

    private AbstractMessage abstractMessage;

    public MsgSendEvent(Object source, AbstractMessage abstractMessage) {
        super(source);
        this.abstractMessage = abstractMessage;
    }

    public AbstractMessage getAbstractMessage() {
        return abstractMessage;
    }

}
