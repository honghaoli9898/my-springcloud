package com.seaboxdata.sdps.bigdataProxy.controller;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;

/**
 * @author: Denny
 * @date: 2021/10/28 10:12
 * @desc:
 */
@Slf4j
@RestController
@RequestMapping("/ambariCommon")
public class AmbariCommonController implements Serializable {

	private static final long serialVersionUID = -2039901524245118663L;


    @Autowired
    private SdpsServerInfoMapper sdpsServerInfoMapper;


    /**
     * 根据clusterId查询ambari-server节点信息。
     * @param clusterId 集群id
     * @return ambari-server的IP和端口。
     */
    @GetMapping("selectAmbariInfo")
    public SdpsServerInfo selectAmbariInfo(@RequestParam("clusterId") Integer clusterId, @RequestParam(name = "port", defaultValue = "8088",required = false) Integer port) {
        if(clusterId == null) {
            return null;
        }
        log.info("clusterId:{},port:{}",clusterId, port);
        return sdpsServerInfoMapper.selectYarnInfoById(clusterId, port);
    }
}
