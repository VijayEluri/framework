package org.oobium.eclipse.designer.editor.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.ui.views.properties.IPropertySource;

public abstract class Element implements IPropertySource {

	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	public void addListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	public void removeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}
	
	protected void firePropertyChanged(String property, Object oldValue, Object newValue) {
		listeners.firePropertyChange(property, oldValue, newValue);
	}
	
	@Override
	public Object getEditableValue() {
		return this;
	}

}
