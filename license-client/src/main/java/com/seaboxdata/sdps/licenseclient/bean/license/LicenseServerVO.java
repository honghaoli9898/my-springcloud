package com.seaboxdata.sdps.licenseclient.bean.license;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 */
@Data
@ApiModel("客户端服务器信息实体")
public class LicenseServerVO{

    @ApiModelProperty("id主键")
    private Integer id;

    @ApiModelProperty("ip地址")
    private String ipAddress;

    @ApiModelProperty("Mac地址")
    private String macAddress;

    @ApiModelProperty("CPU序列号")
    private String cpuSerial;

    @ApiModelProperty("主板序列号")
    private String mainBoardSerial;

    @Override
    public String toString() {
        return "cpuSerial=" + cpuSerial +
                "&macAddress=" + macAddress +
                "&mainBoardSerial=" + mainBoardSerial +
                "&ipAddress=" + ipAddress;
    }
}
