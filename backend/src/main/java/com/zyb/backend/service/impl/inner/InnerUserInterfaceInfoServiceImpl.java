package com.zyb.backend.service.impl.inner;

import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.apiCommon.service.InnerUserInterfaceInfoService;
import com.zyb.backend.common.ErrorCode;
import com.zyb.backend.exception.BusinessException;
import com.zyb.backend.service.InterfaceInfoService;
import com.zyb.backend.service.UserInterfaceInfoService;
import com.zyb.backend.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private InterfaceInfoService interfaceInfoService;


    @Override
    public UserInterfaceInfo checkUserInterface(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .eq(UserInterfaceInfo::getUserId, userId)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invokeCountAndDeductBalance(long interfaceInfoId, long userId) {
        // 校验
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取接口信息
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceInfoId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }
        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 获取用户接口关系
        UserInterfaceInfo userInterfaceInfo = this.checkUserInterface(interfaceInfoId, userId);
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "未开通接口调用权限");
        }

        // TODO 所有接口全部免费，移除计费逻辑（收费之后再改）

        // 计次逻辑
        Integer remainNum = userInterfaceInfo.getRemainNum();
        
        // 仅增加总调用次数，无论是否为无限次调用
        userInterfaceInfo.setTotalNum(userInterfaceInfo.getTotalNum() + 1);
        
        // remainNum != -1 为有限次调用接口，现在默认全部设置为 -1 表示无限调用
        if (remainNum != -1) {
            if (remainNum <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用次数不足");
            }
            // 扣减调用次数
            userInterfaceInfo.setRemainNum(remainNum - 1);
        }
        
        // 无论是否扣减次数，都需要更新总调用次数
            boolean updateUserInterfaceInfoResult = userInterfaceInfoService.updateById(userInterfaceInfo);
            if (!updateUserInterfaceInfoResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新调用次数失败");
        }

        return true;
    }
}
