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

import static org.oobium.build.util.SourceFile.closer;
import static org.oobium.build.util.SourceFile.find;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.field;
import static org.oobium.utils.StringUtils.getterName;
import static org.oobium.utils.StringUtils.varName;
import static org.oobium.utils.json.JsonUtils.format;
import static org.oobium.utils.json.JsonUtils.toJson;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.oobium.build.BuildBundle;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Module;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.test.TestCase;
import org.oobium.utils.FileUtils;
import org.oobium.utils.json.JsonUtils;


public class TestGenerator {

	private static Logger logger = Logger.getLogger(BuildBundle.class);
	
	private static final char[] regen = "@Regen".toCharArray();

	
	private static void appendAssertEquals(StringBuilder sb, ModelAdapter adapter, String method) throws NoSuchFieldException {
		String className = adapter.getModelClass().getSimpleName();
		String field = field(method);
		sb.append("\t\tassertEquals(fixtureValue(\"").append(className).append("01").append("\", \"").append(field).append("\"), ");
		sb.append(varName(className)).append(".").append(method).append("()");
		if(adapter.hasOne(field)) {
			sb.append(".getId()");
		}
		sb.append(");\n");
	}

	private static void appendCreateVar(StringBuilder sb, ModelAdapter adapter) {
		String className = adapter.getModelClass().getSimpleName();
		sb.append("\t\t").append(className).append(" ").append(varName(className)).append(" = ");
		appendSaveFixture(sb, className, false);
	}
	
