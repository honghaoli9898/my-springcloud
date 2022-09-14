package com.seaboxdata.sdps.licenseutil.event;


import com.seaboxdata.sdps.licenseutil.bean.LicenseVerifyParam;
import com.seaboxdata.sdps.licenseutil.common.LicenseUtil;
import com.seaboxdata.sdps.licenseutil.common.LicenseVerify;
import com.seaboxdata.sdps.licenseutil.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

/**
 * @author cainiao
 */
@Slf4j
@Component
public class LicenseUpdateListener {

    private CountDownLatch latch;

    @Autowired
    public LicenseUpdateListener(CountDownLatch latch) {
        this.latch = latch;
    }

    /**
     * 接收License更新通知
     */
    public void licenseUpdateMessage(Object obj) {
        log.info("接收到License更新通知,开始进行License授权更新");

//        File ff = new File(System.getProperty("user.dir"));
//        String licensePath = ff.getParent() + "/LicenseClient/lic/";
        String licensePath = LicenseUtil.LICENSE_PATH;
        String configString = FileUtil.readFileContent(licensePath + "config.config");
        if (null == configString) {
            log.warn("=======License刷新-license配置文件解析失败,可能尚未安装License======");
            return;
        }

        LicenseVerifyParam licenseVerifyParam = LicenseUtil.analysisConfig(configString);
        if (null == licenseVerifyParam) {
            log.warn("==========License刷新-license配置文件解析异常===========");
            return;
        }

        licenseVerifyParam.setLicensePath(licensePath + "license.lic");
        licenseVerifyParam.setPublicKeysStorePath(licensePath + "publicKey.keystore");
        LicenseVerify licenseVerify = new LicenseVerify();
        //安装证书
        try {
            licenseVerify.install(licenseVerifyParam);
            log.info("++++++++ License授权信息刷新完成 ++++++++");
        } catch (Exception e) {
            log.error("License刷新失败", e);
        }

        latch.countDown();
    }
}
