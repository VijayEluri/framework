package org.oobium.persist.migrate.defs.changes;

import org.oobium.persist.migrate.defs.Change;


public class RemoveForeignKey extends Change {

	public final String name;
	public final String column;

	public RemoveForeignKey(String column) {
		super(ChangeType.RemoveForeignKey);
		this.name = null;
		this.column = column;
	}
	
	private RemoveForeignKey(String name, String column) {
		super(ChangeType.RemoveForeignKey);
		this.name = name;
		this.column = column;
	}

	public RemoveForeignKey withName(String name) {
		return new RemoveForeignKey(name, column);
	}
	
}
