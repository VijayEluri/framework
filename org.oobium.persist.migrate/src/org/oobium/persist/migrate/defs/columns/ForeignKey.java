package org.oobium.persist.migrate.defs.columns;

import java.util.Map;

import org.oobium.persist.migrate.defs.Column;

public class ForeignKey extends Column {

	public static final int UNDEFINED = -1;
	public static final int CASCADE = 0;
	public static final int RESTRICT = 1;
	public static final int SET_NULL = 2;
	public static final int NO_ACTION = 3;
	
	
	public final String column;
	public final String reference;

	public ForeignKey(String column, String reference, Map<String , ? extends Object> options) {
		super(ColumnType.ForeignKey, "integer", null, options);
		this.column = column;
		this.reference = reference;
	}
	
	private ForeignKey(String name, ForeignKey fk) {
		super(ColumnType.ForeignKey, "integer", name, (fk.options != null) ? fk.options.getMap() : null);
		this.column = fk.column;
		this.reference = fk.reference;
	}
	
	public ForeignKey withName(String name) {
		return new ForeignKey(name, this);
	}
	
}
