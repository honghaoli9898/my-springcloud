package com.seaboxdata.sdps.common.db.secret;


import com.alibaba.druid.filter.config.ConfigTools;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SecretEncrypt  {
    public static void main(String[] args) {

        try {
            String username="root";
            String password="123456";
            String[] keyPair = ConfigTools.genKeyPair(512);
            String privateKey = keyPair[0];
            String publicKey = keyPair[1];
            String enUsername = ConfigTools.encrypt(privateKey, username);
            String enPassword = ConfigTools.encrypt(privateKey, password);
            String deUsername = ConfigTools.decrypt(publicKey, enUsername);
            String dePassword = ConfigTools.decrypt(publicKey, enPassword);
            System.out.println(
            "username:"+username+"\n"
            +"password"+password+"\n"
            +"publicKey:"+publicKey+"\n"
            +"enUsername:"+enUsername+"\n"
            +"enPassword:"+enPassword+"\n"
            +"deUsername:"+deUsername+"\n"
            +"dePassword:"+dePassword+"\n"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
