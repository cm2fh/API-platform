package com.zyb.apiCommon.model.vo;

import com.zyb.apiCommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调用记录视图对象
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InvokeRecordVO extends InterfaceInfo {

    private static final long serialVersionUID = 1L;

    /**
     * 调用总次数
     */
    private Integer totalNum;
} 