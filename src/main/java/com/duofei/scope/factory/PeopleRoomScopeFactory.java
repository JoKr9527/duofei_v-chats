package com.duofei.scope.factory;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.model.BaseMessage;
import com.duofei.scope.PeopleRoomScope;
import com.duofei.scope.Scope;
import com.duofei.service.KurentoService;
import com.duofei.utils.IdGen;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 负责管理多人聊天域
 * @author duofei
 * @date 2019/9/19
 */
@Component
public class PeopleRoomScopeFactory {

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
     * 创建多人聊天域
     * @author duofei
     * @date 2019/9/19
     * @param userName 发起人
     * @param members 成员
     * @return String 域id
     */
    public String create(String userName, List<String> members){
        String id = IdGen.newShortId();
        PeopleRoomScope peopleRoomScope = new PeopleRoomScope(id,kurentoService.createMediaPipeline());
        peopleRoomScope.setId(id);
        peopleRoomScope.setUserName(userName);
        members.forEach(peopleRoomScope::addMember);
        peopleRoomScope.addMember(userName);
        peopleRoomScope.join(userName);
        scopeContext.putE(id,peopleRoomScope);
        return id;
    }

    /**
     * 激活
     * @author duofei
     * @date 2019/9/20
     * @param scopeId
     * @param userName
     */
    public synchronized void active(String scopeId,String userName){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            WebRtcEndpoint webRtcEndpoint = webRtcEndpointContext.getE(userName);
            if(webRtcEndpoint != null){
                peopleRoomScope.activeMember(userName,webRtcEndpoint);
            }
        }
    }

    /**
     * 加入多人聊天室
     * @author duofei
     * @date 2019/9/20
     * @param scopeId 域id
     * @param userName
     */
    public void join(String scopeId,String userName){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            peopleRoomScope.join(userName);
        }
    }

    /**
     * 拒绝加入多人聊天室
     * @author duofei
     * @date 2019/9/20
     * @param scopeId
     * @param userName
     */
    public void refuse(String scopeId, String userName){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            peopleRoomScope.refuse(userName);
        }
    }

    /**
     * 查询成员在域中的状态;
     * userName 为 null 时，查询发起人的状态
     * @author duofei
     * @date 2019/9/20
     * @param scopeId
     * @param userName
     * @return int -1 不存在 0 存在当前域，1 已加入，2 已激活
     */
    public int queryMemberStatus(String scopeId,String userName){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            if(userName != null){
                return peopleRoomScope.getMembers().get(userName);
            }else{
                return peopleRoomScope.getMembers().get(peopleRoomScope.getUserName());
            }
        }
        return -1;
    }

    /**
     * 根据指定状态，判断是否需要通知成员
     * @author duofei
     * @date 2019/9/20
     * @param scopeId
     * @param userName 为null时，代表发起人
     * @param status 状态
     * @param baseMessage
     */
    public synchronized void notifyMember(String scopeId, String userName, int status, BaseMessage baseMessage){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            if(userName != null){
                if(peopleRoomScope.getMembers().get(userName) == status){
                    if(userContext.getE(userName) != null){
                        applicationContext.publishEvent(new MsgSendEvent(userContext.getE(userName).getSession(), baseMessage));
                    }
                }
            }else{
                if(peopleRoomScope.getMembers().get(peopleRoomScope.getUserName()) == status){
                    if(userContext.getE(peopleRoomScope.getUserName()) != null){
                        applicationContext.publishEvent(new MsgSendEvent(userContext.getE(peopleRoomScope.getUserName()).getSession(), baseMessage));
                    }
                }
            }
        }
    }

    /**
     * 通知成员
     * @author duofei
     * @date 2019/9/19
     * @param scopeId
     * @param excludeUsername 被排除的用户
     * @param baseMessage
     */
    public void notifyMembers(String scopeId, String excludeUsername, BaseMessage baseMessage){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            Set<String> members = peopleRoomScope.getMembers().keySet();
            members.stream().filter(member ->{
                if(member.equals(excludeUsername)){
                    return false;
                }
                return true;
            }).forEach(member -> {
                if(userContext.getE(member) != null){
                    applicationContext.publishEvent(new MsgSendEvent(userContext.getE(member).getSession(), baseMessage));
                }
            });
        }
    }
}
