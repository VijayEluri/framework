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
package org.oobium.session.db;

import java.util.Properties;

import org.oobium.http.HttpSession;
import org.oobium.http.HttpSessionService;
import org.oobium.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DbSessionService implements BundleActivator, HttpSessionService {

	private final Logger logger;

	public DbSessionService() {
		logger = Logger.getLogger(DbSessionService.class);
	}
	
	@Override
	public HttpSession getSession(int id, String uuid, boolean create) {
		HttpSession session = null;
		if(id > 0 && uuid != null && !uuid.isEmpty()) {
			session = DbSession.retrieve(id, uuid);
		}
		if(session == null && create) {
			session = new DbSession();
		}
		return session;
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		logger.setBundle(context.getBundle());
		
		Properties properties = new Properties();
		properties.put(HttpSessionService.TYPE, getClass().getPackage().getName());
		context.registerService(HttpSessionService.class.getName(), this, properties);

		logger.info("SessionService started");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("SessionService stopped");
		logger.setBundle(null);
	}

}
