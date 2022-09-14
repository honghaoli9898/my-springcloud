package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.common.core.model.RoleExternal;
import com.seaboxdata.sdps.common.core.model.UrlRelationVo;
import com.seaboxdata.sdps.common.core.model.UrlVo;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author pengsong
 */
@Mapper
public interface RoleExternalMapper extends SuperMapper<RoleExternal>{

    /**
     * 获取链接关系
     * @param roleId
     * @param username
     * @return
     */
    List<UrlRelationVo> getUrlRelation(@Param(value = "roleId") Long roleId, @Param(value = "urlId") Long urlId, @Param(value = "username") String username);


    /**
     *  获取全部链接
     * @param verifyStatus
     * @param name
     * @param username
     * @return
     */
    List<UrlVo> getAllUrl(@Param(value = "verifyStatus") Integer verifyStatus, @Param(value = "name") String name, @Param(value = "username") String username);


}
