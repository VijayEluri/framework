package org.oobium.eclipse.designer.editor.models;

import java.io.ByteArrayInputStream;
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
import org.oobium.build.workspace.Site;
import org.oobium.eclipse.OobiumPlugin;
import org.oobium.utils.StringUtils;
import org.oobium.utils.json.JsonUtils;

public class SiteDiagram extends Element {

	public static SiteDiagram load(IFile file) throws CoreException {
		String applicationName = file.getParent().getName();
		Application application = OobiumPlugin.getWorkspace().getApplication(applicationName);
		if(application == null) {
			throw new IllegalArgumentException("no application with the name: " + applicationName);
		}
		
		SiteDiagram diagram = new SiteDiagram();

		String json = StringUtils.getString(file.getContents());
		Map<?,?> data = (Map<?,?>) JsonUtils.toMap(json).get("applications");
		if(data == null) {
			data = new HashMap<String, Object>();
		}
		
		Site site = application.getSite();
		if(site == null) {
			ApplicationElement element = new ApplicationElement(diagram, application, (Map<?,?>) data.get(application.name));
			diagram.addApplication(element);
		} else {
			for(String name : site.getApplications()) {
				application = OobiumPlugin.getWorkspace().getApplication(name);
				if(application == null) {
					throw new IllegalArgumentException("no application with the name: " + name);
				}
				ApplicationElement element = new ApplicationElement(diagram, application, (Map<?,?>) data.get(application.name));
				diagram.addApplication(element);
			}
		}
		
		return diagram;
	}
	
	public static void save(SiteDiagram diagram, IFile dst, IProgressMonitor monitor) throws CoreException {
		Map<String, Object> site = new LinkedHashMap<String, Object>();
		Map<String, Map<String, Object>> applications = new LinkedHashMap<String, Map<String,Object>>();
		
		diagram.firePreCommit(null);

		for(ApplicationElement app : diagram.getApplications()) {
			Map<String, Object> appData = new LinkedHashMap<String, Object>();
			Map<String, Map<String, Object>> models = new LinkedHashMap<String, Map<String,Object>>();
			for(ModelElement model : app.getModels(true)) {
				Map<String, Object> mData = model.commit();
				if(mData != null) {
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

			// TODO only refresh updated models?
			Eclipse.refreshProject(app.getName());
		}
		site.put("applications", applications);
		
		String json = JsonUtils.format(JsonUtils.toJson(site));
		dst.setContents(new ByteArrayInputStream(json.getBytes()), true, false, monitor);

		diagram.firePostCommit(null, null);
	}
	
	
	private List<ApplicationElement> applications;

	private List<ModelCommitListener> commitListeners;
	
	
	public void addCommitListener(ModelCommitListener listener) {
		if(commitListeners == null) {
			commitListeners = new ArrayList<ModelCommitListener>();
			commitListeners.add(listener);
		}
		else if(!commitListeners.contains(listener)) {
			commitListeners.add(listener);
		}
	}

	public void removeCommitListener(ModelCommitListener listener) {
		if(commitListeners != null) {
			commitListeners.remove(listener);
		}
	}
	
	private void firePostCommit(ModelElement model, Map<String, Object> mData) {
		if(commitListeners != null) {
			for(ModelCommitListener l : commitListeners.toArray(new ModelCommitListener[commitListeners.size()])) {
				l.postCommit(model, mData);
			}
		}
	}
	
	private void firePreCommit(ModelElement model) {
		if(commitListeners != null) {
			for(ModelCommitListener l : commitListeners.toArray(new ModelCommitListener[commitListeners.size()])) {
				l.preCommit(model);
			}
		}
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
