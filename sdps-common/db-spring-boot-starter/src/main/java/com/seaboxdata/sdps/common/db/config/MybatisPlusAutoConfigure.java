package com.seaboxdata.sdps.common.db.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.seaboxdata.sdps.common.core.properties.TenantProperties;
import com.seaboxdata.sdps.common.db.interceptor.CustomTenantInterceptor;
import com.seaboxdata.sdps.common.db.properties.MybatisPlusAutoFillProperties;

@EnableConfigurationProperties(MybatisPlusAutoFillProperties.class)
public class MybatisPlusAutoConfigure {
	@Autowired
	private TenantLineHandler tenantLineHandler;

	@Autowired
	private TenantProperties tenantProperties;

	@Autowired
	private MybatisPlusAutoFillProperties autoFillProperties;

	/**
	 * 分页插件，自动识别数据库类型
	 */
	@Bean
	public MybatisPlusInterceptor paginationInterceptor() {
		MybatisPlusInterceptor mpInterceptor = new MybatisPlusInterceptor();
		boolean enableTenant = tenantProperties.getEnable();
		// 是否开启多租户隔离
		if (enableTenant) {
			CustomTenantInterceptor tenantInterceptor = new CustomTenantInterceptor(
					tenantLineHandler, tenantProperties.getIgnoreSqls());
			mpInterceptor.addInnerInterceptor(tenantInterceptor);
		}
		mpInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(
				DbType.MYSQL));
		return mpInterceptor;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "zlt.mybatis-plus.auto-fill", name = "enabled", havingValue = "true", matchIfMissing = true)
	public MetaObjectHandler metaObjectHandler() {
		return new DateMetaObjectHandler(autoFillProperties);
	}

	@Bean
	public BatchSqlConfigure BatchSqlConfigure() {
		return new BatchSqlConfigure();
	}
}