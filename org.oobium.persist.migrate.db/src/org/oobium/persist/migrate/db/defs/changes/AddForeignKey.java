package org.oobium.persist.migrate.db.defs.changes;

import java.util.Map;

import org.oobium.persist.migrate.db.defs.Change;
import org.oobium.persist.migrate.db.defs.columns.ForeignKey;


public class AddForeignKey extends Change {

	public final ForeignKey fk;

	public AddForeignKey(String column, String reference, Map<String , ? extends Object> options) {
		super(ChangeType.AddForeignKey);
		this.fk = new ForeignKey(column, reference, options);
	}
	
	private AddForeignKey(ForeignKey fk) {
		super(ChangeType.AddForeignKey);
		this.fk = fk;
	}

	public AddForeignKey withName(String name) {
		return new AddForeignKey(fk.withName(name));
	}
	
}
