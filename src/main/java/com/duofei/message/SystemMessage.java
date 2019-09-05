package com.duofei.message;

/**
 * 系统消息：系统通知
 * @author duofei
 * @date 2019/8/16
 */
public class SystemMessage extends BaseMessage<Object> {

    /**
     * 失败或者成功的类型判断
     */
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
