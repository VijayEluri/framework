package org.oobium.persist.http;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.oobium.framework.tests.dyn.DynClasses;
import org.oobium.framework.tests.dyn.DynModel;

public class ModelFormTests {

	@Test
	public void testAdd() throws Exception {
		DynModel m = DynClasses.getModel("AModel");
		
		ModelForm form = new ModelForm(m.getModelClass());
		form.add("model", "field", "name").as("bob");
		
		Map<String, Object> params = form.getParameters();
		assertEquals(1, params.size());
		assertEquals("model[field][name]", params.keySet().iterator().next());
		assertEquals("bob", params.get("model[field][name]"));
	}
	
	@Test
	public void testAddField() throws Exception {
		DynModel m = DynClasses.getModel("AModel");
		
		ModelForm form = new ModelForm(m.getModelClass());
		form.addField("field", "name").as("joe");
		
		Map<String, Object> params = form.getParameters();
		assertEquals(1, params.size());
		assertEquals("amodel[field][name]", params.keySet().iterator().next());
		assertEquals("joe", params.get("amodel[field][name]"));
	}

	@Test
	public void testAddFieldsFor() throws Exception {
		DynModel am = DynClasses.getModel("Account");
		DynModel tm = DynClasses.getModel("Training");

		File file = new File("");
		ModelForm form = new ModelForm(tm.getModelClass());
		form.addField("media", "data", "bytes").as(file);
		form.addFieldsFor(am.getModelClass())
			.addField("email").as("tester@test.com")
			.addField("key").as("1234");

		Map<String, Object> params = form.getParameters();
		assertEquals(3, params.size());
		assertEquals(file, params.get("training[media][data][bytes]"));
		assertEquals("tester@test.com", params.get("account[email]"));
		assertEquals("1234", params.get("account[key]"));
	}
}
