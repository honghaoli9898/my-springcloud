package com.seaboxdata.sdps.item.dto.table;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.seaboxdata.sdps.item.model.SdpsTable;

@Data
@EqualsAndHashCode(callSuper = false)
public class TableDto extends SdpsTable {
	private static final long serialVersionUID = 6571662351574657455L;
	private String type;
	private String assItemName;
	private String databaseName;
	private String assItemIden;
	private String datasourceName;
	private String datasourceId;
	private String username;
	private String nickname;
}
