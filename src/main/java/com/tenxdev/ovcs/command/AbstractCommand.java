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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.PushResult;

import com.tenxdev.ovcs.OvcsException;

public abstract class AbstractCommand implements Command {

	protected FileRepository getRepoForCurrentDir() throws OvcsException {
		File workingDir = new File(System.getProperty("user.dir"));
		File gitDir = new File(workingDir, ".git");
		if (!gitDir.exists()) {
			throw new OvcsException(
					"The current directory is not a git repository");
		}
		try {
			FileRepository fileRepository = new FileRepository(gitDir);
			if (!fileRepository.getObjectDatabase().exists()) {
				throw new OvcsException(
						"The current directory is not a git repository");
			}
			return fileRepository;
		} catch (IOException e) {
			throw new OvcsException(
					"Could not open the current directory as a git repository");
		}
	}

	protected Connection getDbConnectionForRepo(FileRepository repository)
			throws OvcsException {
		FileBasedConfig config = repository.getConfig();
		String connectionString = config.getString("database", null,
				"connectionString");
		if (connectionString == null) {
			throw new OvcsException(
					"The current git repository is not an OVCS repository");
		}
		return getConnection(connectionString);
	}

	protected Connection getConnection(String connectionString)
			throws OvcsException {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e1) {
			throw new OvcsException(
					"Unable to find Oracle JDBC driver in class path");
		}
		try {
			return DriverManager.getConnection("jdbc:oracle:thin:"
					+ connectionString);
		} catch (SQLException e) {
			throw new OvcsException(String.format(
					"Unable to connect to %s: %s", connectionString,
					e.getMessage()));
		}
	}

	protected String getApplicationError(SQLException e) {
		String message = e.getMessage();
		int start = "ORA-20000: ".length();
		int end = message.indexOf('\n');
		return message.substring(start, end);
	}

	protected boolean isApplicationError(SQLException e) {
		return e.getMessage().startsWith("ORA-20000: ");
	}

	protected void writeSchemaObject(Path workingDirectory, 
			String objectName, String source, boolean feedback)
			throws OvcsException {
		Path filePath = workingDirectory.resolve(objectName + ".sql");
		if (feedback) {
			System.out.println("Writing " + objectName);
		}
		try (FileWriter writer = new FileWriter(filePath.toFile())) {
			writer.write(source);
		} catch (IOException e) {
			throw new OvcsException(String.format("Unable to write file %s",
					filePath.toString()), e);
		}
	}
	
	protected void removeSchemaObject(Path workingDirectory, String objectName, boolean feedback) throws OvcsException {
		File file = workingDirectory.resolve(objectName + ".sql").toFile();
		if (file.exists() && !file.delete()){
			throw new OvcsException(String.format("Unabled to delete \"%s\"", file.getPath()));
		}
		if (feedback){
			System.out.println(String.format("Removing %s ", objectName));
		}
	}
	
	protected void doPush(Git git) throws GitAPIException{
		Iterable<PushResult> pushResults = git.push()
				.setRemote("origin").add("master")
				.setProgressMonitor(new TextProgressMonitor()).call();
		for (PushResult pushResult : pushResults) {
			String messages = pushResult.getMessages();
			if (!"".equals(messages)) {
				System.out.println();
			}
		}
	}


}
