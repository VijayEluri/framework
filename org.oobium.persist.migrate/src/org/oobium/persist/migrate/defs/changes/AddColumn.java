package org.oobium.persist.migrate.defs.changes;

import java.util.Map;

import org.oobium.persist.migrate.defs.Change;
import org.oobium.persist.migrate.defs.Column;

public class AddColumn extends Change {

	public final Column column;
	
	public AddColumn(String type, String name, Map<String, ? extends Object> options) {
		super(ChangeType.AddColumn);
		this.column = new Column(type, name, options);
	}
	
}
