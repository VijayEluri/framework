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
package org.oobium.build.console;

import static org.oobium.client.Client.client;
import static org.oobium.utils.literal.Map;
import static org.oobium.utils.literal.e;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.oobium.build.workspace.Project;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BuilderConsoleActivator implements BundleActivator {

	private static BuilderConsoleActivator instance;
	
	public static void sendImport(Project project) {
		sendImport(project.file);
	}
	
	public static void sendImport(File project) {
		client(instance.port).post("commands/import", Map(e("project", project.getName()), e("file", project.getAbsolutePath())));
	}
	
	public static void sendOpen(Project project, File file) {
		sendOpen(project.file, file);
	}
	
	public static void sendOpen(final File file) {
		if(file.isFile()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Program.launch(file.getAbsolutePath());
				}
			});
		} else if(file.isDirectory()) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						Desktop.getDesktop().open(file);
					} catch(IOException e) {
						e.printStackTrace();
						// oh well...
					}
				}
			});
		}
	}
	
	public static void sendOpen(File project, File file) {
		String projectName = project.getName();
		String filePath = file.getAbsolutePath().substring(project.getAbsolutePath().length());
		client(instance.port).post("commands/open", Map(e("project", projectName), e("file", filePath)));
	}
	
	public static void sendOpenResource(String resource) {
		sendOpenResource(resource, null);
	}
	
	public static void sendOpenResource(String resource, String line) {
		client(instance.port).post("commands/open_resource", Map("resource", resource));
	}
	
	public static void sendOpenType(String type) {
		sendOpenType(type, null);
	}
	
	public static void sendOpenType(String type, String line) {
		client(instance.port).post("commands/open_type", Map(e("type", type), e("line", line)));
	}

	public static void sendRefresh(Project project, long delay) {
		if(project != null) {
			sendRefresh(project.file, delay);
		}
	}

	public static void sendRefresh(Project project, File file, long delay) {
		sendRefresh(project.file, file, delay);
	}
	
	public static void sendRefresh(final File project, final long delay) {
		new Thread("refresh for new model") {
			public void run() {
				try {
					sleep(delay);
					client(instance.port).post("commands/refresh", Map("project", project.getName()));
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	public static void sendRefresh(File project, File file, final long delay) {
		final String projectName = project.getName();
		final String filePath = file.getAbsolutePath().substring(project.getAbsolutePath().length());
		new Thread("refresh for new model") {
			public void run() {
				try {
					sleep(delay);
					client(instance.port).post("commands/refresh", Map(e("project", projectName), e("file", filePath)));
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	
	public static void sendRemove(File project) {
		client(instance.port).post("commands/remove", Map("project", project.getName()));
	}

	
	private int port;
	
	public BuilderConsoleActivator() {
		instance = this;
		port = 3000;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
