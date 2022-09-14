package com.seaboxdata.sdps.seaboxProxy.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.google.common.collect.Lists;
import com.seaboxdata.sdps.common.core.utils.RestTemplateUtil;
import com.seaboxdata.sdps.seaboxProxy.bean.QueuesObj;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author: Denny
 * @date: 2021/10/18 15:05
 * @desc:
 */
@Slf4j
@SuppressWarnings("all")
public class QueueUtil {

    private QueueUtil() {
    }

    public static void main(String[] args) {
        String url = "http://10.1.3.113:8088/ws/v1/cluster/scheduler";
        QueuesObj queuesObj = queryQueue(url);
        System.out.println("result way 1:" + queuesObj);
        System.out.println("result way 2:" + JSONObject.toJSONString(queuesObj));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("root","default");
        recursiveSubQueue(jsonObject);
    }

    public static QueuesObj queryQueue(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate(RestTemplateUtil.generateHttpsRequestFactory());
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.GET, null, JSONObject.class);
            JSONObject jsonObject = exchange.getBody();//获取结果
            JSONObject scheduler = jsonObject.getJSONObject("scheduler");
            JSONObject schedulerInfo = scheduler.getJSONObject("schedulerInfo");
            QueuesObj queuesObj = recursiveSubQueue(schedulerInfo);
            return queuesObj;
        } catch (Exception e) {
            log.error("查询队列失败.", e.getMessage());
        }
        return null;
    }

    /**
     * 递归实现嵌套调用
     *
     * @param schedulerInfo 队列参数
     */
    public static QueuesObj recursiveSubQueue(JSONObject schedulerInfo) {
        QueuesObj queuesObj = new QueuesObj();
        String queueName = schedulerInfo.getString("queueName");
        queuesObj.setQueueName(queueName);
        System.out.println("queueName:" + queueName);
        //除去root之外，依次取出其他队列。
        JSONObject queues = schedulerInfo.getJSONObject("queues");
        if (null != queues) {
            JSONArray queue = queues.getJSONArray("queue");
            queuesObj.setChildQueues(JSONObject.parseArray(JSONArray.toJSONString(queue),QueuesObj.class));
            if (null != queue) {
                for (int i = 0; i < queue.size(); i++) {
                    JSONObject jsonObject = queue.getJSONObject(i);
                    recursiveSubQueue(jsonObject);
                }
            }
        }
        return queuesObj;
    }
}
