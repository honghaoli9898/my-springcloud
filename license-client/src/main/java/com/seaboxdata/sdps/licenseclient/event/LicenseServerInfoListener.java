package com.seaboxdata.sdps.licenseclient.event;

import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerDTO;
import com.seaboxdata.sdps.licenseclient.bean.license.LicenseServerVO;
import com.seaboxdata.sdps.licenseclient.dao.LicenseServerMapper;
import com.seaboxdata.sdps.licenseutil.bean.LicenseServerInfo;
import com.seaboxdata.sdps.licenseutil.common.AbstractServerInfos;
import com.seaboxdata.sdps.licenseutil.common.LinuxServerInfos;
import com.seaboxdata.sdps.licenseutil.common.WindowsServerInfos;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 在项目启动采集服务器信息
 */
@Order(Ordered.LOWEST_PRECEDENCE + 1)
@Component
@Slf4j
public class LicenseServerInfoListener implements ApplicationListener<ContextRefreshedEvent>{

    private static final Logger logger = LogManager.getLogger(LicenseServerInfoListener.class);

    @Autowired
    private LicenseServerMapper licenseServerMapper;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //root application context 没有parent
        // ApplicationContext context = event.getApplicationContext().getParent();
        // if(context == null){
        log.info("========采集服务器信息start=======");
        String osName = System.getProperty("os.name").toLowerCase();
        AbstractServerInfos abstractServerInfos = null;

        //根据不同操作系统类型选择不同的数据获取方法
        if (osName.startsWith("windows")) {
            abstractServerInfos = new WindowsServerInfos();
        } else if (osName.startsWith("linux")) {
            abstractServerInfos = new LinuxServerInfos();
        } else {//其他服务器类型
            abstractServerInfos = new LinuxServerInfos();
        }

        LicenseServerInfo licenseServerInfo = abstractServerInfos.getServerInfos();
        String[] ips = new String[licenseServerInfo.getIpAddress().size()];
        licenseServerInfo.getIpAddress().toArray(ips);

        String[] macs = new String[licenseServerInfo.getMacAddress().size()];
        licenseServerInfo.getMacAddress().toArray(macs);

        LicenseServerDTO licenseServerDTO = new LicenseServerDTO();
        licenseServerDTO.setCpuSerial(licenseServerInfo.getCpuSerial());
        licenseServerDTO.setIpAddress(StringUtils.join(ips, ","));
        licenseServerDTO.setMacAddress(StringUtils.join(macs, ","));
        licenseServerDTO.setMainBoardSerial(licenseServerInfo.getMainBoardSerial());

        LicenseServerVO vo = licenseServerMapper.getServerInfoByCpuAndMainBoard(licenseServerDTO);
        if (null == vo) {
            //如果该服务器没采集则保存到数据库
            licenseServerMapper.insert(licenseServerDTO);
        }
        log.info("========采集服务器信息end=======");
    }
}
