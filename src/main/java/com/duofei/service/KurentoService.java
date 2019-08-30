package com.duofei.service;

import com.duofei.message.IceCandidateUserMessage;
import com.google.gson.JsonObject;
import org.kurento.client.*;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * 封装了kurento的一些常用方法
 *
 * @author duofei
 * @date 2019/8/21
 */
@Service
public class KurentoService {

    private static Logger logger = LoggerFactory.getLogger(KurentoService.class);

    @Autowired
    private KurentoClient kurentoClient;

    /**
     * 根据sdp offer建立 WebRtcEndpoint, 并设置 icecandidate
     * @param mediaPipeline 媒体管道
     * @param webSocketSession session 通道
     * @return WebRtcEndpoint 创建好的 WebRtcEndpoint
     * @author duofei
     * @date 2019/8/21
     */
    public WebRtcEndpoint createWebRtcEndpoint(MediaPipeline mediaPipeline,WebSocketSession webSocketSession) {
        return createWebRtcEndpoint(mediaPipeline, webSocketSession, ((String) webSocketSession.getAttributes().get("userName")));
    }

    /**
     * 根据sdp offer建立 指定名称的WebRtcEndpoint, 并设置 icecandidate
     * @author duofei
     * @date 2019/8/30
     * @param mediaPipeline 媒体管道
     * @param webSocketSession session 通道
     * @param name webRtcEndpoint 名称
     * @return WebRtcEndpoint 创建好的 WebRtcEndpoint
     * @throws
     */
    public WebRtcEndpoint createWebRtcEndpoint(MediaPipeline mediaPipeline,WebSocketSession webSocketSession, String name) {
        WebRtcEndpoint result = new WebRtcEndpoint.Builder(mediaPipeline).build();
        result.setName(name);
        result.addIceCandidateFoundListener(event -> {
            IceCandidateUserMessage iceCandidateUserMessage = new IceCandidateUserMessage();
            iceCandidateUserMessage.setId("iceCandidate");
            iceCandidateUserMessage.setContent(event.getCandidate());
            try {
                synchronized (webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(JsonUtils.toJson(iceCandidateUserMessage)));
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        });
        return result;
    }

    /**
     * 创建新的mediaPipeline
     *
     * @return MediaPipeline
     * @author duofei
     * @date 2019/8/21
     */
    public MediaPipeline createMediaPipeline() {
        return kurentoClient.createMediaPipeline();
    }
}
