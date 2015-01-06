package com.tenxdev.ovcs.command;

import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
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

/**
 * Abstract base class for all ovcs commands that modify the local git
 * repository
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public abstract class AbstractOvcsCommand extends AbstractCommand {

	/**
	 * query run to retrieve schema changes
	 */
	private static final String CHANGES_QUERY = "select user_objects.object_name, action, original_source, "
			+ " dbms_metadata.get_ddl(user_objects.object_type, user_objects.object_name) current_source"
			+ " from ovcs.locked_objects, user_objects" + " where  osuser=lower(sys_context('USERENV', 'OS_USER'))"
			+ " and schema_name=upper(sys_context('USERENV', 'SESSION_USER'))"
			+ " and user_objects.OBJECT_NAME=locked_objects.object_name"
			+ " and user_objects.OBJECT_TYPE=locked_objects.object_type" + " union all"
			+ " select object_name, action, original_source,  null current_source " + " from ovcs.locked_objects"
			+ " where  osuser=lower(sys_context('USERENV', 'OS_USER'))"
			+ " and schema_name=upper(sys_context('USERENV', 'SESSION_USER'))" + " and action='DROP'";

	/**
	 * Default constructor
	 */
	protected AbstractOvcsCommand() {
		super();
	}

	private void showModifiedWarning(final Set<String> list) {
		for (final String filename : list) {
			System.out.println(String.format("Warning: it apppears that %s was modified outside ovcs.", filename));
		}
	}

	private void warnIfNotClean(final FileRepository repo) throws OvcsException {
		final Git git = new Git(repo);
		try {
			final Status status = git.status().call();
			if (!status.isClean()) {
				showModifiedWarning(status.getConflicting());
				showModifiedWarning(status.getModified());
				showModifiedWarning(status.getUntrackedFolders());
			}
		} catch (NoWorkTreeException | GitAPIException e) {
			throw new OvcsException("Unable to query git status: " + e.getMessage(), e);
		}
	}

	private void writeChangesFirstPass(final ResultSet rset, final Path workingDirectory) throws SQLException,
			OvcsException {
		while (rset.next()) {
			final String name = rset.getString("object_name");
			final String source = rset.getString("original_source");
			if (!rset.wasNull()) {
				writeSchemaObject(workingDirectory, name, source, false);
			}
		}
	}

	private List<ChangeEntry> writeChangesSecondPass(final ResultSet rset, final Path workingDirectory)
			throws SQLException, OvcsException {
		final List<ChangeEntry> changes = new ArrayList<>();
		while (rset.next()) {
			final String name = rset.getString("object_name");
			final String action = rset.getString("action");
			final String source = rset.getString("current_source");
			if ("DROP".equals(action)) {
				removeSchemaObject(workingDirectory, name, true);
				changes.add(new ChangeEntry(name, true));
			} else {
				writeSchemaObject(workingDirectory, name, source, true);
				changes.add(new ChangeEntry(name, false));
			}
		}
		return changes;
	}

	/**
	 * Fetch all changed objects from database, write original source to disk,
	 * warn if original is different from last commit, then write all changed
	 * source to disk.
	 *
	 * @param repository
	 *            the local git repository
	 * @return a list of changed files
	 * @throws OvcsException
	 *             for errors during processing
	 */
	protected List<ChangeEntry> writeChanges(final FileRepository repository) throws OvcsException {
		final Path workingDirectory = repository.getWorkTree().toPath();
		try (Connection conn = getDbConnectionForRepo(repository)) {
			try (CallableStatement stmt = conn
					.prepareCall("begin\n"
							+ "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'STORAGE',false);\n"
							+ "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'TABLESPACE',false);\n"
							+ "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'SEGMENT_ATTRIBUTES',false);\nend;")) {

				stmt.execute();
			}
			try (PreparedStatement stmt = conn.prepareStatement(CHANGES_QUERY)) {
				try (ResultSet rset = stmt.executeQuery()) {
					writeChangesFirstPass(rset, workingDirectory);
					warnIfNotClean(repository);
				}
			}
			try (PreparedStatement stmt = conn.prepareStatement(CHANGES_QUERY)) {
				try (ResultSet rset = stmt.executeQuery()) {
					return writeChangesSecondPass(rset, workingDirectory);
				}
			}
		} catch (final SQLException e) {
			throw new OvcsException("Unable to query database: " + e.getMessage(), e);
		}
	}

}
