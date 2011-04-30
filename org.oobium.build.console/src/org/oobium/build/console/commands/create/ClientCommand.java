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
package org.oobium.build.console.commands.create;

import static org.oobium.utils.FileUtils.readFile;
import static org.oobium.utils.FileUtils.writeFile;
import static org.oobium.utils.StringUtils.camelCase;
import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.StringUtils.tableName;
import static org.oobium.utils.StringUtils.varName;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oobium.build.console.BuilderCommand;
import org.oobium.build.console.BuilderConsoleActivator;
import org.oobium.build.console.Eclipse;
import org.oobium.build.gen.ProjectGenerator;
import org.oobium.build.workspace.Application;

public class ClientCommand extends BuilderCommand {

	@Override
	public void configure() {
		applicationRequired = true;
	}

	// TODO move this to a generator in the builder project
	private void createScriptClient(Application app) {
		String template = getResourceAsString(ProjectGenerator.class, "templates/models.js");
		StringBuilder sb = new StringBuilder(template);
		
		List<File> models = app.findModels();
		if(!models.isEmpty()) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("Model.newInstance = function(type, data) {\n");
			sb2.append("\tvar model = null;\n");
			sb2.append("\tswitch(type) {\n");
			for(File model : models) {
				String name = app.getModelName(model);
				String pkg = app.packageName(model);
				sb2.append("\t\tcase '").append(pkg).append('.').append(name).append("':\n");
				sb2.append("\t\t\tmodel = new ").append(name).append("();\n");
				sb2.append("\t\t\tbreak;\n");
			}
			sb2.append("\t}\n");
			sb2.append("\tmodel.id = data.id;\n");
			sb2.append("\tdelete data.id;\n");
			sb2.append("\tmodel.data = data;\n");
			sb2.append("\treturn model;\n");
			sb2.append("}");
			int ix = sb.indexOf("<Model.newInstance>");
			sb.replace(ix, ix+19, sb2.toString());
			
			sb.append("\n\n//---APPLICATION SPECIFIC MODELS--------------------");
			for(int i = 0; i < models.size(); i++) {
				if(i != 0) {
					sb.append("\n\n//--------------------------------------------------");
				}
				File model = models.get(i);
				String src = readFile(model).toString();
				String name = app.getModelName(model);
				String pkg = app.packageName(model);
				sb.append("\n\n");
				sb.append("function ").append(name).append("(params) {\n");
				sb.append("\tModel.call(this, params);\n");
				sb.append("\tthis.type = ").append(name).append(".type;\n");
				sb.append("\tthis.plural = ").append(name).append(".plural;\n");
				sb.append("\tthis.varName = ").append(name).append(".varName;\n");
				sb.append("}\n");
				sb.append('\n');
				sb.append("$Extend(").append(name).append(", Model);\n");
				sb.append('\n');
				sb.append(name).append(".type = '").append(pkg).append('.').append(name).append("';\n");
				sb.append(name).append(".plural = '").append(tableName(name)).append("';\n");
				sb.append(name).append(".varName = '").append(varName(name)).append("';\n");
				sb.append('\n');
				sb.append(name).append(".find = function(id, success, error) {\n");
				sb.append("\tModel.find( { 'type': this.type, 'plural': this.plural, 'id': id, 'success': success, 'error': error } );\n");
				sb.append("}\n");
				sb.append('\n');
				sb.append(name).append(".findAll = function(success, error) {\n");
				sb.append("\tModel.findAll( { 'type': this.type, 'plural': this.plural, 'success': success, 'error': error } );\n");
				sb.append("}");
				
				Pattern p = Pattern.compile("@Attribute\\([^\\)]*name\\=\"(\\w+)\"[^\\)]+\\)");
				Matcher m = p.matcher(src);
				while(m.find()) {
					String field = m.group(1);
					sb.append('\n').append('\n');
					sb.append(name).append(".prototype.get").append(camelCase(field)).append(" = function() {\n");
					sb.append("\treturn this.data['").append(field).append("'];\n");
					sb.append("}");
					sb.append('\n').append('\n');
					sb.append(name).append(".prototype.set").append(camelCase(field)).append(" = function(").append(field).append(") {\n");
					sb.append("\tthis.data['").append(field).append("'] = ").append(field).append(";\n");
					sb.append("\treturn this;\n");
					sb.append("}");
				}
				p = Pattern.compile("hasOne\\s*=\\s*\\{([^\\}]+)\\}");
				m = p.matcher(src);
				if(m.find()) {
					String s = m.group(1);
					p = Pattern.compile("@Relation\\([^\\)]*name\\=\"(\\w+)\"[^\\)]+\\)");
					m = p.matcher(s);
					while(m.find()) {
						String field = m.group(1);
						sb.append('\n').append('\n');
						sb.append(name).append(".prototype.get").append(camelCase(field)).append(" = function() {\n");
						sb.append("\treturn this.data['").append(field).append("'];\n");
						sb.append("}");
						sb.append('\n').append('\n');
						sb.append(name).append(".prototype.set").append(camelCase(field)).append(" = function(").append(field).append(") {\n");
						sb.append("\tthis.data['").append(field).append("'] = ").append(field).append(";\n");
						sb.append("}");
					}
				}
				p = Pattern.compile("hasMany\\s*=\\s*\\{([^\\}]+)\\}");
				m = p.matcher(readFile(model).toString());
				if(m.find()) {
					String s = m.group(1);
					p = Pattern.compile("@Relation\\([^\\)]*name\\=\"(\\w+)\"[^\\)]+\\)");
					m = p.matcher(s);
					while(m.find()) {
						String field = m.group(1);
						sb.append('\n').append('\n');
						sb.append(name).append(".prototype.").append(field).append(" = function() {\n");
						sb.append("\tif(!this.data['").append(field).append("']) {;\n");
						sb.append("\t\tthis.data['").append(field).append("'] = [];\n");
						sb.append("\t}\n");
						sb.append("\treturn this.data['").append(field).append("'];\n");
						sb.append("}");
					}
				}
			}
		}
		
		File scripts = new File(app.assets, "scripts");
		File file = writeFile(scripts, "models.js", sb.toString());
		
		sb = new StringBuilder(file.getAbsolutePath());
		sb.setCharAt(app.file.getAbsolutePath().length(), '#');
		console.out.println("created client <a href=\"open file " + sb.toString() + "\">models.js</a>");

		Eclipse.refresh(app.file, scripts);
	}

	@Override
	public void run() {
		Application app = getApplication();
		createScriptClient(app);
	}

}
