package com.seaboxdata.sdps.common.crypto.exception;

/**
 * 解密异常类
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
public class ApiDecodeException extends RuntimeException {
	private static final long serialVersionUID = -1914283143998053580L;
	private ApiCryptoExceptionType exceptionType;

    public ApiDecodeException(ApiCryptoExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public ApiDecodeException(String message) {
        super(message);
    }

    public ApiDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiDecodeException(Throwable cause) {
        super(cause);
    }

    public ApiCryptoExceptionType getExceptionType() {
        return exceptionType;
    }

}
