package com.seaboxdata.sdps.item.service.impl;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.feign.UserService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.item.feign.BigDataCommonProxyFeign;
import com.seaboxdata.sdps.item.service.FileService;
import com.seaboxdata.sdps.item.utils.CallApiUtil;
@Slf4j
@Service("sdo")
public class SdoFileService implements FileService {
	@Autowired
	private UserService userService;
	@Autowired
	@Qualifier("ItemRestTemplate")
	private RestTemplate restTemplate;
	@Autowired
	private BigDataCommonProxyFeign bigDataCommonProxyFeign;

	@Override
	public Object upload(MultipartFile file, SdpsServerInfo sdpsServerInfo,
			Integer clusterId, String... args) {
		String dest = StrUtil.replaceIgnoreCase(args[0], "hdfs://", "");
		log.info("保存文件:{}",dest);
		Result result = bigDataCommonProxyFeign.uploadScripFile(args[1],clusterId, file, dest, true);
		log.info("调取大数据代理结果:{}",result);
		if(result.isFailed()){
			throw new BusinessException("上传文件失败:".concat(result.getMsg()));
		}
//		Result<Map<String, String>> result = userService.serverLogin(
//				clusterId.toString(), sdpsServerInfo.getType(),
//				sdpsServerInfo.getUser(),true);
//		if (result.isFailed()) {
//			throw new BusinessException("组件登录失败");
//		}
//		Map<String, String> certData = result.getData();
//		HttpHeaders headers = new HttpHeaders();
//		certData.forEach((k, v) -> {
//			headers.add(k, v);
//		});
//		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//		Result<SysGlobalArgs> sysGlobalArgs = bigDataCommonProxyFeign
//				.getGlobalParam("sdo", "uploadFile");
//		if (sysGlobalArgs.isFailed()) {
//			throw new BusinessException("获取url信息失败");
//		}
//		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
//		Resource resource = null;
//		try {
//			resource = new CommonInputStreamResource(file.getInputStream(),
//					file.getOriginalFilename());
//		} catch (IOException e) {
//			log.error("读取流失败", e);
//			throw new BusinessException("读取流失败");
//		}
//		parts.add("hdfs_file", resource);
//		parts.add("dest", dest);
//		Map<Integer, Object> param = MapUtil.newHashMap();
//		param.put(1, dest);
//		JSONObject resultData = CallApiUtil.getApi(restTemplate, headers,
//				CallApiUtil.getUrl(sysGlobalArgs.getData().getArgValue(),
//						sdpsServerInfo.getHost(), sdpsServerInfo.getPort()),
//				sysGlobalArgs.getData().getArgValueDesc(), parts, param);
//		if(!Objects.equal(0,resultData.getInteger("status"))){
//			throw new BusinessException("上传文件报错:"+resultData.toJSONString());
//		}
		return result.getData();
	}

	@Override
	public void download(String path, SdpsServerInfo sdpsServerInfo,
			Integer clusterId, String serverTypem) {
		String dest = StrUtil.replaceIgnoreCase(path, "hdfs://", "");
		Result<Map<String, String>> result = userService.serverLogin(
				clusterId.toString(), sdpsServerInfo.getType(),
				sdpsServerInfo.getUser(),true);
		if (result.isFailed()) {
			throw new BusinessException("组件登录失败");
		}
		Map<String, String> certData = result.getData();
		HttpHeaders headers = new HttpHeaders();
		certData.forEach((k, v) -> {
			headers.add(k, v);
		});
		headers.add(
				"Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		Result<SysGlobalArgs> sysGlobalArgs = bigDataCommonProxyFeign
				.getGlobalParam("sdo", "downloadFile");
		if (sysGlobalArgs.isFailed()) {
			throw new BusinessException("获取url信息失败");
		}
		Map<Integer, Object> param = MapUtil.newHashMap();
		param.put(1, dest);
		CallApiUtil.download(restTemplate, headers, CallApiUtil.getUrl(
				sysGlobalArgs.getData().getArgValue(),
				sdpsServerInfo.getHost(), sdpsServerInfo.getPort()),
				sysGlobalArgs.getData().getArgValueDesc(), param, dest);
	}

	@Override
	public void delete(String path, SdpsServerInfo sdpsServerInfo,
			Integer clusterId, String serverType,String username) {
		String dest = StrUtil.replaceIgnoreCase(path, "hdfs://", "");
		Result result = bigDataCommonProxyFeign.deleteScriptFile(username, clusterId, CollUtil.newArrayList(dest), true);
		log.info("获取删除文件返回:{}",result);
		if(result.isFailed()){
			JSONObject data = new JSONObject((Map<String, Object>) result.getData());
			if(data.getString("message").contains("not exist")){
				return;
			}
			throw new BusinessException("删除文件失败:".concat(result.getMsg()));
		}
//		Result<Map<String, String>> result = userService.serverLogin(
//				clusterId.toString(), sdpsServerInfo.getType(),
//				sdpsServerInfo.getUser(),true);
//		if (result.isFailed()) {
//			throw new BusinessException("组件登录失败");
//		}
//		Map<String, String> certData = result.getData();
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//		headers.add("X-Requested-With", "XMLHttpRequest");
//		certData.forEach((k, v) -> {
//			headers.add(k, v);
//		});
//		Result<SysGlobalArgs> sysGlobalArgs = bigDataCommonProxyFeign
//				.getGlobalParam("sdo", "removeFile");
//		if (sysGlobalArgs.isFailed()) {
//			throw new BusinessException("获取url信息失败");
//		}
//		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
//		parts.add("path", dest);
//		Map<Integer, Object> param = MapUtil.newHashMap();
//		param.put(1, "true");
//		param.put(2, dest.substring(0, dest.lastIndexOf("/")));
//		CallApiUtil.getApi(restTemplate, headers, CallApiUtil.getUrl(
//				sysGlobalArgs.getData().getArgValue(),
//				sdpsServerInfo.getHost(), sdpsServerInfo.getPort()),
//				sysGlobalArgs.getData().getArgValueDesc(), parts, param);
	}

}
