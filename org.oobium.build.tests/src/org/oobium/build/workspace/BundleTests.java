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
package org.oobium.build.workspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oobium.build.workspace.Bundle.Type.Bundle;

import java.io.File;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.Test;
import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.ExportedPackage;
import org.oobium.build.workspace.ImportedPackage;
import org.oobium.build.workspace.Version;
import org.oobium.build.workspace.VersionRange;

public class BundleTests {

	private File file = new File(System.getProperty("user.home"));
	
	private Manifest getManifest(String key, String value) {
		Manifest manifest = mock(Manifest.class);
		Attributes attrs = new Attributes();
		attrs.put(new Attributes.Name(key), value);
		when(manifest.getMainAttributes()).thenReturn(attrs);
		return manifest;
	}
	
	@Test
	public void testBundleVersion() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Bundle bundle = new Bundle(Bundle, file, manifest);
		assertEquals(1, bundle.version.major);
		assertEquals(2, bundle.version.minor);
		assertEquals(3, bundle.version.micro);
		assertEquals("qualifier", bundle.version.qualifier);
	}
	
	@Test
	public void testRequireBundle() throws Exception {
		String s;
		Manifest manifest;
		Bundle bundle;

		s = "org.oobium.app";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[*, *]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);

		s = "org.oobium.app;bundle-version=\"1.2.3\";resolution : = optional;visibility:=\"reexport\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.2.3, *]", bundle.requiredBundles.get(0).versionRange.toString());
		assertTrue(bundle.requiredBundles.get(0).optional);
		
		s = "org.oobium.app;bundle-version=\"1.2.3.today\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.2.3.today, *]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		
		s = "org.oobium.app;bundle-version=\"[1.2.3,1.2.3]\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.2.3, 1.2.3]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		
		s = "org.oobium.app;bundle-version=\"[1.2.3, 1.2.3]\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.2.3, 1.2.3]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		
		s = "org.oobium.app;bundle-version=\"[1, 2)\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.0.0, 2.0.0)", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		
		s = "org.oobium.app;bundle-version=\"(1, 2)\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(1, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("(1.0.0, 2.0.0)", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		
		s = "org.oobium.app,\n org.oobium.http";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(2, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[*, *]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		assertEquals("org.oobium.http", bundle.requiredBundles.get(1).name);
		assertEquals("[*, *]", bundle.requiredBundles.get(1).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(1).optional);

		s = "org.oobium.app;bundle-version=\"1.2.3\",\n org.oobium.http;bundle-version=\"4.5.6\"";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(2, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.2.3, *]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		assertEquals("org.oobium.http", bundle.requiredBundles.get(1).name);
		assertEquals("[4.5.6, *]", bundle.requiredBundles.get(1).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(1).optional);

		s = "org.oobium.app;bundle-version=1.2.3,\n org.oobium.http;bundle-version=4.5.6";
		manifest = getManifest("Require-Bundle", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.requiredBundles);
		assertEquals(2, bundle.requiredBundles.size());
		assertEquals("org.oobium.app", bundle.requiredBundles.get(0).name);
		assertEquals("[1.2.3, *]", bundle.requiredBundles.get(0).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(0).optional);
		assertEquals("org.oobium.http", bundle.requiredBundles.get(1).name);
		assertEquals("[4.5.6, *]", bundle.requiredBundles.get(1).versionRange.toString());
		assertFalse(bundle.requiredBundles.get(1).optional);
	}
	
	@Test
	public void testImportPackage() throws Exception {
		String s;
		Manifest manifest;
		Bundle bundle;

		s = "org.oobium.app";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("[*, *]", bundle.importedPackages.iterator().next().versionRange.toString());
		assertFalse(bundle.importedPackages.iterator().next().optional);

		s = "org.oobium.app;version=\"1.2.3\";resolution : = optional;visibility:=\"reexport\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("[1.2.3, *]", bundle.importedPackages.iterator().next().versionRange.toString());
		assertTrue(bundle.importedPackages.iterator().next().optional);
		
		s = "org.oobium.app;version=\"1.2.3.today\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("[1.2.3.today, *]", bundle.importedPackages.iterator().next().versionRange.toString());
		assertFalse(bundle.importedPackages.iterator().next().optional);
		
		s = "org.oobium.app;version=\"[1.2.3,1.2.3]\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("[1.2.3, 1.2.3]", bundle.importedPackages.iterator().next().versionRange.toString());
		assertFalse(bundle.importedPackages.iterator().next().optional);
		
		s = "org.oobium.app;version=\"[1.2.3, 1.2.3]\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("[1.2.3, 1.2.3]", bundle.importedPackages.iterator().next().versionRange.toString());
		assertFalse(bundle.importedPackages.iterator().next().optional);
		
		s = "org.oobium.app;version=\"[1, 2)\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("[1.0.0, 2.0.0)", bundle.importedPackages.iterator().next().versionRange.toString());
		assertFalse(bundle.importedPackages.iterator().next().optional);
		
		s = "org.oobium.app;version=\"(1, 2)\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(1, bundle.importedPackages.size());
		assertEquals("org.oobium.app", bundle.importedPackages.iterator().next().name);
		assertEquals("(1.0.0, 2.0.0)", bundle.importedPackages.iterator().next().versionRange.toString());
		assertFalse(bundle.importedPackages.iterator().next().optional);
		
		s = "org.oobium.app,\n org.oobium.http";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(2, bundle.importedPackages.size());
		Iterator<ImportedPackage> iter = bundle.importedPackages.iterator();
		ImportedPackage importedPackage = iter.next();
		assertEquals("org.oobium.app", importedPackage.name);
		assertEquals("[*, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
		importedPackage = iter.next();
		assertEquals("org.oobium.http", importedPackage.name);
		assertEquals("[*, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
		
		s = "org.oobium.app;version=\"1.2.3\",\n org.oobium.http;version=\"4.5.6\"";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(2, bundle.importedPackages.size());
		iter = bundle.importedPackages.iterator();
		importedPackage = iter.next();
		assertEquals("org.oobium.app", importedPackage.name);
		assertEquals("[1.2.3, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
		importedPackage = iter.next();
		assertEquals("org.oobium.http", importedPackage.name);
		assertEquals("[4.5.6, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
		
		s = "javax.servlet; version=2.4,javax.servlet.http; version\n =2.4,javax.servlet.resources; version=2.4";
		manifest = getManifest("Import-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.importedPackages);
		assertEquals(3, bundle.importedPackages.size());
		iter = bundle.importedPackages.iterator();
		importedPackage = iter.next();
		assertEquals("javax.servlet", importedPackage.name);
		assertEquals("[2.4.0, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
		importedPackage = iter.next();
		assertEquals("javax.servlet.http", importedPackage.name);
		assertEquals("[2.4.0, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
		importedPackage = iter.next();
		assertEquals("javax.servlet.resources", importedPackage.name);
		assertEquals("[2.4.0, *]", importedPackage.versionRange.toString());
		assertFalse(importedPackage.optional);
	}

	@Test
	public void testExportPackage() throws Exception {
		String s;
		Manifest manifest;
		Bundle bundle;

		s = "org.oobium.app";
		manifest = getManifest("Export-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.exportedPackages);
		assertEquals(1, bundle.exportedPackages.size());
		assertEquals("org.oobium.app", bundle.exportedPackages.iterator().next().name);
		assertEquals("0.0.0", bundle.exportedPackages.iterator().next().version.toString());

		s = "org.oobium.app;version=\"1.2.3\"";
		manifest = getManifest("Export-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.exportedPackages);
		assertEquals(1, bundle.exportedPackages.size());
		assertEquals("org.oobium.app", bundle.exportedPackages.iterator().next().name);
		assertEquals("1.2.3", bundle.exportedPackages.iterator().next().version.toString());
		
		s = "org.oobium.app;version=\"1.2.3.today\"";
		manifest = getManifest("Export-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.exportedPackages);
		assertEquals(1, bundle.exportedPackages.size());
		assertEquals("org.oobium.app", bundle.exportedPackages.iterator().next().name);
		assertEquals("1.2.3.today", bundle.exportedPackages.iterator().next().version.toString());
		
		s = "org.oobium.app,\n org.oobium.http";
		manifest = getManifest("Export-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.exportedPackages);
		assertEquals(2, bundle.exportedPackages.size());
		Iterator<ExportedPackage> iter = bundle.exportedPackages.iterator();
		ExportedPackage exportedPackage = iter.next();
		assertEquals("org.oobium.app", exportedPackage.name);
		assertEquals("0.0.0", exportedPackage.version.toString());
		exportedPackage = iter.next();
		assertEquals("org.oobium.http", exportedPackage.name);
		assertEquals("0.0.0", exportedPackage.version.toString());

		s = "org.oobium.app;version=\"1.2.3\",\n org.oobium.http;version=\"4.5.6\"";
		manifest = getManifest("Export-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.exportedPackages);
		assertEquals(2, bundle.exportedPackages.size());
		iter = bundle.exportedPackages.iterator();
		exportedPackage = iter.next();
		assertEquals("org.oobium.app", exportedPackage.name);
		assertEquals("1.2.3", exportedPackage.version.toString());
		exportedPackage = iter.next();
		assertEquals("org.oobium.http", exportedPackage.name);
		assertEquals("4.5.6", exportedPackage.version.toString());

		s = "org.oobium.app;version=1.2.3,\n org.oobium.http;version=4.5.6";
		manifest = getManifest("Export-Package", s);
		bundle = new Bundle(Bundle, file, manifest);
		assertNotNull(bundle.exportedPackages);
		assertEquals(2, bundle.exportedPackages.size());
		iter = bundle.exportedPackages.iterator();
		exportedPackage = iter.next();
		assertEquals("org.oobium.app", exportedPackage.name);
		assertEquals("1.2.3", exportedPackage.version.toString());
		exportedPackage = iter.next();
		assertEquals("org.oobium.http", exportedPackage.name);
		assertEquals("4.5.6", exportedPackage.version.toString());
	}

	@Test
	public void testResolve() throws Exception {
		assertTrue(new Version("1.2.3").resolves(null));
		
		assertTrue(new Version("1.2.3").resolves(new VersionRange("1.2.3")));
		assertTrue(new Version("1.2.4").resolves(new VersionRange("1.2.3")));
		assertTrue(new Version("2").resolves(new VersionRange("1.2.3")));
		assertFalse(new Version("1.2").resolves(new VersionRange("1.2.3")));
		
		assertTrue(new Version("1.2.3").resolves(new VersionRange("[1.2.3, 1.2.3]")));
		assertFalse(new Version("1.2.3").resolves(new VersionRange("[1.2.3, 1.2.3)")));
		assertFalse(new Version("1.2.3").resolves(new VersionRange("(1.2.3, 1.2.3)")));
		
		assertTrue(new Version("1.5").resolves(new VersionRange("[1.2.3, 2)")));
		assertTrue(new Version("1.5.3.test").resolves(new VersionRange("(1.2.3, 2)")));

		assertTrue(new Version("2.0.0").resolves(new VersionRange("[1.2.3, 2]")));
		assertFalse(new Version("2.0.0").resolves(new VersionRange("[1.2.3, 2)")));
		assertFalse(new Version("2.0.0").resolves(new VersionRange("(1.2.3, 2)")));
	}
	
}
