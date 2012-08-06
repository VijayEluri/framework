package org.oobium.build.clients;

import static org.oobium.utils.StringUtils.getResourceAsString;
import static org.oobium.utils.StringUtils.tableName;
import static org.oobium.utils.StringUtils.varName;

import java.io.File;
import java.util.List;

import org.oobium.build.gen.ProjectGenerator;
import org.oobium.build.workspace.Application;

public class JQueryClientGenerator {

	private final Application application;
	
	public JQueryClientGenerator(Application application) {
		this.application = application;
	}
	
	public String generate() {
		String template = getResourceAsString(ProjectGenerator.class, "templates/models.js");
		StringBuilder sb = new StringBuilder(template);
		
		List<File> models = application.findModels();
		if(!models.isEmpty()) {
			StringBuilder sb2 = new StringBuilder();
			sb2.append("Model.newInstance = function(type, data) {\n");
			sb2.append("\t\tswitch(type) {\n");
			for(File model : models) {
				String name = application.getModelName(model);
				String pkg = application.packageName(model);
				sb2.append("\t\t\tcase '").append(pkg).append('.').append(name).append("': ");
				sb2.append("return new ").append(name).append("().set(data);\n");
			}
			sb2.append("\t\t}\n");
			sb2.append("\t}");
			int ix = sb.indexOf("<Model.newInstance>");
			sb.replace(ix, ix+19, sb2.toString());
			
			sb.append("\n\n//---APPLICATION SPECIFIC MODELS--------------------");
			for(int i = 0; i < models.size(); i++) {
				if(i != 0) {
					sb.append("\n\n//--------------------------------------------------");
				}
				File model = models.get(i);
				String name = application.getModelName(model);
				String pkg = application.packageName(model);
				sb.append("\n\n");
				sb.append("var ").append(name).append(" = Oobium.Model.extend({\n");
				sb.append("\ttype:   '").append(pkg).append('.').append(name).append("',\n");
				sb.append("\tname:   '").append(varName(name)).append("',\n");
				sb.append("\tmodels: '").append(tableName(name)).append("'\n");
				sb.append("});\n");
				sb.append("\n");
				sb.append(name).append(".create = function(data) {\n");
				sb.append("\treturn new ").append(name).append("().set(data);\n");
				sb.append("};\n");
			}
		}
		
		return sb.toString();
	}

}
