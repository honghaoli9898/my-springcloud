package com.seaboxdata.sdps.extendAnalysis.test;

import cn.hutool.core.date.DateUtil;

import com.seaboxdata.sdps.common.framework.bean.SdpsHdfsDbTable;
import com.seaboxdata.sdps.common.framework.bean.task.SdpsTaskInfo;
import com.seaboxdata.sdps.extendAnalysis.mapper.SdpsHdfsDbTableMapper;
import com.seaboxdata.sdps.extendAnalysis.mapper.SdpsTaskInfoMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.io.IOException;

public class ApplicationContextTest {

	public static void main(String[] args) throws IOException {
		System.out.println(DateUtil.offsetHour(DateUtil.date(),1));
		ApplicationContext beanConf = new ClassPathXmlApplicationContext("spring-container.xml");
//		SdpsHdfsDbTableMapper sdpsHdfsDbTableMapper = beanConf.getBean(SdpsHdfsDbTableMapper.class);
//		SdpsHdfsDbTable sdpsHdfsDbTable = sdpsHdfsDbTableMapper.selectById(1);
//		System.out.println(sdpsHdfsDbTable.toString());

		SdpsTaskInfoMapper sdpsTaskInfoMapper = beanConf.getBean(SdpsTaskInfoMapper.class);
		SdpsTaskInfo sdpsTaskInfo = sdpsTaskInfoMapper.selectById(32);
		System.out.println(sdpsTaskInfo);


	}

}