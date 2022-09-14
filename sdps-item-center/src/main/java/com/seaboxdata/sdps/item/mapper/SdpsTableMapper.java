package com.seaboxdata.sdps.item.mapper;

import org.apache.ibatis.annotations.Param;

import com.github.pagehelper.Page;
import com.seaboxdata.sdps.common.db.mapper.SuperMapper;
import com.seaboxdata.sdps.item.dto.table.TableDto;
import com.seaboxdata.sdps.item.model.SdpsTable;
import com.seaboxdata.sdps.item.vo.table.TableRequest;

public interface SdpsTableMapper extends SuperMapper<SdpsTable> {

	Page<TableDto> findTablesByExample(@Param("param") TableRequest param);
}