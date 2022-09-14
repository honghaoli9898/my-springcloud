package com.seaboxdata.sdps.licenseutil.common;


import com.seaboxdata.sdps.licenseutil.bean.CustomKeyStoreParam;
import com.seaboxdata.sdps.licenseutil.bean.LicenseVerifyParam;
import com.seaboxdata.sdps.licenseutil.exception.license.BusinessException;
import de.schlichtherle.license.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

/**
 * License校验类
 */
public class LicenseVerify {
    private static Logger logger = LogManager.getLogger(LicenseVerify.class);

    /**
     * 安装License证书
     */
    public synchronized LicenseContent install(LicenseVerifyParam param) {
        LicenseContent result = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //1. 安装证书
        try {
            //先卸载原有的证书
            LicenseManager licenseManager = LicenseManagerHolder.getInstance(initLicenseParam(param));
            licenseManager.uninstall();
            //卸载完原证书要先把原有实例置空
            licenseManager = LicenseManagerHolder.refreshInstance(initLicenseParam(param));
            result = licenseManager.install(new File(param.getLicensePath()));
            logger.info(MessageFormat.format("证书安装成功，证书有效期：{0} - {1}", format.format(result.getNotBefore()), format.format(result.getNotAfter())));
        } catch (Exception e) {
            logger.error("证书安装失败！", e);
        }

        return result;
    }

    /**
     * 校验License证书
     *
     * @return boolean
     */
    public boolean verify() {
        LicenseManager licenseManager = LicenseManagerHolder.getInstance(null);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (null == licenseManager) {
            logger.error("LicenseVerify:license未安装");
            throw BusinessException.ERR_LICENSE_NOT_INSTALL;
        }
        //2. 校验证书
        try {
            LicenseContent licenseContent = licenseManager.verify();
            if (null == licenseContent) {
                logger.error("License校验失败,License对象为空");
                return false;
            }
            if (licenseContent.getNotAfter().compareTo(new Date()) < 0) {
                logger.error(MessageFormat.format("License已过期, 有效期为: {0} - {1}", format.format(licenseContent.getNotBefore()), format.format(licenseContent.getNotAfter())));
                return false;
            }
            logger.info(MessageFormat.format("证书校验通过，证书有效期：{0} - {1}", format.format(licenseContent.getNotBefore()), format.format(licenseContent.getNotAfter())));
            return true;
        } catch (Exception e) {
            logger.error("证书校验失败！", e);
            return false;
        }
    }

    /**
     * 初始化证书生成参数
     *
     * @param param License校验类需要的参数
     * @return de.schlichtherle.license.LicenseParam
     */
    private LicenseParam initLicenseParam(LicenseVerifyParam param) {
        Preferences preferences = Preferences.userNodeForPackage(LicenseVerify.class);
        CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());
        KeyStoreParam publicStoreParam = new CustomKeyStoreParam(LicenseVerify.class,
                param.getPublicKeysStorePath(),
                param.getPublicAlias(),
                param.getStorePass(),
                null);

        return new DefaultLicenseParam(param.getSubject()
                , preferences
                , publicStoreParam
                , cipherParam);
    }

}
