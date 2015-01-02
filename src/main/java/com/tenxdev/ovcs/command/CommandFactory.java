package com.tenxdev.ovcs.command;
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
public final class CommandFactory {

	private CommandFactory(){}
	
	public static Command parseArgs(String[] args){
		if (args.length==0){
			return new UsageCommand();
		}
		switch(args[0]){
		case "config":
			return new ConfigCommand();
		case "init":
			return new InitCommand();
		case "start":
			return new StartCommand();
		case "end":
			return new EndCommand();
		default:
			return new UsageCommand();
		}
	}
	
}
