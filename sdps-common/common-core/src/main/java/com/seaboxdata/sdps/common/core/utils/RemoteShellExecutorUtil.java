package com.seaboxdata.sdps.common.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import cn.hutool.core.util.StrUtil;

import com.seaboxdata.sdps.job.core.context.XxlJobHelper;

@Slf4j
public class RemoteShellExecutorUtil {
	private static final int TIME_OUT = 1000 * 5 * 60;
	private Connection conn;
	/**
	 * 远程机器IP
	 */
	private String ip;

	private int port = 22;
	/**
	 * 用户名
	 */
	private String osUsername;
	/**
	 * 密码
	 */
	private String password;
	private String charset = Charset.defaultCharset().toString();

	private StringBuffer outStrStringBuffer;
	private StringBuffer outErrStringBuffer;

	private StringBuffer stdOutBuffer;
	private StringBuffer stdErrBuffer;

	/** 匹配正确输出 15颗* */
	public static final String PATCH_CODE = "***************";

	public StringBuffer getOutStrStringBuffer() {
		return outStrStringBuffer;
	}

	public StringBuffer getOutErrStringBuffer() {
		return outErrStringBuffer;
	}

	public StringBuffer getStdOutBuffer() {
		return stdOutBuffer;
	}

	public StringBuffer getStdErrBuffer() {
		return stdErrBuffer;
	}

	public RemoteShellExecutorUtil(String ip, String usr, String pasword) {
		this.ip = ip;
		this.osUsername = usr;
		this.password = pasword;
		this.outStrStringBuffer = new StringBuffer();
		this.outErrStringBuffer = new StringBuffer();

		this.stdOutBuffer = new StringBuffer();
		this.stdErrBuffer = new StringBuffer();
	}

	public RemoteShellExecutorUtil(String ip, String usr, String pasword,
			Integer port) {
		this.ip = ip;
		this.osUsername = usr;
		this.password = pasword;
		this.outStrStringBuffer = new StringBuffer();
		this.outErrStringBuffer = new StringBuffer();
		if (Objects.nonNull(port)) {
			this.port = port;
		}
		this.stdOutBuffer = new StringBuffer();
		this.stdErrBuffer = new StringBuffer();
	}

	public static void main(String[] args) throws Exception {
		RemoteShellExecutorUtil executorUtil = new RemoteShellExecutorUtil(
				"10.1.3.24", "root", "1qaz2wsx3edc!QAZ@WSX");
		executorUtil.sftpDownload("/etc/security/keytabs/admin.keytab", "D:/");
		// int i = executorUtil.exec("/root/sdps/extend-analysis.sh");
//		int i = executorUtil.exec2("/root/sdps/extend-analysis.sh");
		// int i = executorUtil.exec2("sh /root/a.sh");
		// int i = executorUtil.exec("sh /root/a.sh");
//		System.out.println(i);
	}

	/**
	 * 登录
	 *
	 * @return
	 */
	public boolean login() {
		boolean bool = false;
		conn = new Connection(ip, port);
		try {
			conn.connect();
			bool = conn.authenticateWithPassword(osUsername, password);
			log.info("登录" + (bool ? "成功" : "失败，账号密码有误"));
		} catch (IOException e) {
			log.error("登录失败 ", e);
			// e.printStackTrace();
		}
		return bool;
	}

