package org.oobium.persist.db;

import org.oobium.persist.ServiceInfo;

public class DbServiceInfo implements ServiceInfo {

	private final DbPersistService service;
	
	public DbServiceInfo(DbPersistService service) {
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
	
	@Override
	public boolean isRemote() {
		return false;
	}

}
