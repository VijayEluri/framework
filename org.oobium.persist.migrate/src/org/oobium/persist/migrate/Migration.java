package org.oobium.persist.migrate;

import java.sql.SQLException;

public interface Migration {

	public abstract void up() throws SQLException;

	public abstract void down() throws SQLException;
	
	public abstract void setService(MigrationService service);
	
}
