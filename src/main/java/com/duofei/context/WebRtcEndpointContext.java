package com.duofei.context;

import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    private static Logger logger = LoggerFactory.getLogger(WebRtcEndpointContext.class);

    @Override
    public void putE(String key, WebRtcEndpoint webRtcEndpoint) {
        webRtcEndpointMap.put(key, webRtcEndpoint);
    }

    @Override
    public void removeE(String key) {
        webRtcEndpointMap.remove(key);
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
     * @param userName 用户名
     * @param iceCandidate
     */
    public synchronized void addIceCandidate(String userName,IceCandidate iceCandidate){
        try{
            if(webRtcEndpointMap.containsKey(userName)){
                webRtcEndpointMap.get(userName).addIceCandidate(iceCandidate);
            }else{
                if(!iceCandidateMap.containsKey(userName)){
                    iceCandidateMap.put(userName, new ArrayList<>());
                }
                iceCandidateMap.get(userName).add(iceCandidate);
            }
        }catch (Exception e){
            logger.error("addIceCandidate error! userName={}",userName, e);
        }
    }

    /**
     * 为名称为userName的 WebRtcEndpoint 设置iceCandidate，如果存在iceCandidate
     * @author duofei
     * @date 2019/8/22
     * @param userName
     */
    public void setIceCandidate(String userName){
        WebRtcEndpoint webRtcEndpoint = webRtcEndpointMap.get(userName);
        if(webRtcEndpoint != null){
            Optional.ofNullable(iceCandidateMap.get(userName)).orElse(new ArrayList<>()).stream()
                    .forEach(iceCandidate -> {
                        webRtcEndpoint.addIceCandidate(iceCandidate);
                    });
        }
    }
}
