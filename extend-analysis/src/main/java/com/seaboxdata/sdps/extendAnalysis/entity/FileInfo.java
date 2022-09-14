package com.seaboxdata.sdps.extendAnalysis.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FileInfo implements Serializable {
    private static final long serialVersionUID = -7967262717011983649L;
    private String path;
    private String replication;
    private String modificationTime;
    private String accessTime;
    private String preferredBlockSize;
    private String blockCount;
    private String fileSize;
    private String nsQuota;
    private String dsQuota;
    private String storagePolicy;
    private String fileType;
    private String pathIndex;
    private String parentPath;

    public FileInfo(ImageFileInfo imageFile) {
        this.path = imageFile.getPath();
        this.replication = imageFile.getReplication();
        this.modificationTime = imageFile.getModificationTime();
        this.accessTime = imageFile.getAccessTime();
        this.preferredBlockSize = imageFile.getPreferredBlockSize();
        this.blockCount = imageFile.getBlockCount();
        this.fileSize = imageFile.getFileSize();
        this.nsQuota = imageFile.getNsQuota();
        this.dsQuota = imageFile.getDsQuota();
        this.storagePolicy = imageFile.getStoragePolicy();
        this.fileType = imageFile.getFileType();
    }
}