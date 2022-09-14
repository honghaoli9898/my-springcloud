package com.seaboxdata.sdps.licenseclient.bean.license;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;


import com.seaboxdata.sdps.licenseutil.bean.ServiceModel;

import com.seaboxdata.sdps.licenseutil.util.DateUtil;
import de.schlichtherle.license.LicenseContent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * License生成类需要的参数
 */
@Data
@Slf4j
public class LicenseInfoVO implements Serializable{

    private static final long serialVersionUID = -7793154252684580872L;
    /**
     * 证书subject
     */
    private String subject;

    /**
     * 是否过期
     */
    private boolean expire;


    /**
     * 证书生成路径
     */
    private String licensePath;

    /**
     * 密钥库存储路径
     */
    private String privateKeysStorePath;

    /**
     * 公钥文件
     */
    private String publicKeyStorePath;

    /**
     * 证书生效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date issuedTime = new Date();

    /**
     * 证书失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 用户类型
     */
    private String consumerType = "user";

    /**
     * 用户数量
     */
    private Integer consumerAmount = 1;


    /**
     * 系统名称
     */
    private String osName;
    /**
     * 系统版本
     */
    private String version;

    /**
     * 功能列表
     */
    private List<ServiceModel> serviceList;

    /**
     * ESN码
     */
    private String esn;

    /**
     * licenseId
     */
    private String licenseId;

    /**
     * 集群数
     */
    private Integer colonyAmount = 1;

    /**
     * 有效期
     */
    private Integer expireNum;

    /**
     * 当前系统时间
     */
    private String nowTime;

    public LicenseInfoVO() {

    }

    public LicenseInfoVO(LicenseContent licenseContent) {
        this.issuedTime = licenseContent.getIssued();
        this.subject = licenseContent.getSubject();
        this.expireTime = licenseContent.getNotAfter();
        this.nowTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(licenseContent.getExtra()));
        log.info("lic解析结果:" + jsonObject.toString());

        if (!StringUtils.isBlank(jsonObject.getString("esn"))) {
            this.esn = jsonObject.getString("esn");
        }
        if (!StringUtils.isBlank(jsonObject.getString("osName"))) {
            this.osName = jsonObject.getString("osName");
        }
        if (!StringUtils.isBlank(jsonObject.getString("version"))) {
            this.version = jsonObject.getString("version");
        }
        if (!StringUtils.isBlank(jsonObject.getInteger("colonyAmount") + "")) {
            this.colonyAmount = jsonObject.getInteger("colonyAmount");
        }

        if (!jsonObject.getJSONArray("serviceList").isEmpty()) {
            JSONArray jsonArray = jsonObject.getJSONArray("serviceList");
            serviceList = new ArrayList<ServiceModel>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject j = jsonArray.getJSONObject(i);
                ServiceModel serviceModel = new ServiceModel();
                if (null == j) {
                    break;
                }
                if (!StringUtils.isBlank(j.getString("name"))) {
                    serviceModel.setName(j.getString("name"));
                }
                if (!StringUtils.isBlank(j.getString("serviceId"))) {
                    serviceModel.setServiceId(j.getString("serviceId"));
                }
                if (!StringUtils.isBlank(j.getString("desc"))) {
                    serviceModel.setDesc(j.getString("desc"));
                }
                if (!StringUtils.isBlank(j.getString("version"))) {
                    serviceModel.setVersion(j.getString("version"));
                }
                if (!StringUtils.isBlank(j.getString("expire"))) {
                    serviceModel.setExpire(j.getString("expire"));
                }
                serviceList.add(serviceModel);
            }
        }
    }

    @Override
    public String toString() {
        return ((JSONObject) JSONObject.toJSON(this)).toJSONString();
    }
}
