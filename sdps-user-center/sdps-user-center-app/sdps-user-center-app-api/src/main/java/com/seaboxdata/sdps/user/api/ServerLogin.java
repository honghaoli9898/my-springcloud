package com.seaboxdata.sdps.user.api;

import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;

import java.util.Map;

public interface ServerLogin {
    Map<String, String> login(SdpsServerInfo sdpsServerInfo, String username);
}
