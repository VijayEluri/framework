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
package org.oobium.server;

import static org.oobium.server.Data.TIMED_OUT;
import static org.oobium.server.Server.maxReadIdleTime;
import static org.oobium.server.Server.maxReadTime;
import static org.oobium.server.Server.maxRequestHandlers;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.oobium.logging.Logger;

public class ReadHandler extends Thread {

	private class ReadTimeOutTask extends TimerTask {
		private final Logger logger;
		private ReadTimeOutTask() {
			logger = Logger.getLogger(Server.class);
		}
		@Override
		public void run() {
			synchronized(dataMap) {
				for(Iterator<Entry<SelectionKey, Data>> iter = dataMap.entrySet().iterator(); iter.hasNext(); ) {
					Entry<SelectionKey, Data> entry = iter.next();
					Data data = entry.getValue();
					long time = System.currentTimeMillis();
					if(time > (data.lastRead + maxReadIdleTime) || time > (data.start + maxReadTime)) {
						if(logger.isLoggingTrace()) {
							logger.trace("timed out: " + data);
						}
						data.state = TIMED_OUT;
						SelectionKey key = entry.getKey();
						key.cancel();
						try {
							key.channel().close();
						} catch(IOException e) {
							logger.warn(e);
						}
						iter.remove();
					}
				}
			}
		}
	}

	private final Logger logger;

	private final ServerSelector selector;
	private final BlockingQueue<Read> readQueue;

	private final int maxData;
	private final Map<SelectionKey, Data> dataMap;
	private final Timer timeOutTimer;

	private final ExecutorService execService;

	public ReadHandler(ServerSelector selector) {
		this.selector = selector;

		logger = Logger.getLogger(Server.class);
		execService = Executors.newFixedThreadPool(maxRequestHandlers);
		
		readQueue = new LinkedBlockingQueue<Read>();
		maxData = 1000;
		dataMap = new HashMap<SelectionKey, Data>();
		timeOutTimer = new Timer("NioReadQ TimeOutTimer", true);
		timeOutTimer.scheduleAtFixedRate(new ReadTimeOutTask(), 1000, 1000);
	}

	private void createInvalidResponse(SelectionKey key, Data data) {
		StringBuilder sb = new StringBuilder();
		sb.append(data.getProtocol()).append(" 400 Bad Request\r\n\r\n\n");
		selector.send(key, sb.toString(), true);
	}
	
	private void process(Read read) {
		Data data = dataMap.get(read.key);
		boolean remove;
		if(data != null) {
			data.add(read.data);
			if(data.invalid()) {
				synchronized(dataMap) {
					dataMap.remove(data);
				}
				createInvalidResponse(read.key, data);
				return;
			}
			remove = true;
		} else {
			data = new Data(read);
			if(data.invalid()) {
				createInvalidResponse(read.key, data);
				return;
			}
			remove = false;
		}
		
		if(data.ready()) {
			if(remove) {
				synchronized(dataMap) {
					dataMap.remove(data);
				}
			}
			RequestHandler handler = new RequestHandler(selector, read.key, data);
			execService.submit(handler);
		} else {
			while(dataMap.size() > maxData) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					return;
				}
			}
			synchronized(dataMap) {
				dataMap.put(read.key, data);
			}
		}
	}

	public void put(SelectionKey key, byte[] data) {
		if(logger.isLoggingTrace()) {
			logger.trace("read:  " + key.channel() + "\n" + new String(data));
		}
		readQueue.add(new Read(key, data));
	}

	@Override
	public void run() {
		while(true) {
			try {
				Read read = readQueue.take();
				process(read);
			} catch(InterruptedException e) {
				logger.info("read handler interupted - exiting");
				break;
			}
		}
	}

}
