/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.oobium.app.server;

import org.oobium.app.request.Request;
import org.oobium.app.response.Response;


/**
 * Copied from Netty's DefaultChannelFuture implementation; modified for current use.
 */
public abstract class HandlerTask implements Runnable {

	private final Request request;

	private volatile boolean done;
	private volatile Response response;
	private volatile Exception cause;
	private HandlerTaskListener listener;

	public HandlerTask(Request request) {
		this.request = request;
	}
	
	protected abstract Response handleRequest(Request request) throws Exception;
	
	@Override
	public void run() {
		try {
			response = handleRequest(request);
		} catch(Exception e) {
			cause = e;
		} finally {
			setDone();
		}
	}
	
	public Response getResponse() {
		return response;
	}

	public boolean isDone() {
		return done;
	}

	public boolean isSuccess() {
		return done && cause == null;
	}

	public Exception getCause() {
		return cause;
	}

	public void setListener(HandlerTaskListener listener) {
		if(listener == null) {
			this.listener = null;
		} else {
			boolean notifyNow = false;
			if(done) {
				notifyNow = true;
			} else {
				this.listener = listener;
			}

			if(notifyNow) {
				notifyListener();
			}
		}
	}

	public boolean setDone() {
		// Allow only once.
		if(done) {
			return false;
		}

		done = true;

		notifyListener();
		return true;
	}

	private void notifyListener() {
		try {
			listener.onComplete(this);
		} catch(Throwable t) {
//			logger.warn("An exception was thrown by " + ChannelFutureListener.class.getSimpleName() + ".", t);
		}
	}

}
