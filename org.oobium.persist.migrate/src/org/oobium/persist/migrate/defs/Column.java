package org.oobium.persist.migrate.defs;

import java.util.Map;

import org.oobium.persist.migrate.Options;

public class Column {

	public enum ColumnType {
		Column,
		ForeignKey,
		PrimaryKey
	}
	
	public static final String BINARY 		= "binary";
	public static final String BOOLEAN		= "boolean";
	public static final String DATE			= "date";
	public static final String DECIMAL		= "decimal";
	public static final String DOUBLE		= "double";
	public static final String FLOAT		= "float";
	public static final String INTEGER		= "integer";
	public static final String LONG			= "long";
	public static final String STRING		= "string";
	public static final String TEXT			= "text";
	public static final String TIME			= "time";
	public static final String TIMESTAMP	= "timestamp";
	
	public static final String DATESTAMPS	= "datestamps";
	public static final String TIMESTAMPS	= "timestamps";
	
	
	public final ColumnType ctype;
	
	public final String type;
	public final String name;
	public final Options options;

	protected Column(ColumnType ctype, String type, String name, Map<String, ? extends Object> options) {
		this.ctype = ctype;
		this.type = type;
		this.name = name;
		this.options = new Options(options);
	}
	
	public Column(String type, String name) {
		this(type, name, null);
	}
	
	public Column(String type, String name, Map<String, ? extends Object> options) {
		this(ColumnType.Column, type, name, options);
	}

	@Override
	public String toString() {
		return name + "<" + type + ">" + " " + options;
	}
	
}
