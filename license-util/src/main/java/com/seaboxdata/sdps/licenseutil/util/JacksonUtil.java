package com.seaboxdata.sdps.licenseutil.util;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 *  JSON工具类
 * @author lituan
 */
@Slf4j
public class JacksonUtil{

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转json字符串
     * @param obj
     * @return
     * @throws Exception
     */
    public static String objToJson(Object obj) {
       String json = "";
        try{
            json = mapper.writeValueAsString(obj);
        }catch (JsonProcessingException e){
            log.error("对象转json字符串出错",e);
        }
        return json;
    }

    /**
     * JSON字符串转对象
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static  <T> T jsonToObj(String jsonStr,Class<T> clazz)  {
        T t = null;
        try{
             t = mapper.readValue(jsonStr, clazz);
        }catch (Exception e){
            log.error("JSON字符串转对象出错",e);
        }
        return  t;
    }

    /**
     * JSON字符串转对象
     * @param jsonStr
     * @param valueTypeRef
     * @param <T>
     * @return
     * @throws Exception
     */
    public static  <T> T jsonToObjType(String jsonStr, TypeReference<T> valueTypeRef){
        T t = null;
        try{
            t = mapper.readValue(jsonStr, valueTypeRef);
        }catch (Exception e){
            log.error("JSON字符串转对象出错",e);
        }
        return  t;
    }

}
