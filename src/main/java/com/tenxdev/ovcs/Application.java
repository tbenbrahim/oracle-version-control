package com.tenxdev.ovcs;
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
import com.tenxdev.ovcs.command.CommandFactory;

public class Application {

	public static void main(String[] args) {
		try {
			CommandFactory.parseArgs(args).execute(args);
			System.exit(0);
		} catch (OvcsException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

}
