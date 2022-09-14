package com.seaboxdata.sdps.licenseclient.bean.license;

import com.fasterxml.jackson.annotation.JsonFormat;


import com.seaboxdata.sdps.licenseutil.bean.LicenseServerInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * License生成类需要的参数
 */
@Data
public class LicenseInfo implements Serializable{

    private static final long serialVersionUID = -7793154252684580872L;
    /**
     * 证书subject
     */
    private String subject;

    /**
     * 密钥别称
     */
    private String privateAlias;

    /**
     * 密钥密码（需要妥善保管，不能让使用者知道）
     */
    private String keyPass;

    /**
     * 访问秘钥库的密码
     */
    private String storePass;

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
     * 集群数
     */
    private Integer colonyAmount = 1;

    /**
     * 描述信息
     */
    private String description = "";

    /**
     * 额外的服务器硬件校验信息
     */
    private LicenseServerInfo licenseCheckModel;

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
    private List<Map<String, String>> serviceList;


    /**
     * ESN码
     */
    private String esn;


    /**
     * 客户端服务器信息
     */
    private String serverInfos;

    /**
     * 有效期数量(天)
     */
    private Integer expireNum;


    public boolean checkParam() {
        if (StringUtils.isBlank(esn)) {
            return false;
        }
        if (StringUtils.isBlank(expireNum + "")) {
            return false;
        }
        return !StringUtils.isBlank(colonyAmount + "");
    }
}
