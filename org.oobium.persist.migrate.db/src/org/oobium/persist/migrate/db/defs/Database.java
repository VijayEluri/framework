package org.oobium.persist.migrate.db.defs;

import java.util.Map;

import org.oobium.persist.migrate.Options;

public class Database {

	public final Options options;
	
	public Database(Map<String, ? extends Object> options) {
		this.options = new Options(options);
	}
	
}
