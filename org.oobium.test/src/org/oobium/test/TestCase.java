/*******************************************************************************
 * Copyright (c) 2010 Oobium, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
 ******************************************************************************/
package org.oobium.test;

import static org.oobium.utils.SqlUtils.safeSqlWord;
import static org.oobium.utils.StringUtils.columnName;
import static org.oobium.utils.StringUtils.join;
import static org.oobium.utils.StringUtils.tableName;
import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.db.derby.embedded.DerbyEmbeddedPersistService;
import org.oobium.utils.json.JsonUtils;

/**
 * fundamentally broken - do not use
 * @deprecated
 */
public abstract class TestCase {

	protected static final DbPersistService service = new DerbyEmbeddedPersistService("test", true);

	private static final NumberFormat nf = NumberFormat.getInstance();
	static {
		nf.setMinimumIntegerDigits(2);
	}

	private static String projectName(String className) {
		String modelsSegment = ".models.";
		int ix = className.indexOf(modelsSegment);
		if(ix != -1) {
			return className.substring(0, ix);
		}
		String viewsSegment = ".views.";
		ix = className.indexOf(viewsSegment);
		if(ix != -1) {
			return className.substring(0, ix);
		}
		String controllersSegment = ".controllers.";
		ix = className.indexOf(controllersSegment);
		if(ix != -1) {
			return className.substring(0, ix);
		}
		return null;
	}
	
	@AfterClass
	public static void tearDownClass() {
//		migration.service().closeSession();
//		migration = null;
	}
	
	protected final Logger logger;

	private Map<String, Map<String, Object>> fixtures;
	private Map<String, Object> models;

	public TestCase() {
		logger = Logger.getLogger(getClass());
	}

	protected void resetSession() {
		service.closeSession();
		service.openSession(projectName(getClass().getCanonicalName()));
	}

	private void checkService() throws Exception {
		if(service == null) {
//			service = new DbPersistService("test", true);
//			createDatabase();
		}
	}
	
	protected Object fixtureValue(String fixtureName, String fieldName) {
		Map<String, Object> fixture = getFixture(fixtureName);
		return fixture.get(fieldName);
	}

	protected Object getDbValue(Model model, String field) throws NoSuchFieldException, SQLException {
		ModelAdapter adapter = ModelAdapter.getAdapter(model.getClass());
		String table = tableName(adapter.getModelClass());
		String column = safeSqlWord(columnName(field));
		String sql = "SELECT " + column + " FROM " + table + " WHERE id=" + model.getId();
		logger.debug(sql);
		Object value = service.executeQueryValue(sql);
		if(adapter.getClass(field) == Boolean.class && value instanceof Integer) {
			return ((Integer) value == 1);
		}
		return value;
	}

	private Map<String, Object> getFixture(String fixtureName) {
		Map<String, Object> fixture = fixtures.get(fixtureName);
		if(fixture == null) {
			throw new RuntimeException("fixture " + fixtureName + " does not exist");
		}
		return fixture;
	}

	protected <T extends Model> T load(Class<T> clazz) throws Exception {
		return load(clazz, 1);
	}

	protected <T extends Model> T load(Class<T> clazz, int fixtureID) throws Exception {
		return load(clazz, clazz.getSimpleName() + nf.format(fixtureID));
	}
	
	protected <T extends Model> T load(Class<T> clazz, String fixtureName) throws Exception {
		T model = clazz.cast(models.get(fixtureName));
		if(model != null) {
			return model;
		}
		
		model = clazz.newInstance();
		models.put(fixtureName, model);
		ModelAdapter adapter = ModelAdapter.getAdapter(clazz);
		
		Map<String, Object> fixture = getFixture(fixtureName);
		for(String field : fixture.keySet()) {
			if(adapter.isReadOnly(field)) {
				throw new RuntimeException("cannot set readOnly field " + field + " in " + model);
			}
			if(adapter.hasAttribute(field)) {
				model.set(field, coerce(fixture.get(field), adapter.getClass(field)));
			} else if(adapter.hasOne(field)) {
				int fixtureId = (Integer) fixture.get(field);
				model.set(field, load(adapter.getHasOneClass(field), fixtureId));
			} else {
				logger.warn("cannot load hasMany field " + field + " in " + model);
			}
		}

		return model;
	}

