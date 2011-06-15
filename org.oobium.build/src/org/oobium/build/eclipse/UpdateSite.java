package org.oobium.build.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.oobium.build.workspace.Bundle;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;

public class UpdateSite {
	final Workspace workspace;
	final File file;
	final Date date;
	final Map<String, String> features;
	final List<Bundle> plugins;

	UpdateSite(Workspace workspace, String name) {
		this.workspace = workspace;
		
		Project site = workspace.getProject(name);
		if(site == null) {
			throw new IllegalArgumentException("no project '" + name + "' in workspace");
		}
		
		file = new File(site.file, "site.xml");
		if(!file.isFile()) {
			throw new IllegalArgumentException("project '" + name + "' does not contain an site.xml file");
		}

		date = new Date();
		features = new LinkedHashMap<String, String>();
		plugins = new ArrayList<Bundle>();
	}
}