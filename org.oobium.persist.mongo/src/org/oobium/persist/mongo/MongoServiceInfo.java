package org.oobium.persist.mongo;

import org.oobium.persist.ServiceInfo;

public class MongoServiceInfo implements ServiceInfo {

	private final MongoPersistService service;
	
	public MongoServiceInfo(MongoPersistService service) {
		this.service = service;
	}
	
	@Override
	public String getMigrationService() {
		return (String) service.getBundle().getHeaders().get("Oobium-MigrationService");
	}

	@Override
	public String getName() {
		return (String) service.getBundle().getHeaders().get("Bundle-Name");
	}

	@Override
	public String getProvider() {
		return (String) service.getBundle().getHeaders().get("Bundle-Vendor");
	}

	@Override
	public String getSymbolicName() {
		return service.getBundle().getSymbolicName();
	}

	@Override
	public String getVersion() {
		return service.getBundle().getVersion().toString();
	}

}
