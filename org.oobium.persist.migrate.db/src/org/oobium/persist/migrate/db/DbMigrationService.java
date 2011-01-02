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
package org.oobium.persist.migrate.db;

import static org.oobium.persist.Relation.CASCADE;
import static org.oobium.persist.Relation.NO_ACTION;
import static org.oobium.persist.Relation.RESTRICT;
import static org.oobium.persist.Relation.SET_DEFAULT;
import static org.oobium.persist.Relation.SET_NULL;
import static org.oobium.utils.StringUtils.join;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.oobium.persist.PersistService;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.migrate.AbstractMigrationService;
import org.oobium.persist.migrate.defs.Change;
import org.oobium.persist.migrate.defs.Column;
import org.oobium.persist.migrate.defs.Index;
import org.oobium.persist.migrate.defs.Table;
import org.oobium.persist.migrate.defs.changes.AddColumn;
import org.oobium.persist.migrate.defs.changes.AddForeignKey;
import org.oobium.persist.migrate.defs.changes.AddIndex;
import org.oobium.persist.migrate.defs.changes.RemoveColumn;
import org.oobium.persist.migrate.defs.changes.RemoveForeignKey;
import org.oobium.persist.migrate.defs.changes.RemoveIndex;
import org.oobium.persist.migrate.defs.changes.Rename;
import org.oobium.persist.migrate.defs.columns.ForeignKey;
import org.oobium.persist.migrate.defs.columns.PrimaryKey;

public abstract class DbMigrationService extends AbstractMigrationService {

	protected DbPersistService persistor;

	public void addColumn(Table table, AddColumn change) throws SQLException {
		long start = -1;
		if(logger.isLoggingInfo()) {
			logger.info("adding column " + change.column.name + "to table " + table.name + "...");
			start = System.currentTimeMillis();
		}
		
		Connection connection = persistor.getConnection();
		Statement stmt = connection.createStatement();
		try {
			String sql = getAddColumnSql(table, change.column);
			logger.debug(sql);
			stmt.executeUpdate(sql);
			if(logger.isLoggingInfo()) {
				long total = System.currentTimeMillis() - start;
				logger.info("added column " + change.column.name + "to table " + table.name + " in " + total + "ms");
			}
		} finally {
			try {
				stmt.close();
			} catch(SQLException e) {
				// discard
			}
		}
	}

	public void addForeignKey(Table table, AddForeignKey change) throws SQLException {
		long start = -1;
		if(logger.isLoggingInfo()) {
			logger.info("creating foreign key (" + change.fk.column + " -> " + change.fk.reference + ") for table " + table.name + "...");
			start = System.currentTimeMillis();
		}
		
		Connection connection = persistor.getConnection();
		Statement stmt = connection.createStatement();
		try {
			String sql = getCreateForeignKeySql(table, change.fk);
			logger.debug(sql);
			stmt.executeUpdate(sql);
			if(logger.isLoggingInfo()) {
				long total = System.currentTimeMillis() - start;
				logger.info("created foreign key (" + change.fk.column + " -> " + change.fk.reference + ") for table " + table.name + " in " + total + "ms");
			}
		} finally {
			try {
				stmt.close();
			} catch(SQLException e) {
				// discard
			}
		}
	}

	public void addIndex(Table table, AddIndex change) throws SQLException {
		createIndex(table, change.index);
	}

	public void create(Table table) throws SQLException {
		createTable(table);
		for(Index index : table.getIndexes()) {
			createIndex(table, index);
		}
	}

	protected void createIndex(Table table, Index index) throws SQLException {
		long start = -1;
		if(logger.isLoggingInfo()) {
			logger.info("creating index [" + join(index.columns, ',') + "] for table " + table.name + "...");
			start = System.currentTimeMillis();
		}
		
		Connection connection = persistor.getConnection();
		Statement stmt = connection.createStatement();
		try {
			String sql = getCreateIndexSql(table, index);
			logger.debug(sql);
			stmt.executeUpdate(sql);
			if(logger.isLoggingInfo()) {
				long total = System.currentTimeMillis() - start;
				logger.info("created index [" + join(index.columns, ',') + "] for table " + table.name + " in " + total + "ms");
			}
		} finally {
			try {
				stmt.close();
			} catch(SQLException e) {
				// discard
			}
		}
	}
	
