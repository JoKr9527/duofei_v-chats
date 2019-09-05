package com.duofei.message;

/**
 * 消息处理器
 * @author duofei
 * @date 2019/9/3
 */
public interface MsgHandle<T> {

    /**
     * 处理消息
     * @author duofei
     * @date 2019/9/3
     * @param  baseMessage 处理消息
     * @throws Exception 消息处理异常
     */
    void handle(T baseMessage) throws Exception;
}
