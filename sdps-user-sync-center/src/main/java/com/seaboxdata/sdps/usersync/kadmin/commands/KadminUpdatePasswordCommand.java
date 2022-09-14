package com.seaboxdata.sdps.usersync.kadmin.commands;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommand;

public class KadminUpdatePasswordCommand implements KadminCommand {
    private final String principalName;
    private final String password;

	public KadminUpdatePasswordCommand(String principalName, String password) {
		this.principalName = principalName;
		this.password = password;
	}

	public String commandString() {
		return format("cpw -pw %s %s", password, principalName);
	}

	public static List<KadminCommand> toAddPrincipalCommands(
			Map<String, String> principalsWithKeytab) {
		return principalsWithKeytab
				.entrySet()
				.stream()
				.map(entry -> new KadminUpdatePasswordCommand(entry.getKey(),
						entry.getValue())).collect(Collectors.toList());
	}
}
