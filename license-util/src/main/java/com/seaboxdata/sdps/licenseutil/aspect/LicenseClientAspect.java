package com.seaboxdata.sdps.licenseutil.aspect;

import com.alibaba.fastjson.JSONObject;


import com.seaboxdata.sdps.licenseutil.bean.LicenseVerifyParam;
import com.seaboxdata.sdps.licenseutil.common.LicenseUtil;
import com.seaboxdata.sdps.licenseutil.common.LicenseVerify;
import com.seaboxdata.sdps.licenseutil.exception.license.BusinessException;
import com.seaboxdata.sdps.licenseutil.annotation.*;
import com.seaboxdata.sdps.licenseutil.util.FileUtil;
import com.seaboxdata.sdps.licenseutil.util.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * license拦截业务主要实现
 */
@Aspect
@Component
@Slf4j
public class LicenseClientAspect {
    private static final String EXECUTE_NAME = "execute";
    private static final String EXECUTE_CLASS_NAME = "JobManagementServiceImpl";
    private static final String UPDATE_JOB_METHOD_NAME = "updateJobById";


    /**
     * 这是注解到类的切点
     */
    @Pointcut("@within(com.seaboxdata.sdps.licenseutil.annotation.LicenseClientOfClass)")
    public void matchWithin() {
    }

    /**
     * 这是类注解校验
     *
     * @param joinPoint
     * @return
     */
    @Around("matchWithin()")
    public Object doAroundOfClass(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean ret = false;
        LicenseClientOfClass licenseClient = null;
        try {
            Signature signature = joinPoint.getSignature();
            log.info("=========  控制层名:{},  方法名:{}()  =========",
                    signature.getDeclaringTypeName(), signature.getName());
            String methodName = signature.getName();
            licenseClient = (LicenseClientOfClass) signature.getDeclaringType().getAnnotation(LicenseClientOfClass.class);
            ret = licenseVerify();
        } catch (Exception e) {
            log.error(e.toString());
        }
        if (ret) {
            //校验通过正常执行目标方法
            return joinPoint.proceed();
        } else if (null != licenseClient && licenseClient.excunteTarget()) {
            //这里可以配置注解拦截的时候请求照样发送,但不返回请求结果
            joinPoint.proceed();
            throw BusinessException.ERR_LICENSE_VERIFY_INVALID;
        } else {
            //License无效,执行拦截
            log.error("License校验不通过");
            throw BusinessException.ERR_LICENSE_VERIFY_INVALID;
        }
    }


