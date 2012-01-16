/*******************************************************************************
 * Copyright (c) 2011 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.eclipse.designer.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.oobium.build.model.ModelAttribute;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.eclipse.designer.editor.models.ApplicationElement;
import org.oobium.eclipse.designer.editor.models.ModelElement;
import org.oobium.eclipse.designer.editor.models.ModuleElement;
import org.oobium.eclipse.designer.editor.models.SiteElement;
import org.oobium.eclipse.designer.outline.Property.Type;

public class DesignerContentProvider implements ITreeContentProvider, PropertyChangeListener {

	private Comparator<Object> sorter = new Comparator<Object>() {
		private DesignerLabelProvider lp = new DesignerLabelProvider();
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = lp.getText(o1).toLowerCase();
			String s2 = lp.getText(o2).toLowerCase();
			return s1.compareTo(s2);
		}
	};

	
	private TreeViewer viewer;
	private boolean sort;

	public DesignerContentProvider(boolean sort) {
		this.sort = sort;
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer) viewer;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof SiteElement) {
			List<ApplicationElement> apps = ((SiteElement) inputElement).getApplications();
			if(apps.size() == 1) {
				return sorted(apps.get(0).getModels()).toArray();
			} else {
				return sorted(apps).toArray();
			}
		}
		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ApplicationElement) {
			return sorted(((ApplicationElement) parentElement).getModels()).toArray();
		}
		if(parentElement instanceof ModelElement) {
			ModelDefinition model = ((ModelElement) parentElement).getDefinition();
			List<Object> list = new ArrayList<Object>();
			// attributes are currently not sorted since they are usually ordered specifically
			//   maybe add a button later for types/levels of sorting?
			list.addAll(model.getAttributes());
			// relations are sorted since the model editor can not do specific ordering
			list.addAll(sorted(model.getRelations()));
			return list.toArray();
		}
		if(parentElement instanceof ModelAttribute) {
			ModelAttribute a = (ModelAttribute) parentElement;
			List<Property> props = new ArrayList<Property>();
			for(Entry<String, Object> entry : a.getCustomProperties().entrySet()) {
				props.add(new Property(Type.Field, entry));
			}
			if(a.hasValidation()) {
				for(Entry<String, Object> entry : a.getValidation().getCustomProperties().entrySet()) {
					props.add(new Property(Type.Validation, entry));
				}
			}
			return sorted(props.toArray());
		}
		if(parentElement instanceof ModelRelation) {
			ModelRelation r = (ModelRelation) parentElement;
			List<Property> props = new ArrayList<Property>();
			Map<String, Object> properties = r.getCustomProperties();
			properties.remove("opposite"); // displayed in-line
			properties.remove("through"); // displayed in-line
			for(Entry<String, Object> entry : properties.entrySet()) {
				props.add(new Property(Type.Field, entry));
			}
			if(r.hasValidation()) {
				for(Entry<String, Object> entry : r.getValidation().getCustomProperties().entrySet()) {
					props.add(new Property(Type.Validation, entry));
				}
			}
			return sorted(props.toArray());
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof ModuleElement) {
			return ((ModuleElement) element).getSite();
		}
		if(element instanceof ModelElement) {
			return ((ModelElement) element).getModuleElement();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof SiteElement) {
			return ((SiteElement) element).hasApplications();
		}
		if(element instanceof ApplicationElement) {
			return ((ApplicationElement) element).hasModels();
		}
		if(element instanceof ModelElement) {
			ModelDefinition model = ((ModelElement) element).getDefinition();
			return model.hasAttributes() || model.hasRelations();
		}
		if(element instanceof ModelAttribute) {
			ModelAttribute a = (ModelAttribute) element;
			return a.hasValidation() || a.hasCustomProperties();
		}
		if(element instanceof ModelRelation) {
			ModelRelation r = (ModelRelation) element;
			if(r.hasValidation()) {
				return true;
			}
			Map<String, Object> properties = r.getCustomProperties();
			properties.remove("opposite"); // displayed in-line
			properties.remove("through"); // displayed in-line
			return !properties.isEmpty();
		}
		return false;
	}

	public void setSort(boolean sort) {
		if(this.sort != sort) {
			this.sort = sort;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	private <T> List<T> sorted(List<T> list) {
		if(sort) {
			Collections.sort(list, sorter);
		}
		return list;
	}

	private Object[] sorted(Object[] array) {
		if(sort) {
			Arrays.sort(array, sorter);
		}
		return array;
	}

}
