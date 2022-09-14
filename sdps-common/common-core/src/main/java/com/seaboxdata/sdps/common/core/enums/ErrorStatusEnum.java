package com.seaboxdata.sdps.common.core.enums;


public enum ErrorStatusEnum implements EnumBase {
    /**
     * 系统内部错误
     */
    INTERNAL_SERVER_ERROR(10000, ExceptionCodeEnum.B0001.getMessage()),
    /**
     * 参数错误
     */
    ILLEGAL_ARGUMENT(10001, ExceptionCodeEnum.A0400.getMessage()),
    /**
     * 业务错误
     */
    SERVICE_EXCEPTION(10002, "业务错误"),
    /**
     * 非法的数据格式，参数没有经过校验
     */
    ILLEGAL_DATA(10003, "数据错误"),

    MULTIPART_TOO_LARGE(1004, "文件太大"),
    /**
     * 非法状态
     */
    ILLEGAL_STATE(10005, "非法状态"),
    /**
     * 缺少参数
     */
    MISSING_ARGUMENT(10006, "缺少参数"),
    /**
     * 非法访问
     */
    ILLEGAL_ACCESS(10007, "非法访问,没有认证"),
    /**
     * 权限不足
     */
    UNAUTHORIZED(10008, ExceptionCodeEnum.A0301.getMessage()),

    /**
     * 错误的请求
     */
    METHOD_NOT_ALLOWED(10009, "不支持的方法"),


    /**
     * 参数错误
     */
    ILLEGAL_ARGUMENT_TYPE(10010, "参数类型错误");

    private final Integer code;

    private final String message;

    ErrorStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    /**
     * Return the integer code of this status code.
     */
    @Override
    public Integer getCode() {
        return this.code;
    }

    /**
     * Return the reason phrase of this status code.
     */
    @Override
    public String getMessage() {
        return this.message;
    }

}
