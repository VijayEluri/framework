package org.oobium.persist.migrate.defs.changes;

import org.oobium.persist.migrate.defs.Change;
import org.oobium.persist.migrate.defs.Index;

public class AddIndex extends Change {

	public final Index index;
	
	public AddIndex(String[] columns, boolean unique) {
		super(ChangeType.AddIndex);
		this.index = new Index(columns, unique);
	}
	
	private  AddIndex(Index index) {
		super(ChangeType.AddIndex);
		this.index = index;
	}
	
	public AddIndex withName(String name) {
		return new AddIndex(index.withName(name));
	}
	
}
