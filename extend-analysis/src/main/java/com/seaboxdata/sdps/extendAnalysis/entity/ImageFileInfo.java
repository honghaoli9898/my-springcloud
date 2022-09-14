package com.seaboxdata.sdps.extendAnalysis.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;

/**
 * hdfs imageFile
 */
@Data
@NoArgsConstructor
public class ImageFileInfo implements Serializable {
    private static final long serialVersionUID = 8599812644897569039L;
    private String path;                    //路径
    private String replication;             //副本数
    private String modificationTime;        //修改时间
    private String accessTime;              //访问时间、存取时间
    private String preferredBlockSize;      //首选的块大小
    private String blockCount;              //块数量
    private String fileSize;                //文件大小
    private String nsQuota;                 //文件目录数量配额
    private String dsQuota;                 //目录空间配额
    private String storagePolicy;           //存储策略
    private String fileType;                //节点类型(文件:1,目录:2,链接:3)

    public static StructType getSchema() {
        return (new StructType())
                .add("path", "string")
                .add("replication", "string")
                .add("modificationTime", "string")
                .add("accessTime", "string")
                .add("preferredBlockSize", "string")
                .add("blockCount", "string")
                .add("fileSize", "string")
                .add("nsQuota", "string")
                .add("dsQuota", "string")
                .add("storagePolicy", "string")
                .add("fileType", "string");
    }

}
