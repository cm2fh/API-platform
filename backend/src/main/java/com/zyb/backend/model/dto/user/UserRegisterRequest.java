package com.zyb.backend.model.dto.user;

import java.io.Serializable;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, message = "账号长度不能少于4位")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, message = "密码长度不能少于8位")
    private String userPassword;

    @NotBlank(message = "校验密码不能为空")
    @Size(min = 8, message = "校验密码长度不能少于8位")
    private String checkPassword;
}
