package com.seaboxdata.sdps.bigdataProxy.service;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.RoleExternal;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.model.Url;
import com.seaboxdata.sdps.common.core.model.UrlVo;
import com.seaboxdata.sdps.common.framework.bean.PageRequest;
import java.util.List;
import java.util.Set;

/**
 * 链接管理服务接口
 * @author pengsong
 */
public interface UrlManageService{

    /**
     *   新增链接管理
     * @param url
     * @param user
     * @return
     */
    Boolean addUrl(Url url, SysUser user);


    /**
     *   删除链接管理
     * @param id
     * @param user
     * @return
     */
    Boolean deleteUrl(Long id, SysUser user);

    /**
     *  编辑链接管理
     * @param url
     * @param user
     * @return
     */
    Url editUrl(Url url,SysUser user);


    /**
     *  查询链接管理
     * @param url
     * @param user
     * @return
     */
    PageResult<Url> getUrl(PageRequest<Url> url, SysUser user);

    /**
     *  新增链接关系
     * @param role
     * @return
     */
    Boolean addRoleRelation(RoleExternal role);

    /**
     * 获取连接权限
     * @param url
     * @return
     */
    PageResult<RoleExternal> getRoleRelation(PageRequest<RoleExternal> url);

    /**
     * 删除链接关系
     * @param id
     * @param user
     * @return
     */
    Boolean delRoleRelation(Long id, SysUser user);

    /**
     * 通过urlId获取关联用户
     * @param urlId
     * @return
     */
    Set<String> getUsersByUrlId(Long urlId);

    /**
     * 查询所有链接
     * @param url
     * @param user
     * @return
     */
    PageResult<UrlVo> getAllUrl(PageRequest<Url> url, SysUser user);


    /**
     * 新增链接关系
     *
     * @param role
     * @return
     */
    Boolean addRoleRelations(List<RoleExternal> role, SysUser user);

    /**
     * 是否是系统定义的url
     * @param url
     * @return
     */
    String isSystemUrl(RoleExternal url);
}
