package org.oobium.persist.migrate.defs;

public class Change {

	public enum ChangeType {
		AddColumn, 
		AddForeignKey,
		AddIndex, 
		ChangeDefault,
		RemoveColumn, 
		RemoveForeignKey,
		RemoveIndex,
		RenameColumn,
		RenameTable
	}
	
	
	public final ChangeType ctype;
	
	public Change(ChangeType ctype) {
		this.ctype = ctype;
	}
	
}
