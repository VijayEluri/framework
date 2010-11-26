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
package org.oobium.eclipse.projects;

import static org.oobium.eclipse.util.ResourceUtils.createFile;
import static org.oobium.eclipse.util.ResourceUtils.createFolder;
import static org.oobium.eclipse.util.ResourceUtils.nl;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.oobium.build.gen.ViewGenerator;
import org.oobium.eclipse.OobiumNature;
import org.oobium.eclipse.workspace.OobiumBuilder;

public class ProjectGenerator {

	//	private static void createAptPrefsFile(IProject project, IProgressMonitor monitor) {
	//		String src = "eclipse.preferences.version=1" + nl +
	//					 "org.eclipse.jdt.apt.aptEnabled=true" + nl +
	//					 "org.eclipse.jdt.apt.genSrcDir=generated" + nl +
	//					 "org.eclipse.jdt.apt.reconcileEnabled=true";
	//		createFile(project, ".settings/org.eclipse.jdt.apt.core.prefs", src, monitor);
	//	}

	private static void createJdtPrefsFile(IProject project, IProgressMonitor monitor) {
		String src = "eclipse.preferences.version=1" + nl +
		"org.eclipse.jdt.core.builder.resourceCopyExclusionFilter=*.launch,*.esp";
		createFile(project, ".settings/org.eclipse.jdt.core.prefs", src, monitor);
	}

	private static void createBuildFile(IProject project, IProgressMonitor monitor) {
		String src = "source.. = src/,\\" + nl +
		"           assets/,\\" + nl +
		"           generated/" + nl +
		"output.. = bin/" + nl + 
		"bin.includes = META-INF/,\\" + nl + 
		".";
		createFile(project, "build.properties", src, monitor);
	}

	private static void createManifestFile(IProject project, IProgressMonitor monitor) {
		String src = "Manifest-Version: 1.0" + nl +
		"Main-Class: test1.Site" + nl +
		"Class-Path: org.oobium.server_1.0.0.jar" + nl +
		"Bundle-ManifestVersion: 2" + nl +
		"Bundle-Name: " + project.getName() + " Plug-in" + nl +
		"Bundle-SymbolicName: " + project.getName() + nl +
		"Bundle-Version: 1.0.0" + nl +
		"Require-Bundle: org.oobium.server" + nl +
		nl;
		createFile(project, "META-INF/MANIFEST.MF", src, monitor);
	}

	public static IProject createPluginProject(String projectName, IProgressMonitor monitor) {
		monitor.beginTask("Creates new module project.", 50);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = root.getProject(projectName);

		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());

		ICommand[] buildSpec = new ICommand[6];
		for(int i = 0; i < buildSpec.length; i++) {
			buildSpec[i] = description.newCommand();
		}
		buildSpec[0].setBuilderName(OobiumBuilder.ID);
		buildSpec[2].setBuilderName("org.oobium.jml.JmlBuilder");
		buildSpec[3].setBuilderName("org.eclipse.jdt.core.javabuilder");
		buildSpec[4].setBuilderName("org.eclipse.pde.ManifestBuilder");
		buildSpec[5].setBuilderName("org.eclipse.pde.SchemaBuilder");

		description.setBuildSpec(buildSpec);

		description.setNatureIds(new String[] {
				OobiumNature.ID,
				"org.eclipse.pde.PluginNature",
				"org.eclipse.jdt.core.javanature"
		});

		try {
			project.create(description, monitor);
			project.open(monitor);

			String main = "src/" + project.getName().replace('.', '/');

			IFolder src = createFolder(project, "src");
			createFolder(project, main + "/controllers");
			createFolder(project, main + "/models");
			createFolder(project, main + "/views");
			IFolder assets = createFolder(project, "assets");
			IFolder generated = createFolder(project, "generated");
			createFolder(project, "assets/i18n");
			createFolder(project, "assets/scripts");
			createFolder(project, "assets/styles");
			createFolder(project, "assets/images");
			createFolder(project, "assets.src/images");
			createFolder(project, "dist");
			createFolder(project, "lib");
			createFolder(project, ".settings");

			createBuildFile(project, monitor);
			createManifestFile(project, monitor);
			createJdtPrefsFile(project, monitor);

			IJavaProject jproject = JavaCore.create(project);
			IClasspathEntry[] entries = new IClasspathEntry[] {
					JavaCore.newSourceEntry(src.getFullPath()),
					JavaCore.newSourceEntry(assets.getFullPath()),
					JavaCore.newSourceEntry(generated.getFullPath()),
					JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER")),
					JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"))
			};
			jproject.setRawClasspath(entries, monitor);
			jproject.open(monitor);

			monitor.worked(10);
			return project;
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static IProject createServerProject(String projectName, IProgressMonitor monitor) {
		monitor.beginTask("Creates new project.", 50);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = root.getProject(projectName);

		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());

		ICommand[] buildSpec = new ICommand[6];
		for(int i = 0; i < buildSpec.length; i++) {
			buildSpec[i] = description.newCommand();
		}
		buildSpec[0].setBuilderName(OobiumBuilder.ID);
//		buildSpec[1].setBuilderName("org.oobium.modeler.ModelerBuilder");
		buildSpec[2].setBuilderName("org.oobium.jml.JmlBuilder");
		buildSpec[3].setBuilderName("org.eclipse.jdt.core.javabuilder");
		buildSpec[4].setBuilderName("org.eclipse.pde.ManifestBuilder");
		buildSpec[5].setBuilderName("org.eclipse.pde.SchemaBuilder");

		description.setBuildSpec(buildSpec);

		description.setNatureIds(new String[] {
				OobiumNature.ID,
				"org.eclipse.pde.PluginNature",
				"org.eclipse.jdt.core.javanature"
		});

		try {
			project.create(description, monitor);
			project.open(monitor);

			String main = "src/" + project.getName().replace('.', '/');

			IFolder src = createFolder(project, "src");
			createFolder(project, main + "/controllers");
			createFolder(project, main + "/models");
			createFolder(project, main + "/views");
			createFolder(project, main + "/views/layouts");
			IFolder assets = createFolder(project, "assets");
			IFolder generated = createFolder(project, "generated");
			createFolder(project, "assets/i18n");
			createFolder(project, "assets/scripts");
			createFolder(project, "assets/styles");
			createFolder(project, "assets/images");
			createFolder(project, "assets.src/images");
			createFolder(project, "dist");
			createFolder(project, "lib");
			createFolder(project, ".settings");

			createBuildFile(project, monitor);
			createManifestFile(project, monitor);
			createJdtPrefsFile(project, monitor);

			createFile(project, main + "/views/layouts/application.esp", new ViewGenerator(project.getName()).generateLayout(), monitor);

			IJavaProject jproject = JavaCore.create(project);
			IClasspathEntry[] entries = new IClasspathEntry[] {
					JavaCore.newSourceEntry(src.getFullPath()),
					JavaCore.newSourceEntry(assets.getFullPath()),
					JavaCore.newSourceEntry(generated.getFullPath()),
					JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER")),
					JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"))
			};
			jproject.setRawClasspath(entries, monitor);
			jproject.open(monitor);

			monitor.worked(10);
			return project;
		} catch(CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

}
