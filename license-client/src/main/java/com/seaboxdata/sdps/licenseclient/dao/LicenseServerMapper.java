package com.seaboxdata.sdps.licenseclient.dao;

import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerDTO;
import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerVO;
import java.util.List;


/**
 * 客户端服务器信息持久化层
 */
public interface LicenseServerMapper{

    /**
     * 新增一条数据
     *
     * @param licenseServerDTO
     */
    void insert(LicenseServerDTO licenseServerDTO);

    /**
     * 更新CPU序列号和主板序列号查询指定记录
     *
     * @param licenseServerDTO
     * @return
     */
    LicenseServerVO getServerInfoByCpuAndMainBoard(LicenseServerDTO licenseServerDTO);

    /**
     * 查询所有数据
     *
     * @return
     */
    List<LicenseServerVO> getServerInfos();
}
