package com.seaboxdata.sdps.common.core.api;

import com.seaboxdata.sdps.common.core.enums.ResultCodeEnum;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Date;

@SuppressWarnings("all")
public class BaseController {


    /**
     * 构建成功结果
     *
     * @param msg
     * @param data
     * @return
     */
    public <T>Response<T> success(String msg, T data) {
        return Response.success(ResultCodeEnum.SUCCESS, msg, data);
    }

    /**
     * 构建失败结果
     *
     * @param msg
     * @param data
     * @return
     */
    public <T>Response<T> failure(String msg, T data) {
        return Response.failure(msg, data);
    }

    /**
     * 构建失败结果
     *
     * @param code
     * @param msg
     * @param data
     * @return
     */
    public <T>Response<T> failure(Integer code, String msg, T data) {
        return Response.failure(code, msg, data);
    }

    /**
     * 构建失败结果
     *
     * @param code
     * @param data
     * @return
     */
    public <T>Response<T> failure(Integer code, T data) {
        return Response.failure(code, null, data);
    }

    /**
     * 构建失败结果
     *
     * @param code
     * @return
     */
    public Response failure(Integer code) {
        return Response.failure(code, null, null);
    }

    /**
     * 构建成功结果
     *
     * @return
     */
    public Response success() {
        return success(null, null);
    }

    /**
     * 构建成功结果带信息
     *
     * @param msg
     * @return
     */
    public Response success(String msg) {
        return success(msg, null);
    }

    /**
     * 构建成功结果待数据
     *
     * @param data
     * @return
     */
    public <T>Response<T> success(T data) {
        return success(null, data);
    }

    /**
     * 构建失败结果
     *
     * @return
     */
    public Response failure() {
        return failure(0, null, null);
    }

    /**
     * 构建失败结果待数据
     *
     * @param msg
     * @return
     */
    public Response failure(String msg) {
        return failure(msg, null);
    }

    /**
     * 构建失败结果带数据
     *
     * @param data
     * @return
     */
    public Response failure(Object data) {
        return failure(0, null, data);
    }


}