	private static void appendManyToMany(StringBuilder sb, ModelAdapter adapter, String field) throws Exception {
		String className = adapter.getModelClass().getSimpleName();
		String var1 = varName(className) + 1;
		String var2 = varName(className) + 2;
		String var3 = varName(className) + 3;
		String var4 = varName(className) + 4;
		String fieldType = adapter.getHasManyMemberClass(field).getSimpleName();
		String fieldVar = varName(fieldType);
		String oppField = adapter.getOpposite(field);
		
		sb.append("\t\t").append(className).append(' ').append(var1).append(" = load(").append(className).append(".class);\n");
		sb.append("\t\t").append(var1).append('.').append(field).append("().add(load(").append(fieldType).append(".class, 1));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(1, ").append(var1).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var1).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(fieldVar).append('.').append(oppField).append("().contains(").append(var1).append("));\n");
		sb.append("\t\t}\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var1).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var2).append(" = ").append(className).append(".find").append(className).append("(").append(var1).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var1).append(" != ").append(var2).append(");\n");
		sb.append("\t\tassertTrue(").append(var1).append(".equals(").append(var2).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(1, ").append(var2).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var2).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(fieldVar).append('.').append(oppField).append("().contains(").append(var2).append("));\n");
		sb.append("\t\t}\n");
		sb.append("\n");

		sb.append("\t\t").append(var2).append('.').append(field).append("().add(load(").append(fieldType).append(".class, 2));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(2, ").append(var2).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var2).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(fieldVar).append('.').append(oppField).append("().contains(").append(var2).append("));\n");
		sb.append("\t\t}\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var2).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var3).append(" = ").append(className).append(".find").append(className).append("(").append(var2).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var2).append(" != ").append(var3).append(");\n");
		sb.append("\t\tassertTrue(").append(var2).append(".equals(").append(var3).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(2, ").append(var3).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var3).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(fieldVar).append('.').append(oppField).append("().contains(").append(var3).append("));\n");
		sb.append("\t\t}\n");
		sb.append("\n");

		sb.append("\t\t").append(var3).append('.').append(field).append("().clear();\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(0, ").append(var3).append('.').append(field).append("().size());\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var3).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var4).append(" = ").append(className).append(".find").append(className).append("(").append(var3).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var3).append(" != ").append(var4).append(");\n");
		sb.append("\t\tassertTrue(").append(var3).append(".equals(").append(var4).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(0, ").append(var4).append('.').append(field).append("().size());\n");		
	}

	private static void appendManyToNone(StringBuilder sb, ModelAdapter adapter, String field) throws Exception {
		String className = adapter.getModelClass().getSimpleName();
		String var1 = varName(className) + 1;
		String var2 = varName(className) + 2;
		String var3 = varName(className) + 3;
		String var4 = varName(className) + 4;
		String fieldType = adapter.getClass(field).getSimpleName();
		
		sb.append("\t\t").append(className).append(' ').append(var1).append(" = load(").append(className).append(".class);\n");
		sb.append("\t\t").append(var1).append('.').append(field).append("().add(load(").append(fieldType).append(".class, 1));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(1, ").append(var1).append('.').append(field).append("().size());\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var1).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var2).append(" = ").append(className).append(".find").append(className).append("(").append(var1).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var1).append(" != ").append(var2).append(");\n");
		sb.append("\t\tassertTrue(").append(var1).append(".equals(").append(var2).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(1, ").append(var2).append('.').append(field).append("().size());\n");
		sb.append("\n");

		sb.append("\t\t").append(var2).append('.').append(field).append("().add(load(").append(fieldType).append(".class, 2));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(2, ").append(var2).append('.').append(field).append("().size());\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var2).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var3).append(" = ").append(className).append(".find").append(className).append("(").append(var2).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var2).append(" != ").append(var3).append(");\n");
		sb.append("\t\tassertTrue(").append(var2).append(".equals(").append(var3).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(2, ").append(var3).append('.').append(field).append("().size());\n");
		sb.append("\n");

		sb.append("\t\t").append(var3).append('.').append(field).append("().clear();\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(0, ").append(var3).append('.').append(field).append("().size());\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var3).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");

		sb.append("\t\t").append(className).append(' ').append(var4).append(" = ").append(className).append(".find").append(className).append("(").append(var3).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var3).append(" != ").append(var4).append(");\n");
		sb.append("\t\tassertTrue(").append(var3).append(".equals(").append(var4).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(0, ").append(var4).append('.').append(field).append("().size());\n");
	}
	
	private static void appendManyToOne(StringBuilder sb, ModelAdapter adapter, String field) throws Exception {
		String className = adapter.getModelClass().getSimpleName();
		String var = varName(className);
		String var1 = varName(className) + 1;
		String var2 = varName(className) + 2;
		String var3 = varName(className) + 3;
		String var4 = varName(className) + 4;
		String fieldType = adapter.getClass(field).getSimpleName();
		String fieldVar = varName(fieldType);
		String getter = getterName(adapter.getOpposite(field));
		
		sb.append("\t\t").append(className).append(' ').append(var1).append(" = load(").append(className).append(".class);\n");
		sb.append("\t\t").append(var1).append('.').append(field).append("().add(load(").append(fieldType).append(".class, 1));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(1, ").append(var1).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var1).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(var1).append(" == ").append(fieldVar).append('.').append(getter).append("());\n");
		sb.append("\t\t}\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var1).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var2).append(" = ").append(className).append(".find").append(className).append("(").append(var1).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var1).append(" != ").append(var2).append(");\n");
		sb.append("\t\tassertTrue(").append(var1).append(".equals(").append(var2).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(1, ").append(var2).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var2).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(var2).append(" == ").append(fieldVar).append('.').append(getter).append("());\n");
		sb.append("\t\t}\n");
		sb.append("\n");

		sb.append("\t\t").append(var2).append('.').append(field).append("().add(load(").append(fieldType).append(".class, 2));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(2, ").append(var2).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var2).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(var2).append(" == ").append(fieldVar).append('.').append(getter).append("());\n");
		sb.append("\t\t}\n");
		sb.append("\n");
		sb.append("\t\tassertTrue(").append(var2).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");
		sb.append("\t\t").append(className).append(' ').append(var3).append(" = ").append(className).append(".find").append(className).append("(").append(var2).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var2).append(" != ").append(var3).append(");\n");
		sb.append("\t\tassertTrue(").append(var2).append(".equals(").append(var3).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(2, ").append(var3).append('.').append(field).append("().size());\n");
		sb.append("\t\tfor(").append(fieldType).append(" ").append(fieldVar).append(" : ").append(var3).append('.').append(field).append("()) {\n");
		sb.append("\t\t\tassertTrue(").append(var3).append(" == ").append(fieldVar).append('.').append(getter).append("());\n");
		sb.append("\t\t}\n");
		sb.append("\n");

		sb.append("\t\t").append(var3).append('.').append(field).append("().clear();\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(0, ").append(var3).append('.').append(field).append("().size());\n");
		sb.append("\n");
		if(adapter.isOppositeRequired(field)) {
			sb.append("\t\t").append(fieldType).append(' ').append(fieldVar).append("1 = ").append(fieldType).append(".find").append(fieldType).append("(1);\n");
			sb.append("\t\t").append(fieldType).append(' ').append(fieldVar).append("2 = ").append(fieldType).append(".find").append(fieldType).append("(2);\n");
			sb.append("\n");
			sb.append("\t\tassertFalse(").append(fieldVar).append("1.validate());\n");
			sb.append("\t\tassertFalse(").append(fieldVar).append("2.validate());\n");
			sb.append("\t\tassertFalse(").append(var3).append(".save());\n");
			sb.append("\n");
			sb.append("\t\t").append(className).append(' ').append(var).append(" = load(").append(className).append(".class, 2);\n");
			sb.append("\t\t").append(var).append('.').append(field).append("().add(").append(fieldVar).append("1);\n");
			sb.append("\t\t").append(var).append('.').append(field).append("().add(").append(fieldVar).append("2);\n");
			sb.append("\t\tassertTrue(").append(var).append(".save());\n");
			sb.append("\n");
			sb.append("\t\tassertTrue(").append(fieldVar).append("1.validate());\n");
			sb.append("\t\tassertTrue(").append(fieldVar).append("2.validate());\n");
		}
		sb.append("\t\tassertTrue(").append(var3).append(".save());\n");
		sb.append("\t\tresetSession();\n");
		sb.append("\n");

		sb.append("\t\t").append(className).append(' ').append(var4).append(" = ").append(className).append(".find").append(className).append("(").append(var3).append(".getId());\n");
		sb.append("\t\tassertTrue(").append(var3).append(" != ").append(var4).append(");\n");
		sb.append("\t\tassertTrue(").append(var3).append(".equals(").append(var4).append("));\n");
		sb.append("\n");
		sb.append("\t\tassertEquals(0, ").append(var4).append('.').append(field).append("().size());\n");
	}
	
