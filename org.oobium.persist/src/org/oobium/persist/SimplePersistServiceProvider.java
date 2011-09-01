package org.oobium.persist;


public class SimplePersistServiceProvider implements PersistServiceProvider {

	private PersistService service;
	
	public SimplePersistServiceProvider() {
		String vendor = System.getProperty("java.vendor");
		if("The Android Project".equals(vendor)) { // http://developer.android.com/reference/java/lang/System.html
			try {
				Class<?> serviceClass = Class.forName("org.oobium.persist.http.HttpPersistService");
				this.service = (PersistService) serviceClass.newInstance();
			} catch(Exception e) {
				this.service = new NullPersistService();
			}
		} else {
			this.service = new NullPersistService();
		}
	}
	
	public SimplePersistServiceProvider(PersistService service) {
		this.service = service;
	}
	
	@Override
	public void closeSession() {
		service.closeSession();
	}
	
	@Override
	public PersistService get(String serviceName) {
		return service;
	}
	
	@Override
	public PersistService getFor(Class<? extends Model> clazz) {
		return service;
	}

	@Override
	public PersistService getFor(String className) {
		return service;
	}

	@Override
	public PersistService getPrimary() {
		return service;
	}

	@Override
	public void openSession(String name) {
		service.openSession(name);
	}

	@Override
	public Object put(Class<? extends Model> clazz, PersistService service) {
		return null;
	}

	@Override
	public Object remove(Class<? extends Model> clazz) {
		return null;
	}

	@Override
	public Object set(PersistService service) {
		Object old = this.service;
		this.service = service;
		return old;
	}

}
