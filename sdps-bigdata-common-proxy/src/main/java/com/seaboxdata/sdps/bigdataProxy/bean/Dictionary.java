package com.seaboxdata.sdps.bigdataProxy.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.seaboxdata.sdps.common.core.model.SuperEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类
 *
 * @author jiaohongtao
 * @version 1.0.0
 * @since 2021/12/03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sdps_dictionary")
public class Dictionary extends SuperEntity<Dictionary> {

    /**
     * 字典类型
     */
    private String type;

    /**
     * key
     */
    private String name;
    /**
     * value
     */
    private String value;
    /**
     * 备注
     */
    private String remark;
}
