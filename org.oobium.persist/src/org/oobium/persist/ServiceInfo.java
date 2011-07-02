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


public interface ServiceInfo {

	/**
	 * The Bundle-SymbolicName of the MigratorService to use for this PersistService, if one
	 * exists. Typically taken from the "Oobium-MigrationService" manifest header.
	 * @return the symbolic name of the appropriate migration service for this persist service.
	 */
	public abstract String getMigrationService();

	/**
	 * A more descriptive, human readable, name of this service.
	 * Often taken from the "Bundle-Name" manifest header;
	 * @return the human readable name of the service
	 */
	public abstract String getName();

	/**
	 * The provider, usually a company or person, of this implementation of the service.
	 * Often taken from the "Bundle-Vendor" manifest header;
	 * @return the provider of this persist service
	 */
	public abstract String getProvider();

	/**
	 * The name of the service as used by trackers.
	 * Often taken from the "Bundle-SymbolicName" manifest header;
	 * @return the name of the service
	 */
	public abstract String getSymbolicName();
	
	/**
	 * The version of this service, in the same format as {@link Version}.
	 * Often taken from the "Bundle-Version" manifest header;
	 * @return the version of the service
	 */
	public abstract String getVersion();
	
}
