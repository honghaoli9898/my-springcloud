package com.seaboxdata.sdps.seaboxProxy.test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedAction;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.tools.DFSAdmin;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;

/**
 * @time 2017年7月14日
 * @author YeChunBo 类说明： Hdfs api 的相关操作(Kerberos已开启)
 */
public class HdfsSimple {

	public UserGroupInformation getUserGroupInformation() {
		return userGroupInformation;
	}

	public void setUserGroupInformation(
			UserGroupInformation userGroupInformation) {
		this.userGroupInformation = userGroupInformation;
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public FileSystem getFs() {
		return fs;
	}

	public void setFs(FileSystem fs) {
		this.fs = fs;
	}

	private UserGroupInformation userGroupInformation;

	private Configuration conf;

	private FileSystem fs;

	private DFSAdmin dfsAdmin;

	private HdfsAdmin hdfsAdmin;

	public HdfsSimple() {

	}

	public HdfsSimple(String user, String keytabPath) throws Exception {
		Configuration conf = new Configuration();

		conf.addResource(new FileInputStream(new File("C:\\data\\SDP7.1\\sdps\\sdp7.1\\conf\\core-site.xml")));
		conf.addResource(new FileInputStream(new File("C:\\data\\SDP7.1\\sdps\\sdp7.1\\conf\\hdfs-site.xml")));

		conf.set("fs.hdfs.impl",
				org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
		System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
		conf.set("hadoop.security.authentication", "kerberos");
		UserGroupInformation.setConfiguration(conf);
		try {
			System.out.println("======================第一次登录====================");
			UserGroupInformation userGroupInformation = UserGroupInformation
					.loginUserFromKeytabAndReturnUGI(user, keytabPath);
			System.out.println("======================第一次登录结束==================");
			this.userGroupInformation = userGroupInformation;
		} catch (Exception e) {
			System.out.println("身份认证异常： " + e.getMessage());
			e.printStackTrace();
		}
		this.conf = conf;
		
		this.userGroupInformation.doAs(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				try {
					fs = FileSystem.get(new URI("hdfs://master:8020/"), conf);
					dfsAdmin = new DFSAdmin(conf);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});



	}

	/**
	 * 获取Hdfs 指定目录下所有文件
	 * 
	 * @param URI
	 *            hdfs远端连接url,eg:hdfs://hdp39:8020
	 * @param remotePath
	 *            hdfs远端目录路径
	 * @param conf
	 * @throws Exception
	 */
	public void getHdfsFileList(String remotePath) throws Exception {
		this.userGroupInformation.logoutUserFromKeytab();
		System.out.println("=============刷新登录=================");
		this.userGroupInformation.checkTGTAndReloginFromKeytab();
		System.out.println("=============刷新登录结束=================");
		RemoteIterator<LocatedFileStatus> iter = fs.listFiles(new Path(
				remotePath), true);
		while (iter.hasNext()) {
			LocatedFileStatus status = iter.next();
			System.out.println(status.getPath().toUri().getPath());
		}
		fs.close();
	}

	// 读取文件的内容
	public void readFile(String filePath, Configuration conf)
			throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path srcPath = new Path(filePath);
		InputStream in = null;
		try {
			in = fs.open(srcPath);
			IOUtils.copyBytes(in, System.out, 4096, false); // 复制到标准输出流
		} finally {
			IOUtils.closeStream(in);
		}
	}

	/**
	 * 查看文件中的内容
	 * 
	 * @param remoteFile
	 * @return
	 * @throws IOException
	 */
	public String cat(String remoteFile, Configuration conf) throws IOException {
		Path path = new Path(remoteFile);
		FSDataInputStream fsdis = null;
		System.out.println("cat: " + remoteFile);

		OutputStream baos = new ByteArrayOutputStream();
		String str = null;
		try {
			fsdis = fs.open(path);
			IOUtils.copyBytes(fsdis, baos, 4096, false);
			str = baos.toString();
		} finally {
			IOUtils.closeStream(fsdis);
			fs.close();
		}
		System.out.println(str);
		return str;
	}

	// 创建目录
	public void mkdir(String path, Configuration conf) throws IOException,
			URISyntaxException {
		Path srcPath = new Path(path);
		boolean isok = fs.mkdirs(srcPath);
		if (isok) {
			System.out.println("create dir ok!");
		} else {
			System.out.println("create dir failure");
		}
		fs.close();
	}

	/**
	 * 获取hdfs目录相关信息
	 * 
	 * @param URI
	 * @param remotePath
	 * @param conf
	 */
	public void getFileInfo(String remotePath) {
		try {
			Path filenamePath = new Path(remotePath);
			System.out.println("SIZE OF THE HDFS DIRECTORY : "
					+ fs.getContentSummary(filenamePath).getSpaceConsumed()); // 单位是b
			System.out.println("SIZE OF THE HDFS DIRECTORY : "
					+ fs.getContentSummary(filenamePath).getLength());// 一份数据实际占的空间
			FileStatus stat = fs.getFileStatus(new Path(remotePath));
			System.out.print(stat.getAccessTime() + " " + stat.getBlockSize()
					+ " " + stat.getGroup() + " " + stat.getLen() + " "
					+ stat.getModificationTime() + " " + stat.getOwner() + " "
					+ stat.getReplication() + " " + stat.getPermission());
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteFile(String remotePath) {
		try {
			fs.deleteOnExit(new Path(remotePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
//		Thread t1 = new Thread(new KerberosThread("hdfs-seabox4@HADOOP.COM",
//				"hdfs.headless.keytab", "/test01"));
//		Thread t2 = new Thread(new KerberosThread("lihonghao@HADOOP.COM",
//				"lihonghao.keytab", "/test01"));
//		t1.start();
//		t2.start();

		HdfsSimple hdfsSimple = new HdfsSimple("hdfs-seabox4@HADOOP.COM","/data/SDP7.1/sdps/sdp7.1/keytab/1.hdfs.headless.keytab");
		File saveFile = new File("C:\\data\\fsimage_20220419");

//		hdfsSimple.userGroupInformation.doAs(new PrivilegedAction<Object>() {
//			@Override
//			public Object run() {
//				try {
//					int fetchNum = hdfsSimple.dfsAdmin.fetchImage(
//							new String[] { saveFile.getAbsolutePath() }, 0);
//					System.out.println(fetchNum);
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return null;
//			}
//		});
		hdfsSimple.hdfsAdmin.setQuota(new Path(""),11111L);


	}
}

class KerberosThread implements Runnable {
	public String user;
	public String keytabPath;
	public String path;

	public KerberosThread() {

	}

	public KerberosThread(String user, String keytabPath, String path) {
		this.keytabPath = keytabPath;
		this.user = user;
		this.path = path;
	}

	@Override
	public void run() {
		String URI = "hdfs://slave2:8020/";
		HdfsSimple hdfsSimple = null;
		try {
			hdfsSimple = new HdfsSimple(user, keytabPath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			hdfsSimple.getHdfsFileList(path);
			System.out.println(Thread.currentThread().getName()+"-------"+hdfsSimple.getUserGroupInformation().getShortUserName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
