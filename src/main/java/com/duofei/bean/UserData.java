package com.duofei.bean;

/**
 * 用户建立域需要的数据
 * @author duofei
 * @date 2019/8/21
 */
public class UserData {

    private String userName;

    private String sdpOffer;

    public UserData(String userName,String sdpOffer){
        this.userName = userName;
        this.sdpOffer = sdpOffer;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSdpOffer() {
        return sdpOffer;
    }

    public void setSdpOffer(String sdpOffer) {
        this.sdpOffer = sdpOffer;
    }
}
