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

import com.tenxdev.ovcs.UsageException;

/**
 * Implements the usage command
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class UsageCommand implements Command {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final String... args) throws UsageException {
		throw new UsageException("    config          configure ovcs\n"
				+ "    init            initialize a directory as an ovcs local repo.\n"
				+ "    start           start an ovcs session.\n"
				+ "    status          displays changed objects since start of session.\n"
				+ "    diff            show detailed changes between database and last commit\n"
				+ "    commit          end a session, commit and send changes to remote repo.\n"
				+ "    push            send changes to remote repo, if push failed during commit.\n");
	}
}
