package com.zyb.apiClientSdk.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一API响应封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 响应码，0表示成功
     */
    private int code;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, data, "success");
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(0, null, "success");
    }
    
    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }
    
    /**
     * 失败响应（默认错误码）
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(-1, null, message);
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code == 0;
    }
    
    /**
     * 获取数据，如果失败则抛出异常
     */
    public T getDataOrThrow() {
        if (!isSuccess()) {
            throw new RuntimeException("API调用失败: " + message);
        }
        return data;
    }
}