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

import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.StringUtils.packageName;
import static org.oobium.utils.StringUtils.plural;
import static org.oobium.utils.StringUtils.repeat;
import static org.oobium.utils.StringUtils.simpleName;
import static org.oobium.utils.StringUtils.titleize;
import static org.oobium.utils.StringUtils.varName;

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.app.controllers.HttpController;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Module;
import org.oobium.app.http.Action;
import org.oobium.app.http.MimeType;

public class ControllerGenerator {

	private static final Matcher[] sigs = new Matcher[7];
	{
		sigs[0] = Pattern.compile(".*(public\\s+void\\s+create\\().*", Pattern.DOTALL).matcher("");
		sigs[2] = Pattern.compile(".*(public\\s+void\\s+update\\().*", Pattern.DOTALL).matcher("");
		sigs[1] = Pattern.compile(".*(public\\s+void\\s+destroy\\().*", Pattern.DOTALL).matcher("");
		sigs[3] = Pattern.compile(".*(public\\s+void\\s+showAll\\().*", Pattern.DOTALL).matcher("");
		sigs[4] = Pattern.compile(".*(public\\s+void\\s+showEdit\\().*", Pattern.DOTALL).matcher("");
		sigs[5] = Pattern.compile(".*(public\\s+void\\s+showNew\\().*", Pattern.DOTALL).matcher("");
		sigs[6] = Pattern.compile(".*(public\\s+void\\s+show\\().*", Pattern.DOTALL).matcher("");
	}
	
	public static File createController(Module module, String name, SourceFile sf) {
		String controller = camelCase((name.endsWith("Controller")) ? name : (name + "Controller"));
		String canonicalName = module.packageName(module.controllers) + "." + controller;
		File appController = new File(module.controllers, "ApplicationController.java");

		SourceFile src = (sf == null) ? new SourceFile() : sf;

		src.packageName = packageName(canonicalName);
		src.simpleName = simpleName(canonicalName);
		if(appController.isFile()) {
			src.superName = "ApplicationController";
		} else {
			src.superName = HttpController.class.getSimpleName();
			src.imports.add(HttpController.class.getCanonicalName());
		}

		if(sf == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("@Override\n");
			sb.append("public void handleRequest() throws Exception {\n");
			sb.append("\t// TODO handle the request\n");
			sb.append("}");
			src.methods.put("show", sb.toString());
		}
		
		return writeFile(module.controllers, controller + ".java", src.toSource());
	}

	public static String generate(Module module, ModelDefinition model) {
		ControllerGenerator gen = new ControllerGenerator(module, model);
		return gen.doGenerate();
	}
 	
	private final Module module;
	private final ModelDefinition model;
	private final boolean extendAppController;
	private final boolean withViews;
	private final String mType;
	private final String mTypePlural;
	private final String varName;
	private final String varNamePlural;

	private ControllerGenerator(Module module, ModelDefinition model) {
		this.module = module;
		this.model = model;
		extendAppController = module.getController("ApplicationController").isFile();
		withViews = !module.hasNature(Module.NATURE_WEBSERVICE);
		mType = model.getSimpleName();
		mTypePlural = plural(mType);
		varName = varName(mType);
		varNamePlural = varName(mType, true);
	}

	private void addActionCreateImports(TreeSet<String> imports) {
		if(withViews) {
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
		}
	}

	private void addActionDestroyImports(TreeSet<String> imports) {
		addImports(imports);
		if(withViews) {
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
		}
	}
	
	private void addActionUpdateImports(TreeSet<String> imports) {
		addImports(imports);
		if(withViews) {
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
		}
	}
	
	private void addImports(int i, TreeSet<String> imports) {
		switch(i) {
		case 0: addActionCreateImports(imports); break;
		case 1: addActionDestroyImports(imports); break;
		case 2: addActionUpdateImports(imports); break;
		case 3: addShowAllViewImports(imports); break;
		case 4: addShowEditViewImports(imports); break;
		case 5: addShowNewViewImports(imports); break;
		case 6: addShowViewImports(imports); break;
		}
	}
	
	private void addImports(TreeSet<String> imports) {
		imports.add(model.getCanonicalName());
	}
	
