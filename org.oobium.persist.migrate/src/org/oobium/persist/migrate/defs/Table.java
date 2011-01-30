package org.oobium.persist.migrate.defs;

import static org.oobium.persist.migrate.defs.Column.*;
import static org.oobium.persist.migrate.defs.Column.TIMESTAMP;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oobium.persist.migrate.MigrationService;
import org.oobium.persist.migrate.Options;
import org.oobium.persist.migrate.defs.Column.ColumnType;
import org.oobium.persist.migrate.defs.changes.AddColumn;
import org.oobium.persist.migrate.defs.changes.AddForeignKey;
import org.oobium.persist.migrate.defs.changes.AddIndex;
import org.oobium.persist.migrate.defs.changes.RemoveColumn;
import org.oobium.persist.migrate.defs.changes.RemoveForeignKey;
import org.oobium.persist.migrate.defs.changes.RemoveIndex;
import org.oobium.persist.migrate.defs.changes.Rename;
import org.oobium.persist.migrate.defs.columns.ForeignKey;
import org.oobium.persist.migrate.defs.columns.PrimaryKey;

public class Table {

	private static final PrimaryKey defaultPrimaryKey = new PrimaryKey("id", INTEGER, true);
	
	private static String fkname(String table, String column) {
		return table + "__" + column + "_FK";
	}
	
	private static String idxname(String table, String...columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("idx_").append(table);
		for(String column : columns) {
			sb.append('_').append('_').append(column);
		}
		return sb.toString();
	}
	
	private final MigrationService service;
	public final String name;

	public final Options options;
	private boolean hasPk;
	private PrimaryKey pk;
	private List<Column> columns;
	
	private List<Index> indexes;
	
	private List<Change> changes;
	
	public Table(MigrationService service, String name) {
		this(service, name, null);
	}

	public Table(MigrationService service, String name, Map<String, ? extends Object> options) {
		this.service = service;
		this.name = name;
		this.options = new Options(options);
		this.hasPk = true;
	}

	public Table add(Change change) {
		if(changes == null) {
			changes = new ArrayList<Change>();
		}
		switch(change.ctype) {
		case AddColumn:
			AddColumn ac = (AddColumn) change;
			if(TIMESTAMPS.equals(ac.column.type)) {
				changes.add(new AddColumn(TIMESTAMP, "created_at", null));
				changes.add(new AddColumn(TIMESTAMP, "updated_at", null));
				return this;
			} else if(DATESTAMPS.equals(ac.column.type)) {
				changes.add(new AddColumn(DATE, "created_on", null));
				changes.add(new AddColumn(DATE, "updated_on", null));
				return this;
			}
			break;
		case AddForeignKey:
			AddForeignKey afk = (AddForeignKey) change;
			if(afk.fk.name == null) {
				change = afk.withName(fkname(name, afk.fk.column));
			}
			break;
		case AddIndex:
			AddIndex aidx = (AddIndex) change;
			if(aidx.index.name == null) {
				change = aidx.withName(idxname(name, aidx.index.columns));
			}
			break;
		case RemoveColumn:
			RemoveColumn rc = (RemoveColumn) change;
			if(TIMESTAMPS.equals(rc.column)) {
				changes.add(new RemoveColumn("created_at"));
				changes.add(new RemoveColumn("updated_at"));
				return this;
			} else if(DATESTAMPS.equals(rc.column)) {
				changes.add(new RemoveColumn("created_on"));
				changes.add(new RemoveColumn("updated_on"));
				return this;
			}
			break;
		case RemoveForeignKey:
			RemoveForeignKey rfk = (RemoveForeignKey) change;
			if(rfk.name == null) {
				change = rfk.withName(fkname(name, rfk.column));
			}
			break;
		case RemoveIndex:
			RemoveIndex ridx = (RemoveIndex) change;
			if(ridx.name == null) {
				change = ridx.withName(idxname(name, ridx.name));
			}
			break;
		}
		changes.add(change);
		return this;
	}
	
	public Table add(Column column) {
		if(columns == null) {
			columns = new ArrayList<Column>();
		}
		if(column.ctype == ColumnType.ForeignKey) {
			ForeignKey fk = (ForeignKey) column;
			if(fk.name == null) {
				columns.add(fk.withName(fkname(name, fk.column)));
			} else {
				columns.add(fk);
			}
		} else {
			if(TIMESTAMPS.equals(column.type)) {
				columns.add(new Column(TIMESTAMP, "created_at"));
				columns.add(new Column(TIMESTAMP, "updated_at"));
			} else if(DATESTAMPS.equals(column.type)) {
				columns.add(new Column(DATE, "created_on"));
				columns.add(new Column(DATE, "updated_on"));
			} else {
				columns.add(column);
			}
		}
		return this;
	}
	
