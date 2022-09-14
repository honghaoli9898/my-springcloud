package com.seaboxdata.sdps.item.feign.fallback;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsSetDirObj;
import com.seaboxdata.sdps.common.framework.bean.dto.FileStatsDTO;
import com.seaboxdata.sdps.common.framework.bean.ranger.RangerPolicyObj;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnQueueConfInfo;
import com.seaboxdata.sdps.item.feign.BigDataCommonProxyFeign;

import feign.hystrix.FallbackFactory;

@Slf4j
@Component
public class BigDataProxyServiceFallbackFactory implements
		FallbackFactory<BigDataCommonProxyFeign> {

	@Override
	public BigDataCommonProxyFeign create(Throwable throwable) {
		return new BigDataCommonProxyFeign() {

			@Override
			public Result createHdfsPath(Integer clusterId,
					String createHdfsPath) {
				log.error("创建集群:{},hdfs目录:{}异常", clusterId, createHdfsPath,
						throwable);
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteItemFile(Integer clusterId,
					List<String> hdfsPathList) {
				log.error("删除集群:{},hdfs目录:{}异常", clusterId, hdfsPathList,
						throwable);
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result createHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj) {
				log.error("创建集群:{}异常", hdfsSetDirObj, throwable);
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result updataHdfsQNAndSQNAndOwner(HdfsSetDirObj hdfsSetDirObj) {
				log.error("更新集群:{},异常", hdfsSetDirObj, throwable);
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result<HdfsDirObj> selectHdfsQNAndSQN(Integer clusterId,
					String hdfsPath) {
				log.error("查询集群:{},hdfs目录:{},异常", clusterId, hdfsPath,
						throwable);
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result<SysGlobalArgs> getGlobalParam(String type, String key) {
				return Result.failed("调用代理服务失败");
			}

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
			public Result uploadScripFile(String username, Integer clusterId,
					MultipartFile file, String path, boolean isUserFile) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteScriptFile(String username, Integer clusterId,
					List<String> hdfsPaths, boolean flag) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result<List<FileStatsDTO>> getStatsByType(Integer clusterId,
					String type) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result updateYarnQueueConfigurate(Integer clusterId,
					List<YarnQueueConfInfo> infos) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result getYarnQueueConfigurate(Integer clusterId) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result deleteYarnQueueConfigurate(Integer clusterId,
					List<YarnQueueConfInfo> infos) {
				return Result.failed("调用代理服务失败");
			}

			@Override
			public Result insertYarnQueueConfigurate(Integer clusterId,
					List<YarnQueueConfInfo> infos) {
				return Result.failed("调用代理服务失败");
			}

		};
	}

}
