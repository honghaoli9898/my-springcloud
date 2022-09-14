package com.seaboxdata.sdps.item.vo.database;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.seaboxdata.sdps.item.vo.Request;

@Getter
@Setter
@ToString
public class DatabaseRequest extends Request implements Serializable {
	private static final long serialVersionUID = -8378484913748425741L;
	private Long id;
	private String name;
	private Long itemId;
	private List<Long> ids;
	private Long typeId;
	private Long clusterId;
}
