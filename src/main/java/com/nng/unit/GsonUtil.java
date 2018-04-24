package com.nng.unit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * 使用gson来操作Json
 * 1.对象实体类，list,map,等转化成----->jsonString(进行数据传递)
 * 2.jsonString转化成----->对象实体类，list,map(解析返回的数据)
 * 3.我使用的是gson-2.2.2.jar
 *
 */

public class GsonUtil {


    private static Gson gson = null;
    static {
        if (gson == null) {
            gson = new Gson();
        }
    }


    /**
     * 将object对象转成json字符串(jsonString)
     * boject 可以是List，也可以是Bean(对象类型)
     * @param object
     */
    public static String GsonString(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }


    /**入参是json对象
     * 将gsonObjectString转成泛型bean(实体类)
     * @param gsonString
     */
    public static <T> T GsonToBean(String gsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }

    /**
     * 这里的入参是json数组类型的字符串
     * jsonArrayString转成list
     */
    public static <T> List<T> jsonArrayToList(String json, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for(final JsonElement elem : array){
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }



    /**
     * List对泛型没有要求
     * jsonString转成list中有map的
     */
    public static <T> List<Map<String, T>> GsonToListMap(String gsonString) {
        List list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List>() {
                    }.getType());
        }
        return list;
    }

    /**
     * 要求List中全部是Map
     * jsonString转成list中有map的
     */
    public static <T> List<Map<String, T>> GsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }


    /**
     * jsonString转成map的
     */
    public static <T> Map<String, T> GsonToMaps(String gsonString) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }

    /**
     * map转jsonString,指定了Map类型
     */
    public static String MapToJson(Map<String,Object> map){
        String str = null;
        if(gson != null){
            str = gson.toJson(map);
        }
        return str;
    }
}
