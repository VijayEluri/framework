package org.oobium.build.gen;

import static org.junit.Assert.*;
import static org.oobium.utils.StringUtils.simpleName;

import org.junit.Before;
import org.junit.Test;
import org.oobium.build.model.ModelDefinition;
import org.oobium.persist.dyn.DynModel;
import org.oobium.persist.dyn.DynModels;

public class DbGeneratorTests {

	@Before
	public void setup() {
		DynModels.reset();
	}
	
	private String up(String module, DynModel...models) {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getModelDescription(), DynModels.getSiblings(models[i]));
		}
		String schema = DbGenerator.generate(module, defs);
		int s1 = schema.indexOf("public void up() ");
		while(s1 < schema.length() && schema.charAt(s1) != '{') {
			s1++;
		}
		s1++;
		while(s1 < schema.length() && Character.isWhitespace(schema.charAt(s1))) {
			s1++;
		}
		return schema.substring(s1, schema.length() - 6).replace("\n\t\t", "\n");
	}
	
	@Test
	public void testAttr() throws Exception {
		assertEquals("createTable(\"a_models\", tableOptions.get(\"a_models\"),\n" +
					"\tString(\"name\")\n" +
					");",
				up("com.test", DynModels.getClass("AModel").addAttr("name", "String.class")));
	}
	
	@Test
	public void testHasOneToNone() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\", tableOptions.get(\"a_models\"),\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addIndex(\"b_model\");\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\", tableOptions.get(\"b_models\"),\n" +
					"\tInteger(\"a_model\")\n" +
					");\n" +
					"bModels.addIndex(\"a_model\");\n" +
					"\n" +
					"aModels.addForeignKey(\"b_model\", \"b_models\");\n" +
					"aModels.update();\n" +
					"\n" +
					"bModels.addForeignKey(\"a_model\", \"a_models\");\n" +
					"bModels.update();",
				up("com.test",
						DynModels.getClass("AModel").addHasOne("bModel", "BModel.class"),
						DynModels.getClass("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasOneToOne() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\", tableOptions.get(\"a_models\"),\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addIndex(\"b_model\");\n" +
					"\n" +
					"createTable(\"b_models\", tableOptions.get(\"b_models\"));\n" +
					"\n" +
					"aModels.addForeignKey(\"b_model\", \"b_models\");\n" +
					"aModels.update();",
				up("com.test",
						DynModels.getClass("AModel").addHasOne("bModel", "BModel.class", "opposite=\"aModel\""),
						DynModels.getClass("BModel").addHasOne("aModel", "AModel.class", "opposite=\"bModel\"")
				));

		DynModels.reset();
		
		assertEquals("Table bModels = createTable(\"b_models\", tableOptions.get(\"b_models\"),\n" +
					"\tInteger(\"c_model\")\n" +
					");\n" +
					"bModels.addIndex(\"c_model\");\n" +
					"\n" +
					"createTable(\"c_models\", tableOptions.get(\"c_models\"));\n" +
					"\n" +
					"bModels.addForeignKey(\"c_model\", \"c_models\");\n" +
					"bModels.update();",
				up("com.test",
						DynModels.getClass("CModel").addHasOne("bModel", "BModel.class", "opposite=\"cModel\""),
						DynModels.getClass("BModel").addHasOne("cModel", "CModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\", tableOptions.get(\"a_models\"),\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addIndex(\"b_model\");\n" +
					"\n" +
					"createTable(\"b_models\", tableOptions.get(\"b_models\"));\n" +
					"\n" +
					"aModels.addForeignKey(\"b_model\", \"b_models\");\n" +
					"aModels.update();",
				up("com.test",
						DynModels.getClass("AModel").addHasOne("bModel", "BModel.class", "opposite=\"aModels\""),
						DynModels.getClass("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasManyToNone() throws Exception {
		assertEquals("createTable(\"a_models\", tableOptions.get(\"a_models\"));\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\", tableOptions.get(\"b_models\"),\n" +
					"\tInteger(\"a_model\"),\n" + // this is BModel's hasOne
					"\tInteger(\"mk_a_model__b_models\")\n" + // this is AModel's hasMany (a 'hidden' key in the BModel table)
					");\n" +
					"bModels.addIndex(\"a_model\");\n" +
					"bModels.addIndex(\"mk_a_model__b_models\");\n" +
					"\n" +
					"bModels.addForeignKey(\"a_model\", \"a_models\");\n" +
					"bModels.addForeignKey(\"mk_a_model__b_models\", \"a_models\");\n" +
					"bModels.update();",
				up("com.test",
						DynModels.getClass("AModel").addHasMany("bModels", "BModel.class"),
						DynModels.getClass("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasManyToMany() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\", tableOptions.get(\"a_models\"));\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\", tableOptions.get(\"b_models\"));\n" +
					"\n" +
					"createJoinTable(aModels, \"b_models\", bModels, \"a_models\");",
				up("com.test",
						DynModels.getClass("AModel").addHasMany("bModels", "BModel.class", "opposite=\"aModels\""),
						DynModels.getClass("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModels\"")
				));
	}
	
}
