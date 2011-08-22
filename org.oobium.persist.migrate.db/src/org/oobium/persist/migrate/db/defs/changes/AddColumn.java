package org.oobium.persist.migrate.db.defs.changes;

import java.util.Map;

import org.oobium.persist.migrate.db.defs.Change;
import org.oobium.persist.migrate.db.defs.Column;

public class AddColumn extends Change {

	public final Column column;
	
	public AddColumn(String type, String name, Map<String, ? extends Object> options) {
		super(ChangeType.AddColumn);
		this.column = new Column(type, name, options);
	}
	
}
