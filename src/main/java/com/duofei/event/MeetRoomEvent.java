package com.duofei.event;

import org.springframework.context.ApplicationEvent;

/**
 * 在线会议室通知事件
 * @author duofei
 * @date 2019/8/29
 */
public class MeetRoomEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MeetRoomEvent(Object source) {
        super(source);
    }
}
