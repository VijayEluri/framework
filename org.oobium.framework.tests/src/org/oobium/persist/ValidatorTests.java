package org.oobium.persist;

import static org.junit.Assert.*;
import static org.oobium.utils.literal.*;
import static org.oobium.utils.StringUtils.asString;

import org.junit.Test;

public class ValidatorTests {

	@ModelDescription(validations={ @Validate(field="name", isBlank=true) })
	public static class TestIsBlank extends Model { }
	
	@Test
	public void testIsBlank() throws Exception {
		TestIsBlank test = new TestIsBlank();
		assertTrue(test.canSave());

		test.set("name", "");
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertFalse(test.canSave());
		assertEquals("[must be blank]", asString(test.getErrors().get("name")));
	}

	
	@ModelDescription(validations={ @Validate(field="name", isNotBlank=true) })
	public static class TestIsNotBlank extends Model { }
	
	@Test
	public void testIsNotBlank() throws Exception {
		TestIsNotBlank test = new TestIsNotBlank();
		assertFalse(test.canSave());
		assertEquals("[cannot be blank]", asString(test.getErrors().get("name")));

		test.set("name", "");
		assertFalse(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());
	}
	

	@ModelDescription(validations={ @Validate(field="name, content", isNotBlank=true) })
	public static class TestIsNotBlank_MultiField extends Model { }
	
	@Test
	public void testIsNotBlank_MultiField() throws Exception {
		TestIsNotBlank_MultiField test = new TestIsNotBlank_MultiField();
		assertFalse(test.canSave());
		assertEquals(2, test.getErrors().size());
		assertEquals("[cannot be blank]", asString(test.getErrors().get("name")));
		assertEquals("[cannot be blank]", asString(test.getErrors().get("content")));

		test.set("name", "");
		assertFalse(test.canSave());

		test.set("name", "hello");
		assertFalse(test.canSave());

		test.set("content", "what's up?");
		assertTrue(test.canSave());
	}
	

	@ModelDescription(validations={ @Validate(field="name", isIn="bob, joe, bert") })
	public static class TestIsIn extends Model { }
	
	@Test
	public void testIsIn() throws Exception {
		TestIsIn test = new TestIsIn();
		assertFalse(test.canSave());
		assertEquals("[can only be one of \"bob, joe, bert\"]", asString(test.getErrors().get("name")));

		test.set("name", "hello");
		assertFalse(test.canSave());

		test.set("name", "joe");
		assertTrue(test.canSave());
	}

		
	@ModelDescription(validations={ @Validate(field="name", isNotIn="bob, joe, bert") })
	public static class TestIsNotIn extends Model { }
	
	@Test
	public void testIsNotIn() throws Exception {
		TestIsNotIn test = new TestIsNotIn();
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());

