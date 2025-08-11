package com.zyb.backend.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存管理器
 */
@Component
@Slf4j
public class MultiLevelCache {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // L1缓存：用户缓存，1分钟未访问过期，最大500个
    private final Cache<String, Object> userCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    // L1缓存：接口缓存，2分钟未访问过期，最大300个
    private final Cache<String, Object> interfaceCache = Caffeine.newBuilder()
            .maximumSize(300)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    // L1缓存：用户接口关系缓存，4分钟未访问过期，最大1000个
    private final Cache<String, Object> userInterfaceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(4, TimeUnit.MINUTES)
            .build();

    /**
     * 缓存类型枚举
     */
    public enum CacheType {
        USER, INTERFACE, USER_INTERFACE
    }

    /**
     * 获取缓存数据
     */
    public <T> T get(String key, Class<T> type, CacheType cacheType, Supplier<T> supplier) {
        // 根据缓存类型选择对应的本地缓存
        Cache<String, Object> localCache = getLocalCache(cacheType);

        // L1: 先查本地缓存
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            log.debug("命中L1缓存[{}]: {}", cacheType, key);
            return type.cast(localValue);
        }

        // L2: 查Redis缓存
        try {
            String redisValue = stringRedisTemplate.opsForValue().get(key);
            if (redisValue != null) {
                log.debug("命中L2缓存[{}]: {}", cacheType, key);
                T data = objectMapper.readValue(redisValue, type);
                // 回写L1缓存
                localCache.put(key, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败: {}", e.getMessage());
        }

        // L3: 查数据库
        log.debug("缓存未命中，查询数据库[{}]: {}", cacheType, key);
        T data = supplier.get();
        if (data != null) {
            // 写入缓存
            put(key, data, cacheType);
        }
        return data;
    }

    /**
     * 根据缓存类型获取对应的本地缓存
     */
    private Cache<String, Object> getLocalCache(CacheType cacheType) {
        return switch (cacheType) {
            case USER -> userCache;
            case INTERFACE -> interfaceCache;
            case USER_INTERFACE -> userInterfaceCache;
        };
    }

    /**
     * 写入缓存
     */
    public void put(String key, Object value, CacheType cacheType) {
        if (value == null) {
            return;
        }

        // 写入L1缓存
        Cache<String, Object> localCache = getLocalCache(cacheType);
        localCache.put(key, value);

        // 写入L2缓存，根据缓存类型设置不同的过期时间
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            Duration expireTime = getRedisExpireTime(cacheType);
            stringRedisTemplate.opsForValue().set(key, jsonValue, expireTime);
            log.debug("写入缓存[{}]: {}, 过期时间: {}", cacheType, key, expireTime);
        } catch (JsonProcessingException e) {
            log.warn("Redis缓存写入失败: {}", e.getMessage());
        }
    }

    /**
     * 根据缓存类型获取Redis过期时间
     */
    private Duration getRedisExpireTime(CacheType cacheType) {
        return switch (cacheType) {
            case USER -> Duration.ofMinutes(5);        // 用户缓存5分钟
            case INTERFACE -> Duration.ofMinutes(5);   // 接口缓存5分钟
            case USER_INTERFACE -> Duration.ofMinutes(5); // 用户接口关系缓存5分钟
        };
    }

    /**
     * 删除缓存
     */
    public void evict(String key, CacheType cacheType) {
        Cache<String, Object> localCache = getLocalCache(cacheType);
        localCache.invalidate(key);
        try {
            stringRedisTemplate.delete(key);
            log.debug("删除缓存[{}]: {}", cacheType, key);
        } catch (Exception e) {
            log.warn("Redis缓存删除失败: {}", e.getMessage());
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        userCache.invalidateAll();
        interfaceCache.invalidateAll();
        userInterfaceCache.invalidateAll();
        log.info("清空所有本地缓存");
    }

    /**
     * 获取缓存统计信息
     */
    public String getStats() {
        var userStats = userCache.stats();
        var interfaceStats = interfaceCache.stats();
        var userInterfaceStats = userInterfaceCache.stats();

        return String.format(
                "用户缓存 - 命中率: %.2f%%, 大小: %d\n" +
                "接口缓存 - 命中率: %.2f%%, 大小: %d\n" +
                "用户接口缓存 - 命中率: %.2f%%, 大小: %d",
                userStats.hitRate() * 100, userCache.estimatedSize(),
                interfaceStats.hitRate() * 100, interfaceCache.estimatedSize(),
                userInterfaceStats.hitRate() * 100, userInterfaceCache.estimatedSize()
        );
    }
}
