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
package org.oobium.build.gen;

import static org.oobium.utils.StringUtils.*;
import static org.oobium.utils.FileUtils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oobium.app.server.controller.Action;
import org.oobium.build.events.BuildEvent;
import org.oobium.build.events.BuildListener;
import org.oobium.build.events.BuildEvent.Type;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Module;
import org.oobium.build.workspace.TestSuite;
import org.oobium.http.constants.RequestType;
import org.oobium.http.constants.StatusCode;
import org.oobium.test.ControllerTester;
import org.oobium.test.RouteTester;


public class TestGenerator {
	
	private static final Pattern create 	= Pattern.compile("public\\s+void\\s+create()");
	private static final Pattern destroy	= Pattern.compile("public\\s+void\\s+destroy()");
	private static final Pattern update 	= Pattern.compile("public\\s+void\\s+update()");
	private static final Pattern show	 	= Pattern.compile("public\\s+void\\s+show()");
	private static final Pattern showAll 	= Pattern.compile("public\\s+void\\s+showAll()");
	private static final Pattern showEdit 	= Pattern.compile("public\\s+void\\s+showEdit()");
	private static final Pattern showNew 	= Pattern.compile("public\\s+void\\s+showNew()");
	private static final Pattern handle 	= Pattern.compile("public\\s+void\\s+handleRequest()");


	private final TestSuite testSuite;
	private List<BuildListener> listeners;
	
