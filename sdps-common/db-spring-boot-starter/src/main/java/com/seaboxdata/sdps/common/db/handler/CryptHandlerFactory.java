package com.seaboxdata.sdps.common.db.handler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.seaboxdata.sdps.common.db.annotation.CryptField;
import com.seaboxdata.sdps.common.db.utils.CryptUtil;

/**
 * 加解密处理者工厂类
 *
 * @author junliang.zhuo
 * @date 2019-03-29 13:02
 */
public class CryptHandlerFactory {

    private static final CryptHandler STRING_HANDLER = new StringCryptHandler();
    private static final CryptHandler COLLECTION_HANDLER = new CollectionCryptHandler();
    private static final CryptHandler LIST_HANDLER = new ListCryptHandler();
    private static final CryptHandler ARRAY_HANDLER = new ArrayCryptHandler();
    private static final CryptHandler BEAN_HANDLER = new BeanCryptHandler();
    private static final CryptHandler EMPTY_HANDLER = new EmptyCryptHandler();


    public static CryptHandler getCryptHandler(Object obj, CryptField cryptField) {
        // 如果是map不处理
        if (obj == null || CryptUtil.inIgnoreClass(obj.getClass()) || obj instanceof Map) {
            return EMPTY_HANDLER;
        }

        if (obj instanceof String && cryptField == null) {
            return EMPTY_HANDLER;
        }
        if (obj instanceof String) {
            return STRING_HANDLER;
        }

        if (obj instanceof List) {
            return LIST_HANDLER;
        }

        if (obj instanceof Collection) {
            return COLLECTION_HANDLER;
        }

        if (obj.getClass().isArray()) {
            return ARRAY_HANDLER;
        }
        return BEAN_HANDLER;
    }

}
