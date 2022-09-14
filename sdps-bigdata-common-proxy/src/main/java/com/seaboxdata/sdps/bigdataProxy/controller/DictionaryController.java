package com.seaboxdata.sdps.bigdataProxy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.bean.Dictionary;
import com.seaboxdata.sdps.bigdataProxy.mapper.DictionaryMapper;
import com.seaboxdata.sdps.bigdataProxy.mapper.SdpsClusterServiceTemplateConfigMapper;
import com.seaboxdata.sdps.bigdataProxy.util.DictionaryUtil;
import com.seaboxdata.sdps.bigdataProxy.util.ListUtil;
import com.seaboxdata.sdps.common.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典Controller
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/03
 */
@RestController
@RequestMapping("/dictionary")
public class DictionaryController {

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private DictionaryUtil dictionaryUtil;

    @Autowired
    SdpsClusterServiceTemplateConfigMapper serviceTemplateConfigMapper;

    /**
     * 所有字典
     */
    @GetMapping("/all")
    public Result<List<Dictionary>> getAll() {
        return Result.succeed(dictionaryMapper.selectList(new QueryWrapper<>()));
    }

    /**
     * 更新字典缓存
     */
    @PutMapping("/updateDictionary")
    public Result<Map<String, List<Dictionary>>> updateDictionaryList() {
        dictionaryUtil.dictionaryCacheMap = dictionaryMapper.selectList(new QueryWrapper<>())
                .stream().collect(Collectors.groupingBy(Dictionary::getName));
        return Result.succeed(dictionaryUtil.dictionaryCacheMap);
    }

    /**
     * 获取缓存字典
     */
    @GetMapping("/getDictionaryCache")
    public Result<Map<String, List<Dictionary>>> getDictionaryCache() {
        return Result.succeed(dictionaryUtil.dictionaryCacheMap);
    }

    /**
     * 根据类型查询name集合
     * @param type
     * @return
     */
    @GetMapping("/getNamesByType")
    public Result getNamesByType(String type) {
        QueryWrapper<Dictionary> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("name")
                .eq("type", type);
        List<Dictionary> dictionaries = dictionaryMapper.selectList(queryWrapper);
        return Result.succeed(dictionaries.stream().map(Dictionary::getName).collect(Collectors.toList()));
    }

    /**
     * 查询yarn任务状态
     * @return
     */
    @GetMapping("/getAppStates")
    public Result getAppStates() {
        QueryWrapper<Dictionary> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("value")
                .eq("type", "yarn")
                .eq("name", "applicationState");
        List<Dictionary> dictionaries = dictionaryMapper.selectList(queryWrapper);
        return Result.succeed(dictionaries.stream().map(Dictionary::getValue).collect(Collectors.toList()));
    }

    /**
     * 根据类型和名称字典, 这俩个字段唯一一条记录
     *
     * @param type 类型
     * @param name 名称
     */
    @GetMapping("/getByTypeAndName")
    public Result<Dictionary> getByNameAndType(String type, String name) {
        List<Dictionary> dictionaries = dictionaryMapper.selectList(new QueryWrapper<Dictionary>().eq("type", type).eq("name", name));
        return Result.succeed(ListUtil.isNotEmpty(dictionaries) ? dictionaries.get(0) : null);
    }

}
