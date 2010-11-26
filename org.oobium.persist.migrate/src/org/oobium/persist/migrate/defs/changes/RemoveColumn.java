package org.oobium.persist.migrate.defs.changes;

import org.oobium.persist.migrate.defs.Change;

public class RemoveColumn extends Change {

	public final String column;
	
	public RemoveColumn(String column) {
		super(ChangeType.RemoveColumn);
		this.column = column;
	}
	
}
