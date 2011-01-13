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

import java.io.File;
import java.util.List;
import java.util.jar.Manifest;

import org.oobium.build.gen.TestGenerator;

public class TestSuite extends Bundle {
	
	/**
	 * the file of this test suite's module
	 */
	public final File module;

	/**
	 * the name of this test suite's module bundle
	 */
	public final String moduleName;
	
	/**
	 * this project's source directory for functional tests
	 */
	public final File func;

	/**
	 * this project's source directory for integration tests
	 */
	public final File intg;

	/**
	 * this project's source directory for unit tests
	 */
	public final File unit;

	
	TestSuite(Type type, File file, Manifest manifest) {
		super(type, file, manifest);

		this.moduleName = name.substring(0, name.length() - 6);
		this.module = new File(file.getParent(), moduleName);
		this.func = new File(file, "src-functional");
		this.intg = new File(file, "src-integration");
		this.unit = new File(file, "src-unit");
	}

	public void createControllerTests(Module module, String controllerName) {
		TestGenerator gen = new TestGenerator(this);
		gen.createControllerTests(module, controllerName);
	}
	
	public List<File> createTests(Module module) {
		TestGenerator gen = new TestGenerator(this);
		return gen.createTests(module);
	}
	
	public File getFixturesFile(Module module) {
		String relativePath = module.models.getAbsolutePath();
		relativePath = relativePath.substring(module.file.getPath().length());
		return new File(file, relativePath + File.separator + "Fixtures.js");
	}
	
	public File getGenTestFile(Module module, File model) {
		String relativePath = module.models.getAbsolutePath();
		relativePath = relativePath.substring(module.file.getPath().length());
		String name = model.getName();
		name = name.substring(0, name.length() - 5) + "ModelTests.java";
		return new File(file, relativePath + File.separator + name);
	}

	public File getTestFile(Module module, File model) {
		String relativePath = module.models.getAbsolutePath();
		relativePath = relativePath.substring(module.file.getPath().length());
		String name = model.getName();
		name = name.substring(0, name.length() - 5) + "Tests.java";
		return new File(file, relativePath + File.separator + name);
	}

}
