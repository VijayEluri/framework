package org.oobium.persist.migrate;

import java.sql.SQLException;

import org.oobium.logging.Logger;

public interface Migration {

	public abstract void up() throws SQLException;

	public abstract void down() throws SQLException;
	
	public abstract void setLogger(Logger logger);
	
	public abstract void setService(MigrationService service);
	
}
