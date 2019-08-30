package com.duofei.scope;

import org.kurento.client.MediaPipeline;

/**
 * 基础的域
 * @author duofei
 * @date 2019/8/16
 */
public abstract class BaseScope implements Scope{

    /**
     * 域的唯一id
     */
    private String id;
    /**
     * 域的名称
     */
    private String name;
    /**
     * 发起者名称
     */
    private String userName;
    /**
     * 媒体管道
     */
    private MediaPipeline mediaPipeline;

    protected BaseScope(String id,MediaPipeline mediaPipeline){
        this.id = id;
        this.mediaPipeline = mediaPipeline;
    }

    protected BaseScope(String id){
        this.id = id;
    }

    protected BaseScope(String id,String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public MediaPipeline getMediaPipeline() {
        return mediaPipeline;
    }

    public void setMediaPipeline(MediaPipeline mediaPipeline) {
        this.mediaPipeline = mediaPipeline;
    }

    @Override
    public void dispose() {
        mediaPipeline.release();
    }
}
