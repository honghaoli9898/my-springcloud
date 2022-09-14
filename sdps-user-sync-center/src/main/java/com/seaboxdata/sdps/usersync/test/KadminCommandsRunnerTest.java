package com.seaboxdata.sdps.usersync.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommandResult;
import com.seaboxdata.sdps.usersync.kadmin.KadminCommandsRunner;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminCreatePrincipalCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminDeletePrincipalCommand;
import com.seaboxdata.sdps.usersync.kadmin.commands.KadminGetPrincipalsCommand;

public class KadminCommandsRunnerTest {
	private static final String REALM = "HADOOP.COM";
	private static final String PRINCIPAL = "admin/admin";
	private static KadminCommandsRunner kadminCommandsRunner;

	public static void main(String[] args) throws Exception {
		setup();
		runCommand_getPrincipalsCommand();
	}

	public static void setup() {
		kadminCommandsRunner = new KadminCommandsRunner(
				PRINCIPAL + "@" + REALM, "E:/kerberos/admin.keytab",
				"E:/kerberos/krb5.conf");
	}

	public static void runCommand_createPrincipalCommand() throws Exception {
		createPrincipal("test", REALM);
	}

	public static void runCommand_getPrincipalsCommand() throws Exception {
		getPrincipals("test0426", REALM);
		System.out.println("========");
	}

	public static void runCommand_deletePrincipalCommand() throws Exception {
		deletePrincipal("test", REALM);
	}

	public static void runCommand_100() throws Exception {
		for (int i = 0; i < 100; i++) {
			String principalName = "test_" + i;
			createPrincipal(principalName, REALM);
			getPrincipals(principalName, REALM);
			deletePrincipal(principalName, REALM);
		}
	}

	private static void createPrincipal(String principalName, String realm)
			throws Exception {
		KadminCommandResult result = kadminCommandsRunner.runCommand(
				new KadminCreatePrincipalCommand(principalName, "password"), 0);
		assertEquals(result.getExitCode(), 0);
		assertEquals(result.getOutput(), "WARNING: no policy specified for "
				+ principalName + "@" + realm + "; defaulting to no policy\n"
				+ "Authenticating as principal " + PRINCIPAL + "@" + REALM
				+ " with keytab /etc/security/keytabs/krb5.keytab.\n"
				+ "Principal \"" + principalName + "@" + realm + "\" created.");
	}

	private static void getPrincipals(String principalName, String realm)
			throws Exception {
		KadminCommandResult result = kadminCommandsRunner.runCommand(
				new KadminGetPrincipalsCommand(""), 0);
		assertEquals(result.getExitCode(), 0);
		assertTrue(result.getOutput().contains(principalName + "@" + realm));
	}

	private static void deletePrincipal(String principalName, String realm)
			throws Exception {
		KadminCommandResult result = kadminCommandsRunner.runCommand(
				new KadminDeletePrincipalCommand(principalName), 0);
		assertEquals(result.getExitCode(), 0);
		assertEquals(
				result.getOutput(),
				"Authenticating as principal "
						+ PRINCIPAL
						+ "@"
						+ REALM
						+ " with keytab /etc/security/keytabs/krb5.keytab.\n"
						+ "Principal \""
						+ principalName
						+ "@"
						+ realm
						+ "\" deleted.\n"
						+ "Make sure that you have removed this principal from all ACLs before reusing.");
	}
}