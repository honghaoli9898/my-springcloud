package com.seaboxdata.sdps.file.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.file.model.FileInfo;
import com.seaboxdata.sdps.file.service.IFileService;
import com.seaboxdata.sdps.oss.properties.HadoopProperties;
import com.seaboxdata.sdps.oss.utils.FileUtil;

/**
 * 文件上传
 *
 * @author 作者 owen E-mail: 624191343@qq.com
 */
@RestController
@RequestMapping("/fc10")
public class FileController {
	@Resource
	private IFileService fileService;
	@Autowired
	HadoopProperties hadoopProperties;

	/**
	 * 文件上传 根据fileType选择上传方式
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/fc1001")
	public FileInfo upload(@RequestPart("file") MultipartFile file,
			@RequestParam String userId) throws Exception {
		String path = FileUtil.getUploadPath(
				hadoopProperties.getDefaultUploadPath(), userId);
		return fileService.upload(file, path);
	}

	/**
	 * 文件删除
	 *
	 * @param id
	 */
	@DeleteMapping("/files/{id}")
	public Result delete(@PathVariable String id) {
		try {
			fileService.delete(id);
			return Result.succeed("操作成功");
		} catch (Exception ex) {
			return Result.failed("操作失败");
		}
	}

	/**
	 * 文件查询
	 *
	 * @param params
	 * @return
	 */
	@GetMapping("/files")
	public PageResult<FileInfo> findFiles(
			@RequestParam Map<String, Object> params) {
		return fileService.findList(params);
	}
}
