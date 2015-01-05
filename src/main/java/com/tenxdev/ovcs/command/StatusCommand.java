package com.tenxdev.ovcs.command;

import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import com.tenxdev.ovcs.OvcsException;

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

public class StatusCommand extends AbstractOvcsCommand {

	@Override
	public void execute(String[] args) throws OvcsException {
		System.out.println("Fetching changes from database...");
		FileRepository repository = getRepoForCurrentDir();
		try {
			writeChanges(repository);
			displayChanges(repository);
		} finally {
			repository.close();
		}
	}

	private void displayChanges(FileRepository repository) throws OvcsException {
		try {
			Status status = new Git(repository).status().call();
			if (status.isClean()) {
				System.out.println("No changes.");
			} else {
				displayChanges("Added", status.getUntracked());
				displayChanges("Modified", status.getModified());
				displayChanges("Removed", status.getMissing());
			}
		} catch (NoWorkTreeException | GitAPIException e) {
			throw new OvcsException("Unable to query git status: "
					+ e.getMessage(), e);
		}
	}

	private void displayChanges(String title, Set<String> filenames) {
		if (!filenames.isEmpty()) {
			System.out.println(title + ":");
			for (String filename : filenames) {
				System.out.println("\t" + filename);
			}
		}
	}

}
