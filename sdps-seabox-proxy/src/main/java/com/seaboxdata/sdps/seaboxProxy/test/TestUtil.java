package com.seaboxdata.sdps.seaboxProxy.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.bean.HdfsFileStats;
import com.seaboxdata.sdps.common.framework.bean.merge.SdpsMergeSubmitInfo;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileMergeBlockSizeRanking;
import com.seaboxdata.sdps.common.framework.bean.merge.SmallFileMergeNumRanking;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;
import com.seaboxdata.sdps.seaboxProxy.bean.CodecType;
import com.seaboxdata.sdps.seaboxProxy.bean.HdfsDirPathInfo;
import com.seaboxdata.sdps.seaboxProxy.bean.SdpsDirPathInfo;
import com.seaboxdata.sdps.seaboxProxy.feign.BigdataCommonFegin;
import com.seaboxdata.sdps.seaboxProxy.image.SeaboxImageTextWriter;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsHdfsFileStatsMapper;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsMergeDataInfoMapper;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsMergeSubmitInfoMapper;
import com.seaboxdata.sdps.seaboxProxy.service.MetaDataExtract;
import com.seaboxdata.sdps.seaboxProxy.util.AmbariUtil;
import com.seaboxdata.sdps.seaboxProxy.util.BigdataVirtualHost;
import com.seaboxdata.sdps.seaboxProxy.util.HdfsUtil;
import com.seaboxdata.sdps.seaboxProxy.util.RangerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.ranger.plugin.model.RangerPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Component
@Slf4j
public class TestUtil {

    @Autowired
    BigdataVirtualHost bigdataVirtualHost;
    @Autowired
    BigdataCommonFegin bigdataCommonFegin;

//    @Value("${metaData.hdfs.fsimageAndExtractTempPath}")
//    String metaDataHdfsFsimageAndExtractTempPath;
//
//    @Value("${metaData.hdfs.tempPath}")
//    String metaDataHdfsTempPath;

    @Autowired
    MetaDataExtract metaDataExtract;

    @Autowired
    SdpsMergeSubmitInfoMapper sdpsMergeSubmitInfoMapper;

    @Autowired
    SdpsMergeDataInfoMapper sdpsMergeDataInfoMapper;

    @Autowired
    SdpsHdfsFileStatsMapper sdpsHdfsFileStatsMapper;

    @Test
    public void testFetchExtractHdfsMetaData() {
        boolean bool = metaDataExtract.fetchAndExtractHdfsMetaData(1);
        System.out.println(bool);
    }

    @Test
    public void insertSdpsMergeSubmitInfoMapperTest() {
        SdpsMergeSubmitInfo sdpsMergeSubmitInfo = SdpsMergeSubmitInfo.builder()
                .clusterId(1)
                .type("HDFS")
                .sourcePath("/testData2")
                .formatType("TEXT")
                .codecType("UNCOMPRESSED")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        int insertID = sdpsMergeSubmitInfoMapper.insert(sdpsMergeSubmitInfo);
        Long id = sdpsMergeSubmitInfo.getId();
        System.out.println(id);
    }

