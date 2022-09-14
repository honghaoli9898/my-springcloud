package com.seaboxdata.sdps.bigdataProxy.thread;

import java.util.concurrent.Callable;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class GetYarnAndHdfsMetricsCallable implements Callable<JSONObject> {
	private Integer clusterId;

	private CommonBigData commonBigData;

	public GetYarnAndHdfsMetricsCallable(Integer clusterId,
			CommonBigData commonBigData) {
		this.clusterId = clusterId;
		this.commonBigData = commonBigData;
	}

	@Override
	public JSONObject call() throws Exception {
		JSONObject result = new JSONObject();
		try {
			result = commonBigData.getYarnAndHdfsMetrics(clusterId);
		} catch (Exception e) {
			log.error("调用集群获取警告数接口报错", e);
		}
		return result;
	}

}
