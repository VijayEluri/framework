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

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oobium.http.HttpRequest404Handler;
import org.oobium.http.HttpRequest500Handler;
import org.oobium.http.HttpRequestHandler;
import org.oobium.http.HttpResponse;
import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;

public class ServerSelector implements Runnable {

	private Logger logger;
	
	private Selector selector;
	private ByteBuffer readBuffer;
	private Map<Integer, ServerSocketChannel> channels;
	private Map<Integer, List<HttpRequestHandler>> requestHandlers;
	private ReadHandler readHandler;
	private Map<SelectionKey, List<Object>> writeQ;
	
	private Map<Integer, List<HttpRequest404Handler>> request404Handlers;
	private Map<Integer, List<HttpRequest500Handler>> request500Handlers;
	
	private volatile boolean addingRequestHandler;

	public ServerSelector() {
		logger = LogProvider.getLogger(Server.class);
		
		try {
			selector = Selector.open();
			
			readBuffer = ByteBuffer.allocate(8192);
			channels = new HashMap<Integer, ServerSocketChannel>();
			readHandler = new ReadHandler(this);
			readHandler.start();
			writeQ = new HashMap<SelectionKey, List<Object>>();
		} catch(IOException e) {
			logger.error("could not open selector", e);
		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel channel = ssc.accept();
		channel.configureBlocking(false);
		channel.register(selector, OP_READ);
	}

	public HttpRequest404Handler addHandler(HttpRequest404Handler handler, int port) {
		if(request404Handlers == null) {
			request404Handlers = new HashMap<Integer, List<HttpRequest404Handler>>();
		}
		List<HttpRequest404Handler> handlers = request404Handlers.get(port);
		if(handlers == null) {
			handlers = new ArrayList<HttpRequest404Handler>();
			request404Handlers.put(port, handlers);
		}
		if(!handlers.contains(handler)) {
			handlers.add(handler);
		}
		return handler;
	}
	
	public HttpRequest500Handler addHandler(HttpRequest500Handler handler, int port) {
		if(request500Handlers == null) {
			request500Handlers = new HashMap<Integer, List<HttpRequest500Handler>>();
		}
		List<HttpRequest500Handler> handlers = request500Handlers.get(port);
		if(handlers == null) {
			handlers = new ArrayList<HttpRequest500Handler>();
			request500Handlers.put(port, handlers);
		}
		if(!handlers.contains(handler)) {
			handlers.add(handler);
		}
		return handler;
	}
	
	public HttpRequestHandler addHandler(HttpRequestHandler handler) {
		if(selector.isOpen()) {
			synchronized(channels) {
				int port = handler.getPort();
				if(channels.containsKey(port)) {
					addHandler(handler, port);
					logger.info("incremented count of port " + port + " to " + requestHandlers.get(port).size());
				} else {
					logger.info("starting server on port " + port);
					try {
						ServerSocketChannel channel = ServerSocketChannel.open();
						channel.configureBlocking(false);
						channel.socket().bind(new InetSocketAddress(port));
						addingRequestHandler = true;
						selector.wakeup();
						channel.register(selector, OP_ACCEPT);
						addingRequestHandler = false;
						channels.put(port, channel);
						addHandler(handler, port);
						logger.info("  server started on port " + port);
					} catch(IOException e) {
						logger.error("could not listen on port " + port + ": " + e.getLocalizedMessage());
					}
				}
			}
		}
		return handler;
	}
	
	private void addHandler(HttpRequestHandler handler, int port) {
		if(requestHandlers == null) {
			requestHandlers = new HashMap<Integer, List<HttpRequestHandler>>();
		}
		List<HttpRequestHandler> handlers = requestHandlers.get(port);
		if(handlers == null) {
			handlers = new ArrayList<HttpRequestHandler>();
			requestHandlers.put(port, handlers);
		}
		if(!handlers.contains(handler)) {
			handlers.add(handler);
		}
	}
	
	public void close() {
		try {
			selector.close();
		} catch(IOException e) {
			logger.warn(e);
		}
		for(ServerSocketChannel channel : channels.values()) {
			try {
				channel.close();
			} catch(IOException e) {
				logger.warn(e);
			}
		}
	}

	public int[] getPorts() {
		if(selector.isOpen()) {
			synchronized(channels) {
				Integer[] ia = channels.keySet().toArray(new Integer[channels.size()]);
				int[] ports = new int[channels.size()];
				for(int i = 0; i < ports.length; i++) {
					ports[i] = ia[i];
				}
				return ports;
			}
		}
		return new int[0];
	}
	
	public List<HttpRequest404Handler> getRequest404Handlers(int port) {
		if(request404Handlers != null) {
			List<HttpRequest404Handler> handlers = request404Handlers.get(port);
			if(handlers != null) {
				return handlers;
			}
		}
		return new ArrayList<HttpRequest404Handler>(0);
	}
	
	public List<HttpRequest500Handler> getRequest500Handlers(int port) {
		if(request500Handlers != null) {
			List<HttpRequest500Handler> handlers = request500Handlers.get(port);
			if(handlers != null) {
				return handlers;
			}
		}
		return new ArrayList<HttpRequest500Handler>(0);
	}
	
	public List<HttpRequestHandler> getRequestHandlers(int port) {
		if(requestHandlers != null) {
			List<HttpRequestHandler> handlers = requestHandlers.get(port);
			if(handlers != null) {
				return handlers;
			}
		}
		return new ArrayList<HttpRequestHandler>(0);
	}
	
	public boolean isInited() {
		return selector != null;
	}
	
	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		
		readBuffer.clear();
		
		int read;
		try {
			read = channel.read(readBuffer);
		} catch(IOException e) {
			key.cancel();
			channel.close();
			return;
		}
		
		if(read == -1) {
			key.cancel();
			channel.close();
		} else {
			byte[] ba = new byte[read];
			System.arraycopy(readBuffer.array(), 0, ba, 0, read);
			readHandler.put(key, ba);
		}
	}
	
