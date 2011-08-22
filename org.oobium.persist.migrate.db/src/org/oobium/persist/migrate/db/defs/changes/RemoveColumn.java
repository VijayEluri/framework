package org.oobium.persist.migrate.db.defs.changes;

import org.oobium.persist.migrate.db.defs.Change;

public class RemoveColumn extends Change {

	public final String column;
	
	public RemoveColumn(String column) {
		super(ChangeType.RemoveColumn);
		this.column = column;
	}
	
}
