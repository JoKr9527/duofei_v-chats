package com.duofei.context;

import com.duofei.user.BaseUser;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户上下文
 * @author duofei
 * @date 2019/8/19
 */
@Component("userContext")
public class UserContext implements Context<BaseUser> {

    private Map<String, BaseUser> userMap = new ConcurrentHashMap<>();

    @Override
    public void putE(String key, BaseUser baseUser) {
        userMap.put(key, baseUser);
    }

    @Override
    public void removeE(String key) {
        userMap.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return userMap.containsKey(key);
    }

    @Override
    public int eSize() {
        return userMap.size();
    }

    @Override
    public Set<String> holds() {
        return userMap.keySet();
    }

    @Override
    public BaseUser getE(String key) {
        if(key == null){
            return null;
        }
        return userMap.get(key);
    }

    @Override
    public Set<BaseUser> holdE() {
        Set<BaseUser> result = new HashSet<>();
        userMap.forEach((key,baseUser)->result.add(baseUser));
        return result;
    }
}