	/**
	 * 执行脚本
	 *
	 * @param cmds
	 * @return
	 * @throws Exception
	 */
	public int exec(String cmds) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		String outStr = "";
		String outErr = "";
		int ret = -1;
		try {
			if (login()) {
				// Open a new {@link Session} on this connection
				Session session = conn.openSession();
				// Execute a command on the remote machine.
				session.execCommand(cmds);

				XxlJobHelper.log("remote exec:");
				XxlJobHelper.log(cmds);
				log.info("remote exec:");
				log.info(cmds);

				stdOut = new StreamGobbler(session.getStdout());
				outStr = processStream(stdOut, charset);

				stdErr = new StreamGobbler(session.getStderr());
				outErr = processStream(stdErr, charset);

				session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);

				log.info("RemoteShellExecutorUtil exec outStr:");
				log.info(outStr);
				XxlJobHelper.log("RemoteShellExecutorUtil exec outStr:");
				XxlJobHelper.log(outStr);
				log.info("RemoteShellExecutorUtil exec outErr:");
				log.info(outErr);
				XxlJobHelper.log("RemoteShellExecutorUtil exec outErr:");
				XxlJobHelper.log(outErr);

				ret = session.getExitStatus();
			} else {
				throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	private String processStream(InputStream in, String charset) {
		BufferedReader bufferedReader = null;
		try {
			StringBuilder sb = new StringBuilder();
			bufferedReader = new BufferedReader(new InputStreamReader(in,
					charset));
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			String result = sb.toString();
			result = StrUtil.sub(result, 0, result.lastIndexOf("\n"));
			return result;
		} catch (Exception e) {
			log.error("获取执行日志报错", e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				log.error("关闭数据流报错", e);
			}
		}
		return "error";
	}

	public int exec2(String cmds) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		int ret = -1;
		try {
			if (login()) {
				Session session = conn.openSession();
				// 建立虚拟终端
				session.requestPTY("bash");
				// 打开一个Shell
				session.startShell();
				stdOut = new StreamGobbler(session.getStdout());
				stdErr = new StreamGobbler(session.getStderr());
				BufferedReader stdoutReader = new BufferedReader(
						new InputStreamReader(stdOut, "utf-8"));
				BufferedReader stderrReader = new BufferedReader(
						new InputStreamReader(stdErr, "utf-8"));

				// 准备输入命令
				PrintWriter out = new PrintWriter(session.getStdin());
				// 输入待执行命令
				out.println(cmds);
				out.println("exit");
				// 6. 关闭输入流
				out.close();
				// 7. 等待，除非1.连接关闭；2.输出数据传送完毕；3.进程状态为退出；4.超时
				session.waitForCondition(ChannelCondition.CLOSED
						| ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
						30000);
				log.info("RemoteShellExecutorUtil exec2 outStr:");
				while (true) {
					String line = stdoutReader.readLine();
					if (line == null) {
						break;
					}
					log.info(line);
					XxlJobHelper.log(line);
					outStrStringBuffer.append(line).append("\n");
				}

				log.info("RemoteShellExecutorUtil exec2 stderr:");
				while (true) {
					String line = stderrReader.readLine();
					if (line == null) {
						break;
					}
					log.info(line);
					XxlJobHelper.log(line);
					outErrStringBuffer.append(line).append("\n");
				}
				/* Show exit status, if available (otherwise "null") */
				log.info("ExitCode: " + session.getExitStatus());
				ret = session.getExitStatus();
				session.close();/* Close this session */
				conn.close();/* Close the connection */

			} else {
				throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	public int execShell(String cmds) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		int ret = -1;
		try {
			if (login()) {
				Session session = conn.openSession();
				// 建立虚拟终端
				session.requestPTY("bash");
				// 打开一个Shell
				session.startShell();
				stdOut = new StreamGobbler(session.getStdout());
				stdErr = new StreamGobbler(session.getStderr());
				BufferedReader stdoutReader = new BufferedReader(
						new InputStreamReader(stdOut, "utf-8"));
				BufferedReader stderrReader = new BufferedReader(
						new InputStreamReader(stdErr, "utf-8"));

				// 准备输入命令
				PrintWriter out = new PrintWriter(session.getStdin());
				// 输入待执行命令
				out.println(cmds);
				out.println("exit");
				// 6. 关闭输入流
				out.close();
				// 7. 等待，除非1.连接关闭；2.输出数据传送完毕；3.进程状态为退出；4.超时
				session.waitForCondition(ChannelCondition.CLOSED
						| ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
						30000);
				log.info("RemoteShellExecutorUtil exec2 outStr:");
				int count = 0;
				while (true) {
					String line = stdoutReader.readLine();
					if (line == null) {
						break;
					}
					log.info(line);
					outStrStringBuffer.append(line).append("\n");

					// 获取指定正确输出
					if (count == 2) {
						continue;
					}
					if (count == 1) {
						if (!PATCH_CODE.equals(line)) {
							stdOutBuffer.append(line).append("\n");
						}
					}
					if (PATCH_CODE.equals(line)) {
						count++;
					}
				}

				log.info("RemoteShellExecutorUtil exec2 stderr:");
				count = 0;
				while (true) {
					String line = stderrReader.readLine();
					if (line == null) {
						break;
					}
					log.info(line);
					outErrStringBuffer.append(line).append("\n");

					// 获取指定正确输出
					if (count == 2) {
						continue;
					}
					if (count == 1) {
						if (!"***************".equals(line)) {
							stdErrBuffer.append(line).append("\n");
						}
					}
					if ("***************".equals(line)) {
						count++;
					}
				}
				/* Show exit status, if available (otherwise "null") */
				log.info("ExitCode: " + session.getExitStatus());
				ret = session.getExitStatus();
				session.close();/* Close this session */
				conn.close();/* Close the connection */

			} else {
				throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	public int commonExec(String cmds) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		String outStr = "";
		String outErr = "";
		int ret = -1;
		try {
			if (login()) {
				Session session = conn.openSession();
				session.execCommand(cmds);
				log.info("remote exec cmd:{}", cmds);
				stdOut = new StreamGobbler(session.getStdout());
				outStr = processStream(stdOut, charset);
				stdErr = new StreamGobbler(session.getStderr());
				outErr = processStream(stdErr, charset);
				session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
				outStrStringBuffer.append(outStr);
				ret = session.getExitStatus();
			} else {
				throw new Exception("登录远程机器失败" + ip); // 自定义异常类 实现略
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	public boolean uploadFile(String remoteFilePath, String localFilePath,
			String user, String password, String ip, int port) {
		boolean bool = false;
		try {
			if (login()) {
				SCPClient scpClient = conn.createSCPClient();
				scpClient.put(localFilePath, remoteFilePath);
				bool = true;
			}

		} catch (Exception e) {
			log.error("本地目录:{},上传到ip:{},目录:{},失败", localFilePath, ip,
					remoteFilePath, e);
			bool = false;
		} finally {
			conn.close();
		}
		return bool;
	}

	public boolean sftpDownload(String remoteFilePath, String localFilePath) {
		boolean bool = false;
		try {
			if (login()) {
				SCPClient scpClient = conn.createSCPClient();
				scpClient.get(remoteFilePath, localFilePath);
				bool = true;
			}

		} catch (Exception e) {
			log.error("从ip:{},目录:{},下载到本地目录:{}失败", ip, remoteFilePath,
					localFilePath, e);
			bool = false;
		} finally {
			conn.close();
		}
		return bool;
	}

	public boolean sftpDownload(String[] remoteFilePathList, String localFilePath) {
		boolean bool = false;
		try {
			if (login()) {
				SCPClient scpClient = conn.createSCPClient();
				scpClient.get(remoteFilePathList, localFilePath);
				bool = true;
			}

		} catch (Exception e) {
			log.error("从ip:{},目录:{},下载到本地目录:{}失败", ip, remoteFilePathList,
					localFilePath, e);
			bool = false;
		} finally {
			conn.close();
		}
		return bool;
	}

}
