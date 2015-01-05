package com.tenxdev.ovcs.command;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import com.tenxdev.ovcs.OvcsException;

public class PushCommand extends AbstractCommand {

	@Override
	public void execute(String[] args) throws OvcsException {
		FileRepository repository = getRepoForCurrentDir();
		try {
			doPush(new Git(repository));
		} catch (GitAPIException e) {
			throw new OvcsException("Unable to push: "+e.getMessage(), e);
		} finally {
			repository.close();
		}
	}

}