	public void removeHandler(HttpRequest404Handler handler, int port) {
		if(request404Handlers != null) {
			List<HttpRequest404Handler> handlers = request404Handlers.get(port);
			if(handlers != null) {
				handlers.remove(handler);
				if(handlers.isEmpty()) {
					request404Handlers.remove(port);
					if(request404Handlers.isEmpty()) {
						request404Handlers = null;
					}
				}
			}
		}
	}
	
	public void removeHandler(HttpRequest500Handler handler, int port) {
		if(request500Handlers != null) {
			List<HttpRequest500Handler> handlers = request500Handlers.get(port);
			if(handlers != null) {
				handlers.remove(handler);
				if(handlers.isEmpty()) {
					request500Handlers.remove(port);
					if(request500Handlers.isEmpty()) {
						request500Handlers = null;
					}
				}
			}
		}
	}
	
	public void removeHandler(HttpRequestHandler handler) {
		if(selector.isOpen()) {
			synchronized(channels) {
				int port = handler.getPort();
				int count = removeHandler(handler, port);
				if(count > 0) {
					logger.info("decremented count of port " + port + " to " + count);
				} else {
					try {
						ServerSocketChannel ssc = channels.remove(port);
						if(ssc != null) {
							ssc.close();
							logger.info("server stopped on port " + port);
						}
					} catch(IOException e) {
						logger.warn("error stopping server on port " + port, e);
					}
				}
			}
		}
	}
	
	private int removeHandler(HttpRequestHandler handler, int port) {
		if(requestHandlers != null) {
			List<HttpRequestHandler> handlers = requestHandlers.get(port);
			if(handlers != null) {
				handlers.remove(handler);
				if(handlers.isEmpty()) {
					requestHandlers.remove(port);
					if(requestHandlers.isEmpty()) {
						requestHandlers = null;
					}
				}
				return handlers.size();
			}
		}
		return 0;
	}
	
