package com.zyb.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.backend.common.ErrorCode;
import com.zyb.backend.exception.BusinessException;
import com.zyb.backend.mapper.InterfaceInfoMapper;
import com.zyb.backend.mapper.UserInterfaceInfoMapper;
import com.zyb.backend.mapper.UserMapper;
import com.zyb.backend.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.zyb.backend.service.UserInterfaceInfoService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
* @author 张云博
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2025-07-16 15:28:37
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }
        if (userInterfaceInfo.getRemainNum() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求次数不足");
        }
    }

    @Override
    public QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (userInterfaceInfoQueryRequest == null) {
            return queryWrapper;
        }

        Long id = userInterfaceInfoQueryRequest.getId();
        Long userId = userInterfaceInfoQueryRequest.getUserId();
        Long interfaceInfoId = userInterfaceInfoQueryRequest.getInterfaceInfoId();
        Integer totalNum = userInterfaceInfoQueryRequest.getTotalNum();
        Integer remainNum = userInterfaceInfoQueryRequest.getRemainNum();
        Integer status = userInterfaceInfoQueryRequest.getStatus();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(interfaceInfoId != null, "interfaceInfoId", interfaceInfoId);
        queryWrapper.eq(totalNum != null, "totalNum", totalNum);
        queryWrapper.eq(remainNum != null, "remainNum", remainNum);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purchase(long interfaceInfoId, int count, User loginUser) {
        // 1. 校验接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceInfoId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口不存在");
        }

        // 2. 校验是否为免费接口
        if (interfaceInfo.getPrice() == null || interfaceInfo.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "免费或价格未定的接口无需购买次数");
        }

        // 3. 计算总价
        BigDecimal totalPrice = interfaceInfo.getPrice().multiply(new BigDecimal(count));

        // 4. 校验余额
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (user.getBalance() == null || user.getBalance().compareTo(totalPrice) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足");
        }

        // 5. 扣费
        user.setBalance(user.getBalance().subtract(totalPrice));
        int updateUserResult = userMapper.updateById(user);
        if (updateUserResult <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "扣费失败");
        }

        // 6. 增加调用次数
        UserInterfaceInfo userInterfaceInfo = this.lambdaQuery()
                .eq(UserInterfaceInfo::getUserId, loginUser.getId())
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .one();

        if (userInterfaceInfo != null) {
            // 已有记录，增加次数
            userInterfaceInfo.setRemainNum(userInterfaceInfo.getRemainNum() + count);
        } else {
            // 新纪录
            userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(loginUser.getId());
            userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
            userInterfaceInfo.setTotalNum(0);
            userInterfaceInfo.setRemainNum(count);
            userInterfaceInfo.setStatus(0); // 假设 0 是启用状态
        }

        return this.saveOrUpdate(userInterfaceInfo);
    }
} 