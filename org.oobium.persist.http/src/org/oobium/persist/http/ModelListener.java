package org.oobium.persist.http;

import org.oobium.persist.Model;

public abstract class ModelListener<T extends Model> {

	@SuppressWarnings("unchecked")
	void notifyCreate(Model model) {
		onCreate((T) model);
	}
	
	@SuppressWarnings("unchecked")
	void notifyUpdate(Model model) {
		onUpdate((T) model);
	}
	
	@SuppressWarnings("unchecked")
	void notifyDestroy(Model model) {
		onDestroy((T) model);
	}
	
	public abstract void onCreate(T model);
	
	public abstract void onUpdate(T model);
	
	public abstract void onDestroy(T model);
	
}
