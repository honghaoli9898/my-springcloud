package com.seaboxdata.sdps.sshweb.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.seaboxdata.sdps.common.core.constant.CommonConstant;
import com.seaboxdata.sdps.common.core.feign.UserService;
import com.seaboxdata.sdps.common.core.model.Result;
import com.seaboxdata.sdps.common.core.model.SysUser;
import com.seaboxdata.sdps.common.framework.enums.ServerTypeEnum;
import com.seaboxdata.sdps.common.framework.enums.UserSyncStatusEnum;
import com.seaboxdata.sdps.sshweb.pojo.Reboot;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.seaboxdata.sdps.common.core.model.SdpsServerInfo;
import com.seaboxdata.sdps.common.core.model.SysGlobalArgs;
import com.seaboxdata.sdps.common.core.utils.RsaUtil;
import com.seaboxdata.sdps.common.framework.bean.SdpsCluster;
import com.seaboxdata.sdps.common.redis.template.RedisRepository;
import com.seaboxdata.sdps.sshweb.constant.ConstantPool;
import com.seaboxdata.sdps.sshweb.mapper.SdpsClusterMapper;
import com.seaboxdata.sdps.sshweb.mapper.SdpsServerInfoMapper;
import com.seaboxdata.sdps.sshweb.mapper.SysGlobalArgsMapper;
import com.seaboxdata.sdps.sshweb.pojo.SSHConnectInfo;
import com.seaboxdata.sdps.sshweb.pojo.WebSSHData;
import com.seaboxdata.sdps.sshweb.service.WebSSHService;

/**
 * @Description: WebSSH业务逻辑实现
 * @Author: pengsong
 * @Date: 2021/3/8
 */
@Service
@Slf4j
public class WebSSHServiceImpl implements WebSSHService{

	// 存放ssh连接信息的map
	private static Map<String, Object> sshMap = new ConcurrentHashMap<>();

	private Logger logger = LoggerFactory.getLogger(WebSSHServiceImpl.class);
	// 线程池
	private ExecutorService executorService = Executors.newCachedThreadPool();

	@Autowired
	private RedisRepository redisRepository;

	@Autowired
	private SdpsServerInfoMapper serverInfoMapper;

	@Autowired
	private SdpsClusterMapper clusterMapper;

	@Autowired
	private SysGlobalArgsMapper globalArgsMapper;

	@Autowired
	private UserService userService;

	/**
	 * @Description: 初始化连接
	 * @Param: [session]
	 * @return: void
	 * @Author: NoCortY
	 * @Date: 2020/3/7
	 */
	@Override
	public void initConnection(WebSocketSession session) {
		String uuid = String.valueOf(session.getAttributes().get(
				ConstantPool.USER_UUID_KEY));
		JSch jSch = new JSch();
		SSHConnectInfo sshConnectInfo = new SSHConnectInfo();
		sshConnectInfo.setjSch(jSch);
		sshConnectInfo.setWebSocketSession(session);

		// 将这个ssh连接信息放入map中
		sshMap.put(uuid, sshConnectInfo);
	}

