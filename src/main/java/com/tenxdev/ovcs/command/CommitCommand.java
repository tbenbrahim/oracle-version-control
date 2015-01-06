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
public class CommitCommand extends AbstractOvcsCommand {

	public static final String USAGE = "ovcs end schema";

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void execute(final String... args) throws OvcsException {
		System.out.println("Fetching changes from database...");
		final FileRepository repository = getRepoForCurrentDir();
		try {
			try (Connection conn = getDbConnectionForRepo(repository)) {
				try (CallableStatement stmt = conn.prepareCall("begin ovcs.handler.end_session; end;")) {
					stmt.execute();
				} catch (final SQLException e) {
					if (isApplicationError(e)) {
						throw new OvcsException("Unable to commit: " + getApplicationError(e), e);
					}
					throw new OvcsException("Unable to commit: " + e.getMessage(), e);
				}
				final List<ChangeEntry> changes = writeChanges(repository);
				final Git git = new Git(repository);
				try {
					for (final ChangeEntry changeEntry : changes) {
						git.add().addFilepattern(changeEntry.getName().toUpperCase(Locale.getDefault()) + ".sql")
								.call();
					}
					git.commit().setMessage(getCommitMessage()).setAll(true).call();
				} catch (final GitAPIException e) {
					throw new OvcsException("Unable to commit: " + e.getMessage(), e);
				}
				try {
					conn.commit();
				} catch (final SQLException e) {
					throw new OvcsException(
							"Unexpected error while committing database changes, you may have to call "
									+ "ovcs.handler.end_session from your schema and commit to bring the database into a consistent state.",
							e);
				}
				try {
					doPush(git);
				} catch (final GitAPIException e) {
					throw new OvcsException(
							String.format(
									"All changes have been committed, but were not sent to the remote repository%n"
											+ "Please run the ovcs push command to retry sending to the remote repository%nError: %s",
									e.getMessage()), e);
				}
			} catch (final SQLException e) {
				throw new OvcsException(
						"Unexpected error while closing database connection, you may have to call "
								+ "ovcs.handler.end_session from your schema and commit to bring the database into a consistent state.",
						e);
			}
			System.out.println("All changes committed and sent to remote repository.");

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
			} catch (final IOException ioe) {
				throw new OvcsException("Unable to read commit message");
			}
		}
		return stringBuilder.toString();
	}

}
