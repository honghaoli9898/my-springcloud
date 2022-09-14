package com.seaboxdata.sdps.common.db.secret;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSource;


public class SecretDataSource extends DruidDataSource {

	private static final long serialVersionUID = -6070508264237025938L;
	public final static String PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMVv+wEGsnNH59G8Y5kAbPI1gf6SlI8ll+zXEbiGVj/R9hKZGSfupghX60zqiKa4E2Lv1YNhVfbBabHucKlvMAUCAwEAAQ==";

    @Override
    public void setUsername(String username) {
        try {
            username = ConfigTools.decrypt(PUBLIC_KEY, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setUsername(username);
    }

    @Override
    public void setPassword(String password) {
        try {
            password = ConfigTools.decrypt(PUBLIC_KEY, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setPassword(password);
    }
}
