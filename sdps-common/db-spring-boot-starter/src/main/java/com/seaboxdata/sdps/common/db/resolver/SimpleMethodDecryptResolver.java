package com.seaboxdata.sdps.common.db.resolver;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.seaboxdata.sdps.common.db.annotation.CryptField;
import com.seaboxdata.sdps.common.db.handler.CryptHandlerFactory;

/**
 * 简单解密处理者
 *
 * @author junliang.zhuo
 * @date 2019-03-29 13:12
 */
@Getter
@AllArgsConstructor
public class SimpleMethodDecryptResolver implements MethodDecryptResolver {

    private CryptField cryptField;

    @Override
    public Object processDecrypt(Object param) {
        return CryptHandlerFactory.getCryptHandler(param, cryptField).decrypt(param, cryptField);
    }

}