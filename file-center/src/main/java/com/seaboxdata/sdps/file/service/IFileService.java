package com.seaboxdata.sdps.file.service;

import java.io.OutputStream;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.file.model.FileInfo;

/**
 * 文件service
 *
 * @author 作者 owen E-mail: 624191343@qq.com
 */
public interface IFileService extends IService<FileInfo> {
	FileInfo upload(MultipartFile file, String... args) throws Exception;

	PageResult<FileInfo> findList(Map<String, Object> params);

	void delete(String id);

	void out(String id, OutputStream os);
}
