package com.zyb.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyb.apiCommon.model.entity.InterfaceInfo;

/**
 * @author 张云博
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2025-07-15 16:08:21
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 保存接口信息并自动分配权限
     * @param interfaceInfo 接口信息
     * @param creatorId 创建者ID
     * @return 是否保存成功
     */
    boolean saveWithPermissions(InterfaceInfo interfaceInfo, Long creatorId);

}
