package org.oobium.framework.tests.dyn;

import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public class SimpleDynClass {

	private static final SimpleDynClass instance = new SimpleDynClass();
	
	public static Class<?> getClass(String packageName, String simpleName, CharSequence source) throws ClassNotFoundException {
		return getClass(packageName + "." + simpleName, source);
	}
	
	public static Class<?> getClass(String fullName, CharSequence source) throws ClassNotFoundException {
		return instance.newClass(fullName, source);
	}

	
	private final JavaCompiler compiler;
	private final ClassFileManager fileManager;

	private SimpleDynClass() {
		compiler = ToolProvider.getSystemJavaCompiler();
		fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
	}
	
	private Class<?> newClass(String fullName, CharSequence source) throws ClassNotFoundException {
		List<JavaFileObject> files = new ArrayList<JavaFileObject>();
		files.add(new CharSequenceJavaFileObject(fullName, source));
		compiler.getTask(null, fileManager, null, null, null, files).call();
		return fileManager.getClassLoader(null).loadClass(fullName);
	}
	
}
