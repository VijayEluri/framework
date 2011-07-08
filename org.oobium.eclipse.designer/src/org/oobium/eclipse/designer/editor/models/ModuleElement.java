package org.oobium.eclipse.designer.editor.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.build.workspace.Module;

public class ModuleElement extends Element {

	public static final String PROP_MODELS = "Module.Models";
	
	private Module module;
	private Map<String, ModelElement> models;
	
	public ModuleElement(Module module, Map<?,?> modMap) {
		this.module = module;
		this.models = new HashMap<String, ModelElement>();

		Map<?,?> modelsMap = (modMap != null) ? (Map<?,?>) modMap.get("models") : null;
		
		ModelDefinition[] definitions = ModelDefinition.getModelDefinitions(module.findModels());
		for(ModelDefinition definition : definitions) {
			ModelElement model = new ModelElement(this, definition);
			if(modelsMap != null) {
				model.setProperties((Map<?,?>) modelsMap.get(model.getName()));
			}
			addModel(model);
		}
		
		for(ModelElement model : models.values()) {
			for(ModelRelation relation : model.getDefinition().getRelations()) {
				ModelElement sourceModel = model;
				String sourceField = relation.name;
				ModelElement targetModel = models.get(relation.type);
				String targetField = relation.opposite;
				Connection connection = new Connection(sourceModel, sourceField, targetModel, targetField);
				model.addConnection(connection);
			}
		}
	}

	public Module getModule() {
		return module;
	}
	
	public String getName() {
		return module.name;
	}
	
	public void addModel(ModelElement model) {
		models.put(model.getType(), model);
		firePropertyChanged(PROP_MODELS, null, models);
	}
	
	public void removeModel(ModelElement model) {
		models.remove(model.getType());
		firePropertyChanged(PROP_MODELS, null, models);
	}
	
	public Collection<ModelElement> getModels() {
		return getModels(false);
	}
	
	public Collection<ModelElement> getModels(boolean includeDeletedModels) {
		List<ModelElement> list = new ArrayList<ModelElement>(models.values());
		if(!includeDeletedModels) {
			for(Iterator<ModelElement> iter = list.iterator(); iter.hasNext(); ) {
				if(iter.next().isDeleted()) {
					iter.remove();
				}
			}
		}
		return list;
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
