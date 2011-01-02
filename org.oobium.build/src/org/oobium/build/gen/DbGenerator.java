/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.build.gen;

import static org.oobium.persist.migrate.defs.Column.BINARY;
import static org.oobium.persist.migrate.defs.Column.BOOLEAN;
import static org.oobium.persist.migrate.defs.Column.DATE;
import static org.oobium.persist.migrate.defs.Column.DATESTAMPS;
import static org.oobium.persist.migrate.defs.Column.DECIMAL;
import static org.oobium.persist.migrate.defs.Column.DOUBLE;
import static org.oobium.persist.migrate.defs.Column.FLOAT;
import static org.oobium.persist.migrate.defs.Column.INTEGER;
import static org.oobium.persist.migrate.defs.Column.LONG;
import static org.oobium.persist.migrate.defs.Column.STRING;
import static org.oobium.persist.migrate.defs.Column.TEXT;
import static org.oobium.persist.migrate.defs.Column.TIME;
import static org.oobium.persist.migrate.defs.Column.TIMESTAMP;
import static org.oobium.persist.migrate.defs.Column.TIMESTAMPS;
import static org.oobium.utils.StringUtils.varName;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.oobium.build.gen.migration.JoinTable;
import org.oobium.build.gen.migration.ModelTable;
import org.oobium.build.model.ModelDefinition;
import org.oobium.build.model.ModelRelation;
import org.oobium.build.util.SourceFile;
import org.oobium.build.workspace.Bundle;
import org.oobium.persist.Binary;
import org.oobium.persist.Text;
import org.oobium.persist.migrate.AbstractMigration;
import org.oobium.persist.migrate.Options;
import org.oobium.persist.migrate.defs.Column;
import org.oobium.persist.migrate.defs.Index;
import org.oobium.persist.migrate.defs.Table;
import org.oobium.persist.migrate.defs.columns.ForeignKey;
import org.oobium.utils.literal;

public class DbGenerator {

	private static final Map<String, String> migrationTypes;
	static {
		migrationTypes = new HashMap<String, String>();
		migrationTypes.put(Binary.class.getCanonicalName(),			BINARY);
		migrationTypes.put(byte[].class.getCanonicalName(),			BINARY);
		migrationTypes.put(String.class.getCanonicalName(),			STRING);
		migrationTypes.put(Text.class.getCanonicalName(),			TEXT);
		migrationTypes.put(Integer.class.getCanonicalName(),		INTEGER);
		migrationTypes.put(int.class.getCanonicalName(),			INTEGER);
		migrationTypes.put(Float.class.getCanonicalName(),			FLOAT);
		migrationTypes.put(float.class.getCanonicalName(),			FLOAT);
		migrationTypes.put(Long.class.getCanonicalName(), 			LONG);
		migrationTypes.put(long.class.getCanonicalName(), 			LONG);
		migrationTypes.put(Boolean.class.getCanonicalName(), 		BOOLEAN);
		migrationTypes.put(boolean.class.getCanonicalName(), 		BOOLEAN);
		migrationTypes.put(Double.class.getCanonicalName(), 		DOUBLE);
		migrationTypes.put(double.class.getCanonicalName(), 		DOUBLE);
		migrationTypes.put(Date.class.getCanonicalName(), 			TIMESTAMP);
		migrationTypes.put(java.sql.Date.class.getCanonicalName(),	DATE);
		migrationTypes.put(Time.class.getCanonicalName(),			TIME);
		migrationTypes.put(Timestamp.class.getCanonicalName(),		TIMESTAMP);
		migrationTypes.put(BigDecimal.class.getCanonicalName(),		DECIMAL);
	}
	
	/**
	 * convert a Java type into a method
	 */
	private static final String getMethod(String javaType) {
		String type = migrationTypes.get(javaType);
		if(type != null) {
			return Character.toUpperCase(type.charAt(0)) + type.substring(1);
		}
		return "String";
	}
	
