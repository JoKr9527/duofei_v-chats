package com.duofei.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * json 工具类
 * @author duofei
 * @date 2019/8/20
 */
public class JsonUtils {

    private static final Gson gson = new GsonBuilder().create();

    /**
     * 对象 -》 json 字符串
     * @author duofei
     * @date 2019/8/20
     * @param ob
     * @return String
     */
    public static String toJSON(Object ob){
        return gson.toJson(ob);
    }

    /**
     * json -> JsonObject
     * @author duofei
     * @date 2019/8/20
     * @param json
     * @return JsonObject
     */
    public static JsonObject fromJson(String json){
        return gson.fromJson(json, JsonObject.class);
    }

    /**
     * json -> 具体对象
     * @author duofei
     * @date 2019/8/20
     * @param json
     * @param clzz
     * @return T
     */
    public static <T> T fromJson(String json,Class<T> clzz){
        return gson.fromJson(json, clzz);
    }

}
