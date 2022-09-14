package com.seaboxdata.sdps.seaboxProxy.test;

import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.framework.bean.ranger.*;
import com.seaboxdata.sdps.seaboxProxy.constants.BigDataConfConstants;
import com.seaboxdata.sdps.seaboxProxy.util.RangerUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@Component
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRanger {

    @Autowired
    RangerUtil rangerUtil;

    @Test
    public void likeUserByName() {
        //24集群
//        RangerUtil rangerUtil = SpringBeanUtil.getBean(RangerUtil.class);
        rangerUtil.init(2);
        RangerPageUser rangerPageUser = rangerUtil.likeUserByName("Jx001");
        System.out.println("==============>" + rangerPageUser.toString());
    }

    @Test
    public void getUserByName() {
        //24集群
        rangerUtil.init(2);
        VXUsers vxUsers = rangerUtil.getUserByName("Jx204");
        System.out.println("============>" + JSONObject.toJSONString(vxUsers));
        VXUsers rangerUser = rangerUtil.getUserById(vxUsers.getId());
        System.out.println("==============>" + JSONObject.toJSONString(rangerUser));
    }

    @Test
    public void addRangerUser() {
        //24集群
        rangerUtil.init(2);
        ArrayList<String> userRoleList = new ArrayList<>();
        VXUsers vxUsers = VXUsers.builder()
                .name("JX104")
                .firstName("JX104")
                .password("Jx123456")
                .userRoleList(userRoleList)
                .build();
        Boolean bool = rangerUtil.addRangerUser(vxUsers);
        System.out.println(bool);
    }

    @Test
    public void updateRangerUser() {
        //24集群
        rangerUtil.init(2);
        VXUsers jx104 = rangerUtil.getUserByName("JX104");
        System.out.println(jx104.toString());
        ArrayList<String> userRoleList = new ArrayList<>();
        userRoleList.add(BigDataConfConstants.RANGER_SYS_ADMIN);
        jx104.setUserRoleList(userRoleList);
        System.out.println(jx104.toString());
        Boolean bool = rangerUtil.updateRangerUser(jx104);
        System.out.println(bool);

    }

    @Test
    public void deleteRangerUser() {
        rangerUtil.init(2);
        Boolean bool = rangerUtil.deleteRangerUser("Jx205");
        System.out.println(bool);
    }

    @Test
    public void addRangerGroup() {
        rangerUtil.init(2);
        VXGroups vxGroups = VXGroups.builder()
                .name("DFJX104")
                .build();
        Boolean bool = rangerUtil.addRangerGroup(vxGroups);
        System.out.println(bool);
    }

    @Test
    public void getGroupByName() {
        //24集群
        rangerUtil.init(2);
        VXGroups vxGroups = rangerUtil.getGroupByName("DFJX101");
        System.out.println("==============>" + vxGroups.toString());
    }

    @Test
    public void deleteRangerGroup() {
        rangerUtil.init(2);
        Boolean bool = rangerUtil.deleteRangerGroup("DFJX104");
        System.out.println(bool);
    }

    @Test
    public void updateRangerGroup() {
        rangerUtil.init(2);
        VXGroups vxGroups = rangerUtil.getGroupByName("DFJX102");
        vxGroups.setDescription("DFJX102~~~~~~~");
        Boolean bool = rangerUtil.updateRangerGroup(vxGroups);
        System.out.println(bool);
    }

    @Test
    public void likeGroupByName() {
        //24集群
        rangerUtil.init(2);
        RangerPageGroup rangerPageGroup = rangerUtil.likeGroupByName("hdfs");
        System.out.println("==============>" + rangerPageGroup.toString());
    }

    @Test
    public void getUsersByGroupName() {
        //24集群
        rangerUtil.init(2);
        RangerGroupUser rangerGroupUser = rangerUtil.getUsersByGroupName("DFJX103");
        String s = JSONObject.toJSONString(rangerGroupUser);
        System.out.println(s);

    }

    @Test
    public void addUsersToGroup() {
        rangerUtil.init(2);
        ArrayList<String> addUserList = new ArrayList<>();
        addUserList.add("Jx201");
        addUserList.add("Jx202");
        addUserList.add("Jx203");

        Boolean bool = rangerUtil.addUsersToGroup("DFJX103", addUserList);
        System.out.println(bool);
    }

    @Test
    public void deleteUsersToGroup() {
        rangerUtil.init(2);
        ArrayList<String> deleteUsers = new ArrayList<>();
        deleteUsers.add("Jx202");
        deleteUsers.add("Jx203");
        Boolean bool = rangerUtil.deleteUsersToGroup("DFJX103", deleteUsers);
        System.out.println(bool);
    }
}
