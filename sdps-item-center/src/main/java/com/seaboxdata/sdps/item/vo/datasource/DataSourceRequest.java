package com.seaboxdata.sdps.item.vo.datasource;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.seaboxdata.sdps.item.vo.Request;

@Data
public class DataSourceRequest extends Request implements Serializable {
	private static final long serialVersionUID = 664548808850955647L;
	private Long id;
	private String category;

	private String name;

	private Long typeId;

	private Long itemId;

	private List<Long> ids;

	private String properties;

	private String sql;

	private List<String> names;
	
	private Boolean isValid;
	
	private Long clusterId;
	
	private String username;
	
	private Long userId;
}
