package com.seaboxdata.sdps.licenseclient.controller;


import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.licenseclient.bean.license.LicenseInfoVO;
import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerVO;
import com.seaboxdata.sdps.licenseclient.common.ServerResponse;
import com.seaboxdata.sdps.licenseclient.common.utils.MD5Util;
import com.seaboxdata.sdps.licenseclient.service.LicenseServerService;
import com.seaboxdata.sdps.licenseclient.service.RedisService;
import com.seaboxdata.sdps.licenseutil.bean.LicenseVerifyParam;

import com.seaboxdata.sdps.licenseutil.common.LicenseUtil;
import com.seaboxdata.sdps.licenseutil.common.LicenseVerify;
import com.seaboxdata.sdps.licenseutil.exception.license.BusinessException;
import com.seaboxdata.sdps.licenseutil.util.FileUtil;
import de.schlichtherle.license.LicenseContent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * license客户端接口
 */
@Api(tags = "客户端License接口")
@Slf4j
@PropertySource({"license-config.properties"})
@RestController
@RequestMapping(value = "/client")
public class LicenseController{


    /**
     * lic文件名
     */
    @Value("${license.licenseName}")
    private String licenseName;

    /**
     * 公钥文件名
     */
    @Value("${license.publicName}")
    private String publicName;

    /**
     * 私钥文件名
     */
    @Value("${license.privateName}")
    private String privateName;

    /**
     * 配置文件名
     */
    @Value("${license.configName}")
    private String configName;

    @Value("${sdps.version}")
    private String version;

    // @Value("${license.bak}")
    //private String licenseBakPath;

    @Autowired
    private LicenseServerService licenseServerService;

    @Autowired
    private RedisService redisService;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String basePath = System.getProperty("user.home");
    private static final String licenseHome = basePath + File.separator + "seabox-license";
    private static final String licenseKey = "seabox-license.key";
    private static final String licenseValue = "seabox-license.value";

    @Autowired
    private RedisRepository redisRepository;


    @ApiOperation("生成ESN码")
    @GetMapping("/getServerInfos")
    public ServerResponse getServerInfos() throws UnsupportedEncodingException {
        List<LicenseServerVO> list = licenseServerService.getServerInfos();
        String ESN = getEsnByServerInfo(list);

        //生成ESN码返回
        return new ServerResponse<String>(ESN);
    }

    /**
     * 获取授权服务详情
     *
     * @return
     */
    @ApiOperation("获取服务器授权信息,包括ESN,到期时间,版本号等")
    @GetMapping("/getServiceDetails")
    public ServerResponse getServiceDetails() throws UnsupportedEncodingException {
        if (!new File(licenseHome).exists()) {
            FileUtil.createDir(licenseHome);
        }

        String expireTimeStr = decrypt();
        LicenseInfoVO licenseInfoVO = new LicenseInfoVO();
        licenseInfoVO.setExpire(false);
        Date expireTime = null;
        try {
            expireTime = sdf.parse(expireTimeStr);
        } catch (ParseException e) {
            log.error("getServiceDetails 异常", e);
            throw BusinessException.ERR_LICENSE_VERIFY_FAIL;
        }
        licenseInfoVO.setExpireTime(expireTime);
        if (licenseInfoVO.getExpireTime().compareTo(new Date()) < 0) {
            licenseInfoVO.setExpire(true);
        }
        redisRepository.set(CommonConstant.LICENSE, licenseInfoVO.getExpireTime());
        licenseInfoVO.setVersion(version);
        log.info(licenseInfoVO.toString());
        return new ServerResponse(licenseInfoVO);

    }



