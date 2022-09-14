package com.seaboxdata.sdps.licenseclient.service.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

//去掉配置second.spring.datasource，或者把这个类移除
@Configuration
@MapperScan(basePackages = "com.seaboxdata.sdps.licenseclient.seconddao", sqlSessionFactoryRef = "secondSqlSessionFactory")
@ConditionalOnProperty("second.spring.datasource")
public class SecondDataSource{

    private static final Logger logger = LoggerFactory.getLogger(SecondDataSource.class);

    @Bean(name = "secondaryDataSource")
    @Qualifier("secondaryDataSource")
    @ConfigurationProperties(prefix = "second.spring.datasource")
    @ConditionalOnProperty("second.spring.datasource")
    public DataSource secondaryDataSource() {
        logger.info("第2个数据库连接池创建中......");
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "secondSqlSessionFactory")
    @ConditionalOnProperty("second.spring.datasource")
    public SqlSessionFactory clusterSqlSessionFactory(@Qualifier("secondaryDataSource") DataSource dataSource)
            throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:com/seaboxdata/sqlmap2/*.xml"));
        return sessionFactory.getObject();
    }

    @Bean
    @ConditionalOnProperty("second.spring.datasource")
    public SqlSessionTemplate userSqlSessionTemplate(
            @Qualifier("secondSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactory); // 使用上面配置的Factory
        return template;
    }

}
