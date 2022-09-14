package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.common.framework.bean.SdpsDirExpire;

import java.util.List;

public interface SdpsDirExpireMapper extends SuperMapper<SdpsDirExpire> {

    List<SdpsDirExpire> getCleanDir();
}
