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
package org.oobium.persist.migrate;

import static org.oobium.utils.literal.Properties;

import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public abstract class AbstractMigrationService implements MigrationService, BundleActivator {

	protected final Logger logger;
	protected String client;
	
	public AbstractMigrationService() {
		logger = LogProvider.getLogger(getClass());
	}
	
	public AbstractMigrationService(String client, Logger logger) {
		this.client = client;
		this.logger = logger;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		logger.setTag(context.getBundle().getSymbolicName());
		String serviceName = context.getBundle().getSymbolicName();
		context.registerService(MigrationService.class.getName(), this, Properties(MigrationService.SERVICE, serviceName));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.setTag(null);
	}

	@Override
	public void setClient(String client) {
		this.client = client;
	}
	
}
