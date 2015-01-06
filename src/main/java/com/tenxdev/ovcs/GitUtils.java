package com.tenxdev.ovcs;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

/**
 * Useful git routines
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public final class GitUtils {

	/**
	 * Prepare a tree parser for the HEAD revision
	 *
	 * @param repository
	 *            the git repository
	 * @return a tree parser for the HEAD revision
	 * @throws IOException
	 *             if the repository cannot be read
	 */
	public static AbstractTreeIterator prepareHeadTreeParser(final Repository repository) throws IOException {
		final String head = repository.getFullBranch();
		final String headId = repository.resolve(head).getName();
		final RevWalk walk = new RevWalk(repository);
		try {
			final RevCommit commit = walk.parseCommit(ObjectId.fromString(headId));
			final RevTree tree = walk.parseTree(commit.getTree().getId());
			final CanonicalTreeParser treeParser = new CanonicalTreeParser();
			final ObjectReader reader = repository.newObjectReader();
			try {
				treeParser.reset(reader, tree.getId());
			} finally {
				reader.release();
			}
			return treeParser;
		} finally {
			walk.dispose();
		}
	}

	private GitUtils() {
	}

}
