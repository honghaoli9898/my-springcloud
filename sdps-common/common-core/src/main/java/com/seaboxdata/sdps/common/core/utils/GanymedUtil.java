package com.seaboxdata.sdps.common.core.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

/**
 * Shell连接工具
 *
 * @author jiao
 * @since 2019/9/20 16:06
 */
@Slf4j
public class GanymedUtil {

	private static Connection login(String ip, int port, String username,
			String password) throws Exception {
		boolean flag;
		Connection connection = null;
		try {
			connection = new Connection(ip, port);
			connection.connect();// 连接
			flag = connection.authenticateWithPassword(username, password);// 认证
			if (flag) {
				System.out.println("================登录成功==================");
				return connection;
			}
		} catch (IOException e) {
			log.error("登录失败,请检查IP或端口是否有误", e);
			connection.close();
			// System.exit(-1);
			throw new Exception("登录失败,请检查IP或端口是否有误");
		}
		return connection;
	}

	/**
	 * 远程执行shell脚本或者命令
	 *
	 * @param command
	 *            即将执行的命令
	 * @return 命令执行完后返回的结果值
	 */
	private static String execCommand(Connection connection, String command)
			throws Exception {
		String result = "";
		try {
			if (connection != null) {
				Session session = null;// 打开一个会话
				try {
					session = connection.openSession();
				} catch (IllegalStateException ise) {
					log.error("请检查用户名或密码是否有误", ise);
					// System.exit(-1);
					throw new Exception("请检查用户名或密码是否有误");
				}
				try {
					session.execCommand(command);// 执行命令
				} catch (IOException e) {
					log.error("执行命令:{},报错", command, e);
				}
				String DEFAULT_CHART = "UTF-8";
				result = processStdout(session.getStdout(), DEFAULT_CHART);
				if ("".equals(result)) {
					log.error("请检查脚本内容是否有误");
					// System.exit(1);
					throw new Exception("请检查脚本内容是否有误");
				}
				connection.close();
				session.close();
			}
		} catch (IOException e) {
			log.error("执行命令失败,链接conn:{},执行的命令:{}", connection, command, e);
		}
		return result;
	}

	/**
	 * 解析脚本执行返回的结果集
	 *
	 * @param in
	 *            输入流对象
	 * @param charset
	 *            编码
	 * @return 以纯文本的格式返回
	 */
	private static String processStdout(InputStream in, String charset) {
		InputStream stdout = new StreamGobbler(in);
		StringBuilder buffer = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					stdout, charset));
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line).append("\n");
			}
			br.close();
		} catch (IOException e) {
			log.error("解析脚本出错", e);
		}
		return buffer.toString();
	}

	public static JSONObject ganymedExecCommand(String host, int port,
			String username, String password, String command) throws Exception {

		Connection connection = login(host, port, username, password);
		String execCommand = execCommand(connection, command);
		/*
		 * if (execCommand.contains("successfully")) { return new
		 * JSONObject().fluentPut("result", true).fluentPut("data",
		 * execCommand); } else { return new JSONObject().fluentPut("result",
		 * false); }
		 */
		return new JSONObject().fluentPut("result", true).fluentPut("data",
				execCommand);
	}

	// downLoadFile from Linux
	public static boolean uploadFile(String remoteFilePath,
			String localFilePath, String user, String password, String ip,
			int port) {
		boolean bool = false;
		Connection connection = null;
		try {
			connection = login(ip, port, user, password);
			SCPClient scpClient = connection.createSCPClient();
			scpClient.put(localFilePath, remoteFilePath);
			bool = true;
		} catch (Exception e) {
			log.error("本地目录:{},上传到ip:{},目录:{},失败", localFilePath, ip,
					remoteFilePath, e);
			bool = false;
		} finally {
			connection.close();
		}
		return bool;
	}

	// uploadFile to Linux
	public static boolean sftpDownload(String remoteFilePath,
			String localFilePath, String user, String password, String ip,
			int port) {
		boolean bool = false;
		Connection connection = null;
		try {
			connection = login(ip, port, user, password);
			SCPClient scpClient = connection.createSCPClient();
			scpClient.get(remoteFilePath, localFilePath);
			bool = true;
		} catch (Exception e) {
			log.error("从ip:{},目录:{},下载到本地目录:{}失败", ip, remoteFilePath,
					localFilePath, e);
			bool = false;
		} finally {
			connection.close();
		}
		return bool;
	}

	public static void main(String[] args) throws Exception {
		String ip = "10.1.3.11";
		String username = "root";
		String password = "*****";
		int port = 22;

		/* String url = "ls /;"; */
		/*
		 * String url = "https://github.com/jiaoht/LinuxUse/raw/master/CatDisk";
		 * String getArgsExecScript = " wget -c " + install_url +
		 * " -O installer; sh installer;\n";
		 * 
		 * String rmInstaller = " rm -rf installer;\n"; String getAndRm =
		 * getArgsExecScript + rmInstaller; String command = "cd /tmp;\n" +
		 * "if [ ! -f 'installer' ];then\n" + getAndRm + "else\n" + rmInstaller
		 * + getAndRm + "fi";
		 */
		String command = "cat /root/.ssh/id_rsa";

		JSONObject jsonObject = ganymedExecCommand(ip, port, username,
				password, command);
		System.out.println(jsonObject.getString("data"));
		// System.out.println(ganymedExecCommand(ip, port, username, password,
		// command));
	}
}