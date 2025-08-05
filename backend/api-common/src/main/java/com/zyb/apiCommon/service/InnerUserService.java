package com.zyb.apiCommon.service;

import com.zyb.apiCommon.model.entity.User;

/**
 * 内部用户信息服务
 */
public interface InnerUserService {

    /**
     * 数据库中查是否已分配给用户 ak
     */
    User getDbUser(String accessKey);
}
