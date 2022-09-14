package com.seabox.sdps.dynamicroute.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.seabox.sdps.dynamicroute.dto.GatewayFilterDefinition;
import com.seabox.sdps.dynamicroute.dto.GatewayPredicateDefinition;

import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@TableName(value = "gateway_routes")
public class GatewayRoutes {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	@TableField(value = "route_id")
	private String routeId;

	@TableField(value = "route_uri")
	private String routeUri;

	@TableField(value = "route_order")
	private Integer routeOrder;

	@TableField(value = "is_ebl")
	private Boolean isEbl;

	@TableField(value = "is_del")
	private Boolean isDel;

	@TableField(value = "create_time")
	private Date createTime;

	@TableField(value = "update_time")
	private Date updateTime;

	private String predicates;

	private String filters;

	/**
	 * 获取断言集合
	 * 
	 * @return
	 */
	public List<GatewayPredicateDefinition> getPredicateDefinition() {
		if (!StringUtils.isEmpty(this.predicates)) {
			return JSON.parseArray(this.predicates,
					GatewayPredicateDefinition.class);
		}
		return null;
	}

	/**
	 * 获取过滤器集合
	 * 
	 * @return
	 */
	public List<GatewayFilterDefinition> getFilterDefinition() {
		if (!StringUtils.isEmpty(this.filters)) {
			return JSON.parseArray(this.filters, GatewayFilterDefinition.class);
		}
		return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId == null ? null : routeId.trim();
	}

	public String getRouteUri() {
		return routeUri;
	}

	public void setRouteUri(String routeUri) {
		this.routeUri = routeUri == null ? null : routeUri.trim();
	}

	public Integer getRouteOrder() {
		return routeOrder;
	}

	public void setRouteOrder(Integer routeOrder) {
		this.routeOrder = routeOrder;
	}

	public Boolean getIsEbl() {
		return isEbl;
	}

	public void setIsEbl(Boolean isEbl) {
		this.isEbl = isEbl;
	}

	public Boolean getIsDel() {
		return isDel;
	}

	public void setIsDel(Boolean isDel) {
		this.isDel = isDel;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getPredicates() {
		return predicates;
	}

	public void setPredicates(String predicates) {
		this.predicates = predicates == null ? null : predicates.trim();
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters == null ? null : filters.trim();
	}

	@Override
	public String toString() {
		return "GatewayRoutes{" + "id=" + id + ", routeId='" + routeId + '\''
				+ ", routeUri='" + routeUri + '\'' + ", routeOrder="
				+ routeOrder + ", isEbl=" + isEbl + ", isDel=" + isDel
				+ ", createTime=" + createTime + ", updateTime=" + updateTime
				+ ", predicates='" + predicates + '\'' + ", filters='"
				+ filters + '\'' + '}';
	}
}