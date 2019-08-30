package com.duofei.event;

import org.springframework.context.ApplicationEvent;

/**
 * 直播间信息通知事件
 * @author duofei
 * @date 2019/8/27
 */
public class BroadcastRoomEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public BroadcastRoomEvent(Object source) {
        super(source);
    }
}
