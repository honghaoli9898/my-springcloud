package com.seaboxdata.sdps.licenseutil.common;


import de.schlichtherle.license.LicenseManager;
import de.schlichtherle.license.LicenseParam;

/**
 * de.schlichtherle.license.LicenseManager的单例
 */
public class LicenseManagerHolder {

    private static volatile LicenseManager LICENSE_MANAGER;

    public static LicenseManager getInstance(LicenseParam param) {
        if (LICENSE_MANAGER == null) {
            synchronized (LicenseManagerHolder.class) {
                if (LICENSE_MANAGER == null && null != param) {
                    LICENSE_MANAGER = new CustomLicenseManager(param);
                }
            }
        }
        return LICENSE_MANAGER;
    }

    /**
     * 这个是因为Lic更新时出现原证书所用实例不能用在新证书上,所以手动刷新一次
     */
    public static LicenseManager refreshInstance(LicenseParam param) {
        if(null != LICENSE_MANAGER){
            LICENSE_MANAGER = null;
        }
        return  getInstance(param);
    }
}
