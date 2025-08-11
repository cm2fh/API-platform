package com.zyb.backend;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyb.apiClientSdk.utils.SignUtils;
import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.apiCommon.service.InnerUserInterfaceInfoService;
import com.zyb.backend.cache.CacheService;
import com.zyb.backend.common.ErrorCodeEnum;
import com.zyb.backend.config.GatewayConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

    @Slf4j
    @Component
    public class CustomGlobalFilter implements GlobalFilter, Ordered {

        @DubboReference
        private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

        @Resource
        private CacheService cacheService;

        @Resource
        private GatewayConfig gatewayConfig;

    @Override
    @SentinelResource(
        value = "api-gateway-filter",
        blockHandler = "handleBlock",
        fallback = "handleFallback"
    )
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().toString();
        String sourceAddress = request.getLocalAddress().getHostString();
        ServerHttpResponse response = exchange.getResponse();
        String fullUrl = gatewayConfig.getHost() + path;

        // 1. 黑白名单
        if (!gatewayConfig.getIpWhiteList().contains(sourceAddress)) {
            log.error("IP地址不在白名单中：{}", sourceAddress);
            return handleNoAuth(response, ErrorCodeEnum.IP_NOT_IN_WHITE_LIST);
        }

        // 2. 用户鉴权
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");

        String body;
        if ("GET".equalsIgnoreCase(method)) {
            // GET请求使用空字符串作为body参与签名
            body = "";
        } else {
            // POST等其他请求从header中获取body
            body = headers.getFirst("body");
            // 处理编码问题
            if (body != null) {
                try {
                    body = new String(body.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.warn("Body编码转换失败: {}", e.getMessage());
                }
            }
        }

        // 校验参数
        if (accessKey == null || nonce == null || timestamp == null || sign == null || (!"GET".equalsIgnoreCase(method) && body == null)) {
            log.error("请求头参数不完整");
            return handleNoAuth(response, ErrorCodeEnum.MISSING_HEADERS);
        }

        // 使用缓存查询用户信息
        User dbUser = null;
        try {
            dbUser = cacheService.getUserByAccessKey(accessKey);
        } catch (Exception e) {
            log.error("查询用户信息失败", e);
        }
        if (dbUser == null) {
            log.error("用户不存在, accessKey: {}", accessKey);
            return handleNoAuth(response, ErrorCodeEnum.INVALID_ACCESS_KEY);
        }

        // 校验随机数
        if (Long.parseLong(nonce) >= 10000) {
            return handleNoAuth(response, ErrorCodeEnum.INVALID_NONCE);
        }

        // 校验时间戳与当前时间的差距不超过2分钟
        long currentTime = System.currentTimeMillis() / 1000;
        if ((currentTime - Long.parseLong(timestamp)) >= 60 * 2L) {
            return handleNoAuth(response, ErrorCodeEnum.TIMESTAMP_EXPIRED);
        }

        String secretKey = dbUser.getSecretKey();
        String serverSign = SignUtils.genSign(body, secretKey);
        if (!sign.equals(serverSign)) {
            log.error("签名验证失败");
            return handleNoAuth(response, ErrorCodeEnum.SIGNATURE_MISMATCH);
        }

        // 使用缓存查询接口信息
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = cacheService.getInterfaceInfo(fullUrl, method);
        } catch (Exception e) {
            log.error("查询接口信息失败", e);
        }
        if (interfaceInfo == null) {
            log.error("接口不存在，path: {}, method: {}", fullUrl, method);
            return handleNoAuth(response, ErrorCodeEnum.INTERFACE_NOT_FOUND);
        }

        // TODO 接口全部改为免费，移除价格检查 (收费之后再改)

        // 使用缓存校验用户调用次数
        UserInterfaceInfo userInterfaceInfo = null;
        try {
            userInterfaceInfo = cacheService.getUserInterfaceInfo(dbUser.getId(), interfaceInfo.getId());
        } catch (Exception e) {
            log.error("获取用户接口调用信息失败", e);
        }

        if (userInterfaceInfo == null) {
            log.error("用户未开通该接口调用权限，userId: {}, interfaceId: {}",
                      dbUser.getId(), interfaceInfo.getId());
            return handleNoAuth(response, ErrorCodeEnum.NO_INVOKE_PERMISSION);
        }

        if (userInterfaceInfo.getRemainNum() <= 0 && userInterfaceInfo.getRemainNum() != -1) {
            log.error("用户调用次数不足，userId: {}, interfaceId: {}",
                      dbUser.getId(), interfaceInfo.getId());
            return handleNoAuth(response, ErrorCodeEnum.INSUFFICIENT_INVOCATIONS);
        }

        return interceptResponse(exchange, chain, interfaceInfo.getId(), dbUser.getId());
    }

    /**
     * 拦截响应，获取接口返回值并执行后续处理
     */
    private Mono<Void> interceptResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            // 包装响应，拦截响应体
            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux<? extends DataBuffer> fluxBody) {
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            // 读取响应体内容
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            DataBufferUtils.release(dataBuffer);
                            // 获取响应内容
                            String responseBody = new String(content, StandardCharsets.UTF_8);
                            log.info("接口返回值：{}", responseBody);
                            // 扣减调用次数和余额
                            innerUserInterfaceInfoService.invokeCountAndDeductBalance(interfaceInfoId, userId);
                            // 重新包装响应体
                            return bufferFactory.wrap(content);
                        }));
                    }
                    return super.writeWith(body);
                }
            };

            // 使用包装后的响应
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .response(decoratedResponse)
                    .build();

            return chain.filter(mutatedExchange);
        } catch (Exception e) {
            log.error("网关处理响应异常", e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response, ErrorCodeEnum errorCode) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", errorCode.getCode());
        errorBody.put("message", errorCode.getMessage());

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] errorBytes;
        try {
            errorBytes = objectMapper.writeValueAsBytes(errorBody);
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应体失败", e);
            errorBytes = "{\"code\":50000,\"message\":\"服务器内部错误\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(errorBytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Sentinel限流处理器
     */
    public Mono<Void> handleBlock(ServerWebExchange exchange, BlockException ex) {
        log.warn("请求被Sentinel限流，异常类型: {}", ex.getClass().getSimpleName());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        // 设置响应头
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        // 返回限流提示信息
        String result = "{\"code\":429,\"message\":\"系统繁忙，请稍后重试\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Sentinel降级处理器
     */
    public Mono<Void> handleFallback(ServerWebExchange exchange, Throwable ex) {
        log.error("请求处理异常，触发降级: {}", ex.getMessage(), ex);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);

        // 设置响应头
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        // 返回降级提示信息
        String result = "{\"code\":503,\"message\":\"服务暂时不可用，请稍后重试\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
