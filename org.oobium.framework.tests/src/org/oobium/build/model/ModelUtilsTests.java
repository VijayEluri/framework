package org.oobium.build.model;

import static org.junit.Assert.*;
import static org.oobium.build.model.ModelUtils.*;

import org.junit.Test;

public class ModelUtilsTests {

	@Test
	public void testFindStart() throws Exception {
		assertEquals(-1, findStart("TestModel", "public classTestModel {}".toCharArray()));
		assertEquals(0, findStart("TestModel", "public class TestModel {}".toCharArray()));
		assertEquals(0, findStart("TestModel", "public class TestModel{}".toCharArray()));
		assertEquals(0, findStart("TestModel", "public  class  TestModel  {}".toCharArray()));
		assertEquals(0, findStart("TestModel", "public\tclass\tTestModel\t{}".toCharArray()));
		assertEquals(0, findStart("TestModel", "private class TestModel{}".toCharArray()));
		assertEquals(0, findStart("TestModel", "class TestModel{}".toCharArray()));
		assertEquals(1, findStart("TestModel", "\npublic class TestModel {\n\n}".toCharArray()));
		assertEquals(33, findStart("TestModel", "/** public class TestModel {} */\npublic class TestModel {\n\n}".toCharArray()));
		assertEquals(20, findStart("TestModel", "class TestModel2 {}\npublic class TestModel {\n\n}".toCharArray()));

		String description = 
				"@ModelDescription(\n" +
				"\tattrs = {\n" +
				"\t\t@Attribute(name=\"name\", type=String.class)\n" +
				"\t}\n" +
				")\n";
		String source = description + "public class TestModel {}";
		assertEquals(description.length(), findStart("TestModel", source.toCharArray()));
	}
	

}
