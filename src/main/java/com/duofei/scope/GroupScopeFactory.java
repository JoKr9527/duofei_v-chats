package com.duofei.scope;

import com.duofei.context.ScopeContext;
import com.duofei.context.UserContext;
import com.duofei.context.WebRtcEndpointContext;
import com.duofei.event.MsgSendEvent;
import com.duofei.message.SystemMessage;
import com.duofei.service.KurentoService;
import com.duofei.user.BaseUser;
import com.duofei.user.GroupUser;
import com.duofei.utils.IdGen;
import org.kurento.client.MediaPipeline;
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
     * @param meetRoomName 会议室名
     * @param userName 创建者名称
     * @return 域id
     */
    public String create(String meetRoomName,String userName){
        String id = IdGen.newShortId();
        GroupScope groupScope = new GroupScope(id, kurentoService.createMediaPipeline());
        groupScope.setName(meetRoomName);
        groupScope.setUserName(userName);
        scopeContext.putE(id,groupScope);
        return id;
    }

    /**
     * 激活成员
     * @author duofei
     * @date 2019/8/30
     * @param scopeId
     * @param userName
     * @param sdpOffer
     */
    public void activeMember(String scopeId,String userName, String sdpOffer){
        Scope scope = scopeContext.getE(scopeId);
        if (scope instanceof GroupScope) {
            GroupScope groupScope = (GroupScope) scope;
            BaseUser baseUser = userContext.getE(userName);
            if(baseUser != null){
                //创建 webrtc 输出流，将baseUser实例转换为groupuser
                MediaPipeline mediaPipeline = kurentoService.createMediaPipeline();
                WebRtcEndpoint webRtcEndpoint = kurentoService.createWebRtcEndpoint(mediaPipeline, baseUser.getSession());
                webRtcEndpoint.processOffer(sdpOffer);
                webRtcEndpoint.gatherCandidates();
                webRtcEndpointContext.setIceCandidate(userName);
                GroupUser groupUser = new GroupUser(userName,baseUser.getSession(), webRtcEndpoint, scopeId);
                groupUser.setMediaPipeline(mediaPipeline);
                userContext.putE(userName, groupUser);
                webRtcEndpointContext.putE(userName,webRtcEndpoint);
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
     * @param sdpOffer
     */
    public void recvMemberMedia(String userName, String specialMemberUserName, String sdpOffer){
        BaseUser currentUser = userContext.getE(userName);
        BaseUser specialUser = userContext.getE(specialMemberUserName);
        if(currentUser instanceof GroupUser && specialUser instanceof GroupUser){
            GroupUser currentGroupUser = (GroupUser) currentUser;
            GroupUser specialGroupUser = (GroupUser) specialUser;
            // 创建对应的webrtc对象(名称为当前用户名+指定用户名)，设置为当前用户的输入流媒体
            WebRtcEndpoint webRtcEndpoint = kurentoService.createWebRtcEndpoint(currentGroupUser.getMediaPipeline(),
                    currentGroupUser.getSession(), userName+specialMemberUserName);
            webRtcEndpoint.processOffer(sdpOffer);
            webRtcEndpoint.gatherCandidates();
            webRtcEndpointContext.setIceCandidate(userName);
            currentGroupUser.addIncomingMedia(userName+specialMemberUserName,webRtcEndpoint);
            // 使用指定成员的输出流，连接该输入流
            specialGroupUser.getOutgoingMedia().connect(webRtcEndpoint);
        }

    }
}
