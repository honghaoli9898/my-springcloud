package com.seaboxdata.sdps.uaa.oauth.dto;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import com.seaboxdata.sdps.uaa.oauth.model.Client;

@Setter
@Getter
public class ClientDto extends Client {
    private static final long serialVersionUID = 1475637288060027265L;

    private List<Long> permissionIds;

    private Set<Long> serviceIds;
}
