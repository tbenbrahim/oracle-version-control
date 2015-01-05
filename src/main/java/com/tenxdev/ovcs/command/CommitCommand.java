package com.tenxdev.ovcs.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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
	public void execute(String[] args) throws OvcsException {
		System.out.println("Fetching changes from database...");
		FileRepository repository = getRepoForCurrentDir();
		try {
			try (Connection conn = getDbConnectionForRepo(repository)) {
				try (CallableStatement stmt = conn
						.prepareCall("begin ovcs.handler.end_session; end;")) {
					stmt.execute();
				} catch (SQLException e) {
					if (isApplicationError(e)) {
						throw new OvcsException("Unable to commit: "
								+ getApplicationError(e), e);
					}
					throw new OvcsException("Unable to commit: "
							+ e.getMessage(), e);
				}
				List<ChangeEntry> changes = writeChanges(repository);
				Git git = new Git(repository);
				try {
					for (ChangeEntry changeEntry : changes) {
						git.add()
								.addFilepattern(
										changeEntry.getName().toUpperCase()
												+ ".sql").call();
					}
					git.commit().setMessage(getCommitMessage()).setAll(true)
							.call();
				} catch (GitAPIException e) {
					throw new OvcsException("Unable to commit: "
							+ e.getMessage(), e);
				}
				try {
					conn.commit();
				} catch (SQLException e) {
					System.out
							.println("Unexpected error while committing database changes, you may have to call "
									+ "ovcs.handler.end_session from your schema and commit to bring the database into a consistent state.");
				}
			} catch (SQLException e1) {
				System.out
						.println("Unexpected error while closing database connection, you may have to call "
						+ "ovcs.handler.end_session from your schema and commit to bring the database into a consistent state.");
			}
			System.out
					.println("All changes committed, ovcs push to send to remote repository.");

		} finally {
			repository.close();
		}

	}

	private String getCommitMessage() throws OvcsException {
		System.out
				.println("Enter commit message, enter a single . at the start of a line to end:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder stringBuilder = new StringBuilder();

		while (true) {
			try {
				System.out.print("> ");
				String line = br.readLine();
				if (line == null || ".".equals(line.trim())) {
					break;
				}
				stringBuilder.append(line);
			} catch (IOException ioe) {
				throw new OvcsException("Unable to read commit message");
			}
		}
		return stringBuilder.toString();
	}

}
