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

import java.util.concurrent.Future;

import org.oobium.logging.LogProvider;
import org.oobium.persist.Model;

public abstract class RemoteWorker<T> {

	public enum State { Waiting, Running, Completed, Cancelled }
	
	public static class Status {
		public final State state;
		public final float done;
		public Status(State state, float done) {
			this.state = state;
			this.done = done;
		}
	}

	
	long id;
	Runnable task;
	RemoteWorkers workers;
	Future<RemoteWorker<T>> future;
	
	private volatile boolean running;
	private volatile float done;

	private T result;
	private Exception e;
	
	public RemoteWorker() {
		this.task = new Runnable() {
			@Override
			public void run() {
				RemoteWorker.this.execute();
			}
		};
	}
	
	private void execute() {
		running = true;
		workers.serviceProvider.openSession("client worker " + id);
		Model.setLogger(LogProvider.getLogger(HttpPersistService.class));
		Model.setPersistServiceProvider(workers.serviceProvider);
		try {
			result = run();
			onSuccess(result);
		} catch(Exception e) {
			this.e = e;
			onError(e);
		} finally {
			onComplete(result, e);
			workers.serviceProvider.closeSession();
			Model.setPersistServiceProvider(null);
			Model.setLogger(null);
			task = null;
			future = null;
			workers.complete(id);
			running = false;
		}
	}

	public Exception getError() {
		return e;
	}

	public long getId() {
		return id;
	}
	
	public T getResult() {
		return result;
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
	 * Called after the {@link #run()} method has completed and either the
	 * {@link #onSuccess(Object)} method or {@link #onError(Exception)} method has been
	 * called (depended on the outcome of run()).
	 * <p>This method is guaranteed to run regardless of the outcome of the {@link #run()} method.</p>
	 * @param result the result of the run() method, if successful; null otherwise
	 * @param e the uncaught exception thrown in the run() method, if one exists; null otherwise
	 * @see #onSuccess(Object)
	 * @see #onError(Exception)
	 */
	protected void onComplete(T result, Exception e) {
		// subclasses to override if necessary
	}

	/**
	 * Called when the {@link #run()} method throws an uncaught Exception.
	 * @param e the exception thrown
	 * @see #onSuccess(Object)
	 * @see #onComplete(Object, Exception)
	 */
	protected void onError(Exception e) {
		// subclasses to override if necessary
	}

	/**
	 * Called after the {@link #run()} method if it was successful (there were no uncaught Exceptions thrown).
	 * @param result the value returned by the {@link #run()} method
	 * @see #onError(Exception)
	 * @see #onComplete(Object, Exception)
	 */
	protected void onSuccess(T result) {
		// subclasses to override if necessary
	}
	
	protected abstract T run() throws Exception;
	
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
	
	protected void updateStatus(float percentDone) {
		this.done = percentDone;
	}
	
}
