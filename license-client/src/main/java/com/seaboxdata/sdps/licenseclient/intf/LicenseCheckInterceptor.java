package com.seaboxdata.sdps.licenseclient.intf;

import com.alibaba.fastjson.JSON;
import com.seaboxdata.sdps.licenseutil.common.LicenseVerify;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * LicenseCheckInterceptor
 */
public class LicenseCheckInterceptor extends HandlerInterceptorAdapter{

    private static final Logger logger = LogManager.getLogger(LicenseCheckInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        LicenseVerify licenseVerify = new LicenseVerify();
        System.out.println("=========Lic验证拦截器========");
        //校验证书是否有效
        boolean verifyResult = licenseVerify.verify();

        if (verifyResult) {
            return true;
        } else {
            response.setCharacterEncoding("utf-8");
            Map<String, String> result = new HashMap<>(1);
            result.put("result", "您的证书无效，请核查服务器是否取得授权或重新申请证书！");

            response.getWriter().write(JSON.toJSONString(result));

            return false;
        }
    }

}
