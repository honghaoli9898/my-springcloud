package com.seaboxdata.sdps.common.core.api;

public interface ResultCodeI {
    /**
     * 消息
     *
     * @return String
     */
    String getMessage();

    /**
     * 状态码
     *
     * @return int
     */
    int getCode();
}
