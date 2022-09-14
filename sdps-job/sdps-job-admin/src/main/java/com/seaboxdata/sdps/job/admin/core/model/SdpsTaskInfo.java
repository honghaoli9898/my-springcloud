package com.seaboxdata.sdps.job.admin.core.model;

import java.io.Serializable;
import java.util.Date;

public class SdpsTaskInfo implements Serializable {

	private Long id;

	private Integer clusterId;

	private String clusterName;

	private Long submitId;

	private String userName;

	private String yarnAppId;

	private String yarnAppName;

	private String yarnQueue;

	private String yarnTrackingUrl;

	private String applicationType;

	private Long xxlLogId;

	private Long xxlJobId;

	private String shellPath;

	private String shellContext;

	private Date createTime;

	private Date updateTime;

	private String ext0;

	private String ext1;

	private String ext2;

	private String ext3;

	private String ext4;

	private static final long serialVersionUID = 1L;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSubmitId() {
		return submitId;
	}

	public void setSubmitId(Long submitId) {
		this.submitId = submitId;
	}

	public Integer getClusterId() {
		return clusterId;
	}

	public void setClusterId(Integer clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getYarnAppId() {
		return yarnAppId;
	}

	public void setYarnAppId(String yarnAppId) {
		this.yarnAppId = yarnAppId;
	}

	public String getYarnAppName() {
		return yarnAppName;
	}

	public void setYarnAppName(String yarnAppName) {
		this.yarnAppName = yarnAppName;
	}

	public String getYarnQueue() {
		return yarnQueue;
	}

	public void setYarnQueue(String yarnQueue) {
		this.yarnQueue = yarnQueue;
	}

	public String getYarnTrackingUrl() {
		return yarnTrackingUrl;
	}

	public void setYarnTrackingUrl(String yarnTrackingUrl) {
		this.yarnTrackingUrl = yarnTrackingUrl;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public Long getXxlLogId() {
		return xxlLogId;
	}

	public void setXxlLogId(Long xxlLogId) {
		this.xxlLogId = xxlLogId;
	}

	public Long getXxlJobId() {
		return xxlJobId;
	}

	public void setXxlJobId(Long xxlJobId) {
		this.xxlJobId = xxlJobId;
	}

	public String getShellPath() {
		return shellPath;
	}

	public void setShellPath(String shellPath) {
		this.shellPath = shellPath;
	}

	public String getShellContext() {
		return shellContext;
	}

	public void setShellContext(String shellContext) {
		this.shellContext = shellContext;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getExt0() {
		return ext0;
	}

	public void setExt0(String ext0) {
		this.ext0 = ext0;
	}

	public String getExt1() {
		return ext1;
	}

	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}

	public String getExt2() {
		return ext2;
	}

	public void setExt2(String ext2) {
		this.ext2 = ext2;
	}

	public String getExt3() {
		return ext3;
	}

	public void setExt3(String ext3) {
		this.ext3 = ext3;
	}

	public String getExt4() {
		return ext4;
	}

	public void setExt4(String ext4) {
		this.ext4 = ext4;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}