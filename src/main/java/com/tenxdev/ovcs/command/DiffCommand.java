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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.tenxdev.ovcs.GitUtils;
import com.tenxdev.ovcs.OvcsException;
import com.tenxdev.ovcs.UsageException;

public class DiffCommand extends AbstractOvcsCommand {

	private static final String USAGE = "ovcs diff [object-name]";

	@Override
	public void execute(final String... args) throws OvcsException {
		if (args.length != 1 && args.length != 2) {
			throw new UsageException(USAGE);
		}
		String targetObject = null;
		if (args.length == 2) {
			targetObject = args[1].toUpperCase(Locale.getDefault());
			if (!targetObject.toLowerCase(Locale.getDefault()).endsWith(".sql")) {
				targetObject = targetObject + ".sql";
			}
		}
		final FileRepository fileRepository = getRepoForCurrentDir();
		try {
			writeChanges(fileRepository);
			final DiffFormatter formatter = new DiffFormatter(System.out);
			formatter.setNewPrefix("new/");
			formatter.setOldPrefix("old/");
			formatter.setRepository(fileRepository);
			if (targetObject != null) {
				formatter.setPathFilter(PathFilter.create(targetObject));
			}
			final AbstractTreeIterator commitTreeIterator = GitUtils.prepareHeadTreeParser(fileRepository);
			final FileTreeIterator workTreeIterator = new FileTreeIterator(fileRepository);
			final List<DiffEntry> diffEntries = formatter.scan(commitTreeIterator, workTreeIterator);

			for (final DiffEntry entry : diffEntries) {
				System.out.println("Entry: " + entry);
				formatter.format(entry);
			}
		} catch (final IOException e) {
			throw new OvcsException("Unable to generate diff: " + e.getMessage(), e);
		} finally {
			fileRepository.close();
		}
	}

}
