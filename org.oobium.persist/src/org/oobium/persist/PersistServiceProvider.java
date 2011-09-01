package org.oobium.persist;

public interface PersistServiceProvider {

	public abstract void closeSession();

	public abstract PersistService get(String serviceName);
	
	/**
	 * Get the PersistService for the given class, or the primary PersistService
	 * if the given class does not map to any PersistService.<br>
	 * PersistServices are keyed off the class's name, so this method is functionally equivalent
	 * to calling get(clazz.getName()).
	 * @param clazz the class whose PersistService is being retrieved.  Can be null.
	 * @return the PersistService for the given class, if one exists; the primary PersistService otherwise, never null.
	 * @see PersistServices#getFor(String)
	 */
	public abstract PersistService getFor(Class<? extends Model> clazz);

	/**
	 * Get the PersistService for the class with the given name, or the primary PersistService
	 * if the given class does not map to any PersistService.
	 * @param clazz the class whose PersistService is being retrieved. Can be null.
	 * @return the PersistService for the given class, if one exists; the primary PersistService otherwise, never null.
	 */
	public abstract PersistService getFor(String className);

	public abstract PersistService getPrimary();

	/** 
	 * Sets the session name for open persist service sessions in this thread.
	 * First closes any sessions that may already be open.
	 * <p>Thread safe if the persist services handle sessions in a thread safe manner.</p>
	 * @param name
	 */
	public abstract void openSession(String name);

	public abstract Object put(Class<? extends Model> clazz, PersistService service);

	public abstract Object remove(Class<? extends Model> clazz);

	public abstract Object set(PersistService service);

	public abstract String toString();

}