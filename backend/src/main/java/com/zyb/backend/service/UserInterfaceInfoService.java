package com.zyb.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.backend.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;

/**
* @author 张云博
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2025-07-16 15:28:37
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest);

    boolean purchase(long interfaceInfoId, int count, User loginUser);
} 