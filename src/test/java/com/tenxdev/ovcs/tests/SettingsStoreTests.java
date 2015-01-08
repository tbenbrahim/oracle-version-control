package com.tenxdev.ovcs.tests;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tenxdev.ovcs.SettingsStore;
import com.tenxdev.ovcs.SettingsStore.SettingsNotLoadedException;
import com.tenxdev.ovcs.SettingsStore.SettingsStoreException;

public class SettingsStoreTests {

	@AfterClass
	public static void afterClass() throws Exception {
		if (!getSettingsFile().delete() || !getSettingsFile().getParentFile().delete()) {
			System.err.println(String.format("Unbale to delete %s", getSettingsFile().getParent()));
		}
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		configFolderName = "." + UUID.randomUUID().toString();
	}

	private static File getSettingsFile() {
		return Paths.get(System.getProperty("user.home"), configFolderName, "settings").toFile();
	}

	private static String configFolderName;

	@Test
	public void testRetrievingMandatory() throws SettingsStoreException {
		new SettingsStore(configFolderName).load().setSetting("foo", "bar").store();
		String value = new SettingsStore(configFolderName).load().getSetting("foo", true);
		assertEquals("bar", value);
		value = new SettingsStore(configFolderName).load().getSetting("foo", "foo", true);
		assertEquals("bar", value);
	}

	@Test(expected = SettingsStoreException.class)
	public void testRetrievingMandatoryMissing() throws SettingsStoreException {
		new SettingsStore(configFolderName).load().getSetting("qwe", true);
	}

	@Test
	public void testRetrievingWithDefaults() throws SettingsStoreException {
		final SettingsStore store = new SettingsStore(configFolderName).load();
		String value = store.getSetting("baz", "abc");
		assertEquals("abc", value);
		value = store.getSetting("baz", "abc", false);
		assertEquals("abc", value);
	}

	@Test(expected = SettingsNotLoadedException.class)
	public void testSettingWihtoutLoad() throws SettingsStoreException {
		new SettingsStore(configFolderName).setSetting("foo", "bar");
	}

	@Test
	public void testStoringAndRetrieving() throws SettingsStoreException {
		final SettingsStore store = new SettingsStore(configFolderName).load();
		store.setSetting("foo", "bar").store();
		assertTrue(getSettingsFile().exists());
		String value = store.getSetting("foo");
		assertEquals("bar", value);
		value = store.getSetting("foo", false);
		assertEquals("bar", value);
		value = store.getSetting("foo", "foo");
		assertEquals("bar", value);
	}

	@Test(expected = SettingsNotLoadedException.class)
	public void testStoringWihtoutLoad() throws SettingsStoreException {
		new SettingsStore(configFolderName).load().store();
		new SettingsStore(configFolderName).store();
	}

}