    /**
     * 这是方法注解校验
     * 拦截业务主要实现
     */
    @Around(value = "@annotation(com.seaboxdata.sdps.licenseutil.annotation.LicenseClientOfClass)")
    public Object doAroundOfMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        LicenseClientOfMethod licenseClientOfMethod = null;
        boolean ret = false;
        MethodSignature methodSignature;
        String methodName;
        try {

            methodSignature = (MethodSignature) joinPoint.getSignature();
            methodName = methodSignature.getName();

            log.info("license拦截方法-方法名:{}()", methodName);
            Method method = methodSignature.getMethod();
            licenseClientOfMethod = method.getAnnotation(LicenseClientOfMethod.class);
        } catch (Throwable throwable) {
            log.error("异常",throwable);
            throw BusinessException.ERR_LICENSE_VERIFY_FAIL;
        }
        ret = licenseVerify();
        if (ret) {
            //校验通过正常执行目标方法
            return joinPoint.proceed();
        } else if (null != licenseClientOfMethod && licenseClientOfMethod.excunteTarget()) {
            //这里可以配置注解拦截的时候请求照样发送,但是修改目标执行的返回
            joinPoint.proceed();
            throw BusinessException.ERR_LICENSE_VERIFY_INVALID;
        } else {
            // License无效,执行拦截
            // 如果拦截的方法是execute, 获取参数信息，将执行作业的状态修改为失败
            String className = methodSignature.getDeclaringType().getSimpleName();
            if (StringUtils.equals(className, EXECUTE_CLASS_NAME) && StringUtils.equals(methodName, EXECUTE_NAME)) {
                Class<?> cls = methodSignature.getDeclaringType();
                Method method = cls.getDeclaredMethod(UPDATE_JOB_METHOD_NAME, Integer.class);
                // 仅获取切点参数数组中(Integer id)的参数
                method.invoke(SpringBeanUtil.getBean(cls), getParam(joinPoint));
            }
            throw BusinessException.ERR_LICENSE_VERIFY_INVALID;
        }
    }

    private Integer getParam(ProceedingJoinPoint joinPoint){
        //获取参数值
        Object[] args = joinPoint.getArgs();
        //获取参数名
        String[] argNames = ((MethodSignature)joinPoint.getSignature()).getParameterNames();
        for(int i=0;i<args.length; i++){
            if((args[i] instanceof Integer) && "id".equals(argNames[i])){
                return (Integer) args[i];
            }
        }
        return null;
    }

    /**
     * License校验
     *
     * @return
     */
    private boolean licenseVerify() {
        LicenseVerify licenseVerify = new LicenseVerify();
        boolean flag = false;
        try {
            flag = licenseVerify.verify();
            if (flag) {
                log.info("======License拦截验证通过=====");
                //如果没过期此处应还需解析License文件进行Lic真伪校验
                //但由于每次请求都加载License解析校验会非常耗时和消耗性能,此处暂不处理
            }
        } catch (BusinessException e) {
            flag = false;
            if (BusinessException.ERR_LICENSE_NOT_INSTALL_CODE == e.getCode()) {
                log.error("======证书未安装======");
                boolean flag2 = install();
                if (flag2) {
                    flag = licenseVerify.verify();
                }
            }
        }
        return flag;
    }

    /**
     * 证书安装
     */
    private boolean install() {
//        File ff = new File(System.getProperty("user.dir"));
//        String licensePath = ff.getParent() + "/LicenseClient/lic/";
        String licensePath = LicenseUtil.LICENSE_PATH;
        log.info("=======lic文件路径:{}========", licensePath);
        boolean flag = false;
        if (StringUtils.isNotBlank(licensePath)) {
            String configString = FileUtil.readFileContent(licensePath + "config.config");
            if (null == configString) {
                log.warn("=======license配置文件解析失败,可能尚未安装License======");
                return flag;
            }

            LicenseVerifyParam licenseVerifyParam = analysisConfig(configString);
            if (null == licenseVerifyParam) {
                log.warn("==========license配置文件解析异常===========");
                return flag;
            }

            log.info("++++++++ 安装证书 ++++++++");
            LicenseVerifyParam param = new LicenseVerifyParam();
            param.setSubject(licenseVerifyParam.getSubject());
            param.setPublicAlias(licenseVerifyParam.getPublicAlias());
            param.setStorePass(licenseVerifyParam.getStorePass());
            param.setLicensePath(licensePath + "license.lic");
            param.setPublicKeysStorePath(licensePath + "publicKey.keystore");
            LicenseVerify licenseVerify = new LicenseVerify();
            //安装证书
            try {
                licenseVerify.install(param);
                flag = true;
                log.info("++++++++ 证书安装结束 ++++++++");
            } catch (Exception e) {
                flag = false;
            }
        }
        return flag;
    }


    /**
     * 解析license配置文件中参数
     *
     * @return
     */
    private LicenseVerifyParam analysisConfig(String configString) {
        JSONObject json = JSONObject.parseObject(configString);
        LicenseVerifyParam licenseInfo = new LicenseVerifyParam();
        if (null == json) {
            return null;
        }
        if (!org.apache.commons.lang.StringUtils.isBlank(json.getString("subject"))) {
            licenseInfo.setSubject(json.getString("subject"));
        }
        if (!org.apache.commons.lang.StringUtils.isBlank(json.getString("publicAlias"))) {
            licenseInfo.setPublicAlias(json.getString("publicAlias"));
        }
        if (!org.apache.commons.lang.StringUtils.isBlank(json.getString("storePass"))) {
            licenseInfo.setStorePass(json.getString("storePass"));
        }

        return licenseInfo;
    }
}
