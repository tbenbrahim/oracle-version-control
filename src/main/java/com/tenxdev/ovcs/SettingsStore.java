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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Manages settings in a Java properties file
 *
 * @author Tony BenBrahim <tony.benbrahim@10xdev.com>
 *
 */
public class SettingsStore {

	/**
	 * Exception thrown when retrieving a mandatory setting that does not exist
	 *
	 */
	@SuppressWarnings("serial")
	public class MissingSettingException extends SettingsStoreException {
	}

	/**
	 * Exception thrown when calling save or getSetting without initially
	 * calling load
	 *
	 */
	@SuppressWarnings("serial")
	public class SettingsNotLoadedException extends SettingsStoreException {
	}

	/**
	 * Generic settings store exception
	 *
	 */
	@SuppressWarnings("serial")
	public class SettingsStoreException extends Exception {

		/**
		 * default constructor
		 */
		public SettingsStoreException() {
			super();
		}

		/**
		 * constructor
		 *
		 * @param message
		 *            the error message
		 */
		public SettingsStoreException(final String message) {
			super(message);
		}

		/**
		 * constructor
		 *
		 * @param message
		 *            the error message
		 * @param throwable
		 *            the underlying cause of the error
		 */
		public SettingsStoreException(final String message, final Throwable throwable) {
			super(message, throwable);
		}
	}

	/**
	 * hold for loaded properties
	 */
	private transient Properties properties;

	/**
	 * the name of the subfolder under the user home directory that contains the
	 * settings file
	 */
	private transient final String configFolderName;

	/**
	 * constructor
	 *
	 * @param configFolderName
	 *            the name of the subfolder under the user home directory that
	 *            contains the settings file
	 */
	public SettingsStore(final String configFolderName) {
		this.configFolderName = configFolderName;
	}

	/**
	 * get a setting value
	 *
	 * @param key
	 *            the name of the setting
	 * @return the value of the setting, or null if the setting does not exist
	 */
	public String getSetting(final String key) {
		return properties.getProperty(key);
	}

	/**
	 * get a setting value
	 *
	 * @param key
	 *            the name of the setting
	 * @param mustExist
	 *            if true, the setting must exist or a
	 *            {@link MissingSettingException} will be thrown
	 * @return the value of the setting, or null if the setting does not exist
	 *         and mustExist is false
	 * @throws MissingSettingException
	 *             if mustExist is true and the setting does not exist
	 */
	public String getSetting(final String key, final boolean mustExist) throws MissingSettingException {
		if (mustExist && !properties.containsKey(key)) {
			throw new MissingSettingException();
		}
		return getSetting(key);
	}

	/**
	 * get a setting value
	 *
	 * @param key
	 *            the name of the setting
	 * @param defaultValue
	 *            the default value to return if the setting does not exist
	 * @return the value of the setting, or the default value if the setting
	 *         does not exist and mustExist is false
	 */
	public String getSetting(final String key, final String defaultValue) {
		return properties.containsKey(key) ? properties.getProperty(key) : defaultValue;
	}

	/**
	 * get a setting value
	 *
	 * @param key
	 *            the name of the setting
	 * @param defaultValue
	 *            the default value to return if the setting does not exist
	 * @param mustExist
	 *            if true, the setting must exist or a
	 *            {@link MissingSettingException} will be thrown
	 * @return the value of the setting, or defaultValue if the setting does not
	 *         exist and mustExist is false
	 * @throws MissingSettingException
	 *             if mustExist is true and the setting does not exist
	 */
	public String getSetting(final String key, final String defaultValue, final boolean mustExist)
			throws MissingSettingException {
		if (mustExist && !properties.containsKey(key)) {
			throw new MissingSettingException();
		}
		return getSetting(key, defaultValue);
	}

	/**
	 * Load all settings. This must be called before any other operation.
	 *
	 * @return the setting store
	 * @throws SettingsStoreException
	 *             if the settings could not be loaded
	 */
	public SettingsStore load() throws SettingsStoreException {
		properties = new Properties();
		final File settingsFile = Paths.get(System.getProperty("user.home"), configFolderName, "settings").toFile();
		if (settingsFile.exists()) {
			try (Reader reader = new InputStreamReader(new FileInputStream(settingsFile), StandardCharsets.UTF_8)) {
				properties.load(reader);
			} catch (final IOException e) {
				throw new SettingsStoreException("Unable to read settings file: " + e.getMessage(), e);
			}
		} else if (!settingsFile.getParentFile().mkdirs()) {
			throw new SettingsStoreException("Unable to create directory " + settingsFile.getParentFile());
		}
		return this;
	}

	/**
	 * set a setting value
	 *
	 * @param key
	 *            the setting name
	 * @param value
	 *            the setting value
	 * @return the setting store
	 * @throws SettingsNotLoadedException
	 *             if setSetting called before the store has been loaded with
	 *             load
	 */
	public SettingsStore setSetting(final String key, final String value) throws SettingsNotLoadedException {
		if (properties == null) {
			throw new SettingsNotLoadedException();
		}
		properties.setProperty(key, value);
		return this;
	}

	/**
	 * store the contents of the store to the settings file
	 * 
	 * @return the settings store
	 * @throws SettingsStoreException
	 *             if the store cannot be saved, or if store is called before
	 *             load
	 */
	public SettingsStore store() throws SettingsStoreException {
		if (properties == null) {
			throw new SettingsNotLoadedException();
		}
		final File settingsFile = Paths.get(System.getProperty("user.home"), configFolderName, "settings").toFile();
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(settingsFile),
				StandardCharsets.UTF_8)) {
			properties.store(writer, "ovcs Settings");
		} catch (final IOException e) {
			throw new SettingsStoreException("Unable to write to settings file: " + e.getMessage(), e);
		}
		return this;
	}

}
