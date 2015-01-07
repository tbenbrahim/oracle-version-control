package com.tenxdev.ovcs.command;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.jgit.internal.storage.file.FileRepository;

import com.tenxdev.ovcs.OvcsException;
import com.tenxdev.ovcs.UsageException;

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
 * implements the start command
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class StartCommand extends AbstractOvcsCommand {

	/**
	 * command usage
	 */
	private static final String USAGE = "    ovcs start";

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void execute(final String... args) throws OvcsException {
		if (args.length != 1) {
			throw new UsageException(USAGE);
		}
		final FileRepository repository = getRepoForCurrentDir();
		try (Connection conn = getDbConnectionForRepo(repository)) {
			try (CallableStatement stmt = conn.prepareCall("begin ovcs.handler.start_session; end;")) {
				stmt.execute();
				System.out.println("Session started");
			}
		} catch (final SQLException e) {
			if (isApplicationError(e)) {
				throw new OvcsException(getApplicationError(e));
			}
			throw new OvcsException("Unexpected database error: " + e.getMessage(), e);
		}
	}

}
