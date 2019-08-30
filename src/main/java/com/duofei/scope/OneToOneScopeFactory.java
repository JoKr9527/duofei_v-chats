package com.duofei.scope;

import com.duofei.bean.UserData;
import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.AbstractMessage;
import com.duofei.message.UserMessage;
import com.duofei.service.KurentoService;
import com.duofei.utils.IdGen;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * oneTooneScope 管理工厂
 * @author duofei
 * @date 2019/8/21
 */
@Component
public class OneToOneScopeFactory {

    @Autowired
    private KurentoService kurentoService;
    @Autowired
    private ScopeContext scopeContext;
    @Autowired
    private UserContext userContext;
    @Autowired
    private WebRtcEndpointContext webRtcEndpointScope;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 临时数据存储
     */
    private static Map<String, UserData> userDataMap = new ConcurrentHashMap<>();

    /**
     * 创建一个OneToOne域，存入域上下文
     * @author duofei
     * @date 2019/8/21
     * @param userName
     * @param sdpOffer
     * @return String 域的唯一id
     * @throws
     */
    public String create(String userName,String sdpOffer){
        String id = IdGen.newShortId();
        Scope scope = new OneToOneScope(id);
        scopeContext.putE(id,scope);
        UserData userData = new UserData(userName, sdpOffer);
        userDataMap.put(id, userData);
        return id;
    }

    /**
     * 激活域
     * @author duofei
     * @date 2019/8/21
     * @param id 域id
     * @param userName
     * @param sdpOffer sdpOffer
     */
    public void enableScope(String id,String userName,String sdpOffer){
        Scope scope = scopeContext.getE(id);
        UserData userData = userDataMap.get(id);
        if(scope != null && scope instanceof OneToOneScope && userData != null){
            OneToOneScope oneToOneScope = ((OneToOneScope) scope);
            oneToOneScope.setMediaPipeline(kurentoService.createMediaPipeline());
            WebRtcEndpoint callingFrom = kurentoService.createWebRtcEndpoint(oneToOneScope.getMediaPipeline(), userContext.getE(userData.getUserName()).getSession());
            WebRtcEndpoint callingTo = kurentoService.createWebRtcEndpoint(oneToOneScope.getMediaPipeline(), userContext.getE(userName).getSession());
            oneToOneScope.setCallingTo(callingTo);
            oneToOneScope.setCallingFrom(callingFrom);
            callingFrom.connect(callingTo);
            callingTo.connect(callingFrom);
            // 响应调用者 sdp answer
            String fromSdpAnswer = callingFrom.processOffer(userData.getSdpOffer());
            UserMessage fromUserMsg = new UserMessage();
            fromUserMsg.setId("sdpAnswer");
            fromUserMsg.setContent(fromSdpAnswer);
            applicationContext.publishEvent(new MsgSendEvent(userContext.getE(userData.getUserName()).getSession(), fromUserMsg));
            // 响应被调用者 sdp answer
            String toSdpAnswer = callingTo.processOffer(sdpOffer);
            UserMessage toUserMsg = new UserMessage();
            toUserMsg.setId("sdpAnswer");
            toUserMsg.setContent(toSdpAnswer);
            applicationContext.publishEvent(new MsgSendEvent(userContext.getE(userName).getSession(), toUserMsg));
            callingFrom.gatherCandidates();
            callingTo.gatherCandidates();
            webRtcEndpointScope.putE(userData.getUserName(),callingFrom);
            webRtcEndpointScope.putE(userName,callingTo);
            webRtcEndpointScope.setIceCandidate(userData.getUserName());
            webRtcEndpointScope.setIceCandidate(userName);
        }
    }

    /**
     * 通知成员
     * @author duofei
     * @date 2019/8/22
     * @param scopeId
     * @param message
     */
    public void notifyMembers(String scopeId,AbstractMessage message){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToOneScope){
            OneToOneScope oneToOneScope = (OneToOneScope) scope;
            String callFrom = Optional.ofNullable(oneToOneScope.getCallingFrom()).map(WebRtcEndpoint::getName).orElse(null);
            if(callFrom != null && userContext.contains(callFrom)){
                applicationContext.publishEvent(new MsgSendEvent(userContext.getE(callFrom).getSession(),message));
            }
            String callTo = Optional.ofNullable(oneToOneScope.getCallingTo()).map(WebRtcEndpoint::getName).orElse(null);
            if(callTo != null && userContext.contains(callTo)){
                applicationContext.publishEvent(new MsgSendEvent(userContext.getE(callTo).getSession(),message));
            }
        }
    }

    /**
     * 通知成员
     * @author duofei
     * @date 2019/8/22
     * @param scopeId
     * @param message
     */
    public void notifyMembers(String scopeId,String excludeUserName,AbstractMessage message){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToOneScope){
            OneToOneScope oneToOneScope = (OneToOneScope) scope;
            String callFrom = Optional.ofNullable(oneToOneScope.getCallingFrom()).map(WebRtcEndpoint::getName).orElse(null);
            if(callFrom != null && !callFrom.equals(excludeUserName) && userContext.contains(callFrom)){
                applicationContext.publishEvent(new MsgSendEvent(userContext.getE(callFrom).getSession(),message));
            }
            String callTo = Optional.ofNullable(oneToOneScope.getCallingTo()).map(WebRtcEndpoint::getName).orElse(null);
            if(callTo != null && !callTo.equals(excludeUserName) && userContext.contains(callTo)){
                applicationContext.publishEvent(new MsgSendEvent(userContext.getE(callTo).getSession(),message));
            }
        }
    }

    /**
     * 资源销毁
     * @author duofei
     * @date 2019/8/22
     * @param scopeId
     */
    public void dispose(String scopeId){
        Scope scope = scopeContext.getE(scopeId);
        // 上下文资源处理
        if(scope instanceof OneToOneScope){
            OneToOneScope oneToOneScope = (OneToOneScope) scope;
            String callFrom = Optional.ofNullable(oneToOneScope.getCallingFrom()).map(WebRtcEndpoint::getName).orElse(null);
            if(callFrom != null){
                webRtcEndpointScope.removeE(callFrom);
            }
            String callTo = Optional.ofNullable(oneToOneScope.getCallingTo()).map(WebRtcEndpoint::getName).orElse(null);
            if(callTo != null){
                webRtcEndpointScope.removeE(callTo);
            }
        }
        scopeContext.removeE(scopeId);
        if(scope != null){
            // 域资源处理
            scope.dispose();
        }


    }
}
