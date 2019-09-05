package com.duofei.context;

import com.duofei.event.MsgSendEvent;
import com.duofei.message.model.UserMessage;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * webRtcEndpoint域
 * @author duofei
 * @date 2019/8/21
 */
@Component
public class WebRtcEndpointContext implements Context<WebRtcEndpoint> {

    private Map<String,WebRtcEndpoint> webRtcEndpointMap = new ConcurrentHashMap<>();

    private Map<String, List<IceCandidate>> iceCandidateMap = new ConcurrentHashMap<>();

    private Map<String, String> sdpOfferMap = new ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(WebRtcEndpointContext.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void putE(String key, WebRtcEndpoint webRtcEndpoint) {
        webRtcEndpointMap.put(key, webRtcEndpoint);
    }

    @Override
    public void removeE(String key) {
        webRtcEndpointMap.remove(key);
        // 移除相关元素
        iceCandidateMap.remove(key);
        sdpOfferMap.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return webRtcEndpointMap.containsKey(key);
    }

    @Override
    public int eSize() {
        return webRtcEndpointMap.size();
    }

    @Override
    public Set<String> holds() {
        return webRtcEndpointMap.keySet();
    }

    @Override
    public WebRtcEndpoint getE(String key) {
        return webRtcEndpointMap.get(key);
    }

    @Override
    public Set<WebRtcEndpoint> holdE() {
        Set<WebRtcEndpoint> result = new HashSet<>();
        webRtcEndpointMap.forEach((key, webRtcEndpoint)->result.add(webRtcEndpoint));
        return result;
    }

    /**
     * icecandidate 管理
     * @author duofei
     * @date 2019/8/22
     * @param name 用户名
     * @param iceCandidate
     */
    public synchronized void addIceCandidate(String name,IceCandidate iceCandidate){
        try{
            if(webRtcEndpointMap.containsKey(name)){
                webRtcEndpointMap.get(name).addIceCandidate(iceCandidate);
            }else{
                if(!iceCandidateMap.containsKey(name)){
                    iceCandidateMap.put(name, new ArrayList<>());
                }
                iceCandidateMap.get(name).add(iceCandidate);
            }
        }catch (Exception e){
            logger.error("addIceCandidate error! name={}",name, e);
        }
    }

    /**
     * 为名称为name的 WebRtcEndpoint 设置iceCandidate，如果存在iceCandidate
     * @author duofei
     * @date 2019/8/22
     * @param name
     */
    private void setIceCandidate(String name){
        WebRtcEndpoint webRtcEndpoint = webRtcEndpointMap.get(name);
        if(webRtcEndpoint != null){
            Optional.ofNullable(iceCandidateMap.get(name)).orElse(new ArrayList<>()).stream()
                    .forEach(iceCandidate -> {
                        webRtcEndpoint.addIceCandidate(iceCandidate);
                    });
        }
    }

    /**
     * 存放 sdpOffer
     * @author duofei
     * @date 2019/9/5
     * @param name
     * @param sdpOffer
     */
    public void setSdpOffer(String name, String sdpOffer){
        this.sdpOfferMap.put(name, sdpOffer);
    }

    /**
     * 激活 webrtc
     * @author duofei
     * @date 2019/9/5
     * @param
     * @return
     * @throws
     */
    public void activeWebRtc(String name, WebSocketSession session){
        WebRtcEndpoint webRtcEndpoint = webRtcEndpointMap.get(name);
        if(webRtcEndpoint != null){
            String sdpAnswer = webRtcEndpoint.processOffer(sdpOfferMap.get(webRtcEndpoint.getName()));
            webRtcEndpoint.gatherCandidates();
            UserMessage waitSendMsg = new UserMessage();
            waitSendMsg.setId("sdpAnswer");
            waitSendMsg.setFrom(name);
            waitSendMsg.setContent(sdpAnswer);
            applicationContext.publishEvent(new MsgSendEvent(session, waitSendMsg));
            setIceCandidate(name);
        }
    }
}
