package com.seaboxdata.sdps.seaboxProxy.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.sf.jmimemagic.Magic;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.Trash;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.tools.DFSAdmin;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionInputStream;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.orc.CompressionKind;
import org.apache.orc.OrcFile;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.springframework.beans.BeanUtils;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.exception.BusinessException;
import com.seaboxdata.sdps.common.core.properties.KerberosProperties;
import com.seaboxdata.sdps.common.core.utils.SpringUtil;
import com.seaboxdata.sdps.common.framework.bean.HdfsDirObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFSObj;
import com.seaboxdata.sdps.common.framework.bean.HdfsFileType;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.framework.bean.SdpsContentSummary;
import com.seaboxdata.sdps.common.framework.bean.SdpsFileStatus;
import com.seaboxdata.sdps.common.utils.excelutil.DateUtil;
import com.seaboxdata.sdps.seaboxProxy.bean.CodecType;
import com.seaboxdata.sdps.seaboxProxy.bean.FormatType;
import com.seaboxdata.sdps.seaboxProxy.bean.HdfsDirPathInfo;
import com.seaboxdata.sdps.seaboxProxy.bean.SdpsDirPathInfo;
import com.seaboxdata.sdps.seaboxProxy.image.SeaboxImageTextWriter;
import com.seaboxdata.sdps.seaboxProxy.mapper.SdpsClusterMapper;

@Slf4j
public class HdfsUtil {
	public KerberosProperties kerberosProperties = SpringUtil
			.getBean(KerberosProperties.class);
	public static final Long DEFAULT_MERGE_THRESHOLD = 67108864L;
	private FileSystem fs;
	private HdfsAdmin hdfsAdmin;
	private DFSAdmin dfsAdmin;
	private String userName = "hdfs";
	private UserGroupInformation userGroupInformation;
	public BigdataVirtualHost bigdataVirtualHost = SpringUtil
			.getBean(BigdataVirtualHost.class);
	private Boolean isEnableKerberos = false;
	/**
	 * ??????
	 * 
	 * @param clusterId
	 */
	public HdfsUtil(Integer clusterId) {
		try {
			init(clusterId);
		} catch (Exception e) {
			log.error("HdfsUtil???????????????:", e);
		}
	}

	/**
	 * ??????HdfsUtil???????????????????????????
	 * 
	 * @param clusterId
	 * @param userName
	 */
	public HdfsUtil(Integer clusterId, String userName) {
		if(CommonConstant.ADMIN_USER_NAME.equals(userName)){
			userName = this.userName;
		}
		this.userName = userName;
		try {
			init(clusterId);
		} catch (Exception e) {
			log.error("HdfsUtil???????????????:", e);
		}
	}

	/**
	 * ?????????HdfsUtil
	 * 
	 * @param clusterId
	 * @throws Exception
	 */
	private void init(Integer clusterId) throws Exception {
		bigdataVirtualHost.setVirtualHost(clusterId);
		System.setProperty("HADOOP_USER_NAME",
				userName);
		SdpsClusterMapper sdpsClusterMapper = SpringUtil
				.getBean(SdpsClusterMapper.class);
		Configuration hdfsConf = new Configuration();
		// ??????????????????
		ArrayList<String> confList = new ArrayList<>();
		confList.add("core-site");
		confList.add("hdfs-site");
		String confJson = new AmbariUtil(clusterId)
				.getAmbariServerConfByConfName("HDFS", confList);
		Map confMap = JSON.parseObject(confJson, Map.class);
		for (Object obj : confMap.entrySet()) {
			hdfsConf.set(String.valueOf(((Map.Entry) obj).getKey()),
					String.valueOf(((Map.Entry) obj).getValue()));
		}
		hdfsConf.setBoolean(
				CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
				true);
		SdpsCluster sdpsCluster = sdpsClusterMapper.selectById(clusterId);
		if (kerberosProperties.getEnable() && sdpsCluster.getKerberos()) {
			isEnableKerberos = true;
			System.setProperty("java.security.krb5.conf",
					kerberosProperties.getKrb5());
			System.setProperty("javax.security.auth.useSubjectCredsOnly",
					"false");
			hdfsConf.set("hadoop.security.authentication", "kerberos");
			//??????hbase???????????????host
			AmbariUtil ambariUtil = new AmbariUtil(clusterId);
			JSONObject componentAndHost = ambariUtil.getComponentAndHost("HDFS");
			String hdfsHost = AmbariUtil.getHostByComponentByService("HDFS_CLIENT", componentAndHost);
			UserGroupInformation.setConfiguration(hdfsConf);
			try {
				String kerberosUsername = null;
				if (StrUtil.equalsIgnoreCase(this.userName, "hdfs")) {
					kerberosUsername = this.userName.concat("-").concat(
							sdpsCluster.getClusterName());
				}else{
					kerberosUsername = this.userName;
				}
				UserGroupInformation userGroupInformation = UserGroupInformation
						.loginUserFromKeytabAndReturnUGI(
								kerberosUsername.concat(kerberosProperties
										.getUserSuffix()),
								kerberosProperties.getSeaboxKeytabPath().concat("/")
										.concat(clusterId.toString())
										.concat(".").concat(this.userName)
										.concat(".headless.keytab"));
				this.userGroupInformation = userGroupInformation;
			} catch (Exception e) {
				log.error("??????????????????:{} ", e);
				throw new BusinessException("??????????????????:".concat(e.getMessage()));
			}
			this.userGroupInformation.doAs(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					try {
						fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf),
								hdfsConf,userName);
						hdfsAdmin = new HdfsAdmin(FileSystem
								.getDefaultUri(hdfsConf), hdfsConf);
						dfsAdmin = new DFSAdmin(hdfsConf);
					} catch (Exception e) {
						log.error("??????fileSystem??????:{}", e);
					}
					return null;
				}
			});
		} else {
			fs = FileSystem.get(FileSystem.getDefaultUri(hdfsConf), hdfsConf,
					userName);
			hdfsAdmin = new HdfsAdmin(FileSystem.getDefaultUri(hdfsConf),
					hdfsConf);
			dfsAdmin = new DFSAdmin(hdfsConf);
		}
	}

