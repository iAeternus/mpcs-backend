package com.ricky.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricky.common.exception.MyException;

import java.io.InputStream;
import java.io.Writer;

/**
 * @author Ricky
 * @version 1.0
 * @date 2026/1/4
 * @className JsonCodec
 * @desc 基于 ObjectMapper 的 JSON 编解码工具类，封装异常处理逻辑
 */
public class JsonCodec {

    private final ObjectMapper objectMapper;

    public JsonCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param value 要序列化的对象
     * @return JSON 字符串
     */
    public String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象写入指定的 Writer 中（JSON 格式）
     */
    public void writeValue(Writer writer, Object value) {
        try {
            objectMapper.writeValue(writer, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 JSON 字符串反序列化对象
     */
    public <T> T readValue(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 JSON 字符串反序列化复杂泛型对象
     */
    public <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        try {
            return objectMapper.readValue(content, valueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从输入流反序列化对象
     */
    public <T> T readValue(InputStream src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取 JSON 树结构
     */
    public JsonNode readTree(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public <T> T convertValue(Object fromValue, Class<T> toValueType) {
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        try {
            return objectMapper.convertValue(fromValue, toValueTypeRef);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
