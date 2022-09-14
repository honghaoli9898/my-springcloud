package com.seaboxdata.sdps.common.core.model;


import java.util.List;
import lombok.Data;

@Data
public class DocumentVo{
    private Integer id;

    private Integer parentId;

    private String documentName;

    private Integer isParent;

    private Integer documentLayer;

    private List<Document> children;

}
