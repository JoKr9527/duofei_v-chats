package com.duofei.event;

import org.springframework.context.ApplicationEvent;

/**
 * 在线用户通知事件
 * @author duofei
 * @date 2019/8/20
 */
public class OnlineUsersEvent extends ApplicationEvent {

    public OnlineUsersEvent(Object source) {
        super(source);
    }

}
