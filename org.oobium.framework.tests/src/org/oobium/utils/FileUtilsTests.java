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
package org.oobium.utils;

import static org.junit.Assert.*;
import static org.oobium.utils.CharStreamUtils.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class FileUtilsTests {

	/**
	 * The present working directory (should be this project's root folder)
	 */
	private static File pwd = new File(System.getProperty("user.dir"));
	
	/**
	 * The workspace's local working directory
	 */
	private static File lwd = new File(pwd, "temp_local");

	@Before
	public void setup() {
		FileUtils.delete(lwd);
		lwd.mkdirs();
	}

	@After
	public void teardown() {
		FileUtils.delete(lwd);
	}
	
	@Test
	public void testCreateJar_FromString() throws Exception {
		File jar = new File(lwd, "simple.jar");
		
		try {
			FileUtils.createJar(jar, System.currentTimeMillis(), new Object[][] {
				new String[] {	"/META-INF/MANIFEST.MF",
						"Bundle-Name: Test\n"
				},
				new String[] {	"/resources/test.properties",
						"test=yes\n"
				}
			});
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		String s;
		s = FileUtils.readJarEntry(jar, "/resources/test.properties");
		assertEquals("test=yes\n", s);
		System.out.println(s);

		s = FileUtils.readJarEntry(jar, "/does/not/Exist.java");
		assertNull(s);
		System.out.println(s);
	}

	@Ignore
	@Test
	public void testExtract() throws Exception {
		FileUtils.extract(new File("/home/jeremyd/org.oobium.client_0.6.0.201102211603.jar"), new File("/home/jeremyd/tmp/org.oobium.client"));
	}
}
