package org.oobium.build.model;

import static org.junit.Assert.*;
import static org.oobium.utils.FileUtils.*;

import java.io.File;

import org.junit.Test;

public class ModelDefinitionTests {

	@Test
	public void testAttribute() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\tattrs = {\n" +
			"\t\t@Attribute(name=\"name\", type=String.class)\n" +
			"\t}\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}
	
	@Test
	public void testRelation_HasOne() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\thasOne = {\n" +
			"\t\t@Relation(name=\"other\", type=Other.class)\n" +
			"\t}\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}
	
	@Test
	public void testRelation_HasMany() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\thasMany = {\n" +
			"\t\t@Relation(name=\"others\", type=Other.class)\n" +
			"\t}\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}
	
	@Test
	public void testRelation_HasMany_Through() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\thasMany = {\n" +
			"\t\t@Relation(name=\"others\", type=Other.class),\n" +
			"\t\t@Relation(name=\"direct\", type=MyModel.class, through=\"others\")\n" +
			"\t}\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}
	
	@Test
	public void testCombined() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\tattrs = {\n" +
			"\t\t@Attribute(name=\"name\", type=String.class)\n" +
			"\t},\n" +
			"\thasOne = {\n" +
			"\t\t@Relation(name=\"other\", type=Other.class)\n" +
			"\t},\n" +
			"\thasMany = {\n" +
			"\t\t@Relation(name=\"others\", type=Other.class)\n" +
			"\t}\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}

	@Test
	public void testEmbed() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\thasOne = {\n" +
			"\t\t@Relation(name=\"other\", type=Other.class, embed=\"name\")\n" +
			"\t},\n" +
			"\thasMany = {\n" +
			"\t\t@Relation(name=\"others\", type=Other.class, embed=\"name\")\n" +
			"\t}\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}
	
	@Test
	public void testEmbedded() throws Exception {
		String description = 
			"@ModelDescription(\n" +
			"\tembedded = true\n" +
			")";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getDescription());
		assertEquals(description, definition.getDescription());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getDescription());
	}
	
}
