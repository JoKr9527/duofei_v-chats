package com.duofei.message.dispatcher;

import com.duofei.message.MsgHandler;
import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.message.model.IceCandidateUserMessage;
import com.duofei.message.MsgHandle;
import com.duofei.message.model.UserMessage;
import com.duofei.scope.BaseScope;
import com.duofei.scope.factory.OneToManyScopeFactory;
import com.duofei.scope.Scope;
import com.duofei.service.KurentoService;
import com.duofei.user.BaseUser;
import com.duofei.utils.JsonUtils;
import com.google.gson.JsonObject;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * sdp 协商过程消息处理
 * @author duofei
 * @date 2019/9/3
 */
@Component
public class SdpMsgDispatcher implements MsgDispatcher {

    private static Logger logger = LoggerFactory.getLogger(OneToManyScopeFactory.class);

    @Autowired
    private KurentoService kurentoService;
    @Autowired
    private ScopeContext scopeContext;
    @Autowired
    private UserContext userContext;
    @Autowired
    private WebRtcEndpointContext webRtcEndpointContext;

    private static Map<String, MsgHandle> msgHandles = new HashMap<>();

    @Override
    public Map<String, MsgHandle> msgHandles() {
        return msgHandles;
    }

    @Override
    public void dispatch(String msg) throws Exception {
        JsonObject jsonObject = JsonUtils.fromJson(msg);
        // 根据消息类型转换为具体对象
        final String id = jsonObject.get("id").getAsString();
        if("onIceCandidate".equals(id)){
            msgHandles().get(id).handle(JsonUtils.fromJson(msg, IceCandidateUserMessage.class));
        }else{
            msgHandles().get(id).handle(JsonUtils.fromJson(msg, UserMessage.class));
        }
    }

    /**
     * 处理sdpOffer
     * @author duofei
     * @date 2019/9/3
     */
    @MsgHandler
    private MsgHandle sdpOffer(){
        MsgHandle<UserMessage> result = (UserMessage userMessage) -> {
            Scope scope = scopeContext.getE(userMessage.getTo());
            BaseUser baseUser = userContext.getE(userMessage.getFrom());
            MediaPipeline mediaPipeline = null;
            if(scope != null && scope instanceof BaseScope){
                BaseScope baseScope = (BaseScope) scope;
                mediaPipeline = baseScope.getMediaPipeline();
            }else{
                mediaPipeline = kurentoService.createMediaPipeline();
            }
            WebRtcEndpoint webRtcEndpoint = kurentoService.createWebRtcEndpoint(mediaPipeline, baseUser.getSession(), userMessage.getOther());
            webRtcEndpointContext.setSdpOffer(userMessage.getOther(), ((String) userMessage.getContent()));
            webRtcEndpointContext.putE(userMessage.getOther(),webRtcEndpoint);
            webRtcEndpointContext.activeWebRtc(userMessage.getOther(),baseUser.getSession());
        };
        return result;
    }

    /**
     * 处理 iceCandidate消息
     * @author duofei
     * @date 2019/9/4
     * @param
     * @return
     * @throws
     */
    @MsgHandler
    private MsgHandle onIceCandidate(){
        MsgHandle<IceCandidateUserMessage> result = (IceCandidateUserMessage iceCandidateUserMessage) -> webRtcEndpointContext.addIceCandidate(iceCandidateUserMessage.getFrom(),iceCandidateUserMessage.getContent());
        return result;
    }
}
