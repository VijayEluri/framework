package org.oobium.eclipse.designer.editor.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oobium.build.workspace.Application;

public class ApplicationElement extends ModuleElement {

	private List<ModuleElement> modules;

	public ApplicationElement(Application application, Map<?,?> appMap) {
		super(application, appMap);
		for(String name : application.getModules()) {
			// TODO
			System.out.println("TODO: models (skipping " + name + ")");
		}
	}
	
	public void addModule(ModuleElement module) {
		if(modules == null) {
			modules = new ArrayList<ModuleElement>();
		}
		modules.add(module);
	}
	
	public void removeModule(ModuleElement module) {
		if(modules != null) {
			if(modules.remove(module)) {
				if(modules.isEmpty()) {
					modules = null;
				}
			}
		}
	}
	
	public List<ModuleElement> getModules() {
		return (modules != null) ? modules : new ArrayList<ModuleElement>(0);
	}
	
}
