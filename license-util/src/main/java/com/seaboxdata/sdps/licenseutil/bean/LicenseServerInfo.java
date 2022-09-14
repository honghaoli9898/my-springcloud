package com.seaboxdata.sdps.licenseutil.bean;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;


import com.seaboxdata.sdps.licenseutil.common.ServerInfoConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 自定义需要校验的License参数
 */
@Data
public class LicenseServerInfo implements Serializable {

    private static final long serialVersionUID = 8600137500316662317L;
    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;

    /**
     * 可被允许的MAC地址
     */
    private List<String> macAddress;

    /**
     * 可被允许的CPU序列号
     */
    private String cpuSerial;

    /**
     * 可被允许的主板序列号
     */
    private String mainBoardSerial;

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
     * 有效时间
     */
    private Date expireTime;

    /**
     * 发布时间
     */
    private Date issuedTime;

    public LicenseServerInfo() {
    }


    /**
     * 将服务器信息字符串解析成对象属性
     *
     * @param serverInfos
     */
    public LicenseServerInfo(String serverInfos) {
        String[] infos = serverInfos.split("&");
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> m = new HashMap<String, String>();
        for (String s : infos) {
            String[] info = s.split("=");
            m.put(info[0], info[1]);
        }
        this.ipAddress = Lists.newArrayList(m.get(ServerInfoConstant.CPU_SERIAL));
        this.macAddress = Lists.newArrayList(m.get(ServerInfoConstant.MAC_ADDRESS));
        this.cpuSerial = m.get(ServerInfoConstant.CPU_SERIAL);
        this.mainBoardSerial = m.get(ServerInfoConstant.MAIN_BOARD_SERIAL);
    }

    public boolean checkParam() {
        if (StringUtils.isBlank(esn)) {
            return false;
        }
        if (StringUtils.isBlank(expireNum + "")) {
            return false;
        }
        if (StringUtils.isBlank(colonyAmount + "")) {
            return false;
        }
        return true;
    }

    /**
     * 将License中Extra对象里的数据解析成LicenseServerInfo
     *
     * @param obj
     */
    public LicenseServerInfo(Object obj) {
        JSONObject json = (JSONObject) JSONObject.toJSON(obj);
        LicenseServerInfo licenseServerInfo = json.toJavaObject(LicenseServerInfo.class);
        this.colonyAmount = licenseServerInfo.getColonyAmount();
        this.cpuSerial = licenseServerInfo.getCpuSerial();
        this.esn = licenseServerInfo.getEsn();
        this.expireNum = licenseServerInfo.getExpireNum();
        this.ipAddress = licenseServerInfo.getIpAddress();
        this.macAddress = licenseServerInfo.getMacAddress();
        this.osName = licenseServerInfo.getOsName();
        this.serviceList = licenseServerInfo.serviceList;
    }

    /**
     * 重新toString方法把服务器信息组装成字符串
     *
     * @return
     */
    @Override
    public String toString() {
        return "ipAddress=" + ipAddress +
                "&macAddress=" + macAddress +
                "&cpuSerial=" + cpuSerial +
                "&mainBoardSerial=" + mainBoardSerial +
                "&esn=" + esn;
    }
}
