package com.seaboxdata.sdps.common.core.utils;

/**
 * 用户6位随机数token
 * @author pengsong
 */
public class UserTokenUtil{

    public static String generateCode(){
      return String.valueOf((int) ((Math.random()*9+1)*Math.pow(10,5)));
    }

}
