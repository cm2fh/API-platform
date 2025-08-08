package com.zyb.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.apiCommon.model.entity.UserInterfaceInfo;
import com.zyb.apiCommon.model.vo.InvokeRecordVO;
import com.zyb.backend.annotation.AuthCheck;
import com.zyb.backend.common.BaseResponse;
import com.zyb.backend.common.DeleteRequest;
import com.zyb.backend.common.ErrorCode;
import com.zyb.backend.common.ResultUtils;
import com.zyb.backend.common.IdRequest;
import com.zyb.backend.constant.CommonConstant;
import com.zyb.backend.constant.UserConstant;
import com.zyb.backend.exception.BusinessException;
import com.zyb.backend.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.zyb.backend.model.dto.userinterfaceinfo.UserInterfaceInfoAddRequest;
import com.zyb.backend.model.dto.userinterfaceinfo.UserInterfaceInfoPurchaseRequest;
import com.zyb.backend.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.zyb.backend.model.dto.userinterfaceinfo.UserInterfaceInfoUpdateRequest;
import com.zyb.backend.service.InterfaceInfoService;
import com.zyb.backend.service.UserInterfaceInfoService;
import com.zyb.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户调用接口管理
 */
@RestController
@RequestMapping("/userInterfaceInfo")
@Slf4j
public class UserInterfaceInfoController {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    /**
     * 创建
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUserInterfaceInfo(@RequestBody UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        if (userInterfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
        // 校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newUserInterfaceInfoId = userInterfaceInfo.getId();
        return ResultUtils.success(newUserInterfaceInfoId);
    }

    /**
     * 购买接口调用次数
     */
    @PostMapping("/purchase")
    public BaseResponse<Boolean> purchaseInterface(@RequestBody UserInterfaceInfoPurchaseRequest purchaseRequest, HttpServletRequest request) {
        if (purchaseRequest == null || purchaseRequest.getInterfaceInfoId() <= 0 || purchaseRequest.getCount() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long interfaceInfoId = purchaseRequest.getInterfaceInfoId();
        int count = purchaseRequest.getCount();
        User loginUser = userService.getLoginUser(request);

        boolean result = userInterfaceInfoService.purchase(interfaceInfoId, count, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userInterfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserInterfaceInfo(@RequestBody UserInterfaceInfoUpdateRequest userInterfaceInfoUpdateRequest,
                                                         HttpServletRequest request) {
        if (userInterfaceInfoUpdateRequest == null || userInterfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoUpdateRequest, userInterfaceInfo);
        // 参数校验
        userInterfaceInfoService.validUserInterfaceInfo(userInterfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = userInterfaceInfoUpdateRequest.getId();
        // 判断是否存在
        UserInterfaceInfo oldUserInterfaceInfo = userInterfaceInfoService.getById(id);
        if (oldUserInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldUserInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = userInterfaceInfoService.updateById(userInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     */
    @GetMapping("/get")
    public BaseResponse<UserInterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getById(id);
        return ResultUtils.success(userInterfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<UserInterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest userInterfaceInfoQueryRequest) {
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        if (userInterfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.list(queryWrapper);
        return ResultUtils.success(userInterfaceInfoList);
    }

    /**
     * 分页获取列表
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/list/page")
    public BaseResponse<Page<UserInterfaceInfo>> listUserInterfaceInfoByPage(UserInterfaceInfoQueryRequest userInterfaceInfoQueryRequest, HttpServletRequest request) {
        if (userInterfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserInterfaceInfo userInterfaceInfoQuery = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoQueryRequest, userInterfaceInfoQuery);
        long current = userInterfaceInfoQueryRequest.getCurrent();
        long size = userInterfaceInfoQueryRequest.getPageSize();
        String sortField = userInterfaceInfoQueryRequest.getSortField();
        String sortOrder = userInterfaceInfoQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>(userInterfaceInfoQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(userInterfaceInfoPage);
    }

    /**
     * 分页获取我的调用记录
     */
    @GetMapping("/my/list/page")
    public BaseResponse<Page<InvokeRecordVO>> listMyInvokeRecordsByPage(
            UserInterfaceInfoQueryRequest queryRequest, HttpServletRequest request) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        queryRequest.setUserId(loginUser.getId());
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Page<UserInterfaceInfo> userInterfaceInfoPage = userInterfaceInfoService.page(
                new Page<>(current, size),
                userInterfaceInfoService.getQueryWrapper(queryRequest)
        );

        // 转换VO
        Page<InvokeRecordVO> invokeRecordVOPage = new Page<>(
                userInterfaceInfoPage.getCurrent(),
                userInterfaceInfoPage.getSize(),
                userInterfaceInfoPage.getTotal()
        );

        List<InvokeRecordVO> invokeRecordVOList = userInterfaceInfoPage.getRecords().stream().map(record -> {
            InvokeRecordVO vo = new InvokeRecordVO();
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(record.getInterfaceInfoId());
            BeanUtils.copyProperties(interfaceInfo, vo);
            vo.setTotalNum(record.getTotalNum());
            return vo;
        }).collect(Collectors.toList());

        invokeRecordVOPage.setRecords(invokeRecordVOList);
        return ResultUtils.success(invokeRecordVOPage);
    }

    /**
     * 开通接口
     */
    @PostMapping("/apply")
    public BaseResponse<Boolean> applyInterface(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        long interfaceInfoId = idRequest.getId();
        // 校验接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceInfoId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        
        // 判断接口是否已上线
        if (interfaceInfo.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口未上线");
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        long userId = loginUser.getId();
        
        // 判断用户是否已经开通过该接口
        UserInterfaceInfo existUserInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .one();
                
        if (existUserInterfaceInfo != null) {
            // 如果已开通但不是无限次调用，则设置为无限次
            if (existUserInterfaceInfo.getRemainNum() != -1) {
                existUserInterfaceInfo.setRemainNum(-1); // 设置为无限次调用
                userInterfaceInfoService.updateById(existUserInterfaceInfo);
                return ResultUtils.success(true);
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已开通该接口且为无限次调用");
        }
        
        // 创建新的用户接口关系
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
        userInterfaceInfo.setTotalNum(0);
        userInterfaceInfo.setRemainNum(-1); // 设置为-1表示无限次调用
        userInterfaceInfo.setStatus(0); // 正常状态
        
        boolean result = userInterfaceInfoService.save(userInterfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户接口信息
     */
    @GetMapping("/get/interface")
    public BaseResponse<UserInterfaceInfo> getUserInterfaceInfo(@RequestParam long interfaceInfoId, HttpServletRequest request) {
        if (interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        long userId = loginUser.getId();
        
        // 查询用户接口关系
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.lambdaQuery()
                .eq(UserInterfaceInfo::getUserId, userId)
                .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                .one();
        
        return ResultUtils.success(userInterfaceInfo);
    }
} 