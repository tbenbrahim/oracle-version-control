package com.tenxdev.ovcs.command;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;

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
 * Command to initialize the ovcs local git repository
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class InitCommand extends AbstractSyncCommand {

	/**
	 * usage for the init command
	 */
	private static final String USAGE = "    ovcs init connnection-string remote-git-server";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final String... args) throws OvcsException {
		if (args.length != 3) {
			throw new UsageException(USAGE);
		}
		final String connectionString = args[1];
		final String gitServer = args[2];
		final Path workingDir = getWorkingDirectory();
		initGitRepo(workingDir, gitServer, connectionString);
		try (Connection conn = getConnection(connectionString)) {
			writeSchemaObjects(conn, workingDir);
		} catch (final SQLException e) {
			throw new OvcsException("Unable to connect to database: " + e.getMessage(), e);
		}
		commitAndPush();
	}

	private Path getWorkingDirectory() throws OvcsException {
		final Path workingDir = Paths.get(System.getProperty("user.dir"));
		if (!isEmpty(workingDir)) {
			throw new OvcsException(String.format("Working directory '%s' is not empty.", workingDir.toString()));
		}
		return workingDir;
	}

	private void initGitRepo(final Path workingDir, final String remoteUri, final String connectionString)
			throws OvcsException {
		try {
			System.out.println("Cloning git repo");
			Git.cloneRepository().setDirectory(workingDir.toFile()).setCloneAllBranches(true).setRemote("origin")
					.setURI(remoteUri).setProgressMonitor(new TextProgressMonitor()).call();
			final Git git = Git.open(workingDir.toFile());
			final StoredConfig config = git.getRepository().getConfig();
			config.setString("database", null, "connectionString", connectionString);
			config.save();
			git.checkout().setName("master").setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
		} catch (GitAPIException | IOException e) {
			throw new OvcsException("Unable to initialize Git repo: " + e.getMessage(), e);
		}
	}

	private boolean isEmpty(final Path directory) throws OvcsException {
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		} catch (final IOException e) {
			throw new OvcsException(String.format("Unable to access working directory '%s'", directory.toString()), e);
		}
	}

}
