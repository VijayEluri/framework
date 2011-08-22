package org.oobium.persist.migrate.db.defs.changes;

import org.oobium.persist.migrate.db.defs.Change;

public class Rename extends Change {

	public final String from;
	public final String to;
	
	public Rename(String to) {
		super(ChangeType.RenameTable);
		this.from = null;
		this.to = to;
	}
	
	public Rename(String from, String to) {
		super(ChangeType.RenameColumn);
		this.from = from;
		this.to = to;
	}

}
