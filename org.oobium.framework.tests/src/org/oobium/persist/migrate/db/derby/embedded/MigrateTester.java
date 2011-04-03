package org.oobium.persist.migrate.db.derby.embedded;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.StringUtils.simpleName;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.build.gen.DbGenerator;
import org.oobium.build.model.ModelDefinition;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.dyn.DynClass;
import org.oobium.persist.dyn.DynModel;
import org.oobium.persist.dyn.DynModels;
import org.oobium.persist.migrate.AbstractMigration;

public class MigrateTester {

	private static int count; // test counter
	
	private List<String> statements;
	private DerbyEmbeddedMigrationService migrationService;

	@Before
	public void setup() throws Exception {
		DynModels.reset();
		
		statements = new ArrayList<String>();
		
		Statement statement = mock(Statement.class);
		when(statement.executeUpdate(anyString())).thenAnswer(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				String statement = (String) invocation.getArguments()[0];
				statements.add(statement);
				System.out.println(statement);
				return 1;
			}
		});
		
		Connection connection = mock(Connection.class);
		when(connection.createStatement()).thenReturn(statement);
		
		DbPersistService persistor = mock(DbPersistService.class);
		when(persistor.getConnection()).thenReturn(connection);
		
		migrationService = new DerbyEmbeddedMigrationService();
		migrationService.setPersistService(persistor);
	}
	
	private String migrateUp(DynModel...models) throws Exception {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getModelDescription(), DynModels.getSiblings(models[i]));
		}
		DbGenerator gen = new DbGenerator("test" + (count++), "CreateDatabase", defs);
		gen.generate();
		Class<?> clazz = DynClass.getClass(gen.getFullName(), gen.getSource());
		System.out.println(gen.getSource());
		AbstractMigration mig = (AbstractMigration) clazz.newInstance();
		mig.setService(migrationService);
		mig.up();
		return join(statements, '\n');
	}

	@Test
	public void testAttr() throws Exception {
		assertEquals("CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,name VARCHAR(255))",
				migrateUp(DynModels.getClass("AModel").addAttr("name", "String.class")));
	}
	
	@Test
	public void testHasOneToNone() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,b_model INTEGER)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,a_model INTEGER)\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models (id)\n" +
				"CREATE INDEX idx_b_models__a_model ON b_models(a_model)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__a_model_FK Foreign Key (a_model) REFERENCES a_models (id)",
				migrateUp(
					DynModels.getClass("AModel").addHasOne("bModel", "BModel.class"),
					DynModels.getClass("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasOneToOne() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,b_model INTEGER)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models (id)",
				migrateUp(
					DynModels.getClass("AModel").addHasOne("b_model", "BModel.class", "opposite=\"a_model\""),
					DynModels.getClass("BModel").addHasOne("a_model", "AModel.class", "opposite=\"b_model\"")
				));

		DynModels.reset();
		
		assertEquals(
				"CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,b_model INTEGER)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models (id)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,c_model INTEGER)\n" +
				"CREATE TABLE c_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE INDEX idx_b_models__c_model ON b_models(c_model)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__c_model_FK Foreign Key (c_model) REFERENCES c_models (id)",
				migrateUp(
					DynModels.getClass("CModel").addHasOne("bModel", "BModel.class", "opposite=\"cModel\""),
					DynModels.getClass("BModel").addHasOne("cModel", "CModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,b_model INTEGER)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models (id)",
				migrateUp(
					DynModels.getClass("AModel").addHasOne("bModel", "BModel.class", "opposite=\"aModels\""),
					DynModels.getClass("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasManyToNone() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,a_model INTEGER,mk_a_model__b_models INTEGER)\n" +
				"CREATE INDEX idx_b_models__a_model ON b_models(a_model)\n" +
				"CREATE INDEX idx_b_models__mk_a_model__b_models ON b_models(mk_a_model__b_models)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__a_model_FK Foreign Key (a_model) REFERENCES a_models (id)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__mk_a_model__b_models_FK Foreign Key (mk_a_model__b_models) REFERENCES a_models (id)",
				migrateUp(
					DynModels.getClass("AModel").addHasMany("bModels", "BModel.class"),
					DynModels.getClass("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasManyToMany() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE TABLE b_models(id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY)\n" +
				"CREATE TABLE a_models__b_models___b_models__a_models(a INTEGER CONSTRAINT a_models__b_models___b_models__a_models__a_FK REFERENCES b_models (id),b INTEGER CONSTRAINT a_models__b_models___b_models__a_models__b_FK REFERENCES a_models (id))\n" +
				"CREATE INDEX idx_a_models__b_models___b_models__a_models__a ON a_models__b_models___b_models__a_models(a)\n" +
				"CREATE INDEX idx_a_models__b_models___b_models__a_models__b ON a_models__b_models___b_models__a_models(b)",
				migrateUp(
					DynModels.getClass("AModel").addHasMany("bModels", "BModel.class", "opposite=\"aModels\""),
					DynModels.getClass("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModels\"")
				));
	}
	
}
