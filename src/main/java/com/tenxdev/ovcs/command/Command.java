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
import com.tenxdev.ovcs.OvcsException;

/**
 * Interface for all ovcs commands
 *
 * @author Tony BenBrahim <tony.benrbahim@10xdev.com>
 *
 */
public interface Command {

	/**
	 * Execute the command
	 * 
	 * @param args
	 *            the command line arguments
	 * @throws OvcsException
	 *             in case of error
	 */
	void execute(String... args) throws OvcsException;

}
