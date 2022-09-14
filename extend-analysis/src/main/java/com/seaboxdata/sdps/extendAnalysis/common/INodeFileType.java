package com.seaboxdata.sdps.extendAnalysis.common;

import lombok.Getter;

/**
 * hdfs目录的类型
 */
@Getter
public enum INodeFileType {
    FILE("1", "FILE"),
    DIRECTORY("2", "DIRECTORY"),
    SYMLINK("3", "SYMLINK");

    private String index;
    private String name;

    private INodeFileType(String index, String name) {
        this.index = index;
        this.name = name;
    }
}
