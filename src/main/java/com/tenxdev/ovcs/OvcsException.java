/*
 * Copyright 2015 Abed Tony BenBrahim <tony.benbrahim@10xdev.com>
 *  This file is part of OVCS.
 *
 * OVCS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OVCS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OVCS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tenxdev.ovcs;

/**
 * class for general ovcs errors
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
@SuppressWarnings("serial")
public class OvcsException extends Exception {

	/**
	 * constructor
	 * 
	 * @param message
	 *            error message
	 */
	public OvcsException(final String message) {
		super(message);
	}

	/**
	 * constructor
	 * 
	 * @param message
	 *            error message
	 * @param throwable
	 *            underlying cause of error
	 */
	public OvcsException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
