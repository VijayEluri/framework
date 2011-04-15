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

import java.sql.SQLException;
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
	public void commit() throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
		throw new SQLException(msg);
	}
	
	@Override
	public void create(Model...models) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public void destroy(Model...models) throws SQLException {
		throw new SQLException(msg);
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
		throw new SQLException(msg);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getSymbolicName() {
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
			public String getName() {
				return msg;
			}
			@Override
			public String getMigrationService() {
				return null;
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
	public void retrieve(Model...models) throws SQLException {
		throw new SQLException(msg);
	}

	@Override
	public void retrieve(Model model, String hasMany) throws SQLException {
		throw new SQLException(msg);
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
		throw new SQLException(msg);
	}

}
