package com.zyb.backend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.model.entity.User;
import com.zyb.backend.annotation.AuthCheck;
import com.zyb.backend.common.*;
import com.zyb.backend.constant.CommonConstant;
import com.zyb.backend.constant.UserConstant;
import com.zyb.backend.exception.BusinessException;
import com.zyb.backend.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.zyb.backend.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.zyb.backend.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.zyb.backend.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.zyb.backend.service.InterfaceInfoService;
import com.zyb.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.zyb.apiCommon.model.enums.InterfaceInfoStatusEnum.OFFLINE;
import static com.zyb.apiCommon.model.enums.InterfaceInfoStatusEnum.ONLINE;

/**
 * 接口管理
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    /**
     * 创建
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.saveWithPermissions(interfaceInfo, loginUser.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
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
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return ResultUtils.success(interfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     */
    @GetMapping("/list/page")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String name = interfaceInfoQuery.getName();
        String description = interfaceInfoQuery.getDescription();
        // name 和 description 需支持模糊搜索
        interfaceInfoQuery.setName(null);
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 分页获取公开接口列表（无需权限）
     */
    @GetMapping("/list/page/public")
    public BaseResponse<Page<InterfaceInfo>> listPublicInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            interfaceInfoQueryRequest = new InterfaceInfoQueryRequest();
        }

        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();

        // 限制爬虫
        if (size > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        // 只查询已上线的接口
        queryWrapper.eq("status", 1);
        // 支持按名称和描述模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 排序
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder != null && sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(interfaceInfoPage);
    }

    /**
     * 上线接口
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验该接口是否存在
        long id = idRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        // 测试调用
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        // 获取接口信息
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();

        // 根据请求方法确定body内容，与网关保持一致
        String bodyForSign;
        if ("GET".equalsIgnoreCase(method)) {
            bodyForSign = ""; // GET请求使用空字符串参与签名
        } else {
            bodyForSign = "{}"; // 其他请求使用空JSON对象
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("accessKey", accessKey);
        headers.put("nonce", RandomUtil.randomNumbers(4));
        headers.put("body", bodyForSign);
        headers.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        headers.put("sign", com.zyb.apiClientSdk.utils.SignUtils.genSign(bodyForSign, secretKey));

        HttpResponse httpResponse;

        try {
            if ("POST".equalsIgnoreCase(method)) {
                httpResponse = HttpRequest.post(url)
                        .addHeaders(headers)
                        .body(bodyForSign)
                        .execute();
            } else { // 默认为 GET
                // GET请求不需要解析JSON参数，直接发送空参数
                httpResponse = HttpRequest.get(url)
                        .addHeaders(headers)
                        .execute();
            }
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败: " + e.getMessage());
        }


        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);

        return ResultUtils.success(result);
    }

    /**
     * 下线接口
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);

        return ResultUtils.success(result);
    }

    /**
     * 测试调用
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = interfaceInfoInvokeRequest.getId();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (oldInterfaceInfo.getStatus() == OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }

        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();

        // 获取接口信息
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();

        // 根据请求方法确定body内容
        String bodyForSign;
        if ("GET".equalsIgnoreCase(method)) {
            bodyForSign = ""; // GET请求使用空字符串参与签名
        } else {
            bodyForSign = userRequestParams; // 其他请求使用实际参数
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("accessKey", accessKey);
        headers.put("nonce", RandomUtil.randomNumbers(4));
        headers.put("body", bodyForSign);
        headers.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        headers.put("sign", com.zyb.apiClientSdk.utils.SignUtils.genSign(bodyForSign, secretKey));

        HttpResponse httpResponse;

        if ("POST".equalsIgnoreCase(method)) {
            httpResponse = HttpRequest.post(url)
                    .addHeaders(headers)
                    .body(userRequestParams)
                    .execute();
        } else if ("GET".equalsIgnoreCase(method)) {
            // GET请求处理
            if (userRequestParams != null && !userRequestParams.trim().isEmpty()) {
                // 如果有参数，解析为查询参数
                Gson gson = new Gson();
                Map<String, Object> params = gson.fromJson(userRequestParams, new TypeToken<Map<String, Object>>() {}.getType());
                httpResponse = HttpRequest.get(url)
                        .addHeaders(headers)
                        .form(params)
                        .execute();
            } else {
                // 无参数的GET请求
                httpResponse = HttpRequest.get(url)
                        .addHeaders(headers)
                        .execute();
            }
        } else {
            // 其他HTTP方法暂不支持
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂不支持该HTTP方法: " + method);
        }

        return ResultUtils.success(httpResponse.body());
    }

} 