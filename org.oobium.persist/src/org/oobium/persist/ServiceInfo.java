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
	 * The name of the service as used by trackers.
	 * Typically the name of the class, super class, or interface, depending
	 * where the actual service is defined.
	 * @return the name of the service
	 */
	public abstract String getName();

	/**
	 * The provider, usually a company or person, of this implementation of the service.
	 * @return
	 */
	public abstract String getProvider();

	/**
	 * The version of this service, in the same format as {@link Version}.
	 * @return the version of the service
	 */
	public abstract String getVersion();

	/**
	 * A human readable description of this service and any details about it
	 * that the developers wish to display.
	 * @return the human readable description of the service
	 */
	public abstract String getDescription();
	
}
