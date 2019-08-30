package com.duofei.utils;

import java.util.UUID;

/**
 * id 生成器
 * @author duofei
 * @date 2019/8/21
 */
public class IdGen {
    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static String newShortId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 为账号创建salt
     *
     * @return String
     */
    public static String createSalt() {
        String id = newShortId();
        return id.substring(0, 8);

    }
}
