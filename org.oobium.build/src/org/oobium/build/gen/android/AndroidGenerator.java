package org.oobium.build.gen.android;

import static org.oobium.utils.FileUtils.*;
import static org.oobium.utils.StringUtils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.oobium.build.gen.android.GeneratorEvent.Type;
import org.oobium.build.workspace.AndroidApp;
import org.oobium.build.workspace.Application;

public class AndroidGenerator {

	private Application application;
	private AndroidApp android;

	private GeneratorListener listener;
	
	public AndroidGenerator(Application app, AndroidApp androidApp) {
		this.android = androidApp;
		this.application = app;
	}

	private void notify(Object data) {
		if(listener != null) {
			GeneratorEvent event = new GeneratorEvent(Type.Logging, data);
			listener.handleEvent(event);
		}
	}
	
	public void setListener(GeneratorListener listener) {
		this.listener = listener;
	}
	
	public List<File> generateScaffolding() {
		List<File> files = new ArrayList<File>();
		try {
			for(File model : application.findModels()) {
				notify("  generating scaffolding for " + application.getModelName(model));
				files.addAll(scaffold(model));
			}
			notify("  adding Internet permission to Android manifest file");
			android.addPermission("android.permission.INTERNET");
			
			if(application.addDiscoveryRoute("/api", true)) {
				files.add(application.activator);
			}
			
			files.add(writeFile(android.src, "oobium.server", "10.0.2.2:5000"));
		} catch(IOException e) {
			notify(e);
		}
		return files;
	}
	
	private List<File> scaffold(File model) throws IOException {
		List<File> files = new ArrayList<File>();
		
		String projectPackage = android.packageName(android.main);
		String modelPackage = application.packageName(model);
		String modelName = application.getModelName(model);
		String modelsName = plural(modelName);
		String modelVar = varName(modelName);
		String modelsVar = varName(modelName, true);

		ModelParser parser = new ModelParser(model);
		List<String[]> attrs = parser.getAttributes();
		
		StringBuilder sb = new StringBuilder();
		for(String[] attr : attrs) {
			sb.append("\t\tString ").append(attr[1]).append(" = ((TextView) findViewById(R.id.").append(attr[1]).append(")).getText().toString();\n");
			sb.append("\t\t").append(modelVar).append(".set").append(titleize(attr[1])).append('(').append(attr[1]).append(");\n");
		}
		String setModelFields = sb.delete(0, 2).deleteCharAt(sb.length()-1).toString();
		
		sb = new StringBuilder();
		for(String[] attr : attrs) {
			sb.append("\t\t((EditText) view.findViewById(R.id.").append(attr[1]).append(")).setText(\"");
			sb.append(titleize(attr[1])).append(": \" + ").append(modelVar).append(".").append(getterName(attr[1])).append("());\n");
		}
		String setViewEditFields = sb.delete(0, 2).deleteCharAt(sb.length()-1).toString();

		sb = new StringBuilder();
		for(String[] attr : attrs) {
			sb.append("\t\t((TextView) view.findViewById(R.id.").append(attr[1]).append(")).setText(\"");
			sb.append(titleize(attr[1])).append(": \" + ").append(modelVar).append(".").append(getterName(attr[1])).append("());\n");
		}
		String setViewFields = sb.delete(0, 2).deleteCharAt(sb.length()-1).toString();

		File dstFolder = new File(android.main, "activities" + File.separator + modelsVar);

		String[] resources = new String[] {
				"src/ModelAdapter.java.android",
				"src/ShowModel.java.android",
				"src/ShowAllModels.java.android",
				"src/ShowEditModel.java.android",
				"src/ShowNewModel.java.android",
		};

		for(String resource : resources) {
			String src = getResourceAsString(getClass(), resource);
			src = src.replace("{projectPackage}", projectPackage);
			src = src.replace("{modelPackage}", modelPackage);
			src = src.replace("{modelName}", modelName);
			src = src.replace("{modelsName}", modelsName);
			src = src.replace("{modelVar}", modelVar);
			src = src.replace("{modelsVar}", modelsVar);
			src = src.replace("{setModelFields}", setModelFields);
			if(resource.contains("Edit")) {
				src = src.replace("{setViewFields}", setViewEditFields);
			} else {
				src = src.replace("{setViewFields}", setViewFields);
			}
			if(resource.contains("Models")) {
				resource = resource.replace("Models", modelsName);
			} else {
				resource = resource.replace("Model", modelName);
			}
			files.add(writeFile(dstFolder, resource.substring(4, resource.lastIndexOf('.')), src));
		}
		
		// add image resources
		dstFolder = new File(android.file, "res" + File.separator + "drawable");
		if(!dstFolder.exists()) {
			dstFolder.mkdirs();
		}
		
		File dst = new File(dstFolder, "destroy.gif");
		if(!dst.isFile()) {
			InputStream in = getClass().getResourceAsStream("res/destroy.gif");
			files.add(copy(in, dst));
		}
		
		dst = new File(dstFolder, "edit.png");
		if(!dst.isFile()) {
			InputStream in = getClass().getResourceAsStream("res/edit.png");
			files.add(copy(in, dst));
		}
		
		// add layout resources
		dstFolder = new File(android.file, "res" + File.separator + "layout");
		if(!dstFolder.exists()) {
			dstFolder.mkdirs();
		}
		
		int indent = 3;
		sb = new StringBuilder();
		for(String[] attr : attrs) {
			if("Text".equals(attr[0])) {
				sb.append(getEditText(attr[1], indent, true));
			} else if("String".equals(attr[0])) {
				sb.append(getEditText(attr[1], indent, false));
			}
		}
		String editFields = sb.delete(0, indent).deleteCharAt(sb.length()-1).toString();

		sb = new StringBuilder();
		for(String[] attr : attrs) {
			sb.append(getInlineField(attr[1], indent));
		}
		String inlineFields = sb.delete(0, indent).deleteCharAt(sb.length()-1).toString();

		sb = new StringBuilder();
		for(String[] attr : attrs) {
			sb.append(getStackedField(attr[1], indent));
		}
		String stackedFields = sb.delete(0, indent).deleteCharAt(sb.length()-1).toString();

		resources = new String[] {
				"res/model_item.xml",
				"res/show_model.xml",
				"res/show_all_models.xml",
				"res/show_edit_model.xml",
				"res/show_new_model.xml",
		};

		for(String resource : resources) {
			String src = getResourceAsString(getClass(), resource);
			src = src.replace("{modelName}", modelName);
			src = src.replace("{modelsName}", modelsName);
			src = src.replace("{editFields}", editFields);
			src = src.replace("{inlineFields}", inlineFields);
			src = src.replace("{stackedFields}", stackedFields);
			if(resource.contains("models")) {
				resource = resource.replace("models", underscored(modelsVar));
			} else {
				resource = resource.replace("model", underscored(modelVar));
			}
			files.add(writeFile(dstFolder, resource.substring(4), src));
		}

		// add activities to manifest file
		notify("  adding activities to Android manifest file");
		android.addActivity(".activities." + modelsVar + ".Show" + modelName);
		android.addActivity(".activities." + modelsVar + ".ShowAll" + modelsName);
		android.addActivity(".activities." + modelsVar + ".ShowEdit" + modelName);
		android.addActivity(".activities." + modelsVar + ".ShowNew" + modelName);
		files.add(android.manifest);
		
		return files;
	}

