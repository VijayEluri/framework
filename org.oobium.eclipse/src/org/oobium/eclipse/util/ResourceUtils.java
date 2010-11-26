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
package org.oobium.eclipse.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class ResourceUtils {

	public static final String nl = System.getProperty("line.separator");
	
	/**
	 * copy a resource from the given plugin to a file in the given project
	 * @param plugin
	 * @param resource
	 * @param project
	 * @param file
	 * @return the newly created IFile to which the resource was copied (aka the target).
	 */
	public static IFile copy(Object plugin, String resource, IProject project, String file) {
		try {
			IFile ifile = project.getFile(file);
			ifile.create(plugin.getClass().getResourceAsStream(resource), true, null);
			return ifile;
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static IFile createFile(IProject project, String name) {
		return createFile(project, name, null, null);
	}

	public static IFile createFile(IProject project, String name, IProgressMonitor monitor) {
		return createFile(project, name, null, monitor);
	}

	public static IFile createFile(IProject project, String name, String contents) {
		return createFile(project, name, contents, null);
	}

	public static IFile createFile(IProject project, String name, String contents, IProgressMonitor monitor) {
		IFile file = null;
		int ix = name.lastIndexOf('/');
		if(ix > 0) {
			file = createFolder(project, name.substring(0, ix)).getFile(name.substring(ix));
		} else {
			file = project.getFile(name);
		}
		try {
			if(contents != null && contents.length() > 0) {
				InputStream is = new ByteArrayInputStream(contents.getBytes());
				if(!file.exists()) {
					file.create(is, true, monitor);
				} else {
					file.setContents(is, true, false, monitor);
				}
			} else {
				if(!file.exists()){
					InputStream is = new ByteArrayInputStream(new byte[0]);
					file.create(is, true, monitor);
				} else {
					file.touch(monitor);
				}
			}
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static IFolder createFolder(IFolder folder) {
		return createFolder(folder.getProject(), folder.getProjectRelativePath().toString());
	}

	public static IFolder createFolder(IProject project, String path) {
		return createFolder(project, path.split("/"));
	}

	public static IFolder createFolder(IProject project, String...segments) {
		try {
			IFolder folder = project.getFolder(segments[0]);
			if(!folder.exists()) {
				folder.create(true, false, null);
			}
			for(int i = 1; i < segments.length; i++) {
				folder = folder.getFolder(segments[i]);
				if(!folder.exists()) {
					folder.create(true, false, null);
				}
			}
			return folder;
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void format(IFile file) {
		Map<String, String> options = getFormatterOptions();

		// initialize the compiler settings to be able to format 1.5 code
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

		// instanciate the default code formatter with the given options
		final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

		
		StringBuilder builder = new StringBuilder();
		try {
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
		} catch(CoreException e1) {
			e1.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		String source = builder.toString();
		
		TextEdit edit = codeFormatter.format(
				CodeFormatter.K_COMPILATION_UNIT, 
				source, 
				0, 
				source.length(), 
				0,
				System.getProperty("line.separator"));

		if(edit != null) {
			IDocument document = new Document(source);
			try {
				edit.apply(document);
				file.setContents(new ByteArrayInputStream(document.get().getBytes()), true, false, null);
			} catch(MalformedTreeException e) {
				e.printStackTrace();
			} catch(BadLocationException e) {
				e.printStackTrace();
			} catch(CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getControllerPackage(IProject project) {
		return getPackage(project, "controllers");
	}
	
	public static String getLayoutPackage(IProject project) {
		return getPackage(project, "layouts");
	}

	public static String getMigrationPackage(IProject project, int version) {
		return getModelPackage(project) + ".migrations.v_" + version;
	}
	
	public static final int MODEL_FILE = 1;
	public static final int CONTROLLER_FILE = 2;
	public static final int VIEW_FOLDER = 3;
	
	public static String getModelName(IResource resource, int type) {
		String name = resource.getName();
		switch(type) {
			case MODEL_FILE:
				if(name.endsWith(".java")) return name.substring(0, name.length()-5);
				break;
			case CONTROLLER_FILE:
				if(name.endsWith("ActionController.java")) return name.substring(0, name.length()-21);
				if(name.endsWith("ViewController.java")) return name.substring(0, name.length()-19);
				break;
			case VIEW_FOLDER:
//				return StringUtils.getCamelCase(resource.getName());
				return String.valueOf(resource.getName().charAt(0)).toUpperCase() + resource.getName().substring(1);
		}
		return null;
	}
	
	public static String getModelPackage(IProject project) {
		return getPackage(project, "models");
	}
	
	public static int getMigrationVersion(IProject project) {
		int version = 0;
		IFolder folder = project.getFolder(getMigrationPath(project));
		try {
			for(IResource resource : folder.members()) {
				if(resource.getType() == IResource.FOLDER) {
					String name = resource.getName().substring(resource.getName().lastIndexOf("_") + 1);
					try {
						version = Math.max(version, Integer.parseInt(name));
					} catch (NumberFormatException e) {
						// oh well... next.
					}
				}
			}
		} catch(CoreException e) {
		}
		return version;
	}
	
	public static String getViewPackage(IProject project, String name) {
		return getPackage(project, "views.") + name.substring(0,1).toLowerCase() + name.substring(1);
	}
	
	public static String getPackage(IProject project) {
		return project.getName();
	}
	
	public static String getPackage(IProject project, String path) {
		return getPackage(project) + "." + path;
	}
	
	public static String getControllerPath(IProject project) {
		return getSrcPath(project) + "controllers/";
	}

	public static String getControllerPath(IProject project, String fileName) {
		return getControllerPath(project) + fileName;
	}

	public static String getModelPath(IProject project) {
		return getSrcPath(project) + "models/";
	}
	
	public static String getModelPath(IProject project, String fileName) {
		return getModelPath(project) + fileName;
	}

	public static String getMigrationPath(IProject project) {
		return getModelPath(project) + "migrations/";
	}

	public static String getMigrationPath(IProject project, int version) {
		return getMigrationPath(project, version, null);
	}

	public static String getMigrationPath(IProject project, int version, String extension) {
		return getMigrationPath(project) + "v_" + version + 
					((extension != null) ? ("/Migration_" + version + "." + extension) : "/");
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String, String> getFormatterOptions() {
		return DefaultCodeFormatterConstants.getEclipseDefaultSettings();
	}

	public static String getLayoutPath(IProject project) {
		return getViewPath(project) + "layouts/";
	}
	
	public static String getLayoutPath(IProject project, String fileName) {
		return getLayoutPath(project) + fileName;
	}
	
	public static IFile getSrcFile(IProject project, String fileName) {
		return project.getFile(getSrcPath(project, fileName));
	}

	public static ICompilationUnit getSrcCompilationUnit(IProject project, String fileName) {
		return JavaCore.createCompilationUnitFrom(getSrcFile(project, fileName));
	}

	public static String getSrcPath(IProject project) {
		return "src/" + getPackage(project).replace("\\.", "/") + "/";
	}

	public static String getSrcPath(IProject project, String fileName) {
		return getSrcPath(project) + fileName;
	}

	public static String getViewPath(IProject project) {
		return getSrcPath(project) + "views/";
	}

	public static String getViewPath(IProject project, String fileName) {
		return getViewPath(project) + fileName;
	}
}
