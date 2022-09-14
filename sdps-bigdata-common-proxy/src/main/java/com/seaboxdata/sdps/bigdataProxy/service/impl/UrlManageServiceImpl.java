package com.seaboxdata.sdps.bigdataProxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.seaboxdata.sdps.bigdataProxy.mapper.ExternalRoleMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.RoleExternalMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.UrlManageMapper;
import com.seaboxdata.sdps.bigdataProxy.service.UrlManageService;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.constant.UrlConstants;
import com.seaboxdata.sdps.common.core.model.ExternalRole;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.RoleExternal;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.model.Url;
import com.seaboxdata.sdps.common.core.model.UrlRelationVo;
import com.seaboxdata.sdps.common.core.model.UrlVo;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 链接管理业务
 * @author pengsong
 */
@Slf4j
@Service
public class UrlManageServiceImpl implements UrlManageService{


    @Autowired
    private UrlManageMapper urlManageMapper;

    @Autowired
    private RoleExternalMapper roleExternalMapper;

    @Autowired
    private ExternalRoleMapper externalRoleMapper;

    @Override
    public Boolean addUrl(Url url, SysUser user) {
        try {
            url.setCreator(user.getUsername());
            url.setVerifyStatus(UrlConstants.UNAPPLY_STATUS);
            urlManageMapper.insert(url);
        } catch (Exception e) {
            log.error("url插入异常");
            return false;
        }
        return true;
    }

    @Override
    public Boolean deleteUrl(Long id, SysUser user) {
        try {
            if(Objects.isNull(user) || Objects.isNull(id) || Objects.isNull(urlManageMapper.selectById(id))) {
                return false;
            }
            urlManageMapper.deleteById(id);
        } catch (Exception e) {
            log.error("url插入异常");
            return false;
        }
        return true;
    }

    @Override
    public Url editUrl(Url url, SysUser user) {
        Url urlDb = null;
        try {
            urlDb = urlManageMapper.selectById(url.getId());
            if(Objects.isNull(urlDb)) {
                return urlDb;
            }
            urlDb.setName(url.getName());
            urlDb.setDescription(url.getDescription());
            urlDb.setUrl(url.getUrl());
            urlDb.setModifier(user.getUsername());
            urlDb.setModifyTime(new Date());
            urlDb.setStatus(url.getStatus());
            urlManageMapper.updateById(urlDb);
        } catch (Exception e) {
            log.error("url插入异常");
            return urlDb;
        }
        return urlDb;
    }

    @Override
    public PageResult<Url> getUrl(PageRequest<Url> url, SysUser user) {
        //修复链接管理分页问题
        QueryWrapper<Url> queryWrapper = new QueryWrapper<>();
        queryWrapper.clear();
        if(!StringUtils.equals(user.getUsername(), CommonConstant.ADMIN_USER_NAME)) {
            queryWrapper.eq("creator",url.getParam().getCreator());
        }
        if(StringUtils.isNotBlank(url.getParam().getName())) {
            queryWrapper.like("name", url.getParam().getName());
        }
        if(!Objects.isNull(url.getParam().getVerifyStatus())) {
            queryWrapper.eq("verify_status", url.getParam().getVerifyStatus());
        }
        //queryWrapper.eq("status",UrlConstants.OK_STATUS);
        List<Url> urls = urlManageMapper.selectList(queryWrapper);


        PageHelper.startPage(url.getPage(), url.getSize());


        PageResult<Url> success = (PageResult<Url>) PageResult.success((long) urls.size(), urls);
        return success;
    }

    @Override
    public Boolean addRoleRelation(RoleExternal role) {
        role.setDelStatus(UrlConstants.OK_STATUS);
        role.setVerifyStatus(UrlConstants.APPLYING_STATUS);
        role.setCreator(role.getUsername());
        role.setCreateTime(new Date());
        roleExternalMapper.insert(role);
        return null;
    }



    /**
     * 是否为外部系统的url
     * @param urlDb
     * @param systems
     * @return
     */
    public boolean isExternalUrl(Url urlDb, List<ExternalRole> systems){
        AtomicBoolean res = new AtomicBoolean(false);
        systems.forEach(t->{
            if(urlDb.getUrl().contains(t.getSystemName()) || urlDb.getUrl().contains(t.getSystemName().toLowerCase(Locale.ROOT))
                    || urlDb.getName().contains(t.getSystemName()) || urlDb.getName().contains(t.getSystemName().toLowerCase(
                    Locale.ROOT))){
                res.set(true);
            }
        });
        return res.get();
    }


