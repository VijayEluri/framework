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
package org.oobium.app.persist;

import static org.oobium.utils.coercion.TypeCoercer.coerce;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.PersistService;
import org.oobium.persist.ServiceInfo;
import org.oobium.utils.json.JsonUtils;

public class MemoryPersistService implements PersistService {

	private static final String msg = "unsupported operation";

	private AtomicInteger count;
	private Map<Integer, Map<String, Object>> models;
	private boolean open;

	public MemoryPersistService() {
		count = new AtomicInteger();
		models = new HashMap<Integer, Map<String, Object>>();
	}
	
	@Override
	public void closeSession() {
		open = false;
	}
	
	@Override
	public void commit() throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
		throw new SQLException(msg);
	}
	
	@Override
	public void create(Model...models) throws SQLException {
		for(Model model : models) {
			int id = count.incrementAndGet();
			model.setId(id);
			ModelAdapter adapter = ModelAdapter.getAdapter(model);
			Map<String, Object> data = model.getAll();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("class", model.getClass());
			for(String field : adapter.getFields()) {
				map.put(field, data.get(field));
			}
			this.models.put(id, map);
		}
	}

	@Override
	public void destroy(Model...models) throws SQLException {
		for(Model model : models) {
			Map<String, Object> map = this.models.get(model.getId());
			if(map != null && map.get("class") == model.getClass()) {
				this.models.remove(model.getId());
			}
		}
	}

	@Override
	public List<Map<String, Object>> executeQuery(String sql, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public List<List<Object>> executeQueryLists(String sql, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public Object executeQueryValue(String sql, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public int executeUpdate(String sql, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
		Map<String, Object> map = models.get(id);
		if(map != null && map.get("class") == clazz) {
			return coerce(map, clazz);
		}
		return null;
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		List<T> list = new ArrayList<T>();
		for(Map<String, Object> map : models.values()) {
			if(map.get("class") == clazz) {
				list.add(coerce(map, clazz));
			}
		}
		return list;
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws SQLException {
		if(where != null && where.startsWith("where ")) {
			Map<String, Object> params = new HashMap<String, Object>();
			String[] sa1 = where.substring(6).split("\\s+[aA][nN][dD]\\s+");
			int i = 0;
			for(String s : sa1) {
				String[] sa2 = s.trim().split("\\s*=\\s*", 2);
				Object value = "?".equals(sa2[1]) ? values[i++] : JsonUtils.toObject(sa2[1]);
				params.put(sa2[0].trim(), value);
			}

			List<T> list = new ArrayList<T>();
			for(Map<String, Object> map : models.values()) {
				if(map.get("class") == clazz) {
					boolean match = true;
					for(Entry<String, Object> entry : params.entrySet()) {
						if(!entry.getValue().equals(map.get(entry.getKey()))) {
							match = false;
							break;
						}
					}
					if(match) {
						list.add(coerce(map, clazz));
					}
				}
			}
			return list;
		}
		throw new SQLException(msg);
	}

	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getName() {
				return getClass().getName();
			}
			@Override
			public String getProvider() {
				return "oobium.org";
			}
			@Override
			public String getVersion() {
				return "0.6.0";
			}
			@Override
			public String getDescription() {
				return "Simple in-memory persist service";
			}
		};
	}
	
	@Override
	public boolean isSessionOpen() {
		return open;
	}

	@Override
	public void openSession(String name) {
		open = true;
	}

	@Override
	public void retrieve(Model...models) throws SQLException {
		for(Model model : models) {
			Map<String, Object> map = this.models.get(model.getId());
			if(map != null && map.get("class") == model.getClass()) {
				model.putAll(map);
			}
		}
	}

	@Override
	public void rollback() throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public void update(Model...models) throws SQLException {
		for(Model model : models) {
			Map<String, Object> map = this.models.get(model.getId());
			if(map != null && map.get("class") == model.getClass()) {
				Map<String, Object> data = model.getAll();
				ModelAdapter adapter = ModelAdapter.getAdapter(model);
				for(String field : adapter.getFields()) {
					map.put(field, data.get(field));
				}
			}
		}
	}

}
