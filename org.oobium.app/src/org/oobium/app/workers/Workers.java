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
package org.oobium.app.workers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.oobium.app.AppService;
import org.oobium.utils.Config;

public class Workers {

	private final AppService service;
	private final ExecutorService executor;
	private final AtomicLong workerCount;
	
//	private Map<Long, Worker> workers;
	
	
	public Workers(AppService service) {
		this.service = service;
		Config config = Config.loadConfiguration(service.getClass());
		int max = 10;
		String s = config.getString("workers");
		if(s != null) {
			try {
				max = Integer.parseInt(s);
			} catch(Exception e) {
				// discard;
			}
		}
		executor = Executors.newFixedThreadPool(max);
		workerCount = new AtomicLong();
	}
	
	void complete(long id) {
//		TODO redo worker get and complete
//		Worker worker = workers.remove(id);
//		if(worker != null) {
//			worker.workers = null;
//		}
	}
	
	public AppService getService() {
		return service;
	}
	
	public void shutdown() {
		if(executor != null) {
			executor.shutdown();
		}
	}
	
	public boolean submit(Worker worker) {
		if(worker.id != 0) {
			return false; // worker has already been submitted
		}
		
		worker.workers = this;
		worker.id = workerCount.addAndGet(1);
		worker.future = executor.submit(worker.task, worker);
		return true;
	}
	
}
