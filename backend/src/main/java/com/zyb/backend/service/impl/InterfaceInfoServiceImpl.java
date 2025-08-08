package com.zyb.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.backend.common.ErrorCode;
import com.zyb.backend.constant.UserConstant;
import com.zyb.backend.exception.BusinessException;
import com.zyb.backend.mapper.InterfaceInfoMapper;
import com.zyb.backend.service.InterfaceInfoService;
import com.zyb.backend.service.UserInterfaceInfoService;
import com.zyb.backend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 张云博
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2025-07-15 16:08:21
*/
@Service
@Slf4j
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = interfaceInfo.getName();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithPermissions(InterfaceInfo interfaceInfo, Long creatorId) {
        // 1. 保存接口信息
        boolean result = this.save(interfaceInfo);
        if (!result) {
            return false;
        }

        // 2. 自动为创建者和管理员分配接口权限
        try {
            autoAssignInterfacePermissions(interfaceInfo.getId(), creatorId);
            log.info("成功为接口 {} 自动分配权限", interfaceInfo.getId());
        } catch (Exception e) {
            log.error("自动分配接口权限失败，接口ID: {}, 创建者ID: {}", interfaceInfo.getId(), creatorId, e);
            // 不抛出异常，避免影响接口创建的主流程
        }

        return true;
    }

    /**
     * 自动为创建者和管理员分配接口权限
     * @param interfaceInfoId 接口ID
     * @param creatorId 创建者ID
     */
    private void autoAssignInterfacePermissions(long interfaceInfoId, long creatorId) {
        // 1. 为创建者分配权限（如果创建者不是管理员）
        User creator = userService.getById(creatorId);
        if (creator != null && !UserConstant.ADMIN_ROLE.equals(creator.getUserRole())) {
            createUserInterfacePermission(creatorId, interfaceInfoId, 100); // 给创建者100次调用权限
        }

        // 2. 为所有管理员分配权限
        List<User> adminUsers = userService.list(
            new QueryWrapper<User>().eq("userRole", UserConstant.ADMIN_ROLE)
        );

        for (User admin : adminUsers) {
            createUserInterfacePermission(admin.getId(), interfaceInfoId, -1); // 给管理员无限次调用权限
        }

        log.info("成功为接口 {} 自动分配权限，创建者: {}，管理员数量: {}",
                interfaceInfoId, creatorId, adminUsers.size());
    }

    /**
     * 创建用户接口权限记录
     * @param userId 用户ID
     * @param interfaceInfoId 接口ID
     * @param remainNum 剩余调用次数（-1表示无限次）
     */
    private void createUserInterfacePermission(long userId, long interfaceInfoId, int remainNum) {
        // 检查是否已存在权限记录
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo existingPermission = userInterfaceInfoService.getOne(queryWrapper);

        if (existingPermission == null) {
            // 创建新的权限记录
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
            userInterfaceInfo.setTotalNum(0);
            userInterfaceInfo.setRemainNum(remainNum);
            userInterfaceInfo.setStatus(0); // 0-正常

            userInterfaceInfoService.save(userInterfaceInfo);
            log.info("为用户 {} 创建接口 {} 的调用权限，剩余次数: {}", userId, interfaceInfoId, remainNum);
        } else {
            log.info("用户 {} 已有接口 {} 的调用权限，跳过创建", userId, interfaceInfoId);
        }
    }
}




