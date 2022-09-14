package com.seaboxdata.sdps.common.core.exception;

/**
 * 业务异常
 *
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 6610083281801529147L;

    public BusinessException(String message) {
        super(message);
    }

    public static final BusinessException REST_GET_SERVICE_EXCEPTION = new BusinessException("内部服务get调用异常！");

    public static final BusinessException REST_POST_SERVICE_EXCEPTION = new BusinessException("内部服务post调用异常！");

    public static final BusinessException NULL_PARAM_EXCEPTION = new BusinessException("存在空参");

    public static final BusinessException FILE_NOT_EXIST_EXCEPTION = new BusinessException("文件不存在");

    public static final BusinessException FILE_EXIST_EXCEPTION = new BusinessException("文件已存在");

    public static final BusinessException BASE64_CONVERT_EXCEPTION = new BusinessException("base64转换错误");

    public static final BusinessException FILE_COPY_EXCEPTION = new BusinessException("文件复制错误");

    public static final BusinessException FILE_DEL_EXCEPTION = new BusinessException("文件删除错误");
}
