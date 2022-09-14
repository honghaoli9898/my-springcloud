package com.seaboxdata.sdps.seaboxProxy.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.map.MapUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpServerKeytab;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.KeytabUtil;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.service.ISeaboxKeytabService;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;

@Slf4j
@RestController
@RequestMapping("/seaboxKeytab")
public class SeaboxKeytabController {
	@Autowired
	ISeaboxKeytabService keytabService;

	@Autowired
	BigdataCommonFegin bigdataCommonFegin;

    @Autowired
    private KerberosProperties kerberosProperties;

	/**
	 * 检查keytab的有效性
	 *
	 * @param keytabs
	 *            keytab文件名
	 * @return
	 */
	@GetMapping("/checkKeytab")
	public Result checkKeytab(@RequestParam("keytabs") List<String> keytabs,
			@RequestParam("clusterIds") List<Integer> clusterIds) {
		try {
			keytabService.checkKeytab(keytabs, clusterIds);
			return Result.succeed("成功");
		} catch (Exception e) {
			log.error("检查keytab文件有效性失败", e);
			return Result.failed(e.getMessage());
		}
	}

    /**
     * 更新keytab文件
     *
     * @param list
     * @return
     */
    @PostMapping("/updateKeytab")
    public Result updateKeytab(@RequestBody List<SdpServerKeytab> list) {
        try {
            keytabService.updateKeytabs(list);
            return Result.succeed("成功");
        } catch (Exception e) {
            log.error("更新keytab失败", e);
            return Result.failed(e.getMessage());
        }
    }

    /**
     * 获取服务keytab列表
     *
     * @param list
     * @return
     */
    @GetMapping("/findServerKerberosInfo")
    public Result<JSONObject> findServerKerberosInfo(
            @RequestParam("clusterId") Integer clusterId) {
        try {
            AmbariUtil ambariUtil = new AmbariUtil(clusterId);
            ambariUtil.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            ambariUtil.getHeaders().set("X-Http-Method-Override", "GET");
            Result<SysGlobalArgs> args = bigdataCommonFegin.getGlobalParam(
                    "ambari", "ip");
            JSONObject result = ambariUtil.getAmbariApi(args.getData()
                            .getArgValue(), args.getData().getArgValueDesc(),
                    SeaBoxPlatformController.getClusterNameParam(ambariUtil));
            JSONArray items = result.getJSONArray("items");
            Map<String, String> ipHostMap = MapUtil.newHashMap();
            items.forEach(item -> {
                JSONObject jsonObj = (JSONObject) item;
                JSONObject host = jsonObj.getJSONObject("Hosts");
                String ip = host.getString("ip");
                String hostName = host.getString("host_name");
                ipHostMap.put(hostName, ip);
            });
            args = bigdataCommonFegin.getGlobalParam("ambari",
                    "downloadKerberosCsv");
            ambariUtil.getHeaders().remove("X-Http-Method-Override");
            String resultStr = ambariUtil.getAmbariApiString(args.getData()
                            .getArgValue(), args.getData().getArgValueDesc(),
                    SeaBoxPlatformController.getClusterNameParam(ambariUtil));
            return AmbariUtil.analysisServerKerberosInfo(resultStr, ipHostMap,
                    clusterId);
        } catch (Exception e) {
            log.error("获取服务keytab列表失败", e);
            return Result.failed(new JSONObject(), "获取服务keytab列表失败");
        }
    }

    @GetMapping("/downloadKrb5")
    public ResponseEntity<byte[]> downloadKrb5(){
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();
        try {
            String krb5File = kerberosProperties.getKrb5();
            return bodyBuilder.body(FileUtils.readFileToByteArray(new File(krb5File)));
        }catch (Exception e){
            log.error("下载krb5文件失败:",e);
            return ResponseEntity.badRequest().body(new byte[]{});
        }

    }
}
