package com.seaboxdata.sdps.usersync.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.hutool.core.util.StrUtil;

import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.Base64Util;
import com.seaboxdata.sdps.common.core.utils.KeytabUtil;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.usersync.feign.BigDataCommonProxyFeign;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerKeytabMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.usersync.service.IKeytabService;

@Slf4j
@Service
public class KeytabServiceImpl implements IKeytabService {
	@Value("${security.kerberos.login.keytabPath}")
	private String sourceKeytabPath;
	@Value("${security.kerberos.login.krb5}")
	private String krb5Path;
	@Autowired
	private SdpsClusterMapper clusterMapper;
	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;
	@Autowired
	private SysGlobalArgsMapper globalArgsMapper;
	@Autowired
	private SdpsServerKeytabMapper keytabMapper;
	@Autowired
	private KerberosProperties kerberosProperties;
	@Autowired
	private BigDataCommonProxyFeign bigDataCommonProxyFeign;

	@Override
	public String getKeytab(Integer clusterId, String keytabFileName)
			throws Exception {
		log.info("获取 keytab clusterId:{} keytabName:{}", clusterId,
				keytabFileName);
		return getBase64ByFile(sourceKeytabPath, keytabFileName, clusterId);
	}

	/**
	 * 根据集群 获取krb5文件
	 *
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	public String getKrb5(Integer clusterId) throws Exception {
		log.info("获取 krb5 clusterId:{}", clusterId);
		String krb5ParentPath = krb5Path
				.substring(0, krb5Path.lastIndexOf("/"));
		String krb5Name = krb5Path.substring(krb5Path.lastIndexOf("/") + 1);
		return getBase64ByFile(krb5ParentPath, krb5Name, clusterId);
	}

	@Override
	public Page<SdpServerKeytab> selectKeytab(Integer clusterId,
			String keytabName, String principalType, Integer pageNo,
			Integer pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<String> list = new ArrayList<>();
		if (StringUtils.isNotBlank(principalType)) {
			list.add(principalType);
			// 如果是用户类型，则添加USER类型查询
			if ("SDPS".equals(principalType)) {
				list.add("USER");
			}
		}
		return keytabMapper.selectKeytab(clusterId, keytabName, list);
	}

	/**
	 * 根据文件名和目录读取文件转为base64字符串
	 *
	 * @param parentPath
	 *            文件路径
	 * @param fileName
	 *            文件
	 * @param clusterId
	 *            集群id
	 * @return
	 * @throws Exception
	 */
	public String getBase64ByFile(String parentPath, String fileName,
			Integer clusterId) throws Exception {
		String tmpFile = "";
		try {
			log.info("获取文件 parentPath:{} fileName:{}", parentPath, fileName);
			// 拼接keytab文件路径
			String filePath = parentPath.concat("/") + fileName;
			// SdpsServerInfo serverInfo = getServerInfo(clusterId, fileName,
			// isKeytab);
			// 获取集群对应的kdc所在地址信息
			SdpsCluster sdpsCluster = clusterMapper
					.selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
							clusterId));
			SdpsServerInfo serverInfo = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>().eq(
							"server_id", sdpsCluster.getServerId()).eq("type",
							ServerTypeEnum.KDC.name()));

			SysGlobalArgs sysGlobalArgs = globalArgsMapper
					.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
							"password").eq("arg_key", "privateKey"));
			String sourcePassword = RsaUtil.decrypt(serverInfo.getPasswd(),
					sysGlobalArgs.getArgValue());

			// 如果端口为空或为0，则赋值默认端口22
			String port = serverInfo.getPort();
			if (StringUtils.isBlank(port) || "0".equals(port)) {
				port = "22";
			}
			// scp到本地的/tmp目录
			RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(
					serverInfo.getHost(), serverInfo.getUser(), sourcePassword,
					Integer.valueOf(port));
			boolean isSuccessed = remoteShellExecutorUtil.sftpDownload(
					filePath, "/tmp");
			// 如果状态是false，说明执行异常
			if (!isSuccessed) {
				throw new Exception("获取文件报错 parentPath:" + parentPath
						+ " fileName:" + fileName);
			}
			// 读取keytab文件转为base64字符串
			tmpFile = "/tmp/" + fileName;
			return Base64Util.convertFileToStr(tmpFile);
		} catch (Exception e) {
			log.error("获取文件报错：parentPath:{} fileName:{}", parentPath, fileName,
					e);
			throw new Exception(e.getMessage());
		} finally {
			// 删除临时文件
			FileUtils.deleteQuietly(new File(tmpFile));
		}
	}

	@Override
	public void checkKeytab(List<String> keytabs, List<Integer> clusterIds)
			throws Exception {

	}

	@Override
	public void updateKeytabs(List<SdpServerKeytab> list) {
		KeytabUtil.updateKeytabs(list);
	}

	@Override
	public Map<String, Object> pullKeytabFromKdc(List<String> pathList) {
		// 获取集群对应的kdc所在地址信息
		SdpsServerInfo serverInfo = serverInfoMapper
				.selectOne(new QueryWrapper<SdpsServerInfo>().eq("type",
						ServerTypeEnum.KDC.name()));
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		String pass = RsaUtil.decrypt(serverInfo.getPasswd(),
				sysGlobalArgs.getArgValue());
		RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(
				serverInfo.getHost(), serverInfo.getUser(), pass,
				Integer.valueOf(serverInfo.getPort()));
		//检查目录是否存在，不存在则创建
		File file = new File(kerberosProperties.getUserSyncKeytabPath());
		file.mkdirs();
		remoteShellExecutorUtil.sftpDownload(
				pathList.toArray(new String[pathList.size()]),
				kerberosProperties.getUserSyncKeytabPath());
		Map<String, Object> map = Maps.newHashMap();
		pathList.forEach(keytabPath -> {
			List<String> arr = StrUtil.split(keytabPath, '/');
			String userSyncKeytabPath = kerberosProperties.getUserSyncKeytabPath().concat("/")
					.concat(arr.get(arr.size() - 1));
			String kebtabStr = Base64Util.convertFileToStr(userSyncKeytabPath);
			map.put(keytabPath, kebtabStr);
		});
		return map;
	}


	/**
	 * 根据集群和keytab文件名获取keytab所在服务
	 *
	 * @param clusterId
	 *            集群id
	 * @param fileName
	 *            文件名
	 * @param isKeytab
	 *            是否是keytab文件
	 * @return
	 */
	// private SdpsServerInfo getServerInfo(Integer clusterId, String fileName,
	// Boolean isKeytab) throws Exception {
	// SdpsServerInfo serverInfo = null;
	// SdpsCluster sdpsCluster = clusterMapper
	// .selectOne(new QueryWrapper<SdpsCluster>().eq("cluster_id",
	// clusterId));
	// //判断是否是服务的keytab文件
	// if (KeytabServerEnum.getKeytabServerList().contains(fileName) &&
	// isKeytab) {
	// Result serverResult =
	// bigDataCommonProxyFeign.getComponentAndHost(clusterId,
	// KeytabServerEnum.getServiceNameByKeytabName(fileName));
	// if (serverResult == null || serverResult.isFailed()) {
	// throw new Exception("获取服务所在机器报错");
	// }
	// LinkedHashMap<String, List> data = (LinkedHashMap<String, List>)
	// serverResult.getData();
	//
	// String ip = null;
	//
	// serv: for (String server : data.keySet()) {
	// List<LinkedHashMap<String, Object>> params = (List<LinkedHashMap<String,
	// Object>>) data.get(server);
	// for (int i = 0;i<params.size();i++) {
	// LinkedHashMap<String, Object> param = params.get(i);
	// if (param.containsKey("ip")) {
	// ip = param.get("ip").toString();
	// break serv;
	// }
	// }
	// }
	// serverInfo = serverInfoMapper.selectOne(new
	// QueryWrapper<SdpsServerInfo>().eq("server_id", sdpsCluster.getServerId())
	// .eq("host", ip));
	//
	// } else {
	// //获取集群对应的kdc所在地址信息
	// serverInfo = serverInfoMapper
	// .selectOne(new QueryWrapper<SdpsServerInfo>().eq(
	// "server_id", sdpsCluster.getServerId()).eq(
	// "type", ServerTypeEnum.KDC.name()));
	// }
	// return serverInfo;
	// }
}