	private void addShowAllViewImports(TreeSet<String> imports) {
		addImports(imports);
		imports.add(List.class.getCanonicalName());
		if(withViews) {
			imports.add("static " + Action.class.getCanonicalName() + ".*");
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
			imports.add(module.packageName(module.getViewsFolder(mType))+".ShowAll"+mTypePlural);
		}
	}

	private void addShowEditViewImports(TreeSet<String> imports) {
		addImports(imports);
		if(withViews) {
			imports.add("static " + Action.class.getCanonicalName() + ".*");
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
			imports.add(module.packageName(module.getViewsFolder(mType))+".ShowEdit"+mType);
		}
	}

	private void addShowNewViewImports(TreeSet<String> imports) {
		addImports(imports);
		if(withViews) {
			imports.add("static " + Action.class.getCanonicalName() + ".*");
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
			imports.add(module.packageName(module.getViewsFolder(mType))+".ShowNew"+mType);
		}
	}

	private void addShowViewImports(TreeSet<String> imports) {
		addImports(imports);
		if(withViews) {
			imports.add("static " + Action.class.getCanonicalName() + ".*");
			imports.add("static " + MimeType.class.getCanonicalName() + ".*");
			imports.add(module.packageName(module.getViewsFolder(mType))+".Show"+mType);
		}
	}

	private String doGenerate() {
		SourceFile src = new SourceFile();

		String controllerName = model.getControllerName();
		
		src.packageName = module.packageName(module.getController(controllerName).getParentFile());
		src.simpleName = controllerName;
		if(extendAppController) {
			src.superName = "ApplicationController";
		} else {
			src.superName = HttpController.class.getSimpleName();
			src.imports.add(HttpController.class.getCanonicalName());
		}

		for(int i = 0; i < 7; i++) {
			addImports(i, src.imports);
		}
		
		src.methods.put("create", genCreate());
		src.methods.put("update", genUpdate());
		src.methods.put("destroy", genDestroy());
		src.methods.put("showAll", genShowAll());
		if(withViews) {
			src.methods.put("showEdit", genShowEdit());
			src.methods.put("showNew", genShowNew());
		}
		src.methods.put("show", genShow());
		
		return src.toSource();
	}
	
