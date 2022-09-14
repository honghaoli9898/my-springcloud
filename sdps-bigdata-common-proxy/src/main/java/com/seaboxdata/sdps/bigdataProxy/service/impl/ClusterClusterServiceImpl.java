package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.bigdataProxy.service.IClusterClusterService;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.bigdataProxy.util.RedisUtil;
import com.seaboxdata.sdps.common.core.utils.UserTokenUtil;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClusterClusterServiceImpl implements IClusterClusterService{
    @Autowired
    private SdpsServerInfoMapper serverInfoMapper;

    @Autowired
    private RedisUtil redisUtil;



    @Override
    public List<SdpsServerInfo> queryClusterHosts(String user) {
        if(StringUtils.isBlank(user)) {
            return null;
        }

        QueryWrapper<SdpsServerInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user", user);
        return serverInfoMapper.selectList(queryWrapper);
    }

    @Override
    public String generateCode(String user) {
        String token = "";
        if(redisUtil.exists(CommonConstant.AUTH_TOKEN + user)) {
            token = redisUtil.getValueByKey(CommonConstant.AUTH_TOKEN + user).toString();
        }else {
            token = UserTokenUtil.generateCode();
            redisUtil.set(CommonConstant.AUTH_TOKEN + user,token , 600L);
        }

        log.info("token:{}",token);
        return token;
    }

    @Override
    public Boolean compareUserToken(String code, String user) {
        Object redisCode = redisUtil.getValueByKey(CommonConstant.AUTH_TOKEN + user);

        if(Objects.isNull(redisCode)) {
            return false;
        }
        if(StringUtils.equals(code, redisCode.toString())){
            return true;
        }
        log.info("redisCode:{}"+redisCode.toString());
        return false;
    }
}
