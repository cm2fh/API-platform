package com.zyb.backend.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 购买接口调用次数请求
 */
@Data
public class UserInterfaceInfoPurchaseRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceInfoId;

    /**
     * 购买次数
     */
    private Integer count;

    private static final long serialVersionUID = 1L;
} 