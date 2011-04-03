package org.oobium.persist.db;

import static org.oobium.utils.StringUtils.simpleName;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.oobium.build.gen.DbGenerator;
import org.oobium.build.model.ModelDefinition;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.SimplePersistServiceProvider;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;
import org.oobium.persist.dyn.DynClass;
import org.oobium.persist.dyn.DynModel;
import org.oobium.persist.dyn.DynModels;
import org.oobium.persist.migrate.AbstractMigration;
import org.oobium.persist.migrate.db.derby.embedded.DerbyEmbeddedMigrationService;

public class BaseDbTestCase {

	protected static final Logger logger = LogProvider.getLogger(DbPersistService.class);
	protected static final String schema = "dbtest";
	protected static DbPersistService persistService;
	protected static DerbyEmbeddedMigrationService migrationService;

	protected void migrate(DynModel...models) throws Exception {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getModelDescription(), DynModels.getSiblings(models[i]));
		}
		DbGenerator gen = new DbGenerator(pkg, "CreateDatabase", defs);
		gen.generate();
		Class<?> clazz = DynClass.getClass(gen.getFullName(), gen.getSource());
		System.out.println(gen.getSource());
		AbstractMigration mig = (AbstractMigration) clazz.newInstance();
		mig.setService(migrationService);
		mig.up();
	}


	@BeforeClass
	public static void setUpClass() {
		logger.setConsoleLevel(Logger.TRACE);
	}

	protected static String pkg;
	private static int count;

	@Before
	public void setUp() {
		pkg = "test" + count++;
		persistService = new DerbyEmbeddedPersistService(schema, true);
		Model.setLogger(logger);
		Model.setPersistServiceProvider(new SimplePersistServiceProvider(persistService));
		migrationService = new DerbyEmbeddedMigrationService(logger);
		migrationService.setPersistService(persistService);
	}

	@After
	public void tearDown() {
		persistService.dropDatabase();
		Model.setLogger(null);
		Model.setPersistServiceProvider(null);
		persistService.closeSession();
		persistService = null;
	}

}
