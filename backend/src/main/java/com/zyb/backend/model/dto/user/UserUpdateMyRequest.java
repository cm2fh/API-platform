package com.zyb.backend.model.dto.user;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *
 * @author zyb
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 充值金额
     */
    private BigDecimal balance;

    private static final long serialVersionUID = 1L;
}