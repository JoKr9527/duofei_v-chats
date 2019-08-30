package com.duofei.context;

import com.duofei.scope.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 域上下文
 * @author duofei
 * @date 2019/8/19
 */
@Component
public class ScopeContext implements Context<Scope> {

    private Map<String,Scope> scopeMap = new ConcurrentHashMap<>();

    @Override
    public void putE(String key, Scope scope) {
        scopeMap.put(key, scope);
    }

    @Override
    public void removeE(String key) {
        scopeMap.remove(key);
    }

    @Override
    public boolean contains(String key) {
        return scopeMap.containsKey(key);
    }

    @Override
    public int eSize() {
        return scopeMap.size();
    }

    @Override
    public Set<String> holds() {
        return scopeMap.keySet();
    }

    @Override
    public Scope getE(String key) {
        return scopeMap.get(key);
    }

    @Override
    public Set<Scope> holdE() {
        Set<Scope> result = new HashSet<>();
        scopeMap.forEach((key,scope)->result.add(scope));
        return result;
    }
}
