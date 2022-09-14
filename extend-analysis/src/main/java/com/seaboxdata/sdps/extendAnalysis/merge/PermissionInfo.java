package com.seaboxdata.sdps.extendAnalysis.merge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.apache.hadoop.fs.permission.FsPermission;

@Data
@ToString
@AllArgsConstructor
public class PermissionInfo {
    private String owner;
    private String group;
    private FsPermission permission;
}