	private static void appendNewVar(StringBuilder sb, ModelAdapter adapter) {
		String className = adapter.getModelClass().getSimpleName();
		sb.append("\t\t").append(className).append(" ").append(varName(className)).append(" = new ").append(className).append("();\n");
	}

	private static void appendSaveFixture(StringBuilder sb, String className, boolean tab) {
		if(tab) {
			sb.append("\t\t");
		}
		sb.append("saveFixture(").append(className).append(".class, \"").append(className).append("01").append("\");\n");
	}

	private static void appendSetVar(StringBuilder sb, ModelAdapter adapter) {
		String className = adapter.getModelClass().getSimpleName();
		sb.append("\t\t").append(varName(className)).append(" = ");
		appendSaveFixture(sb, className, false);
	}

	private static String className(Module module, File fileInSrcFolder) {
		String srcSegment = File.separator + module.src + File.separator;
		String path = fileInSrcFolder.getAbsolutePath();
		int ix = path.indexOf(srcSegment);
		path = path.substring(ix+srcSegment.length(), path.length()-5);
		path = path.replace(File.separatorChar, '.');
		return path;
	}
	
	@SuppressWarnings("unchecked")
	private static void createFixtures(Module module) throws Exception {
		File fixturesFile = fixturesFile(module);
		Map<String, Map<String, Object>> fixtures;
		if(fixturesFile.exists()) {
			StringBuilder sb = FileUtils.readFile(fixturesFile);
			fixtures = (Map<String, Map<String, Object>>) JsonUtils.toObject(sb.toString());
			fixtures = new TreeMap<String, Map<String,Object>>(fixtures);
		} else {
			fixtures = new TreeMap<String, Map<String,Object>>();
		}
		for(File modelFile : module.findModels()) {
			for(int i = 1; i < 3; i++) {
				Class<? extends Model> clazz = getClass(module, modelFile);
				ModelAdapter adapter = ModelAdapter.getAdapter(clazz);
				Map<String, Object> fixture = new TreeMap<String, Object>();
				for(String field : adapter.getFields()) {
					if(!adapter.isReadOnly(field) && !adapter.isVirtual(field)) {
						Class<?> fieldClass = adapter.getClass(field);
						if(fieldClass == String.class) {
							fixture.put(field, "test " + field + " 0" + i);
						} else if(adapter.hasOne(field)) {
							fixture.put(field, i);
						} else if(fieldClass == Boolean.class || fieldClass == boolean.class) {
							fixture.put(field, true);
						} else if(fieldClass == Integer.class || fieldClass == int.class) {
							fixture.put(field, 10 + i);
						} else if(Date.class.isAssignableFrom(fieldClass)) {
							fixture.put(field, "/Date(" + (System.currentTimeMillis() + i) + ")/");
						} else if(adapter.isRequired(field)) {
							logger.error("could not create field " + field + " of type: " + fieldClass + " in " + clazz);
						}
					}
				}
				fixtures.put(clazz.getSimpleName() + "0" + i, fixture);
			}
		}

		if(fixturesFile.exists()) {
			logger.info("updating  " + fixturesFile);
		} else {
			logger.info("creating " + fixturesFile);
		}
		writeFile(fixturesFile, format(toJson(fixtures)));
	}

