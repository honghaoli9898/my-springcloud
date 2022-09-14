package com.seaboxdata.sdps.seaboxProxy.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.framework.bean.yarn.QueueState;
import com.seaboxdata.sdps.common.framework.bean.yarn.YarnConstant;

import java.util.HashMap;

/**
 * @author: Denny
 * @date: 2021/11/1 15:27
 * @desc:
 */
public class TestList2Json {
    public static void main(String[] args) {
        String json = "{\"Clusters\":{\"desired_config\":[{\"type\":\"capacity-scheduler\",\"tag\":\"version1635490144967\",\"service_config_version_note\":\"\",\"properties\":{\"yarn.scheduler.capacity.maximum-am-resource-percent\":0.5,\"yarn.scheduler.capacity.maximum-applications\":10000,\"yarn.scheduler.capacity.node-locality-delay\":40,\"yarn.scheduler.capacity.resource-calculator\":\"org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator\",\"yarn.scheduler.capacity.queue-mappings-override.enable\":false,\"yarn.scheduler.capacity.root.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.maximum-capacity\":100,\"yarn.scheduler.capacity.root.capacity\":100,\"yarn.scheduler.capacity.root.queues\":\"abc,default,test,test1\",\"yarn.scheduler.capacity.root.priority\":0,\"yarn.scheduler.capacity.root.accessible-node-labels\":\"*\",\"yarn.scheduler.capacity.root.abc.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.abc.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.abc.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.abc.maximum-capacity\":10,\"yarn.scheduler.capacity.root.abc.user-limit-factor\":1,\"yarn.scheduler.capacity.root.abc.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.abc.capacity\":10,\"yarn.scheduler.capacity.root.abc.ordering-policy\":\"fair\",\"yarn.scheduler.capacity.root.abc.ordering-policy.fair.enable-size-based-weight\":false,\"yarn.scheduler.capacity.root.abc.priority\":0,\"yarn.scheduler.capacity.root.default.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.default.maximum-capacity\":90,\"yarn.scheduler.capacity.root.default.user-limit-factor\":1,\"yarn.scheduler.capacity.root.default.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.default.capacity\":85,\"yarn.scheduler.capacity.root.default.priority\":0,\"yarn.scheduler.capacity.root.test.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test.maximum-capacity\":45,\"yarn.scheduler.capacity.root.test.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test.capacity\":3,\"yarn.scheduler.capacity.root.test.queues\":\"sub01,sub02\",\"yarn.scheduler.capacity.root.test.priority\":0,\"yarn.scheduler.capacity.root.test.sub01.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test.sub01.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test.sub01.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test.sub01.maximum-capacity\":50,\"yarn.scheduler.capacity.root.test.sub01.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test.sub01.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test.sub01.capacity\":50,\"yarn.scheduler.capacity.root.test.sub01.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test.sub01.priority\":0,\"yarn.scheduler.capacity.root.test.sub02.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test.sub02.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test.sub02.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test.sub02.maximum-capacity\":50,\"yarn.scheduler.capacity.root.test.sub02.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test.sub02.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test.sub02.capacity\":50,\"yarn.scheduler.capacity.root.test.sub02.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test.sub02.priority\":0,\"yarn.scheduler.capacity.root.test1.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test1.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test1.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test1.maximum-capacity\":10,\"yarn.scheduler.capacity.root.test1.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test1.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test1.capacity\":2,\"yarn.scheduler.capacity.root.test1.queues\":\"sub03,sub04\",\"yarn.scheduler.capacity.root.test1.priority\":0,\"yarn.scheduler.capacity.root.test1.sub03.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test1.sub03.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test1.sub03.minimum-user-limit-percent\":50,\"yarn.scheduler.capacity.root.test1.sub03.maximum-capacity\":50,\"yarn.scheduler.capacity.root.test1.sub03.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test1.sub03.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test1.sub03.capacity\":50,\"yarn.scheduler.capacity.root.test1.sub03.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test1.sub03.priority\":0,\"yarn.scheduler.capacity.root.test1.sub04.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test1.sub04.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test1.sub04.minimum-user-limit-percent\":50,\"yarn.scheduler.capacity.root.test1.sub04.maximum-capacity\":50,\"yarn.scheduler.capacity.root.test1.sub04.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test1.sub04.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test1.sub04.capacity\":50,\"yarn.scheduler.capacity.root.test1.sub04.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test1.sub04.priority\":0}}]}}";
        JSONObject jsonObject = JSONObject.parseObject(json);

        JSONObject object = jsonObject.getJSONObject("Clusters").getJSONArray("desired_config").getJSONObject(0);
        String tag = object.getString("tag");
        String type = object.getString("type");
        String versionNote = null;
        String service_config_version_note = (versionNote = object.getString("service_config_version_note")) == null ? "" : versionNote;
        System.out.println("tag:" + tag);
        System.out.println("type:" + type);
        System.out.println("service_config_version_note:" + service_config_version_note);
        System.out.println("-------------------------------------------------");
        System.out.println("-------------------------------------------------");

        JSONObject propsJson = object.getJSONObject("properties");
        System.out.println("propsJson:" + propsJson);
        String commonString01 = propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_MAXIMUM_AM_RESOURCE_PERCENT);
        String commonString02 = propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_MAXIMUM_APPLICATIONS);
        String commonString03 = propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_NODE_LOCALITY_DELAY);
        String commonString04 = propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_RESOURCE_CALCULATOR);
        String commonString05 = propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_QUEUE_MAPPINGS_OVERRIDE_ENABLE);
        System.out.println("commonString01:" + commonString01);
        System.out.println("commonString02:" + commonString02);
        System.out.println("commonString03:" + commonString03);
        System.out.println("commonString04:" + commonString04);
        System.out.println("commonString05:" + commonString05);
        HashMap<String,Object> properties = new HashMap<>();
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_MAXIMUM_AM_RESOURCE_PERCENT, propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_MAXIMUM_AM_RESOURCE_PERCENT));
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_MAXIMUM_APPLICATIONS, propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_MAXIMUM_APPLICATIONS));
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_NODE_LOCALITY_DELAY, propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_NODE_LOCALITY_DELAY));
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_RESOURCE_CALCULATOR, propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_RESOURCE_CALCULATOR));
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_QUEUE_MAPPINGS_OVERRIDE_ENABLE, propsJson.getString(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + YarnConstant.QUEUE_SUFFIX_COMMON_QUEUE_MAPPINGS_OVERRIDE_ENABLE));

        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_QUEUES, "default,test,test1");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root" + YarnConstant.QUEUE_SUFFIX_ACCESSIBLE_NODE_LABELS, "*");

        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.default" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.default" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 50);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.default" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.default" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.default" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 50);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.default" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);

        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 20);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_QUEUES, "sub01,sub02");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_ORDERING_POLICY, "fifo");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);

        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 30);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_QUEUES, "sub03");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_ORDERING_POLICY, "fifo");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);

        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 50);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_ORDERING_POLICY, "fifo");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub01" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);

        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 50);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_ORDERING_POLICY, "fifo");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test.sub02" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);


        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_ORDERING_POLICY, "fifo");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub03" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);


        /*properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_ACL_ADMINISTER_QUEUE, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_ACL_SUBMIT_APPLICATIONS, "*");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_MINIMUM_USER_LIMIT_PERCENT, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_MAXIMUM_CAPACITY, 100);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_USER_LIMIT_FACTOR, 1);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_STATE, QueueState.QUEUE_STATE_RUNNING);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_CAPACITY, 50);
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_ORDERING_POLICY, "fifo");
        properties.put(YarnConstant.QUEUE_PREFIX_YARN_SCHEDULER_CAPACITY_ROOT + "root.test1.sub04" + YarnConstant.QUEUE_SUFFIX_PRIORITY, 0);*/
        //desiredConfig.setProperties(properties);

        /**
         * 封装成一个匿名的JSON对象。
         */
        JSONObject anonymousJson = new JSONObject();
        anonymousJson.put("tag","version" + System.currentTimeMillis());
        anonymousJson.put("type", type);
        anonymousJson.put("service_config_version_note", versionNote);
        anonymousJson.put("properties",properties);

        /**
         * 把上面的对象封装成一个数组。
         */
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(0,anonymousJson);

        /**
         * 把上面的数组再次封装成一个desired_config的 json对象。
         */
        JSONObject desiredConfigJson = new JSONObject();
        desiredConfigJson.put("desired_config",JSONArray.toJSON(jsonArray));

        /**
         * 把desired_config json对象最终封装成Cluster json对象。
         */
        JSONObject clusterJson = new JSONObject();
        clusterJson.put("Cluster",desiredConfigJson);

        System.out.println(clusterJson);

    }



    public void defaultQueueInfo() {
        String json = "{\"Clusters\":{\"desired_config\":[{\"type\":\"capacity-scheduler\",\"tag\":\"version1635490144967\",\"service_config_version_note\":\"\",\"properties\":{\"yarn.scheduler.capacity.maximum-am-resource-percent\":0.5,\"yarn.scheduler.capacity.maximum-applications\":10000,\"yarn.scheduler.capacity.node-locality-delay\":40,\"yarn.scheduler.capacity.resource-calculator\":\"org.apache.hadoop.yarn.util.resource.DefaultResourceCalculator\",\"yarn.scheduler.capacity.queue-mappings-override.enable\":false,\"yarn.scheduler.capacity.root.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.maximum-capacity\":100,\"yarn.scheduler.capacity.root.capacity\":100,\"yarn.scheduler.capacity.root.queues\":\"abc,default,test,test1\",\"yarn.scheduler.capacity.root.priority\":0,\"yarn.scheduler.capacity.root.accessible-node-labels\":\"*\",\"yarn.scheduler.capacity.root.abc.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.abc.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.abc.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.abc.maximum-capacity\":10,\"yarn.scheduler.capacity.root.abc.user-limit-factor\":1,\"yarn.scheduler.capacity.root.abc.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.abc.capacity\":10,\"yarn.scheduler.capacity.root.abc.ordering-policy\":\"fair\",\"yarn.scheduler.capacity.root.abc.ordering-policy.fair.enable-size-based-weight\":false,\"yarn.scheduler.capacity.root.abc.priority\":0,\"yarn.scheduler.capacity.root.default.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.default.maximum-capacity\":90,\"yarn.scheduler.capacity.root.default.user-limit-factor\":1,\"yarn.scheduler.capacity.root.default.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.default.capacity\":85,\"yarn.scheduler.capacity.root.default.priority\":0,\"yarn.scheduler.capacity.root.test.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test.maximum-capacity\":45,\"yarn.scheduler.capacity.root.test.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test.capacity\":3,\"yarn.scheduler.capacity.root.test.queues\":\"sub01,sub02\",\"yarn.scheduler.capacity.root.test.priority\":0,\"yarn.scheduler.capacity.root.test.sub01.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test.sub01.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test.sub01.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test.sub01.maximum-capacity\":50,\"yarn.scheduler.capacity.root.test.sub01.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test.sub01.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test.sub01.capacity\":50,\"yarn.scheduler.capacity.root.test.sub01.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test.sub01.priority\":0,\"yarn.scheduler.capacity.root.test.sub02.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test.sub02.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test.sub02.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test.sub02.maximum-capacity\":50,\"yarn.scheduler.capacity.root.test.sub02.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test.sub02.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test.sub02.capacity\":50,\"yarn.scheduler.capacity.root.test.sub02.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test.sub02.priority\":0,\"yarn.scheduler.capacity.root.test1.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test1.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test1.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test1.maximum-capacity\":10,\"yarn.scheduler.capacity.root.test1.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test1.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test1.capacity\":2,\"yarn.scheduler.capacity.root.test1.queues\":\"sub03\",\"yarn.scheduler.capacity.root.test1.priority\":0,\"yarn.scheduler.capacity.root.test1.sub03.acl_administer_queue\":\"*\",\"yarn.scheduler.capacity.root.test1.sub03.acl_submit_applications\":\"*\",\"yarn.scheduler.capacity.root.test1.sub03.minimum-user-limit-percent\":100,\"yarn.scheduler.capacity.root.test1.sub03.maximum-capacity\":100,\"yarn.scheduler.capacity.root.test1.sub03.user-limit-factor\":1,\"yarn.scheduler.capacity.root.test1.sub03.state\":\"RUNNING\",\"yarn.scheduler.capacity.root.test1.sub03.capacity\":100,\"yarn.scheduler.capacity.root.test1.sub03.ordering-policy\":\"fifo\",\"yarn.scheduler.capacity.root.test1.sub03.priority\":0}}]}}";

    }
}
