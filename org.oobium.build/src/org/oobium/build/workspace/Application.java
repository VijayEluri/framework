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
package org.oobium.build.workspace;

import static org.oobium.utils.Config.SERVER;

import java.io.File;
import java.util.Set;
import java.util.jar.Manifest;

import org.oobium.build.gen.ModelGenerator;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;

public class Application extends Module {

	/**
	 * this application's site configuration file
	 */
	public final File site;
	
	Application(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
		this.site = new File(main, "site.js");
	}

	public Bundle getServer(Workspace workspace, Mode mode) {
		Config configuration = loadConfiguration();
		String fullName = configuration.getString(SERVER, mode);
		return workspace.getBundle(fullName);
	}
	
	@Override
	protected void addDependencies(Workspace workspace, Mode mode, Set<Bundle> dependencies) {
		super.addDependencies(workspace, mode, dependencies);
		
		Config configuration = loadConfiguration();
		
		addDependency(workspace, mode, configuration.getString(SERVER, mode), dependencies);
	}
	
	public File createSchema(Workspace workspace, Mode mode) {
		ModelGenerator.generateSchema(workspace, this, mode);
		return getSchema();
	}

	public Application getExportedBundle(Workspace workspace) {
		return getExportedBundle(workspace, this);
	}

	@SuppressWarnings("unchecked")
	public <T extends Bundle> T getExportedBundle(Workspace workspace, T bundle) {
		Exporter exporter = new Exporter(workspace, this);
		File jar = exporter.getExportedJar(bundle);
		if(jar != null) {
			return (T) Bundle.load(jar);
		}
		return null;
	}
	
	public File getExportedJar(Workspace workspace) {
		return getExportedJar(workspace, this);
	}
	
	public File getExportedJar(Workspace workspace, Bundle bundle) {
		Exporter exporter = new Exporter(workspace, this);
		return exporter.getExportedJar(bundle);
	}
	
	public int getPort(Mode mode) {
		return loadConfiguration().getPort(mode);
	}
	
	public File getSchema() {
		return new File(migrator + File.separator + "generated" + File.separator + migrator.getName().replace('.', File.separatorChar) + File.separator + "migrations", 
				"AbstractCreateDatabase.java");
	}
	
	public String name() {
		return name;
	}
	
	public String toString() {
		return "Application: " + file.getName();
	}

}