	private static void createGenTestFile(Class<? extends Model> clazz, File genTestFile) throws Exception {
		ModelAdapter adapter = ModelAdapter.getAdapter(clazz);
		
		Class<?> genClass = clazz.getSuperclass();
		
		List<String> methods = new ArrayList<String>();
		for(Method method : genClass.getDeclaredMethods()) {
			int mods = method.getModifiers();
			if((mods & Modifier.PUBLIC) != 0 && (mods & Modifier.ABSTRACT) == 0 && (mods & Modifier.STATIC) == 0) {
				String name = method.getName();
				if(!name.equals("validate") && !name.equals("doValidate")) {
					methods.add(name);
				}
			}
		}
		
		SourceFile src = new SourceFile();
		src.packageName = genClass.getPackage().getName();
		src.simpleName = genClass.getSimpleName() + "Test";
		src.superName = "TestCase";
		src.imports.add("static " + Assert.class.getCanonicalName() + ".*");
		src.imports.add(Test.class.getCanonicalName());
		src.imports.add(TestCase.class.getCanonicalName());
		for(String method : methods) {
			String testMethod = "test" + Character.toUpperCase(method.charAt(0)) + method.substring(1);
			src.methods.put(testMethod, createMethod(adapter, method, testMethod, true));
		}

		logger.info("creating " + genTestFile);
		writeFile(genTestFile, src.toSource());
	}

	private static String createMethod(ModelAdapter adapter, String modelMethod, String testMethod, boolean leadingTab) throws Exception {
		StringBuilder sb = new StringBuilder();
		if(leadingTab) {
			sb.append("\t");
		}
		sb.append("@Regen\n");
		sb.append("\t@Test\n");
		sb.append("\tpublic void ").append(testMethod).append("() throws Exception {\n");
		sb.append("\t\trefreshDatabase();\n");
		sb.append("\n");
		if(modelMethod.startsWith("set")) {
			String className = adapter.getModelClass().getSimpleName();
			String var = varName(className);
			String field = field(modelMethod);
			sb.append("\t\t").append(className).append(' ').append(var).append(" = load(").append(className).append(".class);\n");
			sb.append("\t\tassertTrue(").append(var).append(".save());\n");
			sb.append("\t\t").append("assertEquals(fixtureValue(\"").append(className).append("01\", \"").append(field).append("\"), getDbValue(");
			sb.append(var).append(", \"").append(field).append("\"));\n");
		} else if(modelMethod.equals("getCreatedAt") || modelMethod.equals("getUpdatedAt") || modelMethod.equals("getCreatedOn") || modelMethod.equals("getUpdatedOn")) {
			String className = adapter.getModelClass().getSimpleName();
			appendCreateVar(sb, adapter);
			sb.append("\t\tassertNotNull(").append(varName(className)).append(".").append(modelMethod).append("());\n");
		} else if(modelMethod.startsWith("get")) {
			appendCreateVar(sb, adapter);
			appendAssertEquals(sb, adapter, modelMethod);
		} else if(modelMethod.startsWith("has")) {
			String field = field(modelMethod);
			if(adapter.isInitialized(field)) {
				appendCreateVar(sb, adapter);
			} else {
				appendNewVar(sb, adapter);
				sb.append("\t\tassertFalse(").append(varName(adapter.getModelClass())).append('.').append(modelMethod).append("());\n");
				sb.append("\n");
				appendSetVar(sb, adapter);
			}
			sb.append("\t\tassertTrue(").append(varName(adapter.getModelClass())).append('.').append(modelMethod).append("());\n");
		} else if(modelMethod.startsWith("is")) {
			appendNewVar(sb, adapter);
			sb.append("\t\tassertFalse(").append(varName(adapter.getModelClass())).append('.').append(modelMethod).append("());\n");
			sb.append("\n");
			appendSetVar(sb, adapter);
			sb.append("\t\tassertTrue(").append(varName(adapter.getModelClass())).append('.').append(modelMethod).append("());\n");
		} else {
			String field = field(modelMethod);
			if(adapter.hasMany(field)) {
				if(adapter.isManyToMany(field)) {
					appendManyToMany(sb, adapter, field);
				} else if(adapter.isManyToOne(field)) {
					appendManyToOne(sb, adapter, field);
				} else {
					appendManyToNone(sb, adapter, field);
				}
			} else {
				sb.append("\t\tfail(\"don't know how to build test for ").append(field).append(" in ").append(adapter.getModelClass()).append("\");\n");
				logger.warn("don't know how to build test for " + field + " in " + adapter.getModelClass());
			}
		}
		sb.append("\t}");
		return sb.toString();
	}
	
