package com.duofei.scope;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.BroadcastRoomEvent;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.AbstractMessage;
import com.duofei.message.UserMessage;
import com.duofei.service.KurentoService;
import com.duofei.user.BaseUser;
import com.duofei.utils.IdGen;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * oneToMany 管理工厂
 * @author duofei
 * @date 2019/8/27
 */
@Component
public class OneToManyScopeFactory {

    private static Logger logger = LoggerFactory.getLogger(OneToManyScopeFactory.class);

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
     * 创建成功的域id
     * @author duofei
     * @date 2019/8/27
     * @param userName
     * @param scopeName
     * @return String
     */
    public String create(String userName, String scopeName){
        String id = IdGen.newShortId();
        OneToManyScope scope = new OneToManyScope(id,scopeName);
        scope.setUserName(userName);
        scopeContext.putE(id,scope);
        return id;
    }

    /**
     * 激活域的主持人
     * @author duofei
     * @date 2019/8/27
     * @param scopeId 域id
     * @param sdpOffer
     */
    public void activePresenter(String scopeId,String sdpOffer){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = ((OneToManyScope) scope);
            BaseUser presenterUser = userContext.getE(oneToManyScope.getUserName());
            if(presenterUser != null){
                MediaPipeline mediaPipeline = kurentoService.createMediaPipeline();
                oneToManyScope.setMediaPipeline(mediaPipeline);
                WebRtcEndpoint webRtcEndpoint = kurentoService.createWebRtcEndpoint(mediaPipeline, presenterUser.getSession());
                String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);
                webRtcEndpoint.gatherCandidates();
                oneToManyScope.setPresenter(webRtcEndpoint);
                UserMessage userMessage = new UserMessage();
                userMessage.setId("sdpAnswer");
                userMessage.setContent(sdpAnswer);
                applicationContext.publishEvent(new MsgSendEvent(presenterUser.getSession(), userMessage));
                webRtcEndpointScope.putE(oneToManyScope.getUserName(),webRtcEndpoint);
                webRtcEndpointScope.setIceCandidate(oneToManyScope.getUserName());
            }
        }
    }

    /**
     * 激活域的参观者
     * @author duofei
     * @date 2019/8/27
     * @param scopeId
     * @param userName
     * @param sdpOffer
     */
    public void activeViewer(String scopeId,String userName,String sdpOffer){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = ((OneToManyScope) scope);
            BaseUser viewer = userContext.getE(userName);
            if(viewer != null){
                MediaPipeline mediaPipeline = oneToManyScope.getMediaPipeline();
                WebRtcEndpoint webRtcEndpoint = kurentoService.createWebRtcEndpoint(mediaPipeline, viewer.getSession());
                String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);
                webRtcEndpoint.gatherCandidates();
                oneToManyScope.addViewer(userName,webRtcEndpoint);
                UserMessage userMessage = new UserMessage();
                userMessage.setId("sdpAnswer");
                userMessage.setContent(sdpAnswer);
                applicationContext.publishEvent(new MsgSendEvent(viewer.getSession(), userMessage));
                webRtcEndpointScope.putE(userName,webRtcEndpoint);
                webRtcEndpointScope.setIceCandidate(userName);
            }
        }
    }

    /**
     * 通知一对多聊天室内参观者，不包括excludeUserName
     * @author duofei
     * @date 2019/8/28
     * @param scopeId 域id
     * @param excludeUserName 排除的参观者名称
     * @param message
     */
    public void notifyMembers(String scopeId, String excludeUserName, AbstractMessage message){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = (OneToManyScope) scope;
            oneToManyScope.getViewers().forEach(userName->{
                if(!userName.equals(excludeUserName)){
                    BaseUser baseUser = userContext.getE(userName);
                    if(baseUser != null){
                        applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),message));
                    }
                }
            });
        }
    }

    /**
     * 通知主持人
     * @author duofei
     * @date 2019/8/28
     * @param scopeId 域id
     */
    public void notifyPresenter(String scopeId, AbstractMessage message){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = (OneToManyScope) scope;
            String name = oneToManyScope.getPresenter().getName();
            if(name != null){
                BaseUser baseUser = userContext.getE(name);
                if(baseUser != null){
                    applicationContext.publishEvent(new MsgSendEvent(baseUser.getSession(),message));
                }
            }
        }
    }

    /**
     * 直播间资源销毁
     * @author duofei
     * @date 2019/8/28
     * @param scopeId
     */
    public void dispose(String scopeId){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = (OneToManyScope) scope;
            // 上下文资源清理
            webRtcEndpointScope.removeE(oneToManyScope.getPresenter().getName());
            oneToManyScope.getViewers().forEach(webRtcEndpointScope::removeE);
        }
        scopeContext.removeE(scopeId);
        if(scope != null){
            scope.dispose();
        }
    }
}
