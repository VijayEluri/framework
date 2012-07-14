package org.oobium.eclipse.designer.editor.models;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.oobium.build.console.Eclipse;
import org.oobium.build.workspace.Application;
import org.oobium.build.workspace.Project;
import org.oobium.build.workspace.Site;
import org.oobium.build.workspace.Workspace;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.eclipse.designer.manager.DataService;
import org.oobium.eclipse.designer.manager.DataServiceManager;
import org.oobium.utils.Config.Mode;
import org.oobium.utils.StringUtils;
import org.oobium.utils.json.JsonUtils;

public class SiteElement extends Element {

	public static SiteElement load(IFile file) throws CoreException {
		String json = StringUtils.getString(file.getContents());
		Map<String, Object> data = JsonUtils.toMap(json);
		if(data == null) {
			data = new HashMap<String, Object>();
		}

		Application application;
		String serviceName = (String) data.get("service");
		if(serviceName != null) {
			application = DataServiceManager.instance().getApplicationFor(serviceName);
		} else {
			Workspace workspace = OobiumPlugin.getWorkspace();
			application = workspace.getApplication(file.getParent().getName());
		}
		
		SiteElement diagram = new SiteElement(serviceName);
		if(application != null) {
			load(diagram, application, data);
		}
		return diagram;
	}

	private static void load(SiteElement diagram, Application application, Map<String, Object> data) {
		Map<?,?> applications = (Map<?,?>) data.get("applications");
		if(applications == null) {
			applications = new HashMap<String, Object>();
		}
		
		Site site = application.getSite();
		if(site == null) {
			ApplicationElement element = new ApplicationElement(diagram, application, (Map<?,?>) applications.get(application.name));
			diagram.addApplication(element);
		} else {
			for(String name : site.getApplications()) {
				application = OobiumPlugin.getWorkspace().getApplication(name);
				if(application == null) {
					throw new IllegalArgumentException("no application with the name: " + name);
				}
				ApplicationElement element = new ApplicationElement(diagram, application, (Map<?,?>) applications.get(application.name));
				diagram.addApplication(element);
			}
		}
	}
	
	public static void save(SiteElement diagram, IFile dst, IProgressMonitor monitor) throws CoreException {
		boolean recompile = diagram.firePreSave(); // ModelDeleteCommands will run here
		
		Map<String, Object> site = new LinkedHashMap<String, Object>();
		site.put("service", diagram.service);
		
		Map<String, Map<String, Object>> applications = new LinkedHashMap<String, Map<String,Object>>();
		
		for(ApplicationElement app : diagram.getApplications()) {
			Map<String, Object> appData = new LinkedHashMap<String, Object>();
			Map<String, Map<String, Object>> models = new LinkedHashMap<String, Map<String,Object>>();
			for(ModelElement model : app.getModels()) {
				Map<String, Object> mData = model.save();
				if(mData != null) {
					if(coerce(mData.remove("recompile")).to(boolean.class)) {
						recompile = true;
					}
					models.put(model.getName(), mData);
				}
			}
			if(!models.isEmpty()) {
				appData.put("models", models);
			}
			// TODO modules
			if(!appData.isEmpty()) {
				applications.put(app.getName(), appData);
			}
		}
		site.put("applications", applications);
		
		String json = JsonUtils.format(JsonUtils.toJson(site));
		dst.setContents(new ByteArrayInputStream(json.getBytes()), true, false, monitor);

		Workspace workspace = OobiumPlugin.getWorkspace();
		File file = dst.getProject().getLocation().toFile();
		Project target = workspace.getProject(file);

		if(recompile && diagram.service != null) {
			DataService service = DataServiceManager.instance().getService(diagram.service, Mode.DEV);
			service.export(target);
		}

		Eclipse.refreshProject(target.name);
		
		diagram.firePostSave();
	}
	

	private final String service;
	private List<ApplicationElement> applications;
	private List<SiteSaveListener> commitListeners;
	

	public SiteElement(String source) {
		this.service = source;
	}
	
	public void addCommitListener(SiteSaveListener listener) {
		if(commitListeners == null) {
			commitListeners = new ArrayList<SiteSaveListener>();
			commitListeners.add(listener);
		}
		else if(!commitListeners.contains(listener)) {
			commitListeners.add(listener);
		}
	}

	public void removeCommitListener(SiteSaveListener listener) {
		if(commitListeners != null) {
			commitListeners.remove(listener);
		}
	}
	
	private void firePostSave() {
		if(commitListeners != null) {
			for(SiteSaveListener l : commitListeners.toArray(new SiteSaveListener[commitListeners.size()])) {
				l.postSave();
			}
		}
	}
	
	private boolean firePreSave() {
		boolean result = false;
		if(commitListeners != null) {
			for(SiteSaveListener l : commitListeners.toArray(new SiteSaveListener[commitListeners.size()])) {
				if(l.preSave()) {
					result = true;
				}
			}
		}
		return result;
	}

	public void addApplication(ApplicationElement application) {
		if(applications == null) {
			applications = new ArrayList<ApplicationElement>();
		}
		applications.add(application);
	}
	
	public List<ApplicationElement> getApplications() {
		return (applications != null) ? applications : new ArrayList<ApplicationElement>(0);
	}
	
	public boolean hasApplications() {
		return applications != null && !applications.isEmpty();
	}
	
	public void removeApplication(ApplicationElement application) {
		if(applications != null) {
			if(applications.remove(application)) {
				if(applications.isEmpty()) {
					applications = null;
				}
			}
		}
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyValue(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		// TODO Auto-generated method stub

	}

}