	public static String generate(String name, String version, String type, Collection<ModelDefinition> models) {
		SourceFile sf = new SourceFile();
		
		Map<String, ModelTable> tables = new TreeMap<String, ModelTable>();
		Map<String, JoinTable> joins = new TreeMap<String, JoinTable>();
		
		for(ModelDefinition model : models) {
			tables.put(model.getSimpleName(), new ModelTable(sf, model, models));
		}

		for(ModelDefinition model : models) {
			for(ModelRelation relation : model.getRelations()) {
				if(relation.hasMany() && !relation.isThrough()) {
					ModelRelation oppositeRelation = relation.getOpposite();
					if(oppositeRelation == null || oppositeRelation.hasMany()) {
						JoinTable joinTable = new JoinTable(relation, oppositeRelation);
						joins.put(joinTable.name, joinTable);
					}
				}
			}
		}
		

		sf.isAbstract = true;
		sf.packageName = name.replace(File.separatorChar, '.') + ".migrator.migrations";
		sf.simpleName = "AbstractCreateDatabase";
		sf.superName = AbstractMigration.class.getSimpleName();
		sf.imports.add(AbstractMigration.class.getCanonicalName());
		sf.imports.add(Map.class.getCanonicalName());
		sf.imports.add(HashMap.class.getCanonicalName());
		sf.imports.add(SQLException.class.getCanonicalName());

		sf.variables.put("tableOptions", "private Map<String, Map<String, Object>> tableOptions");
		sf.constructors.put(0, "\tpublic AbstractCreateDatabase() {\n\t\ttableOptions = new HashMap<String, Map<String,Object>>();\n\t}");
		sf.methods.put("1", "\tprotected void setOptions(String table, Map<String, Object> options) {\n\t\ttableOptions.put(table, options);\n\t}");
		
		StringBuilder sb = new StringBuilder();
		sb.append("\t@Override\n\tpublic void up() throws SQLException {\n");
		
		if(type.equals(Bundle.Type.Application.name())) {
			sf.imports.add(Table.class.getCanonicalName());
			sb.append("\t\tTable systemAttrs = createTable(\"system_attrs\",\n" +
					  "\t\t\tString(\"name\"),\n" +
					  "\t\t\tString(\"detail\"),\n" +
					  "\t\t\tText(\"data\")\n" +
					  "\t\t);\n" +
					  "\t\tsystemAttrs.addUniqueIndex(\"name\", \"detail\");\n" +
					  "\t\tsystemAttrs.update();\n"
				);
		}
//		sb.append("\t\texecuteUpdate(\"INSERT INTO system_attrs (name, value) VALUES ('");
//		sb.append(name).append(".schema.initial', '").append(version).append("')").append("\");\n");
//		sb.append("\t\texecuteUpdate(\"INSERT INTO system_attrs (attr_name, attr_value) VALUES ('");
//		sb.append(name).append(".schema.current', '").append(version).append("')").append("\");\n");

		for(ModelTable table : tables.values()) {
			sb.append('\n');
			String var = varName(table.name);
			if(table.hasForeignKey() || table.hasIndex()) {
				sf.imports.add(Table.class.getCanonicalName());
				sb.append("\t\tTable ").append(var).append(" = createTable(\"").append(table.name).append("\", tableOptions.get(\"").append(table.name).append("\"),\n");
			} else {
				sb.append("\t\tcreateTable(\"").append(table.name).append("\", tableOptions.get(\"").append(table.name).append("\"),\n");
			}
			for(Iterator<Column> iter = table.columns.iterator(); iter.hasNext(); ) {
				Column column = iter.next();
				if(DATESTAMPS.equals(column.name)) {
					sb.append("\t\t\tDatestamps(");
				} else if(TIMESTAMPS.equals(column.name)) {
					sb.append("\t\t\tTimestamps(");
				} else {
					sb.append("\t\t\t").append(getMethod(column.type)).append("(\"").append(column.name).append("\"");
					if(column.options.hasAny()) {
						appendOptions(sf, sb, column.options);
					}
				}
				if(iter.hasNext()) {
					sb.append("),\n");
				} else {
					sb.append(")\n");
				}
			}
			sb.append("\t\t);\n");
			if(table.hasIndex()) {
				for(Index index : table.indexes) {
					if(index.unique) {
						sb.append("\t\t").append(var).append(".addUniqueIndex(");
					} else {
						sb.append("\t\t").append(var).append(".addIndex(");
					}
					for(int i = 0; i < index.columns.length; i++) {
						if(i != 0) sb.append(", ");
						sb.append('"').append(index.columns[i]).append('"');
					}
					sb.append(");\n");
				}
				if(!table.hasForeignKey()) {
					sb.append("\t\t").append(var).append(".update();\n");
				}
			}
		}

		if(!joins.isEmpty()) {
			sb.append('\n');
			for(JoinTable join : joins.values()) {
				sb.append("\t\tcreateJoinTable(\"");
				sb.append(join.table1).append("\", \"").append(join.column1).append("\", \"");
				sb.append(join.table2).append("\", \"").append(join.column2).append("\");\n");
			}
		}
		
		for(ModelTable table : tables.values()) {
			if(table.hasForeignKey()) {
				String var = varName(table.name);
				for(int i = 0; i < table.foreignKeys.size(); i++) {
					ForeignKey fk = table.foreignKeys.get(i);
					sb.append("\n\t\t").append(var).append(".addForeignKey(\"");
					sb.append(fk.column).append("\", \"").append(fk.reference).append('"');
					if(fk.options.hasAny()) {
						appendOptions(sf, sb, fk.options);
					}
					sb.append(");");
				}
				sb.append("\n\t\t").append(var).append(".update();");
			}
		}

		sb.append("\n\t}");
		sf.methods.put("2", sb.toString());
		
		return sf.toSource();
	}

	private static void appendOptions(SourceFile sf, StringBuilder sb, Options options) {
		sf.staticImports.add(literal.class.getCanonicalName() + ".Map");
		sb.append(", Map(");
		if(options.size() == 1) {
			String key = options.getKeys().iterator().next();
			sb.append("\"").append(key).append("\", ").append(options.get(key)).append(')');
		} else {
			sf.staticImports.add(literal.class.getCanonicalName() + ".e");
			for(Iterator<String> iter = options.getKeys().iterator(); iter.hasNext(); ) {
				String key = iter.next();
				sb.append("\n\t\t\t\te(\"").append(key).append("\", ").append(options.get(key)).append(')');
				if(iter.hasNext()) sb.append(", ");
			}
			sb.append("\n\t\t\t)");
		}
	}
	
}
