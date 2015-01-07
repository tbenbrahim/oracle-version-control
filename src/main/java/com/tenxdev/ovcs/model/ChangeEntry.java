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
package com.tenxdev.ovcs.model;

/**
 * Description of a database change
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class ChangeEntry {
	/**
	 * the name of the database object
	 */
	private final String name;
	/**
	 * true if the object was dropped from the database, false otherwise
	 */
	private final boolean removed;

	/**
	 * constructor
	 *
	 * @param name
	 *            the name of the database object
	 * @param removed
	 *            true if the object was dropped from the database, false
	 *            otherwise
	 */
	public ChangeEntry(final String name, final boolean removed) {
		super();
		this.name = name;
		this.removed = removed;
	}

	/**
	 * gets the name of the database object
	 *
	 * @return the name of the database object
	 */
	public String getName() {
		return name;
	}

	/**
	 * determines if the object was removed from the database
	 *
	 * @return true if the object was dropped from the database, false otherwise
	 */
	public boolean isRemoved() {
		return removed;
	}

}
