package com.duofei.scope.factory;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.model.BaseMessage;
import com.duofei.message.model.SystemMessage;
import com.duofei.scope.PeopleRoomScope;
import com.duofei.scope.Scope;
import com.duofei.service.KurentoService;
import com.duofei.utils.IdGen;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
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
     * 退出多人聊天室
     * @author duofei
     * @date 2019/9/20
     * @param scopeId 域id
     * @param userName
     */
    public void uselessMember(String scopeId,String userName){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            // 清理webRtcEndpoint
            WebRtcEndpoint webRtcEndpoint = webRtcEndpointContext.getE(userName);
            peopleRoomScope.uselessMember(userName,webRtcEndpoint);
            webRtcEndpointContext.removeE(userName);

            Map<String, Integer> members = peopleRoomScope.getMembers();
            Integer size = members.values().stream().filter(status -> {
                if (status == 1 || status == 2) {
                    return true;
                }
                return false;
            }).collect(Collector.of(ArrayList::new, List::add, (c1, c2) -> {
                c1.addAll(c2);
                return c1;
            }, List::size));
            if(size <= 1){
                // 发送多人聊天室关闭请求
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setId("peopleRoomClosed");
                systemMessage.setContent(peopleRoomScope.getId());
                members.forEach((un,status) -> {
                    if(status == 1 || status == 2){
                        if(userContext.getE(un)!=null){
                            applicationContext.publishEvent(new MsgSendEvent(userContext.getE(un).getSession(),systemMessage));
                        }
                    }
                });
                // 发送移除聊天室
                SystemMessage removeOnlinePeopleRoomMsg = new SystemMessage();
                removeOnlinePeopleRoomMsg.setId("removeOnlinePeopleRoom");
                removeOnlinePeopleRoomMsg.setContent(peopleRoomScope.getId());
                members.forEach((un,status) ->
                    applicationContext.publishEvent(new MsgSendEvent(userContext.getE(un).getSession(),removeOnlinePeopleRoomMsg))
                );
                peopleRoomScope.dispose();
            }
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
     * 判断是否已经有成员加入 处于 1 或者 2状态 (排除创建者)
     * @author duofei
     * @date 2019/9/25
     * @param
     * @return boolean 有
     * @throws
     */
    public boolean isPeopelJoin(String scopeId){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof PeopleRoomScope){
            PeopleRoomScope peopleRoomScope = (PeopleRoomScope) scope;
            List<Integer> memberStatus = new ArrayList<>();
            peopleRoomScope.getMembers().forEach((user,status) -> {
                if(!user.equals(peopleRoomScope.getUserName())){
                    memberStatus.add(status);
                }
            });
            return memberStatus.stream().filter(status -> {
                if (status == 1 || status == 2 ) {
                    return true;
                }
                return false;
            }).collect(Collector.of(ArrayList::new, List::add, (c1, c2) -> {
                c1.addAll(c2);
                return c1;
            }, c1 -> {
                if (c1.size() > 0) {
                    return true;
                }
                return false;
            }));
        }
        return false;
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
                if(excludeUsername != null && member.equals(excludeUsername)){
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
