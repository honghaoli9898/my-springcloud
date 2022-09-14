package com.seaboxdata.sdps.usersync.kadmin.commands;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommand;

public class KadminCreateKeytabCommand implements KadminCommand {
	private final String principalName;
	private final String keytabPath;

	public KadminCreateKeytabCommand(String keytabPath, String principalName) {
		this.principalName = principalName;
		this.keytabPath = keytabPath;
	}

	public String commandString() {
		return format("xst -k %s -norandkey %s", keytabPath, principalName);
	}

	public static List<KadminCommand> toAddPrincipalCommands(
			Map<String, String> principalsWithKeytab) {
		return principalsWithKeytab
				.entrySet()
				.stream()
				.map(entry -> new KadminCreateKeytabCommand(entry.getKey(),
						entry.getValue())).collect(Collectors.toList());
	}
}
