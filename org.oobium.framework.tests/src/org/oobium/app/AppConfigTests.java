/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.app;

import static org.oobium.utils.Config.*;
import static org.oobium.utils.Config.Mode.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oobium.utils.Config;

public class AppConfigTests {

	@Test
	public void testAppLocations() throws Exception {
		Config config = Config.loadConfiguration("");
		assertEquals("com.test", config.getPathToApp("com.test"));
	}
	
	@Test
	public void testDefaultConfiguration() throws Exception {
		String json =	"({\n" +
						"\n" +
						"cache:   \"org.oobium.cache.file\",\n" +
						"session: \"org.oobium.session.db\",\n" +
						"persist: \"org.oobium.persist.db.derby.embedded\",\n" +
						"server:  \"org.oobium.server\",\n" +
						"\n" +
						"dev: {\n" +
						"	host: \"localhost\",\n" +
						"	port: 5000,\n" +
						"	modules: \"org.oobium.app.dev\",\n" +
						"},\n" +
						"\n" +
						"test: {\n" +
						"	host: \"localhost\",\n" +
						"	port: 5001,\n" +
						"},\n" +
						"\n" +
						"prod: {\n" +
						"	host: \"my.domain.com\",\n" +
						"	port: 80,\n" +
						"}\n" +
						"\n" +
						"});";
		Config config = Config.loadConfiguration(json);

		fail("out of date");
	}
	
	@Test
	public void testNested() throws Exception {
		String json;
		Config config;
		
		json = "modules: 'org.test',\ndev: {\n\tmodules: 'org.test.dev'\n}\n";
		config = Config.loadConfiguration(json);
		
		assertTrue(config.get(MODULES) instanceof List<?>);
		assertEquals(2, ((List<?>) config.get(MODULES)).size());
		assertEquals("org.test.dev", ((List<?>) config.get(MODULES)).get(1));

		json =	"modules: {\n" +
				"	\"org.test\": {\n" +
				"		persist: \"org.oobium.persist.db\"\n" +
				"	}\n" +
				"},\n" +
				"\n" +
				"dev: {\n" +
				"	host: \"localhost\",\n" +
				"	port: 5000,\n" +
				"	modules: \"org.test.dev\"\n" +
				"}";
		config = Config.loadConfiguration(json);
		
		assertTrue(config.get(MODULES) instanceof List<?>);
		assertEquals(2, ((List<?>) config.get(MODULES)).size());
		assertEquals("org.test.dev", ((List<?>) config.get(MODULES)).get(1));
		assertEquals("org.test", ((Map<?,?>) ((List<?>) config.get(MODULES)).get(0)).keySet().iterator().next());
	}
	
}
