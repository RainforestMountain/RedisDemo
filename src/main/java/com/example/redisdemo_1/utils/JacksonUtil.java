package com.example.redisdemo_1.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JacksonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将普通对象序列化为 {"value": 对象值} 格式的JSON
     */
    public static String serializeObject(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 将列表序列化为 {"value": 列表值} 格式的JSON
     */
    public static <T> String serializeList(List<T> list) throws JsonProcessingException {
        return objectMapper.writeValueAsString(list);
    }

    /**
     * 将Map序列化为 {"value": Map值} 格式的JSON
     */
    public static <K, V> String serializeMap(Map<K, V> map) throws JsonProcessingException {
        return objectMapper.writeValueAsString(map);
    }

    // ==================================新增反序列化方法==============================

    // 反序列化普通对象 - 关键修复
    public static <T> T deserializeObject(String json, Class<T> clazz) throws JsonProcessingException {
        // 使用convertValue方法进行类型转换
        return objectMapper.readValue(json, clazz);
    }

    /**
     * 将JSON反序列化为List
     */
    public static <T> List<T> deserializeList(String json, TypeReference<List<T>> typeRef) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<List<T>>() {
        });
    }

    /**
     * 将JSON反序列化为Map
     */
    public static <K, V> Map<K, V> deserializeMap(String json, TypeReference<Map<K, V>> typeRef) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<K, V>>() {
        });
    }
}
