package com.seaboxdata.sdps.webssh.user.service;


import com.seaboxdata.sdps.common.core.model.DocumentContent;
import com.seaboxdata.sdps.common.core.model.DocumentVo;
import com.seaboxdata.sdps.common.core.model.SysUser;
import java.util.List;

/**
 * @author pengsong
 */
public interface DocumentService{

    /**
     * 获取文档树
     */
    List<DocumentVo> getDocumentTree();

    /**
     * 新增文档
     * @param content
     * @return
     */
    Boolean addContent(DocumentContent content, SysUser user);

    /**
     * 修改文档
     * @param content
     * @param user
     * @return
     */
    DocumentContent editContent(DocumentContent content, SysUser user);

    /**
     * 获取文档内容
     * @param content
     * @return
     */
    DocumentContent getContent(DocumentContent content);

    /**
     * 读文档
     * @param content
     * @return
     */
    DocumentContent readDocument(DocumentContent content);
}
