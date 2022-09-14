package com.seaboxdata.sdps.bigdataProxy.thread;

import java.util.concurrent.Callable;

import com.seaboxdata.sdps.bigdataProxy.platform.impl.CommonBigData;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class GetWarningCntCallable implements Callable<Integer> {
	private Integer clusterId;

	private CommonBigData commonBigData;

	public GetWarningCntCallable(Integer clusterId, CommonBigData commonBigData) {
		this.clusterId = clusterId;
		this.commonBigData = commonBigData;
	}

	@Override
	public Integer call() throws Exception {
		Integer cnt = -1;
		try {
			cnt = commonBigData.getWarningCnt(clusterId);
		} catch (Exception e) {
			log.error("调用集群获取警告数接口报错",e);
		}
		return cnt;
	}

}
