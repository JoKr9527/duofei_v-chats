package com.duofei.context;

import java.util.Set;

/**
 * 基础上下文
 * @author duofei
 * @date 2019/8/19
 */
public interface Context<T> {

    /**
     * 放入元素
     * @author duofei
     * @date 2019/8/19
     * @param key 映射key
     * @param t 元素
     */
    void putE(String key,T t);

    /**
     * 移除元素
     * @author duofei
     * @date 2019/8/19
     * @param key 映射key
     */
    void removeE(String key);

    /**
     * 元素是否存在
     * @author duofei
     * @date 2019/8/19
     * @param key
     * @return true 存在
     */
    boolean contains(String key);

    /**
     * 元素数量
     * @author duofei
     * @date 2019/8/19
     * @return int  数量
     */
    int eSize();

    /**
     * 上下文持有的元素 key
     * @author duofei
     * @date 2019/8/19
     * @return Set<String> 持有的元素
     */
    Set<String> holds();

    /**
     * 根据key,获取持有的元素
     * @author duofei
     * @date 2019/8/20
     * @param key
     * @return T
     */
    T getE(String key);

    /**
     * 持有的所有元素
     * @author duofei
     * @date 2019/8/20
     * @return Set<T>
     */
    Set<T> holdE();

}
