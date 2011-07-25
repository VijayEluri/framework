package org.oobium.build.workspace;

import java.io.File;
import java.util.jar.Manifest;

public class JavaApp extends Project {

	public static final String NATURE = "org.eclipse.jdt.core.javanature";
	
	JavaApp(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
	}

}
