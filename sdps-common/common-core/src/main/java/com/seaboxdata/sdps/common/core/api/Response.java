package com.seaboxdata.sdps.common.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.seaboxdata.sdps.common.core.constant.BaseConstant;
import com.seaboxdata.sdps.common.core.enums.ErrorStatusEnum;
import com.seaboxdata.sdps.common.core.enums.ResultCodeEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("all")
public class Response<T> {
    private static final long serialVersionUID = 1L;

    // @ApiModelProperty(value = "状态码", required = true)
    private int code;
    // @ApiModelProperty(value = "是否成功", required = true)
    private boolean success;
    // @ApiModelProperty(value = "返回消息", required = true)
    private String msg;
    // @ApiModelProperty(value = "承载数据")
    private T data;



    private Response(ResultCodeI resultCode) {
        this(resultCode, null, resultCode.getMessage());
    }

    private Response(ResultCodeI resultCode, String msg) {
        this(resultCode, null, msg);
    }

    private Response(ResultCodeI resultCode, T data) {
        this(resultCode, data, resultCode.getMessage());
    }

    private Response(ResultCodeI resultCode, T data, String msg) {
        this(resultCode.getCode(), data, msg);
    }



    @SuppressWarnings("unchecked")
    private Response(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.success = ResultCodeEnum.SUCCESS.code == code;
    }

    /**
     * 判断返回是否为成功
     *
     * @param result Result
     * @return 是否成功
     */
    public static boolean isSuccess(@Nullable Response<?> result) {
        return Optional.ofNullable(result).map(x -> ObjectUtils.nullSafeEquals(ResultCodeEnum.SUCCESS.code, x.code)).orElse(Boolean.FALSE);
    }

    /**
     * 判断返回是否为成功
     *
     * @param result Result
     * @return 是否成功
     */
    public static boolean isNotSuccess(@Nullable Response<?> result) {
        return !Response.isSuccess(result);
    }

    /**
     * 返回Response
     *
     * @param data 数据
     * @param <T>  T 泛型标记
     * @return Response
     */
    public static <T> Response<T> data(T data) {
        return data(data, BaseConstant.DEFAULT_SUCCESS_MESSAGE);
    }

    /**
     * 返回R
     *
     * @param data 数据
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return Response
     */
    public static <T> Response<T> data(T data, String msg) {
        return data(HttpServletResponse.SC_OK, data, msg);
    }

    /**
     * 返回R
     *
     * @param code 状态码
     * @param data 数据
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return Response
     */
    public static <T> Response<T> data(int code, T data, String msg) {
        return new Response<T>(code, data, data == null ? BaseConstant.DEFAULT_NULL_MESSAGE : msg);
    }



    /**
     * 返回R
     *
     * @param msg 消息
     * @param <T> T 泛型标记
     * @return Response
     */
    public static <T> Response<T> success(String msg) {
        return new Response<T>(ResultCodeEnum.SUCCESS, msg);
    }

    /**
     * 返回R
     *
     * @param resultCode 业务代码
     * @param <T>        T 泛型标记
     * @return Response
     */
    public static <T> Response<T> success(ResultCodeI resultCode) {
        return new Response<>(resultCode);
    }

    /**
     * 返回R
     *
     * @param resultCode 业务代码
     * @param msg        消息
     * @param <T>        T 泛型标记
     * @return Response
     */
    public static <T> Response<T> success(ResultCodeI resultCode, String msg) {
        return new Response<>(resultCode, msg);
    }

    public static <T> Response<T> success(ResultCodeI resultCode, String msg, T data) {
        return new Response<>(resultCode, data, msg);
    }



    /**
     * 返回R
     *
     * @param msg 消息
     * @param <T> T 泛型标记
     * @return Response
     */
    public static <T> Response<T> failure(String msg) {
        return new Response<>(ResultCodeEnum.FAILURE, msg);
    }

    /**
     * 构建失败结果待数据
     *
     * @param status 失败状态枚举对象
     * @return
     */
    public static <T> Response<T> failure(ErrorStatusEnum status) {
        return failure(status.getCode(), status.getMessage());
    }

    public static <T> Response<T> failure(String msg, T data) {
        return new Response<>(ResultCodeEnum.FAILURE, data, msg);
    }

    /**
     * 返回R
     *
     * @param code 状态码
     * @param msg  消息
     * @param <T>  T 泛型标记
     * @return Response
     */
    public static <T> Response<T> failure(int code, String msg) {
        return new Response<T>(code, null, msg);
    }

    public static <T> Response<T> failure(int code, String msg, T data) {
        return new Response<T>(code, data, msg);
    }

    /**
     * 返回R
     *
     * @param resultCode 业务代码
     * @param <T>        T 泛型标记
     * @return Response
     */
    public static <T> Response<T> fail(ResultCodeI resultCode) {
        return new Response<>(resultCode);
    }

    /**
     * 返回R
     *
     * @param resultCode 业务代码
     * @param msg        消息
     * @param <T>        T 泛型标记
     * @return Response
     */
    public static <T> Response<T> failure(ResultCodeI resultCode, String msg) {
        return new Response<>(resultCode, msg);
    }

    /**
     * 返回R
     *
     * @param flag 成功状态
     * @return Response
     */
    public static <T> Response<T> status(boolean flag) {
        return flag ? success(BaseConstant.DEFAULT_SUCCESS_MESSAGE) : failure(BaseConstant.DEFAULT_FAILURE_MESSAGE);
    }

}
