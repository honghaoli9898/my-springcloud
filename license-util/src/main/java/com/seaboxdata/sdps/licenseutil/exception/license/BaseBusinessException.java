package com.seaboxdata.sdps.licenseutil.exception.license;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 异常处理基类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseBusinessException extends RuntimeException implements Serializable {

    /**
     * 异常错误码
     */
    public int code;
    /**
     * 异常消息
     */
    public String msg;

    public BaseBusinessException() {
    }

    public BaseBusinessException(String msg) {
        super(msg);
    }
}
