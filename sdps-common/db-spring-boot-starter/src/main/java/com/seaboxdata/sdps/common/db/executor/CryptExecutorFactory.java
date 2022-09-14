package com.seaboxdata.sdps.common.db.executor;

import com.seaboxdata.sdps.common.db.annotation.CryptField;
import com.seaboxdata.sdps.common.db.exception.CryptException;

/**
 * 加解密执行者工厂类
 *
 * @author junliang.zhuo
 * @date 2019-03-29 20:31
 */
public class CryptExecutorFactory {

    private static CryptExecutor PHONE_CRYPT_HANDLER = new PhoneCryptExecutor();
    private static CryptExecutor NAME_CRYPT_HANDLER = new NameCryptExecutor();

    /**
     * 根据cryptField中不同的配置
     */
    public static CryptExecutor getTypeHandler(CryptField cryptField) {
        CryptExecutor cryptExecutor;
        if (cryptField.value() == CryptType.NAME) {
            cryptExecutor = NAME_CRYPT_HANDLER;
        } else if (cryptField.value() == CryptType.PHONE) {
            cryptExecutor = PHONE_CRYPT_HANDLER;
        } else {
            throw new CryptException();
        }
        return cryptExecutor;
    }
}
