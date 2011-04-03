package org.oobium.persist.db;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.oobium.utils.StringUtils.simpleName;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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

public class DestroyTests {

	private static final Logger logger = LogProvider.getLogger(DbPersistService.class);
	private static final String schema = "dbtest";
	private static DbPersistService persistService;
	private static DerbyEmbeddedMigrationService migrationService;

	/**
	 * Drops all tables in the database, if it exists
	 */
	public final void dropDatabase() {
		logger.info("Dropping database...");

		try {
			Connection connection = persistService.getConnection();
			ResultSet rs = null;
			try {
				rs = connection.getMetaData().getTables(null, "APP", "%", new String[] { "TABLE" });
				while(rs.next()) {
					String sql = "drop table APP." + rs.getString(3);
					logger.debug(sql);
					Statement stmt = connection.createStatement();
					try {
						stmt.executeUpdate(sql);
					} finally{
						stmt.close();
					}
				}
			} finally {
				if(rs != null) {
					rs.close();
				}
				// connection.close(); no need - connection will be closed when the session is closed
			}
			logger.info("Database dropped.\n");
		} catch(SQLException e) {
			// well, something went wrong...
			logger.error("ERROR dropping database", e);
		}
	}
	
	private void migrate(DynModel...models) throws Exception {
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

	private static String pkg;
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

	@Test
	public void testHasOne() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addHasOne("bModel", "BModel.class", "dependent=Relation.DESTROY");
		DynModel bm = DynModels.getClass(pkg, "BModel").addAttr("name", "String.class");
		
		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "name1");
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", b);
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b).destroy();
		
		// double check it works even if bModel is not set
		persistService.executeUpdate("INSERT INTO b_models(name) VALUES(?)", "name1");
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 2);

		a.setId(2);
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 2));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 2));
	}

	@Ignore
	@Test
	public void testHasOne_Bidi() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").addHasOne("bModel", "BModel.class", "dependent=Relation.DESTROY");
		DynModel bm = DynModels.getClass(pkg, "BModel").addHasOne("aModel", "AModel.class", "dependent=Relation.DESTROY");
		
		migrate(am, bm);

//		TODO don't support? this is a cycle in the dependency graph... (what about self-referential tables?)
		
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(null)");
		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b =  spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);

		a.set("bModel", b);
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b).destroy();

		// now, where they both have an instance to the other
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);
		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);

		b = spy(bm.newInstance());
		b.setId(1);
		
		a = am.newInstance();
		a.setId(1);

		b.set("aModel", a);
		a.set("bModel", b);
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b).destroy();
	}

	@Test(expected=StackOverflowError.class)
	public void testHasOneToOne_Bidi() throws Exception {
		
		// DESTROY is not currently supported on both models when linked. maybe in the future...
		
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"", "dependent=Relation.DESTROY");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"", "dependent=Relation.DESTROY");

		Model b = bm.newInstance();
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", b);
		
		a.destroy();
	}
	
	@Test
	public void testHasOneToOne() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"", "dependent=Relation.DESTROY");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = spy(bm.newInstance());
		b.setId(1);
		
		Model a = am.newInstance();
		a.setId(1);
		a.set("bModel", b);
		
		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b).destroy();
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"", "dependent=Relation.DESTROY");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);

		Model b = bm.newInstance();
		b.setId(1);

		Model a = spy(am.newInstance());
		a.setId(1);
		a.set("bModel", b);

		b.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(a).destroy();
	}

	@Test
	public void testHasManyToNone() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "dependent=Relation.DESTROY");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(mk_a_model__b_models) VALUES(?)", 1);

		Model b = spy(bm.newInstance());
		b.setId(1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModels", new Model[] { b });

		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b).destroy();
	}

	@Test
	public void testHasManyToMany() throws Exception {
		DynModel am = DynModels.getClass(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"", "dependent=Relation.DESTROY");
		DynModel bm = DynModels.getClass(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");

		migrate(am, bm);

		persistService.executeUpdate("INSERT INTO a_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
		persistService.executeUpdate("INSERT INTO a_models__b_models___b_models__a_models(a,b) VALUES(?,?)", 1, 1);

		Model b = spy(bm.newInstance());
		b.setId(1);

		Model a = am.newInstance();
		a.setId(1);
		a.set("bModels", new Model[] { b });

		a.destroy();
		
		assertNull(persistService.executeQueryValue("SELECT * from a_models where id=?", 1));
		assertNull(persistService.executeQueryValue("SELECT * from b_models where id=?", 1));
		
		verify(b).destroy();
	}

}
