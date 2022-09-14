package com.seaboxdata.sdps.licenseutil.event;


import com.seaboxdata.sdps.licenseutil.bean.LicenseServerInfo;
import com.seaboxdata.sdps.licenseutil.bean.LicenseVerifyParam;
import com.seaboxdata.sdps.licenseutil.common.LicenseUtil;
import com.seaboxdata.sdps.licenseutil.common.LicenseVerify;
import com.seaboxdata.sdps.licenseutil.util.FileUtil;
import de.schlichtherle.license.LicenseContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 在项目启动时安装证书
 *
 * @author cainiao
 */
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE + 3)
public class LicenseCheckListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
//        String esn = LicenseUtil.getServerInfo(restTemplate);
//        if (StringUtils.isBlank(esn)) {
//            log.error("获取ESN码失败,安装License失败,请提前启动LicenseClient服务后重试");
//        }

//        File ff = new File(System.getProperty("user.dir"));
//        String licensePath = ff.getParent() + "/LicenseClient/lic/";
        String licensePath = LicenseUtil.LICENSE_PATH;
        String configString = FileUtil.readFileContent(licensePath + "config.config");
        if (null == configString) {
            log.warn("=======license配置文件解析失败,可能尚未安装License======");
            return;
        }

        LicenseVerifyParam licenseVerifyParam = LicenseUtil.analysisConfig(configString);
        if (null == licenseVerifyParam) {
            log.warn("==========license配置文件解析异常===========");
            return;
        }

        log.info("++++++++ License服务启动安装证书 ++++++++");
        licenseVerifyParam.setLicensePath(licensePath + "license.lic");
        licenseVerifyParam.setPublicKeysStorePath(licensePath + "publicKey.keystore");
        LicenseVerify licenseVerify = new LicenseVerify();
        //安装证书
        try {
            LicenseContent licenseContent = licenseVerify.install(licenseVerifyParam);
            LicenseServerInfo licenseServerInfo = new LicenseServerInfo();
//            String esn = LicenseUtil.getServerInfo(restTemplate);
//            if (StringUtils.isBlank(esn)) {
//                log.error("获取ESN码失败,安装License失败,请提前启动LicenseClient服务后重试");
//                //卸载已安装的License
//                LicenseManager licenseManager = LicenseManagerHolder.getInstance(null);
//                licenseManager.uninstall();
//            }
            log.info("++++++++ License服务启动证书安装结束 ++++++++");
        } catch (Exception e) {
            log.error("License启动安装失败", e);
        }
    }
}
