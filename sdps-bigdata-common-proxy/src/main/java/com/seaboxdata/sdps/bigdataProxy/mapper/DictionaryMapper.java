package com.seaboxdata.sdps.bigdataProxy.mapper;

import com.seaboxdata.sdps.bigdataProxy.bean.Dictionary;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典Mapper
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/03
 */
@Mapper
public interface DictionaryMapper extends SuperMapper<Dictionary> {

}
