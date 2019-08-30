package com.duofei.constant;

/**
 * 消息处理的模式
 * @author duofei
 * @date 2019/8/20
 */
public enum MessageProcessMode {
    /**
     * 直接转发，无需做任何处理
     */
    REDIRECT("redirect"),
    /**
     * 一对一的sdp协商
     */
    SDP_ONETOONE("sdp_onetoone"),
    /**
     * 一对多的sdp协商
     */
    SDP_ONETOMANY("sdp_onetomany"),
    /**
     * 多对多
     */
    SDP_MANYTOMANY("sdp_manytomany"),
    /**
     * 交互消息
     */
    MUTUAL("mutual"),
    /**
     * 默认
     */
    DEFAULT("");

    private String value;

    MessageProcessMode(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageProcessMode getMessageProcessMode(String value){
        final MessageProcessMode[] values = MessageProcessMode.values();
        for (int i = 0; i < values.length; i++) {
            MessageProcessMode mode = values[i];
            if(mode.getValue().equals(value)){
                return mode;
            }
        }
        return MessageProcessMode.DEFAULT;
    }
}
