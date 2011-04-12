package org.oobium.persist.migrate.db.derby.embedded;

import static org.oobium.persist.Relation.CASCADE;
import static org.oobium.persist.Relation.NO_ACTION;
import static org.oobium.persist.Relation.RESTRICT;
import static org.oobium.persist.Relation.SET_DEFAULT;
import static org.oobium.persist.Relation.SET_NULL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oobium.logging.Logger;
import org.oobium.persist.migrate.db.DbMigrationService;
import org.oobium.persist.migrate.defs.Table;
import org.oobium.persist.migrate.defs.columns.ForeignKey;
import org.oobium.persist.migrate.defs.columns.PrimaryKey;
import org.oobium.utils.SqlUtils;

public class DerbyEmbeddedMigrationService extends DbMigrationService {

	// Derby data types: http://db.apache.org/derby/docs/10.6/ref/crefsqlj31068.html
	private static final Map<String, String> sqlTypes;
	static {
		sqlTypes = new HashMap<String, String>();
		sqlTypes.put("binary",		"BLOB");
		sqlTypes.put("boolean", 	"BOOLEAN");
		sqlTypes.put("date", 		"DATE");
		sqlTypes.put("decimal", 	"DECIMAL");
		sqlTypes.put("double", 		"DOUBLE");
		sqlTypes.put("float", 		"FLOAT");
		sqlTypes.put("integer", 	"INTEGER");
		sqlTypes.put("long", 		"BIGINT");
		sqlTypes.put("string", 		"VARCHAR(255)");
		sqlTypes.put("text", 		"CLOB");
		sqlTypes.put("time", 		"TIME");
		sqlTypes.put("timestamp", 	"BIGINT");
	}
	
	
	public DerbyEmbeddedMigrationService() {
		super();
	}
	
	public DerbyEmbeddedMigrationService(Logger logger) {
		super(logger);
	}
	
	@Override
	public void dropAll() {
		logger.info("dropping all tables...");
		
		String sql = "select t.tablename, c.constraintname" + " from sys.sysconstraints c, sys.systables t"
				+ " where c.type = 'F' and t.tableid = c.tableid";

		List<Map<String, Object>> constraints = null;
		try {
			constraints = persistor.executeQuery(sql);
		} catch(SQLException e) {
			logger.info("database has not yet been created");
			return;
		}

		for(Map<String, Object> map : constraints) {
			sql = "alter table " + map.get("tablename") + " drop constraint " + map.get("constraintname");
			logger.debug(sql);
			try {
				persistor.executeUpdate(sql);
			} catch(Exception e) {
				logger.error("could not alter table: " + sql, e);
			}
		}

		try {
			Connection connection = persistor.getConnection();
			ResultSet rs = null;
			try {
				String appSchema = "ROOT";
				rs = connection.getMetaData().getTables(null, appSchema, "%", new String[] { "TABLE" });
				while(rs.next()) {
					sql = "drop table " + appSchema + "." + rs.getString(3);
					logger.debug(sql);
					Statement stmt = connection.createStatement();
					try {
						stmt.executeUpdate(sql);
					} finally {
						stmt.close();
					}
				}
			} finally {
				if(rs != null) {
					rs.close();
				}
				// connection.close(); no need - connection will be closed when
				// the session is closed
			}
			logger.info("all tables dropped.");
		} catch(SQLException e) {
			// well, something went wrong...
			logger.error("ERROR dropping database", e);
		}
	}

	@Override
	public void dropDatabase() {
		logger.info("Dropping database...");
		dropAll();
	}
	
	@Override
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

	@Override
	protected String getCreatePrimaryKeySql(PrimaryKey pk) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSqlSafe(pk.name)).append(' ').append(getSqlType(pk.type));
		if(pk.autoIncrement) sb.append(" GENERATED ALWAYS");
		sb.append(" AS IDENTITY PRIMARY KEY");
		return sb.toString();
	}
	
	@Override
	protected String getCreateTableOptionsSql(Table table) {
		return null;
	}

	@Override
	protected String getSqlSafe(String rawString) {
		return SqlUtils.safeSqlWord(rawString);
	}
	
	@Override
	protected String getSqlType(String migrationType) {
		String type = sqlTypes.get(migrationType);
		if(type != null) {
			return type;
		}
		return migrationType;
	}

}
