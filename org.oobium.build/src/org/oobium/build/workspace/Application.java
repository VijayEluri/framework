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
import java.util.Set;
import java.util.jar.Manifest;

import org.oobium.build.gen.ModelGenerator;
import org.oobium.utils.Config;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.FileUtils;

public class Application extends Module {

	Application(Type type, File file, Manifest manifest) {
		super(type, file, manifest);
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
	 * @param mode the to use for the application's configuration during the export
	 * @param clean if true, the export folder will be cleaned before running this export.
	 * @return a File object for the export folder.
	 * @throws IOException
	 */
	public File export(Workspace workspace, Mode mode) throws IOException {
		Exporter exporter = new Exporter(workspace, this);
		if(mode == Mode.DEV) {
//			Application manager = workspace.getApplication("org.oobium.manager");
			Migrator migrator = workspace.getMigratorFor(this);
			if(migrator != null) {
				exporter.addStart(migrator);
				exporter.addStart(migrator.getMigratorService(workspace, mode));
			}
		}
		exporter.setMode(mode);
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
		File migration = getMigratorFile();
		return new File(migration + File.separator + "generated" + File.separator + migration.getName().replaceAll("\\.", File.separator) + File.separator + "migrations", 
				"AbstractCreateDatabase.java");
	}
	
	public String name() {
		return name;
	}
	
	public String toString() {
		return "Application: " + file.getName();
	}

}
