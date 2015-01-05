package com.tenxdev.ovcs.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
public class InitCommand extends AbstractCommand {

	public static final String USAGE = "ovcs init connnection-string remote-git-server";

	@Override
	public void execute(String[] args) throws OvcsException {
		if (args.length != 3) {
			throw new UsageException(USAGE);
		}
		String connectionString = args[1];
		String gitServer = args[2];
		Path workingDir = getWorkingDirectory();
		initGitRepo(workingDir, gitServer, connectionString);
		try (Connection conn = getConnection(connectionString)) {
			writeSchemaObjects(conn, workingDir);
		} catch (SQLException e) {
			throw new OvcsException("Unable to connect to database: "
					+ e.getMessage(), e);
		}
		commitAndPush(workingDir);
	}

	private Path getWorkingDirectory() throws OvcsException {
		Path workingDir = Paths.get(System.getProperty("user.dir"));
		if (!isEmpty(workingDir)) {
			throw new OvcsException(String.format(
					"Working directory '%s' is not empty.",
					workingDir.toString()));
		}
		return workingDir;
	}

	private void commitAndPush(Path workingDir) throws OvcsException {
		try {
			Git git = new Git(new FileRepositoryBuilder()
					.setGitDir(new File(workingDir.toFile(), ".git"))
					.setMustExist(true).build());
			Status status = git.status()
					.setProgressMonitor(new TextProgressMonitor()).call();
			if (!status.isClean()) {
				git.add().addFilepattern(".").call();
				git.commit().setMessage("initial synchronization").call();
				doPush(git);
			}
		} catch (IOException | GitAPIException e) {
			throw new OvcsException("Unable to commit to git repo: "
					+ e.getMessage(), e);
		}
	}

	private void initGitRepo(Path workingDir, String remoteUri, String connectionString)
			throws OvcsException {
		try {
			System.out.println("Cloning git repo");
			Git.cloneRepository().setDirectory(workingDir.toFile())
					.setCloneAllBranches(true).setRemote("origin")
					.setURI(remoteUri)
					.setProgressMonitor(new TextProgressMonitor()).call();
			Git git = Git.open(workingDir.toFile());
			StoredConfig config = git.getRepository().getConfig();
			config.setString("database", null, "connectionString", connectionString);
			config.save();
			git.checkout().setName("master")
					.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
			throw new OvcsException("Unable to initialize Git repo: "
					+ e.getMessage(), e);
		}
	}

	private void writeSchemaObjects(Connection conn, Path workingDir) throws OvcsException {
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
						writeSchemaObject(workingDir,
								rset.getString("object_name"),
								rset.getString("src"), true);
					}
				}
			}
		} catch (SQLException e) {
			throw new OvcsException("Unable to fetch schema objects: "
					+ e.getMessage(), e);
		}
	}

	private boolean isEmpty(Path directory) throws OvcsException {
		try (DirectoryStream<Path> dirStream = Files
				.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		} catch (IOException e) {
			throw new OvcsException(String.format(
					"Unable to access working directory '%s'",
					directory.toString()));
		}
	}

}