//	public static Boolean checkKeytab(Integer clusterId, String userName, String keytabName) {
//		Boolean result = false;
//		SdpsClusterMapper sdpsClusterMapper = SpringUtil
//				.getBean(SdpsClusterMapper.class);
//		KerberosProperties kp = SpringUtil
//				.getBean(KerberosProperties.class);
//		SdpsCluster sdpsCluster = sdpsClusterMapper.selectById(clusterId);
//		String kerberosUsername = userName.concat("-").concat(
//				sdpsCluster.getClusterName()).concat(kp
//				.getUserSuffix());
//		Configuration hdfsConf = new Configuration();
//		// ??????????????????
//		ArrayList<String> confList = new ArrayList<>();
//		confList.add("core-site");
//		confList.add("hdfs-site");
//		String confJson = new AmbariUtil(clusterId)
//				.getAmbariServerConfByConfName("HDFS", confList);
//		Map confMap = JSON.parseObject(confJson, Map.class);
//		for (Object obj : confMap.entrySet()) {
//			hdfsConf.set(String.valueOf(((Map.Entry) obj).getKey()),
//					String.valueOf(((Map.Entry) obj).getValue()));
//		}
//		hdfsConf.setBoolean(
//				CommonConfigurationKeys.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH_ALLOWED_KEY,
//				true);
//
//		System.setProperty("java.security.krb5.conf", kp.getKrb5());
//		System.setProperty("javax.security.auth.useSubjectCredsOnly",
//				"false");
//		hdfsConf.set("hadoop.security.authorization", "true");
//		hdfsConf.set("dfs.namenode.kerberos.principal", kerberosUsername);
//		hdfsConf.set("dfs.namenode.kerberos.principal.pattern", "*");
//		hdfsConf.set("hadoop.security.authentication", "kerberos");
//
//		try {
//			UserGroupInformation.setConfiguration(hdfsConf);
//			UserGroupInformation.loginUserFromKeytab(kerberosUsername, kp.getKeytabPath() + "/" + keytabName);
//		} catch (IOException e) {
//			log.error("kerberos????????????", e);
//			String message = e.getMessage();
////			if (message.endsWith("javax.security.auth.login.LoginException: Unable to obtain password")) {
////				result = true;
////			}
//			result = true;
//		}
//		return result;
//	}

	/**
	 * ??????FS??????
	 * 
	 * @return
	 */
	public FileSystem getFs() {
		return fs;
	}

	/**
	 * ??????hdfsAdmin??????
	 * 
	 * @return
	 */
	public HdfsAdmin getHdfsAdmin() {
		return hdfsAdmin;
	}

	/**
	 * ??????dfsAdmin??????
	 * 
	 * @return
	 */
	public DFSAdmin getDfsAdmin() {
		return dfsAdmin;
	}

	/**
	 * ????????????
	 * 
	 * @return
	 */
	public String getUserName() {
		return userName;
	}

	public void closeFs() {
		try {
			fs.close();
			dfsAdmin.close();
			logoutUser();
		} catch (IOException e) {
			log.error("Hdfs FileSystem????????????:", e);
		}
	}

	/**
	 * ???????????????
	 *
	 * @param createHdfsPath
	 *            ??????
	 * @return ?????????????????????
	 */
	public boolean mkdir(String createHdfsPath) {
		if (StrUtil.isBlankIfStr(createHdfsPath)) {
			return false;
		}
		Path path = new Path(createHdfsPath);
		try {
			if (fs.exists(path)) {
				log.info("??????????????????????????????.{}", createHdfsPath);
			}
		} catch (IOException e) {
			log.error("??????????????????.", e);
		}
		boolean bool = false;
		try {
			bool = fs.mkdirs(path);
		} catch (Exception e) {
			log.error("Hdfs ??????????????????:", e);
		}
		return bool;
	}

	/**
	 * ?????? ??????????????????????????????????????????
	 *
	 * @param hdfsPath
	 *            hdfs??????
	 * @param quotaNum
	 *            ???????????????(?????????????????????1,??????????????????1)
	 * @param spaceQuotaNum
	 *            ????????????????????????(??????:B)
	 * @param owner
	 *            ?????????
	 * @param ownergroup
	 *            ?????????
	 * @return
	 */
	public boolean setHdfsQNAndSQNAndOwner(String hdfsPath, long quotaNum,
			long spaceQuotaNum, String owner, String ownergroup) {
		boolean bool = false;
		try {
			boolean exists = fs.exists(new Path(hdfsPath));
			if (exists) {
				boolean isDirectory = fs.getFileStatus(new Path(hdfsPath))
						.isDirectory();
				if (!StrUtil.isBlankIfStr(quotaNum) && isDirectory) {
					hdfsAdmin.setQuota(new Path(hdfsPath), quotaNum);
				}
				if (!StrUtil.isBlankIfStr(spaceQuotaNum) && isDirectory) {
					hdfsAdmin.setSpaceQuota(new Path(hdfsPath), spaceQuotaNum);
				}
				if (!StrUtil.isBlankIfStr(owner)) {
					fs.setOwner(new Path(hdfsPath), owner, null);
				}
				if (!StrUtil.isBlankIfStr(ownergroup)) {
					fs.setOwner(new Path(hdfsPath), null, ownergroup);
				}
				bool = true;
			} else {
				log.error("Hdfs ??????????????????????????????????????????????????????????????????????????????:hdfs??????[" + hdfsPath
						+ "]?????????");
				throw new BusinessException(
						"Hdfs ??????????????????????????????????????????????????????????????????????????????:hdfs??????[" + hdfsPath
								+ "]?????????");
			}
		} catch (Exception e) {
			log.error("Hdfs ??????????????????????????????????????????????????????????????????????????????:", e);
			throw new BusinessException(e.getMessage());
		}
		return bool;
	}

	/**
	 * Hdfs ????????????????????? ????????????????????????????????????????????????????????????
	 *
	 * @param createHdfsPath
	 *            ????????????
	 * @param quotaNum
	 *            ???????????????(?????????????????????1,??????????????????1)
	 * @param spaceQuotaNum
	 *            ????????????????????????(??????:B)
	 * @param owner
	 *            ?????????
	 * @param ownergroup
	 *            ?????????
	 * @return
	 */
	public boolean mkdirSetQNAndSQNAndOwner(String createHdfsPath,
			long quotaNum, long spaceQuotaNum, String owner, String ownergroup) {
		boolean bool = false;
		try {
			bool = fs.mkdirs(new Path(createHdfsPath));
			if (bool) {
				if (!StrUtil.isBlankIfStr(quotaNum)) {
					hdfsAdmin.setQuota(new Path(createHdfsPath), quotaNum);
				}
				if (!StrUtil.isBlankIfStr(spaceQuotaNum)) {
					hdfsAdmin.setSpaceQuota(new Path(createHdfsPath),
							spaceQuotaNum);
				}
				if (!StrUtil.isBlankIfStr(owner)) {
					fs.setOwner(new Path(createHdfsPath), owner, null);
				}
				if (!StrUtil.isBlankIfStr(ownergroup)) {
					fs.setOwner(new Path(createHdfsPath), null, ownergroup);
				}
			}
		} catch (Exception e) {
			bool = false;
			try {
				fs.delete(new Path(createHdfsPath), true);
			} catch (IOException ioException) {
				log.error("??????Hdfs??????[" + createHdfsPath + "]??????", ioException);
			}
			log.error("Hdfs ????????????????????? ??????????????????????????????????????????????????????????????????", e);
		}
		return bool;
	}

	/**
	 * Hdfs ?????????????????????????????????
	 *
	 * @param createHdfsPath
	 *            ????????????
	 * @param owner
	 *            ?????????
	 * @param ownergroup
	 *            ?????????
	 * @return
	 */
	public boolean mkdirAndOwner(String createHdfsPath, String owner,
			String ownergroup) {
		boolean bool = false;
		try {
			bool = fs.mkdirs(new Path(createHdfsPath));
			if (bool) {
				if (!StrUtil.isBlankIfStr(owner)) {
					fs.setOwner(new Path(createHdfsPath), owner, null);
				}
				if (!StrUtil.isBlankIfStr(ownergroup)) {
					fs.setOwner(new Path(createHdfsPath), null, ownergroup);
				}
			}
		} catch (Exception e) {
			bool = false;
			try {
				fs.delete(new Path(createHdfsPath), true);
			} catch (IOException ioException) {
				log.error("??????Hdfs??????[" + createHdfsPath + "]??????", ioException);
			}
			log.error("Hdfs ?????????????????????????????????????????????", e);
		}
		return bool;
	}

	/**
	 * ????????????
	 *
	 * @param hdfsPath
	 * @param permissionCode
	 *            ?????????(???:775)
	 * @return
	 */
	public boolean setPermission(String hdfsPath, String permissionCode) {
		boolean bool = false;
		try {
			fs.setPermission(new Path(hdfsPath),
					new FsPermission(Short.parseShort(permissionCode, 8)));
			bool = true;
		} catch (Exception e) {
			log.error("Hdfs ??????????????????:", e);
		}
		return bool;
	}

	/**
	 * ??????hdfs??????
	 * 
	 * @throws IOException
	 */
	public boolean deleteFile(List<String> hdfsPaths) throws IOException {
		boolean bool = false;
		if (StrUtil.isBlankIfStr(hdfsPaths)) {
			log.error("hdfsPath??????.");
			return bool;
		}
		for (String hdfsPath : hdfsPaths) {
			Path path = new Path(hdfsPath);
			if (fs.exists(path)) {
				bool = fs.delete(path, true);
			} else {
				log.error("hdfs???????????????{}", path);
			}
		}
		return bool;
	}

	/**
	 * ??????hdfs??????????????????
	 */
	public boolean moveToTrash(String hdfsPath) throws Exception {
		Boolean flag = false;

		// ??????trash
		Trash trash = new Trash(fs, fs.getConf());
		// ???????????????path???hdfs?????????????????????????????????
		// moveToTrash????????????boolean??????????????????true?????????
		flag = trash.moveToTrash(new Path(hdfsPath));

		return flag;
	}

	/**
	 * ??????hdfs??????
	 */
	public boolean cleanDir(ArrayList<String> hdfsPathList) {
		boolean bool = false;
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		try {
			for (String hdfsPath : hdfsPathList) {
				int length = fs.listStatus(new Path(hdfsPath)).length;
				if (length > 0) {
					FileStatus fileStatus = fs
							.getFileStatus(new Path(hdfsPath));
					String owner = fileStatus.getOwner();
					String group = fileStatus.getGroup();
					ContentSummary contentSummary = fs
							.getContentSummary(fileStatus.getPath());
					long quota = contentSummary.getQuota();
					long spaceQuota = contentSummary.getSpaceQuota();

					fs.delete(new Path(hdfsPath), true);

					mkdirSetQNAndSQNAndOwner(hdfsPath, quota, spaceQuota,
							owner, group);
					setPermission(hdfsPath, "775");
					log.info("??????hdfs??????:" + hdfsPath);
				}
			}
			bool = true;
		} catch (Exception e) {
			log.error("??????hdfs????????????:", e);
		}
		return bool;
	}

	/**
	 * ??????hdfs????????????(??????????????????????????????????????????????????????(??????)???????????????????????????(??????)?????????????????????????????????)
	 *
	 * @param hdfsPath
	 * @return
	 */
	public ArrayList<HdfsDirObj> getDirOwnerGroupUsedQuotaNumSpaceQuotaNum(
			String hdfsPath) {
		ArrayList<HdfsDirObj> hdfsDirObjsList = new ArrayList<>();
		try {
			boolean exists = fs.exists(new Path(hdfsPath));
			if (!exists) {
				return null;
			} else {
				FileStatus[] fileStatuses = fs.listStatus(new Path(hdfsPath));
				for (FileStatus fileStatus : fileStatuses) {
					if (fileStatus.isDirectory()) {
						String dirName = fileStatus.getPath().getName();
						String owner = fileStatus.getOwner();
						String group = fileStatus.getGroup();
						FsPermission permission = fileStatus.getPermission();
						long modificationTime = fileStatus
								.getModificationTime();
						ContentSummary summary = fs
								.getContentSummary(fileStatus.getPath());
						long spaceConsumed = summary.getSpaceConsumed();
						long spaceQuota = summary.getSpaceQuota();
						long quota = summary.getQuota();

						HdfsDirObj hdfsDirObj = HdfsDirObj
								.builder()
								.dirName(dirName)
								.owner(owner)
								.group(group)
								.quotaNum(quota)
								.spaceConsumed(spaceConsumed)
								.spaceQuotaNum(spaceQuota)
								.modificationTime(
										DateUtil.timestampToDate(
												modificationTime,
												DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS))
								.fsPermission(permission.toString()).build();
						hdfsDirObjsList.add(hdfsDirObj);
					}
				}
			}
		} catch (Exception e) {
			log.error("??????hdfs??????[" + hdfsPath + "]????????????:", e);
		}
		return hdfsDirObjsList;
	}

	/**
	 * ??????hdfs??????(????????????????????????(??????)??????????????????)
	 *
	 * @param hdfsPath
	 * @return
	 */
	public HdfsDirObj getDirQNAndSQN(String hdfsPath) {
		HdfsDirObj hdfsDirObj = new HdfsDirObj();
		try {
			ContentSummary summary = fs.getContentSummary(new Path(hdfsPath));
			long quota = summary.getQuota();
			long spaceQuota = summary.getSpaceQuota();
			long spaceConsumed = summary.getSpaceConsumed();
			hdfsDirObj = HdfsDirObj.builder().dirName(hdfsPath).quotaNum(quota)
					.spaceQuotaNum(spaceQuota).spaceConsumed(spaceConsumed)
					.build();
			System.out.println(hdfsDirObj.toString());
		} catch (Exception e) {
			log.error("??????hdfs??????[" + hdfsPath + "]????????????:", e);
		}
		return hdfsDirObj;
	}

	/**
	 * ??????HDFS????????????
	 *
	 * @param hdfsPath
	 *            hdfs??????
	 * @return ??????????????????
	 */
	public ArrayList<HdfsFSObj> selectHdfsSaveObjList(String hdfsPath) {
		ArrayList<HdfsFSObj> hdfsFSObjs = new ArrayList<>();
		try {
			if (!fs.exists(new Path(hdfsPath))) {
				throw new BusinessException("?????????????????????");
			}
			FileStatus[] fileStatuses = fs.listStatus(new Path(hdfsPath));
			for (FileStatus fileStatus : fileStatuses) {
				if (fileStatus.isDirectory() || fileStatus.isFile()) {
					String fileName = fileStatus.getPath().getName();
					String owner = fileStatus.getOwner();
					String group = fileStatus.getGroup();
					FsPermission permission = fileStatus.getPermission();
					long modificationTime = fileStatus.getModificationTime();
					short replication = fileStatus.getReplication();
					long blockSize = fileStatus.getBlockSize();
					long fileLength = fileStatus.getLen();
					HdfsFileType type = fileStatus.isDirectory() ? HdfsFileType.DIRECTORY
							: HdfsFileType.FILE;
					HdfsFSObj hdfsFSObj = new HdfsFSObj();
					hdfsFSObj.setFileName(fileName);
					hdfsFSObj.setOwner(owner);
					hdfsFSObj.setGroup(group);
					hdfsFSObj.setBlockSize(blockSize);
					hdfsFSObj.setFsPermission(permission.toString());
					hdfsFSObj.setLength(fileLength);
					hdfsFSObj.setModificationTime(DateUtil.timestampToDate(
							modificationTime,
							DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS));
					hdfsFSObj.setReplication(replication);
					hdfsFSObj.setType(type);
					hdfsFSObjs.add(hdfsFSObj);
				}
			}
		} catch (Exception e) {
			log.error("??????hdfs??????[" + hdfsPath + "]????????????:", e);
			throw new BusinessException(e.getMessage());
		}
		return hdfsFSObjs;
	}

	/**
	 * ??????????????????
	 *
	 * @param path
	 *            ????????????????????????
	 * @param permission
	 *            ?????????
	 * @return ????????????
	 */
	public boolean permission(String path, String permission) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		boolean flag = false;
		if (StrUtil.isBlankIfStr(path)) {
			log.error("????????????:{}", path);
			return flag;
		}
		Path file = new Path(path);
		try {
			if (fs.exists(file)) {
				fs.setPermission(file,
						new FsPermission(Integer.parseInt(permission, 8)));
				flag = true;
			} else {
				log.error("???????????????,??????????????????.");
				flag = false;
			}
		} catch (IOException e) {
			log.error("??????????????????.{}", e);
		}
		return flag;
	}

	/**
	 * ??????HDFS???????????????
	 *
	 * @return
	 */
	public boolean fetchHdfsImage(File saveFile) {

		final boolean[] flag = {false};
		try {
			log.info("??????????????????:");
			log.info("ShortUserName:"+this.userGroupInformation.getShortUserName());
			log.info("UserName:"+this.userGroupInformation.getUserName());
			if(isEnableKerberos){
				this.userGroupInformation.doAs(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						try {
							// ????????????????????????1:String??????,??????2????????????1??????????????????????????????0?????????
							int fetchNum = dfsAdmin.fetchImage(
									new String[] { saveFile.getAbsolutePath() }, 0);
							if (fetchNum == 0 && saveFile.exists()) {
								flag[0] = true;
							}
						} catch (Exception e) {
							log.error("??????kerberos?????????,??????HDFS???????????????:{}", e);
						}
						return null;
					}
				});
			}else {
				// ????????????????????????1:String??????,??????2????????????1??????????????????????????????0?????????
				int fetchNum = dfsAdmin.fetchImage(
						new String[] { saveFile.getAbsolutePath() }, 0);
				if (fetchNum == 0 && saveFile.exists()) {
					flag[0] = true;
				}
			}


		} catch (Exception e) {
			log.error("??????HDFS???????????????:", e);
		}
		return flag[0];
	}

	/**
	 * ??????HDFS?????????
	 *
	 * @return
	 */
	public Boolean extractHdfsMetaData(String fsimageFile,
			String extractOutFile, String delimiter, String tempPath) {
		boolean flag = false;
		PrintStream out = null;
		RandomAccessFile accessFile = null;
		SeaboxImageTextWriter writer = null;
		try {
			out = new PrintStream(extractOutFile, "UTF-8");
			writer = new SeaboxImageTextWriter(out, delimiter, tempPath,
					Boolean.FALSE);
			accessFile = new RandomAccessFile(fsimageFile, "r");
			writer.visit(accessFile);

			boolean exists = new File(extractOutFile).exists();
			if (exists) {
				flag = true;
			}
		} catch (Exception e) {
			flag = false;
			log.error("??????HDFS???????????????:", e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (accessFile != null) {
					accessFile.close();
				}
			} catch (IOException e) {
				log.error("???????????????", e);
			}
		}
		return true;
	}

	/**
	 * ???????????????
	 *
	 * @param oldPath
	 *            ???????????????
	 * @param oldPath
	 *            ???????????????
	 * @return ?????????????????????
	 */
	public boolean rename(String oldPath, String newPath) {
		boolean flag = false;
		if (StrUtil.isBlankIfStr(oldPath) || StrUtil.isBlankIfStr(newPath)) {
			log.error("?????????????????????:{}????????????????????????:{}", oldPath, newPath);
			return false;
		}
		try {
			if (fs.exists(new Path(newPath))) {
				throw new BusinessException("??????????????????");
			}
			flag = fs.rename(new Path(oldPath), new Path(newPath));
		} catch (Exception e) {
			log.error("???????????????????????????.{}", e);
			closeFs();
			throw new BusinessException(e.getMessage());
		}
		return flag;
	}

	/**
	 * ??????????????????hdfs?????????
	 * 
	 * @param dir
	 *            hdfs??????
	 * @return
	 */
	public HdfsFSObj getStorageSize(String dir) {
		HdfsFSObj fsObj = new HdfsFSObj();
		try {
			Path path = new Path(dir);
			// ???????????????????????????
			ContentSummary contentSummary = fs.getContentSummary(path);
			long spaceConsumed = contentSummary.getSpaceConsumed();
			fsObj.setUsed(spaceConsumed);
			// ????????????????????????
			long total = 0L;
			DistributedFileSystem dfs = (DistributedFileSystem) fs;
			DatanodeInfo[] dataNodeStats = dfs.getDataNodeStats();
			for (DatanodeInfo datanodeInfo : dataNodeStats) {
				total += datanodeInfo.getCapacity();
			}
			fsObj.setTotalCapacity(total);
		} catch (Exception e) {
			log.error("????????????????????????:{}", e);
		}
		return fsObj;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param dir
	 * @return
	 */
	public SdpsContentSummary getContentSummary(String dir) {
		SdpsContentSummary sdpsContentSummary = new SdpsContentSummary();
		try {
			Path path = new Path(dir);
			ContentSummary contentSummary = fs.getContentSummary(path);

			BeanUtils.copyProperties(contentSummary, sdpsContentSummary);
		} catch (Exception e) {
			log.error("??????hdfs??????????????????", e);
			throw new BusinessException("??????hdfs??????????????????");
		}
		return sdpsContentSummary;
	}

	/**
	 * ?????????????????????????????????
	 * 
	 * @param paths
	 *            ????????????
	 * @return
	 */
	public SdpsContentSummary getContentSummaryByPaths(List<String> paths) {
		SdpsContentSummary sdpsContentSummary = new SdpsContentSummary();
		paths.forEach(path -> {
			SdpsContentSummary contentSummary = getContentSummary(path);
			sdpsContentSummary.sum(contentSummary);
		});
		return sdpsContentSummary;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param dir
	 * @return
	 */
	public List<SdpsFileStatus> getChildrenFileStatus(String dir) {
		try {
			Path path = new Path(dir);
			FileStatus[] fileStatuses = fs.listStatus(path);
			List<SdpsFileStatus> fileStatusList = Arrays
					.stream(fileStatuses)
					.map(x -> {
						SdpsFileStatus sdpsFileStatus = new SdpsFileStatus();
						sdpsFileStatus.setFileName(x.getPath().getName());
						String hdfsPathLocation = getHdfsPathLocation(x
								.getPath().toString());
						sdpsFileStatus.setPath(hdfsPathLocation);
						SdpsContentSummary contentSummary = getContentSummary(hdfsPathLocation);
						sdpsFileStatus.setTotalFileNum(contentSummary
								.getFileCount());
						sdpsFileStatus.setTotalFileSize(contentSummary
								.getSpaceConsumed());
						sdpsFileStatus.setDsQuota(contentSummary
								.getSpaceQuota());
						sdpsFileStatus.setNsQuota(contentSummary.getQuota());
						return sdpsFileStatus;
					}).collect(Collectors.toList());
			return fileStatusList;
		} catch (Exception e) {
			log.error("??????hdfs???????????????????????????", e);
			throw new BusinessException("??????hdfs???????????????????????????");
		}
	}

	/**
	 * ??????hive???????????????
	 * 
	 * @param hdfsPath
	 *            hive??????????????????
	 * @return
	 */
	public static String getHdfsPathLocation(String hdfsPath) {
		String substring = hdfsPath.substring(hdfsPath.indexOf("//") + 2);
		return substring.substring(substring.indexOf("/"));
	}

	/**
	 * ??????????????????
	 * 
	 * @param filePath
	 * @return
	 */
	public SdpsFileStatus getDirSummary(String filePath) {
		SdpsFileStatus status = new SdpsFileStatus();
		status.setType("DIRECTORY");
		status.setPath(filePath);
		status.setFileName(StringUtils.substringAfterLast(filePath, "/"));
		setStatusSummary(status);
		return status;
	}

	/**
	 * ???????????????????????????????????????????????????(??????)????????????????????????????????????
	 * 
	 * @param status
	 */
	private void setStatusSummary(SdpsFileStatus status) {
		String path = status.getPath();
		SdpsContentSummary summary = getContentSummary(path);
		status.setLength(summary.getLength());
		status.setTotalFileSize(summary.getLength());
		status.setTotalFileNum(summary.getFileCount());
		status.setNsQuota(summary.getQuota());
		status.setDsQuota(summary.getSpaceQuota());
	}

	/**
	 * ????????????????????????,?????????
	 * 
	 * @param sourcePath
	 * @param recursive
	 * @return
	 * @throws Exception
	 */
	public SdpsDirPathInfo getDirPathInfo(String sourcePath, boolean recursive)
			throws Exception {
		RemoteIterator<LocatedFileStatus> fileIterator = fs.listFiles(new Path(
				sourcePath), recursive);
		SdpsDirPathInfo dirPathInfo = new SdpsDirPathInfo(sourcePath);
		long modifyTime = 0L;
		// ?????????????????????????????????
		while (fileIterator.hasNext()) {
			LocatedFileStatus file = fileIterator.next();
			if (file.getLen() < DEFAULT_MERGE_THRESHOLD) {
				dirPathInfo.addFile(file);
			}

			if (file.getModificationTime() > modifyTime) {
				modifyTime = file.getModificationTime();
			}
		}

		dirPathInfo.setLastModifyTime(modifyTime);
		// ??????????????????????????????????????????
		if (CollectionUtils.isEmpty(dirPathInfo.getFilePaths())) {
			return dirPathInfo;
		} else {
			// ????????????????????????????????????????????????????????????
			FormatType detectFormat = detectFileType(dirPathInfo);
			Preconditions.checkArgument(detectFormat != null, "detect="
					+ detectFormat);
			dirPathInfo.setFormatType(detectFormat);

			CodecType detectCodec = detectCodecType(dirPathInfo);
			Preconditions.checkArgument(detectCodec != null, " detect="
					+ detectCodec);
			dirPathInfo.setCodecType(detectCodec);

			return dirPathInfo;
		}
	}

	/**
	 * ??????Hdfs????????????????????????
	 * 
	 * @param sourcePath
	 * @param recursive
	 * @return
	 */
	public HdfsDirPathInfo getHdfsDirAndSmallFileInfo(String sourcePath,
			boolean recursive) {
		HdfsDirPathInfo dirPathInfo = new HdfsDirPathInfo(sourcePath);
		try {
			RemoteIterator<LocatedFileStatus> fileIterator = fs.listFiles(
					new Path(sourcePath), recursive);
			while (fileIterator.hasNext()) {
				LocatedFileStatus file = fileIterator.next();
				dirPathInfo.addFile(file);
				if (file.getLen() < DEFAULT_MERGE_THRESHOLD) {
					dirPathInfo.addSmallFile(file);
				}
			}

		} catch (IOException e) {
			log.error("??????Hdfs??????????????????:", e);
		}
		return dirPathInfo;
	}

	/**
	 * ????????????????????????
	 * 
	 * @param dir
	 * @return
	 */
	public Boolean isExist(String dir) {
		try {
			return fs.exists(new Path(dir));
		} catch (Exception e) {
			log.error("??????????????????", e);
			throw new BusinessException("??????????????????");
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param dir
	 *            hdfs??????
	 * @param quotaNum
	 *            ????????????????????????
	 */
	public void setNsQuota(String dir, Long quotaNum) {
		try {
			if (!StrUtil.isBlankIfStr(quotaNum)) {
				hdfsAdmin.setQuota(new Path(dir), quotaNum);
			}
		} catch (Exception e) {
			log.error("????????????????????????", e);
			throw new BusinessException("????????????????????????");
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param dir
	 *            hdfs??????
	 */
	public void clearNsQuota(String dir) {
		try {
			hdfsAdmin.clearQuota(new Path(dir));
		} catch (Exception e) {
			log.error("????????????????????????", e);
			throw new BusinessException("????????????????????????");
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param dir
	 *            hdfs??????
	 * @param quotaSize
	 *            ????????????
	 */
	public void setDsQuota(String dir, Long quotaSize) {
		try {
			if (!StrUtil.isBlankIfStr(quotaSize)) {
				hdfsAdmin.setSpaceQuota(new Path(dir), quotaSize);
			}
		} catch (Exception e) {
			log.error("????????????????????????", e);
			throw new BusinessException("????????????????????????");
		}
	}

	/**
	 * ??????????????????
	 * 
	 * @param dir
	 *            hdfs??????
	 */
	public void clearDsQuota(String dir) {
		try {
			hdfsAdmin.clearSpaceQuota(new Path(dir));
		} catch (Exception e) {
			log.error("????????????????????????", e);
			throw new BusinessException("????????????????????????");
		}
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param dirPathInfo
	 * @return
	 * @throws Exception
	 */
	private CodecType detectCodecType(SdpsDirPathInfo dirPathInfo)
			throws Exception {
		List<Path> randomPaths = getRandomPaths(dirPathInfo.getFilePaths());
		CodecType finalCodec = null;
		boolean same = true;
		FormatType format = dirPathInfo.getFormatType();

		for (Path path : randomPaths) {
			CodecType codec;
			switch (format) {
			case AVRO:
				codec = readAvroCodec(path);
				break;
			case SEQ:
				codec = readSeqCodec(path);
				break;
			case ORC:
				codec = readORCCodec(path);
				break;
			case PARQUET:
				codec = readParquetCodec(path);
				break;
			case TEXT:
			default:
				codec = detectCodecTypeByExt(dirPathInfo);
			}

			Preconditions
					.checkNotNull(codec,
							"[merge.detect_codec_null] can't get dir path info of codecType");
			if (finalCodec == null) {
				finalCodec = codec;
			}

			if (finalCodec.getClass() != codec.getClass()) {
				same = false;
				break;
			}
		}

		Preconditions.checkArgument(same,
				"[merge.detect_codec_mismatch] file codec type is not same for dir="
						+ dirPathInfo.getDirPath());
		return finalCodec;
	}

	/**
	 * ??????avro????????????????????????
	 * 
	 * @param path
	 *            hdfs??????
	 * @return
	 * @throws Exception
	 */
	private CodecType readAvroCodec(Path path) throws Exception {
		DataFileStream reader = new DataFileStream(fs.open(path),
				new GenericDatumReader());

		CodecType codecType;
		try {
			Schema schema = reader.getSchema();
			String codec = reader.getMetaString("avro.codec");
			log.info("??????avro???????????? schema={}, codec={}", schema, codec);
			codecType = CodecType.getTypeByName(codec, CodecType.UNCOMPRESSED);
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					throw e;
				}
			}

		}

		return codecType;
	}

	/**
	 * ??????seq????????????????????????
	 * 
	 * @param path
	 *            hdfs??????
	 * @return
	 * @throws Exception
	 */
	private CodecType readSeqCodec(Path path) throws Exception {
		SequenceFile.Reader reader = new SequenceFile.Reader(fs.getConf(),
				new SequenceFile.Reader.Option[] { SequenceFile.Reader
						.file(path) });

		CodecType codecType;
		try {
			CompressionCodec compressionCodec = reader.getCompressionCodec();
			if(Objects.isNull(compressionCodec)){
				codecType = CodecType.getTypeByCodec("", CodecType.UNCOMPRESSED);
			}else {
				codecType = CodecType.getTypeByCodec(compressionCodec.getClass().getName(), CodecType.UNCOMPRESSED);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					throw e;
				}
			}

		}

		return codecType;
	}

	/**
	 * ??????orc????????????????????????
	 * 
	 * @param path
	 *            hdfs??????
	 * @return
	 * @throws Exception
	 */
	private CodecType readORCCodec(Path path) throws Exception {
		org.apache.orc.Reader reader = OrcFile.createReader(path,
				OrcFile.readerOptions(fs.getConf()));
		CompressionKind codec = reader.getCompressionKind();

		return CodecType.getTypeByName(codec.name(), CodecType.UNCOMPRESSED);
	}

	/**
	 * ??????parquet????????????????????????
	 * 
	 * @param path
	 *            hdfs??????
	 * @return
	 * @throws Exception
	 */
	private CodecType readParquetCodec(Path path) throws Exception {
		ParquetMetadata metadata = ParquetFileReader.readFooter(fs.getConf(),
				path, ParquetMetadataConverter.NO_FILTER);
		CompressionCodecName codec = CompressionCodecName.UNCOMPRESSED;

		try {
			codec = metadata.getBlocks().get(0).getColumns().get(0).getCodec();
		} catch (Exception e) {
			log.error("??????parquet????????????????????????", e);
		}

		return CodecType.getTypeByName(codec.getParquetCompressionCodec()
				.name(), CodecType.UNCOMPRESSED);
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param dirPathInfo
	 * @return
	 */
	private CodecType detectCodecTypeByExt(SdpsDirPathInfo dirPathInfo) {
		List<Path> randomPaths = getRandomPaths(dirPathInfo.getFilePaths());
		CodecType finalCodec = null;
		boolean same = true;
		for (Path path : randomPaths) {
			String extension = FilenameUtils.getExtension(path.getName());
			CodecType codec = CodecType.getTypeByExtension(extension);
			Preconditions
					.checkNotNull(codec,
							"[merge.detect_codec_null] can't get dir path info of codecType");
			if (finalCodec == null) {
				finalCodec = codec;
			}

			if (finalCodec.getClass() != codec.getClass()) {
				same = false;
				break;
			}
		}

		Preconditions.checkArgument(same,
				"[merge.detect_codec_mismatch] file codec type is not same for dir="
						+ dirPathInfo.getDirPath());
		return finalCodec;
	}

	/**
	 * ??????????????????
	 * 
	 * @param dirPathInfo
	 * @return
	 * @throws Exception
	 */
	private FormatType detectFileType(SdpsDirPathInfo dirPathInfo)
			throws Exception {
		List<Path> randomPaths = getRandomPaths(dirPathInfo.getFilePaths());
		FormatType finalType = null;
		boolean same = true;

		for (int i = 0; i < randomPaths.size(); ++i) {
			Path path = randomPaths.get(i);
			FormatType formatType = null;
			FSDataInputStream input = fs.open(path);

			try {
				byte[] header = new byte[100];
				input.read(header);
				String magicHeader = new String(
						Arrays.copyOfRange(header, 0, 3));
				formatType = FormatType.getByMagic(magicHeader);
			} catch (Exception e) {
				throw e;
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (Exception e) {
						throw e;
					}
				}

			}

			if (formatType == null) {
				CodecType detectCodec = detectCodecTypeByExt(dirPathInfo);
				formatType = getTextFormatType(path, detectCodec);
			}

			Preconditions.checkNotNull(formatType,
					"[merge.detect_format_null] can't detect get dir path info of formatType of="
							+ path.toString());
			if (finalType == null) {
				finalType = formatType;
			}

			if (finalType != formatType) {
				same = false;
				break;
			}
		}

		Preconditions.checkArgument(same,
				"[merge.detect_format_mismatch] file format type is not same for dir="
						+ dirPathInfo.getDirPath());
		return finalType;
	}

	/**
	 * ??????????????????????????????
	 * 
	 * @param path
	 *            ??????
	 * @param codecType
	 *            ??????
	 * @return
	 */
	private FormatType getTextFormatType(Path path, CodecType codecType)
			throws Exception {
		FormatType formatType = null;

		try {
			FSDataInputStream input = fs.open(path);

			try {
				byte[] header = new byte[100];
				if (codecType == CodecType.UNCOMPRESSED) {
					input.read(header);
				} else {
					Class<?> codecClass = Class.forName(codecType.getCodec());
					CompressionCodec codec = (CompressionCodec) ReflectionUtils
							.newInstance(codecClass, fs.getConf());
					CompressionInputStream codecHeader = codec
							.createInputStream(input);
					codecHeader.read(header);
				}

				String mimeType = Magic.getMagicMatch(header, false)
						.getMimeType();
				formatType = FormatType.getByMagic(mimeType);
			} catch (Exception e) {
				throw e;
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (Exception e) {
						throw e;
					}
				}

			}
		} catch (Exception e) {
			throw e;
		}

		return formatType;
	}

	/**
	 * ????????????????????????
	 * 
	 * @param paths
	 * @return
	 */
	private List<Path> getRandomPaths(List<Path> paths) {
		int count = 3;
		List<Path> randomPaths = new ArrayList(3);
		if (CollectionUtils.isEmpty(paths)) {
			return randomPaths;
		} else if (paths.size() <= count) {
			return paths;
		} else {
			while (count > 0) {
				int random = (new Random()).nextInt(paths.size());
				randomPaths.add(paths.get(random));
				--count;
			}

			return randomPaths;
		}
	}

	public void logoutUser() {
		try {
			if(Objects.nonNull(this.userGroupInformation)){
				this.userGroupInformation.logoutUserFromKeytab();
			}
		} catch (IOException e) {
			log.error("??????:{},??????kerberos??????:{}",
					this.userGroupInformation.getShortUserName(), e);
		}
	}
}
