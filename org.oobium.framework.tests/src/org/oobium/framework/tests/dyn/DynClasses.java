package org.oobium.framework.tests.dyn;

import static org.oobium.utils.StringUtils.packageName;
import static org.oobium.utils.StringUtils.simpleName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.oobium.persist.Model;

public class DynClasses {

	private static final DynClasses instance = new DynClasses();
	
	public static DynModel getModel(String packageName, String simpleName) {
		return getModel(packageName + "." + simpleName);
	}
	
	public static DynModel getModel(String fullName) {
		DynModel model = instance.models.get(fullName);
		if(model == null) {
			synchronized(instance.models) {
				model = instance.models.get(fullName);
				if(model == null) {
					model = new DynModel(instance, fullName);
					instance.models.put(fullName, model);
				}
			}
		}
		return model;
	}

	public static String[] getSiblings(DynModel model) {
		String mName = model.getFullName();
		String pkg = packageName(mName);
		List<String> list = new ArrayList<String>();
		for(String sName : instance.models.keySet()) {
			if((pkg == null && sName.indexOf('.') == -1) || pkg.equals(packageName(sName))) {
				if(!sName.equals(mName)) {
					String name = simpleName(sName) + ".java";
					list.add(name);
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public static void reset() {
		// TODO doesn't clear the class enough for Mockito
		instance.clear();
		instance.fileManager = new ClassFileManager(instance.compiler.getStandardFileManager(null, null, null));
	}
	
	
	private final JavaCompiler compiler;
	private final Map<String, DynModel> models;
	private final Set<String> compiled;

	private ClassFileManager fileManager;

	private DynClasses() {
		compiler = ToolProvider.getSystemJavaCompiler();
		models = new HashMap<String, DynModel>();
		compiled = new HashSet<String>();
		fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
	}

	private void clear() {
		models.clear();
		compiled.clear();
		fileManager.clear();
	}
	
	void compile() {
		List<JavaFileObject> files = new ArrayList<JavaFileObject>();
		for(Entry<String, DynModel> entry : models.entrySet()) {
			String fullName = entry.getKey();
			if(!compiled.contains(fullName)) {
				compiled.add(fullName);
				files.add(new CharSequenceJavaFileObject(fullName, entry.getValue().getSource()));
			}
		}
		if(!files.isEmpty()) {
			compiler.getTask(null, fileManager, null, null, null, files).call();
		}
	}

	Object newInstance(String fullName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		compile();
		Object instance = (Model) fileManager.getClassLoader(null).loadClass(fullName).newInstance();
		return instance;
	}
	
	Class<? extends Model> newModelClass(String fullName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		compile();
		Class<?> c = fileManager.getClassLoader(null).loadClass(fullName);
		return c.asSubclass(Model.class);
	}
	
}
