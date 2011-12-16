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

public class Remote {

	private static final Remote instance = new Remote();

	public static void shutdown() {
		if(instance.executor != null) {
			instance.executor.shutdown();
		}
	}

	public static <T> long asyncExec(RemoteRunnable<T> worker) {
		if(worker.id == 0) { // non-zero id: worker has already been submitted
			worker.remoteInstance = instance;
			worker.id = instance.workerCount.addAndGet(1);
			worker.future = instance.executor.submit(worker.task, worker);
			instance.workers.put(worker.id, worker);
		}
		return worker.id;
	}

	public static long asyncExec(final Runnable runnable) {
		return asyncExec(new RemoteRunnable<Object>() {
			@Override
			protected Object run() throws Exception {
				runnable.run();
				return null;
			}
		});
	}

	public static <T> T syncExec(RemoteRunnable<T> worker) {
		asyncExec(worker);
		while(!worker.isDone()) {
			Thread.yield();
		}
		return worker.getResult();
	}
	
	public static void syncExec(final Runnable runnable) {
		syncExec(new RemoteRunnable<Object>() {
			@Override
			protected Object run() throws Exception {
				runnable.run();
				return null;
			}
		});
	}


	final HttpPersistService service;
	final PersistServiceProvider serviceProvider;

	private final ExecutorService executor;
	private final AtomicLong workerCount;
	private final Map<Long, RemoteRunnable<?>> workers;
	
	private Remote() {
		service = new HttpPersistService();
		serviceProvider = new SimplePersistServiceProvider(service);
		executor = Executors.newFixedThreadPool(10);
		workerCount = new AtomicLong();
		workers = new HashMap<Long, RemoteRunnable<?>>();
	}
	
	void complete(long id) {
		if(workers != null) {
			RemoteRunnable<?> worker = workers.remove(id);
			if(worker != null) {
				worker.remoteInstance = null;
			}
		}
	}
	
	public RemoteRunnable<?> get(long id) {
		return workers.get(id);
	}
	
}
