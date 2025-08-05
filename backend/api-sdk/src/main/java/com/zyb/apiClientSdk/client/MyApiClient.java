package com.zyb.apiClientSdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zyb.apiClientSdk.builder.ApiRequestBuilder;
import com.zyb.apiClientSdk.common.ApiResponse;
import com.zyb.apiClientSdk.common.ResponseParser;
import com.zyb.apiClientSdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用第三方接口的客户端
 */
@Slf4j
public class MyApiClient {

    private final String accessKey;
    private final String secretKey;

    public MyApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * 创建带类型的请求构建器
     */
    public <T> ApiRequestBuilder<T> builder(Class<T> responseType) {
        return new ApiRequestBuilder<>(this, responseType);
    }

    /**
     * 默认String类型的请求构建器
     */
    public ApiRequestBuilder<String> builder() {
        return new ApiRequestBuilder<>(this, String.class);
    }

    /**
     * 通用POST请求方法
     */
    public <T> ApiResponse<T> post(String path, Object params, Class<T> responseType) {
        try {
            String json = JSONUtil.toJsonStr(params);
            HttpResponse httpResponse = HttpRequest.post(path)
                    .addHeaders(getHeaderMap(json))
                    .body(json)
                    .charset(StandardCharsets.UTF_8)
                    .execute();

            int statusCode = httpResponse.getStatus();
            String responseBody = httpResponse.body();

            return ResponseParser.parseHttpResponse(statusCode, responseBody, responseType);
        } catch (Exception e) {
            return ApiResponse.error("请求失败: " + e.getMessage());
        }
    }

    /**
     * 通用GET请求方法
     */
    public <T> ApiResponse<T> get(String path, Map<String, Object> params, Class<T> responseType) {
        try {
            // GET请求使用空字符串参与签名
            HttpRequest request = HttpRequest.get(path)
                    .addHeaders(getHeaderMap(""));

            if (params != null && !params.isEmpty()) {
                request.form(params);
            }

            HttpResponse httpResponse = request.execute();
            int statusCode = httpResponse.getStatus();
            String responseBody = httpResponse.body();

            return ResponseParser.parseHttpResponse(statusCode, responseBody, responseType);
        } catch (Exception e) {
            return ApiResponse.error("请求失败: " + e.getMessage());
        }
    }

    /**
     * 支持TypeReference的POST方法（用于复杂类型）
     */
    public <T> ApiResponse<T> post(String path, Object params, TypeReference<T> typeRef) {
        try {
            String json = JSONUtil.toJsonStr(params);
            HttpResponse httpResponse = HttpRequest.post(path)
                    .addHeaders(getHeaderMap(json))
                    .body(json)
                    .execute();
            
            int statusCode = httpResponse.getStatus();
            String responseBody = httpResponse.body();
            
            if (statusCode >= 400) {
                return ApiResponse.error(statusCode, "HTTP请求失败: " + responseBody);
            }
            
            return ResponseParser.parse(responseBody, typeRef);
        } catch (Exception e) {
            return ApiResponse.error("请求失败: " + e.getMessage());
        }
    }

    /**
     * 获取请求头的映射
     * @param body 请求体内容
     * @return 包含请求头参数的哈希映射
     */
    private Map<String, String> getHeaderMap(String body) {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(4));
        hashMap.put("body", body);
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        hashMap.put("sign", SignUtils.genSign(body, secretKey));
        hashMap.put("Content-Type", "application/json;charset=UTF-8");

        return hashMap;
    }

}
