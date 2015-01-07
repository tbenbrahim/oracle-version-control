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

import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.TextProgressMonitor;

import com.tenxdev.ovcs.OvcsException;

/**
 * Abstract base class for commands that synchronize the database objects with
 * the local and remote git repository
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public abstract class AbstractSyncCommand extends AbstractChangeCommand {

	/**
	 * Commit all changes and push to remote repository
	 *
	 * @throws OvcsException
	 *             if changes could not be committed or pushed
	 */
	protected void commitAndPush() throws OvcsException {
		try {
			final FileRepository fileRepository = getRepoForCurrentDir();
			final Git git = new Git(fileRepository);
			final Status status = git.status().setProgressMonitor(new TextProgressMonitor()).call();
			if (!status.isClean()) {
				git.add().addFilepattern(".").call();
				git.commit().setMessage("initial synchronization").setAll(true).call();
				doPush(git);
			}
		} catch (final GitAPIException e) {
			throw new OvcsException("Unable to commit to git repo: " + e.getMessage(), e);
		}
	}

	/**
	 * Fetch the source of all schema objects from the database and writes it to
	 * the local repository
	 * 
	 * @param conn
	 *            the database connection to the managed schema
	 * @param workingDir
	 *            the working directory of the local git repository
	 * @throws OvcsException
	 *             if changes could not be fecthed from the database or written
	 *             to the local repository
	 */
	protected void writeSchemaObjects(final Connection conn, final Path workingDir) throws OvcsException {
		try {
			System.out.println("Fetching objects");
			try (CallableStatement stmt = conn
					.prepareCall("begin\n"
							+ "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'STORAGE',false);\n"
							+ "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'TABLESPACE',false);\n"
							+ "DBMS_METADATA.SET_TRANSFORM_PARAM(DBMS_METADATA.SESSION_TRANSFORM,'SEGMENT_ATTRIBUTES',false);\nend;")) {

				stmt.execute();
			}
			try (PreparedStatement stmt = conn
					.prepareStatement("select object_type, object_name, dbms_metadata.get_ddl(object_type, object_name) src "
							+ " from user_objects where object_type not like '% BODY' and object_name not like 'OVCS#'")) {
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						writeSchemaObject(workingDir, rset.getString("object_name"), rset.getString("src"), true);
					}
				}
			}
		} catch (final SQLException e) {
			throw new OvcsException("Unable to fetch schema objects: " + e.getMessage(), e);
		}
	}

}