    /**
     * 是否为外部系统的url
     * @param urlDb
     * @param systems
     * @return
     */
    public String externalSystem(Url urlDb, List<ExternalRole> systems){
        AtomicReference<String> res = new AtomicReference<>("");
        systems.forEach(t->{
            if(urlDb.getUrl().contains(t.getSystemName()) || urlDb.getUrl().contains(t.getSystemName().toLowerCase(Locale.ROOT))
                    || urlDb.getName().contains(t.getSystemName()) || urlDb.getName().contains(t.getSystemName().toLowerCase(
                    Locale.ROOT))){
                res.set(t.getSystemName());
            }
        });
        return res.get();
    }


    @Override
    public PageResult<RoleExternal> getRoleRelation(PageRequest<RoleExternal> url) {
        //url未包含外部角色的系统名称，不允许配置角色权限
        QueryWrapper<ExternalRole> externalRoleQueryWrapper = new QueryWrapper<>();
        externalRoleQueryWrapper.select("DISTINCT system_name");
        List<ExternalRole> systems = externalRoleMapper.selectList(externalRoleQueryWrapper);


        //获取url链接名称
        Url urlDb = urlManageMapper.selectById(url.getParam().getUrlId());
        boolean isSystemUrl = isExternalUrl(urlDb, systems);
        if(!isSystemUrl){
            PageResult pageResult = new PageResult();
            pageResult.setCode(1);
            pageResult.setMsg("非海盒产品定义的链接，无需分配角色权限。");
            return pageResult;
        }


        List<UrlRelationVo> urls = roleExternalMapper
                .getUrlRelation(url.getParam().getExternalId(), url.getParam().getUrlId(), url.getParam().getUsername());
        PageHelper.startPage(url.getPage(), url.getSize());
        PageResult<RoleExternal> success = (PageResult<RoleExternal>) PageResult.success((long) urls.size(), urls);
        return success;
    }

    @Override
    public Boolean delRoleRelation(Long id, SysUser user) {
        if(StringUtils.equals(CommonConstant.ADMIN_USER_NAME, user.getUsername())) {
            RoleExternal roleExternal = roleExternalMapper.selectById(id);
            if(!Objects.isNull(roleExternal)){
                roleExternal.setDelStatus(UrlConstants.DOWN_STATUS);
                roleExternalMapper.updateById(roleExternal);
            }

        }
        return false;
    }

    @Override
    public Set<String> getUsersByUrlId(Long urlId) {
        QueryWrapper<RoleExternal> queryWrapper = new QueryWrapper<>();
        Set<String> res = new HashSet<>();
        queryWrapper.eq("url_id",urlId);
        queryWrapper.eq("del_status",UrlConstants.OK_STATUS);
        queryWrapper.eq("verify_status",UrlConstants.APPLY_SUCCESS_STATUS);
        List<RoleExternal> roleExternals = roleExternalMapper.selectList(queryWrapper);
        roleExternals.forEach(url->{
            res.add(url.getUsername());
        });
        return res;
    }

    @Override
    public PageResult<UrlVo> getAllUrl(PageRequest<Url> url, SysUser user) {
        List<UrlVo> urls = roleExternalMapper.getAllUrl(url.getParam().getVerifyStatus(),url.getParam().getName(),user.getUsername());
        PageHelper.startPage(url.getPage(), url.getSize());
        PageResult<UrlVo> success = (PageResult<UrlVo>) PageResult.success((long) urls.size(), urls);
        return success;
    }

    @Override
    @Transactional
    public Boolean addRoleRelations(List<RoleExternal> role, SysUser user) {
        role.forEach(t->{
            t.setDelStatus(UrlConstants.OK_STATUS);
            t.setVerifyStatus(UrlConstants.APPLY_SUCCESS_STATUS);
            t.setCreator(user.getUsername());
            QueryWrapper<RoleExternal> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("url_id",t.getUrlId());
            roleExternalMapper.delete(queryWrapper);
        });
        role.forEach(t->{
            t.setDelStatus(UrlConstants.OK_STATUS);
            t.setVerifyStatus(UrlConstants.APPLY_SUCCESS_STATUS);
        });
        roleExternalMapper.insertBatchSomeColumn(role);
        return true;
    }

    @Override
    public String isSystemUrl(RoleExternal url) {
        //url未包含外部角色的系统名称，不允许配置角色权限
        QueryWrapper<ExternalRole> externalRoleQueryWrapper = new QueryWrapper<>();
        externalRoleQueryWrapper.select("DISTINCT system_name");
        List<ExternalRole> systems = externalRoleMapper.selectList(externalRoleQueryWrapper);


        //获取url链接名称
        Url urlDb = urlManageMapper.selectById(url.getUrlId());
        String isSystemUrl = externalSystem(urlDb, systems);
        return isSystemUrl;
    }

    /**
     * 更新链接状态
     * @param urlId
     * @param status
     */
    public void updateUrlStatus(Long urlId,Integer status){
        Url urlDb = urlManageMapper.selectById(urlId);
        if(Objects.isNull(urlDb)) {
            return ;
        }
        urlDb.setStatus(status);
        urlManageMapper.updateById(urlDb);
    }


}
