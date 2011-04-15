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
package org.oobium.persist.db.postgresql;

import java.util.Map;

import org.oobium.persist.ServiceInfo;
import org.oobium.persist.db.Database;
import org.oobium.persist.db.DbPersistService;
import org.oobium.persist.db.DbServiceInfo;

public class PostgreSqlPersistService extends DbPersistService {

	public PostgreSqlPersistService() {
		super();
	}
	
	public PostgreSqlPersistService(String client, String url) {
		super(client, url);
	}
	
	@Override
	protected Database createDatabase(String client, Map<String, Object> properties) {
		return new PostgreSqlDatabase(client, properties);
	}

	@Override
	public ServiceInfo getInfo() {
		return new DbServiceInfo(this);
	}

}
