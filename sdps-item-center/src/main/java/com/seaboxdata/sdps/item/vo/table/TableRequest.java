package com.seaboxdata.sdps.item.vo.table;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.seaboxdata.sdps.item.vo.Request;

@Data
public class TableRequest extends Request implements Serializable {
	private static final long serialVersionUID = -3473131568131639127L;
	private String name;
	private Long id;
	private Long itemId;
	private Long typeId;
	private Long databaseId;
	private List<Long> ids;
	private Boolean isExternal;
}
