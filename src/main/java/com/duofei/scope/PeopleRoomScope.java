package com.duofei.scope;

import com.duofei.context.WebRtcEndpointContext;
import org.kurento.client.Composite;
import org.kurento.client.HubPort;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多人聊天域
 * @author duofei
 * @date 2019/9/19
 */
public class PeopleRoomScope extends BaseScope {

    /**
     * 成员名称 -》状态 （0 存在当前域，1 已加入，2 已激活 3 已拒绝）
     */
    private Map<String,Integer> members;
    /**
     * 集线器: 将多个视频流汇成网格
     */
    private Composite composite;
    /**
     * 媒体元素指定与集线器的连接
     */
    private HubPort hubPort;

    public PeopleRoomScope(String id, MediaPipeline mediaPipeline) {
        super(id, mediaPipeline);
        this.composite = new Composite.Builder(mediaPipeline).build();
        this.hubPort = new HubPort.Builder(this.composite).build();
        this.members = new ConcurrentHashMap<>(8);
    }

    /**
     * 为当前域新增成员
     * @author duofei
     * @date 2019/8/30
     * @param userName
     */
    public void addMember(String userName){
        this.members.put(userName,0);
    }

    /**
     * 获取当前域所有成员
     * @author duofei
     * @date 2019/8/30
     * @return Map<String,Integer> 所有成员
     */
    public Map<String,Integer> getMembers(){
        return this.members;
    }

    /**
     * 移除成员
     * @author duofei
     * @date 2019/9/2
     * @param userName
     */
    public void removeMember(String userName){
        this.members.remove(userName);
    }

    /**
     * 激活成员
     * @author duofei
     * @date 2019/9/19
     * @param userName 用户名
     * @param webRtcEndpoint
     */
    public void activeMember(String userName, WebRtcEndpoint webRtcEndpoint){
        if(members.containsKey(userName)){
            members.put(userName, 2);
            HubPort outHubPort = new HubPort.Builder(composite).build();
            webRtcEndpoint.connect(outHubPort);
            // 集线器输出到自己
            this.hubPort.connect(webRtcEndpoint);
        }
    }

    /**
     * 加入当前域
     * @author duofei
     * @date 2019/9/20
     * @param userName 用户名
     */
    public void join(String userName){
        if(members.containsKey(userName)){
            members.put(userName, 1);
        }
    }

    /**
     * 处在当前域中的成员拒绝邀请
     * @author duofei
     * @date 2019/9/20
     * @param userName
     */
    public void refuse(String userName){
        if(members.containsKey(userName)){
            members.put(userName, 3);
        }
    }

    /**
     * 成员退出
     * @author duofei
     * @date 2019/9/19
     * @param userName
     */
    public void uselessMember(String userName, WebRtcEndpoint webRtcEndpoint){
        if(members.containsKey(userName)){
            members.put(userName, 0);
            this.hubPort.disconnect(webRtcEndpoint);
            webRtcEndpoint.release();
        }
    }
}
