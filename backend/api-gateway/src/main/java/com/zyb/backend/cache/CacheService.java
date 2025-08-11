package com.zyb.backend.cache;

import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.apiCommon.service.InnerInterfaceInfoService;
import com.zyb.apiCommon.service.InnerUserInterfaceInfoService;
import com.zyb.apiCommon.service.InnerUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;


/**
 * 缓存服务类
 */
@Service
@Slf4j
public class CacheService {

    @Resource
    private MultiLevelCache multiLevelCache;

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    // 缓存key前缀
    private static final String USER_KEY_PREFIX = "gateway:user:";
    private static final String INTERFACE_KEY_PREFIX = "gateway:interface:";
    private static final String USER_INTERFACE_KEY_PREFIX = "gateway:user_interface:";

    /**
     * 根据accessKey获取用户信息（带缓存）
     */
    public User getUserByAccessKey(String accessKey) {
        String cacheKey = USER_KEY_PREFIX + accessKey;
        return multiLevelCache.get(cacheKey, User.class, MultiLevelCache.CacheType.USER, () -> {
            log.debug("从数据库查询用户: {}", accessKey);
            return innerUserService.getDbUser(accessKey);
        });
    }

    /**
     * 根据url和method获取接口信息（带缓存）
     */
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        String cacheKey = INTERFACE_KEY_PREFIX + url + ":" + method;
        return multiLevelCache.get(cacheKey, InterfaceInfo.class, MultiLevelCache.CacheType.INTERFACE, () -> {
            log.debug("从数据库查询接口: {} {}", method, url);
            return innerInterfaceInfoService.getInterfaceInfo(url, method);
        });
    }

    /**
     * 获取用户接口调用信息（带缓存）
     */
    public UserInterfaceInfo getUserInterfaceInfo(long userId, long interfaceInfoId) {
        String cacheKey = USER_INTERFACE_KEY_PREFIX + userId + ":" + interfaceInfoId;
        return multiLevelCache.get(cacheKey, UserInterfaceInfo.class, MultiLevelCache.CacheType.USER_INTERFACE, () -> {
            log.debug("从数据库查询用户接口信息: userId={}, interfaceId={}", userId, interfaceInfoId);
            return innerUserInterfaceInfoService.checkUserInterface(interfaceInfoId, userId);
        });
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        multiLevelCache.clear();
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return multiLevelCache.getStats();
    }
}
