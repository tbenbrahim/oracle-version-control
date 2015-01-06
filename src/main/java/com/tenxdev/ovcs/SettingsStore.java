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

public class SettingsStore {

	@SuppressWarnings("serial")
	public class MissingSettingException extends SettingsStoreException {
	}

	@SuppressWarnings("serial")
	public class SettingsNotLoadedException extends SettingsStoreException {
	}

	@SuppressWarnings("serial")
	public class SettingsStoreException extends Exception {

		public SettingsStoreException() {
			super();
		}

		public SettingsStoreException(final String message) {
			super(message);
		}

		public SettingsStoreException(final String message, final Throwable throwable) {
			super(message, throwable);
		}
	}

	private transient Properties properties;

	private transient final String configFolderName;

	public SettingsStore(final String configFolderName) {
		this.configFolderName = configFolderName;
	}

	public String getSetting(final String key) {
		return properties.getProperty(key);
	}

	public String getSetting(final String key, final boolean mustExist) throws MissingSettingException {
		if (mustExist && !properties.containsKey(key)) {
			throw new MissingSettingException();
		}
		return getSetting(key);
	}

	public String getSetting(final String key, final String defaultValue) {
		return properties.containsKey(key) ? properties.getProperty(key) : defaultValue;
	}

	public String getSetting(final String key, final String defaultValue, final boolean mustExist)
			throws MissingSettingException {
		if (mustExist && !properties.containsKey(key)) {
			throw new MissingSettingException();
		}
		return getSetting(key, defaultValue);
	}

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

	public SettingsStore setSetting(final String key, final String value) throws SettingsNotLoadedException {
		if (properties == null) {
			throw new SettingsNotLoadedException();
		}
		properties.setProperty(key, value);
		return this;
	}

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
