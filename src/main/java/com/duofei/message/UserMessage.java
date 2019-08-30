package com.duofei.message;

/**
 * 用户交互消息：用于流程控制，会话协商
 * @author duofei
 * @date 2019/8/16
 */
public class UserMessage<T> extends AbstractMessage<T> {

    /**
     * 消息发送者
     */
    private String from;
    /**
     * 消息接收者
     */
    private String to;
    /**
     * 消息处理模式
     */
    private String messageProcessMode;

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

    public String getMessageProcessMode() {
        return messageProcessMode;
    }

    public void setMessageProcessMode(String messageProcessMode) {
        this.messageProcessMode = messageProcessMode;
    }
}
