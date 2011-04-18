package org.oobium.persist.migrate.db;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.persist.db.internal.QueryUtils;

public class MySqlMigrateTester extends MigrateTester {

	@Before
	public void setup() throws Exception {
		super.setup(QueryUtils.MYSQL);
	}
	
	@Test
	public void testAttrBoolean() throws Exception {
		assertEquals("CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,attr BOOLEAN) TYPE = INNODB CHARACTER SET UTF8",
				migrateUp(DynClasses.getModel("AModel").addAttr("attr", "Boolean.class")));
	}
	
	@Test
	public void testAttrBoolean_Primitive() throws Exception {
		assertEquals("CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,attr BOOLEAN NOT NULL DEFAULT false) TYPE = INNODB CHARACTER SET UTF8",
				migrateUp(DynClasses.getModel("AModel").addAttr("attr", "boolean.class")));
	}
	
	@Test
	public void testAttrInteger() throws Exception {
		assertEquals("CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,attr INTEGER) TYPE = INNODB CHARACTER SET UTF8",
				migrateUp(DynClasses.getModel("AModel").addAttr("attr", "Integer.class")));
	}
	
	@Test
	public void testAttrInteger_Primitive() throws Exception {
		assertEquals("CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,attr INTEGER NOT NULL DEFAULT 0) TYPE = INNODB CHARACTER SET UTF8",
				migrateUp(DynClasses.getModel("AModel").addAttr("attr", "int.class")));
	}
	
	@Test
	public void testAttrString() throws Exception {
		assertEquals("CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,attr VARCHAR(255)) TYPE = INNODB CHARACTER SET UTF8",
				migrateUp(DynClasses.getModel("AModel").addAttr("attr", "String.class")));
	}
	
	@Test
	public void testHasOneToNone() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,b_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,a_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models(id)\n" +
				"CREATE INDEX idx_b_models__a_model ON b_models(a_model)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__a_model_FK Foreign Key (a_model) REFERENCES a_models(id)",
				migrateUp(
					DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class"),
					DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasOneToNone_OnDeleteCascade() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,b_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,a_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models(id) ON DELETE CASCADE\n" +
				"CREATE INDEX idx_b_models__a_model ON b_models(a_model)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__a_model_FK Foreign Key (a_model) REFERENCES a_models(id)",
				migrateUp(
					DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class", "onDelete=Relation.CASCADE"),
					DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasOneToOne() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,b_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE UNIQUE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models(id)",
				migrateUp(
					DynClasses.getModel("AModel").addHasOne("b_model", "BModel.class", "opposite=\"a_model\""),
					DynClasses.getModel("BModel").addHasOne("a_model", "AModel.class", "opposite=\"b_model\"")
				));
	}

	@Test
	public void testHasOneToOne_OppositeOrder() throws Exception {
		assertEquals(
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,c_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE c_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE UNIQUE INDEX idx_b_models__c_model ON b_models(c_model)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__c_model_FK Foreign Key (c_model) REFERENCES c_models(id)",
				migrateUp(
					DynClasses.getModel("CModel").addHasOne("bModel", "BModel.class", "opposite=\"cModel\""),
					DynClasses.getModel("BModel").addHasOne("cModel", "CModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,b_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE INDEX idx_a_models__b_model ON a_models(b_model)\n" +
				"ALTER TABLE a_models ADD CONSTRAINT a_models__b_model_FK Foreign Key (b_model) REFERENCES b_models(id)",
				migrateUp(
					DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class", "opposite=\"aModels\""),
					DynClasses.getModel("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasManyToNone() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,a_model INTEGER) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE a_models__b_models___b_models__null(a INTEGER REFERENCES b_models(id),b INTEGER REFERENCES a_models(id)) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE INDEX idx_a_models__b_models___b_models__null__a ON a_models__b_models___b_models__null(a)\n" +
				"CREATE INDEX idx_a_models__b_models___b_models__null__b ON a_models__b_models___b_models__null(b)\n" +
				"CREATE INDEX idx_b_models__a_model ON b_models(a_model)\n" +
				"ALTER TABLE b_models ADD CONSTRAINT b_models__a_model_FK Foreign Key (a_model) REFERENCES a_models(id)",
				migrateUp(
					DynClasses.getModel("AModel").addHasMany("bModels", "BModel.class"),
					DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasManyToMany() throws Exception {
		assertEquals(
				"CREATE TABLE a_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE b_models(id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE TABLE a_models__b_models___b_models__a_models(a INTEGER REFERENCES b_models(id),b INTEGER REFERENCES a_models(id)) TYPE = INNODB CHARACTER SET UTF8\n" +
				"CREATE INDEX idx_a_models__b_models___b_models__a_models__a ON a_models__b_models___b_models__a_models(a)\n" +
				"CREATE INDEX idx_a_models__b_models___b_models__a_models__b ON a_models__b_models___b_models__a_models(b)",
				migrateUp(
					DynClasses.getModel("AModel").addHasMany("bModels", "BModel.class", "opposite=\"aModels\""),
					DynClasses.getModel("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModels\"")
				));
	}
	
}
