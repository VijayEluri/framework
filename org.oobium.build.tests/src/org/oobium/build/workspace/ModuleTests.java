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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.oobium.build.workspace.Bundle.Type.Module;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.Test;

public class ModuleTests {

	private File file = new File(System.getProperty("user.home"));
	
	private Manifest getManifest(String key, String value) {
		Manifest manifest = mock(Manifest.class);
		Attributes attrs = new Attributes();
		attrs.put(new Attributes.Name(key), value);
		when(manifest.getMainAttributes()).thenReturn(attrs);
		return manifest;
	}
	
	@Test
	public void testGetActionCache() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.caches, "Cache1.java"), module.getActionCache("cache1"));
		assertEquals(new File(module.caches, "Cache1.java"), module.getActionCache("Cache1"));
		assertEquals(new File(module.caches, "Cache1.java"), module.getActionCache("Cache1.java"));
	}
	
	@Test
	public void testGetController() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.controllers, "NoticeController.java"), module.getController("notice"));
		assertEquals(new File(module.controllers, "NoticeController.java"), module.getController("Notice"));
		assertEquals(new File(module.controllers, "NoticeController.java"), module.getController("Notice.java"));
		assertEquals(new File(module.controllers, "TestController.java"), module.getController("testController"));
		assertEquals(new File(module.controllers, "TestController.java"), module.getController("testcontroller"));
		assertEquals(new File(module.controllers, "TestController.java"), module.getController("Testcontroller"));
		assertEquals(new File(module.controllers, "TestController.java"), module.getController("TestController"));
		assertEquals(new File(module.controllers, "TestController.java"), module.getController("TestController.java"));
	}	
	
	@Test
	public void testGetMailer() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.mailers, "NoticeMailer.java"), module.getMailer("notice"));
		assertEquals(new File(module.mailers, "NoticeMailer.java"), module.getMailer("Notice"));
		assertEquals(new File(module.mailers, "NoticeMailer.java"), module.getMailer("noticemailer"));
		assertEquals(new File(module.mailers, "NoticeMailer.java"), module.getMailer("NoticeMailer"));
		assertEquals(new File(module.mailers, "NoticeMailer.java"), module.getMailer("NoticeMailer.java"));
	}
	
	@Test
	public void testGetModel() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.models, "Notice.java"), module.getModel("notice"));
		assertEquals(new File(module.models, "Notice.java"), module.getModel("Notice"));
		assertEquals(new File(module.models, "Notice.java"), module.getModel("Notice.java"));
	}
	
	@Test
	public void testGetObserver() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.observers, "NoticeObserver.java"), module.getObserver("notice"));
		assertEquals(new File(module.observers, "NoticeObserver.java"), module.getObserver("Notice"));
		assertEquals(new File(module.observers, "NoticeObserver.java"), module.getObserver("Notice.java"));
	}
	
	@Test
	public void testGetView() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.views, "Home.esp"), module.getView("home"));
		assertEquals(new File(module.views, "Home.esp"), module.getView("Home"));
		assertEquals(new File(module.views, "Home.esp"), module.getView("Home.esp"));
		assertEquals(new File(module.views, "Home.esp"), module.getView("Home.java"));
		assertEquals(new File(module.views, "pages/Home.esp"), module.getView("pages/home"));
		assertEquals(new File(module.views, "pages/Home.esp"), module.getView("pages/Home"));
		assertEquals(new File(module.views, "test_pages/HomePage.esp"), module.getView("test_pages/homePage"));
		assertEquals(new File(module.views, "test_pages/HomePage.esp"), module.getView("test_pages/HomePage"));
	}
	
	@Test
	public void testGetViewsFolder() throws Exception {
		Manifest manifest = getManifest("Bundle-Version", "1.2.3.qualifier");
		Module module = new Module(Module, file, manifest);
		
		assertEquals(new File(module.views, "notices"), module.getViewsFolder("notice"));
		assertEquals(new File(module.views, "notices"), module.getViewsFolder("Notice"));
		assertEquals(new File(module.views, "simple_notices"), module.getViewsFolder("SimpleNotice"));
		assertEquals(new File(module.views, "simple_notices"), module.getViewsFolder("simpleNotice"));
	}
	
}
