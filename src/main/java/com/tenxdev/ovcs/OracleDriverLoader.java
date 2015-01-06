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
package com.tenxdev.ovcs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;

import com.tenxdev.ovcs.SettingsStore.SettingsStoreException;

/**
 * Loads the Oracle JDBC driver dynamically
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class OracleDriverLoader {

	/**
	 * Loads the JDBC driver, using the location previously specified using ovcs
	 * config
	 *
	 * @throws OvcsException
	 *             if the driver could not be loaded
	 */
	public Driver load() throws OvcsException {
		String jdbcLibPath;
		try {
			jdbcLibPath = new SettingsStore(Application.CONFIG_FOLDER_NAME).load().getSetting(Settings.ORACLE_DRIVER,
					true);
		} catch (final SettingsStoreException e) {
			throw new OvcsException("Unable to read configuration: " + e.getMessage(), e);
		}
		return load(jdbcLibPath);
	}

	/**
	 * Loads the JDBC driver from the specified path
	 *
	 * @param jdbcLibPath
	 *            the path to the KDBC driver jar file
	 * @return
	 * @throws OvcsException
	 *             if the driver could not be loaded
	 */
	public Driver load(final String jdbcLibPath) throws OvcsException {
		final File jar = new File(jdbcLibPath);
		if (!jar.exists()) {
			throw new OvcsException(jdbcLibPath + " does not exist");
		}
		try {
			final URL[] urls = new URL[] { jar.toURI().toURL() };
			@SuppressWarnings("resource")
			final URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
			return (Driver) classLoader.loadClass("oracle.jdbc.driver.OracleDriver").newInstance();
		} catch (final IOException e) {
			throw new OvcsException("Unable to load Oracle library: " + e.getMessage(), e);
		} catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new OvcsException("The library was loaded, but it does not appear to be an Oracle driver: "
					+ e.getMessage(), e);
		}
	}
}
