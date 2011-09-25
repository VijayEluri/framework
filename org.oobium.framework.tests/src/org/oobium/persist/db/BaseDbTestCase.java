package org.oobium.persist.db;

import static org.oobium.utils.StringUtils.simpleName;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.oobium.build.gen.DbGenerator;
import org.oobium.build.model.ModelDefinition;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.SimpleDynClass;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.SimplePersistServiceProvider;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;
import org.oobium.persist.db.mysql.MySqlPersistService;
import org.oobium.persist.db.postgresql.PostgreSqlPersistService;
import org.oobium.persist.migrate.Migration;
import org.oobium.persist.migrate.db.DbMigrationService;
import org.oobium.persist.migrate.db.derby.embedded.DerbyEmbeddedMigrationService;
import org.oobium.persist.migrate.db.mysql.MySqlMigrationService;
import org.oobium.persist.migrate.db.postgresql.PostgreSqlMigrationService;
import org.oobium.utils.SqlUtils;

public class BaseDbTestCase {

	protected final int dbType = SqlUtils.DERBY;
//	protected final int dbType = SqlUtils.MYSQL;
//	protected final int dbType = SqlUtils.POSTGRESQL;

	protected static final Logger logger = LogProvider.getLogger(DbPersistService.class);
	protected static final String client = "testClient";
	
	protected static DbPersistService persistService;
	protected static DbMigrationService migrationService;

	protected void migrate(DynModel...models) throws Exception {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getSource(), DynClasses.getSiblings(models[i]));
		}
		DbGenerator gen = new DbGenerator(pkg, "CreateDatabase", defs);
		gen.generate();
		System.out.println(gen.getSource());
		Class<?> clazz = SimpleDynClass.getClass(gen.getFullName(), gen.getSource());
		Migration mig = (Migration) clazz.newInstance();
		mig.setService(migrationService);
		mig.up();
	}


	@BeforeClass
	public static void setupClass() {
		logger.setConsoleLevel(Logger.TRACE);
	}

	protected static String pkg;
	private static int count;

	private void createPersistence() {
		switch(dbType) {
		case SqlUtils.DERBY:
			persistService = new DerbyEmbeddedPersistService(client, "testDatabase", true);
			migrationService = new DerbyEmbeddedMigrationService(client, logger);
			break;
		case SqlUtils.MYSQL:
			persistService = new MySqlPersistService(client, "root:password@localhost/testDatabase");
			migrationService = new MySqlMigrationService(client, logger);
			break;
		case SqlUtils.POSTGRESQL:
			persistService = new PostgreSqlPersistService(client, "postgres:password@localhost/testDatabase");
			migrationService = new PostgreSqlMigrationService(client, logger);
			break;
		default:
			throw new IllegalStateException();
		}
	}
	
	@Before
	public void setup() throws Exception {
		DynClasses.reset();
		pkg = "test" + count++;
		createPersistence();
		migrationService.setPersistServices(new SimplePersistServiceProvider(persistService));
		migrationService.createDatastore();
		Model.setLogger(logger);
		Model.setPersistServiceProvider(new SimplePersistServiceProvider(persistService));
	}

	@After
	public void tearDown() throws Exception {
		Model.setLogger(null);
		Model.setPersistServiceProvider(null);
		migrationService.dropDatastore();
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
