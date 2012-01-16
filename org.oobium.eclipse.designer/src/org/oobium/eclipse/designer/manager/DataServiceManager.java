package org.oobium.eclipse.designer.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.DesignerPlugin;
import org.oobium.utils.Config.Mode;

public class DataServiceManager {

	private static final DataServiceManager instance = new DataServiceManager();
	
	public static DataServiceManager instance() {
		return instance;
	}
	
	
	private Map<String, DataService> services;
	
	private DataServiceManager() {
		// private constructor
	}
	
	public String getServiceName(Project client) {
		String name = client.name;
		name = name + ".data";
		return name;
	}
	
	public Application createApplication(String name) {
		Workspace ws = OobiumPlugin.getWorkspace();
		Application app = null;
		
		File appFile = new File(getServiceFolder(), name);
		ws.load(appFile);
		app = ws.getApplication(appFile);

		if(app == null) {
			app = ws.createApplication(appFile, new HashMap<String, String>(0));
		}
		else if(!app.file.exists()) {
			ws.unload(appFile);
			app = ws.createApplication(appFile, new HashMap<String, String>(0));
			ws.load(appFile);
		}
		
		if(!app.migrator.exists()) {
			ws.createMigrator(app);
		}

		return app;
	}
	
	Application getApplication(String name) {
		Workspace ws = OobiumPlugin.getWorkspace();
		File appFile = new File(getServiceFolder(), name);
		ws.load(appFile);
		Application app = ws.getApplication(appFile);
		if(app != null) {
			ws.load(app.migrator);
		}
		return app;
	}

	public Application getApplicationFor(String service) {
		return getApplication(service);
	}
	
	public DataService getService(String name, Mode mode) {
		if(services == null) {
			services = new HashMap<String, DataService>();
		}
		String key = name + ":" + mode.name();
		DataService service = services.get(key);
		if(service == null) {
			services.put(key, service = new DataService(this, name, mode));
		}
		return service;
	}
	
	public List<String> getServices() {
		File folder = getServiceFolder();
		if(folder.isDirectory()) {
			List<String> services = new ArrayList<String>();
			for(String name : folder.list()) {
				if(!name.endsWith(".migrator")) {
					services.add(name);
				}
			}
			return services;
		}
		return new ArrayList<String>(0);
	}

	private File getServiceFolder() {
		return DesignerPlugin.getDefault().getBundle().getBundleContext().getDataFile("oobium_data_services");
	}
	
}