    @Test
    public void selectDirInfo(){
        bigdataVirtualHost.setVirtualHost(1);
        HdfsUtil hdfsUtil = new HdfsUtil(1);
        try {
            SdpsDirPathInfo dirPathInfo = hdfsUtil.getDirPathInfo("/testData2", true);
            System.out.println(dirPathInfo.toString());
            hdfsUtil.closeFs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void selectHdfsDirInfo() throws IOException {
        bigdataVirtualHost.setVirtualHost(1);
        HdfsUtil hdfsUtil = new HdfsUtil(1);
//        String sourcePath = "/testData2";
//        FileSystem fs = hdfsUtil.getFs();
//        RemoteIterator<LocatedFileStatus> fileIterator = fs.listFiles(new Path(sourcePath), true);
//        HdfsDirPathInfo hdfsDirPathInfo = new HdfsDirPathInfo(sourcePath);
//        while(fileIterator.hasNext()) {
//            LocatedFileStatus file = fileIterator.next();
//            hdfsDirPathInfo.addFile(file);
//        }
//        System.out.println(hdfsDirPathInfo.toString());

        HdfsDirPathInfo smallFileInfo = hdfsUtil.getHdfsDirAndSmallFileInfo("/testData2", true);
        System.out.println(smallFileInfo.toString());

        hdfsUtil.closeFs();
    }

    @Test
    public void getSmallFileMergeCount(){
//        Long smallFile = sdpsMergeDataInfoMapper.getCompleteMergeSmallFileSum(1);
//        System.out.println(smallFile);
//        Long mergeSmallFileBlockSum = sdpsMergeDataInfoMapper.getCompleteMergeSmallFileBlockSum(1);
//        System.out.println(mergeSmallFileBlockSum);


//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.DATE_FORMAT_YYYY_MM_DD);
//        String nowDate = simpleDateFormat.format(new Date());
//        QueryWrapper<HdfsFileStats> queryWrapper = new QueryWrapper<HdfsFileStats>()
//                .apply("date_format(update_time, '%Y-%m-%d') = {0}", nowDate)
//                .eq("type_key","SIZE_64M");
//        HdfsFileStats hdfsFileStats = sdpsHdfsFileStatsMapper.selectOne(queryWrapper);
//        System.out.println(hdfsFileStats.toString());

        List<SmallFileMergeNumRanking> mergeFileNumTopN = sdpsMergeDataInfoMapper.getMergeFileNumTopN(1,2,5);
        for (SmallFileMergeNumRanking smallFileMergeNumRanking : mergeFileNumTopN) {
            System.out.println(smallFileMergeNumRanking.toString());
        }
        System.out.println("===========================================");
        List<SmallFileMergeBlockSizeRanking> smallFileMergeBlockSizeRankingList = sdpsMergeDataInfoMapper.getMergeFileBlockNumTopN(1,2,5);
        for (SmallFileMergeBlockSizeRanking smallFileMergeBlockSizeRanking : smallFileMergeBlockSizeRankingList) {
            System.out.println(smallFileMergeBlockSizeRanking.toString());
        }
    }

    @Autowired
    private RangerUtil rangerUtil;

    @Test
    public void testRanger(){
        AmbariUtil ambariUtil = new AmbariUtil(1);
        String clusterName = ambariUtil.getClusterName();
        rangerUtil.init(1);
        RangerPolicy rangerPolicy = new RangerPolicy();
        rangerPolicy.setService(clusterName.concat("_hadoop"));
        rangerPolicy.setName("testPolicy0011");
        rangerPolicy.setIsEnabled(Boolean.TRUE);
        rangerPolicy.setIsAuditEnabled(Boolean.TRUE);
            Map<String, RangerPolicy.RangerPolicyResource> resources = new HashMap<>();
                RangerPolicy.RangerPolicyResource rangerPolicyResource = new RangerPolicy.RangerPolicyResource();
                rangerPolicyResource.setIsRecursive(Boolean.TRUE);
                List<String> values = Arrays.asList("/test0011", "test0022");
                rangerPolicyResource.setValues(values);
            resources.put("path",rangerPolicyResource);
        rangerPolicy.setResources(resources);
        List<RangerPolicy.RangerPolicyItem> rangerPolicyItems = new ArrayList<>();
            RangerPolicy.RangerPolicyItem rangerPolicyItem = new RangerPolicy.RangerPolicyItem();
            rangerPolicyItem.setGroups(Arrays.asList("test"));
                List<RangerPolicy.RangerPolicyItemAccess> accesses = new ArrayList<>();
                accesses.add(new RangerPolicy.RangerPolicyItemAccess("read",Boolean.TRUE));
                accesses.add(new RangerPolicy.RangerPolicyItemAccess("write",Boolean.TRUE));
                accesses.add(new RangerPolicy.RangerPolicyItemAccess("execute",Boolean.TRUE));
            rangerPolicyItem.setAccesses(accesses);
            rangerPolicyItems.add(rangerPolicyItem);
        rangerPolicy.setPolicyItems(rangerPolicyItems);
        Boolean bool = rangerUtil.addRangerPolicy(rangerPolicy);
        System.out.println(bool.toString());
    }

    @Test
    public void testReadSeqCodec(){

        bigdataVirtualHost.setVirtualHost(2);

        String path = "hdfs://MyHdfsHA/testData/mergeFile-demo";
        HdfsUtil hdfsUtil = new HdfsUtil(2,"hdfs");
        FileSystem fs = hdfsUtil.getFs();
//        try {
//            FileStatus[] fileStatuses = fs.listStatus(new Path(path));
//            for (FileStatus fileStatus : fileStatuses) {
//                System.out.println(fileStatus.getPath().toString());
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        String seqPath = "hdfs://MyHdfsHA/testData/mergeFile-demo/SEQ/part.seq";
//        String seqPath = "hdfs://MyHdfsHA/testData/SEQ/000000_0";
//
//        SequenceFile.Reader reader = null;
//        try {
//            reader = new SequenceFile.Reader(
//                    fs.getConf(),
//                    new SequenceFile.Reader.Option[]{SequenceFile.Reader.file(new Path(seqPath))}
//            );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        CompressionCodec compressionCodec = reader.getCompressionCodec();
//        System.out.println(compressionCodec.getClass().getName());


//        String seqPath = "/testData/SEQ/test.seq";
//        String seqPath = "/testData/SEQ/000000_0";
        String seqPath = "/testData/SEQ/part.seq";
        try {
//            SequenceFile.Writer writer = SequenceFile.createWriter(fs.getConf(), SequenceFile.Writer.file(new Path(seqPath)),
//                    SequenceFile.Writer.keyClass(Text.class), SequenceFile.Writer.valueClass(Text.class),
//                    SequenceFile.Writer.compression(SequenceFile.CompressionType.NONE));
//            writer.append(new Text("111"),new Text("aaaaaa"));
//            IOUtils.closeStream(writer);

            SequenceFile.Reader reader = new SequenceFile.Reader(
                fs.getConf(),
                new SequenceFile.Reader.Option[]{SequenceFile.Reader.file(new Path(seqPath))});
            System.out.println("压缩类型:");
            System.out.println(reader.getCompressionType().name());

            CompressionCodec compressionCodec = reader.getCompressionCodec();
//            System.out.println("压缩编码:");
//            System.out.println(compressionCodec.getCompressorType().getName());


            if(Objects.isNull(compressionCodec)){
                CodecType typeByCodec = CodecType.getTypeByCodec("", CodecType.UNCOMPRESSED);
                System.out.println(typeByCodec.getName());
            }else {
                CodecType.getTypeByCodec(compressionCodec.getClass().getName(), CodecType.UNCOMPRESSED);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

//                        CodecType codecType = CodecType.getTypeByCodec(compressionCodec.getClass().getName(), CodecType.UNCOMPRESSED);
//                        System.out.println(codecType.toString());

//        try {
//            UserGroupInformation userGroupInformation = hdfsUtil.getUserGroupInformation();
//            userGroupInformation.doAs(new PrivilegedAction<Object>() {
//
//                @Override
//                public Object run() {
//                    try {
//                        SequenceFile.Reader reader = new SequenceFile.Reader(
//                                fs.getConf(),
//                                new SequenceFile.Reader.Option[]{SequenceFile.Reader.file(new Path(seqPath))}
//                        );
//                        CompressionCodec compressionCodec = reader.getCompressionCodec();
//                        System.out.println(compressionCodec.getClass().getName());
////                        CodecType codecType = CodecType.getTypeByCodec(compressionCodec.getClass().getName(), CodecType.UNCOMPRESSED);
////                        System.out.println(codecType.toString());
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }
//            });
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        try {
//            SequenceFile.Reader reader = new SequenceFile.Reader(fs.getConf(),
//                    new SequenceFile.Reader.Option[]{SequenceFile.Reader.file(new Path(path))});
//            CompressionCodec compressionCodec = reader.getCompressionCodec();
//            String name = compressionCodec.getClass().getName();
//            System.out.println(name);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }


}
