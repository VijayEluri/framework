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
package org.oobium.persist.db.derby;

import java.util.Map;

import org.oobium.persist.ServiceInfo;
import org.oobium.persist.db.ConnectionPool;
import org.oobium.persist.db.DbPersistService;

public class DerbyPersistService extends DbPersistService {

	@Override
	protected ConnectionPool createConnectionPool(String client, Map<String, Object> properties) {
		return new DerbyConnectionPool(client, properties);
	}

	@Override
	public ServiceInfo getInfo() {
		return new ServiceInfo() {
			@Override
			public String getName() {
				return "Derby Database Persist Service";
			}
			@Override
			public String getProvider() {
				return "oobium.org";
			}
			@Override
			public String getVersion() {
				return "0.5.0";
			}
			@Override
			public String getDescription() {
				return "Persist service for networked derby databases";
			}
		};
	}

	@Override
	public String getPersistServiceName() {
		return "derby";
	}

}