	public static void createModelTests(Module module, File model) throws Exception {
		Class<? extends Model> modelClass = getClass(module, model);
		if(modelClass == null) {
			return;
		}

		File testFile = testFile(module, model);
		if(testFile.exists()) {
			logger.info("skipping " + testFile);
		} else {
			createNewTestFile(modelClass, testFile);
		}
		
		File genTestFile = genTestFile(module, model);
		if(genTestFile.exists()) {
			updateGenTestFile(modelClass, genTestFile);
		} else {
			createGenTestFile(modelClass, genTestFile);
		}
	}

	private static void createNewTestFile(Class<? extends Model> clazz, File testFile) throws Exception {
		List<String> methods = new ArrayList<String>();
		for(Method method : clazz.getDeclaredMethods()) {
			int mods = method.getModifiers();
			if((mods & Modifier.PUBLIC) != 0 && (mods & Modifier.ABSTRACT) == 0) {
				methods.add(method.getName());
			}
		}
		
		if(!methods.contains("validate")) {
			methods.add("validate");
		}
		
		ModelAdapter adapter = ModelAdapter.getAdapter(clazz);
		String className = clazz.getSimpleName();
		
		SourceFile src = new SourceFile();
		src.packageName = clazz.getPackage().getName();
		src.simpleName = clazz.getSimpleName() + "Test";
		src.superName = "TestCase";
		src.imports.add("static " + Assert.class.getCanonicalName() + ".*");
		src.imports.add(Test.class.getCanonicalName());
		src.imports.add(TestCase.class.getCanonicalName());
		for(String method : methods) {
			String testMethod = "test" + Character.toUpperCase(method.charAt(0)) + method.substring(1);
			StringBuilder sb = new StringBuilder();
			sb.append("\t@Test\n");
			sb.append("\tpublic void ").append(testMethod).append("() throws Exception {\n");
			if("validate".equals(method)) {
				String var = varName(className);
				sb.append("\t\t").append(className).append(' ').append(var).append(" = new ").append(className).append("();\n");
				sb.append("\t\t").append("assertTrue(").append(var).append(".validate());\n");
			} else {
				sb.append("\t\tfail(\"Not yet implemented\");\n");
			}
			sb.append("\t}");
			src.methods.put(testMethod, sb.toString());
		}

		if(testFile.exists()) {
			logger.info("writing  " + testFile);
		} else {
			logger.info("creating " + testFile);
		}
		writeFile(testFile, src.toSource());
	}

	public static void createTests(Module module) {
		try {
			createFixtures(module);
			for(File model : module.findModels()) {
				createModelTests(module, model);
			}
		} catch(Exception e) {
			logger.error(e);
		}
	}

