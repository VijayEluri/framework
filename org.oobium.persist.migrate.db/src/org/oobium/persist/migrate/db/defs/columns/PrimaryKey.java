package org.oobium.persist.migrate.db.defs.columns;

import java.util.Map;

import org.oobium.persist.migrate.db.defs.Column;

public class PrimaryKey extends Column {

	public final boolean autoIncrement;

	public PrimaryKey(String name, String type, boolean autoIncrement) {
		this(name, type, autoIncrement, null);
	}
	
	public PrimaryKey(String name, String type, boolean autoIncrement, Map<String, ? extends Object> options) {
		super(ColumnType.PrimaryKey, "integer", name, options);
		this.autoIncrement = autoIncrement;
	}
	
}
