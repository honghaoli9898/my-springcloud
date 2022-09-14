package com.seaboxdata.sdps.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.model.Document;
import com.seaboxdata.sdps.common.core.model.DocumentContent;
import com.seaboxdata.sdps.common.core.model.DocumentVo;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.mybatis.mapper.DocumentContentMapper;
import com.seaboxdata.sdps.user.mybatis.mapper.DocumentMapper;
import com.seaboxdata.sdps.user.mybatis.model.SdpsMessage;
import com.seaboxdata.sdps.webssh.user.service.DocumentService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文档树业务类
 * @author pengsong
 */
@Service
public class DocumentServiceImpl implements DocumentService{

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentContentMapper contentMapper;


    @Override
    public List<DocumentVo> getDocumentTree() {
        QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",0);
        List<Document> documents = documentMapper.selectList(queryWrapper);
        List<DocumentVo> res = new ArrayList<>();
        documents.forEach(t-> {
            QueryWrapper<Document> query = new QueryWrapper<>();
            query.eq("parent_id",t.getId());
            List<Document> docs = documentMapper.selectList(query);
            DocumentVo documentVo = new DocumentVo();
            BeanUtils.copyProperties(t, documentVo);
            documentVo.setChildren(docs);
            res.add(documentVo);
        });
        return res;
    }

    @Override
    public Boolean addContent(DocumentContent content, SysUser user) {

        //文档树id校验
        QueryWrapper<Document> queryWrapper = new QueryWrapper<>();
        List<Document> documents = documentMapper.selectList(queryWrapper);
        ArrayList<Integer> ids = new ArrayList<>();
        documents.forEach(t->{
            ids.add(t.getId());
        });
        if(!ids.contains(content.getDocumentId())) {
            throw new BusinessException("非法文档id");
        }

        //
        QueryWrapper<DocumentContent> query = new QueryWrapper<>();
        List<DocumentContent> documentContents = contentMapper.selectList(query);
        ArrayList<Integer> contents = new ArrayList<>();
        documentContents.forEach(t->{
            contents.add(t.getDocumentId());
        });
        if(contents.contains(content.getDocumentId())) {
            throw new BusinessException("该文档下已有文档，不能新增文档");
        }

        try {
            content.setCreator(user.getUsername());
            content.setCreatTime(new Date());
            contentMapper.insert(content);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public DocumentContent editContent(DocumentContent content, SysUser user) {

        if(!StringUtils.equals(user.getUsername(), CommonConstant.ADMIN_USER_NAME)) {
            throw new BusinessException("非管理员用户无编辑权限");
        }
        QueryWrapper<DocumentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("document_id",content.getDocumentId());
        DocumentContent documentContent = contentMapper.selectOne(queryWrapper);
        documentContent.setContent(content.getContent());
        documentContent.setCreatTime(new Date());
        contentMapper.updateById(documentContent);
        return documentContent;
    }

    @Override
    public DocumentContent getContent(DocumentContent content) {
        QueryWrapper<DocumentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("document_id",content.getDocumentId());
        DocumentContent documentContent = contentMapper.selectOne(queryWrapper);
        return documentContent;
    }

    @Override
    public DocumentContent readDocument(DocumentContent content) {

        QueryWrapper<DocumentContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("document_id",content.getDocumentId());
        DocumentContent documentContent = contentMapper.selectOne(queryWrapper);
        if(Objects.isNull(documentContent)) {
            throw new BusinessException("非法的文档id");
        }
        Integer count = documentContent.getCount();
        if(Objects.isNull(count)){
            count = 0;
        }
        ++count;
        documentContent.setCount(count);
        contentMapper.updateById(documentContent);
        return documentContent;
    }
}
