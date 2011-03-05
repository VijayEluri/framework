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
package org.oobium.persist.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.oobium.persist.PersistServiceProvider;
import org.oobium.persist.SimplePersistServiceProvider;

public class RemoteWorkers {

	private static final RemoteWorkers instance = new RemoteWorkers();

	public static void setDiscoveryUrl(String url) {
		instance.service.setDiscoveryUrl(url);
	}
	
	public static void shutdown() {
		if(instance.executor != null) {
			instance.executor.shutdown();
		}
	}

	public static <T> long submit(RemoteWorker<T> worker) {
		if(worker.id == 0) { // non-zero id: worker has already been submitted
			worker.workers = instance;
			worker.id = instance.workerCount.addAndGet(1);
			worker.future = instance.executor.submit(worker.task, worker);
			if(instance.workers == null) {
				synchronized(instance) {
					if(instance.workers == null) {
						instance.workers = new HashMap<Long, RemoteWorker<?>>();
					}
				}
			}
			instance.workers.put(worker.id, worker);
		}
		return worker.id;
	}

	
	final HttpPersistService service;
	final PersistServiceProvider serviceProvider;

	private final ExecutorService executor;
	private final AtomicLong workerCount;
	private Map<Long, RemoteWorker<?>> workers;
	
	private RemoteWorkers() {
		service = new HttpPersistService();
		serviceProvider = new SimplePersistServiceProvider(service);
		executor = Executors.newFixedThreadPool(10);
		workerCount = new AtomicLong();
	}
	
	void complete(long id) {
		if(workers != null) {
			RemoteWorker<?> worker = workers.remove(id);
			if(worker != null) {
				worker.workers = null;
			}
			if(workers.isEmpty()) {
				synchronized(instance) {
					if(workers.isEmpty()) {
						workers = null;
					}
				}
			}
		}
	}
	
}
