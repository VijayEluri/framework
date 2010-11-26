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
package org.oobium.build.util;

import static org.oobium.utils.CharStreamUtils.findAll;
import static org.oobium.utils.CharStreamUtils.isNext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.oobium.build.BuildBundle;
import org.oobium.logging.Logger;
import org.oobium.persist.ModelDescription;

public class ProjectUtils {

	public static final String BUNDLE_REPOS = "org.oobium.bundle.repos";
	public static final String JAVA_DIR = "org.oobium.java.dir";
	public static final String WORKSPACE = "org.oobium.workspace";
	public static final String RUNTIME = "org.oobium.runtime";

	public static final String RUNTIME_EQUINOX = "equinox";
	public static final String RUNTIME_FELIX = "felix";

	private static final Logger logger = Logger.getLogger(BuildBundle.class);

	private static final String mailerAnnotation = "@Mailer";// + Mailer.class.getSimpleName();
	private static final String modelDescription = "@" + ModelDescription.class.getSimpleName();
	
	
	public static String getGenAnnotations(File file) throws IOException {
		char[] ca = new char[(int) file.length()];

		BufferedReader reader = new BufferedReader(new FileReader(file));
		int c;
		int i = 0;
		while((c = reader.read()) != -1) {
			ca[i++] = (char) c;
		}
		
		i = 3;
		while(i < ca.length) {
			if(ca[i] == '/' && ca[i-1] == '*') {
				return new String(ca, 3, i-4);
			}
			i++;
		}
		
		throw new IllegalStateException("could not find the class annotations in file: " + file);
	}
	
	public static String getSrcAnnotations(File file) throws IOException {
		char[] ca = new char[(int) file.length()];

		BufferedReader reader = new BufferedReader(new FileReader(file));
		int c;
		int i = 0;
		while((c = reader.read()) != -1) {
			ca[i++] = (char) c;
		}

		i = 0;
		while(i < ca.length) {
			switch(ca[i]) {
			case '*':
				if(i != 0 && ca[i-1] == '/') {
					i += 2;
					while(i < ca.length) {
						if(ca[i] == '/' && ca[i-1] == '*') {
							break;
						} else {
							i++;
						}
					}
				}
				break;
			case '/':
				if(i != 0 && ca[i-1] == '/') {
					i += 2;
					while(i < ca.length) {
						if(ca[i] == '\n') {
							break;
						} else {
							i++;
						}
					}
				}
				break;
			case '@':
				int e = findAll(ca, i, "public class ".toCharArray());
				return new String(ca, i, e-i);
			}
			if(isNext(ca, i, "public class ".toCharArray())) {
				throw new IllegalStateException("if there are no class annotation, why are we building a model?!?");
			}
			i++;
		}
		throw new IllegalStateException("if there are no class annotation, why are we building a model?!?");
	}
	
	public static boolean isController(File file) {
		return file.isFile() && file.getName().endsWith("Controller.java");
	}

	public static boolean isView(File file) {
		return file.isFile() && file.getName().endsWith(".esp");
	}

	public static boolean isMailer(File file) {
		if(file.isFile() && file.getName().endsWith(".java")) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while((line = reader.readLine()) != null) {
					if(line.contains(mailerAnnotation)) {
						return true;
					}
				}
			} catch(FileNotFoundException e) {
			} catch(IOException e) {
		    } finally {
		    	if(reader != null) {
			    	try {
			    		reader.close();
					} catch(IOException e) {
					}
		    	}
			}
		}
		return false;
	}
	
	public static boolean isManifest(File file) {
		return file.isFile() && "MANIFEST.MF".equals(file.getName());
	}

	public static boolean isModel(File file) {
		if(file.isFile() && file.getName().endsWith(".java")) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				while((line = reader.readLine()) != null) {
					if(line.contains(modelDescription)) {
						return true;
					}
				}
			} catch(FileNotFoundException e) {
			} catch(IOException e) {
		    } finally {
		    	if(reader != null) {
			    	try {
			    		reader.close();
					} catch(IOException e) {
					}
		    	}
			}
		}
		return false;
	}

	public static boolean isProject(File file) {
		if(file.exists() && file.isDirectory()) {
			File project = new File(file, ".project");
			return project.exists();
		}
		return false;
	}
	
	public static File resourcesPluginsFolder() {
		String path = System.getProperty(WORKSPACE) + File.separator + ".metadata" +
				".plugins" + File.separator + "org.eclipse.core.resources" + File.separator + ".projects";
		return new File(path);
	}

	public static String runtime() {
		return System.getProperty(RUNTIME);
	}

	public static boolean runtimeIsEquinox() {
		return RUNTIME_EQUINOX.equals(runtime());
	}

	public static boolean runtimeIsFelix() {
		return RUNTIME_FELIX.equals(runtime());
	}

}
