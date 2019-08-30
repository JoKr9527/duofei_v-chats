package com.duofei.scope;

import com.duofei.user.GroupUser;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 群组域
 * @author duofei
 * @date 2019/8/16
 */
public class GroupScope extends BaseScope {

    /**
     * 成员
     */
    private List<GroupUser> groupUsers;


    public GroupScope(String id, MediaPipeline mediaPipeline){
        super(id,mediaPipeline);
        groupUsers = new CopyOnWriteArrayList<>();
    }

    /**
     * 为当前域新增成员
     * @author duofei
     * @date 2019/8/30
     * @param groupUser
     */
    public void addMember(GroupUser groupUser){
        this.groupUsers.add(groupUser);
    }

    /**
     * 获取当前域所有成员
     * @author duofei
     * @date 2019/8/30
     * @return List<GroupUser> 所有成员
     */
    public List<GroupUser> getAllGroupUsers(){
        return this.groupUsers;
    }
}
