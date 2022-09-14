package com.seaboxdata.sdps.sshweb.pojo;

import lombok.Data;

@Data
public class Reboot{

    private String deviceIp;
    private Integer port;
    private String username;
    private String password;
    private String command;

}
