package org.oobium.persist.migrate.defs;

import static org.oobium.persist.migrate.defs.Column.DATE;
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

	private static final PrimaryKey defaultPrimaryKey = new PrimaryKey("id", "integer", true);
	
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
			if("Timestamps".equals(ac.column.name)) {
				changes.add(new AddColumn(TIMESTAMP, "createdAt", null));
				changes.add(new AddColumn(TIMESTAMP, "updatedAt", null));
				return this;
			} else if("Datestamps".equals(ac.column.name)) {
				changes.add(new AddColumn(DATE, "createdOn", null));
				changes.add(new AddColumn(DATE, "updatedOn", null));
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
			if("Timestamps".equals(rc.column)) {
				changes.add(new RemoveColumn("createdAt"));
				changes.add(new RemoveColumn("updatedAt"));
				return this;
			} else if("Datestamps".equals(rc.column)) {
				changes.add(new RemoveColumn("createdOn"));
				changes.add(new RemoveColumn("updatedOn"));
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
			if("Timestamps".equals(column.name)) {
				columns.add(new Column(TIMESTAMP, "createdAt"));
				columns.add(new Column(TIMESTAMP, "updatedAt"));
			} else if("Datestamps".equals(column.name)) {
				columns.add(new Column(DATE, "createdOn"));
				columns.add(new Column(DATE, "updatedOn"));
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
		return add("Binary", name);
	}
	
	public Table addBinary(String name, Map<String, ? extends Object> options) {
		return add("Binary", name, options);
	}
	
	public Table addBoolean(String name) {
		return add("Boolean", name);
	}
	
	public Table addBoolean(String name, Map<String, ? extends Object> options) {
		return add("Boolean", name, options);
	}
	
	public Table addDate(String name) {
		return add("Date", name);
	}
	
	public Table addDate(String name, Map<String, ? extends Object> options) {
		return add("Date", name, options);
	}
	
	public Table addDatestamps() {
		return add(null, "Datestamps");
    }
	
	public Table addDatetime(String name) {
		return add("Datetime", name);
	}
	
	public Table addDatetime(String name, Map<String, ? extends Object> options) {
		return add("Datetime", name, options);
	}

	public Table addDecimal(String name) {
		return add("Decimal", name);
	}
	
	public Table addDecimal(String name, Map<String, ? extends Object> options) {
		return add("Decimal", name, options);
	}

	public Table addFloat(String name) {
		return add("Float", name);
	}
	
	public Table addFloat(String name, Map<String, ? extends Object> options) {
		return add("Float", name, options);
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
		return add("Integer", name);
	}
	
	public Table addInteger(String name, Map<String, ? extends Object> options) {
		return add("Integer", name, options);
	}
	
	public Table addString(String name) {
		return add("String", name);
	}
	
    public Table addString(String name, Map<String, ? extends Object> options) {
		return add("String", name, options);
	}
    
    public Table addText(String name) {
		return add("Text", name);
	}
    
    public Table addText(String name, Map<String, ? extends Object> options) {
		return add("Text", name, options);
	}
    
    public Table addTime(String name) {
		return add("Time", name);
	}
    
    public Table addTime(String name, Map<String, ? extends Object> options) {
		return add("Time", name, options);
	}

    public Table addTimestamp(String name) {
		return add("Timestamp", name);
	}

	public Table addTimestamp(String name, Map<String, ? extends Object> options) {
		return add("Timestamp", name, options);
	}

    public Table addTimestamps() {
		return add(null, "Timestamps");
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
    	return remove("Datestamps");
    }

	public Table removeForeignKey(String column) {
    	return add(new RemoveForeignKey(column));
    }

    public Table removeIndex(String...columns) {
    	return add(new RemoveIndex(columns));
    }

    public Table removeTimestamps() {
    	return remove("Timestamps");
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
