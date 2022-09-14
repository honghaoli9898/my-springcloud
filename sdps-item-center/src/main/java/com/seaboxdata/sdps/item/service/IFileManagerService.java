package com.seaboxdata.sdps.item.service;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.model.SdpsFileManager;
import com.seaboxdata.sdps.item.vo.script.ScriptRequest;

public interface IFileManagerService extends IService<SdpsFileManager> {

	public Object uploadFile(SysUser sysUser, Integer clusterId, Long itemId,
			MultipartFile file, String belong, String serverType);

	public void downloadFile(Long fileId);

	public void removeFile(Long fileId, String string);

	public Page<SdpsFileManager> pageList(SysUser sysUser, PageRequest<ScriptRequest> request);

}
