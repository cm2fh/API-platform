package com.zyb.apiClientSdk.builder;

import com.zyb.apiClientSdk.client.MyApiClient;
import com.zyb.apiClientSdk.common.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * API请求构建器
 */
public class ApiRequestBuilder<T> {

    private final MyApiClient client;
    private final Class<T> responseType;
    private String url;
    private String method = "GET";
    private final Map<String, Object> params = new HashMap<>();

    public ApiRequestBuilder(MyApiClient client, Class<T> responseType) {
        this.client = client;
        this.responseType = responseType;
    }

    public ApiRequestBuilder<T> url(String url) {
        this.url = url;
        return this;
    }

    public ApiRequestBuilder<T> get() {
        this.method = "GET";
        return this;
    }

    public ApiRequestBuilder<T> post() {
        this.method = "POST";
        return this;
    }

    public ApiRequestBuilder<T> param(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    public ApiResponse<T> execute() {
        if ("POST".equalsIgnoreCase(method)) {
            return client.post(url, params, responseType);
        } else {
            return client.get(url, params, responseType);
        }
    }
}