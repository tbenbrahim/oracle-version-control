package com.tenxdev.ovcs;

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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public final class PersistentSettings {

	private static PersistentSettings settings;
	private final Properties properties = new Properties();
	private final File settingsFile;

	protected PersistentSettings() throws IOException {
		settingsFile = Paths.get(System.getProperty("user.home"), ".ovcs",
				"settings").toFile();
		if (settingsFile.exists()) {
			try (FileReader reader = new FileReader(settingsFile)) {
				properties.load(reader);
			}
		} else if (!settingsFile.getParentFile().mkdirs()) {
			throw new IOException("Unable to create directory "
					+ settingsFile.getParentFile());
		}
	}

	public void set(String key, String value) throws IOException {
		properties.setProperty(key, value);
		try (FileWriter writer = new FileWriter(settingsFile)) {
			properties.store(writer, "ovcs Settings");
		}
	}

	public static synchronized PersistentSettings getInstance()
			throws IOException {
		if (settings == null) {
			settings = new PersistentSettings();
		}
		return settings;
	}
}
