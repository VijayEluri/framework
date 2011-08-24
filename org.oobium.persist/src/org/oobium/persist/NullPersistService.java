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
package org.oobium.persist;

import java.util.List;
import java.util.Map;


public class NullPersistService implements PersistService {

	private String msg;
	private boolean open;

	/**
	 * Constructs this {@link NullPersistService} and calls {@link #openSession(null)}
	 */
	public NullPersistService() {
		this(null);
	}
	
	/**
	 * Constructs this {@link NullPersistService} and calls {@link #openSession(String)}
	 * @param name the name to use in the call to {@link #openSession(String)}; can be null
	 */
	public NullPersistService(String name) {
		openSession(name);
	}
	
	@Override
	public void closeSession() {
		open = false;
	}
	
	@Override
	public long count(Class<? extends Model> clazz) throws Exception {
		throw new Exception(msg);
	}
	
	@Override
	public long count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public long count(Class<? extends Model> clazz, String query, Object... values) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public void create(Model...models) throws Exception {
		throw new Exception(msg);
	}
	
	@Override
	public void destroy(Model...models) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws Exception {
		throw new Exception(msg);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, String query, Object... values) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String query, Object... values) throws Exception {
		throw new Exception(msg);
	}
	
	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public Class<?> getIdType() {
				return Object.class;
			}
			@Override
			public String getMigrationService() {
				return null;
			}
			@Override
			public String getName() {
				return msg;
			}
			@Override
			public String getProvider() {
				return "oobium.org";
			}
			@Override
			public String getSymbolicName() {
				return getClass().getName();
			}
			@Override
			public String getVersion() {
				return "0.6.0";
			}
		};
	}

	@Override
	public boolean isSessionOpen() {
		return open;
	}

	@Override
	public void openSession(String name) {
		if(name == null) {
			msg = "no PersistService present";
		} else {
			msg = "no PersistService present in " + name;
		}
		open = true;
	}

	@Override
	public void retrieve(Model...models) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public void retrieve(Model model, String hasMany) throws Exception {
		throw new Exception(msg);
	}

	@Override
	public void update(Model...models) throws Exception {
		throw new Exception(msg);
	}

}
