package org.oobium.persist.db;

import static org.oobium.utils.StringUtils.simpleName;

import java.io.File;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.oobium.build.gen.DbGenerator;
import org.oobium.build.model.ModelDefinition;
import org.oobium.framework.tests.dyn.SimpleDynClass;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.SimplePersistServiceProvider;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;
import org.oobium.persist.db.internal.QueryUtils;
import org.oobium.persist.db.mysql.MySqlPersistService;
import org.oobium.persist.db.postgresql.PostgreSqlPersistService;
import org.oobium.persist.migrate.AbstractMigration;
import org.oobium.persist.migrate.db.DbMigrationService;
import org.oobium.persist.migrate.db.derby.embedded.DerbyEmbeddedMigrationService;
import org.oobium.persist.migrate.db.mysql.MySqlMigrationService;
import org.oobium.persist.migrate.db.postgresql.PostgreSqlMigrationService;

public class BaseDbTestCase {

	protected final int dbType = QueryUtils.DERBY;
//	protected final int dbType = QueryUtils.MYSQL;
//	protected final int dbType = QueryUtils.POSTGRESQL;

	protected static final Logger logger = LogProvider.getLogger(DbPersistService.class);
	protected static final String schema = System.getProperty("user.dir") + File.separator + "dbtest";
	
	protected static DbPersistService persistService;
	protected static DbMigrationService migrationService;

	protected void migrate(DynModel...models) throws Exception {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getModelDescription(), DynClasses.getSiblings(models[i]));
		}
		DbGenerator gen = new DbGenerator(pkg, "CreateDatabase", defs);
		gen.generate();
		Class<?> clazz = SimpleDynClass.getClass(gen.getFullName(), gen.getSource());
		System.out.println(gen.getSource());
		AbstractMigration mig = (AbstractMigration) clazz.newInstance();
		mig.setService(migrationService);
		mig.up();
	}


	@BeforeClass
	public static void setupClass() {
		logger.setConsoleLevel(Logger.TRACE);
	}

	protected static String pkg;
	private static int count;

	private void setupPersistence() {
		switch(dbType) {
		case QueryUtils.DERBY:
			persistService = new DerbyEmbeddedPersistService(schema, true);
			migrationService = new DerbyEmbeddedMigrationService(logger);
			break;
		case QueryUtils.MYSQL:
			persistService = new MySqlPersistService("root:sepultra@localhost/test");
			migrationService = new MySqlMigrationService(logger);
			break;
		case QueryUtils.POSTGRESQL:
			persistService = new PostgreSqlPersistService("postgres:sepultra@localhost/test");
			migrationService = new PostgreSqlMigrationService(logger);
			break;
		default:
			throw new IllegalStateException();
		}
	}
	
	@Before
	public void setup() {
		DynClasses.reset();
		pkg = "test" + count++;
		setupPersistence();
		persistService.createDatabase();
		migrationService.setPersistService(persistService);
		Model.setLogger(logger);
		Model.setPersistServiceProvider(new SimplePersistServiceProvider(persistService));
	}

	@After
	public void tearDown() {
		Model.setLogger(null);
		Model.setPersistServiceProvider(null);
		persistService.closeSession();
		persistService.dropDatabase();
		persistService = null;
	}

	/**
	 * A convenience method for performing a row count on a database table.
	 * This method always returns an int for use in assertEquals, whereas the straight
	 * query returns an int for some databases, and a long for others.
	 * @param table the table whose rows to count
	 * @return the number of rows as an int
	 * @throws SQLException
	 */
	protected int count(String table) throws SQLException {
		Object o = persistService.executeQueryValue("SELECT COUNT(*) FROM " + table);
		if(o instanceof Number) {
			return ((Number) o).intValue();
		}
		throw new IllegalStateException("count did not return a Number...");
	}

}
