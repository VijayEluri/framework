package org.oobium.persist.migrate.db.mysql;

import static org.oobium.persist.Relation.CASCADE;
import static org.oobium.persist.Relation.NO_ACTION;
import static org.oobium.persist.Relation.RESTRICT;
import static org.oobium.persist.Relation.SET_DEFAULT;
import static org.oobium.persist.Relation.SET_NULL;
import static org.oobium.utils.SqlUtils.MYSQL;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.oobium.logging.Logger;
import org.oobium.persist.migrate.db.DbMigrationService;
import org.oobium.persist.migrate.db.defs.Table;
import org.oobium.persist.migrate.db.defs.columns.ForeignKey;
import org.oobium.persist.migrate.db.defs.columns.PrimaryKey;
import org.oobium.utils.SqlUtils;

public class MySqlMigrationService extends DbMigrationService {

	// MySQL data types: http://dev.mysql.com/doc/refman/5.1/en/connector-j-reference-type-conversions.html
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
		sqlTypes.put("text", 		"TEXT");
		sqlTypes.put("time", 		"TIME");
		sqlTypes.put("timestamp", 	"BIGINT");
	}
	

	public MySqlMigrationService() {
		super();
	}
	
	public MySqlMigrationService(String client, Logger logger) {
		super(client, logger);
	}
	
	@Override
	protected String getCreateForeignKeyColumnSql(ForeignKey fk) {
		StringBuilder sb = new StringBuilder();
		sb.append(getSqlSafe(fk.column)).append(' ').append(getSqlType(fk.type));
		sb.append(" REFERENCES ").append(getSqlSafe(fk.reference)).append("(id)");
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
		sb.append(" NOT NULL");
		if(pk.autoIncrement) sb.append(" AUTO_INCREMENT");
		sb.append(" PRIMARY KEY");
		return sb.toString();
	}
	
	@Override
	protected String getCreateTableOptionsSql(Table table) {
		String engine = (String) persistor.getDatabase().get("engine");
		try {
			DatabaseMetaData meta = persistor.getConnection().getMetaData();
			if(meta.getDatabaseMajorVersion() >= 5 && meta.getDatabaseMinorVersion() >= 1) {
				return "ENGINE = " + engine + " CHARACTER SET UTF8";
			}
			return "TYPE = " + engine + " CHARACTER SET UTF8";
		} catch(SQLException e) {
			throw new RuntimeException("can't get version of MySQL database", e);
		}
	}

	@Override
	protected String getSqlSafe(String rawString) {
		return SqlUtils.safeSqlWord(MYSQL, rawString);
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
