package com.zyb.apiCommon.service;

import com.zyb.apiCommon.model.entity.UserInterfaceInfo;

/**
 * 内部用户接口信息服务
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 校验用户接口
     */
    UserInterfaceInfo checkUserInterface(long interfaceInfoId, long userId);

    /**
     * 调用接口，统一处理计数和扣费
     */
    boolean invokeCountAndDeductBalance(long interfaceInfoId, long userId);
}