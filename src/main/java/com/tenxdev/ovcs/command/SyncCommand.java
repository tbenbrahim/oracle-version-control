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

package com.tenxdev.ovcs.command;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.TextProgressMonitor;

import com.tenxdev.ovcs.OvcsException;
import com.tenxdev.ovcs.UsageException;

/**
 * Command to synchronize the database objects with the local and remote git
 * repository
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class SyncCommand extends AbstractSyncCommand {

	/**
	 * command usage
	 */
	private static final String USAGE = "    ovcs sync";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final String... args) throws OvcsException {
		if (args.length != 1) {
			throw new UsageException(USAGE);
		}
		final FileRepository repository = getRepoForCurrentDir();
		try {
			final File workingDirectory = repository.getWorkTree();
			new Git(repository).pull().setProgressMonitor(new TextProgressMonitor()).call();
			try (Connection conn = getDbConnectionForRepo(repository)) {
				writeSchemaObjects(conn, workingDirectory.toPath());
				commitAndPush();
			} catch (final SQLException e) {
				throw new OvcsException("Unable to connect to database: " + e.getMessage(), e);
			}
		} catch (final GitAPIException e) {
			throw new OvcsException("Unable to synchronize with remote repository: " + e.getMessage(), e);
		} finally {
			repository.close();
		}
	}

}
