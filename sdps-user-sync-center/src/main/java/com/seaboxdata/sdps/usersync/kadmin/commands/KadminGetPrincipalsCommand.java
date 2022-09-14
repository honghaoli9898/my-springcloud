package com.seaboxdata.sdps.usersync.kadmin.commands;

import static java.lang.String.format;

import com.seaboxdata.sdps.usersync.kadmin.KadminCommand;


public class KadminGetPrincipalsCommand implements KadminCommand {
    private final String expression;

    public KadminGetPrincipalsCommand(String expression) {
        this.expression = expression;
    }

    public String commandString() {
        return format("getprincs %s", expression);
    }
}
