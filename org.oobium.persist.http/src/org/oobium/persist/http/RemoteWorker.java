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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.oobium.logging.LogProvider;
import org.oobium.persist.Model;

public class RemoteWorker {

	public enum State { Waiting, Running, Completed, Cancelled }
	
	public static class Status {
		public final State state;
		public final float done;
		public Status(State state, float done) {
			this.state = state;
			this.done = done;
		}
	}

	
	public static long submit(Runnable runnable) {
		RemoteWorker worker = new RemoteWorker(runnable);
		return RemoteWorkers.submit(worker);
	}
	
	
	long id;
	Runnable task;
	RemoteWorkers workers;
	Future<RemoteWorker> future;
	List<Runnable> runnables;
	
	private volatile boolean running;
	private volatile float done;

	private Exception e;
	
	public RemoteWorker() {
		this.task = new Runnable() {
			@Override
			public void run() {
				RemoteWorker.this.execute();
			}
		};
	}
	
	public RemoteWorker(Runnable runnable) {
		this();
		set(runnable);
	}

	public void add(Runnable runnable) {
		if(runnable != null) {
			if(runnables == null) {
				runnables = new ArrayList<Runnable>();
			}
			runnables.add(runnable);
		}
	}
	
	private void execute() {
		running = true;
		workers.serviceProvider.openSession("client worker " + id);
		Model.setLogger(LogProvider.getLogger(HttpPersistService.class));
		Model.setPersistServiceProvider(workers.serviceProvider);
		try {
			if(runnables == null) {
				run();
			} else {
				for(int i = 0; i < runnables.size(); i++) {
					float tmp = done;
					runnables.get(i).run();
					if(done == tmp) {
						done = (float) i / (float) runnables.size();
					}
					if(Thread.interrupted()) {
						throw new InterruptedException();
					}
					Thread.yield();
				}
			}
		} catch(InterruptedException e) {
			// exit execution
		} catch(Exception e) {
			this.e = e;
		} finally {
			workers.serviceProvider.closeSession();
			Model.setPersistServiceProvider(null);
			Model.setLogger(null);
			task = null;
			runnables.clear();
			runnables = null;
			e = null;
			future = null;
			workers.complete(id);
			running = false;
		}
	}
	
	public Exception getException() {
		return e;
	}
	
	public long getId() {
		return id;
	}

	public Status getStatus() {
		if(future.isDone()) {
			return new Status(State.Completed, 1f);
		}
		if(future.isCancelled()) {
			return new Status(State.Cancelled, 1f);
		}
		if(running) {
			return new Status(State.Running, done);
		} else {
			return new Status(State.Waiting, 0f);
		}
	}

	public boolean hasException() {
		return e != null;
	}
	
	/**
	 *  Subclasses to override if not using separate {@link Runnable}.<br>
	 *  Default implementation does nothing.
	 */
	protected void run() throws SQLException {
		// subclasses to override if not using separate Runnable
	}
	
	public void set(Runnable runnable) {
		if(runnable != null) {
			runnables = new ArrayList<Runnable>();
			runnables.add(runnable);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Worker ").append(id).append(' ').append('{');
		if(workers == null) {
			sb.append(" completed ");
		}
		sb.append('}');
		return sb.toString();
	}
	
}
