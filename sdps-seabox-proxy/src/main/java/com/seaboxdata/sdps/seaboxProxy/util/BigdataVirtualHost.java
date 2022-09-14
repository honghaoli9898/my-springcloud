package com.seaboxdata.sdps.seaboxProxy.util;

import io.leopard.javahost.JavaHost;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import cn.hutool.core.map.MapUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;

@Component
public class BigdataVirtualHost {

    @Autowired
    BigdataCommonFegin bigdataCommonFegin;
	private Map<Integer, Object> getClusterNameParam(AmbariUtil ambariUtil) {
		String clusterName = ambariUtil.getClusterName();
		Map<Integer, Object> param = MapUtil.newHashMap();
		param.put(1, clusterName);
		return param;
	}
    /**
     * 设置虚拟host,解决大数据配置都是域名问题
     *
     * @param clusterId
     */
    public void setVirtualHost(Integer clusterId) {
    	AmbariUtil ambariUtil = new AmbariUtil(clusterId);
		ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
		ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
		Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
				"ambari", "ip");
		JSONObject result = ambariUtil.getAmbariApi(args.getData()
				.getArgValue(), args.getData().getArgValueDesc(),
				getClusterNameParam(ambariUtil));
		JSONArray items = result.getJSONArray("items");
		items.forEach(item ->{
			JSONObject jsonObj = (JSONObject)item;
			JSONObject host = jsonObj.getJSONObject("Hosts");
			String ip = host.getString("ip");
			String hostName = host.getString("host_name"); 
			JavaHost.updateVirtualDns(hostName, ip);
		});
        //打印虚拟host
        JavaHost.printAllVirtualDns();
    }
}
