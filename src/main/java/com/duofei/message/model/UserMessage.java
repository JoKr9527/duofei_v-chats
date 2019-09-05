package com.duofei.message.model;

import com.duofei.message.model.BaseMessage;

/**
 * 用户交互消息：用于流程控制，会话协商
 * @author duofei
 * @date 2019/8/16
 */
public class UserMessage<T> extends BaseMessage<T> {

    /**
     * 消息发送者
     */
    private String from;
    /**
     * 消息接收者
     */
    private String to;
    /**
     * 存放额外的字段
     */
    private String other;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
