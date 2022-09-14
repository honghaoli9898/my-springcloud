package com.seaboxdata.sdps.usersync.kadmin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.seaboxdata.sdps.common.core.utils.RemoteShellExecutorUtil;

@Slf4j
public class KadminCommandsRunner {
	private String principal;
	private String keytab;
	private String krb5ConfigPath;
	private String ip;
	private Integer port;
	private String username;
	private String password;

	public KadminCommandsRunner(String principal, String keytab,
			String krb5ConfigPath) {
		this.principal = principal;
		this.keytab = keytab;
		this.krb5ConfigPath = krb5ConfigPath;
	}

	public KadminCommandsRunner(String principal, String keytab, String ip,
			String username, String password, Integer port) {
		this.principal = principal;
		this.keytab = keytab;
		this.ip = ip;
		this.password = password;
		this.port = port;
		this.username = username;
	}

	public KadminCommandResult runCommand(KadminCommand command, int mode)
			throws Exception {
		try (KadminProccess kAdmin = new KadminProccess(command, mode)) {
			int status = -999;
			switch (mode) {
			case 0:
				status = kAdmin.waitFor();
				break;
			case 1:
				status = kAdmin.status;
				break;
			default:
				break;
			}
			String output = kAdmin.getOutPutStr();
			return KadminCommandResultParser.parse(status, output);
		}
	}

	private class KadminProccess implements AutoCloseable {
		private Process kAdmin;
		private Integer mode;
		private RemoteShellExecutorUtil remoteShellExecutorUtil;
		private Integer status;

		public KadminProccess(KadminCommand command, Integer mode)
				throws Exception {
			this.mode = mode;
			String kadminShellStr = "kadmin";
			if(command.commandString().contains("-norandkey")){
				kadminShellStr = kadminShellStr.concat(".local");
			}
			switch (mode) {
			case 0:
				ProcessBuilder processBuilder = new ProcessBuilder(kadminShellStr,
						"-p", principal, "-kt", keytab, "-q",
						command.commandString())
						.directory(new File("/usr/bin")).redirectErrorStream(
								true);
				processBuilder.environment().put("KRB5_CONFIG", krb5ConfigPath);
				kAdmin = processBuilder.start();

				log.info("Kadmin process PID: {} has started", getPid(kAdmin));
				break;
			case 1:
				remoteShellExecutorUtil = new RemoteShellExecutorUtil(ip,
						username, password, port);
				StringBuilder sb = new StringBuilder();
				sb.append(kadminShellStr);
				sb.append(" -p ");
				sb.append(principal);
				sb.append(" -kt ");
				sb.append(keytab);
				sb.append(" -q '");
				sb.append(command.commandString());
				sb.append("'");
				status = remoteShellExecutorUtil.commonExec(sb.toString());
				break;
			default:
				break;
			}
		}

		public int waitFor() {
			Integer status = null;
			switch (mode) {
			case 0:
				try {
					status = kAdmin.waitFor();
				} catch (InterruptedException e) {
					status = kAdmin.exitValue();
				}
				break;
			case 1:
				status = this.status;
				break;
			default:
				break;
			}
			return status;

		}

		public String getOutPutStr() {
			switch (mode) {
			case 0:
				return new BufferedReader(new InputStreamReader(
						kAdmin.getInputStream())).lines().collect(
						Collectors.joining("\n"));
			case 1:
				return remoteShellExecutorUtil.getOutStrStringBuffer()
						.toString();
			default:
				break;
			}
			return null;

		}

		@Override
		public void close() throws IOException {
			switch (mode) {
			case 0:
				if (!destroy()) {
					kAdmin.destroyForcibly();
				}
				break;
			default:
				break;
			}

		}

		private boolean destroy() {
			kAdmin.destroy();
			try {
				return kAdmin.waitFor(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				return false;
			}
		}

		private Long getPid(Process process) {
			Long pid = 0L;
			try {
				String name = process.getClass().getName();
				if ("java.lang.UNIXProcess".equals(name)
						|| "java.lang.ProcessImpl".equals(name)) {
					Field field = process.getClass().getDeclaredField("pid");
					field.setAccessible(true);
					pid = field.getLong(process);
					field.setAccessible(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return pid;

		}

	}

}
