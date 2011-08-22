package org.oobium.persist.migrate.db.defs.changes;

import org.oobium.persist.migrate.db.defs.Change;

public class RemoveIndex extends Change {

	public final String name;
	public final String[] columns;
	
	public RemoveIndex(String...columns) {
		super(ChangeType.RemoveIndex);
		this.name = null;
		this.columns = columns;
	}
	
	private RemoveIndex(String name, String...columns) {
		super(ChangeType.RemoveIndex);
		this.name = name;
		this.columns = columns;
	}

	public RemoveIndex withName(String name) {
		return new RemoveIndex(name, columns);
	}
	
}
