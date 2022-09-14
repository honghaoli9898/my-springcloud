package com.seaboxdata.sdps.common.framework.bean;

import lombok.Data;

/**
 * 主机数据对象
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/14
 */
@Data
public class SdpsClusterHost {
    private String ip;
    /** 域名 */
    private String domainName;
    private String name;
    private String passwd;
}
