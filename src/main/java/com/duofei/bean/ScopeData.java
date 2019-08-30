package com.duofei.bean;

/**
 * 域描述信息
 * @author duofei
 * @date 2019/8/27
 */
public class ScopeData {

    /**
     * 域id
     */
    private String id;
    /**
     * 域名称
     */
    private String name;
    /**
     * 域发起者
     */
    private String userName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
