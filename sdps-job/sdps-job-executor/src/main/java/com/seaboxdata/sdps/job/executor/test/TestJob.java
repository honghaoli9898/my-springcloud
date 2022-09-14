package com.seaboxdata.sdps.job.executor.test;


import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.job.executor.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.job.executor.service.jobhandler.analysis.AnalysisHdfsMetaDataDispatch;
import com.seaboxdata.sdps.job.executor.service.jobhandler.clean.CleanHdfsDir;
import com.seaboxdata.sdps.job.executor.service.jobhandler.merge.MergeHdfsFileJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@SpringBootTest
@RunWith(SpringRunner.class)
@Component
public class TestJob {

    @Autowired
    AnalysisHdfsMetaDataDispatch analysisHdfsMetaDataDispatch;

    @Autowired
    MergeHdfsFileJob mergeHdfsFileJob;

    @Autowired
    CleanHdfsDir cleanHdfsDir;

    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

    @Test
    public void runjob() {
        analysisHdfsMetaDataDispatch.executeSeaboxAnalysisHdfsMetaData();
    }

    @Test
    public void runMergejob() {
        mergeHdfsFileJob.executeMergeHdfsFile();
    }

    @Test
    public void runCleanJob() {
        cleanHdfsDir.executeCleanHdfsDir();
    }

    @Test
    public void getYarnConf() {
        Result<String> yarnConf = bigdataCommonFegin.getServerConfByConfName(2, "YARN", Arrays.asList(new String[]{"yarn-site"}));
        System.out.println(yarnConf);
    }

}
