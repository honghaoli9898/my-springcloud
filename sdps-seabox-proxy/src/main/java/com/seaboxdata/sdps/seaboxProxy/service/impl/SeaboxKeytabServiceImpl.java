package com.seaboxdata.sdps.seaboxProxy.service.impl;

import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.utils.KeytabUtil;
import com.seaboxdata.sdps.seaboxProxy.config.CommonConstraint;
import com.seaboxdata.sdps.seaboxProxy.config.DynamicDataSourceConfig;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaboxKeytabService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SeaboxKeytabServiceImpl implements ISeaboxKeytabService {
    @Autowired
    private DynamicDataSourceConfig dynamicDataSourceConfig;

    @Override
    public void checkKeytab(List<String> keytabs, List<Integer> clusterIds) throws Exception {
        KeytabUtil.checkKeytabEffective(keytabs, clusterIds, "SeaboxInnerRestTemplate", true);
        //移除Phoenix连接池
        clusterIds.forEach(clusterId -> {
            String datasourceKey = clusterId + CommonConstraint.phoenix;
            dynamicDataSourceConfig.removeDataSource(datasourceKey);
        });
    }

    @Override
    public void updateKeytabs(List<SdpServerKeytab> list) {
        KeytabUtil.updateKeytabs(list);
        for (SdpServerKeytab keytab : list) {
            if ("SDPS".equals(keytab.getPrincipalType())) {
                continue;
            }
            String datasourceKey = keytab.getClusterId() + CommonConstraint.phoenix;
            dynamicDataSourceConfig.removeDataSource(datasourceKey);
        }
    }

}
