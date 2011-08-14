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

public class PersistAdapter implements PersistService {

	private boolean open;
	
	@Override
	public void closeSession() {
		// subclasses to override if necessary
		open = false;
	}

	@Override
	public int count(Class<? extends Model> clazz) throws PersistException {
		// subclasses to override if necessary
		return 0;
	}

	@Override
	public int count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws PersistException {
		// subclasses to override if necessary
		return 0;
	}
	
	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws PersistException {
		// subclasses to override if necessary
		return 0;
	}

	@Override
	public void create(Model... models) throws PersistException {
		// subclasses to override if necessary
	}

	@Override
	public void destroy(Model... models) throws PersistException {
		// subclasses to override if necessary
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws PersistException {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id) throws PersistException {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws PersistException {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object... values) throws PersistException {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws PersistException {
		// subclasses to override if necessary
		return null;
	}
	
	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws PersistException {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object... values) throws PersistException {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public ServiceInfo getInfo() {
		// subclasses to override if necessary
		return null;
	}

	@Override
	public boolean isSessionOpen() {
		return open;
	}

	@Override
	public void openSession(String name) {
		// subclasses to override if necessary
		open = true;
	}

	@Override
	public void retrieve(Model... models) throws PersistException {
		// subclasses to override if necessary
	}

	@Override
	public void retrieve(Model model, String hasMany) throws PersistException {
		// subclasses to override if necessary
	}

	@Override
	public void update(Model... models) throws PersistException {
		// subclasses to override if necessary
	}

}
