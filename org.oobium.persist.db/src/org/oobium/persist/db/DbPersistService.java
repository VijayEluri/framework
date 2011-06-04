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
package org.oobium.persist.db;

import static org.oobium.persist.db.internal.DbCache.expireCache;
import static org.oobium.utils.literal.Properties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.oobium.logging.LogProvider;
import org.oobium.logging.Logger;
import org.oobium.persist.Model;
import org.oobium.persist.ModelAdapter;
import org.oobium.persist.PersistClient;
import org.oobium.persist.PersistService;
import org.oobium.persist.db.internal.DbPersistor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public abstract class DbPersistService implements BundleActivator, PersistService {

	private static final ThreadLocal<String> threadClient = new ThreadLocal<String>();
	private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<Connection>();
	private static final ThreadLocal<Boolean> threadAutoCommit = new ThreadLocal<Boolean>();
	private static final int CREATE = 0;

	private static final int DESTROY = 1;
	private static final int RETRIEVE = 2;
	private static final int UPDATE = 3;
	
	private static Map<String, Object> parseUrl(String url) {
		Map<String, Object> properties = new HashMap<String, Object>();
		int ix = url.indexOf('@');
		if(ix == -1) {
			properties.put("username", "root");
			properties.put("password", "");
		} else {
			String credentials = url.substring(0, ix);
			url = url.substring(ix+1);
			ix = credentials.indexOf(':');
			if(ix == -1) {
				properties.put("username", credentials);
				properties.put("password", "");
			} else {
				properties.put("username", credentials.substring(0, ix));
				properties.put("password", credentials.substring(ix+1));
			}
		}

		ix = url.indexOf('/');
		if(ix == -1) {
			properties.put("host", null); // use default
			properties.put("port", null); // use default
			properties.put("database", url);
		} else if(ix == 0) {
			properties.put("host", null); // use default
			properties.put("port", null); // use default
			properties.put("database", url.substring(1));
		} else {
			String s = url.substring(0, ix);
			properties.put("database", url.substring(ix+1));
			ix = s.indexOf(':');
			if(ix == -1) {
				properties.put("host", s);
				properties.put("port", null); // use default
			} else {
				properties.put("host", s.substring(0, ix));
				properties.put("port", Integer.parseInt(s.substring(ix+1)));
			}
		}
		return properties;
	}

	protected final Logger logger;
	private BundleContext context;
	private DbPersistor persistor;
	private Map<String, Database> databases;

	private ServiceTracker appTracker;

	private final ReadWriteLock lock;


	public DbPersistService() {
		logger = LogProvider.getLogger(DbPersistService.class);
		persistor = new DbPersistor();
		databases = new HashMap<String, Database>();
		lock = new ReentrantReadWriteLock();
	}

	/**
	 * <p>Instantiates a new DbPersistService, opens a session and adds the given database.
	 * The service is ready to use as-is, but closeSession() must be called when it is
	 * no longer used to free up database resources.</p>
	 * <p>This form of DbPersistService is not intended to be used in a multi-threaded 
	 * environment because it uses a single connection</p>
	 * <p>Specifying an in-memory database is a good performance increase for tests</p>
	 * @param client
	 * @param timeout
	 */
	public DbPersistService(String client, Map<String, Object> properties) {
		this();
		addDatabase(client, properties);
		openSession(client);
	}
	
	public DbPersistService(String client, String url) {
		this(client, parseUrl(url));
	}
	
	private void addDatabase(String client, Map<String, Object> properties) {
		lock.readLock().lock();
		try {
			if(databases.containsKey(client)) {
				return;
			}
		} finally {
			lock.readLock().unlock();
		}
		
		lock.writeLock().lock();
		try {
			Database db = createDatabase(client, properties);
			databases.put(client, db);
			if(logger.isLoggingInfo()) {
				logger.info("added Database for " + client + " (" + db.getDatabaseIdentifier() + ")");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	public boolean getAutoCommit() {
		Boolean ac = threadAutoCommit.get();
		return (ac != null) && ac.booleanValue();
	}
	
	@Override
	public void closeSession() {
		Connection connection = threadConnection.get();
		if(connection != null) {
			boolean closed;
			try {
				closed = connection.isClosed();
			} catch(SQLException e) {
				closed = true;
			}
			if(!closed) {
				try {
					if(!connection.getAutoCommit()) {
						connection.rollback();
					}
				} catch(Exception e) {
					// discard
				}
				try {
					connection.close();
				} catch(Exception e) {
					logger.warn("could not close database connection", e);
				}
			}
		}
		threadConnection.set(null);
		threadClient.set(null);
		expireCache();
	}
	
	public void commit() throws SQLException {
		Connection connection = getConnection(false);
		if(connection != null) {
			connection.commit();
		}
	}
	
	@Override
	public int count(Class<? extends Model> clazz, String where, Object... values) throws SQLException {
		Connection connection = getConnection();
		return persistor.count(connection, clazz, where, values);
	}
	
	@Override
	public void create(Model...models) throws SQLException {
		handleCrud(CREATE, models);
	}

	public void createDatabase(String client) throws SQLException {
		Database db = getDatabase(client);
		db.createDatabase();
	}
	
	protected abstract Database createDatabase(String client, Map<String, Object> properties);

	@Override
	public void destroy(Model...models) throws SQLException {
		handleCrud(DESTROY, models);
	}

	public void dropDatabase(String client) throws SQLException {
		Database db = getDatabase(client);
		db.dispose();
		db.dropDatabase();
	}

	@Override
	public List<Map<String, Object>> executeQuery(String sql, Object...values) throws SQLException {
		Connection connection = getConnection();
		return persistor.executeQuery(connection, sql, values);
	}
	
	@Override
	public List<List<Object>> executeQueryLists(String sql, Object...values) throws SQLException {
		Connection connection = getConnection();
		return persistor.executeQueryLists(connection, sql, values);
	}

	@Override
	public Object executeQueryValue(String sql, Object...values) throws SQLException {
		Connection connection = getConnection();
		return persistor.executeQueryValue(connection, sql, values);
	}

	@Override
	public int executeUpdate(String sql, Object... values) throws SQLException {
		Connection connection = getConnection();
		return persistor.executeUpdate(connection, sql, values);
	}
	
	@Override
	public <T extends Model> T find(Class<T> clazz, int id) throws SQLException {
		Connection connection = getConnection();
		return persistor.find(connection, clazz, id);
	}

	@Override
	public <T extends Model> T find(Class<T> clazz, String where, Object...values) throws SQLException {
		Connection connection = getConnection();
		return persistor.find(connection, clazz, where, values);
	}

	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz) throws SQLException {
		return findAll(clazz, null);
	}
	
	@Override
	public <T extends Model> List<T> findAll(Class<T> clazz, String where, Object...values) throws SQLException {
		Connection connection = getConnection();
		return persistor.findAll(connection, clazz, where, values);
	}
	
	public Connection getConnection() throws SQLException {
		return getConnection(true);
	}
	
	private Connection getConnection(boolean create) throws SQLException {
		lock.readLock().lock();
		try {
			Connection connection = threadConnection.get();
			if(connection != null && connection.isClosed()) {
				connection = null;
			}
			if(connection == null && create) {
				String client = threadClient.get();
				if(client == null) {
					throw new SQLException(client + " is not a registered PersistClient");
				}
				Database db = getDatabase(client);
				connection = db.getConnection();
				connection.setAutoCommit(getAutoCommit());
				threadConnection.set(connection);
			}
			return connection;
		} finally {
			lock.readLock().unlock();
		}
	}
	
	private Database getDatabase(String client) throws SQLException {
		if(databases != null) {
			Database db = databases.get(client);
			if(db == null) {
				throw new SQLException("database for " + client + " has not been setup");
			}
			return db;
		} else {
			throw new SQLException("no connection pool has been setup");
		}
	}
	
	public String getMigrationServiceName() {
		Object o = context.getBundle().getHeaders().get("Oobium-MigrationService");
		if(o instanceof String) {
			return (String) o;
		}
		return null;
	}
	
	private void handleCrud(int task, Model[] models) throws SQLException {
		if(models.length == 0) {
			return;
		}
		
		Connection connection = getConnection();
		if(task == RETRIEVE) {
			persistor.retrieve(connection, models);
		} else {
			try {
				connection.setAutoCommit(false);
				switch(task) {
				case CREATE:
					persistor.create(connection, models);
					break;
				case DESTROY:
					persistor.destroy(connection, models);
					break;
				case UPDATE:
					persistor.update(connection, models);
					break;
				}
				if(getAutoCommit()) {
					connection.commit();
				}
			} catch(Exception e) {
				connection.rollback();
				threadAutoCommit.set(null);
				logger.warn("transaction was rolledback", e);
				if(e instanceof SQLException) {
					throw (SQLException) e;
				} else {
					throw new SQLException("transaction was rolledback", e);
				}
			} finally {
				try {
					if(getAutoCommit()) {
						connection.setAutoCommit(true);
					}
				} catch(Exception e) {
					logger.warn("failed to reset connection autocommit", e);
				}
			}
		}
	}
	
	@Override
	public boolean isSessionOpen() {
		return threadClient.get() != null;
	}

	@Override
	public void openSession(String name) {
		threadClient.set(name);
		threadAutoCommit.set(true);
		expireCache();
	}
	
	private void removeDatabase(String client) {
		lock.writeLock().lock();
		try {
			Database cp = databases.remove(client);
			if(cp != null) {
				cp.dispose();
				logger.log(Logger.INFO, "removed Database for " + client);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	@Override
	public void retrieve(Model...models) throws SQLException {
		handleCrud(RETRIEVE, models);
	}

	/**
	 * @param relation may be a hasMany or hasOne (if 1:1 and opposite holds the key)
	 */
	@Override
	public void retrieve(Model model, String relation) throws SQLException {
		// TODO hack: re-implement directly in DbPersistor
		Connection connection = getConnection();
		Model tmp = persistor.find(connection, model.getClass(), "where id=? include:?", model.getId(), relation);
		if(tmp != null || ModelAdapter.getAdapter(model).hasOne(relation)) {
			model.put(relation, tmp.get(relation));
		}
	}

	public void rollback() throws SQLException {
		Connection connection = getConnection(false);
		if(connection != null) {
			connection.rollback();
		}
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		boolean changed = false;
		if(getAutoCommit()) {
			if(!autoCommit) {
				threadAutoCommit.set(null);
				changed = true;
			}
		} else {
			if(autoCommit) {
				threadAutoCommit.set(true);
				changed = true;
			}
		}
		if(changed) {
			Connection connection = getConnection(false);
			if(connection != null) {
				connection.setAutoCommit(autoCommit);
			}
		}
	}

	Bundle getBundle() {
		return context.getBundle();
	}
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		final String name = context.getBundle().getSymbolicName();

		logger.setTag(name);
		logger.info("PersistService starting");
		
		appTracker = new ServiceTracker(context, PersistClient.class.getName(), new ServiceTrackerCustomizer() {
			@Override
			public Object addingService(ServiceReference reference) {
				String service = (String) reference.getProperty(PersistService.SERVICE);
				if(name.equals(service)) {
					String clientName = (String) reference.getProperty(PersistService.CLIENT);
					if(clientName != null) {
						Map<String, Object> properties = new HashMap<String, Object>();
						for(String key : reference.getPropertyKeys()) {
							properties.put(key, reference.getProperty(key));
						}
						addDatabase(clientName, properties);
						return clientName;
					}
				}
				return null;
			}
			@Override
			public void modifiedService(ServiceReference reference, Object service) {
				// nothing to do... ?
			}
			@Override
			public void removedService(ServiceReference reference, Object service) {
				if(service != null) {
					removeDatabase((String) service);
					DbPersistService.this.context.ungetService(reference);
				}
			}
		});
		appTracker.open();

		context.registerService(PersistService.class.getName(), this, Properties(PersistService.SERVICE, name));

		logger.info("PersistService started (" + name + ")");
	}
	
	public void stop(BundleContext context) throws Exception {
		appTracker.close();
		appTracker = null;
		this.context = null;
		logger.info("PersistService stopped");
		logger.setTag(null);
	}

	@Override
	public void update(Model...models) throws SQLException {
		handleCrud(UPDATE, models);
	}
	
}
