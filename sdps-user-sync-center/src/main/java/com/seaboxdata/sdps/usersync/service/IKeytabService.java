package com.seaboxdata.sdps.usersync.service;

import java.util.List;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;

import java.util.List;
import java.util.Map;

public interface IKeytabService {
    String getKeytab(Integer clusterId, String keytabFileName) throws Exception;

    String getKrb5(Integer clusterId) throws Exception;

    Page<SdpServerKeytab> selectKeytab(Integer clusterId, String keytabName, String principalType, Integer pageNo, Integer pageSize);

    void checkKeytab(List<String> keytabs, List<Integer> clusterIds) throws Exception;

    void updateKeytabs(List<SdpServerKeytab> list);

    Map<String, Object> pullKeytabFromKdc(List<String> pathList);
}
