package com.seaboxdata.sdps.bigdataProxy.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.bigdataProxy.bean.Dictionary;
import com.seaboxdata.sdps.bigdataProxy.mapper.DictionaryMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务启动后，数据库字典表加载
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/03
 */
@Slf4j
@Component
public class DictionaryUtil {

    /**
     * 一级Map (type, List)
     */
    public Map<String, List<Dictionary>> dictionaryCacheMap = new HashedMap<>(1024);
    /**
     * 二级Map (type, (name, List))
     */
    public Map<String, Map<String, List<Dictionary>>> dictionaryCacheNewMap = new HashedMap<>(1024);

    @Autowired
    DictionaryMapper dictionaryMapper;

    @PostConstruct
    void init() {
        log.info("**************** 字典初始化...");
        List<Dictionary> dictionaries = dictionaryMapper.selectList(new QueryWrapper<>());
        // type -> typeMap
        dictionaryCacheMap = dictionaries.stream().collect(Collectors.groupingBy(Dictionary::getType));

        Map<String, List<Dictionary>> typeMap = dictionaries.stream().collect(Collectors.groupingBy(Dictionary::getType));
        // type -> typeMap -> typeNameMap
        typeMap.forEach((k, v) -> dictionaryCacheNewMap.put(k, v.stream().collect(Collectors.groupingBy(Dictionary::getName))));
        log.info("**************** 字典初始化完成");
    }
}
