package com.seaboxdata.sdps.usersync.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.ranger.VXUsers;
import com.seaboxdata.sdps.common.framework.bean.request.UserSyncRequest;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncOperatorEnum;
import com.seaboxdata.sdps.usersync.feign.BigDataCommonProxyFeign;
import com.seaboxdata.sdps.usersync.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SdpsUserSyncInfoMapper;
import com.seaboxdata.sdps.usersync.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.usersync.mapper.SysUserMapper;
import com.seaboxdata.sdps.usersync.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.usersync.service.UserSyncService;
import com.seaboxdata.sdps.usersync.utils.UserSyncUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service("linuxUserSyncService")
public class LinuxUserSyncServiceImpl implements UserSyncService{

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysGlobalArgsMapper globalArgsMapper;

    @Autowired
    private SdpsServerInfoMapper serverInfoMapper;

    @Autowired
    private BigDataCommonProxyFeign bigdataCommonFegin;

    @Autowired
    private SdpsUserSyncInfoMapper userSyncInfoMapper;

    @Autowired
    private SdpsClusterMapper clusterMapper;

    @Override
    public boolean userSyncAdd(UserSyncRequest userSyncRequest) {
        List<Integer> clusterIds = userSyncRequest.getClusterIds();
        SysUser user = new SysUser();
        try {
            user = sysUserMapper.selectById(userSyncRequest.getUserId());
            for (int i = 0; i < 5; i++) {
                if (Objects.nonNull(user)) {
                    break;
                }
                Thread.sleep(20);
                user = sysUserMapper.selectById(userSyncRequest.getUserId());
            }
            if (Objects.isNull(user)) {
                throw new BusinessException("??????????????????id="
                        + userSyncRequest.getUserId());
            }

        } catch (Exception e) {
            log.error("????????????id={},??????ranger??????????????????", userSyncRequest.getUserId(),
                    e);
            if (CollUtil.isNotEmpty(clusterIds)) {
                for (Integer clusterId : clusterIds) {
                    SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
                    userSyncInfo.setAssName("linux?????????");
                    userSyncInfo.setAssType("linux");
                    userSyncInfo.setClusterId(clusterId);
                    userSyncInfo.setSyncResult(false);
                    userSyncInfo.setUserId(userSyncRequest.getUserId());
                    userSyncInfo.setUpdateTime(DateUtil.date());
                    userSyncInfo
                            .setOperator(UserSyncOperatorEnum.ADD.getCode());
                    userSyncInfo.setUsername(user.getUsername());
                    userSyncInfo.setInfo(e.getMessage());
                    try {
                        userSyncInfoMapper
                                .delete(new UpdateWrapper<SdpsUserSyncInfo>()
                                        .eq("user_id",
                                                userSyncRequest.getUserId())
                                        .eq("cluster_id", clusterId)
                                        .eq("operator",
                                                UserSyncOperatorEnum.ADD
                                                        .getCode())
                                        .eq("ass_type", "linux"));
                        userSyncInfoMapper.insert(userSyncInfo);
                    } catch (Exception e1) {
                        log.error("???????????????usersyncinfo={},????????????", userSyncInfo, e1);
                    }
                }
            }
            return false;
        }
        boolean isSuccess = true;
        for (Integer clusterId : clusterIds) {
            Result result = null;
            try {
                SdpsCluster sdpsCluster = clusterMapper.selectById(clusterId);
                //1.????????????root????????????
                SdpsServerInfo sdpsServerInfo = serverInfoMapper
                        .selectOne(new QueryWrapper<SdpsServerInfo>()
                                .eq("user", "root")
                                .eq("type", "SSH")
                                .eq("server_id", sdpsCluster.getServerId()));
                SysGlobalArgs sysGlobalArgs = globalArgsMapper
                        .selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
                                "password").eq("arg_key", "privateKey"));
                //??????shell????????????????????????
                SdpsServerInfo shellUser = serverInfoMapper
                        .selectOne(new QueryWrapper<SdpsServerInfo>()
                                .eq("user", user.getUsername())
                                .eq("type", ServerTypeEnum.C.name())
                                .eq("server_id", 0));

                String pass = RsaUtil.decrypt(shellUser.getPasswd(),
                        sysGlobalArgs.getArgValue());

                String passShell = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
                        sysGlobalArgs.getArgValue());
                //2,????????????shell
                log.info("host:"+sdpsServerInfo.getHost()+";user:"+sdpsServerInfo.getUser()+";pass:"+passShell);
                log.info("port:"+Integer.parseInt(sdpsServerInfo.getPort()));
                RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(),
                        sdpsServerInfo.getUser(), passShell, Integer.parseInt(sdpsServerInfo.getPort()));
                StringBuilder sb = new StringBuilder();
                //3.???????????????linux ????????????perl -e 'print crypt("pass", "wtf")' |xargs useradd test0428 -p
                sb.append("perl -e 'print crypt(\"");
                sb.append(pass);
                sb.append("\", \"wtf\")' |xargs useradd ");
                sb.append(user.getUsername() + " -p");
                remoteShellExecutorUtil.commonExec(sb.toString());
                result = Result.succeed("?????? shell??????user??????");
            } catch (Exception e) {
                log.error("????????????linux??????????????????", e);
                result = Result.failed("????????????linux??????????????????");
                isSuccess = false;
            }
            log.info("??????id={},linux????????????id={},?????????????????????????????????:{}", clusterId,
                    userSyncRequest.getUserId(), result.isSuccess());
            SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
            userSyncInfo.setAssName("linux????????????");
            userSyncInfo.setAssType("linux");
            userSyncInfo.setClusterId(clusterId);
            userSyncInfo.setSyncResult(result.isSuccess());
            userSyncInfo.setUserId(userSyncRequest.getUserId());
            userSyncInfo.setUpdateTime(DateUtil.date());
            userSyncInfo.setOperator(UserSyncOperatorEnum.ADD.getCode());
            userSyncInfo.setInfo(result.getMsg());
            userSyncInfo.setUsername(user.getUsername());
            try {
                userSyncInfoMapper.delete(new UpdateWrapper<SdpsUserSyncInfo>()
                        .eq("user_id", userSyncRequest.getUserId())
                        .eq("cluster_id", clusterId)
                        .eq("operator", UserSyncOperatorEnum.ADD.getCode())
                        .eq("ass_type", "linux"));
                userSyncInfoMapper.insert(userSyncInfo);
            } catch (Exception e) {
                log.error("??????usersyncinfo={},????????????", userSyncInfo, e);
                isSuccess = false;
            }
        }
        return isSuccess;
    }

    @Override
    public boolean userSyncDelete(UserSyncRequest userSyncRequest) {
        List<Integer> clusterIds = userSyncRequest.getClusterIds();
        SysUser sysUser = new SysUser();
        try {
            sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
        } catch (Exception e) {
            log.error("????????????id={},??????ranger??????????????????", userSyncRequest.getUserId(),
                    e);
            if (CollUtil.isNotEmpty(clusterIds)) {
                for (Integer clusterId : clusterIds) {
                    SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
                    userSyncInfo.setAssName("linux????????????");
                    userSyncInfo.setAssType("linux");
                    userSyncInfo.setClusterId(clusterId);
                    userSyncInfo.setSyncResult(false);
                    userSyncInfo.setUserId(userSyncRequest.getUserId());
                    userSyncInfo.setUpdateTime(DateUtil.date());
                    userSyncInfo.setOperator(UserSyncOperatorEnum.DELETE
                            .getCode());
                    userSyncInfo.setUsername(sysUser.getUsername());
                    userSyncInfo.setInfo(e.getMessage());
                    try {
                        userSyncInfoMapper
                                .delete(new UpdateWrapper<SdpsUserSyncInfo>()
                                        .eq("user_id",
                                                userSyncRequest.getUserId())
                                        .eq("cluster_id", clusterId)
                                        .eq("operator",
                                                UserSyncOperatorEnum.DELETE
                                                        .getCode())
                                        .eq("ass_type", "linux"));
                        userSyncInfoMapper.insert(userSyncInfo);
                    } catch (Exception e1) {
                        log.error("???????????????usersyncinfo={},????????????", userSyncInfo, e1);
                    }
                }
            }
            return false;
        }
        Result result;
        boolean isSuccess = true;
        for (Integer clusterId : clusterIds) {
            try {
                SdpsCluster sdpsCluster = clusterMapper.selectById(clusterId);
                //1.????????????root????????????
                SdpsServerInfo sdpsServerInfo = serverInfoMapper
                        .selectOne(new QueryWrapper<SdpsServerInfo>()
                                .eq("user", "root")
                                .eq("type", "SSH")
                                .eq("server_id", sdpsCluster.getServerId()));
                SysGlobalArgs sysGlobalArgs = globalArgsMapper
                        .selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
                                "password").eq("arg_key", "privateKey"));
                //??????shell????????????????????????
                SdpsServerInfo shellUser = serverInfoMapper
                        .selectOne(new QueryWrapper<SdpsServerInfo>()
                                .eq("user", sdpsServerInfo.getUser())
                                .eq("type", ServerTypeEnum.C.name())
                                .eq("server_id", 0));

                String passShell = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
                        sysGlobalArgs.getArgValue());
                //2,????????????shell
                log.info("host:"+sdpsServerInfo.getHost()+";user:"+sdpsServerInfo.getUser()+";pass:"+passShell);
                log.info("port:"+Integer.parseInt(sdpsServerInfo.getPort()));
                RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(),
                        sdpsServerInfo.getUser(), passShell, Integer.parseInt(sdpsServerInfo.getPort()));
                StringBuilder sb = new StringBuilder();
                //3.linux???????????? ????????????userdel -r test0429
                sb.append("userdel -r" + sysUser.getUsername());
                remoteShellExecutorUtil.commonExec(sb.toString());
                result = Result.succeed("??????shell??????user??????");
            } catch (Exception e) {
                log.error("????????????linux??????????????????", e);
                result = Result.failed("????????????linux??????????????????");
                isSuccess = false;
            }
            log.info("??????id={},linux????????????id={},?????????????????????????????????:{}", clusterId,
                    userSyncRequest.getUserId(), result.isSuccess());
            SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
            userSyncInfo.setAssName("linux????????????");
            userSyncInfo.setAssType("linux");
            userSyncInfo.setUsername(sysUser.getUsername());
            userSyncInfo.setClusterId(clusterId);
            userSyncInfo.setSyncResult(result.isSuccess());
            userSyncInfo.setUserId(userSyncRequest.getUserId());
            userSyncInfo.setUpdateTime(DateUtil.date());
            userSyncInfo.setOperator(UserSyncOperatorEnum.DELETE.getCode());
            userSyncInfo.setInfo(result.getMsg());
            try {
                userSyncInfoMapper.delete(new UpdateWrapper<SdpsUserSyncInfo>()
                        .eq("user_id", userSyncRequest.getUserId())
                        .eq("cluster_id", clusterId)
                        .eq("operator", UserSyncOperatorEnum.DELETE.getCode())
                        .eq("ass_type", "linux"));
                userSyncInfoMapper.insert(userSyncInfo);
            } catch (Exception e) {
                log.error("??????usersyncinfo={},????????????", userSyncInfo, e);
                isSuccess = false;
            }

        }
        return isSuccess;
    }

    @Override
    @Transactional
    public boolean userSyncUpdate(UserSyncRequest userSyncRequest) {
        String pass = null;
        List<Integer> clusterIds = userSyncRequest.getClusterIds();
        SysUser sysUser = new SysUser();
        try {
            Thread.sleep(1000);
            sysUser = sysUserMapper.selectById(userSyncRequest.getUserId());
            SdpsServerInfo sdpsServerInfo = serverInfoMapper
                    .selectOne(new QueryWrapper<SdpsServerInfo>()
                            .eq("user", sysUser.getUsername())
                            .eq("type", ServerTypeEnum.C.name())
                            .eq("server_id", 0));
            SysGlobalArgs sysGlobalArgs = globalArgsMapper
                    .selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
                            "password").eq("arg_key", "privateKey"));
            pass = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
                    sysGlobalArgs.getArgValue());
        } catch (Exception e) {
            log.error("????????????id={},??????linux??????????????????", userSyncRequest.getUserId(),
                    e);
            if (CollUtil.isNotEmpty(clusterIds)) {
                for (Integer clusterId : clusterIds) {
                    SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
                    userSyncInfo.setAssName("linux????????????");
                    userSyncInfo.setAssType("linux");
                    userSyncInfo.setClusterId(clusterId);
                    userSyncInfo.setSyncResult(false);
                    userSyncInfo.setUserId(userSyncRequest.getUserId());
                    userSyncInfo.setUpdateTime(DateUtil.date());
                    userSyncInfo.setOperator(UserSyncOperatorEnum.UPDATE
                            .getCode());
                    userSyncInfo.setUsername(sysUser.getUsername());
                    userSyncInfo.setInfo(e.getMessage());
                    try {
                        userSyncInfoMapper
                                .delete(new UpdateWrapper<SdpsUserSyncInfo>()
                                        .eq("user_id",
                                                userSyncRequest.getUserId())
                                        .eq("cluster_id", clusterId)
                                        .eq("operator",
                                                UserSyncOperatorEnum.UPDATE
                                                        .getCode())
                                        .eq("ass_type", "linux"));
                        userSyncInfoMapper.insert(userSyncInfo);
                    } catch (Exception e1) {
                        log.error("???????????????usersyncinfo={},????????????", userSyncInfo, e1);
                    }
                }
            }
            return false;
        }

        Result result = null;

        boolean isSuccess = true;
        for (Integer clusterId : clusterIds) {
            try {
                SdpsCluster sdpsCluster = clusterMapper.selectById(clusterId);
                //1.????????????root????????????
                SdpsServerInfo sdpsServerInfo = serverInfoMapper
                        .selectOne(new QueryWrapper<SdpsServerInfo>()
                                .eq("user", "root")
                                .eq("type", "SSH")
                                .eq("server_id", sdpsCluster.getServerId()));
                SysGlobalArgs sysGlobalArgs = globalArgsMapper
                        .selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
                                "password").eq("arg_key", "privateKey"));
                //??????shell????????????????????????
                SdpsServerInfo shellUser = serverInfoMapper
                        .selectOne(new QueryWrapper<SdpsServerInfo>()
                                .eq("user", sysUser.getUsername())
                                .eq("type", ServerTypeEnum.C.name())
                                .eq("server_id", 0));

                String password = RsaUtil.decrypt(shellUser.getPasswd(),
                        sysGlobalArgs.getArgValue());

                String passShell = RsaUtil.decrypt(sdpsServerInfo.getPasswd(),
                        sysGlobalArgs.getArgValue());
                //2,????????????shell
                log.info("host:"+sdpsServerInfo.getHost()+";user:"+sdpsServerInfo.getUser()+";pass:"+passShell);
                log.info("port:"+Integer.parseInt(sdpsServerInfo.getPort()));
                RemoteShellExecutorUtil remoteShellExecutorUtil = new RemoteShellExecutorUtil(sdpsServerInfo.getHost(),
                        sdpsServerInfo.getUser(), passShell, Integer.parseInt(sdpsServerInfo.getPort()));
                StringBuilder sb = new StringBuilder();
                //3.?????????????????????linux ????????????echo "admin1234" | passwd bpz --stdin > /dev/null 2>&1
                sb.append("echo");
                log.info("pass:"+pass);
                sb.append(" \""+pass+ "\""+ " | passwd bpz --stdin > /dev/null 2>&1");
                remoteShellExecutorUtil.commonExec(sb.toString());
                result = Result.succeed("?????? shell??????user??????");
            } catch (Exception e) {
                log.error("????????????linux??????????????????", e);
                result = Result.failed("????????????linux??????????????????");
                isSuccess = false;
            }

            SdpsUserSyncInfo userSyncInfo = new SdpsUserSyncInfo();
            userSyncInfo.setAssName("linux????????????");
            userSyncInfo.setAssType("linux");
            userSyncInfo.setClusterId(clusterId);
            userSyncInfo.setSyncResult(result.isSuccess());
            userSyncInfo.setUserId(userSyncRequest.getUserId());
            userSyncInfo.setUpdateTime(DateUtil.date());
            userSyncInfo.setOperator(UserSyncOperatorEnum.UPDATE.getCode());
            userSyncInfo.setInfo(result.getMsg());
            userSyncInfo.setUsername(sysUser.getUsername());
            try {
                userSyncInfoMapper.delete(new UpdateWrapper<SdpsUserSyncInfo>()
                        .eq("user_id", userSyncRequest.getUserId())
                        .eq("cluster_id", clusterId)
                        .eq("operator", UserSyncOperatorEnum.UPDATE.getCode())
                        .eq("ass_type", "linux"));
                userSyncInfoMapper.insert(userSyncInfo);
                if (StrUtil.equalsAnyIgnoreCase(sysUser.getUsername(),
                        CommonConstant.ADMIN_USER_NAME)) {
                    SdpsServerInfo sdpsServerInfo = new SdpsServerInfo();
                    sdpsServerInfo.setServerId(clusterId);
                    sdpsServerInfo.setUser(sysUser.getUsername());
                    sdpsServerInfo.setPasswd(UserSyncUtil
                            .getEncryptPassword(pass));
                    sdpsServerInfo.setType(ServerTypeEnum.R.name());
                    serverInfoMapper.update(
                            sdpsServerInfo,
                            new UpdateWrapper<SdpsServerInfo>()
                                    .eq("user", sysUser.getUsername())
                                    .eq("server_id", clusterId)
                                    .eq("type", ServerTypeEnum.R.name()));
                }
            } catch (Exception e) {
                log.error("??????usersyncinfo={},????????????", userSyncInfo, e);
                isSuccess = false;
            }
        }
        return isSuccess;
    }
}
