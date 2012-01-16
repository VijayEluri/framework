package org.oobium.eclipse.designer.editor.models;


public interface SiteSaveListener {

	public abstract boolean preSave();
	
	public abstract void postSave();

}
