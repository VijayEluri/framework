package org.oobium.persist.migrate;

import org.oobium.logging.Logger;

public interface Migration {

	public abstract void up() throws Exception;

	public abstract void down() throws Exception;
	
	public abstract void setLogger(Logger logger);
	
	public abstract void setService(MigrationService service);
	
}