	public static File fixturesFile(Module module) {
		StringBuilder sb = new StringBuilder(module.file.getParentFile().getAbsolutePath()).append(File.separatorChar);
		sb.append(module.name).append(".tests").append(File.separatorChar).append(module.src);
		for(String segment : module.name.split("\\.")) {
			sb.append(File.separatorChar).append(segment);
		}
		sb.append(File.separatorChar).append("models").append(File.separatorChar).append("Fixtures.js");
		return new File(sb.toString());
	}

	private static File genTestFile(Module module, File model) {
		String srcSegment = File.separator + module.src + File.separator;
		StringBuilder sb = new StringBuilder(model.getAbsolutePath());
		sb.insert(sb.indexOf(srcSegment), ".tests");
		sb.insert(sb.length()-5, "ModelTest");
		return new File(sb.toString());
	}
	
	@SuppressWarnings("unchecked")
	private static Class<? extends Model> getClass(Module module, File srcFile) {
		try {
			URLClassLoader ucl = URLClassLoader.newInstance(new URL[] { module.bin.toURI().toURL() });
			try {
				return (Class<? extends Model>) ucl.loadClass(className(module, srcFile));
			} catch(ClassNotFoundException e) {
				// throw away
			}
		} catch(MalformedURLException e) {
			logger.error(e);
		}
		return null;
	}
	
	public static File testFile(Module module, File fileInSrcFolder) {
		String srcSegment = File.separator + module.src + File.separator;
		StringBuilder sb = new StringBuilder(fileInSrcFolder.getAbsolutePath());
		sb.insert(sb.indexOf(srcSegment), ".tests");
		sb.insert(sb.length()-5, "Test");
		return new File(sb.toString());
	}
	
	private static void updateGenTestFile(Class<? extends Model> clazz, File genTestFile) throws Exception {
		Class<?> genClass = clazz.getSuperclass();
		Map<String, String> methods = new HashMap<String, String>();
		for(Method method : genClass.getDeclaredMethods()) {
			int mods = method.getModifiers();
			if((mods & Modifier.PUBLIC) != 0 && (mods & Modifier.ABSTRACT) == 0 && (mods & Modifier.STATIC) == 0) {
				String name = method.getName();
				if(!name.equals("validate") && !name.equals("doValidate")) {
					String testName = "test" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
					methods.put(testName, name);
				}
			}
		}

		boolean writeFile = false;
		ModelAdapter adapter = ModelAdapter.getAdapter(clazz);
		StringBuilder sb = FileUtils.readFile(genTestFile);

		int start = find(sb, 0, sb.length(), regen);
		while(start != -1) {
			int end = find(sb, start+regen.length, sb.length(), '{');
			String str = sb.substring(start, end);
			Pattern p = Pattern.compile(".*public +void +(\\w+) *\\(.*", Pattern.DOTALL);
			Matcher m = p.matcher(str);
			if(m.matches()) {
				end = closer(sb, end);
				if(end != -1) {
					writeFile = true;
					String testMethod = m.group(1);
					if(methods.containsKey(testMethod)) {
						sb.replace(start, end+1, createMethod(adapter, methods.get(testMethod), testMethod, false));
					} else {
						sb.delete(start, end+1);
					}
					methods.remove(testMethod);
				}
			}
			start = find(sb, end+1, sb.length(), regen);
		}

		for(Entry<String, String> entry : methods.entrySet()) {
			String testMethod = entry.getKey();
			Pattern p = Pattern.compile(".*public +void +" + testMethod + " *\\([\\w,]*\\).*\\{.*", Pattern.DOTALL);
			Matcher m = p.matcher(sb);
			if(!m.matches()) {
				writeFile = true;
				int offset = closer(sb, find(sb, 0, sb.length(), '{')) - 1;
				sb.insert(offset++, "\n\n");
				sb.insert(offset, createMethod(adapter, entry.getValue(), entry.getKey(), true));
			}
		}

		if(writeFile) {
			logger.info("updating " + genTestFile);
			writeFile(genTestFile, sb.toString());
		} else {
			logger.info("skipping " + genTestFile);
		}
	}
}
