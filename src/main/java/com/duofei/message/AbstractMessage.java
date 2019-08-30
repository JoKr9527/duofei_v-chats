package com.duofei.message;

/**
 * 消息
 * @author duofei
 * @date 2019/8/16
 */
public abstract class AbstractMessage<T> {

    /**
     * 消息id：标识消息
     */
    private String id;

    private T content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
