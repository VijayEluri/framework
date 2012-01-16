package org.oobium.eclipse.designer.manager;

import org.oobium.persist.http.HttpPersistService;

public class OsgiHttpPersistService extends HttpPersistService {

	private final DataService service;
	
	public OsgiHttpPersistService(DataService service) {
		super(service.location, true);
		this.service = service;
	}
	
	@Override
	protected Class<?> loadClass(String className) throws Exception {
		return service.bundle.loadClass(className);
	}

}
