package org.oobium.build.gen;

import static org.junit.Assert.*;
import static org.oobium.utils.StringUtils.simpleName;

import org.junit.Before;
import org.junit.Test;
import org.oobium.build.model.ModelDefinition;
import org.oobium.framework.tests.dyn.DynModel;
import org.oobium.framework.tests.dyn.DynClasses;

public class DbGeneratorTests {

	@Before
	public void setup() {
		DynClasses.reset();
	}
	
	private String up(String module, DynModel...models) {
		ModelDefinition[] defs = new ModelDefinition[models.length];
		for(int i = 0; i < models.length; i++) {
			defs[i] = new ModelDefinition(simpleName(models[i].getFullName()), models[i].getSource(), DynClasses.getSiblings(models[i]));
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
		int s2 = schema.indexOf("public void down() ");
		while(s2 > s1 && schema.charAt(s2) != '}') {
			s2--;
		}
		if(s2 > s1+2) {
			return schema.substring(s1, s2 - 2).replace("\n\t\t", "\n");
		}
		return "";
	}
	
	@Test
	public void testAttrBigDecimal() throws Exception {
		assertEquals("createTable(\"a_models\",\n" +
					"\tDecimal(\"attr\", Map(\n" +
					"\t\te(\"precision\", 8), \n" +
					"\t\te(\"scale\", 2)\n" +
					"\t))\n" +
					");",
				up("com.test", DynClasses.getModel("AModel").addAttr("attr", "java.math.BigDecimal.class")));
	}
	
	@Test
	public void testAttrBoolean() throws Exception {
		assertEquals("createTable(\"a_models\",\n" +
					"\tBoolean(\"attr\")\n" +
					");",
				up("com.test", DynClasses.getModel("AModel").addAttr("attr", "Boolean.class")));
	}
	
	@Test
	public void testAttrBoolean_Primitive() throws Exception {
		assertEquals("createTable(\"a_models\",\n" +
					"\tBoolean(\"attr\", Map(\n" +
					"\t\te(\"required\", true), \n" +
					"\t\te(\"primitive\", true)\n" +
					"\t))\n" +
					");",
				up("com.test", DynClasses.getModel("AModel").addAttr("attr", "boolean.class")));
	}
	
	@Test
	public void testAttrString() throws Exception {
		assertEquals("createTable(\"a_models\",\n" +
					"\tString(\"attr\")\n" +
					");",
				up("com.test", DynClasses.getModel("AModel").addAttr("attr", "String.class")));
	}
	
	@Test
	public void testAttrText() throws Exception {
		assertEquals("createTable(\"a_models\",\n" +
					"\tText(\"attr\")\n" +
					");",
				up("com.test", DynClasses.getModel("AModel").addAttr("attr", "org.oobium.persist.Text.class")));
	}
	
	@Test
	public void testEmbedded() throws Exception {
		assertEquals("", up("com.test", DynClasses.getModel("AModel").addAttr("attr", "Boolean.class").embedded()));
	}
	
	@Test
	public void testHasOneToNone() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\",\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addIndex(\"b_model\");\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\",\n" +
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
						DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class"),
						DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasOneToNone_OnDeleteCascade() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\",\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addIndex(\"b_model\");\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\",\n" +
					"\tInteger(\"a_model\")\n" +
					");\n" +
					"bModels.addIndex(\"a_model\");\n" +
					"\n" +
					"aModels.addForeignKey(\"b_model\", \"b_models\", Map(\"onDelete\", CASCADE));\n" +
					"aModels.update();\n" +
					"\n" +
					"bModels.addForeignKey(\"a_model\", \"a_models\");\n" +
					"bModels.update();",
				up("com.test",
						DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class", "onDelete=Relation.CASCADE"),
						DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasOneToOne() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\",\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addUniqueIndex(\"b_model\");\n" +
					"\n" +
					"createTable(\"b_models\");\n" +
					"\n" +
					"aModels.addForeignKey(\"b_model\", \"b_models\");\n" +
					"aModels.update();",
				up("com.test",
						DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class", "opposite=\"aModel\""),
						DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class", "opposite=\"bModel\"")
				));

		DynClasses.reset();
		
		assertEquals("Table bModels = createTable(\"b_models\",\n" +
					"\tInteger(\"c_model\")\n" +
					");\n" +
					"bModels.addUniqueIndex(\"c_model\");\n" +
					"\n" +
					"createTable(\"c_models\");\n" +
					"\n" +
					"bModels.addForeignKey(\"c_model\", \"c_models\");\n" +
					"bModels.update();",
				up("com.test",
						DynClasses.getModel("CModel").addHasOne("bModel", "BModel.class", "opposite=\"cModel\""),
						DynClasses.getModel("BModel").addHasOne("cModel", "CModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasOneToMany() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\",\n" +
					"\tInteger(\"b_model\")\n" +
					");\n" +
					"aModels.addIndex(\"b_model\");\n" +
					"\n" +
					"createTable(\"b_models\");\n" +
					"\n" +
					"aModels.addForeignKey(\"b_model\", \"b_models\");\n" +
					"aModels.update();",
				up("com.test",
						DynClasses.getModel("AModel").addHasOne("bModel", "BModel.class", "opposite=\"aModels\""),
						DynClasses.getModel("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModel\"")
				));
	}
	
	@Test
	public void testHasManyToNone() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\");\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\",\n" +
					"\tInteger(\"a_model\")\n" + // this is BModel's hasOne
					");\n" +
					"bModels.addIndex(\"a_model\");\n" +
					"\n" +
					"createJoinTable(aModels, \"b_models\", bModels, \"null\");\n" +
					"\n" +
					"bModels.addForeignKey(\"a_model\", \"a_models\");\n" +
					"bModels.update();",
				up("com.test",
						DynClasses.getModel("AModel").addHasMany("bModels", "BModel.class"),
						DynClasses.getModel("BModel").addHasOne("aModel", "AModel.class")
				));
	}
	
	@Test
	public void testHasManyToMany() throws Exception {
		assertEquals("Table aModels = createTable(\"a_models\");\n" +
					"\n" +
					"Table bModels = createTable(\"b_models\");\n" +
					"\n" +
					"createJoinTable(aModels, \"b_models\", bModels, \"a_models\");",
				up("com.test",
						DynClasses.getModel("AModel").addHasMany("bModels", "BModel.class", "opposite=\"aModels\""),
						DynClasses.getModel("BModel").addHasMany("aModels", "AModel.class", "opposite=\"bModels\"")
				));
	}
	
}
