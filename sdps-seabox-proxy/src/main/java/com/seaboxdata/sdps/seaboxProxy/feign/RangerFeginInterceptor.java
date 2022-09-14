package com.seaboxdata.sdps.seaboxProxy.feign;

import com.seaboxdata.sdps.common.core.constant.BaseConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class RangerFeginInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String plainCreds = "admin:Admin1234";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        template.header(HttpHeaders.AUTHORIZATION, "Basic " + base64Creds);
        template.header(HttpHeaders.CONTENT_TYPE, BaseConstant.CONTENT_TYPE);
    }


}
