package com.duofei.user;

import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author duofei
 * @date 2019/8/16
 */
public class GroupUser extends BaseUser {

    /**
     * 域id
     */
    private String scopeId;

    private final WebRtcEndpoint outgoingMedia;

    private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    private MediaPipeline mediaPipeline;

    public GroupUser(String userName, WebSocketSession webSocketSession,WebRtcEndpoint outgoingMedia, String scopeId) {
        super(userName, webSocketSession);
        this.scopeId = scopeId;
        this.outgoingMedia = outgoingMedia;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public WebRtcEndpoint getOutgoingMedia() {
        return outgoingMedia;
    }

    /**
     * 添加输入端
     * @author duofei
     * @date 2019/8/29
     * @param name 当前用户名+输入端用户名
     * @param webRtcEndpoint
     */
    public void addIncomingMedia(String name,WebRtcEndpoint webRtcEndpoint){
        this.incomingMedia.put(name, webRtcEndpoint);
    }

    public MediaPipeline getMediaPipeline() {
        return mediaPipeline;
    }

    public void setMediaPipeline(MediaPipeline mediaPipeline) {
        this.mediaPipeline = mediaPipeline;
    }
}
