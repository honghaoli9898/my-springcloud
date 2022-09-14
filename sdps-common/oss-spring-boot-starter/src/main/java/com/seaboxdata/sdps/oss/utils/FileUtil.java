package com.seaboxdata.sdps.oss.utils;

import org.apache.hadoop.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * 文件工具类
 *
 * @author Ruison on 2019/7/4 - 14:56
 */
public class FileUtil {
	/**
	 * 文件写入输入流
	 * 
	 * @param in
	 *            输入流
	 * @param file
	 *            文件
	 */
	public static void inputStreamToFile(InputStream in, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead;
			byte[] buffer = new byte[8192];
			while ((bytesRead = in.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			IOUtils.cleanupWithLogger(null,os, in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MultipartFile 转 File
	 *
	 * @param file
	 *            上传的文件
	 * @return {@link File}
	 */
	public static File MultipartFileToFile(MultipartFile file) {
		File f = null;
		try {
			if (file != null && file.getSize() > 0) {
				InputStream in = file.getInputStream();
				f = new File(IdUtil.fastSimpleUUID()+"/"+Objects.requireNonNull(file.getOriginalFilename()));
				inputStreamToFile(in, f);
			}
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			return f;
		}
	}

	public static String getUploadPath(String defaultUploadPath, String userId) {
		if (defaultUploadPath.endsWith("/")) {
			return defaultUploadPath + userId
					+ DateUtil.format(DateUtil.date(), "yyyyMMdd") + "/"
					+ IdUtil.fastSimpleUUID() + "/";
		}
		return defaultUploadPath + "/" + userId
				+ DateUtil.format(DateUtil.date(), "yyyyMMdd") + "/"
				+ IdUtil.fastSimpleUUID() + "/";
	}

}
