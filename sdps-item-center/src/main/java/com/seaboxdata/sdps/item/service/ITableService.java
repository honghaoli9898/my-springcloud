package com.seaboxdata.sdps.item.service;

import com.seaboxdata.sdps.common.core.model.PageResult;
import com.seaboxdata.sdps.common.core.service.ISuperService;
import com.seaboxdata.sdps.item.dto.table.TableDto;
import com.seaboxdata.sdps.item.model.SdpsTable;
import com.seaboxdata.sdps.item.vo.table.TableRequest;

public interface ITableService extends ISuperService<SdpsTable> {

	void saveAndCreateTable(SdpsTable table);

	PageResult<TableDto> findTables(Integer page, Integer size,
			TableRequest param);

	void deleteTableByIds(TableRequest request);


}
