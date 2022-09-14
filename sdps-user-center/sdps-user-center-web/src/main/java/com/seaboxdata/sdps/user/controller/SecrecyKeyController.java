package com.seaboxdata.sdps.user.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.user.api.IUserService;
import com.seaboxdata.sdps.user.api.SecrecyKeyService;
import com.seaboxdata.sdps.user.mybatis.model.SecrecyKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * 密钥管理操作处理
 *
 * @author SU
 */
@RestController
@RequestMapping(value = "/secrecy/")
public class SecrecyKeyController {

    private static final Logger logger = LoggerFactory.getLogger(SecrecyKeyController.class);

    @Autowired
    private SecrecyKeyService secrecyKeyService;

    @Autowired
    private IUserService iUserService;

    /**
     * 获取密钥分页列表
     * @return
     */
    @PostMapping(value = "/pageList")
    public Result pageList(@RequestBody HashMap<String,Object> map) {
        try {
            String name = (String)map.get("name");
            Integer pageNum = (Integer)map.get("pageNum");
            Integer pageSize = (Integer)map.get("pageSize");
            PageHelper.startPage(pageNum, pageSize);
            SecrecyKey secrecyKey = new SecrecyKey();
            secrecyKey.setName(name);
            List<SecrecyKey> list= secrecyKeyService.selectList(secrecyKey);
            PageInfo pageInfo = new PageInfo<>(list);

            logger.info("获取密钥分页列表成功");
            return Result.succeed(pageInfo);
        }catch (Exception e){
            logger.error("获取密钥分页列表失败:",e);
            return Result.failed("1","获取密钥分页列表失败");
        }
    }

    /**
     * 获取密钥列表
     */
    @PostMapping(value = "/list")
    public Result list(@RequestBody SecrecyKey secrecyKey) {
        try {
            List<SecrecyKey> list= secrecyKeyService.selectList(secrecyKey);
//            list.stream().map(i ->{
//                i.setSecrecyKey(null);
//                return i;
//            }).collect(Collectors.toList());
            logger.info("获取列表成功");
            return Result.succeed(list);
        }catch (Exception e){
            logger.error("获取列表失败:",e);
            return Result.failed("1","获取列表失败");
        }
    }

    /**
     * 添加密钥
     */
    @PostMapping(value = "/add")
    public Result add(@RequestBody SecrecyKey secrecyKey){

        try {
            SysUser userName = iUserService.selectByUserId(secrecyKey.getUserId().toString());
            secrecyKey.setCreateBy(userName.getUsername());

            final List<SecrecyKey> list = secrecyKeyService.selectListByName(secrecyKey);
            if (!list.isEmpty()){
                return Result.failed("1001", "密钥名称重复");
            }
            secrecyKeyService.insert(secrecyKey);
            logger.info("添加密钥成功");
            return Result.succeed("0","添加密钥成功");
        }catch (Exception e){
            logger.error("添加密钥失败:",e);
            return Result.failed("1","添加密钥失败");
        }
    }

    /**
     * 修改密钥
     */
    @PostMapping(value = "/edit")
    public Result edit(@RequestBody SecrecyKey secrecyKey){

        try {
            final List<SecrecyKey> list = secrecyKeyService.selectListByName(secrecyKey);
            if (!list.isEmpty()){
                return Result.failed("1001", "密钥名称重复");
            }

            SysUser userName = iUserService.selectByUserId(secrecyKey.getUserId().toString());
            secrecyKey.setUpdateBy(userName.getUsername());

            secrecyKeyService.update(secrecyKey);
            logger.info("修改密钥成功");
            return Result.succeed("0","修改密钥成功");
        }catch (Exception e){
            logger.error("修改密钥失败:",e);
            return Result.failed("1","修改密钥失败");
        }
    }

    /**
     * 删除密钥
     */
    @DeleteMapping(value = "/delete/{ids}")
    public Result delete(@PathVariable Long[] ids){

        try {
            secrecyKeyService.delete(ids);
            logger.info("删除密钥成功");
            return Result.succeed("0","删除密钥成功");
        }catch (Exception e){
            logger.error("删除密钥失败:",e);
            return Result.failed("1","删除密钥失败");
        }
    }

}
