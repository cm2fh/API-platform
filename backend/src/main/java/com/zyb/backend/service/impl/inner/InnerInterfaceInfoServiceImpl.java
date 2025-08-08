package com.zyb.backend.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zyb.apiCommon.model.entity.InterfaceInfo;
import com.zyb.apiCommon.service.InnerInterfaceInfoService;
import com.zyb.backend.common.ErrorCode;
import com.zyb.backend.exception.BusinessException;
import com.zyb.backend.mapper.InterfaceInfoMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        // 参数校验
        if (StringUtils.isAnyBlank(path, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建查询条件包装器
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);

        return interfaceInfoMapper.selectOne(queryWrapper);
    }
}