		test.set("name", "joe");
		assertFalse(test.canSave());
		assertEquals("[cannot be one of \"bob, joe, bert\"]", asString(test.getErrors().get("name")));
	}

	
	@ModelDescription(validations={ @Validate(field="name", isNotNull=true) })
	public static class TestIsNotNull extends Model { }
	
	@Test
	public void testIsNotNull() throws Exception {
		TestIsNotNull test = new TestIsNotNull();
		assertFalse(test.canSave());
		assertEquals("[cannot be null]", asString(test.getErrors().get("name")));

		test.set("name", "");
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());
	}

	
	@ModelDescription(validations={ @Validate(field="name", isNull=true) })
	public static class TestIsNull extends Model { }
	
	@Test
	public void testIsNull() throws Exception {
		TestIsNull test = new TestIsNull();
		assertTrue(test.canSave());

		test.set("name", "");
		assertFalse(test.canSave());
		assertEquals("[must be null]", asString(test.getErrors().get("name")));

		test.set("name", "hello");
		assertFalse(test.canSave());
	}

	
	@ModelDescription(validations={ @Validate(field="name", lengthIs=3) })
	public static class TestLengthIs extends Model { }
	
	@Test
	public void testLengthIs() throws Exception {
		TestLengthIs test = new TestLengthIs();
		assertFalse(test.canSave());
		assertEquals("[length must be 3]", asString(test.getErrors().get("name")));

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertFalse(test.canSave());

		test.set("name", new int[] { 1, 2, 3 });
		assertTrue(test.canSave());
		test.set("name", new int[] { 1, 2 });
		assertFalse(test.canSave());

		test.set("name", List(1, 2, 3));
		assertTrue(test.canSave());
		test.set("name", List(1, 2, 3, 4));
		assertFalse(test.canSave());

		test.set("name", Map(1, 1));
		assertFalse(test.canSave());
		test.set("name", Map(e(1, 1), e(2, 2), e(3, 3)));
		assertTrue(test.canSave());
	}

	
	@ModelDescription(validations = { @Validate(field = "name", matches = "\\w+") })
	public static class TestMatches extends Model { public String toString() { return "test"; } }
	
	@Test
	public void testMatches() throws Exception {
		TestMatches test = new TestMatches();
		assertFalse(test.canSave());
		assertEquals("[must be of format \"\\w+\"]", asString(test.getErrors().get("name")));

		test.set("name", "");
		assertFalse(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());
		
		test.set("name", "123");
		assertTrue(test.canSave());
		
		test.set("name", new int[] { 1, 2, 3 });
		assertFalse(test.canSave());
		
		test.set("name", test);
		assertTrue(test.canSave());
	}

	
	@ModelDescription(validations={ @Validate(field="name", maxLength=3) })
	public static class TestMaxLength extends Model { }
	
	@Test
	public void testMaxLength() throws Exception {
		TestMaxLength test = new TestMaxLength();
		assertTrue(test.canSave());

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertFalse(test.canSave());
		assertEquals("[length cannot be more than 3]", asString(test.getErrors().get("name")));

		test.set("name", new int[] { 1, 2, 3 });
		assertTrue(test.canSave());
		test.set("name", new int[] { 1, 2 });
		assertTrue(test.canSave());

		test.set("name", List(1, 2, 3));
		assertTrue(test.canSave());
		test.set("name", List(1, 2, 3, 4));
		assertFalse(test.canSave());

		test.set("name", Map(1, 1));
		assertTrue(test.canSave());
		test.set("name", Map(e(1, 1), e(2, 2), e(3, 3)));
		assertTrue(test.canSave());
	}

		
	@ModelDescription(validations={ @Validate(field="name", minLength=3) })
	public static class TestMinLength extends Model { }
	
	@Test
	public void testMinLength() throws Exception {
		TestMinLength test = new TestMinLength();
		assertFalse(test.canSave());
		assertEquals("[length cannot be less than 3]", asString(test.getErrors().get("name")));

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());

		test.set("name", "hi");
		assertFalse(test.canSave());

		test.set("name", new int[] { 1, 2, 3 });
		assertTrue(test.canSave());
		test.set("name", new int[] { 1, 2 });
		assertFalse(test.canSave());

		test.set("name", List(1, 2, 3));
		assertTrue(test.canSave());
		test.set("name", List(1, 2, 3, 4));
		assertTrue(test.canSave());

		test.set("name", Map(1, 1));
		assertFalse(test.canSave());
		test.set("name", Map(e(1, 1), e(2, 2), e(3, 3)));
		assertTrue(test.canSave());
	}


	@ModelDescription(validations={ @Validate(field="content", minLength=3, tokenizer="\\W+") })
	public static class TestMinLength_WithTokenizer extends Model { }
	
	@Test
	public void testMinLength_WithTokenizer() throws Exception {
		TestMinLength_WithTokenizer test = new TestMinLength_WithTokenizer();
		assertFalse(test.canSave());
		assertEquals("[length cannot be less than 3]", asString(test.getErrors().get("content")));

		test.set("content", "hello world!");
		assertFalse(test.canSave());

		test.set("content", "goodbye cruel world a...");
		assertTrue(test.canSave());
	}


	@ModelDescription(validations={ @Validate(field="name", minLength=4, unless="isSpecial") })
	public static class TestUnless extends Model {
		@SuppressWarnings("unused")
		private boolean isSpecial() {
			return "bob".equals(get("name"));
		}
	}
	
	@Test
	public void testUnless() throws Exception {
		TestUnless test = new TestUnless();
		assertFalse(test.canSave());
		assertEquals("[length cannot be less than 4]", asString(test.getErrors().get("name")));

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "joe");
		assertFalse(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());

		test.set("name", "hi");
		assertFalse(test.canSave());
	}

	
	@ModelDescription(
			attrs={ @Attribute(name="name", type=String.class) },
			validations={ @Validate(field="name", minLength=4, unless="isSpecial") })
	public static class TestUnless_WithAttr extends Model {
		@SuppressWarnings("unused")
		private boolean isSpecial(String name) {
			return "bob".equals(name);
		}
	}
	
	@Test
	public void testUnless_WithAttr() throws Exception {
		TestUnless_WithAttr test = new TestUnless_WithAttr();
		assertFalse(test.canSave());
		assertEquals("[length cannot be less than 4]", asString(test.getErrors().get("name")));

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "joe");
		assertFalse(test.canSave());

		test.set("name", "hello");
		assertTrue(test.canSave());

		test.set("name", "hi");
		assertFalse(test.canSave());
	}

	
	@ModelDescription(validations={ @Validate(field="name", matches="\\d+", unlessBlank=true) })
	public static class TestUnlessBlank extends Model { }
	
	@Test
	public void testUnlessBlank() throws Exception {
		TestUnlessBlank test = new TestUnlessBlank();
		assertTrue(test.canSave());

		test.set("name", "");
		assertTrue(test.canSave());

		test.set("name", "hello");
		assertFalse(test.canSave());
		assertEquals("[must be of format \"\\d+\"]", asString(test.getErrors().get("name")));

		test.set("name", "123");
		assertTrue(test.canSave());
	}


	@ModelDescription(validations={ @Validate(field="name", matches="\\d+", unlessNull=true) })
	public static class TestUnlessNull extends Model { }
	
	@Test
	public void testUnlessNull() throws Exception {
		TestUnlessNull test = new TestUnlessNull();
		assertTrue(test.canSave());

		test.set("name", "");
		assertFalse(test.canSave());
		assertEquals("[must be of format \"\\d+\"]", asString(test.getErrors().get("name")));

		test.set("name", "hello");
		assertFalse(test.canSave());

		test.set("name", "123");
		assertTrue(test.canSave());
	}


	@ModelDescription(validations={ @Validate(field="name", minLength=4, when="isSonOfBob") })
	public static class TestWhen extends Model {
		@SuppressWarnings("unused")
		private boolean isSonOfBob() {
			return "bob".equals(get("father"));
		}
	}
	
	@Test
	public void testWhen() throws Exception {
		TestWhen test = new TestWhen();
		assertTrue(test.canSave());

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("father", "bob");
		
		test.set("name", "bob");
		assertFalse(test.canSave());
		assertEquals("[length cannot be less than 4]", asString(test.getErrors().get("name")));

		test.set("father", "joe");
		test.set("name", "bob");
		assertTrue(test.canSave());
	}

	
	@ModelDescription(validations={ @Validate(with=TestValidator.class) })
	public static class TestWith extends Model { }
	public static class TestValidator implements Validator<TestWith> { 
		public void validate(TestWith model) { 
			if(!"bob".equals(model.get("name"))) {
				model.addError("name", "not bob");
			}
		}
	}
	
	@Test
	public void testWith() throws Exception {
		TestWith test = new TestWith();
		assertFalse(test.canSave());
		assertEquals("[not bob]", asString(test.getErrors().get("name")));

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "joe");
		assertFalse(test.canSave());
	}

	
	@ModelDescription(validations={ @Validate(withMethod="isBob") })
	public static class TestWithMethod extends Model {
		public void isBob() { 
			if(!"bob".equals(get("name"))) {
				addError("name", "not bob");
			}
		}
	}
	
	@Test
	public void testWithMethod() throws Exception {
		TestWithMethod test = new TestWithMethod();
		assertFalse(test.canSave());
		assertEquals("[not bob]", asString(test.getErrors().get("name")));

		test.set("name", "bob");
		assertTrue(test.canSave());

		test.set("name", "joe");
		assertFalse(test.canSave());
	}

}