	private static String editText = 
		"{i}<EditText\n" +
		"{i}\tandroid:id=\"@+id/{field}\"\n" +
		"{i}\tandroid:hint=\"{Field}\"\n" +
		"{i}\tandroid:layout_width=\"fill_parent\"\n" +
		"{i}\tandroid:layout_height=\"wrap_content\" />\n";

	private static String multilineEditText = 
		"{i}<EditText\n" +
		"{i}\tandroid:id=\"@+id/{field}\"\n" +
		"{i}\tandroid:hint=\"{Field}\"\n" +
		"{i}\tandroid:inputType=\"textMultiLine\"\n" +
		"{i}\tandroid:lines=\"5\"\n" +
		"{i}\tandroid:gravity=\"top\"\n" +
		"{i}\tandroid:layout_width=\"fill_parent\"\n" +
		"{i}\tandroid:layout_height=\"wrap_content\" />\n";

	private static String inlineField = 
		"{i}<TextView\n" +
		"{i}\tandroid:id=\"@+id/{field}\"\n" +
		"{i}\tandroid:layout_width=\"wrap_content\"\n" +
		"{i}\tandroid:layout_height=\"wrap_content\"\n" +
		"{i}\tandroid:gravity=\"center_vertical\" />\n";

	private static String stackedField =
		"{i}<TextView\n" +
		"{i}\tandroid:text=\"{Field}:\"\n" +
		"{i}\tandroid:layout_width=\"wrap_content\"\n" +
		"{i}\tandroid:layout_height=\"wrap_content\" />\n" +
		"{i}<TextView\n" +
		"{i}\tandroid:id=\"@+id/{field}\"\n" +
		"{i}\tandroid:paddingLeft=\"5dip\"\n" +
		"{i}\tandroid:layout_width=\"wrap_content\"\n" +
		"{i}\tandroid:layout_height=\"wrap_content\" />\n";

	private String getInlineField(String field, int indent) {
		return inlineField.replace("{i}", repeat('\t', indent)).replace("{field}", field);
	}
	
	private String getStackedField(String field, int indent) {
		return stackedField.replace("{i}", repeat('\t', indent)).replace("{field}", field).replace("{Field}", titleize(field));
	}
	
	private String getEditText(String field, int indent, boolean multiline) {
		if(multiline) {
			return multilineEditText.replace("{i}", repeat('\t', indent)).replace("{field}", field).replace("{Field}", titleize(field));
		}
		return editText.replace("{i}", repeat('\t', indent)).replace("{field}", field).replace("{Field}", titleize(field));
	}
	
}
