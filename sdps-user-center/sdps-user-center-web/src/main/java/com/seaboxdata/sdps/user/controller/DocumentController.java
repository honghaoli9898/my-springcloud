package com.seaboxdata.sdps.user.controller;

import com.seaboxdata.sdps.common.core.annotation.LoginUser;
import com.seaboxdata.sdps.common.core.model.DocumentContent;
import com.seaboxdata.sdps.common.core.model.DocumentVo;
import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.webssh.user.service.DocumentService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档中心控制器
 * @author pengsong
 */
@RestController
@RequestMapping("/document")
public class DocumentController{
    @Autowired
    private DocumentService documentService;

    @PostMapping("/getDocumentTree")
    public Result getDocumentTree() {
        List<DocumentVo> res = documentService.getDocumentTree();
        return Result.succeed(res);
    }

    @PostMapping("/addContent")
    public Result addContent(@RequestBody DocumentContent content,@LoginUser(isFull = true) SysUser user){
        Boolean res = documentService.addContent(content, user);
        return Result.succeed(res);
    }

    @PostMapping("/editContent")
    public Result editContent(@RequestBody DocumentContent content,@LoginUser(isFull = true) SysUser user){
        DocumentContent res = documentService.editContent(content, user);
        return Result.succeed(res);
    }

    @PostMapping("/getContent")
    public Result getContent(@RequestBody DocumentContent content){
        DocumentContent res = documentService.getContent(content);
        return Result.succeed(res);
    }

    @PostMapping("/readDocument")
    public Result readDocument(@RequestBody DocumentContent content){
        DocumentContent res = documentService.readDocument(content);
        return Result.succeed(res);
    }

}
