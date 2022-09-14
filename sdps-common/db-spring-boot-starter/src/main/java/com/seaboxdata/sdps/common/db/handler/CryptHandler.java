package com.seaboxdata.sdps.common.db.handler;

import com.seaboxdata.sdps.common.db.annotation.CryptField;

/**
 * 加解密处理抽象类
 *
 * @author junliang.zhuo
 * @date 2019-03-29 11:40
 */
public interface CryptHandler<T> {

    Object encrypt(T param, CryptField cryptField);

    Object decrypt(T param, CryptField cryptField);
}
