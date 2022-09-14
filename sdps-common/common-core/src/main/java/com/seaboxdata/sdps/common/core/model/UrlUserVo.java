package com.seaboxdata.sdps.common.core.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 *  url关联用户
 * @author  pengsong
 */
@Data
public class UrlUserVo{
    public List<SysUser> checkUsers = new ArrayList<>();
    public List<SysUser> unCheckUsers = new ArrayList<>();
}
