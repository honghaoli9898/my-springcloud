package com.seaboxdata.sdps.seaboxProxy.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

/**
 * @author: Denny
 * @date: 2022/3/9 15:56
 * @desc: 测试StringUtils的用法
 */
public class StringT {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String s1 = null;
        String s2 = "";
        String s3 = " ";
        String s4 = "1";

        System.out.println(StringUtils.isNotBlank(s1));
        System.out.println(StringUtils.isNotBlank(s2));
        System.out.println(StringUtils.isNotBlank(s3));
        System.out.println(StringUtils.isNotBlank(s4));
        System.out.println(StringUtils.isBlank(s1));
        System.out.println(StringUtils.isBlank(s2));
        System.out.println(StringUtils.isBlank(s3));
        System.out.println(StringUtils.isBlank(s4));
        System.out.println("=============================");
        String url = "https://10.1.3.24:8080/ws/v1/cluster/scheduler";
        ArrayList<String> lists = Lists.newArrayList();
        HashMap<String, Object> hashMap = Maps.newHashMap();
        hashMap.put("clusterId", 1);
        hashMap.put("name", "admin");
        hashMap.put("password", "admin1234");
        hashMap.entrySet().forEach(entry -> {
            lists.add(entry.getKey() + "=" + entry.getValue());
        });
        String join = StringUtils.join(lists, "&");
        System.out.println("join:" + join);
        //url += "?" + join;
        url = url + "?" + join;
        System.out.println("url:" + url);

        System.out.println("-----------------------------------------");
        System.out.println("YWRtaW46YWRtaW4xMjM0");
        String str = "YWRtaW46YWRtaW4xMjM0";
        byte[] decode = Base64.getDecoder().decode(str.getBytes());
        String s = new String(decode);
        System.out.println(s);
        byte[] encode = Base64.getEncoder().encode(s.getBytes());
        String s5 = new String(encode);
        System.out.println(s5);
        byte[] decode1 = Base64Utils.decode(str.getBytes());
        System.out.println(new String(decode1));
        String encode1 = Base64Util.encode(s);
        System.out.println(encode1);
        Configuration entries = new Configuration();
        GenericOptionsParser parser1 = new GenericOptionsParser(entries, args);
        String[] remainingArgs = parser1.getRemainingArgs();
        Job job = Job.getInstance();
        job.waitForCompletion(true);
    }
}