	protected void createTable(Table table) throws SQLException {
		long start = -1;
		if(logger.isLoggingInfo()) {
			logger.info("creating " + table.name + "...");
			start = System.currentTimeMillis();
		}
		
		Connection connection = persistor.getConnection();
		Statement stmt = connection.createStatement();
		try {
			String sql = getCreateTableSql(table);
			logger.debug(sql);
			stmt.executeUpdate(sql);
			if(logger.isLoggingInfo()) {
				long total = System.currentTimeMillis() - start;
				logger.info("created " + table.name + " in " + total + "ms");
			}
		} finally {
			try {
				stmt.close();
			} catch(SQLException e) {
				// discard
			}
		}
	}
	
	public void drop(Table table) throws SQLException {
		long start = -1;
		if(logger.isLoggingInfo()) {
			logger.info("dropping " + table.name + "...");
			start = System.currentTimeMillis();
		}
		
		Connection connection = persistor.getConnection();
		Statement stmt = connection.createStatement();
		try {
			String sql = "drop table " + table.name;
			logger.debug(sql);
			stmt.executeUpdate(sql);
			if(logger.isLoggingInfo()) {
				long total = System.currentTimeMillis() - start;
				logger.info("dropped " + table.name + " in " + total + "ms");
			}
		} finally {
			try {
				stmt.close();
			} catch(SQLException e) {
				// discard
			}
		}
	}

	public List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException {
		return persistor.executeQuery(sql, values);
	}

	public List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException {
		return persistor.executeQueryLists(sql, values);
	}

	public Object executeQueryValue(String sql, Object...values) throws SQLException {
		return persistor.executeQueryValue(sql, values);
	}

	public int executeUpdate(String sql, Object...values) throws SQLException {
		return persistor.executeUpdate(sql, values);
	}
	
