package com.seaboxdata.sdps.licenseclient.service;

import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerDTO;
import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerVO;
import java.util.List;


/**
 * 客户端服务器信息服务层
 */
public interface LicenseServerService{

    void add(LicenseServerDTO licenseServerDTO);

    LicenseServerVO getServerInfo(LicenseServerDTO licenseServerDTO);

    List<LicenseServerVO> getServerInfos();
}
