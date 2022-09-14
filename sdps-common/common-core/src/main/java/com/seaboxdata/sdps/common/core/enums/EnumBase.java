package com.seaboxdata.sdps.common.core.enums;


public interface EnumBase {

    /**
     * 码值.
     * @return 返回枚举码值
     */
    Object getCode();

    /**
     * 码值的对应信息.
     * @return 返回枚举码值对应的信息
     */
    String getMessage();
}
