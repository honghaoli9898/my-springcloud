package com.seaboxdata.sdps.seaboxProxy.service;

import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;

import java.util.List;

public interface ISeaboxKeytabService {

    void checkKeytab(List<String> keytabs, List<Integer> clusterIds) throws Exception;

    void updateKeytabs(List<SdpServerKeytab> list);
}
