package com.duofei.constant;

/**
 * 消息 id 常量
 * @author duofei
 * @date 2019/8/19
 */
public enum MessageIdConstant {
    //一对一聊天消息说明
    REFUSE_CONNECT("refuseConnect","拒绝连接"),ONLINE_USERS("onlineUsers","在线用户列表请求"),REQ_VIDEO_CALL("reqVideoCall","视频通话请求"),REQ_VIDEO_REP_ACCEPT("reqVideoRep","视频通话请求响应接受"),
    REQ_VIDEO_REP_REFUSE("reqVideoRep","视频通话请求响应拒绝"),CALL("call","客户端本地已建立对等点,请求远程建立对等点"),ON_ICE_CANDIDATE("onIceCandidate","接收到的icecandidate消息"),
    ICE_CANDIDATE("iceCandidate","发送给客户端的iceCandidate消息"),CALLEE("callee","要求客户端远程建立对等点"),
    SDP_ANSWER("sdpAnswer","发送给客户端的sdp answer"),HANGUP("hangup","挂断消息通知"),
    JOIN_SCOPE("joinScope","通知客户端加入域的id"),
    // 直播间消息id说明
    CREATE_BROADCAST_ROOM("createBroadcastRoom","创建直播间请求"),CREATE_BROADCAST_ROOM_RESP("createBroadcastRoomResp","创建直播间请求的响应"),
    CLOSE_BROADCAST_ROOM("closeBroadcastRoom","关闭直播间"),
    // 会议室消息id说明
    EXIST_MEMBERS("existMembers","通知客户端已经存在的成员"),
    NEW_MEMBER_JOID("newMemberJoin", "通知客户端新的成员加入")
    ;

    /**
     * 值
     */
    private String v;
    private String desc;

    MessageIdConstant(String v,String desc){
        this.v = v;
        this.desc = desc;
    }

    public String getV() {
        return v;
    }

    public String getDesc() {
        return desc;
    }
}
