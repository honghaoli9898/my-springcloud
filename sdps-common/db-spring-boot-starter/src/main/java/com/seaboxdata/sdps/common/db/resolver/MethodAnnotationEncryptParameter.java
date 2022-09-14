package com.seaboxdata.sdps.common.db.resolver;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.seaboxdata.sdps.common.db.annotation.CryptField;

/**
 * 方法加密注解了的参数
 *
 * @author junliang.zhuo
 * @date 2019-03-29 11:38
 */
@AllArgsConstructor
@Getter
class MethodAnnotationEncryptParameter {

    private String paramName;
    private CryptField cryptField;
    private Class cls;
}
