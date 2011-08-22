package org.oobium.persist.mongo;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.SimplePersistServiceProvider;
import org.oobium.persist.migrate.mongo.MongoMigrationService;

public class BaseMongoTestCase {

	protected static final Logger logger = LogProvider.getLogger(MongoPersistService.class);
	protected static final String client = "testClient";
	
	protected static MongoPersistService persistService;
	protected static MongoMigrationService migrationService;

	public static void assertNoErrors(Model model) {
		assertFalse(model.getErrors().toString(), model.hasErrors());
	}
	
	@BeforeClass
	public static void setupClass() {
		logger.setConsoleLevel(Logger.TRACE);
	}

	protected static String pkg;
	private static int count;

	@Before
	public void setup() throws Exception {
		DynClasses.reset();
		pkg = "test" + count++;
		persistService = new MongoPersistService(client, "postgres:password@localhost/testDatabase");
		migrationService = new MongoMigrationService(client, logger);
		migrationService.setPersistService(persistService);
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

}
