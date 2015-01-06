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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.PushResult;

import com.tenxdev.ovcs.OracleDriverLoader;
import com.tenxdev.ovcs.OvcsException;

/**
 * Abstract base class for all ovcs commands that need to access the local git
 * repository
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public abstract class AbstractCommand implements Command {

	/**
	 * Default constructor
	 */
	protected AbstractCommand() {
		super();
	}

	/**
	 * Push all committed changes to the remote repository
	 *
	 * @param git
	 *            the local git repository
	 * @throws GitAPIException
	 *             if the push operation failed
	 */
	protected void doPush(final Git git) throws GitAPIException {
		final Iterable<PushResult> pushResults = git.push().setRemote("origin").add("master")
				.setProgressMonitor(new TextProgressMonitor()).call();
		for (final PushResult pushResult : pushResults) {
			final String messages = pushResult.getMessages();
			if (!"".equals(messages)) {
				System.out.println();
			}
		}
	}

	/**
	 * Extract error message from ORA-20000 user defined messages
	 *
	 * @param exception
	 *            the sql exception
	 * @return the error message
	 */
	protected String getApplicationError(final SQLException exception) {
		final String message = exception.getMessage();
		final int start = "ORA-20000: ".length();
		final int end = message.indexOf('\n');
		return message.substring(start, end);
	}

	/**
	 * get an Oracle DB connection
	 *
	 * @param connectionString
	 *            the Oracle DB connection string, in format
	 *            user/password@host:port:sid
	 * @return a JDBC connection to the database schema being tracked
	 * @throws OvcsException
	 *             if the connection could not be obtained
	 */
	protected Connection getConnection(final String connectionString) throws OvcsException {
		final Driver driver = new OracleDriverLoader().load();
		try {

			return driver.connect("jdbc:oracle:thin:" + connectionString, new Properties());
		} catch (final SQLException e) {
			throw new OvcsException(String.format("Unable to connect to %s: %s", connectionString, e.getMessage()), e);
		}
	}

	/**
	 * gets an Oracle DB connection, based on connection string stored in the
	 * local git repo's settings
	 *
	 * @param repository
	 *            the local git repository
	 * @return a JDBC connection to the database schema being tracked
	 * @throws OvcsException
	 */
	protected Connection getDbConnectionForRepo(final FileRepository repository) throws OvcsException {
		final FileBasedConfig config = repository.getConfig();
		final String connectionString = config.getString("database", null, "connectionString");
		if (connectionString == null) {
			throw new OvcsException("The current git repository is not an OVCS repository");
		}
		return getConnection(connectionString);
	}

	/**
	 * checks if the current directory is a git repository and returns a
	 * repository object if it is
	 *
	 * @return a repository object
	 * @throws OvcsException
	 *             if the current directory is not a git repository
	 */
	protected FileRepository getRepoForCurrentDir() throws OvcsException {
		final File workingDir = new File(System.getProperty("user.dir"));
		final File gitDir = new File(workingDir, ".git");
		if (!gitDir.exists()) {
			throw new OvcsException("The current directory is not a git repository");
		}
		try {
			final FileRepository fileRepository = new FileRepository(gitDir);
			if (!fileRepository.getObjectDatabase().exists()) {
				throw new OvcsException("The current directory is not a git repository");
			}
			return fileRepository;
		} catch (final IOException e) {
			throw new OvcsException("Could not open the current directory as a git repository", e);
		}
	}

	/**
	 * Determines if a SQL exception is an Oracle user defined application
	 * exception
	 *
	 * @param exception
	 *            the SQL exception
	 * @return true if the SQL exception is an Oracle user defined application,
	 *         false otherwise
	 */
	protected boolean isApplicationError(final SQLException exception) {
		return exception.getMessage().startsWith("ORA-20000: ");
	}

	/**
	 * Remove a schema object from thw working directory
	 *
	 * @param workingDirectory
	 *            the path to the working directory
	 * @param objectName
	 *            the name of the database object to remove
	 * @param feedback
	 *            true to display a status message when removing an object,
	 *            false otherwise
	 * @throws OvcsException
	 *             if the file corresponding to the object cannot be removed
	 */
	protected void removeSchemaObject(final Path workingDirectory, final String objectName, final boolean feedback)
			throws OvcsException {
		final File file = workingDirectory.resolve(objectName + ".sql").toFile();
		if (file.exists() && !file.delete()) {
			throw new OvcsException(String.format("Unabled to delete \"%s\"", file.getPath()));
		}
		if (feedback) {
			System.out.println(String.format("Removing %s ", objectName));
		}
	}

	/**
	 * Write a database schema object's source to the working directory
	 *
	 * @param workingDirectory
	 *            the path to the working directory
	 * @param objectName
	 *            the name of the database object to write
	 * @param source
	 *            the SQL source corresponding to the creation of the database
	 *            object
	 * @param feedback
	 *            true to display a status message when writing an object, false
	 *            otherwise
	 * @throws OvcsException
	 *             if the object could not be written
	 */
	protected void writeSchemaObject(final Path workingDirectory, final String objectName, final String source,
			final boolean feedback) throws OvcsException {
		final Path filePath = workingDirectory.resolve(objectName + ".sql");
		if (feedback) {
			System.out.println("Writing " + objectName);
		}
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
			writer.write(source);
		} catch (final IOException e) {
			throw new OvcsException(String.format("Unable to write file %s", filePath.toString()), e);
		}
	}

}