	public Table add(Index index) {
		if(indexes == null) {
			indexes = new ArrayList<Index>();
		}
		if(index.name == null) {
			indexes.add(index.withName(idxname(name, index.columns)));
		} else {
			indexes.add(index);
		}
		return this;
	}
	
	public Table add(String type, String name) {
		return add(type, name, null);
	}
	
	public Table add(String type, String name, Map<String, ? extends Object> options) {
		return add(new AddColumn(type, name, options));
	}
	
	public Table addBinary(String name) {
		return add(BINARY, name);
	}
	
	public Table addBinary(String name, Map<String, ? extends Object> options) {
		return add(BINARY, name, options);
	}
	
	public Table addBoolean(String name) {
		return add(BOOLEAN, name);
	}
	
	public Table addBoolean(String name, Map<String, ? extends Object> options) {
		return add(BOOLEAN, name, options);
	}
	
	public Table addDate(String name) {
		return add(DATE, name);
	}
	
	public Table addDate(String name, Map<String, ? extends Object> options) {
		return add(DATE, name, options);
	}
	
	public Table addDatestamps() {
		return add(DATESTAMPS, null);
    }
	
	public Table addDecimal(String name) {
		return add(DECIMAL, name);
	}
	
	public Table addDecimal(String name, Map<String, ? extends Object> options) {
		return add(DECIMAL, name, options);
	}

	public Table addFloat(String name) {
		return add(FLOAT, name);
	}
	
	public Table addFloat(String name, Map<String, ? extends Object> options) {
		return add(FLOAT, name, options);
	}
	
	public Table addForeignKey(String column, String reference) {
		return addForeignKey(column, reference, null);
	}
	
	public Table addForeignKey(String column, String reference, Map<String, ? extends Object> options) {
		return add(new AddForeignKey(column, reference, options));
	}
	
	public Table addIndex(String...columns) {
    	return add(new AddIndex(columns, false));
    }

	public Table addInteger(String name) {
		return add(INTEGER, name);
	}
	
	public Table addInteger(String name, Map<String, ? extends Object> options) {
		return add(INTEGER, name, options);
	}
	
	public Table addString(String name) {
		return add(STRING, name);
	}
	
    public Table addString(String name, Map<String, ? extends Object> options) {
		return add(STRING, name, options);
	}
    
    public Table addText(String name) {
		return add(TEXT, name);
	}
    
    public Table addText(String name, Map<String, ? extends Object> options) {
		return add(TEXT, name, options);
	}
    
    public Table addTime(String name) {
		return add(TIME, name);
	}
    
    public Table addTime(String name, Map<String, ? extends Object> options) {
		return add(TIME, name, options);
	}

    public Table addTimestamp(String name) {
		return add(TIMESTAMP, name);
	}

	public Table addTimestamp(String name, Map<String, ? extends Object> options) {
		return add(TIMESTAMP, name, options);
	}

    public Table addTimestamps() {
		return add(TIMESTAMPS, null);
    }

    public Table addUniqueIndex(String...columns) {
    	return add(new AddIndex(columns, false));
    }

    public void create() throws SQLException {
    	service.create(this);
    }

    public void destroy() throws SQLException {
    	service.drop(this);
    }

    public List<Change> getChanges() {
		return (changes != null) ? changes : new ArrayList<Change>(0);
	}

    public List<Column> getColumns() {
		return (columns != null) ? columns : new ArrayList<Column>(0);
	}

    public List<Index> getIndexes() {
		return (indexes != null) ? indexes : new ArrayList<Index>(0);
	}

    public PrimaryKey getPrimaryKey() {
		if(hasPk) {
			if(pk != null) {
				return pk;
			}
			return defaultPrimaryKey;
		}
		return null;
	}

    public boolean hasPrimaryKey() {
		return hasPk;
	}

	public Table remove(String column) {
    	return add(new RemoveColumn(column));
    }

    public Table removeDatestamps() {
    	return remove(DATESTAMPS);
    }

	public Table removeForeignKey(String column) {
    	return add(new RemoveForeignKey(column));
    }

    public Table removeIndex(String...columns) {
    	return add(new RemoveIndex(columns));
    }

    public Table removeTimestamps() {
    	return remove(TIMESTAMPS);
    }
    
    /**
     * Rename the table to the given value.
     * @param to the new name for the table
     */
    public Table rename(String to) {
		return add(new Rename(to));
	}
    
    /**
     * Rename the given column to the give value
     * @param column the name of the column to be renamed
     * @param to the new name for the column
     */
    public Table rename(String column, String to) {
		return add(new Rename(column, to));
	}
    
    public Table setPrimaryKey(boolean hasPrimaryKey) {
		hasPk = hasPrimaryKey;
		return this;
	}

    public Table setPrimaryKey(String name, String type, boolean autoIncrement) {
		hasPk = true;
		pk = new PrimaryKey(name, type, autoIncrement);
		return this;
	}

	public void update() throws SQLException {
		service.update(this);
	}
	
}
