package com.seaboxdata.sdps.extendAnalysis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileStatsInfo implements Serializable {

    private static final long serialVersionUID = 6154887250299379146L;
    private Long num;
    private Long size;

    public FileStatsInfo add(FileStatsInfo other) {
        this.num = this.num + other.getNum();
        this.size = this.size + other.getSize();
        return new FileStatsInfo(this.num, this.size);
    }
}
