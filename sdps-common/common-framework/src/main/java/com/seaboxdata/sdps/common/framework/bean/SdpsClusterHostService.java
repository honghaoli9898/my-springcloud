package com.seaboxdata.sdps.common.framework.bean;

import com.seaboxdata.sdps.common.core.model.SuperEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 集群主机和服务传输类
 *
 * @author jiaohongtao
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpsClusterHostService extends SuperEntity<SdpsClusterHostService> {
	private static final long serialVersionUID = 1421965143798651449L;
	/** 名称 */
	private SdpsClusterHost host;
	/** 集群ID */
	private List<SdpsClusterService> services;
	/** 类型 master/slave */
	private String type;
}