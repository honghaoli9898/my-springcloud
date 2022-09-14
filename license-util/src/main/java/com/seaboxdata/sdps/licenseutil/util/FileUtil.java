package com.seaboxdata.sdps.licenseutil.util;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.licenseutil.exception.license.BusinessException;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件操作工具类
 */
@Slf4j
public class FileUtil{
    /**
     * 解压文件
     *
     * @param sourcefiles zip文件路径
     * @param decompreDirectory 解压后的目录
     * @throws IOException
     */
    public static void unZip(String sourcefiles, String decompreDirectory) throws IOException {
        ZipFile readfile = null;
        try {
            readfile = new ZipFile(sourcefiles);
            Enumeration takeentrie = readfile.getEntries();
            ZipEntry zipEntry = null;
            File credirectory = new File(decompreDirectory);
            credirectory.mkdirs();
            while (takeentrie.hasMoreElements()) {
                zipEntry = (ZipEntry) takeentrie.nextElement();
                String entryName = zipEntry.getName();
                try (InputStream in = readfile.getInputStream(zipEntry)) {
                    if (zipEntry.isDirectory()) {
                        String name = zipEntry.getName();
                        name = name.substring(0, name.length() - 1);
                        File createDirectory = new File(decompreDirectory + File.separator + name);
                        createDirectory.mkdirs();
                    } else {
                        int index = entryName.lastIndexOf("\\");
                        if (index != -1) {
                            File createDirectory = new File(
                                    decompreDirectory + File.separator + entryName.substring(0, index));
                            createDirectory.mkdirs();
                        }
                        index = entryName.lastIndexOf("/");
                        if (index != -1) {
                            File createDirectory = new File(
                                    decompreDirectory + File.separator + entryName.substring(0, index));
                            createDirectory.mkdirs();
                        }
                        File unpackfile = new File(decompreDirectory + File.separator + zipEntry.getName());

                        try (FileOutputStream out = new FileOutputStream(unpackfile)) {
                            int c;
                            byte[] by = new byte[1024];
                            while ((c = in.read(by)) != -1) {
                                out.write(by, 0, c);
                            }
                            out.flush();
                        }
                    }
                } catch (IOException ex) {
                    log.error("解压异常", ex);
                    throw new IOException("zip解压失败：" + ex.toString());
                }

            }
        } catch (IOException ex) {
            throw new IOException("解压失败：" + ex.toString());
        } finally {
            if (readfile != null) {
                try {
                    readfile.close();
                } catch (IOException ex) {
                    log.error("readfile.close() error!", ex);
                }
            }
        }
    }

    /**
     * 读取文件内容
     *
     * @param fileName
     * @return
     */
    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            reader.close();
            return sbf.toString();
        } catch (IOException e) {
            log.error("读取文件异常", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    log.error("IO异常", e1);
                }
            }
        }
        return sbf.toString();
    }

    /**
     * 创建目录
     *
     * @param destDirName
     * @return
     */
    public static boolean createDir(String destDirName) {
        File dir = new File(destDirName);
        if (dir.exists()) {
            log.warn("目录:" + destDirName + " 已存在");
            return false;
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            log.info("目录:" + destDirName + " 创建成功");
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除指定文件
     *
     * @param fileName 文件
     * @return
     */
    public static boolean deleteFile(String fileName) {
        log.info("正在删除指定文件" + fileName);
        return new File(fileName).delete();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 所要删除的目录
     * @return
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 读取文件二进制流
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFileToByte(String file) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            int len = bis.available();
            byte[] b = new byte[len];

            bis.read(b, 0, len);

            return b;
        }
    }

    /**
     * 往指定文件写入内容
     *
     * @param fileName
     * @param content
     */
    public static void writeToFile(String fileName, String content) throws IOException {
        File file = new File(fileName);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return;
                }
            }
            writer.write(content);
        } catch (Exception e) {
            log.error("写入文件异常", e);
        }
    }

    /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检 查是否还有线程在读或写
     *
     * @param mappedByteBuffer
     */
    public static void freedMappedByteBuffer(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass().getMethod("cleaner", new Class[0]);
                        //可以访问private的权限
                        getCleanerMethod.setAccessible(true);
                        //在具有指定参数的 方法对象上调用此 方法对象表示的底层方法
                        sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(mappedByteBuffer,
                                new Object[0]);
                        cleaner.clean();
                    } catch (Exception e) {
                        log.error("clean MappedByteBuffer error!!!", e);
                    }
                    log.info("clean MappedByteBuffer completed!!!");
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("内存异常", e);
        }
    }

    /**
     * 获取文件md5值
     *
     * @param path 文件路径
     * @return
     */
    public static String getFileMD5(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        byte[] buffer = new byte[1024];
        int len;
        try (FileInputStream in = new FileInputStream(file)) {
            digest = MessageDigest.getInstance("MD5");

            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            log.error("MD5异常", e);
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    /**
     * 获取文件的后缀名
     */
    public static String getFileNameSuffix(String fileName) {
        String fileSuffix = "";
        String str1 = ".";
        String str2 = "。";
        if (org.apache.commons.lang.StringUtils.isNotEmpty(fileName)) {
            if (fileName.contains(str1)) {
                fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            }
            if (fileName.contains(str2)) {
                fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            }
        }
        return fileSuffix;
    }


    /**
     * 获取文件的后缀名
     */
    public static boolean isTxtFile(String fileName) {
        return "txt".equals(getFileNameSuffix(fileName));
    }


    /**
     * 获取文件名称,不含后缀不含路径
     *
     * @param fileName
     * @return
     */
    public static String getShortFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        int end = fileName.lastIndexOf(".");
        if (end == -1) {
            //没有后缀名
            return fileName;
        }
        int start = fileName.lastIndexOf("/");
        start = start >= 0 ? start : 0;
        return fileName.substring(start, end);
    }

    /**
     * 判断是否是图片
     *
     * @param file
     * @return
     */
    public static boolean isImage(File file) {
        try {
            Image img = ImageIO.read(file);
            if (null == img) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }




    /**
     * 加载json文件解析为对象
     *
     * @param fileName 文件名
     * @return
     */
    public static Object getJsonFile(String fileName) {
        Object obj = null;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (null != is) {
                obj = JSONObject.parse(IOUtils.toString(is, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("加载json文件解析为对象出错，文件:{}", fileName, e);
        }
        return obj;
    }

    /**
     * 写入json文件
     * @param path 路径
     * @param json json字符串
     * @param fileName 文件路径
     */
    public static void writeJSON(String path, String json, String fileName) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("==========没有当前目录，创建目录==========");
                file.mkdirs();
            }
            log.info("============在{}路径创建文件{}", path, fileName);
            PrintStream stream=new PrintStream(path + fileName);//写入的文件path
            stream.print(json);//写入的字符串
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