	@SuppressWarnings("unchecked")
	private void loadFixtures() throws Exception {
		InputStream is = getClass().getResourceAsStream("Fixtures.js");
		if(is == null) {
			throw new IOException("Fixtures.js does not exist");
		}
		
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			int c;
			while((c = reader.read()) != -1) {
				sb.append((char) c);
			}
		} finally {
			reader.close();
		}
		
		fixtures = (Map<String, Map<String, Object>>) JsonUtils.toObject(sb.toString());
	}

	protected void refreshDatabase() throws Exception {
//		migration.refreshDatabase();
	}

	/**
	 * saves the fixture data directly to the database and returns an unresolved
	 * model object with its id set to the newly created record.
	 */
	protected <T extends Model> T saveFixture(Class<T> clazz, String fixtureName) throws Exception {
		Map<String, Object> fixture = getFixture(fixtureName);

		T model = clazz.newInstance();
		ModelAdapter adapter = ModelAdapter.getAdapter(clazz);

		List<String> manyToManys = new ArrayList<String>();
		
		String table = tableName(clazz);
		LinkedHashMap<String, Object> fields = new LinkedHashMap<String, Object>();
		for(String field : adapter.getFields()) {
			if(fixture.containsKey(field)) {
				if(adapter.hasOne(field)) {
					Integer fId = coerce(fixture.get(field), Integer.class);
					Class<? extends Model> fType = adapter.getHasOneClass(field);
					String fName = fType.getSimpleName() + nf.format(fId);
					saveFixture(fType, fName);
					fields.put(field, fId);
				} else if(adapter.hasMany(field)) {
					if(adapter.isManyToMany(field)) {
						manyToManys.add(field);
					} else {
						logger.error("can't save ManyToOne relations yet...");
					}
				} else {
					fields.put(field, coerce(fixture.get(field), adapter.getClass(field)));
				}
			}
		}
		
		List<String> columns = new ArrayList<String>();
		for(String field : fields.keySet()) {
			columns.add(safeSqlWord(columnName(field)));
		}
		if(adapter.isDateStamped()) {
			columns.add("created_on");
			columns.add("updated_on");
		}
		if(adapter.isTimeStamped()) {
			columns.add("created_at");
			columns.add("updated_at");
		}

		List<String> values = new ArrayList<String>();
		for(int i = 0; i < fields.size(); i++) {
			values.add("?");
		}
		if(adapter.isDateStamped()) {
			values.add("CURRENT_DATE");
			values.add("CURRENT_DATE");
		}
		if(adapter.isTimeStamped()) {
			values.add("CURRENT_TIMESTAMP");
			values.add("CURRENT_TIMESTAMP");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(table).append('(').append(join(columns, ','));
		sb.append(") VALUES(").append(join(values, ','));
		sb.append(')');

		String sql = sb.toString();
		logger.debug(sql);

		int id = service.executeUpdate(sql, fields.values().toArray());
		model.setId(id);
		
		return model;
	}
	
	protected int setDbValue(Model model, String field, Object value) throws Exception {
		String table = tableName(model.getClass());
		String column = safeSqlWord(columnName(field));
		return service.executeUpdate("UPDATE " + table + " set " + column + "=? WHERE id=" + model.getId());
	}

	@Before
	public void setUp() throws Exception {
		checkService();
		service.openSession("test");
		loadFixtures();
		models = new HashMap<String, Object>();
	}

	@After
	public void tearDown() throws Exception {
		service.closeSession();
//		service = null;
		if(models != null) {
			models.clear();
			models = null;
		}
	}
	
}
