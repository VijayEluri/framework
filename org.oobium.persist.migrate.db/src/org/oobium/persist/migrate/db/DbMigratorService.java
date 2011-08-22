package org.oobium.persist.migrate.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.oobium.persist.PersistService;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.migrate.MigratorService;
import org.oobium.persist.migrate.db.defs.Column;
import org.oobium.persist.migrate.db.defs.Table;
import org.oobium.utils.Config;

public abstract class DbMigratorService extends MigratorService {

	@Override
	protected void commit() throws Exception {
		getPersistService().commit();
	}
	
	@Override
	protected String getCurrentMigration() {
		try {
			String sql = "SELECT detail FROM system_attrs WHERE name='migration.current'";
			return (String) getPersistService().executeQueryValue(sql);
		} catch(SQLException e) {
			return null;
		}
	}

	@Override
	public DbMigrationService getMigrationService() {
		return (DbMigrationService) super.getMigrationService();
	}
	
	protected void createSystemAttrs() {
		try {
			Table systemAttrs = new Table(getMigrationService(), "system_attrs");
			systemAttrs.add(new Column(Column.STRING, "name"));
			systemAttrs.add(new Column(Column.STRING, "detail"));
			systemAttrs.add(new Column(Column.TEXT, "data"));
			systemAttrs.create();
			systemAttrs.addUniqueIndex("name", "detail");
			systemAttrs.update();
		} catch(SQLException e) {
			logger.error(e);
		}
	}

	@Override
	protected List<String> getMigrated() {
		try {
			String sql = "SELECT detail FROM system_attrs WHERE name='migrated'";
			List<List<Object>> lists = getPersistService().executeQueryLists(sql);
			List<String> migrated = new ArrayList<String>();
			for(int i = 1; i < lists.size(); i++) {
				migrated.add((String) lists.get(i).get(0));
			}
			return migrated;
		} catch(SQLException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public DbPersistService getPersistService() {
		PersistService service = super.getPersistService();
		if(service instanceof DbPersistService) {
			return (DbPersistService) service;
		}
		throw new IllegalStateException("Database Migration cannot run without a DbPersistService: " + appConfig.get(Config.PERSIST));
	}

	@Override
	protected void rollback() throws Exception {
		getPersistService().rollback();
	}

	@Override
	protected void setAutoCommit(boolean autoCommit) throws Exception {
		getPersistService().setAutoCommit(autoCommit);
	}
	
	@Override
	protected void setCurrentMigration(String current) throws Exception {
		DbPersistService ps = getPersistService();
		if(current == null) {
			try {
				ps.executeUpdate("DELETE FROM system_attrs WHERE name='migration.current'");
			} catch(SQLException e) {
				// last migration will remove the table
				rollback();
			}
		} else {
			String sql = "UPDATE system_attrs SET detail='" + current + "' where name='migration.current'";
			try {
				int r = ps.executeUpdate(sql);
				if(r == 0) {
					sql = "INSERT INTO system_attrs (name, detail, data) VALUES ('migration.current', '" + current + "', NULL)";
					ps.executeUpdate(sql);
				}
			} catch(SQLException e) {
				rollback();
				createSystemAttrs();
				sql = "INSERT INTO system_attrs (name, detail, data) VALUES ('migration.current', '" + current + "', NULL)";
				ps.executeUpdate(sql);
			}
			commit();
		}
	}

	@Override
	protected void setMigrated(String name, boolean migrated) throws Exception {
		if(migrated) {
			String sql = "INSERT INTO system_attrs (name, detail, data) VALUES ('migrated', '" + name + "', NULL)";
			try {
				getPersistService().executeUpdate(sql);
			} catch(SQLException e) {
				rollback();
				createSystemAttrs();
				getPersistService().executeUpdate(sql);
			}
			commit();
		} else {
			try {
				String sql = "DELETE FROM system_attrs WHERE name='migrated' AND detail='" + name + "'";
				getPersistService().executeUpdate(sql);
				commit();
			} catch(SQLException e) {
				// last migration will remove the table
				rollback();
			}
		}
	}

}
