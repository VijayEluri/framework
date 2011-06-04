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


public interface PersistService {

	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String CLIENT = "client";
	public static final String MIGRATION_SERVICE = "migration.service";
	public static final String SERVICE = "service";
	public static final String MODELS = "models";
	public static final String HOST = "host";
	public static final String DATABASE = "database";

	public static final int SUCCESS = 0;
	public static final int ADAPT_FAILED = 1;
	public static final int UNKNOWN_FAILURE = 2;
	
	
	public abstract ServiceInfo getInfo();
	
	
	public abstract void openSession(String name);

	public abstract void closeSession();
	
	public abstract boolean isSessionOpen();

	
    public abstract void create(Model...models) throws SQLException;

    public abstract void retrieve(Model...models) throws SQLException;

    public abstract void retrieve(Model model, String hasMany) throws SQLException;

    public abstract void update(Model...models) throws SQLException;

	public abstract void destroy(Model...models) throws SQLException;
	
	
	public abstract int count(Class<? extends Model> clazz, String where, Object...values) throws SQLException;
	
	
	public abstract <T extends Model> T find(Class<T> clazz, int id) throws SQLException;
	
	public abstract <T extends Model> T find(Class<T> clazz, String where, Object...values) throws SQLException;
	
	public abstract <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException;

	public abstract <T extends Model> List<T> findAll(Class<T> clazz, String where, Object...values) throws SQLException;
	
	
	public abstract List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException;

	public abstract List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException;

	public abstract Object executeQueryValue(String sql, Object...values) throws SQLException;

	public abstract int executeUpdate(String sql, Object...values) throws SQLException;

}
