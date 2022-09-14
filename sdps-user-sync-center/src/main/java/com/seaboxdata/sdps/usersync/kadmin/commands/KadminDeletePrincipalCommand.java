package com.seaboxdata.sdps.usersync.kadmin.commands;

import static java.lang.String.format;

import java.util.List;
import java.util.stream.Collectors;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommand;


public class KadminDeletePrincipalCommand implements KadminCommand {
    private final String principalName;

    public KadminDeletePrincipalCommand(String principalName) {
        this.principalName = principalName;
    }

    public String commandString() {
        return format("delprinc -force %s", principalName);
    }

    public static List<KadminCommand> toDeletePrincipalCommands(List<String> principalNames) {
        return principalNames.stream()
                .map(KadminDeletePrincipalCommand::new)
                .collect(Collectors.toList());
    }
}
