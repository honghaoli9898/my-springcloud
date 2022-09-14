package com.seaboxdata.sdps.item.model;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.common.base.CaseFormat;
import com.seaboxdata.sdps.item.enums.SqlTypeAndJavaTypeEnum;
import com.seaboxdata.sdps.item.utils.DdlBuilder;
import com.seaboxdata.sdps.item.utils.SqlTypeMapUtil;

@Getter
@Setter
@ToString
public class Field {

	public String name;

	private String type;

	private Boolean primaryKey;

	private String comment;

	private Integer size;

	private Integer accuracy;
	
	private Boolean isNull;
	
	private String defaultValue;
	
	private Boolean partition;
	
	private int location;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Field field = (Field) o;
		return Objects.equals(name, field.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public static Field newField(String name, String type, boolean primaryKey,
			String comment) {
		return new Field(name, type, primaryKey, comment);
	}

	public static Field newField(String name, String type) {
		return new Field(name, type);
	}

	public String getTableColumn() {
		return CaseFormat.LOWER_CAMEL
				.to(CaseFormat.LOWER_UNDERSCORE, this.name);
	}

	public String getSqlType() {
		SqlTypeMapUtil.ConvertBean convertBean = SqlTypeMapUtil.getInstance()
				.typeConvert(this.type);
		if (null != convertBean) {
			if (Objects.nonNull(this.size)) {
				DdlBuilder ddlBuilder = new DdlBuilder();
				ddlBuilder.LeftParenthesis(false).addSize(this.size);
				if (Objects.nonNull(this.accuracy)) {
					ddlBuilder.addComma().addSize(this.accuracy);
				}
				ddlBuilder.rightParenthesis();
				convertBean.setSqlTypeLength(ddlBuilder.result());
			}
			return convertBean.getSqlType() + convertBean.getSqlTypeLength();
		}
		/* 兜底配置 */
		return getSqlTypeForMapping() + getSqlTypeSize();
	}

	/**
	 * 获取mysql类型
	 */
	public String getSqlTypeForMapping() {
		/* 类型映射 */
		return SqlTypeAndJavaTypeEnum.findByJavaType(this.type).getSqlType();
	}

	public String getSqlTypeSize() {
		return SqlTypeAndJavaTypeEnum.findByJavaType(this.type)
				.getDefaultLength();
	}

	public Field() {
	}

	public Field(String name, String type) {
		this.name = name;
		this.type = type;
		this.primaryKey = false;
	}

	public Field(String name, String type, Boolean primaryKey, String comment) {
		this.name = name;
		this.type = type;
		this.primaryKey = primaryKey;
		this.comment = comment;
	}

	public Field(String name, String type, Boolean primaryKey, String comment,
			Integer size) {
		this.name = name;
		this.type = type;
		this.primaryKey = primaryKey;
		this.comment = comment;
		this.size = size;
	}

	public Field(String name, String type, Boolean primaryKey, String comment,
			Integer size, Integer accuracy) {
		this.name = name;
		this.type = type;
		this.primaryKey = primaryKey;
		this.comment = comment;
		this.size = size;
		this.accuracy = accuracy;
	}

}
