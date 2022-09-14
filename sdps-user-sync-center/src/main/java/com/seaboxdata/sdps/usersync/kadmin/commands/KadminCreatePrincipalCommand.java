package com.seaboxdata.sdps.usersync.kadmin.commands;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommand;

public class KadminCreatePrincipalCommand implements KadminCommand {
    private final String principalName;
    private final String password;

    public KadminCreatePrincipalCommand(String principalName, String password) {
        this.principalName = principalName;
        this.password = password;
    }

    public String commandString() {
        return format("ank -pw %s %s", password, principalName);
    }

    public static List<KadminCommand> toAddPrincipalCommands(Map<String, String> principalsWithPassword) {
        return principalsWithPassword.entrySet().stream()
                .map(entry -> new KadminCreatePrincipalCommand(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
