package com.seaboxdata.sdps.licenseclient.service.impl;

import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerDTO;
import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerVO;
import com.seaboxdata.sdps.licenseclient.dao.LicenseServerMapper;
import com.seaboxdata.sdps.licenseclient.service.LicenseServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LicenseServerServiceImpl implements LicenseServerService{

    @Autowired
    private LicenseServerMapper licenseServerMapper;

    @Override
    public void add(LicenseServerDTO licenseServerDTO) {

    }

    @Override
    public LicenseServerVO getServerInfo(LicenseServerDTO licenseServerDTO) {
        return null;
    }

    @Override
    public List<LicenseServerVO> getServerInfos() {
        return licenseServerMapper.getServerInfos();
    }
}
