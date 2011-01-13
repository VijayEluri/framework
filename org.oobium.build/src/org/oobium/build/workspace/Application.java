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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.oobium.build.gen.ModelGenerator;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.FileUtils;

public class Application extends Module {

	/**
	 * this application's site configuration file
	 */
	public final File site;
	
	Application(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
		this.site = new File(main, "site.js");
	}

	@Override
	protected void addDependencies(Workspace workspace, Mode mode, Set<Bundle> dependencies) {
		super.addDependencies(workspace, mode, dependencies);
		
		Config configuration = loadConfiguration();
		
		addDependency(workspace, mode, configuration.getString(SERVER, mode), dependencies);
	}
	
	public File cleanExport(Workspace workspace) {
		Exporter exporter = new Exporter(workspace, this);
		File exportDir = exporter.getExportDir();
		FileUtils.deleteContents(exportDir);
		return exportDir;
	}

	public File createSchema(Workspace workspace, Mode mode) {
		ModelGenerator.generateSchema(workspace, this, mode);
		return getSchema();
	}
	
	/**
	 * Export the application, configured for the given mode.
	 * @param workspace the workspace to use
	 * @param mode the to use for the application's configuration during the export
	 * @param includeMigrators if true Migrator projects will also be exported with the Application
	 * @return a File object for the export folder.
	 * @throws IOException
	 */
	public File export(Workspace workspace, Mode mode, boolean includeMigrators) throws IOException {
		return export(workspace, mode, includeMigrators, null);
	}
	
	/**
	 * Export the application, configured for the given mode.
	 * @param workspace the workspace to use
	 * @param mode the to use for the application's configuration during the export
	 * @param includeMigrators if true Migrator projects will also be exported with the Application
	 * @param properties a Map of system properties; will be exported to the system.properties file
	 * @return a File object for the export folder.
	 * @throws IOException
	 */
	public File export(Workspace workspace, Mode mode, boolean includeMigrators, Map<String, String> properties) throws IOException {
		Exporter exporter = new Exporter(workspace, getExportApps(workspace));
		exporter.setMode(mode);
		exporter.setProperties(properties);
		if(includeMigrators) {
			Migrator migrator = workspace.getMigratorFor(this);
			if(migrator != null) {
				exporter.addStart(migrator);
				exporter.addStart(migrator.getMigratorService(workspace, mode));
			}
		}
		return exporter.export();
	}

	public Bundle export(Workspace workspace, Mode mode, Bundle bundle) throws IOException {
		Exporter exporter = new Exporter(workspace, this);
		return exporter.export(bundle);
	}
	
	public Set<Bundle> exportMigration(Workspace workspace, Mode mode) throws IOException {
		Exporter exporter = new Exporter(workspace, this);
		return exporter.exportMigration(mode);
	}
	
	private List<Application> getExportApps(Workspace workspace) {
		List<Application> apps = new ArrayList<Application>();
		
		apps.add(this);
		
		Config config = Config.loadConfiguration(site);
		Object o = config.get("apps");
		if(o instanceof String) {
			Application app = workspace.getApplication((String) o);
			if(app == null) {
				throw new IllegalStateException(this + " has an unresolved export requirement: " + o);
			}
			apps.add(app);
		} else if(o instanceof Collection) {
			for(Object e : (Collection<?>) o) {
				if(e instanceof String) {
					Application app = workspace.getApplication((String) e);
					if(app == null) {
						throw new IllegalStateException(this + " has an unresolved export requirement: " + e);
					}
					apps.add(app);
				} else {
					throw new IllegalArgumentException(this + " has an unknown type of export requirement: " + e);
				}
			}
		}
		
		return apps;
	}
	
	public Application getExportedBundle(Workspace workspace) {
		return getExportedBundle(workspace, this);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Bundle> T getExportedBundle(Workspace workspace, T bundle) {
		Exporter exporter = new Exporter(workspace, this);
		File jar = exporter.getExportedJar(bundle);
		if(jar != null) {
			return (T) Bundle.create(jar);
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
