package com.seaboxdata.sdps.usersync.test;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommandResult;
import com.seaboxdata.sdps.usersync.kadmin.KadminCommandsRunner;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminCreateKeytabCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminCreatePrincipalCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminDeletePrincipalCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminGetPrincipalsCommand;

public class SshTest {
	private static final String PRINCIPAL = "admin/admin";
	private static final String keytab = "/etc/security/keytabs/admin.keytab";
	private static KadminCommandsRunner kadminCommandsRunner;
	private static String ip = "10.1.3.24";
	private static String username = "root";
	private static String password = "1qaz2wsx3edc!QAZ@WSX";

	public static void main(String[] args) throws Exception {
		setup();
//		KadminCommandResult result = kadminCommandsRunner.runCommand(
//				new KadminCreateKeytabCommand("/test0427.keytab","test0427@HADOOP.COM"), 1);
//		KadminCommandResult result = kadminCommandsRunner.runCommand(
//				new KadminGetPrincipalsCommand("test0427"), 1);
//		KadminCommandResult result = kadminCommandsRunner.runCommand(
//				new KadminDeletePrincipalCommand("test0426"), 1);
		KadminCommandResult result = kadminCommandsRunner.runCommand(
		new KadminCreatePrincipalCommand("test0427","admin1234"), 1);
		System.out.println(result.getExitCode());
		System.out.println(result.getOutput());
		
	}

	public static void setup() {
		kadminCommandsRunner = new KadminCommandsRunner(PRINCIPAL, keytab, ip,
				username, password, 22);
	}

}