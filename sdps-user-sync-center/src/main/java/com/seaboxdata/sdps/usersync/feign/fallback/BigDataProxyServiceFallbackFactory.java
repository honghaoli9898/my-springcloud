package com.seaboxdata.sdps.usersync.feign.fallback;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.ambari.AmbariUser;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.usersync.feign.BigDataCommonProxyFeign;

import feign.hystrix.FallbackFactory;

@Component
public class BigDataProxyServiceFallbackFactory implements
		FallbackFactory<BigDataCommonProxyFeign> {

	@Override
	public BigDataCommonProxyFeign create(Throwable throwable) {
		return new BigDataCommonProxyFeign() {

			@Override
			public Result createItemResource(String itemIden,
					HdfsSetDirObj hdfsSetDirObj) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteRangerGroupByName(Integer clusterId,
					String groupName) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result addUsersToGroup(Integer clusterId, String groupName,
					List<String> rangerUsers) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteUsersToGroup(Integer clusterId,
					String groupName, List<String> rangerUsers) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result addRangerPolicy(RangerPolicyObj rangerPolicyObj) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteRangerPolicy(Integer clusterId,
					String serviceType, String policyName) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result addRangerUser(Integer clusterId,
					List<VXUsers> rangerObjList) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result addAmbariUser(Integer clusterId, AmbariUser ambariUser) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteAmbariUser(Integer clusterId, String username) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteRangerUserByName(Integer clusterId,
					String userName) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result getRangerUserByName(Integer clusterId, String userName) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result updateRangerUserByName(Integer clusterId,
					VXUsers rangerUserObj) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result updateAmbariUserPassword(Integer clusterId,
					AmbariUser ambariUser) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result<JSONArray> getAmbariUsers(Integer clusterId) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result getComponentAndHost(Integer clusterId, String serviceName) {
				return Result.failed("调用代理服务失败");
			}

		};
	}

}