	public TestGenerator(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	public boolean addListener(BuildListener listener) {
		if(listeners == null) {
			listeners = new ArrayList<BuildListener>();
			return listeners.add(listener);
		} else if(!listeners.contains(listener)) {
			return listeners.add(listener);
		}
		return false;
	}

	private File controllers(Module module) {
		String path = module.controllers.getAbsolutePath();
		path = path.substring(module.src.getAbsolutePath().length() + 1);
		return new File(testSuite.unit, path);
	}
	
	public File createControllerTests(Module module, File controller) {
		String name = controller.getName();
		name = name.substring(0, name.length() - 5);
		return createControllerTests(module, name);
	}

	public File createControllerTests(Module module, String controllerName) {
		File folder = controllers(module);
		String simpleName = controllerName + "Tests";
		
		File file = new File(folder, simpleName + ".java");
		if(listeners != null && file.exists()) {
			BuildEvent event = new BuildEvent(Type.FileExists, file);
			for(BuildListener listener : listeners.toArray(new BuildListener[listeners.size()])) {
				listener.handleEvent(event);
				if(event.doIt == false) {
					return file;
				}
			}
		}

		String controllerSrc = readFile(module.getController(controllerName)).toString();
		
		SourceFile sf = new SourceFile();
		
		sf.simpleName = simpleName;
		sf.packageName = testSuite.packageName(testSuite.unit, folder);
		sf.staticImports.add(Assert.class.getCanonicalName() + ".*");
		sf.staticImports.add(CoreMatchers.class.getCanonicalName() + ".*");
		sf.staticImports.add(StatusCode.class.getCanonicalName() + ".*");
		sf.imports.add(module.packageName(module.controllers) + "." + controllerName);
		sf.imports.add(Before.class.getCanonicalName());
		sf.imports.add(Test.class.getCanonicalName());
		sf.imports.add(ControllerTester.class.getCanonicalName());

		sf.variables.put("tester", ControllerTester.class.getSimpleName() + " tester");
		
		sf.methods.put("0_setup",
				"\t@Before\n" +
				"\tpublic void setup() throws Exception {\n" +
				"\t\ttester = new ControllerTester(" + controllerName + ".class);\n" +
				"\t}"
		);

		if(handle.matcher(controllerSrc).find()) {
			sf.staticImports.add(RequestType.class.getCanonicalName() + ".*");
			sf.methods.put("1_handleRequest",
					"\t@Test\n" +
					"\tpublic void handleRequest() throws Exception {\n" +
					"\t\ttester.execute(GET);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		if(create.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("2_create",
					"\t@Test\n" +
					"\tpublic void create() throws Exception {\n" +
					"\t\ttester.execute(create);\n" +
					"\t\tassertThat(tester.getStatus(), is(CREATED));\n" +
					"\t}"
			);
		}

		if(destroy.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("3_destroy",
					"\t@Test\n" +
					"\tpublic void destroy() throws Exception {\n" +
					"\t\ttester.execute(destroy);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		if(update.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("4_update",
					"\t@Test\n" +
					"\tpublic void update() throws Exception {\n" +
					"\t\ttester.execute(update);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		if(show.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("5_show",
					"\t@Test\n" +
					"\tpublic void show() throws Exception {\n" +
					"\t\ttester.execute(show);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		if(showAll.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("6_showAll",
					"\t@Test\n" +
					"\tpublic void showAll() throws Exception {\n" +
					"\t\ttester.execute(showAll);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		if(showEdit.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("7_showEdit",
					"\t@Test\n" +
					"\tpublic void showEdit() throws Exception {\n" +
					"\t\ttester.execute(showEdit);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		if(showNew.matcher(controllerSrc).find()) {
			sf.staticImports.add(Action.class.getCanonicalName() + ".*");
			sf.methods.put("8_showNew",
					"\t@Test\n" +
					"\tpublic void showNew() throws Exception {\n" +
					"\t\ttester.execute(showNew);\n" +
					"\t\tassertThat(tester.getStatus(), is(OK));\n" +
					"\t}"
			);
		}
		
		writeFile(file, sf.toSource());
		
		return file;
	}
	
	public File createModelTests(Module module, File model) {
		String name = model.getName();
		name = name.substring(0, name.length() - 5);
		return createModelTests(module, name);
	}

	public File createModelTests(Module module, String modelName) {
		File folder = models(module);
		String simpleName = modelName + "Tests";
		
		File file = new File(folder, simpleName + ".java");
		if(listeners != null && file.exists()) {
			BuildEvent event = new BuildEvent(Type.FileExists, file);
			for(BuildListener listener : listeners.toArray(new BuildListener[listeners.size()])) {
				listener.handleEvent(event);
				if(event.doIt == false) {
					return file;
				}
			}
		}

		SourceFile sf = new SourceFile();
		
		sf.simpleName = simpleName;
		sf.packageName = testSuite.packageName(testSuite.unit, folder);
		sf.staticImports.add(Assert.class.getCanonicalName() + ".*");
		sf.staticImports.add(CoreMatchers.class.getCanonicalName() + ".*");
		sf.imports.add(Test.class.getCanonicalName());

		String varName = varName(modelName);
		sf.methods.put("testCanSave",
				"\t@Test\n" +
				"\tpublic void testCanSave() throws Exception {\n" +
				"\t\t" + modelName + " " + varName + " = new " + modelName + "();\n" +
				"\t\tassertThat(" + varName + ".canSave(), is(true));\n" +
				"\t}"
		);

		writeFile(file, sf.toSource());
		
		return file;
	}

	/**
	 * @param module the module for which to make the RouteTests
	 * @return the RouteTests File; never null
	 */
	public File createRouteTests(Module module) {
		File folder = mainFolder(module);
		String simpleName = "RouteTests";
		
		File file = new File(folder, simpleName + ".java");
		if(listeners != null && file.exists()) {
			BuildEvent event = new BuildEvent(Type.FileExists, file);
			for(BuildListener listener : listeners.toArray(new BuildListener[listeners.size()])) {
				listener.handleEvent(event);
				if(event.doIt == false) {
					return file;
				}
			}
		}

		String activator = module.activator.getName();
		activator = activator.substring(0, activator.length() - 5);
		
		SourceFile sf = new SourceFile();
		
		sf.simpleName = simpleName;
		sf.packageName = testSuite.packageName(testSuite.unit, folder);
		sf.staticImports.add(Assert.class.getCanonicalName() + ".*");
		sf.staticImports.add(CoreMatchers.class.getCanonicalName() + ".*");
		sf.imports.add(Before.class.getCanonicalName());
		sf.imports.add(Test.class.getCanonicalName());
		sf.imports.add(RouteTester.class.getCanonicalName());
		sf.imports.add(module.packageName(module.activator) + "." + activator);

		sf.variables.put("tester", RouteTester.class.getSimpleName() + " tester");
		
		sf.methods.put("0_setup",
				"\t@Before\n" +
				"\tpublic void setup() throws Exception {\n" +
				"\t\ttester = new RouteTester(" + activator + ".class);\n" +
				"\t}"
		);
		
		if(module.isWebservice()) {
			sf.methods.put("testHome",
					"\t@Test\n" +
					"\tpublic void testHome() throws Exception {\n" +
					"\t\tassertThat(tester.hasHome(), is(false));\n" +
					"\t}"
			);
		} else {
			sf.methods.put("testHome",
					"\t@Test\n" +
					"\tpublic void testHome() throws Exception {\n" +
					"\t\tassertThat(tester.hasHome(), is(true));\n" +
					"\t}"
			);
		}
		
		writeFile(file, sf.toSource());
		
		return file;
	}

	private File createTest(File folder, String simpleName) {
		File file = new File(folder, simpleName + ".java");
		if(listeners != null && file.exists()) {
			BuildEvent event = new BuildEvent(Type.FileExists, file);
			for(BuildListener listener : listeners.toArray(new BuildListener[listeners.size()])) {
				listener.handleEvent(event);
				if(event.doIt == false) {
					return file;
				}
			}
		}

		SourceFile sf = new SourceFile();
		
		sf.simpleName = simpleName;
		sf.packageName = testSuite.packageName(testSuite.unit, folder);
		sf.staticImports.add(Assert.class.getCanonicalName() + ".*");
		sf.staticImports.add(CoreMatchers.class.getCanonicalName() + ".*");
		sf.imports.add(Test.class.getCanonicalName());

		sf.methods.put("testTruth",
				"\t@Test\n" +
				"\tpublic void testTruth() throws Exception {\n" +
				"\t\tassertThat(true, is(true));\n" +
				"\t}"
		);

		writeFile(file, sf.toSource());
		
		return file;
	}

	public List<File> createTests(Module module) {
		List<File> files = new ArrayList<File>();

		File test = createRouteTests(module);
		if(test != null) files.add(test);
		
		for(File file : module.findModels()) {
			test = createModelTests(module, file);
			if(test != null) files.add(test);
		}
		for(File file : module.findViews()) {
			test = createViewTests(module, file);
			if(test != null) files.add(test);
		}
		for(File file : module.findControllers()) {
			if(!"ApplicationController.java".equals(file.getName())) {
				test = createControllerTests(module, file);
				if(test != null) files.add(test);
			}
		}

		return files;
	}
	
	public File createViewTests(Module module, File view) {
		String name = view.getAbsolutePath();
		name = name.substring(module.views.getAbsolutePath().length()+1, name.length()-4);
		return createViewTests(module, name);
	}
	
	public File createViewTests(Module module, String viewName) {
		File folder = viewFolder(module, viewName);
		String simpleName = viewSimpleName(viewName) + "Tests";
		
		return createTest(folder, simpleName);
	}
	
	private File mainFolder(Module module) {
		String path = module.main.getAbsolutePath();
		path = path.substring(module.src.getAbsolutePath().length() + 1);
		return new File(testSuite.unit, path);
	}
	
	private File models(Module module) {
		String path = module.models.getAbsolutePath();
		path = path.substring(module.src.getAbsolutePath().length() + 1);
		return new File(testSuite.unit, path);
	}
	
	public boolean removeListener(BuildListener listener) {
		if(listeners != null && listeners.remove(listener)) {
			return true;
		}
		return false;
	}
	
	private File viewFolder(Module module, String viewName) {
		String path = module.getView(viewName).getParent();
		path = path.substring(module.src.getAbsolutePath().length() + 1);
		return new File(testSuite.unit, path);
	}
	
	private String viewSimpleName(String viewName) {
		String[] sa = viewName.split("/");
		return sa[sa.length - 1];
	}
	
}
