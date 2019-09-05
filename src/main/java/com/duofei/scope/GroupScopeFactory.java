package com.duofei.scope;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.model.BaseMessage;
import com.duofei.message.model.SystemMessage;
import com.duofei.service.KurentoService;
import com.duofei.user.BaseUser;
import com.duofei.user.GroupUser;
import com.duofei.utils.IdGen;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * groupScope 管理工厂
 * @author duofei
 * @date 2019/8/29
 */
@Component
public class GroupScopeFactory {

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
     * 创建 groupScope
     * @author duofei
     * @date 2019/8/29
     * @param userName 创建者名称
     * @param meetRoomName 会议室名
     * @return 域id
     */
    public String create(String userName,String meetRoomName){
        String id = IdGen.newShortId();
        GroupScope groupScope = new GroupScope(id);
        groupScope.setName(meetRoomName);
        groupScope.setUserName(userName);
        groupScope.setMediaPipeline(kurentoService.createMediaPipeline());
        scopeContext.putE(id,groupScope);
        return id;
    }

    /**
     * 激活成员
     * @author duofei
     * @date 2019/8/30
     * @param scopeId
     * @param userName
     */
    public void active(String scopeId,String userName){
        Scope scope = scopeContext.getE(scopeId);
        if (scope instanceof GroupScope) {
            GroupScope groupScope = (GroupScope) scope;
            // 创建mediaPipeline
            if(groupScope.getMediaPipeline() == null){
                synchronized (groupScope.getId().intern()){
                    groupScope.setMediaPipeline(kurentoService.createMediaPipeline());
                }
            }
            BaseUser baseUser = userContext.getE(userName);
            if(baseUser != null){
                WebRtcEndpoint webRtcEndpoint = webRtcEndpointContext.getE(userName);
                GroupUser groupUser = new GroupUser(userName,baseUser.getSession(), webRtcEndpoint, scopeId);
                groupUser.setMediaPipeline(groupScope.getMediaPipeline());
                groupUser.setScopeId(scopeId);
                userContext.putE(userName, groupUser);
                // 检验当前域是否有其它成员，存在的话，发送消息通知当前成员，要求其建立对应数量的 webrtcRecvOnly 对象，并且通知已经存在的成员有新的成员加入
                List<GroupUser> allGroupUsers = groupScope.getAllGroupUsers();
                if(allGroupUsers.size() != 0){
                    SystemMessage systemMessage = new SystemMessage();
                    systemMessage.setId("existMembers");
                    systemMessage.setContent(allGroupUsers.stream().map(GroupUser::getUserName).collect(Collectors.toSet()));
                    applicationContext.publishEvent(new MsgSendEvent(groupUser.getSession(),systemMessage));
                    SystemMessage sm = new SystemMessage();
                    sm.setId("newMemberJoin");
                    sm.setContent(groupUser.getUserName());
                    allGroupUsers.forEach(gu->
                        applicationContext.publishEvent(new MsgSendEvent(gu.getSession(),sm))
                    );
                }
                // 加入当前域
                groupScope.addMember(groupUser);
            }

        }
    }

    /**
     * 请求接收指定成员的流媒体数据
     * @author duofei
     * @date 2019/8/30
     * @param userName 当前用户
     * @param specialMemberUserName 指定成员
     */
    public void recvMemberMedia(String userName, String specialMemberUserName){
        BaseUser currentUser = userContext.getE(userName);
        BaseUser specialUser = userContext.getE(specialMemberUserName);
        if(currentUser instanceof GroupUser && specialUser instanceof GroupUser){
            GroupUser currentGroupUser = (GroupUser) currentUser;
            GroupUser specialGroupUser = (GroupUser) specialUser;
            WebRtcEndpoint webRtcEndpoint = webRtcEndpointContext.getE(userName + specialMemberUserName);
            currentGroupUser.addIncomingMedia(userName+specialMemberUserName,webRtcEndpoint);
            // 使用指定成员的输出流，连接该输入流
            specialGroupUser.getOutgoingMedia().connect(webRtcEndpoint);
        }
    }

    /**
     * 退出会议室
     * @author duofei
     * @date 2019/9/2
     * @param scopeId
     * @param userName
     */
    public void quitMeetRoom(String scopeId, String userName){
        // 清理域
        Scope scope = scopeContext.getE(scopeId);
        BaseUser baseUser = userContext.getE(userName);
        if(scope instanceof GroupScope && baseUser instanceof GroupUser){
            GroupScope groupScope = (GroupScope) scope;
            GroupUser user = (GroupUser) baseUser;
            user.getOutgoingMedia().release();
            user.getIncomingMedia().forEach((name,webRtcEndpoint)->{
                webRtcEndpoint.release();
                webRtcEndpointContext.removeE(name);
            });
            groupScope.removeGroupUser(user);
            // 清理该用户（向上转型为普通用户为普通用户）
            userContext.putE(userName,user.upgrade());
        }
        // 上下文清理
        webRtcEndpointContext.removeE(userName);
    }

    /**
     * 关闭会议室
     * @author duofei
     * @date 2019/9/2
     * @param scopeId
     * @param userName
     */
    public void closeMeetRoom(String scopeId, String userName){
        Scope scope = scopeContext.getE(scopeId);
        BaseUser baseUser = userContext.getE(userName);
        if(scope instanceof GroupScope && baseUser instanceof GroupUser){
            GroupScope groupScope = (GroupScope) scope;
            GroupUser user = (GroupUser) baseUser;
            // 清理域
            groupScope.dispose();
            // 清理域中所有用户（降级为普通用户）
            groupScope.getAllGroupUsers().forEach(groupUser->{
                userContext.putE(groupUser.getUserName(),groupUser.upgrade());
                webRtcEndpointContext.removeE(groupUser.getUserName());
                user.getIncomingMedia().forEach((name,webRtcEndpoint)->{
                    webRtcEndpoint.release();
                    webRtcEndpointContext.removeE(name);
                });
            });
            groupScope.getAllGroupUsers().clear();
            groupScope.setMediaPipeline(null);
        }
    }

    /**
     * 通知消息
     * @author duofei
     * @date 2019/9/2
     * @param scopeId
     * @param excludeUsername 被排除的用户
     * @param baseMessage
     */
    public void notifyMembers(String scopeId, String excludeUsername, BaseMessage baseMessage){
        Scope scope = scopeContext.getE(scopeId);
        if(scope instanceof GroupScope){
            GroupScope groupScope = (GroupScope) scope;
            List<GroupUser> allGroupUsers = groupScope.getAllGroupUsers();
            allGroupUsers.stream().filter(groupUser ->{
                if(groupUser.getUserName().equals(excludeUsername)){
                    return false;
                }
                return true;
            }).forEach(groupUser -> {
                if(groupUser.getSession() != null){
                    applicationContext.publishEvent(new MsgSendEvent(groupUser.getSession(), baseMessage));
                }
            });
        }
    }
}