	@Override
	public Table find(String table) {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	@Override
	public List<Table> findAll() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	protected String getAddColumnSql(Table table, Column column) {
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ").append(getSqlSafe(table.name));
		sb.append(" ADD COLUMN ").append(getColumnDefinitionSql(column));
		return sb.toString();
	}

	protected String getColumnDefinitionSql(Column column) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSqlSafe(column.name));
		sb.append(' ');
		sb.append(getSqlType(column.type));
		if("decimal".equals(getSqlType(column.type))) {
			String precision = column.options.get("precision", 2).toString();
			String scale = column.options.get("scale", 8).toString();
			sb.append("(").append(precision).append(",").append(scale).append(")");
		}
		if(column.options.get("unique", false)) {
			sb.append(" UNIQUE");
		}
		if(column.options.get("required", false)) {
			sb.append(" NOT NULL");
		}
		if(column.options.has("default")) {
			sb.append(" DEFAULT ").append(column.options.get("default"));
		} else if(column.options.has("primitive")) {
			sb.append(" DEFAULT ").append(getSqlForPrimitive(column.type));
		}
		if(column.options.has("check")) {
			sb.append(" CHECK(").append(column.options.get("check")).append(")");
		}
		return sb.toString();
	}

	protected String getCreateForeignKeyColumnSql(ForeignKey fk) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSqlSafe(fk.column)).append(' ').append(getSqlType(fk.type)).append(" CONSTRAINT ");
		sb.append(fk.name).append(" REFERENCES ").append(getSqlSafe(fk.reference)).append(" (id)");
		switch(fk.options.get("onDelete", -1)) {
		case CASCADE:		sb.append(" ON DELETE CASCADE");	break;
		case NO_ACTION:		sb.append(" ON DELETE NO ACTION");	break;
		case RESTRICT:		sb.append(" ON DELETE RESTRICT");	break;
		case SET_DEFAULT:	sb.append(" ON DELETE SET DEFAULT");break;
		case SET_NULL:		sb.append(" ON DELETE SET NULL");	break;
		}
		switch(fk.options.get("onUpdate", -1)) {
		case CASCADE:		sb.append(" ON UPDATE CASCADE");	break;
		case NO_ACTION:		sb.append(" ON UPDATE NO ACTION");	break;
		case RESTRICT:		sb.append(" ON UPDATE RESTRICT");	break;
		case SET_DEFAULT:	sb.append(" ON UPDATE SET DEFAULT");break;
		case SET_NULL:		sb.append(" ON UPDATE SET NULL");	break;
		}
		return sb.toString();
	}
	
	/**
	 * Generate the SQL to create a foreign key constraint on an existing column.
	 * @param table
	 * @param fk
	 * @return
	 */
	protected String getCreateForeignKeySql(Table table, ForeignKey fk) {
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ").append(getSqlSafe(table.name)).append(" ADD");
		sb.append(" CONSTRAINT ").append(getSqlSafe(fk.name));
		sb.append(" Foreign Key (").append(getSqlSafe(fk.column)).append(")");
		sb.append(" REFERENCES ").append(getSqlSafe(fk.reference)).append(" (id)");
		switch(fk.options.get("onDelete", -1)) {
		case CASCADE:		sb.append(" ON DELETE CASCADE");	break;
		case NO_ACTION:		sb.append(" ON DELETE NO ACTION");	break;
		case RESTRICT:		sb.append(" ON DELETE RESTRICT");	break;
		case SET_DEFAULT:	sb.append(" ON DELETE SET DEFAULT");break;
		case SET_NULL:		sb.append(" ON DELETE SET NULL");	break;
		}
		switch(fk.options.get("onUpdate", -1)) {
		case CASCADE:		sb.append(" ON UPDATE CASCADE");	break;
		case NO_ACTION:		sb.append(" ON UPDATE NO ACTION");	break;
		case RESTRICT:		sb.append(" ON UPDATE RESTRICT");	break;
		case SET_DEFAULT:	sb.append(" ON UPDATE SET DEFAULT");break;
		case SET_NULL:		sb.append(" ON UPDATE SET NULL");	break;
		}
		return sb.toString();
	}

	protected String getCreateIndexSql(Table table, Index index) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE ");
		if(index.unique) {
			sb.append("UNIQUE ");
		}
		sb.append("INDEX ").append(index.name);
		sb.append(" ON ").append(getSqlSafe(table.name)).append('(');
		for(int i = 0; i < index.columns.length; i++) {
			if(i != 0) {
				sb.append(',');
			}
			sb.append(getSqlSafe(index.columns[i]));
		}
		sb.append(')');
		return sb.toString();
	}
	
	protected String getCreatePrimaryKeySql(PrimaryKey pk) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSqlSafe(pk.name)).append(' ').append(getSqlType(pk.type));
		if(pk.autoIncrement) sb.append(" GENERATED ALWAYS");
		sb.append(" AS IDENTITY PRIMARY KEY");
		return sb.toString();
	}
	
	protected abstract String getCreateTableOptionsSql(Table table);
	
	protected String getCreateTableSql(Table table) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(getSqlSafe(table.name)).append('(');
		if(table.hasPrimaryKey()) {
			sb.append(getCreatePrimaryKeySql(table.getPrimaryKey()));
		}
		for(Column column : table.getColumns()) {
			sb.append(',');
			switch(column.ctype) {
			case Column:	 sb.append(getColumnDefinitionSql(column));	break;
			case ForeignKey: sb.append(getCreateForeignKeyColumnSql((ForeignKey) column)); break;
			}
		}
		String options = getCreateTableOptionsSql(table);
		if(options != null) {
			sb.append(options);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int getCurrentRevision() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	protected abstract String getSqlSafe(String rawString);

	protected abstract String getSqlType(String migrationType);
	
	protected abstract String getSqlForPrimitive(String type);
	
	public void removeColumn(Table table, RemoveColumn change) throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public void removeForeignKey(Table table, RemoveForeignKey change) throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public void removeIndex(Table table, RemoveIndex change) throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public void renameColumn(Table table, Rename change) throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}
	
	public void renameTable(Table table, Rename change) throws SQLException {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void setPersistService(PersistService persistor) {
		this.persistor = (DbPersistService) persistor;
	}

	@Override
	public void update(Table table) throws SQLException {
		for(Change change : table.getChanges()) {
			switch(change.ctype) {
			case AddColumn:			addColumn(table, 		(AddColumn) change); 		break;
			case AddForeignKey:		addForeignKey(table,	(AddForeignKey) change); 	break;
			case AddIndex:			addIndex(table,			(AddIndex) change); 		break;
//			case ChangeDefault:		changeDefault(table, change); 		break;
			case RemoveColumn:		removeColumn(table,		(RemoveColumn) change); 	break;
			case RemoveForeignKey:	removeForeignKey(table,	(RemoveForeignKey) change); break;
			case RemoveIndex:		removeIndex(table,		(RemoveIndex) change); 		break;
			case RenameColumn:		renameColumn(table,		(Rename) change); 			break;
			case RenameTable:		renameTable(table,		(Rename) change); 			break;
			}
		}
		// TODO
	}

}
