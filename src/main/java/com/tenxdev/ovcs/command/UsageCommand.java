package com.tenxdev.ovcs.command;

import com.tenxdev.ovcs.UsageException;

public class UsageCommand implements Command {

	public void execute(String[] args) throws UsageException {
		throw new UsageException(
				""
						+ "init            initialize a directory as an ovcs local repo.\n"
						+ "\tstart           start an ovcs session.\n"
						+ "\tstatus          displays changes since start of session.\n"
						+ "\tcommit          end an ovcs session, commits changes.\n"
						+" \tpush            push changes to remote repo.\n");
	}
}
