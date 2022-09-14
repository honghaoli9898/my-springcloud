package com.seaboxdata.sdps.common.core.utils;

import com.seaboxdata.sdps.common.core.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Base64;

public class Base64Util {

    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();

    /**
     * 将文件转为base64字符串
     *
     * @param path 文件路径
     * @return
     */
    public static String convertFileToStr(String path) {
        if (org.springframework.util.StringUtils.isEmpty(path)) {
            throw BusinessException.NULL_PARAM_EXCEPTION;
        }
        File file = new File(path);
        if (!file.exists()) {
            throw BusinessException.FILE_NOT_EXIST_EXCEPTION;
        }

        try (FileInputStream inputFile = new FileInputStream(path);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            int len = 0;
            byte[] b = new byte[2048];
            while ((len = inputFile.read(b, 0, b.length)) != -1) {
                byteArrayOutputStream.write(b, 0, len);
            }
            byte[] buffer = byteArrayOutputStream.toByteArray();
            String base64Str = base64Encoder.encodeToString(buffer);
            return base64Str;
        } catch (Exception e) {
            throw BusinessException.BASE64_CONVERT_EXCEPTION;
        }
    }

    /**
     * 将base64转换为文件写到本地
     *
     * @param base64Str 文件编码的字符串
     * @param filePath  输出的文件路径
     */
    public static void convertStrToFile(String base64Str, String filePath) {
        if (org.springframework.util.StringUtils.isEmpty(filePath) || StringUtils.isEmpty(base64Str)) {
            throw BusinessException.NULL_PARAM_EXCEPTION;
        }
        //判断文件目录是否存在,没有则创建
        String parentDir = filePath.substring(0, filePath.lastIndexOf("/"));
        File dir = new File(parentDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        byte[] fileIO = base64Decoder.decode(base64Str);
        try (ByteArrayInputStream in = new ByteArrayInputStream(fileIO);
             FileOutputStream out = new FileOutputStream(filePath);) {
            byte[] buffer = new byte[1024];
            int byteRead = 0;
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
            }
            out.flush();
        } catch (Exception e) {
            throw BusinessException.BASE64_CONVERT_EXCEPTION;
        }
    }
}
