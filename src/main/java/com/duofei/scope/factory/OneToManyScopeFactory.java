package com.duofei.scope.factory;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.model.BaseMessage;
import com.duofei.scope.OneToManyScope;
import com.duofei.scope.Scope;
import com.duofei.service.KurentoService;
import com.duofei.user.BaseUser;
import com.duofei.utils.IdGen;
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
    private WebRtcEndpointContext webRtcEndpointContext;
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
        scope.setMediaPipeline(kurentoService.createMediaPipeline());
        scope.setUserName(userName);
        scopeContext.putE(id,scope);
        return id;
    }

    /**
     * 激活域
     * @author duofei
     * @date 2019/9/4
     * @param scopeId 域id
     * @param username 用户名
     */
    public void active(String scopeId, String username){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = ((OneToManyScope) scope);
            if(username.equals(oneToManyScope.getUserName())){
                oneToManyScope.setPresenter(webRtcEndpointContext.getE(username));
                oneToManyScope.setPresenterUserName(username);
            }else{
                oneToManyScope.addViewer(username, webRtcEndpointContext.getE(username));
            }
            BaseUser user = userContext.getE(username);
            if(user != null){
                user.setScopeId(scopeId);
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
    public void notifyMembers(String scopeId, String excludeUserName, BaseMessage message){
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
    public void notifyPresenter(String scopeId, BaseMessage message){
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
     * 移除成员
     * @author duofei
     * @date 2019/9/4
     * @param scopeId
     * @param username
     */
    public void removeMember(String scopeId,String username){
        Scope scope = scopeContext.getE(scopeId);
        if(scope != null && scope instanceof OneToManyScope){
            OneToManyScope oneToManyScope = (OneToManyScope) scope;
            oneToManyScope.removeViewer(username);
            BaseUser baseUser = userContext.getE(username);
            if(baseUser != null){
                baseUser.setScopeId(null);
            }
        }
        webRtcEndpointContext.removeE(username);
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
            webRtcEndpointContext.removeE(oneToManyScope.getPresenter().getName());
            oneToManyScope.getViewers().forEach(username -> {
                webRtcEndpointContext.removeE(username);
                BaseUser baseUser = userContext.getE(username);
                if(baseUser != null){
                    baseUser.setScopeId(null);
                }
            });
        }
        scopeContext.removeE(scopeId);
        if(scope != null){
            scope.dispose();
        }
    }
}
