package com.zyb.apiInterface.common;

/**
 * controller 统一的错误响应
 */
public class ErrorResponse {

    public static String buildErrorResponse(String message) {
        return String.format("{\"error\": \"%s\"}", message);
    }
}
