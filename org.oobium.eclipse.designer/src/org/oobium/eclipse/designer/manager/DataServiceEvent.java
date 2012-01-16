package org.oobium.eclipse.designer.manager;

public class DataServiceEvent {

	public enum Type {
		ServiceCreated,
		ServiceDestroyed,
		ServiceUpdated,
		Connected,
		Disconnected
	}
	
	public final DataService service;
	public final Type type;
	
	public DataServiceEvent(DataService service, Type type) {
		this.service = service;
		this.type = type;
	}
	
}
