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
package org.oobium.app.server.persist;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.oobium.utils.json.JsonUtils.*;


import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oobium.app.persist.PersistServices;
import org.oobium.persist.Model;
import org.oobium.persist.NullPersistService;
import org.oobium.persist.PersistAdapter;
import org.oobium.persist.ServiceInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

public class PersistServicesTests {

	private class Model1 extends Model { }
	private class Model2 extends Model { }
	private class Persistor1 extends PersistAdapter { public ServiceInfo getInfo() { return null; } }
	private class Persistor2 extends PersistAdapter { public ServiceInfo getInfo() { return null; } }
	
	
	private BundleContext context;
	
	@Before
	public void setup() throws Exception {
		context = mock(BundleContext.class);
		
		when(context.createFilter(anyString())).thenAnswer(new Answer<Filter>() {
			@Override
			public Filter answer(InvocationOnMock invocation) throws Throwable {
				Filter filter = mock(Filter.class);
				when(filter.toString()).thenReturn((String) invocation.getArguments()[0]);
				return filter;
			}
		});
		
		final ServiceReference reference1 = mock(ServiceReference.class);
		final ServiceReference reference2 = mock(ServiceReference.class);
		
		when(context.getServiceReferences(anyString(), anyString())).thenAnswer(new Answer<ServiceReference[]>() {
			@Override
			public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
				String filter = (String) invocation.getArguments()[1];
				if(filter.contains("service=" + Persistor1.class.getName())) {
					return new ServiceReference[] { reference1 };
				}
				if(filter.contains("service=" + Persistor2.class.getName())) {
					return new ServiceReference[] { reference2 };
				}
				return null;
			}
		});
		
		when(context.getService(eq(reference1))).thenReturn(new Persistor1());
		when(context.getService(eq(reference2))).thenReturn(new Persistor2());
	}
	
	@Test
	public void testNull() throws Exception {
		PersistServices services = new PersistServices(context, null);
		assertTrue(services.getFor(Model.class) instanceof NullPersistService);
		assertTrue(services.getFor(Model1.class) instanceof NullPersistService);
		assertTrue(services.getFor(Model2.class) instanceof NullPersistService);
	}
	
	@Test
	public void testString() throws Exception {
		String persist = "\"" + Persistor1.class.getName() + "\"";
		Object object = toObject(persist);
		PersistServices services = new PersistServices(context, object);
		assertTrue(services.getFor(Model.class) instanceof Persistor1);
		assertTrue(services.getFor(Model1.class) instanceof Persistor1);
		assertTrue(services.getFor(Model2.class) instanceof Persistor1);
	}
	
	@Test
	public void testMap() throws Exception {
		String persist = "{service:\"" + Persistor1.class.getName() + "\"," +
				"models:[\"" + Model1.class.getName() + "\",\"" + Model2.class.getName() + "\"]}";
		Object object = toObject(persist);
		PersistServices services = new PersistServices(context, object);
		assertTrue(services.getFor(Model.class) instanceof NullPersistService);
		assertTrue(services.getFor(Model1.class) instanceof Persistor1);
		assertTrue(services.getFor(Model2.class) instanceof Persistor1);
	}
	
	@Test
	public void testMap_OneClass() throws Exception {
		String persist = "{service:\"" + Persistor1.class.getName() + "\"," +
				"models:\"" + Model1.class.getName() + "\"}";
		Object object = toObject(persist);
		PersistServices services = new PersistServices(context, object);
		assertTrue(services.getFor(Model.class) instanceof NullPersistService);
		assertTrue(services.getFor(Model1.class) instanceof Persistor1);
		assertTrue(services.getFor(Model2.class) instanceof NullPersistService);
	}
	
	@Test
	public void testList() throws Exception {
		String persist = "[\"" + Persistor1.class.getName() + "\", {service:\"" + Persistor2.class.getName() + "\"," +
				"models:\"" + Model2.class.getName() + "\"}]";
		Object object = toObject(persist);
		PersistServices services = new PersistServices(context, object);
		assertTrue(services.getFor(Model.class) instanceof Persistor1);
		assertTrue(services.getFor(Model1.class) instanceof Persistor1);
		assertTrue(services.getFor(Model2.class) instanceof Persistor2);
	}
	
	@Test
	public void testGetServices() throws Exception {
		assertEquals(1, new PersistServices(context, null).getServiceNames().size());

		String persist = "[\"" + Persistor1.class.getName() + "\", {service:\"" + Persistor2.class.getName() + "\"," +
				"models:\"" + Model2.class.getName() + "\"}]";
		Object object = toObject(persist);
		PersistServices services = new PersistServices(context, object);
		assertEquals(asList(Persistor1.class.getName(), Persistor2.class.getName()), services.getServiceNames());
	}
	
}
