package com.zyb.backend.common;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {

    FORBIDDEN(40300, "禁止访问"),

    IP_NOT_IN_WHITE_LIST(40301, "IP地址不在白名单中"),

    MISSING_HEADERS(40302, "请求头参数不完整"),

    INVALID_ACCESS_KEY(40303, "accessKey无效"),

    INVALID_NONCE(40304, "随机数无效"),

    TIMESTAMP_EXPIRED(40305, "时间戳已过期"),

    SIGNATURE_MISMATCH(40306, "签名验证失败"),

    INTERFACE_NOT_FOUND(40307, "接口不存在"),

    NO_INVOKE_PERMISSION(40308, "没有调用该接口的权限"),

    INSUFFICIENT_INVOCATIONS(40309, "接口调用次数不足"),

    INSUFFICIENT_BALANCE(40310, "用户余额不足");

    private final int code;
    private final String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
