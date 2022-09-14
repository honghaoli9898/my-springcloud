package com.seaboxdata.sdps.item.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsItem;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.item.dto.item.ItemDto;
import com.seaboxdata.sdps.item.service.IClusterDevOpsService;
import com.seaboxdata.sdps.item.service.IItemService;
import com.seaboxdata.sdps.item.vo.item.DataHistogramRequest;

@RestController
@RequestMapping("/devOps")
public class ClusterDevOpsController {
	@Autowired
	private IClusterDevOpsService clusterevOpsService;
	@Autowired
	private IItemService itemService;

	/**
	 * 查询集群总数
	 * 
	 * @return
	 */
	@GetMapping("/cluster/count")
	public Result<JSONObject> getClusterCnt() {
		long total = clusterevOpsService.count(new QueryWrapper<SdpsCluster>()
				.eq("is_use", 1));
		long remoteClusterTotal = clusterevOpsService
				.count(new QueryWrapper<SdpsCluster>().eq("is_use", 1)
						.isNotNull("remote_url"));
		JSONObject result = new JSONObject();
		result.put("total", total);
		result.put("remoteClusterTotal", remoteClusterTotal);
		return Result.succeed(result, "操作成功");
	}

	/**
	 * 查询项目总数
	 * 
	 * @return
	 */
	@GetMapping("/item/count")
	public Result<Long> getItemCnt() {
		long count = itemService.count(new QueryWrapper<SdpsItem>().eq(
				"enabled", 1));
		return Result.succeed(count, "操作成功");
	}

	/**
	 * 查询当前登录用户所包含的项目
	 * 
	 * @return
	 */
	@GetMapping("/item/info")
	public Result<List<ItemDto>> getItemInfo(@LoginUser SysUser sysUser) {
		List<ItemDto> result = itemService
				.selectCurrentUserHasItemInfo(sysUser);
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/item/rankInfo")
	public Result<JSONObject> getItemRankInfo() {
		JSONObject result = itemService.selectItemRankInfo();
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/data/rankInfo")
	public Result<JSONObject> getDataRankInfo() {
		JSONObject result = itemService.selectDataRankInfo();
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/data/histogram")
	public Result<JSONObject> getHistogram(DataHistogramRequest request) {
		try {
			JSONObject result = itemService.getHistogram(request);
			return Result.succeed(result, "操作成功");
		} catch (Exception e) {
			return Result.failed(null, "操作失败");
		}
	}

	/**
	 * 查询当前登录用户集群信息
	 *
	 * @return
	 */
	/*
	 * @GetMapping("/v1/clusterInfo") public Result<List<ItemDto>>
	 * getClusterInfo(@LoginUser SysUser sysUser) { //List<ItemDto> result=
	 * clusterevOpsService.getClusterInfo(sysUser); return
	 * Result.succeed(result, "操作成功"); }
	 */

}
