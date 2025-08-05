package com.zyb.apiClientSdk.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 响应解析器
 */
@Slf4j
public class ResponseParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析HTTP响应为ApiResponse（包含状态码检查）
     */
    public static <T> ApiResponse<T> parseHttpResponse(int statusCode, String responseBody, Class<T> dataType) {
        // 首先检查HTTP状态码
        if (statusCode >= 400) {
            return ApiResponse.error(statusCode, "HTTP请求失败: " + responseBody);
        }
        
        return parse(responseBody, dataType);
    }
    
    /**
     * 解析JSON响应为ApiResponse
     */
    public static <T> ApiResponse<T> parse(String jsonResponse, Class<T> dataType) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return ApiResponse.error("响应内容为空");
        }
        
        try {
            // 先解析为JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // 检查是否是标准的ApiResponse格式
            if (rootNode.has("code") && rootNode.has("data") && rootNode.has("message")) {
                int code = rootNode.get("code").asInt();
                String message = rootNode.get("message").asText();
                JsonNode dataNode = rootNode.get("data");
                
                T data = null;
                if (dataNode != null && !dataNode.isNull()) {
                    if (dataType == String.class) {
                        data = (T) dataNode.asText();
                    } else {
                        data = objectMapper.treeToValue(dataNode, dataType);
                    }
                }
                
                return new ApiResponse<>(code, data, message);
            } else {
                // 非标准格式，直接解析为目标类型
                return parseDirectly(jsonResponse, dataType);
            }
        } catch (Exception e) {
            // 如果不是有效的JSON，作为错误响应处理
            return ApiResponse.error("响应解析失败: " + jsonResponse);
        }
    }
    
    /**
     * 使用TypeReference解析复杂类型
     */
    public static <T> ApiResponse<T> parse(String jsonResponse, TypeReference<T> typeRef) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return ApiResponse.error("响应内容为空");
        }
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            if (rootNode.has("code") && rootNode.has("data") && rootNode.has("message")) {
                int code = rootNode.get("code").asInt();
                String message = rootNode.get("message").asText();
                JsonNode dataNode = rootNode.get("data");
                
                T data = null;
                if (dataNode != null && !dataNode.isNull()) {
                    data = objectMapper.readValue(dataNode.toString(), typeRef);
                }
                
                return new ApiResponse<>(code, data, message);
            } else {
                // 非标准格式，直接解析为目标类型
                T data = objectMapper.readValue(jsonResponse, typeRef);
                return ApiResponse.success(data);
            }
        } catch (Exception e) {
            return ApiResponse.error("响应解析失败: " + jsonResponse);
        }
    }
    
    /**
     * 直接解析为目标类型（兼容非标准响应格式）
     */
    private static <T> ApiResponse<T> parseDirectly(String jsonResponse, Class<T> dataType) {
        try {
            if (dataType == String.class) {
                return ApiResponse.success((T) jsonResponse);
            } else {
                T data = objectMapper.readValue(jsonResponse, dataType);
                return ApiResponse.success(data);
            }
        } catch (Exception e) {
            return ApiResponse.error("响应解析失败: " + jsonResponse);
        }
    }
}

