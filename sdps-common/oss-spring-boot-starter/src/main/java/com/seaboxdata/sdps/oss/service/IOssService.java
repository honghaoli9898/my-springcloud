package com.seaboxdata.sdps.oss.service;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.web.multipart.MultipartFile;

import com.seaboxdata.sdps.oss.model.ObjectInfo;

/**
 * @author zlt
 * @date 2021/2/9
 * <p>
 * Blog: https://zlt2000.gitee.io
 * Github: https://github.com/zlt2000
 */
public interface IOssService {
    /**
     * 上传对象
     * @param objectName 对象名
     * @param is 对象流
     */
    ObjectInfo upload(String objectName, InputStream is);

    /**
     * 上传对象
     * @param file 对象
     */
    ObjectInfo upload(MultipartFile file);

    /**
     * 删除对象
     * @param objectKey 对象标识
     */
    void delete(String objectKey);

    /**
     * 查看文件
     * @param objectPath 对象路径
     * @param os 输出流
     */
    void view(String objectPath, OutputStream os);
}