	/**
	 * @Description: 处理客户端发送的数据
	 * @Param: [buffer, session]
	 * @return: void
	 * @Author: NoCortY
	 * @Date: 2020/3/7
	 */
	@Override
	public void recvHandle(String buffer, WebSocketSession session) {
		ObjectMapper objectMapper = new ObjectMapper();
		WebSSHData webSSHData = null;
		try {
			webSSHData = objectMapper.readValue(buffer, WebSSHData.class);
		} catch (IOException e) {
			logger.error("Json转换异常");
			logger.error("异常信息:{}", e.getMessage());
			return;
		}

		String userId = String.valueOf(session.getAttributes().get(
				ConstantPool.USER_UUID_KEY));
		if (ConstantPool.WEBSSH_OPERATE_CONNECT.equals(webSSHData.getOperate())) {
			// 找到刚才存储的ssh连接对象
			SSHConnectInfo sshConnectInfo = (SSHConnectInfo) sshMap.get(userId);

			// token校验
			if (StringUtils.isBlank(webSSHData.getAuth())
					|| StringUtils.isBlank(webSSHData.getUser())) {
				return;
			}
			Object redisCode = redisRepository.get(CommonConstant.AUTH_TOKEN
					+ webSSHData.getUser());
			if (Objects.isNull(redisCode)) {
				return;
			}

			if (!StringUtils.equals(webSSHData.getAuth(), redisCode.toString())) {
				return;
			}

			// 启动线程异步处理
			WebSSHData finalWebSSHData = webSSHData;
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						connectToSSH(sshConnectInfo, finalWebSSHData, session, userId);
					} catch (JSchException | IOException e) {
						logger.error("webssh连接异常");
						logger.error("异常信息:{}", e.getMessage());
						close(session);
					}
				}
			});
		} else if (ConstantPool.WEBSSH_OPERATE_COMMAND.equals(webSSHData
				.getOperate())) {
			String command = webSSHData.getCommand();
			SSHConnectInfo sshConnectInfo = (SSHConnectInfo) sshMap.get(userId);
			if (sshConnectInfo != null) {
				try {
					transToSSH(sshConnectInfo.getChannel(), command);
				} catch (IOException e) {
					logger.error("webssh连接异常");
					logger.error("异常信息:{}", e.getMessage());
					close(session);
				}
			}
		} else {
			logger.error("不支持的操作");
			close(session);
		}
	}

	@Override
	public void sendMessage(WebSocketSession session, byte[] buffer)
			throws IOException {
		session.sendMessage(new TextMessage(buffer));
	}

	@Override
	public void close(WebSocketSession session) {
		String userId = String.valueOf(session.getAttributes().get(
				ConstantPool.USER_UUID_KEY));
		SSHConnectInfo sshConnectInfo = (SSHConnectInfo) sshMap.get(userId);
		if (sshConnectInfo != null) {
			// 断开连接
			if (sshConnectInfo.getChannel() != null) {
				sshConnectInfo.getChannel().disconnect();
			}
			// map中移除
			sshMap.remove(userId);
		}
	}

	private String getDecryptPassword(String pass) {
		SysGlobalArgs sysGlobalArgs = globalArgsMapper
				.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
						"password").eq("arg_key", "privateKey"));
		return RsaUtil.decrypt(pass, sysGlobalArgs.getArgValue());
	}

	/**
	 * @Description: 使用jsch连接终端
	 * @Param: [cloudSSH, webSSHData, webSocketSession,userId]
	 * @return: void
	 * @Author: NoCortY
	 * @Date: 2020/3/7
	 */
	private void connectToSSH(SSHConnectInfo sshConnectInfo,
			WebSSHData webSSHData, WebSocketSession webSocketSession, String userId)
			throws JSchException, IOException {
		Session session = null;
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");

		logger.info("用户id" + userId + "登录ssh");

		//根据userId，拉取用户信息
		SysUser sysUser = userService.selectByUserId(userId);
		if (Objects.isNull(webSSHData.getClusterId())) {
			return;
		}
		SdpsCluster sdpsCluster = clusterMapper.selectById(webSSHData
				.getClusterId());
		if (StringUtils.equals(sysUser.getUsername(), "admin")) {

			SdpsServerInfo sdpsServerInfo = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>().eq("server_id",
							sdpsCluster.getServerId()).eq("type", "SSH"));
			// serverInfoMapper
			webSSHData.setHost(sdpsServerInfo.getHost());
			webSSHData.setPort(Integer.valueOf(sdpsServerInfo.getPort()));
			webSSHData.setUsername(sdpsServerInfo.getUser());
			webSSHData.setPassword(getDecryptPassword(sdpsServerInfo.getPasswd()));
			// 获取jsch的会话
			session = sshConnectInfo.getjSch().getSession(webSSHData.getUsername(),
					webSSHData.getHost(), webSSHData.getPort());
			session.setConfig(config);
			// 设置密码
			session.setPassword(webSSHData.getPassword());
			// 连接 超时时间30s
			session.connect(30000);

			// 开启shell通道
			Channel channel = session.openChannel("shell");

			// 通道连接 超时时间3s
			channel.connect(3000);

			// 设置channel
			sshConnectInfo.setChannel(channel);

			// 转发消息
			transToSSH(channel, "\r");

			// 读取终端返回的信息流
			InputStream inputStream = channel.getInputStream();
			try {
				// 循环读取
				byte[] buffer = new byte[1024];
				int i = 0;
				// 如果没有数据来，线程会一直阻塞在这个地方等待数据。
				while ((i = inputStream.read(buffer)) != -1) {
					sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
				}

			} finally {
				// 断开连接后关闭会话
				session.disconnect();
				channel.disconnect();
				if (inputStream != null) {
					inputStream.close();
				}
			}
		} else {
			Result result = null;
			//未同步成功
			if(!StringUtils.equals(sysUser.getSyncUserStatus(), UserSyncStatusEnum.SYNC_SUCCESS.getCode())) {
				result = Result.failed("您所登录的用户暂同步，请同步再使用。");
			}
			//非admin用户走用户同步逻辑
			//1.获取集群root用户密钥
			SdpsServerInfo sdpsServerInfo = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>()
							.eq("user", "root")
							.eq("type", "SSH")
							.eq("server_id", sdpsCluster.getServerId()));
			SysGlobalArgs sysGlobalArgs = globalArgsMapper
					.selectOne(new QueryWrapper<SysGlobalArgs>().eq("arg_type",
							"password").eq("arg_key", "privateKey"));
			//获取shell创建用户名及密码
			SdpsServerInfo shellUser = serverInfoMapper
					.selectOne(new QueryWrapper<SdpsServerInfo>()
							.eq("user", sysUser.getUsername())
							.eq("type", ServerTypeEnum.C.name())
							.eq("server_id", 0));

			String passShell = RsaUtil.decrypt(shellUser.getPasswd(),
					sysGlobalArgs.getArgValue());
			// serverInfoMapper
			log.info("host:"+sdpsServerInfo.getHost());
			log.info("host:"+sdpsServerInfo.getPort());
			log.info("host:"+shellUser.getUser());
			log.info("host:"+passShell);
			webSSHData.setHost(sdpsServerInfo.getHost());
			webSSHData.setPort(Integer.valueOf(sdpsServerInfo.getPort()));
			webSSHData.setUsername(shellUser.getUser());
			webSSHData.setPassword(passShell);
			// 获取jsch的会话
			session = sshConnectInfo.getjSch().getSession(webSSHData.getUsername(),
					webSSHData.getHost(), webSSHData.getPort());
			session.setConfig(config);
			// 设置密码
			session.setPassword(webSSHData.getPassword());
			// 连接 超时时间30s
			session.connect(30000);

			// 开启shell通道
			Channel channel = session.openChannel("shell");

			// 通道连接 超时时间3s
			channel.connect(3000);

			// 设置channel
			sshConnectInfo.setChannel(channel);

			// 转发消息
			transToSSH(channel, "\r");

			// 读取终端返回的信息流
			InputStream inputStream = channel.getInputStream();
			try {
				// 循环读取
				byte[] buffer = new byte[1024];
				int i = 0;
				// 如果没有数据来，线程会一直阻塞在这个地方等待数据。
				while ((i = inputStream.read(buffer)) != -1) {
					sendMessage(webSocketSession, Arrays.copyOfRange(buffer, 0, i));
				}

			} finally {
				// 断开连接后关闭会话
				session.disconnect();
				channel.disconnect();
				if (inputStream != null) {
					inputStream.close();
				}
			}
		}


	}

	/**
	 * @Description: 将消息转发到终端
	 * @Param: [channel, data]
	 * @return: void
	 * @Author: NoCortY
	 * @Date: 2020/3/7
	 */
	private void transToSSH(Channel channel, String command) throws IOException {
		if (channel != null) {
			OutputStream outputStream = channel.getOutputStream();
			outputStream.write(command.getBytes());
			outputStream.flush();
		}
	}

	/**
	 * 登录linux，执行linux命令
	 *
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @param command
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	private static String exeCommand(String host, int port, String user, String password, String command)
			throws JSchException, IOException {
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(password);
		session.connect();

		//Process process = null;

		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
		InputStream in = channelExec.getInputStream();
		channelExec.setCommand(command);
		channelExec.setErrStream(System.err);
		channelExec.connect();
		String out = IOUtils.toString(in, "UTF-8");
		channelExec.disconnect();
		session.disconnect();
		return out;
	}


	public static void route(Reboot rebootBean) {
		String host = rebootBean.getDeviceIp();
		int port = rebootBean.getPort();
		String user = rebootBean.getUsername();
		String password = rebootBean.getPassword();
		String command = "perl -e 'print crypt(\"pass\", \"wtf\")' |xargs useradd test0428 -p";
		String res = "";
		try {
			res = exeCommand(host, port, user, password, command);
		} catch (Exception e){
			log.error("netstat -rn 报错,详情：" + e);
		}
	}

	/**
	 * 测试web终端用户demo
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Reboot reboot = new Reboot();
		reboot.setDeviceIp("10.1.3.114");
		reboot.setPort(22);
		reboot.setUsername("root");
		reboot.setPassword("1qaz2wsx3edc!QAZ@WSX");
		route(reboot);
	}




}
