package org.oobium.persist.migrate.db.defs;


public class Index {

	public final String name;
	public final String[] columns;
	public final boolean unique;

	public Index(String column, boolean unique) {
		this.name = null;
		this.unique = unique;
		this.columns = new String[] { column };
	}
	
	public Index(String[] columns, boolean unique) {
		this.name = null;
		this.unique = unique;
		this.columns = columns;
	}
	
	private Index(String name, String[] columns, boolean unique) {
		this.name = name;
		this.unique = unique;
		this.columns = columns;
	}
	
	public Index setUnique(boolean unique) {
		return new Index(name, columns, unique);
	}
	
	public Index withName(String name) {
		return new Index(name, columns, unique);
	}
	
}
