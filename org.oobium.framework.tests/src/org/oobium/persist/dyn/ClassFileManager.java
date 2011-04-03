package org.oobium.persist.dyn;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

public class ClassFileManager extends ForwardingJavaFileManager {

	private SecureClassLoader loader;
	private Map<String, JavaClassObject> jclassObjects;

	/**
	 * Will initialize the manager with the specified standard java file manager
	 * 
	 * @param standardManger
	 */
	public ClassFileManager(StandardJavaFileManager standardManager) {
		super(standardManager);
		jclassObjects = new HashMap<String, JavaClassObject>();
		loader = new SecureClassLoader() {
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				if(jclassObjects != null) {
					return super.loadClass(name);
				}
				throw new ClassNotFoundException(name);
			};
			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				if(jclassObjects != null) {
					JavaClassObject jclassObject = jclassObjects.get(name);
					if(jclassObject != null) {
						byte[] b = jclassObject.getBytes();
						return super.defineClass(name, b, 0, b.length);
					}
				}
				throw new ClassNotFoundException(name);
			}
		};
	}

	public void clear() {
		loader = null;
		if(jclassObjects != null) {
			jclassObjects.clear();
			jclassObjects = null;
		}
	}
	
	/**
	 * Will be used by us to get the class loader for our compiled class.
	 * It creates an anonymous class extending the SecureClassLoader which uses the byte code created by the compiler and stored in
	 * the JavaClassObject, and returns the Class for it
	 */
	@Override
	public ClassLoader getClassLoader(Location location) {
		return loader;
	}

	/**
	 * Gives the compiler an instance of the JavaClassObject so that the compiler can write the byte code into it.
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		JavaClassObject jclassObject = new JavaClassObject(className, kind);
		jclassObjects.put(className, jclassObject);
		return jclassObject;
	}

}
