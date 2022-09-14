package com.seaboxdata.sdps.seaboxProxy.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.seaboxdata.sdps.common.framework.bean.ambari.SdpsAmbariInfo;
import com.seaboxdata.sdps.seaboxProxy.bean.QueuesObj;
import com.seaboxdata.sdps.seaboxProxy.util.ShellUtil;
import com.seaboxdata.sdps.seaboxProxy.util.YarnUtil;
import org.apache.hadoop.service.Service;
import org.apache.hadoop.yarn.api.records.QueueInfo;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author: Denny
 * @date: 2021/10/12 18:02
 * @desc:
 */

@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestYarnUtil {

    @Test
    public void testYarnQueue() {
        QueuesObj queuesObj = new YarnUtil(1).queueAllExtract();
        System.out.println(queuesObj.toString());
        System.out.println("end...");
    }

    @Test
    public void testQueueName() {
        QueueInfo root = new YarnUtil(1).queryYarnQueue("default");
        System.out.println("start...");
        System.out.println(root);
        System.out.println("end...");
    }

    @Test
    public void testQueueState() {
        YarnClient yc = new YarnUtil(1).getYc();
        boolean inState = yc.isInState(Service.STATE.STARTED);
        System.out.println("inState:" + inState);
    }

    /**
     * 以Shell的放肆调用第三方接口，并且以String获取结果。
     * 不推荐。
     */
    @Test
    public void testRestTemplate1() {
        try {
            String result = ShellUtil.execCmd("curl http://10.1.3.113:8088/ws/v1/cluster/scheduler", null);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用第三方接口，并且以String的形式返回
     * 适用返回简单的接口。
     */
    @Test
    public void testRestTemplate2() {
        try {
            String result = null;
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://10.1.3.113:8088/ws/v1/cluster/scheduler";
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            result = exchange.getBody();//获取结果
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用第三方接口，并且以HashMap的形式返回
     * 适合返回值复杂的情形
     * 自研，失败。
     */
    @Test
    public void testRestTemplate3() {
        try {
            Object result = null;
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://10.1.3.113:8088/ws/v1/cluster/scheduler";
            ResponseEntity<HashMap> exchange = restTemplate.exchange(url, HttpMethod.GET, null, HashMap.class);
            HashMap map = exchange.getBody();//获取结果
            System.out.println("map:" + map);
            Object queue = map.get("scheduler.schedulerInfo.queues.queue.queueName");
            System.out.println("queue:" + queue);
            /*HashMap queues = (HashMap)map.get("queues");
            Iterator iterator = queues.entrySet().iterator();
            while(iterator.hasNext()) {
                HashMap next = (HashMap)iterator.next();
                result = next.get("queue");
                System.out.println(result);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用第三方接口，并且以HashMap的形式返回
     * 适合返回值复杂的情形
     * 借鉴，试验中。
     * 未解决自队列问题。
     */
    @Test
    public void testRestTemplate4() {
        try {
            Object result = null;
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://10.1.3.113:8088/ws/v1/cluster/scheduler";
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
            JSONObject jsonObject = exchange.getBody();//获取结果
            //System.out.println("jsonObject:" + jsonObject);
            JSONObject scheduler = jsonObject.getJSONObject("scheduler");
            JSONObject schedulerInfo = scheduler.getJSONObject("schedulerInfo");
            String queueName = schedulerInfo.getString("queueName");
            System.out.println("queueName:" + queueName);
            JSONObject queues = schedulerInfo.getJSONObject("queues");
            JSONArray queue = queues.getJSONArray("queue");
            int size = queue.size();
            //System.out.println("queue:" + queue);
            System.out.println("queue size:" + size);
            for (int i = 0; i < queue.size(); i++) {
                JSONObject queueJSONObject = queue.getJSONObject(i);
                System.out.println("queueJSONObject of " + i + ":" + queueJSONObject);
                String queueName1 = queueJSONObject.getString("queueName");
                System.out.println("queueName1 of " + i + ":" + queueName1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用第三方接口，并且以HashMap的形式返回
     * 适合返回值复杂的情形
     * 初级版。
     * 解决子队列问题。
     */
    @Test
    public void testRestTemplate5() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://10.1.3.113:8088/ws/v1/cluster/scheduler";
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
            JSONObject jsonObject = exchange.getBody();//获取结果
            //System.out.println("jsonObject:" + jsonObject);
            JSONObject scheduler = jsonObject.getJSONObject("scheduler");
            JSONObject schedulerInfo = scheduler.getJSONObject("schedulerInfo");
            recursiveSubQueue(schedulerInfo);//递归调用
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归实现嵌套调用
     * @param schedulerInfo 队列参数
     */
    public void recursiveSubQueue(JSONObject schedulerInfo) {
        String queueName = schedulerInfo.getString("queueName");
        System.out.println("queueName:" + queueName);
        //除去root之外，依次取出其他队列。
        JSONObject queues = schedulerInfo.getJSONObject("queues");
        if (null != queues) {
            JSONArray queue = queues.getJSONArray("queue");
            if(null != queue) {
                for (int i = 0; i < queue.size(); i++) {
                    JSONObject jsonObject = queue.getJSONObject(i);
                    recursiveSubQueue(jsonObject);
                }
            }
        }
    }

    /**
     * 大数据集群信息
     */
    @Test
    public void testClusterInformation() {
        //String url ="http://10.1.3.113:8088/ws/v1/cluster/info";
        //String url ="http://10.1.3.24:8088/ws/v1/cluster/info";
        String url ="http://10.1.3.24:8088/ws/v1/cluster";
        //cluster 和 cluster/info 两个接口是一样的。
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }

    /**
     * 大数据集群指标
     */
    @Test
    public void testClusterMetric() {
        //String url ="http://10.1.3.113:8088/ws/v1/cluster/metrics";
        String url ="http://10.1.3.24:8088/ws/v1/cluster/metrics";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }

    /**
     * 大数据集群应用详情
     */
    @Test
    public void testClusterapps() {
        //String url ="http://10.1.3.113:8088/ws/v1/cluster/apps";
        String url ="http://10.1.3.24:8088/ws/v1/cluster/apps";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }

    /**
     * 大数据集群应用详情
     */
    @Test
    public void testClusterapps1() {
        //String url ="http://10.1.3.113:8088/ws/v1/cluster/apps?deSelects=resouceRequests";
        String url ="http://10.1.3.24:8088/ws/v1/cluster/apps?deSelects=resouceRequests";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }


    /**
     * 大数据集群调度详情
     */
    @Test
    public void testClusterScheduler() {
        String url ="http://10.1.3.113:8088/ws/v1/cluster/scheduler";
        //String url ="http://10.1.3.24:8088/ws/v1/cluster/scheduler";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }

    /**
     * 大数据集群调度队列配置修改
     */
    @Test
    public void testClusterSchedulerConf() {
        //HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        //24集群
        //String url ="http://10.1.3.24:8088/ws/v1/cluster/scheduler-conf";
        /*String queueName = "root.default";
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        hashMap.put("capacity","50");
        hashMap.put("test","30");
        hashMap.put("test1","20");*/
        //113集群
        String url ="http://10.1.3.113:8088/ws/v1/cluster/scheduler-conf";
        String queueName = "root.default";
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        //hashMap.put("maximum-applications","100");
        //hashMap.put("minimum-user-limit-percen","10");
        hashMap.put("maxCapacity",80);
        hashMap.put("absoluteMaxCapacity",80);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set(headers.ACCEPT_ENCODING,"identity");
        headers.set(headers.TRANSFER_ENCODING,"chunked");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("queue-name", queueName);
        jsonObject.put("params", hashMap);

        HttpEntity<String> request = new HttpEntity<>(JSONObject.toJSONString(jsonObject),headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, request,String.class);
        String body = responseEntity.getBody();
        //restTemplate.put(url,request);
        System.out.println("body:" + body);
    }

    /**
     * 大数据集群调度队列配置修改
     */
    @Test
    public void testClusterSchedulerConf2() {
        String url ="http://10.1.3.113:8088/ws/v1/cluster/scheduler-conf";
        //String url ="http://10.1.3.24:8088/ws/v1/cluster/scheduler-conf";
        RestTemplate restTemplate = new RestTemplate();
        JSONObject params = new JSONObject();
        params.put("maximum-applications","100");
        params.put("minimum-user-limit-percent","10");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("queue-name", "root.default");
        jsonObject.put("params", params);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(headers.TRANSFER_ENCODING, "chunked");
        headers.set(headers.ACCEPT, "application/xml");
        HttpEntity httpEntity = new HttpEntity(jsonObject,headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
        String result = responseEntity.getBody();
        System.out.println("result:" + result);
    }

    @Test
    public void removeQueue() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://10.1.3.113:8088/ws/v1/cluster/scheduler-conf";
        JSONObject requestObject = new JSONObject();
        //requestObject.put("yarn.scheduler.capacity.queue-mappings-override.enable","true");
        requestObject.put("remove-queue","root.qtest.q001");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(requestObject),headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        String body = responseEntity.getBody();
        System.out.println("result:" +body);
    }

    /**
     * 大数据集群调度信息配置变化
     */
    @Test
    public void testCluster() {
        //String url ="http://10.1.3.113:8088/ws/v1/cluster/scheduler-conf";//已修改，3.1.1版本不可用。
        String url ="http://10.1.3.24:8088/ws/v1/cluster/scheduler/activities";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }

    /**
     * 大数据集群节点信息
     */
    @Test
    public void testNodes() {
        //String url ="http://10.1.3.113:8088/ws/v1/cluster/scheduler-conf";//已修改，3.1.1版本不可用。
        String url ="http://10.1.3.113:8088/ws/v1/cluster/nodes";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
        JSONObject jsonObject = responseEntity.getBody();
        System.out.println("result:" + jsonObject);
    }

    @Test
    public void testlistScheduler() {
        YarnUtil yarnUtil = new YarnUtil(1);
        System.out.println("======================11111111111111======================");
        JSONObject jsonObject = yarnUtil.listScheduler(1);
        System.out.println("======================22222222222222======================");
        System.out.println("jsonObject:" +jsonObject);
    }

    @Test
    public void testList() {
        List<SdpsAmbariInfo> ambariQueueLists = new ArrayList<>();
        SdpsAmbariInfo sdpsAmbariInfo = new SdpsAmbariInfo();
        sdpsAmbariInfo.setAmbariId(1);
        sdpsAmbariInfo.setAmbariHost("10.1.3.113");
        sdpsAmbariInfo.setAmbariPort("8080");
        //sdpsAmbariInfo.setAmbariAdminUser("admin");
        //sdpsAmbariInfo.setAmbariAdminPassword("admin");
        //sdpsAmbariInfo.setClusterId(1);
        SdpsAmbariInfo sdpsAmbariInfo2 = new SdpsAmbariInfo();
        //sdpsAmbariInfo2.setAmbariId(2);
        sdpsAmbariInfo2.setAmbariHost("10.1.3.24");
        sdpsAmbariInfo2.setAmbariPort("8080");
        //sdpsAmbariInfo2.setAmbariAdminUser("admin");
        //sdpsAmbariInfo2.setAmbariAdminPassword("admin");
        sdpsAmbariInfo2.setClusterId(2);
        ambariQueueLists.add(sdpsAmbariInfo);
        ambariQueueLists.add(sdpsAmbariInfo2);
        System.out.println("ambariQueueLists" + ambariQueueLists);
        System.out.println("ambariQueueLists" + JSONObject.toJSON(ambariQueueLists));
    }

}
