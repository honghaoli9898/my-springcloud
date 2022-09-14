package com.seaboxdata.sdps.job.admin.core.model;

import java.io.Serializable;
import java.util.Date;

public class SdpsCluster implements Serializable {

    private Integer clusterId;

    private String clusterPasswd;

    private String clusterName;

    private String clusterShowName;

    private Integer storageResourceId;

    private Integer calcResourceId;

    private Date createTime;

    private Integer clusterStatusId;

    private String clusterSource;

    private Integer clusterTypeId;

    private Long principalId;

    private Boolean isUse;

    private String clusterIp;

    private Integer clusterPort;

    private String clusterAccount;

    private String clusterDescription;

    private String clusterHostConf;

    private String clusterConfSavePath;

    private Integer serverId;

    private Long createrId;

    private Long menderId;

    private String remoteUrl;

    private Date updateTime;

    private Boolean isRunning;

    private static final long serialVersionUID = 1L;

	public Integer getClusterId() {
		return clusterId;
	}

	public void setClusterId(Integer clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterPasswd() {
		return clusterPasswd;
	}

	public void setClusterPasswd(String clusterPasswd) {
		this.clusterPasswd = clusterPasswd;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getClusterShowName() {
		return clusterShowName;
	}

	public void setClusterShowName(String clusterShowName) {
		this.clusterShowName = clusterShowName;
	}

	public Integer getStorageResourceId() {
		return storageResourceId;
	}

	public void setStorageResourceId(Integer storageResourceId) {
		this.storageResourceId = storageResourceId;
	}

	public Integer getCalcResourceId() {
		return calcResourceId;
	}

	public void setCalcResourceId(Integer calcResourceId) {
		this.calcResourceId = calcResourceId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getClusterStatusId() {
		return clusterStatusId;
	}

	public void setClusterStatusId(Integer clusterStatusId) {
		this.clusterStatusId = clusterStatusId;
	}

	public String getClusterSource() {
		return clusterSource;
	}

	public void setClusterSource(String clusterSource) {
		this.clusterSource = clusterSource;
	}

	public Integer getClusterTypeId() {
		return clusterTypeId;
	}

	public void setClusterTypeId(Integer clusterTypeId) {
		this.clusterTypeId = clusterTypeId;
	}

	public Long getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(Long principalId) {
		this.principalId = principalId;
	}

	public Boolean getIsUse() {
		return isUse;
	}

	public void setIsUse(Boolean isUse) {
		this.isUse = isUse;
	}

	public String getClusterIp() {
		return clusterIp;
	}

	public void setClusterIp(String clusterIp) {
		this.clusterIp = clusterIp;
	}

	public Integer getClusterPort() {
		return clusterPort;
	}

	public void setClusterPort(Integer clusterPort) {
		this.clusterPort = clusterPort;
	}

	public String getClusterAccount() {
		return clusterAccount;
	}

	public void setClusterAccount(String clusterAccount) {
		this.clusterAccount = clusterAccount;
	}

	public String getClusterDescription() {
		return clusterDescription;
	}

	public void setClusterDescription(String clusterDescription) {
		this.clusterDescription = clusterDescription;
	}

	public String getClusterHostConf() {
		return clusterHostConf;
	}

	public void setClusterHostConf(String clusterHostConf) {
		this.clusterHostConf = clusterHostConf;
	}

	public String getClusterConfSavePath() {
		return clusterConfSavePath;
	}

	public void setClusterConfSavePath(String clusterConfSavePath) {
		this.clusterConfSavePath = clusterConfSavePath;
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public Long getCreaterId() {
		return createrId;
	}

	public void setCreaterId(Long createrId) {
		this.createrId = createrId;
	}

	public Long getMenderId() {
		return menderId;
	}

	public void setMenderId(Long menderId) {
		this.menderId = menderId;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Boolean getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}