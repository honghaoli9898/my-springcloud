package com.seaboxdata.sdps.user.mybatis.vo.group;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GroupRequest implements Serializable {

	private static final long serialVersionUID = -509277376775661013L;

	private Long id;

	private Long itemId;

	private Long userId;

	private String name;

	private String code;

	private List<Long> ids;

	private Integer members = 0;

	private String desc;

}
