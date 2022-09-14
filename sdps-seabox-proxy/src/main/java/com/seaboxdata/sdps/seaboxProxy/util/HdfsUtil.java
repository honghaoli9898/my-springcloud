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
	 * 构造
	 * 
	 * @param clusterId
	 */
	public HdfsUtil(Integer clusterId) {
		try {
			init(clusterId);
		} catch (Exception e) {
			log.error("HdfsUtil初始化异常:", e);
		}
	}

	/**
	 * 构造HdfsUtil主要为特定用户使用
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
			log.error("HdfsUtil初始化异常:", e);
		}
	}

	/**
	 * 初始化HdfsUtil
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
		// 加载集群配置
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
			//获取hbase组件所在的host
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
				log.error("身份认证异常:{} ", e);
				throw new BusinessException("身份认证异常:".concat(e.getMessage()));
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
						log.error("获取fileSystem报错:{}", e);
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
//		// 加载集群配置
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
//			log.error("kerberos登录异常", e);
//			String message = e.getMessage();
////			if (message.endsWith("javax.security.auth.login.LoginException: Unable to obtain password")) {
////				result = true;
////			}
//			result = true;
//		}
//		return result;
//	}

	/**
	 * 获取FS对象
	 * 
	 * @return
	 */
	public FileSystem getFs() {
		return fs;
	}

	/**
	 * 获取hdfsAdmin对象
	 * 
	 * @return
	 */
	public HdfsAdmin getHdfsAdmin() {
		return hdfsAdmin;
	}

	/**
	 * 获取dfsAdmin对象
	 * 
	 * @return
	 */
	public DFSAdmin getDfsAdmin() {
		return dfsAdmin;
	}

	/**
	 * 获取用户
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
			log.error("Hdfs FileSystem关闭异常:", e);
		}
	}

	/**
	 * 创建文件夹
	 *
	 * @param createHdfsPath
	 *            路径
	 * @return 是否创建成功。
	 */
	public boolean mkdir(String createHdfsPath) {
		if (StrUtil.isBlankIfStr(createHdfsPath)) {
			return false;
		}
		Path path = new Path(createHdfsPath);
		try {
			if (fs.exists(path)) {
				log.info("目录已存在，无法创建.{}", createHdfsPath);
			}
		} catch (IOException e) {
			log.error("目录创建异常.", e);
		}
		boolean bool = false;
		try {
			bool = fs.mkdirs(path);
		} catch (Exception e) {
			log.error("Hdfs 创建目录异常:", e);
		}
		return bool;
	}

	/**
	 * 设置 文件数限制、存储空间最大配额
	 *
	 * @param hdfsPath
	 *            hdfs路径
	 * @param quotaNum
	 *            文件数限制(当前目录数量计1,文件夹数量计1)
	 * @param spaceQuotaNum
	 *            存储空间最大配置(单位:B)
	 * @param owner
	 *            所属者
	 * @param ownergroup
	 *            所属组
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
				log.error("Hdfs 更新文件数限制、存储空间最大配额、所属者、所属组异常:hdfs目录[" + hdfsPath
						+ "]不存在");
				throw new BusinessException(
						"Hdfs 更新文件数限制、存储空间最大配额、所属者、所属组异常:hdfs目录[" + hdfsPath
								+ "]不存在");
			}
		} catch (Exception e) {
			log.error("Hdfs 更新文件数限制、存储空间最大配额、所属者、所属组异常:", e);
			throw new BusinessException(e.getMessage());
		}
		return bool;
	}

	/**
	 * Hdfs 创建目录并设置 文件数限制、存储空间最大配额、所属用户组
	 *
	 * @param createHdfsPath
	 *            创建路径
	 * @param quotaNum
	 *            文件数限制(当前目录数量计1,文件夹数量计1)
	 * @param spaceQuotaNum
	 *            存储空间最大配置(单位:B)
	 * @param owner
	 *            所属者
	 * @param ownergroup
	 *            所属组
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
				log.error("删除Hdfs目录[" + createHdfsPath + "]异常", ioException);
			}
			log.error("Hdfs 创建目录并设置 文件数限制、存储空间最大配额、所属用户组异常", e);
		}
		return bool;
	}

	/**
	 * Hdfs 创建目录并所属用户和组
	 *
	 * @param createHdfsPath
	 *            创建路径
	 * @param owner
	 *            所属者
	 * @param ownergroup
	 *            所属组
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
				log.error("删除Hdfs目录[" + createHdfsPath + "]异常", ioException);
			}
			log.error("Hdfs 创建目录并设置所属用户和组异常", e);
		}
		return bool;
	}

	/**
	 * 修改权限
	 *
	 * @param hdfsPath
	 * @param permissionCode
	 *            权限码(例:775)
	 * @return
	 */
	public boolean setPermission(String hdfsPath, String permissionCode) {
		boolean bool = false;
		try {
			fs.setPermission(new Path(hdfsPath),
					new FsPermission(Short.parseShort(permissionCode, 8)));
			bool = true;
		} catch (Exception e) {
			log.error("Hdfs 设置权限异常:", e);
		}
		return bool;
	}

	/**
	 * 删除hdfs目录
	 * 
	 * @throws IOException
	 */
	public boolean deleteFile(List<String> hdfsPaths) throws IOException {
		boolean bool = false;
		if (StrUtil.isBlankIfStr(hdfsPaths)) {
			log.error("hdfsPath为空.");
			return bool;
		}
		for (String hdfsPath : hdfsPaths) {
			Path path = new Path(hdfsPath);
			if (fs.exists(path)) {
				bool = fs.delete(path, true);
			} else {
				log.error("hdfs目录不存在{}", path);
			}
		}
		return bool;
	}

	/**
	 * 移动hdfs目录到回收站
	 */
	public boolean moveToTrash(String hdfsPath) throws Exception {
		Boolean flag = false;

		// 创建trash
		Trash trash = new Trash(fs, fs.getConf());
		// 通过传入的path将hdfs中的目标文件移入回收站
		// moveToTrash返回值为boolean类型，如果为true则成功
		flag = trash.moveToTrash(new Path(hdfsPath));

		return flag;
	}

	/**
	 * 清理hdfs目录
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
					log.info("清空hdfs目录:" + hdfsPath);
				}
			}
			bool = true;
		} catch (Exception e) {
			log.error("清理hdfs目录异常:", e);
		}
		return bool;
	}

	/**
	 * 查询hdfs目录信息(目录名称、所属者、所属组、空间消耗值(字节)、存储空间最大配额(字节)、文件数限制、修改时间)
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
			log.error("获取hdfs目录[" + hdfsPath + "]信息异常:", e);
		}
		return hdfsDirObjsList;
	}

	/**
	 * 查询hdfs信息(存储空间最大配额(字节)、文件数限制)
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
			log.error("获取hdfs目录[" + hdfsPath + "]信息异常:", e);
		}
		return hdfsDirObj;
	}

	/**
	 * 查询HDFS文件目录
	 *
	 * @param hdfsPath
	 *            hdfs路径
	 * @return 查询结果列表
	 */
	public ArrayList<HdfsFSObj> selectHdfsSaveObjList(String hdfsPath) {
		ArrayList<HdfsFSObj> hdfsFSObjs = new ArrayList<>();
		try {
			if (!fs.exists(new Path(hdfsPath))) {
				throw new BusinessException("查询目录不存在");
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
			log.error("获取hdfs目录[" + hdfsPath + "]信息异常:", e);
			throw new BusinessException(e.getMessage());
		}
		return hdfsFSObjs;
	}

	/**
	 * 修改文件权限
	 *
	 * @param path
	 *            待修改的文件路径
	 * @param permission
	 *            新权限
	 * @return 新的文件
	 */
	public boolean permission(String path, String permission) {
		// System.setProperty("HADOOP_USER_NAME", "hdfs");
		boolean flag = false;
		if (StrUtil.isBlankIfStr(path)) {
			log.error("路径为空:{}", path);
			return flag;
		}
		Path file = new Path(path);
		try {
			if (fs.exists(file)) {
				fs.setPermission(file,
						new FsPermission(Integer.parseInt(permission, 8)));
				flag = true;
			} else {
				log.error("文件不存在,修改权限失败.");
				flag = false;
			}
		} catch (IOException e) {
			log.error("修改权限异常.{}", e);
		}
		return flag;
	}

	/**
	 * 拉取HDFS元数据文件
	 *
	 * @return
	 */
	public boolean fetchHdfsImage(File saveFile) {

		final boolean[] flag = {false};
		try {
			log.info("当前登录用户:");
			log.info("ShortUserName:"+this.userGroupInformation.getShortUserName());
			log.info("UserName:"+this.userGroupInformation.getUserName());
			if(isEnableKerberos){
				this.userGroupInformation.doAs(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						try {
							// 拉取元数据（参数1:String数组,参数2：取参数1数组中第几个参数，从0开始）
							int fetchNum = dfsAdmin.fetchImage(
									new String[] { saveFile.getAbsolutePath() }, 0);
							if (fetchNum == 0 && saveFile.exists()) {
								flag[0] = true;
							}
						} catch (Exception e) {
							log.error("开启kerberos情况下,拉取HDFS元数据异常:{}", e);
						}
						return null;
					}
				});
			}else {
				// 拉取元数据（参数1:String数组,参数2：取参数1数组中第几个参数，从0开始）
				int fetchNum = dfsAdmin.fetchImage(
						new String[] { saveFile.getAbsolutePath() }, 0);
				if (fetchNum == 0 && saveFile.exists()) {
					flag[0] = true;
				}
			}


		} catch (Exception e) {
			log.error("拉取HDFS元数据异常:", e);
		}
		return flag[0];
	}

	/**
	 * 提取HDFS元数据
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
			log.error("提取HDFS元数据异常:", e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (accessFile != null) {
					accessFile.close();
				}
			} catch (IOException e) {
				log.error("闭流异常：", e);
			}
		}
		return true;
	}

	/**
	 * 文件重命名
	 *
	 * @param oldPath
	 *            旧文件路径
	 * @param oldPath
	 *            新文件路径
	 * @return 是否重命名成功
	 */
	public boolean rename(String oldPath, String newPath) {
		boolean flag = false;
		if (StrUtil.isBlankIfStr(oldPath) || StrUtil.isBlankIfStr(newPath)) {
			log.error("旧文件路径为空:{}，新文件路径为空:{}", oldPath, newPath);
			return false;
		}
		try {
			if (fs.exists(new Path(newPath))) {
				throw new BusinessException("文件名已存在");
			}
			flag = fs.rename(new Path(oldPath), new Path(newPath));
		} catch (Exception e) {
			log.error("文件重命名出现异常.{}", e);
			closeFs();
			throw new BusinessException(e.getMessage());
		}
		return flag;
	}

	/**
	 * 根据目录获取hdfs存储量
	 * 
	 * @param dir
	 *            hdfs路径
	 * @return
	 */
	public HdfsFSObj getStorageSize(String dir) {
		HdfsFSObj fsObj = new HdfsFSObj();
		try {
			Path path = new Path(dir);
			// 获取已使用的空间量
			ContentSummary contentSummary = fs.getContentSummary(path);
			long spaceConsumed = contentSummary.getSpaceConsumed();
			fsObj.setUsed(spaceConsumed);
			// 获取集群总空间量
			long total = 0L;
			DistributedFileSystem dfs = (DistributedFileSystem) fs;
			DatanodeInfo[] dataNodeStats = dfs.getDataNodeStats();
			for (DatanodeInfo datanodeInfo : dataNodeStats) {
				total += datanodeInfo.getCapacity();
			}
			fsObj.setTotalCapacity(total);
		} catch (Exception e) {
			log.error("获取统计数据异常:{}", e);
		}
		return fsObj;
	}

	/**
	 * 获取目录的总结上下文
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
			log.error("获取hdfs目录信息报错", e);
			throw new BusinessException("获取hdfs目录信息报错");
		}
		return sdpsContentSummary;
	}

	/**
	 * 获取多个目录的统计总和
	 * 
	 * @param paths
	 *            目录集合
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
	 * 获取子集文件对象列表
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
			log.error("获取hdfs目录下文件信息报错", e);
			throw new BusinessException("获取hdfs目录下文件信息报错");
		}
	}

	/**
	 * 截取hive文件夹路径
	 * 
	 * @param hdfsPath
	 *            hive文件夹全路径
	 * @return
	 */
	public static String getHdfsPathLocation(String hdfsPath) {
		String substring = hdfsPath.substring(hdfsPath.indexOf("//") + 2);
		return substring.substring(substring.indexOf("/"));
	}

	/**
	 * 获取目录信息
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
	 * 设置文件目录对象（存储空间最大配额(字节)、文件数限制、修改时间）
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
	 * 获取目录文件信息,可递归
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
		// 遍历获取目录下的小文件
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
		// 如果不存在小文件，则直接返回
		if (CollectionUtils.isEmpty(dirPathInfo.getFilePaths())) {
			return dirPathInfo;
		} else {
			// 如果存在小文件，则获取文件格式和压缩编码
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
	 * 获取Hdfs目录和小文件信息
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
			log.error("获取Hdfs目录信息异常:", e);
		}
		return dirPathInfo;
	}

	/**
	 * 判断路径是否存在
	 * 
	 * @param dir
	 * @return
	 */
	public Boolean isExist(String dir) {
		try {
			return fs.exists(new Path(dir));
		} catch (Exception e) {
			log.error("查询路径报错", e);
			throw new BusinessException("查询路径报错");
		}
	}

	/**
	 * 设置文件数量
	 * 
	 * @param dir
	 *            hdfs路径
	 * @param quotaNum
	 *            文件、文件夹数量
	 */
	public void setNsQuota(String dir, Long quotaNum) {
		try {
			if (!StrUtil.isBlankIfStr(quotaNum)) {
				hdfsAdmin.setQuota(new Path(dir), quotaNum);
			}
		} catch (Exception e) {
			log.error("设置文件数量报错", e);
			throw new BusinessException("设置文件数量报错");
		}
	}

	/**
	 * 清空逻辑配额
	 * 
	 * @param dir
	 *            hdfs路径
	 */
	public void clearNsQuota(String dir) {
		try {
			hdfsAdmin.clearQuota(new Path(dir));
		} catch (Exception e) {
			log.error("清除逻辑配额报错", e);
			throw new BusinessException("清除逻辑配额报错");
		}
	}

	/**
	 * 设置空间配额
	 * 
	 * @param dir
	 *            hdfs路径
	 * @param quotaSize
	 *            空间配额
	 */
	public void setDsQuota(String dir, Long quotaSize) {
		try {
			if (!StrUtil.isBlankIfStr(quotaSize)) {
				hdfsAdmin.setSpaceQuota(new Path(dir), quotaSize);
			}
		} catch (Exception e) {
			log.error("设置空间配额报错", e);
			throw new BusinessException("设置空间配额报错");
		}
	}

	/**
	 * 清空物理配额
	 * 
	 * @param dir
	 *            hdfs路径
	 */
	public void clearDsQuota(String dir) {
		try {
			hdfsAdmin.clearSpaceQuota(new Path(dir));
		} catch (Exception e) {
			log.error("清除空间配额报错", e);
			throw new BusinessException("清除空间配额报错");
		}
	}

	/**
	 * 根据路径下文件获取格式编码
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
	 * 读取avro格式文件压缩编码
	 * 
	 * @param path
	 *            hdfs路径
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
			log.info("读取avro文件信息 schema={}, codec={}", schema, codec);
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
	 * 读取seq格式文件压缩编码
	 * 
	 * @param path
	 *            hdfs路径
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
	 * 读取orc格式文件压缩编码
	 * 
	 * @param path
	 *            hdfs路径
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
	 * 读取parquet格式文件压缩编码
	 * 
	 * @param path
	 *            hdfs路径
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
			log.error("获取parquet文件压缩信息报错", e);
		}

		return CodecType.getTypeByName(codec.getParquetCompressionCodec()
				.name(), CodecType.UNCOMPRESSED);
	}

	/**
	 * 根据文件后缀获取压缩编码
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
	 * 获取文件格式
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
	 * 获取文本类型文件格式
	 * 
	 * @param path
	 *            路径
	 * @param codecType
	 *            编码
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
	 * 随机获取三个路径
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
			log.error("用户:{},登出kerberos失败:{}",
					this.userGroupInformation.getShortUserName(), e);
		}
	}
}