    /**
     * 文件上传,这里我们直接上传从服务端生成的zip文件
     *
     * @param multiReq
     * @return
     */
    @ApiOperation("上传License安装包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "lic安装包文件对象", required = true)
    })
    @ResponseBody
    @PostMapping("/licenseUpload")
    public ServerResponse licenseUpload(MultipartHttpServletRequest multiReq) {
        String savePath = "";
        String baseDir = System.getProperty("user.dir");
        //  String endTime = decrypt(); 
        //  System.out.println("过期日期--------------------->"+endTime);
        //Gateway HeaderFilter POST, /api/licenseClient/client/licenseUpload
        try {
            //上传的文件路径,放在lic文件目录下
            // savePath = licensePath;
            savePath = baseDir + File.separator + "data";
            File f = new File(savePath);
            if (f.exists()) {
                //先备份旧的Lic文件
                // licBak();
                //如果目录已经存在则删掉重建防止久Lic文件冲突
                FileUtil.deleteDir(f);
            }
            if (!new File(licenseHome).exists()) {
                FileUtil.createDir(licenseHome);
            }

            //创建文件夹
            FileUtil.createDir(savePath);
            MultipartFile file = multiReq.getFile("file");
            String filename = file.getOriginalFilename();
            String zipPath = savePath + File.separator + filename;

            System.out.println("保存指定位置----------------------->" + savePath);
            //将上传过来的文件保存到指定路径
            craeteZip(zipPath, file);
            System.out.println("解压路径----------------------->" + zipPath);
            //解压zip文件
            FileUtil.unZip(zipPath, savePath);
            //安装
            /*
             * LicenseContent licenseContent = install(savePath); if
             * (!verifyLic(licenseContent)) {
             * log.error("======Lic不可用,该Lic文件非本集群或已被篡改:{}=====", (new
             * LicenseInfoVO(licenseContent)).toString()); //将以安装的License卸载 LicenseManager
             * licenseManager = LicenseManagerHolder.getInstance(null);
             * licenseManager.uninstall(); //删除已上传的Lic文件 FileUtil.deleteDir(f); throw
             * BusinessException.ERR_LICENSE_VERIFY_INVALID; }
             */
            /**
             * 备份文件老的license文件
             */
            String keyTemp = licenseHome + File.separator + licenseKey;
            String valueTemp = licenseHome + File.separator + licenseValue;
            long timeMillis = System.currentTimeMillis();
            if (new File(keyTemp).exists()) {
                renameFile(keyTemp, keyTemp + timeMillis);
            }

            if (new File(valueTemp).exists()) {
                renameFile(valueTemp, valueTemp + timeMillis);
            }

            if (new File(savePath + File.separator + licenseKey).exists()) {
                renameFile(savePath + File.separator + licenseKey, keyTemp);
            }

            if (new File(savePath + File.separator + licenseValue).exists()) {
                renameFile(savePath + File.separator + licenseValue, valueTemp);
            }

            //通知各模块刷新License缓存
            sendUpMsg();
            //安装完成
            /*
             * if (null != licenseContent) { return new ServerResponse(0, "License部署成功"); }
             * else { throw BusinessException.ERR_LICENSE_INSTALL_FAIL; }
             */
            return new ServerResponse(0, "License部署成功");
        } catch (Exception e) {
            //如果安装失败则还原原有的Lic文件
            reductionLic();
            if (e instanceof BusinessException) {
                throw new BusinessException(((BusinessException) e).getMsg(), ((BusinessException) e).getCode());
            }
            throw BusinessException.ERR_LICENSE_UPLOAD_FAIL;
        }
    }

    /**
     * 重命名
     *
     * @param oldFileName
     * @param newFileName
     */
    private void renameFile(String oldFileName, String newFileName) {
        //    SimpleDateFormat fmdate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        //   String oldFileName = filePath+"/"+fileName;
        System.gc();
        File oldFile = new File(oldFileName);
        // String newFileName = filePath+"/"+fileName.split("\\.")[0]+fmdate.format(new Date())+"."+fileName.split("\\.")[1];
        File newFile = new File(newFileName);
        if (oldFile.exists() && oldFile.isFile()) {
            oldFile.renameTo(newFile);
        }
    }

    /**
     * 将上传过来的文件保存到指定路径
     *
     * @param zipPath
     * @param file
     * @throws IOException
     */
    private void craeteZip(String zipPath, MultipartFile file) throws IOException {
        if (file.getName() == null || zipPath == null) {
            throw BusinessException.ERR_LICENSE_CREATE_PARAMS_DEFACT;
        }

        if (file.getName().contains("..")) {
            return;
        }

        if (zipPath.contains("..")) {
            return;
        }

        File uploadFile = new File(zipPath);
        if (!uploadFile.exists()) {
            InputStream in = file.getInputStream();
            FileOutputStream out = new FileOutputStream(zipPath);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        }
    }

    private String getEsnByServerInfo(List<LicenseServerVO> list) {
        StringBuffer sbCpus = new StringBuffer();
        StringBuffer sbIps = new StringBuffer();
        StringBuffer sbMacs = new StringBuffer();
        StringBuffer sbMainBoard = new StringBuffer();
        LicenseServerVO licenseServerVO = new LicenseServerVO();
        String serverInfosBase64 = "";
        if (null != list) {
            for (LicenseServerVO vo : list) {
                sbCpus.append(vo.getCpuSerial()).append(",");
                sbIps.append(vo.getIpAddress()).append(",");
                sbMacs.append(vo.getMacAddress()).append(",");
                sbMainBoard.append(vo.getMainBoardSerial()).append(",");
            }

            List<String> cpuList = Arrays.asList(sbCpus.toString().split(","));
            cpuList.sort(Comparator.naturalOrder());
            licenseServerVO.setCpuSerial(StringUtils.join(cpuList.toArray()));

            List<String> mainBoardList = Arrays.asList(sbMainBoard.toString().split(","));
            mainBoardList.sort(Comparator.naturalOrder());
            licenseServerVO.setMainBoardSerial(StringUtils.join(mainBoardList.toArray()));

            List<String> ipList = Arrays.asList(sbIps.toString().split(","));
            ipList.sort(Comparator.naturalOrder());
            licenseServerVO.setIpAddress(StringUtils.join(ipList.toArray()));

            List<String> macList = Arrays.asList(sbMacs.toString().split(","));
            macList.sort(Comparator.naturalOrder());
            licenseServerVO.setMacAddress(StringUtils.join(macList.toArray()));

            serverInfosBase64 = Base64.getEncoder()
                    .encodeToString(licenseServerVO.toString().getBytes(StandardCharsets.UTF_8));
        }
        return MD5Util.getMD5(serverInfosBase64).toUpperCase();
    }

    /**
     * 发送更新License通知
     */
    private void sendUpMsg() {
        log.info("发送更新License的通知");
        redisService.releaseLicenseUpdata(null);
    }

    /**
     * 校验ESN码是否一致
     *
     * @param esn
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean verifyEsn(String esn) throws UnsupportedEncodingException {
        ServerResponse sr = this.getServerInfos();

        String myEsn = String.valueOf(sr.getData());
        if (StringUtils.isBlank(myEsn)) {
            return false;
        }
        return myEsn.equals(esn);
    }

    /**
     * 校验Lic信息
     *
     * @param licenseContent
     * @return
     */
    private boolean verifyLic(LicenseContent licenseContent) throws UnsupportedEncodingException {
        if (null == licenseContent) {
            return false;
        }
        LicenseInfoVO licenseInfoVO = new LicenseInfoVO(licenseContent);
        return verifyEsn(licenseInfoVO.getEsn());
    }


    /**
     * 旧Lic文件备份
     *
     * @return
     */
    private void licBak(String licenseBakPath) {
        /*
         * FileUtil.createDir(licenseBakPath); File pubFile = new File(this.licensePath
         * + this.publicName); if (licenseFile.isFile() && licenseFile.exists()) { if
         * (licenseFile.renameTo(new File(licenseBakPath + this.licenseName))) {
         * log.info("lic文件备份完成"); } } if (configFile.isFile() && configFile.exists()) {
         * if (configFile.renameTo(new File(this.licenseBakPath + this.configName))) {
         * log.info("config文件备份完成"); } } if (pubFile.isFile() && pubFile.exists()) { if
         * (pubFile.renameTo(new File(this.licenseBakPath + this.publicName))) {
         * log.info("publickey文件备份完成"); } }
         */
    }

    /**
     * 还原Lic文件
     */
    private void reductionLic() {
        /*
         * log.info("=====lic文件还原===="); File licenseFile = new File(this.licenseBakPath
         * + this.licenseName); File configFile = new File(this.licenseBakPath +
         * this.configName); File pubFile = new File(this.licenseBakPath +
         * this.publicName); boolean flag = false; FileUtil.createDir(this.licensePath);
         * if (licenseFile.isFile() && licenseFile.exists()) { if
         * (licenseFile.renameTo(new File(this.licensePath + this.licenseName))) {
         * log.info("lic文件还原完成"); flag = true; } } if (configFile.isFile() &&
         * configFile.exists()) { if (configFile.renameTo(new File(this.licensePath +
         * this.configName))) { log.info("config文件还原完成"); } } if (pubFile.isFile() &&
         * pubFile.exists()) { if (pubFile.renameTo(new File(this.licensePath +
         * this.publicName))) { log.info("publickey文件还原完成"); } } if (flag) {
         * install(this.licensePath); FileUtil.deleteDir(new File(this.licenseBakPath));
         * }
         */
    }

    /**
     * 安装License
     *
     * @param savePath
     * @return
     */
    private LicenseContent install(String savePath) {
        //解压后拿到公钥文件解密Lic文件内容
        String configString = FileUtil.readFileContent(savePath + configName);
        /*
         * if (null == configString) { throw BusinessException.ERR_LICENSE_CONFIG_READ;
         * }
         */
        //解析配置
        LicenseVerifyParam licenseVerifyParam = LicenseUtil.analysisConfig(configString);
        /*
         * if (null == licenseVerifyParam) { throw
         * BusinessException.ERR_LICENSE_CONFIG_ANALYS; }
         */

        //开始安装
        licenseVerifyParam.setLicensePath(savePath + licenseName);
        licenseVerifyParam.setPublicKeysStorePath(savePath + publicName);
        LicenseVerify licenseVerify = new LicenseVerify();
        //    LicenseContent licenseContent = licenseVerify.install(licenseVerifyParam);
        return null;
    }

    @GetMapping("test")
    public String test() {
        log.info("发送更新License的通知");
        redisService.releaseLicenseUpdata("消息内容");
        return "发送完成";
    }

    /**
     * 解密
     *
     * @throws Exception
     */
    public String decrypt() {

        StringBuffer str = new StringBuffer();
        File license_dir = new File(licenseHome);
        if (!license_dir.exists()) {
            license_dir.mkdir();
        }
        try {
            // 读取密文
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(licenseHome + File.separator + licenseValue)));
            if (in == null) {
                return null;
            }
            String ctext = in.readLine();
            BigInteger c = new BigInteger(ctext);
            // 读取私钥
            FileInputStream f = new FileInputStream(licenseHome + File.separator + licenseKey);
            if (f == null) {
                return null;
            }
            ObjectInputStream b = new ObjectInputStream(f);
            RSAPublicKey pbk = (RSAPublicKey) b.readObject();
            BigInteger d = pbk.getPublicExponent();
            // 获取公钥参数及解密
            BigInteger n = pbk.getModulus();
            BigInteger m = c.modPow(d, n);
            // 显示解密结果
            byte[] mt = m.toByteArray();

            for (int i = 0; i < mt.length; i++) {
                str.append((char) mt[i]);
            }
        } catch (Exception e) {
        	log.error("获取license失败",e);
        }
        // String dateStr="2023-06-25 23:59:59";
        String dateStr = str.toString();
        if (dateStr == null || "".equals(dateStr)) {
            dateStr = "1970-01-01 00:00:00";
        }

        return dateStr;
    }

    public static void main(String[] args) {
        String str = "1970-01-01 00:00:00";
        Date expireTime = null;
        try {
            expireTime = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(expireTime);
    }


}
