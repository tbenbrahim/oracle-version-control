package com.tenxdev.ovcs.command;

import java.util.Locale;

/*
 * Copyright 2015 Abed Tony BenBrahim <tony.benbrahim@10xdev.com> This file is
 * part of OVCS.
 *
 * OVCS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OVCS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OVCS. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Creates the appropriate command, based on the command line arguments
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public final class CommandFactory {

	/**
	 * gets the appropriate command, based on the command line arguments
	 *
	 * @param args
	 *            the command line arguments
	 * @return a command to handle the request
	 */
	public static Command getCommandForArguments(final String... args) {
		if (args.length == 0) {
			return new UsageCommand();
		}
		switch (args[0].toLowerCase(Locale.getDefault())) {
		case "config":
			return new ConfigCommand();
		case "init":
			return new InitCommand();
		case "start":
			return new StartCommand();
		case "status":
			return new StatusCommand();
		case "diff":
			return new DiffCommand();
		case "commit":
			return new CommitCommand();
		case "push":
			return new PushCommand();
		default:
			return new UsageCommand();
		}
	}

	private CommandFactory() {
	}

}
