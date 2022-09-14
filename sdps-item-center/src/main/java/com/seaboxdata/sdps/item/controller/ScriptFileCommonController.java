package com.seaboxdata.sdps.item.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import com.seaboxdata.sdps.item.model.SdpsFileManager;
import com.seaboxdata.sdps.item.service.IFileManagerService;
import com.seaboxdata.sdps.item.vo.script.ScriptRequest;

@Slf4j
@RestController
@RequestMapping("/scriptManager")
public class ScriptFileCommonController {
	@Autowired
	private IFileManagerService fileManagerService;

	@PostMapping("/upload")
	public Result<Object> uploadFile(@LoginUser SysUser sysUser,
			@RequestParam("clusterId") Integer clusterId,
			@RequestParam(value="itemId",required=false) Long itemId,
			@RequestPart("file") MultipartFile file,
			@RequestParam("belong") String belong,
			@RequestParam("serverType") String serverType) {
		if (file.isEmpty()) {
			return Result.failed("文件不能为空，请选择文件.");
		}
		Object result = fileManagerService.uploadFile(sysUser, clusterId,
				itemId, file, belong, serverType);
		return Result.succeed(result, "操作成功");
	}

	@GetMapping("/download/{id}")
	public void downloadFile(@PathVariable("id") Long id) {
		fileManagerService.downloadFile(id);
	}

	@GetMapping("/remove/{id}")
	public Result removeFile(@LoginUser SysUser sysUser,@PathVariable("id") Long id) {
		fileManagerService.removeFile(id,sysUser.getUsername());
		return Result.succeed("操作成功");
	}

	@PostMapping("/pageList")
	public PageResult<SdpsFileManager> pageList(@LoginUser SysUser sysUser,
			@RequestBody PageRequest<ScriptRequest> request) {
		try {
			Page<SdpsFileManager> result = fileManagerService.pageList(sysUser,
					request);
			return PageResult.<SdpsFileManager> builder().code(0)
					.count(result.getTotal()).data(result.getResult())
					.msg("操作成功").build();
		} catch (Exception e) {
			log.error("操作失败", e);
			return PageResult.<SdpsFileManager> builder().code(1).msg("操作失败")
					.build();
		}
	}

}
