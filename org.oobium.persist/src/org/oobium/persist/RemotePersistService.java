package org.oobium.persist;



public abstract class RemotePersistService implements PersistService {

	protected abstract void addSocketListener();
	
	protected void notifyCreate(Model model) {
		Observer.runAfterCreate(model);
	}
	
	protected void notifyUpdate(Model model, String[] fields) {
		Observer.runAfterUpdate(model);
	}

	protected void notifyDestroy(String className, int id) throws Exception {
		Model model = (Model) Class.forName(className).newInstance();
		model.destroyed = id;
		Observer.runAfterDestroy(model);
	}

	protected abstract void removeSocketListener();

}
