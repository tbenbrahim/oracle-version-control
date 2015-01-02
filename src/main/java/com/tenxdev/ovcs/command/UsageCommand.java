package com.tenxdev.ovcs.command;

import com.tenxdev.ovcs.UsageException;

public class UsageCommand implements Command {

	public void execute(String[] args) throws UsageException {
		throw new UsageException(String.format("%s\n\t%s\n\t%s\n\t%s\n",
				ConfigCommand.USAGE, InitCommand.USAGE, StartCommand.USAGE,
				EndCommand.USAGE));
	}
}
