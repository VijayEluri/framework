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

import java.io.File;
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

	/**
	 * Create the initial migration where the database schema is created from the models.
	 * @param workspace
	 * @param mode
	 * @return a File object pointing to the new migration source file
	 */
	public File createInitialMigration(Workspace workspace, Mode mode) {
		return ModelGenerator.generateSchema(workspace, this, mode);
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
	
	public String getLocation(Mode mode) {
		Config config = loadConfiguration(mode);
		String[] hosts = config.getHosts();
		if(hosts.length > 0) {
			return hosts[0] + ":" + config.getPort();
		}
		return "localhost:" + config.getPort();
	}

	public String[] getLocations(Mode mode) {
		Config config = loadConfiguration(mode);
		String[] hosts = config.getHosts();
		if(hosts.length > 0) {
			int port = config.getPort();
			String[] locations = new String[hosts.length];
			for(int i = 0; i < locations.length; i++) {
				locations[i] = hosts[i] + ":" + port;
			}
			return locations;
		}
		return new String[0];
	}

	public int getPort(Mode mode) {
		return loadConfiguration().getPort(mode);
	}

	public Site getSite() {
		return getSite(Mode.DEV);
	}
	
	public Site getSite(Mode mode) {
		if(hasSite()) {
			Site site = new Site(this);
			site.setMode(mode);
			return site;
		}
		return null;
	}
	
	public boolean hasSite() {
		return site != null && site.isFile();
	}
	
	public String name() {
		return name;
	}
	
	public String toString() {
		return "Application: " + file.getName();
	}

}
