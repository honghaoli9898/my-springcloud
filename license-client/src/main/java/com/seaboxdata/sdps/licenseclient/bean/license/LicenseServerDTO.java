package com.seaboxdata.sdps.licenseclient.bean.license;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 *
 */
@Data
@ApiModel("客户端服务器信息实体")
public class LicenseServerDTO{

    @ApiModelProperty("id主键")
    private Integer id;

    @ApiModelProperty("ip地址")
    @NotBlank(message = "IP地址不可为空")
    private String ipAddress;
    @ApiModelProperty("Mac地址")
    @NotBlank(message = "Mac地址不可为空!")
    private String macAddress;
    @ApiModelProperty("CPU序列号")
    @NotBlank(message = "CPU序列号不可为空!")
    private String cpuSerial;
    @ApiModelProperty("主板序列号")
    @NotBlank(message = "主板序列号不可为空!")
    private String mainBoardSerial;
}