	private String genCreate() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // POST/URL/[models]\n");
		sb.append("public void create() throws Exception {\n");
		sb.append("\t").append(mType).append(" ").append(varName).append(" = ").append("param(\"").append(varName).append("\", new ").append(mType).append("());\n");
		sb.append("\tif(").append(varName).append(".create()) {\n");
		if(!withViews) {
			sb.append("\t\trenderCreated(").append(varName).append(");\n");
		} else {
			sb.append("\t\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\t\tcase JS:\n");
			sb.append("\t\t\tcase JSON: renderCreated(").append(varName).append("); break;\n");
			sb.append("\t\t\tcase HTML: redirectTo(").append(varName).append(", show, \"").append(titleize(mType)).append(" was successfully created.\"); break;\n");
			sb.append("\t\t}\n");
		}
		sb.append("\t} else {\n");
		if(!withViews) {
			sb.append("\t\trenderErrors(").append(varName).append(");\n");
		} else {
			sb.append("\t\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\t\tcase JS:\n");
			sb.append("\t\t\tcase JSON: renderErrors(").append(varName).append("); break;\n");
			sb.append("\t\t\tcase HTML: render(new ShowNew").append(mType).append('(').append(varName).append(")); break;\n");
			sb.append("\t\t}\n");
		}
		sb.append("\t}\n");
		sb.append("}");
		return sb.toString();
	}
	
	private String genDestroy() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // DELETE/URL/[models]/id\n");
		sb.append("public void destroy() throws Exception {\n");
		sb.append("\t").append(mType).append(" ").append(varName).append(" = new ").append(mType).append("().setId(getId());\n");
		sb.append("\tif(").append(varName).append(".destroy()) {\n");
		if(!withViews) {
			sb.append("\t\trenderDestroyed(").append(varName).append(");\n");
		} else {
			sb.append("\t\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\t\tcase JS:\n");
			sb.append("\t\t\tcase JSON: renderDestroyed(").append(varName).append(");     break;\n");
			sb.append("\t\t\tcase HTML: redirectTo(").append(varName).append(", showAll); break;\n");
			sb.append("\t\t}\n");
		}
		sb.append("\t} else {\n");
		sb.append("\t\trenderErrors(").append(varName).append(");\n");
		sb.append("\t}\n");
		sb.append("}");

		return sb.toString();
	}
	
	private String genShow() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // GET/URL/[models]/id\n");
		sb.append("public void show() throws Exception {\n");
		sb.append("\t").append(mType).append(" ").append(varName).append(" = ").append(mType).append(".findById(getId());\n");
		sb.append("\tif(").append(varName).append(" != null) {\n");
		if(!withViews) {
			sb.append("\t\trender(").append(varName).append(");\n");
		} else {
			sb.append("\t\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\t\tcase JS:\n");
			sb.append("\t\t\tcase JSON: render(").append(varName).append(");           ").append(repeat(' ', mType.length())).append("break;\n");
			sb.append("\t\t\tcase HTML: render(new Show").append(mType).append("(").append(varName).append(")); break;\n");
			sb.append("\t\t}\n");
		}
		sb.append("\t}\n");
		sb.append("}");
		return sb.toString();
	}

	private String genShowAll() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // GET/URL/[models]\n");
		sb.append("public void showAll() throws Exception {\n");
		sb.append("\tList<").append(mType).append("> ").append(varNamePlural).append(" = ").append(mType).append(".findAll(getQuery(), getValues());\n");
		sb.append('\n');
		if(!withViews) {
			sb.append("\trender(").append(varNamePlural).append(");\n");
		} else {
			sb.append("\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\tcase JS:\n");
			sb.append("\t\tcase JSON: render(").append(varNamePlural).append("); break;\n");;
			sb.append("\t\tcase HTML: render(new ShowAll").append(mTypePlural).append("(").append(varNamePlural).append(")); break;\n");
			sb.append("\t}\n");
		}
		sb.append("}");
		return sb.toString();
	}

	private String genShowEdit() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // GET/URL/[models]/id/edit\n");
		sb.append("public void showEdit() throws Exception {\n");
		sb.append("\t").append(mType).append(" ").append(varName).append(" = ").append(mType).append(".findById(getId());\n");
		sb.append("\tif(").append(varName).append(" != null) {\n");
		sb.append("\t\trender(new ShowEdit").append(mType).append("(").append(varName).append("));\n");
		sb.append("\t}\n");
		sb.append("}");
		return sb.toString();
	}
	
	private String genShowNew() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // GET/URL/[models]/new\n");
		sb.append("public void showNew() throws Exception {\n");
		sb.append("\t").append(mType).append(" ").append(varName).append(" = new ").append(mType).append("();\n");
		sb.append("\trender(new ShowNew").append(mType).append('(').append(varName).append("));\n");
		sb.append("}");
		return sb.toString();
	}
	
	private String genUpdate() {
		StringBuilder sb = new StringBuilder();
		sb.append("@Override // PUT/URL/[models]/id\n");
		sb.append("public void update() throws Exception {\n");
		sb.append("\t").append(mType).append(" ").append(varName).append(" = ").append("param(\"").append(varName).append("\", new ").append(mType).append("()).setId(getId());\n");
		sb.append("\tif(").append(varName).append(".update()) {\n");
		if(!withViews) {
			sb.append("\t\trenderCreated(").append(varName).append(");\n");
		} else {
			sb.append("\t\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\t\tcase JS:\n");
			sb.append("\t\t\tcase JSON: renderOK(); break;\n");
			sb.append("\t\t\tcase HTML: redirectTo(").append(varName).append(", show, \"").append(titleize(mType)).append(" was successfully updated.\"); break;\n");
			sb.append("\t\t}\n");
		}
		sb.append("\t} else {\n");
		if(!withViews) {
			sb.append("\t\trenderErrors(").append(varName).append(");\n");
		} else {
			sb.append("\t\tswitch(wants(JS, JSON, HTML)) {\n");
			sb.append("\t\t\tcase JS:\n");
			sb.append("\t\t\tcase JSON: renderErrors(").append(varName).append("); break;\n");
			sb.append("\t\t\tcase HTML: render(new ShowEdit").append(mType).append('(').append(varName).append(")); break;\n");
			sb.append("\t\t}\n");
		}
		sb.append("\t}\n");
		sb.append("}");
		return sb.toString();
	}
	
}
