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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import com.tenxdev.ovcs.OvcsException;
import com.tenxdev.ovcs.model.ChangeEntry;

/**
 * implementation of the commit command
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class CommitCommand extends AbstractOvcsCommand {

	/**
	 * command usage
	 */
	private static final String USAGE = "    ovcs end schema";

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void execute(final String... args) throws OvcsException {
		if (args.length != 1) {
			throw new OvcsException(USAGE);
		}
		System.out.println("Fetching changes from database...");
		final FileRepository repository = getRepoForCurrentDir();
		try {
			try (Connection conn = getDbConnectionForRepo(repository)) {
				final Git git = new Git(repository);
				final List<ChangeEntry> changes = writeChanges(repository);
				if (changes.isEmpty()) {
					System.out.println("No changes have been made, ending session");
				} else {
					try {
						for (final ChangeEntry changeEntry : changes) {
							git.add().addFilepattern(changeEntry.getName().toUpperCase(Locale.getDefault()) + ".sql")
									.call();
						}
						git.commit().setMessage(getCommitMessage()).setAll(true).call();
					} catch (final GitAPIException e) {
						throw new OvcsException("Unable to commit: " + e.getMessage(), e);
					}
				}
				try (CallableStatement stmt = conn.prepareCall("begin ovcs.handler.end_session; end;")) {
					stmt.execute();
					conn.commit();
				} catch (final SQLException e) {
					throw new OvcsException(
							"Unexpected error while committing database changes, you may have to call "
									+ "ovcs.handler.end_session from your schema and commit to bring the database into a consistent state.",
							e);
				}
				if (!changes.isEmpty()) {
					try {
						doPush(git);
						System.out.println("All changes committed and sent to remote repository.");
					} catch (final GitAPIException e) {
						throw new OvcsException(
								String.format(
										"All changes have been committed, but were not sent to the remote repository%n"
												+ "Please run the ovcs push command to retry sending to the remote repository%nError: %s",
										e.getMessage()), e);
					}
				}
			} catch (final SQLException e) {
				throw new OvcsException(
						"Unexpected error while closing database connection, you may have to call "
								+ "ovcs.handler.end_session from your schema and commit to bring the database into a consistent state.",
						e);
			}
		} finally {
			repository.close();
		}

	}

	private String getCommitMessage() throws OvcsException {
		System.out.println("Enter commit message, enter a single . at the start of a line to end:");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
		final StringBuilder stringBuilder = new StringBuilder();

		while (true) {
			try {
				System.out.print("> ");
				final String line = reader.readLine();
				if (line == null || ".".equals(line.trim())) {
					break;
				}
				stringBuilder.append(line);
			} catch (final IOException e) {
				throw new OvcsException("Unable to read commit message", e);
			}
		}
		return stringBuilder.toString();
	}

}
