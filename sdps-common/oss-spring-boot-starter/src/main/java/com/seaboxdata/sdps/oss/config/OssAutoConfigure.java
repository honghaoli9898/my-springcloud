package com.seaboxdata.sdps.oss.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import com.seaboxdata.sdps.oss.properties.FileServerProperties;
import com.seaboxdata.sdps.oss.template.FdfsTemplate;
import com.seaboxdata.sdps.oss.template.HdfsTemplate;
import com.seaboxdata.sdps.oss.template.S3Template;

/**
 * @author zlt
 * @date 2021/2/13
 *       <p>
 *       Blog: https://zlt2000.gitee.io Github: https://github.com/zlt2000
 */
@EnableConfigurationProperties(FileServerProperties.class)
@Import({ HdfsTemplate.class, FdfsTemplate.class, S3Template.class })
public class OssAutoConfigure {

}
