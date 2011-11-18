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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
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
		System.out.println(definition.getModelDescriptionAnnotation());
		assertEquals(description, definition.getModelDescriptionAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getModelDescriptionAnnotation());
	}
	
	@Test
	public void testValidations_Single() throws Exception {
		String description = "@Validations(@Validate(field=\"name\", isNotBlank=true))";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getValidationsAnnotation());
		assertEquals(description, definition.getValidationsAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getValidationsAnnotation());
	}
	
	@Test
	public void testValidations_Multiple() throws Exception {
		String description = 
			"@Validations({\n" +
			"\t@Validate(field=\"name\", isNotBlank=true),\n" +
			"\t@Validate(field=\"type\", isBlank=true)\n" +
			"})";
		
		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getValidationsAnnotation());
		assertEquals(description, definition.getValidationsAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(description, new ModelDefinition(file).getValidationsAnnotation());
	}

	@Test
	public void testValidations_Multiple_SameField() throws Exception {
		String description = 
			"@Validations({\n" +
			"\t@Validate(field=\"name\", isNotBlank=true),\n" +
			"\t@Validate(field=\"name\", isBlank=true)\n" +
			"})";
		
		String packed = "@Validations(@Validate(field=\"name\", isBlank=true, isNotBlank=true))";

		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getValidationsAnnotation());
		assertEquals(packed, definition.getValidationsAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(packed, new ModelDefinition(file).getValidationsAnnotation());
	}
	
	@Test
	public void testValidations_Multiple_SameValue() throws Exception {
		String description = 
			"@Validations({\n" +
			"\t@Validate(field=\"name\", isNotBlank=true),\n" +
			"\t@Validate(field=\"type\", isNotBlank=true)\n" +
			"})";

		String packed = "@Validations(@Validate(field=\"name,type\", isNotBlank=true))";

		String source = description + "\npublic class TestModel {\n\n}";
		
		File file = writeFile(File.createTempFile("test", null), source);
		
		ModelDefinition definition = new ModelDefinition(file);
		System.out.println(definition.getValidationsAnnotation());
		assertEquals(packed, definition.getValidationsAnnotation());

		definition.save();
		System.out.println(readFile(file));
		assertEquals(packed, new ModelDefinition(file).getValidationsAnnotation());
	}

}
