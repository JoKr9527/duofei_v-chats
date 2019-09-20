package com.duofei.scope.factory;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.model.BaseMessage;
import com.duofei.scope.OneToOneScope;
import com.duofei.scope.Scope;
import com.duofei.service.KurentoService;
import com.duofei.user.BaseUser;
import com.duofei.utils.IdGen;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    private WebRtcEndpointContext webRtcEndpointContext;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 创建一个OneToOne域，存入域上下文
     * @author duofei
     * @date 2019/8/21
     * @param userName
     * @return String 域的唯一id
     * @throws
     */
    public OneToOneScope create(String userName){
        String id = IdGen.newShortId();
        OneToOneScope scope = new OneToOneScope(id);
        scope.setUserName(userName);
        scope.setMediaPipeline(kurentoService.createMediaPipeline());
        scopeContext.putE(id,scope);
        return scope;
    }

    /**
     * 通知成员
     * @author duofei
     * @date 2019/8/22
     * @param scopeId
     * @param message
     */
    public void notifyMembers(String scopeId, BaseMessage message){
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
    public void notifyMembers(String scopeId, String excludeUserName, BaseMessage message){
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
                webRtcEndpointContext.removeE(callFrom);
            }
            String callTo = Optional.ofNullable(oneToOneScope.getCallingTo()).map(WebRtcEndpoint::getName).orElse(null);
            if(callTo != null){
                webRtcEndpointContext.removeE(callTo);
            }
            // 重置用户域id
            BaseUser fromUser = userContext.getE(oneToOneScope.getCallingFromUserName());
            if(fromUser != null){
                fromUser.setScopeId(null);
            }
            BaseUser toUser = userContext.getE(oneToOneScope.getCallingToUserName());
            if(toUser != null){
                toUser.setScopeId(null);
            }
        }
        scopeContext.removeE(scopeId);
        if(scope != null){
            // 域资源处理
            scope.dispose();
        }
    }

    /**
     * 激活域
     * @author duofei
     * @date 2019/9/4
     * @param scopeId 域id
     * @param userName 用户名
     * @param name webRtc 名称
     */
    public void active(String scopeId, String userName, String name){
        Scope scope = scopeContext.getE(scopeId);
        synchronized (scopeId.intern()){
            if(scope != null && scope instanceof OneToOneScope){
                OneToOneScope oneToOneScope = ((OneToOneScope) scope);
                WebRtcEndpoint webRtcEndpoint = webRtcEndpointContext.getE(name);
                if(userName.equals(oneToOneScope.getUserName())){
                    oneToOneScope.setCallingFrom(webRtcEndpoint);
                    if(oneToOneScope.getCallingTo() != null){
                        oneToOneScope.connect();
                    }
                }else {
                    oneToOneScope.setCallingTo(webRtcEndpoint);
                    if(oneToOneScope.getCallingFrom() != null){
                        oneToOneScope.connect();
                    }
                }
                userContext.getE(userName).setScopeId(scopeId);
            }
        }
    }
}
