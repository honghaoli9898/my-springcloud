package com.seaboxdata.sdps.user.api;

import java.util.List;
import java.util.Map;



import com.alibaba.fastjson.JSONObject;
import com.seaboxdata.sdps.common.core.model.LoginAppUser;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.core.model.UrlUserVo;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.user.mybatis.model.SdpsUserSyncInfo;
import com.seaboxdata.sdps.user.mybatis.model.UserGroup;
import com.seaboxdata.sdps.user.mybatis.vo.user.UserRequest;
import com.seaboxdata.sdps.user.vo.user.CreateUserVo;
import com.seaboxdata.sdps.user.vo.user.ImportUserCsvVo;
import com.seaboxdata.sdps.user.vo.user.PageUserRequest;
import com.seaboxdata.sdps.user.vo.user.UserVo;

public interface IUserService extends ISuperService<SysUser> {
    public PageResult<SysUser> findUsers(PageUserRequest request);

    public void createUserAllInfo(CreateUserVo createUserVo);

    public void deleteUserAllInfo(List<Long> ids);

    public void insertUserItemRole(UserVo userVo);

    public void updateUserItemRole(UserVo userVo);

    public void removeUserFromGroups(UserRequest request);

    public void addUserToGroup(List<UserGroup> userGroup);

    public List<SysUser> findUserList(UserRequest sysUser);

    public void updateUserInfo(SysUser sysUser);

    public void batchImportUserInfo(List<ImportUserCsvVo> rows);

    public LoginAppUser findByUsername(String username);

    public SysUser selectByUsername(String username);

    public LoginAppUser getLoginAppUser(SysUser sysUser);

    public Result saveOrUpdateUser(SysUser sysUser) throws Exception;

    public Result updatePassword(Long id, String oldPassword, String newPassword, Boolean syncUser);

    public SdpsServerInfo selectServerUserInfo(String username, String type, String argType);

    public Map<String, String> serverLogin(String clusterId, String type, String username, Boolean isCache);

    public SysUser findUserByEmail(String email);

    /**
     * 获取用户链接关系
     * @param urlId
     * @param user
     * @return
     */
    public UrlUserVo getUrlUsers(Long urlId, SysUser user);

    /**
     * 根据用户id获取用户信息
     * @param userId
     * @return
     */
    public SysUser selectByUserId(String userId);

    public List<SdpsUserSyncInfo> getUserSyncInfo(Long userId);

	public JSONObject getRolesByUserId(Long userId);

	public void insertTenantUsers(UserVo userVo);

}
