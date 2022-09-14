package com.seaboxdata.sdps.licenseutil.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import com.seaboxdata.sdps.licenseutil.bean.LicenseServerInfo;
import com.seaboxdata.sdps.licenseutil.bean.LicenseVerifyParam;
import com.seaboxdata.sdps.licenseutil.bean.ServiceModel;
import com.seaboxdata.sdps.licenseutil.util.RestTemplateUtil;
import de.schlichtherle.license.LicenseContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

/**
 * license工具
 */
@Slf4j
public class LicenseUtil {

    public Map<String, String> analysisLic(String licPath) {
        File licFile = new File(licPath);
        StringBuffer sb = new StringBuffer();
        Map<String, String> licContentMap = new HashMap<String, String>();
        if (licFile.isFile() && licFile.exists()) {
            try (InputStream in = new FileInputStream(licFile)) {
                byte[] tempbytes = new byte[1024];
                int byteread = 0;
                // 读入多个字节到字节数组中，byteread为一次读入的字节数
                while ((byteread = in.read(tempbytes)) != -1) {
                    String str = new String(tempbytes, 0, byteread);
                    sb.append(str);
                }

                String licContent = sb.toString();
                String[] licArr = licContent.split("&");
                for (String s : licArr) {
                    String[] ss = s.split("=");
                    licContentMap.put(ss[0], ss[1]);
                }

            } catch (IOException e) {
                log.error("IOException", e);
            }
        } else {
            log.error("lic文件不存在");
        }
        return licContentMap;
    }

    /**
     * 解析license配置文件中参数
     *
     * @return
     */
    public static LicenseVerifyParam analysisConfig(String configString) {
        JSONObject json = JSONObject.parseObject(configString);
        if (null == json) {
            return null;
        }
        LicenseVerifyParam licenseInfo = new LicenseVerifyParam();
        if (!StringUtils.isBlank(json.getString("subject"))) {
            licenseInfo.setSubject(json.getString("subject"));
        }
        if (!StringUtils.isBlank(json.getString("publicAlias"))) {
            licenseInfo.setPublicAlias(json.getString("publicAlias"));
        }
        if (!StringUtils.isBlank(json.getString("storePass"))) {
            licenseInfo.setStorePass(json.getString("storePass"));
        }

        return licenseInfo;
    }

    /**
     * a请求LicenseClient的获取ESN码接口
     *
     * @param restTemplate
     * @return
     */
    public static String getServerInfo(RestTemplate restTemplate) {
        final String SERVERINFO_URL = "http://licenseclient/client/getServerInfos";
        String esn = "";
        try {
            esn = RestTemplateUtil.restGet(restTemplate, SERVERINFO_URL, String.class);
        } catch (Exception e) {
            log.error("向LicenseClient请求ESN码失败");
        }
        return esn;
    }

    /**
     * 校验ESN码
     *
     * @param licenseContent
     * @return
     */
    public static boolean verifyLic(LicenseContent licenseContent, LicenseServerInfo serverInfo) {
        LicenseServerInfo licenseServerInfo = analysisLicenseContent(licenseContent);
        if (null == licenseServerInfo || null == serverInfo) {
            return false;
        }
        if (licenseServerInfo.getEsn().equals(serverInfo.getEsn())) {
            return true;
        }
        return false;
    }

    /**
     * 将LicenseContent对象解析成LicenseServerInfo实体
     *
     * @param licenseContent
     * @return
     */
    public static LicenseServerInfo analysisLicenseContent(LicenseContent licenseContent) {
        if (null == licenseContent) {
            return null;
        }
        LicenseServerInfo licenseServerInfo = new LicenseServerInfo();
        licenseServerInfo.setIssuedTime(licenseContent.getIssued());
        licenseServerInfo.setExpireTime(licenseContent.getNotAfter());
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(licenseContent.getExtra()));

        if (!org.apache.commons.lang3.StringUtils.isBlank(jsonObject.getString("esn"))) {
            licenseServerInfo.setEsn(jsonObject.getString("esn"));
        }
        if (!org.apache.commons.lang3.StringUtils.isBlank(jsonObject.getString("osName"))) {
            licenseServerInfo.setOsName(jsonObject.getString("osName"));
        }
        if (!org.apache.commons.lang3.StringUtils.isBlank(jsonObject.getString("version"))) {
            licenseServerInfo.setVersion(jsonObject.getString("version"));
        }
        if (!org.apache.commons.lang3.StringUtils.isBlank(jsonObject.getInteger("colonyAmount") + "")) {
            licenseServerInfo.setColonyAmount(jsonObject.getInteger("colonyAmount"));
        }

        if (!jsonObject.getJSONArray("serviceList").isEmpty()) {
            JSONArray jsonArray = jsonObject.getJSONArray("serviceList");
            List<ServiceModel> serviceList = new ArrayList<ServiceModel>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject j = jsonArray.getJSONObject(i);
                ServiceModel serviceModel = new ServiceModel();
                if (null == j) {
                    break;
                }
                if (!org.apache.commons.lang3.StringUtils.isBlank(j.getString("name"))) {
                    serviceModel.setName(j.getString("name"));
                }
                if (!org.apache.commons.lang3.StringUtils.isBlank(j.getString("serviceId"))) {
                    serviceModel.setServiceId(j.getString("serviceId"));
                }
                if (!org.apache.commons.lang3.StringUtils.isBlank(j.getString("desc"))) {
                    serviceModel.setDesc(j.getString("desc"));
                }
                if (!org.apache.commons.lang3.StringUtils.isBlank(j.getString("version"))) {
                    serviceModel.setVersion(j.getString("version"));
                }
                if (!org.apache.commons.lang3.StringUtils.isBlank(j.getString("expire"))) {
                    serviceModel.setExpire(j.getString("expire"));
                }
                serviceList.add(serviceModel);
            }
        }
        return licenseServerInfo;
    }

    /**
     * License默认安装路径
     */
    public static final String LICENSE_PATH = "/usr/lic/";

}
