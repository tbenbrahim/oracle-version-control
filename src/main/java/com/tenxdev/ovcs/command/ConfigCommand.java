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
import java.util.Arrays;
import java.util.List;

import com.tenxdev.ovcs.Application;
import com.tenxdev.ovcs.OracleDriverLoader;
import com.tenxdev.ovcs.OvcsException;
import com.tenxdev.ovcs.Settings;
import com.tenxdev.ovcs.SettingsStore;
import com.tenxdev.ovcs.SettingsStore.SettingsStoreException;
import com.tenxdev.ovcs.UsageException;

public class ConfigCommand implements Command {

	private static final List<String> KNOWN_KEYS = Arrays.asList(new String[] { "oracle-driver" });
	public static final String USAGE = "ovcs config key value";

	@Override
	public void execute(final String... args) throws OvcsException {
		if (args.length != 3) {
			throw new UsageException(USAGE);
		}
		final String key = args[1];
		if (!KNOWN_KEYS.contains(key)) {
			throw new OvcsException(String.format("Error: Unknown configuration key %s", key));
		}
		final String value = args[2];
		if (Settings.ORACLE_DRIVER.equals(key)) {
			new OracleDriverLoader().load(value);
		}
		storeSetting(key, value);
	}

	private void storeSetting(final String key, final String value) throws OvcsException {
		try {
			new SettingsStore(Application.CONFIG_FOLDER_NAME).load().setSetting(key, value).store();
		} catch (final SettingsStoreException e) {
			throw new OvcsException("Unable to configure ovcs: " + e.getMessage(), e);
		}
	}

}
