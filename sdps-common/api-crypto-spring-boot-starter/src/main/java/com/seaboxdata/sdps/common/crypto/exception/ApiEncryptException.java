package com.seaboxdata.sdps.common.crypto.exception;

/**
 * 加密异常类
 *
 * @author hermes-di
 * @since 1.0.0.RELEASE
 */
public class ApiEncryptException extends RuntimeException {
	private static final long serialVersionUID = -8058149114929097261L;
	private ApiCryptoExceptionType exceptionType;

	public ApiEncryptException(ApiCryptoExceptionType exceptionType) {
		super(exceptionType.getMessage());
		this.exceptionType = exceptionType;
	}

	public ApiEncryptException(String message) {
		super(message);
	}

	public ApiEncryptException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiEncryptException(Throwable cause) {
		super(cause);
	}

	public ApiCryptoExceptionType getExceptionType() {
		return exceptionType;
	}

}