	public void run() {
		while(selector.isOpen()) {
			try {
				selector.select();
				if(addingRequestHandler) {
					while(addingRequestHandler) {
						Thread.yield();
					}
					continue;
				}
				
				if(!writeQ.isEmpty()) {
					synchronized(writeQ) {
						for(Iterator<SelectionKey> iter = writeQ.keySet().iterator(); iter.hasNext(); ) {
							SelectionKey key = iter.next();
							if(key.isValid()) {
								key.interestOps(OP_WRITE);
							} else {
								iter.remove();
							}
						}
					}
				}
				
				if(selector.isOpen()) {
					for(Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); iter.hasNext(); ) {
						SelectionKey key = iter.next();
						iter.remove();
						
						if(key.isValid()) {
							if(key.isAcceptable()) {
								accept(key);
							} else if(key.isReadable()) {
								read(key);
							} else if(key.isWritable()) {
								write(key);
							}
						}
					}
				}
			} catch(IOException e) {
				logger.warn(e);
			}
		}
	}

	public void send(SelectionKey key, byte[] data) {
		doSend(key, ByteBuffer.wrap(data), false);
	}
	
	public void send(SelectionKey key, byte[] data, boolean close) {
		doSend(key, ByteBuffer.wrap(data), close);
	}
	
	private void doSend(SelectionKey key, Object value, boolean close) {
		synchronized(writeQ) {
			List<Object> q = writeQ.get(key);
			if(q == null) {
				q = new ArrayList<Object>();
				writeQ.put(key, q);
			}
			q.add(value);
			if(close && value != null) {
				q.add(null);
			}
		}
		
		selector.wakeup();
	}

	/**
	 * if response is null - close the socket
	 * @param key
	 * @param response
	 */
	public void send(SelectionKey key, HttpResponse response) {
		if(response == null) {
			doSend(key, null, false);
		} else {
			synchronized(writeQ) {
				List<Object> q = writeQ.get(key);
				if(q == null) {
					q = new ArrayList<Object>();
					writeQ.put(key, q);
				}
				q.add(response.getBuffer());
				if(response.hasDataChannel()) {
					q.add(response.getDataChannel());
				}
			}
			
			selector.wakeup();
		}
	}
	
	public void send(SelectionKey key, String data) {
		if(data == null) {
			doSend(key, null, false);
		} else {
			doSend(key, ByteBuffer.wrap(data.getBytes()), false);
		}
	}
	
	public void send(SelectionKey key, String data, boolean close) {
		if(data == null) {
			doSend(key, null, false);
		} else {
			doSend(key, ByteBuffer.wrap(data.getBytes()), close);
		}
	}
	
	// TODO currently set to force close connections, need to implement KeepAlive and Server#maxOpenSockets
	private void write(SelectionKey key) throws IOException {
		synchronized(writeQ) {
			try {
				List<Object> q = writeQ.get(key);
				if(q != null) {
					SocketChannel socket = (SocketChannel) key.channel();
					
					while(!q.isEmpty()) {
						Object o = q.get(0);
						if(o == null) {
							socket.close();
							key.cancel();
							q.clear();
						} else {
							if(o instanceof ByteBuffer) {
								try {
									ByteBuffer buffer = (ByteBuffer) o;
									socket.write(buffer);
									if(buffer.hasRemaining()) {
										break; // can't write any more now, exit the loop w/out removing the buffer
									}
									q.remove(0);
								} catch(Exception e) {
									// socket may have been reset by peer
									if(logger.isLoggingTrace()) logger.trace(e);
									socket.close();
									key.cancel();
									q.clear();
								}
							} else if(o instanceof ReadableByteChannel) {
								ReadableByteChannel in = (ReadableByteChannel) o;
								ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
								try {
									buffer.clear();
									while(in.read(buffer) != -1) {
										buffer.flip();
										socket.write(buffer);
										if(buffer.hasRemaining()) {
											q.add(0, buffer); // can't write any more now, add buffer to q and exit
											return;
										}
										buffer.compact();
									}
									try {
										in.close();
									} catch(IOException readE) {
										if(logger.isLoggingTrace()) logger.trace(readE);
									}
									buffer.flip();
									if(buffer.hasRemaining()) {
										q.set(0, buffer); // we've read all, but can't write, swap channel for buffer on q and exit
										return;
									} else {
										q.remove(0);
									}
								} catch(Exception e) {
									// socket may have been reset by peer
									if(logger.isLoggingTrace()) logger.trace(e);
									socket.close();
									key.cancel();
									q.clear();
									try {
										in.close();
									} catch(Exception e2) {
										// discard
									}
								}
							} else {
								logger.debug("else: \"" + String.valueOf(o) + "\"");
								socket.close();
								key.cancel();
								q.clear();
							}
						}
					}
					
					if(q.isEmpty()) {
						// TODO start of remove this
						socket.close();
						key.cancel();
						// TODO end of remove this
						writeQ.remove(key);
						if(key.isValid()) {
							key.interestOps(OP_READ);
						}
						key.cancel();
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
