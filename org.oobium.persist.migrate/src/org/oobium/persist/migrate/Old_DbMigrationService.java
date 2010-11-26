package org.oobium.persist.migrate;
///*******************************************************************************
// * Copyright (c) 2010 Oobium, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
// ******************************************************************************/
//package org.oobium.persist.db.migrate;
//
//import static org.oobium.app.AppConfig.loadConfiguration;
//import static org.oobium.utils.StringUtils.blank;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//import org.oobium.app.AppConfig;
//import org.oobium.app.AppService;
//import org.oobium.app.AppConfig.Mode;
//import org.oobium.logging.Logger;
//import org.oobium.persist.PersistClient;
//import org.oobium.persist.PersistService;
//import org.oobium.persist.PersistServices;
//import org.oobium.persist.db.DbPersistService;
//import org.oobium.utils.StringUtils;
//import org.oobium.utils.json.JsonUtils;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleActivator;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceReference;
//import org.osgi.framework.Version;
//import org.osgi.service.packageadmin.PackageAdmin;
//
//public abstract class DbMigrationService implements BundleActivator, PersistClient {
//
//	public enum Direction { 
//		FORWARD, REVERSE;
//		public static Direction parse(String dir) {
//			try {
//				return Direction.valueOf(dir.toUpperCase());
//			} catch(Exception e) {
//				return FORWARD;
//			}
//		}
//	}
//	
//	
//	private final Logger logger;
//	private BundleContext context;
//
//	private String appName;
//	private String appVersion;
//	private String appSchema;
//	
//	private Direction dir;
//	private AppConfig appConfig;
//	private PersistServices persistServices;
//	
//
//	public DbMigrationService() {
//		logger = Logger.getLogger(getClass());
//	}
//
//	/**
//	 * Create the database.
//	 * Called when migrating forward and the database has not
//	 * yet been created.
//	 */
//	protected abstract void create() throws SQLException;
//	
//	/**
//	 * Create the initial database.  Assumes that it does not yet exist.
//	 * @return true if successful, false otherwise
//	 */
//	public final boolean createDatabase() {
//		logger.info("Creating database...");
//		try {
//			List<String> statements = loadCreateStatements();
//			for(String statement : statements) {
//				logger.debug(statement);
//				getPersistService().executeUpdate(statement);
//			}
//			logger.info("Database created.");
//			return true;
//		} catch(IOException e) {
//			logger.error(e);
//		} catch(SQLException e) {
//			logger.error(e);
//		}
//		return false;
//	}
//	
//	/**
//	 * Drops all tables in the database, if it exists
//	 */
//	public final void dropDatabase() {
//		logger.info("Dropping database...");
//		
//		String sql = "select t.tablename, c.constraintname" + " from sys.sysconstraints c, sys.systables t"
//				+ " where c.type = 'F' and t.tableid = c.tableid";
//
//		List<Map<String, Object>> constraints = null;
//		try {
//			constraints = getPersistService().executeQuery(sql);
//		} catch(SQLException e) {
//			logger.info("database has not yet been created");
//			return;
//		}
//		
//		for(Map<String, Object> map : constraints) {
//			sql = "alter table " + map.get("tablename") + " drop constraint " + map.get("constraintname");
//			logger.debug(sql);
//			try {
//				getPersistService().executeUpdate(sql);
//			} catch(Exception e) {
//				logger.error("could not alter table: " + sql, e);
//			}
//		}
//
//		try {
//			Connection connection = getPersistService().getConnection();
//			ResultSet rs = null;
//			try {
//				rs = connection.getMetaData().getTables(null, appSchema, "%", new String[] { "TABLE" });
//				while(rs.next()) {
//					sql = "drop table " + appSchema + "." + rs.getString(3);
//					logger.debug(sql);
//					Statement stmt = connection.createStatement();
//					try {
//						stmt.executeUpdate(sql);
//					} finally{
//						stmt.close();
//					}
//				}
//			} finally {
//				if(rs != null) {
//					rs.close();
//				}
//				// connection.close(); no need - connection will be closed when the session is closed
//			}
//			logger.info("Database dropped.");
//		} catch(SQLException e) {
//			// well, something went wrong...
//			logger.error("ERROR dropping database", e);
//		}
//	}
//
//	void forward() throws SQLException {
//		if(hasDatabase()) {
//			if(isCurrentSchema()) {
//				if(isInitialSchema()) {
//					dropDatabase();
//					create();
//				} else {
//					revert();
//					update();
//				}
//			} else {
//				update();
//			}
//		} else {
//			create();
//		}
//	}
//	
//	@Override
//	public BundleContext getContext() {
//		return context;
//	}
//	
//	@Override
//	public String getName() {
//		Bundle bundle = context.getBundle();
//		String n = bundle.getSymbolicName();
//		n = n.substring(0, n.length() - 10);
//		Version v = bundle.getVersion();
//		StringBuilder sb = new StringBuilder();
//		sb.append(n).append('_');
//		sb.append(v.getMajor()).append('.');
//		sb.append(v.getMinor()).append('.');
//		sb.append(v.getMicro());
//		return sb.toString();
//	}
//
//	private DbPersistService getPersistService() {
//		PersistService service = null;
//		if(persistServices != null) {
//			service = persistServices.getPrimary();
//		}
//		if(service instanceof DbPersistService) {
//			return (DbPersistService) service;
//		}
//		throw new IllegalStateException("Migration cannot run without a DbPersistService: " + appConfig.get(AppConfig.PERSIST));
//	}
//	
//	public PersistServices getPersistServices() {
//		return persistServices;
//	}
//
//	public final boolean hasDatabase() {
//		try {
//			Object o = getPersistService().executeQueryValue("select count(*) from system_attrs");
//			if(o instanceof Number) {
//				return ((Number) o).intValue() > 0;
//			}
//		} catch(SQLException e) {
//			// discard
//		}
//		return false;
//	}
//
//	private void initializePersistServices(AppConfig config) throws InvalidSyntaxException {
//		Object persist = config.get(AppConfig.PERSIST);
//		persistServices = new PersistServices(getContext(), persist);
//		List<String> services = persistServices.getServiceNames();
//		if(services.isEmpty()) {
//			logger.debug("no presist services configured - skipping registration");
//		} else {
//			if(logger.isLoggingDebug()) {
//				if(services.size() == 1) {
//					logger.debug("registering for persist service: " + services.get(0));
//				} else {
//					logger.debug("registering for persist services: " + StringUtils.asString(services));
//				}
//			}
//			Properties properties = new Properties();
//			properties.setProperty(PersistService.CLIENT, getName());
//			properties.setProperty(PersistService.SERVICE, JsonUtils.toJson(services));
//			getContext().registerService(PersistClient.class.getName(), this, properties);
//		}
//	}
//
//	private boolean isCurrentSchema() throws SQLException {
//		String attrName = appName + ".schema.current";
//		Object value = getPersistService().executeQueryValue("select attr_value from system_attrs where attr_name=?", attrName);
//		return appVersion.equals(value);
//	}
//
//	public boolean isForward() {
//		return dir == Direction.FORWARD;
//	}
//	
//	private boolean isInitialSchema() throws SQLException {
//		String attrName = appName + ".schema.initial";
//		Object value = getPersistService().executeQueryValue("select attr_value from system_attrs where attr_name=?", attrName);
//		return appVersion.equals(value);
//	}
//	
//	private List<String> loadCreateStatements() throws IOException {
//		return loadStatements("/create.sql");
//	}
//	
//	private List<String> loadStatements(String name) throws IOException {
//		InputStream is = getClass().getResourceAsStream(name);
//		if(is == null) {
//			return new ArrayList<String>(0);
//		}
//		
//		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//		try {
//			String line;
//			List<String> statements = new ArrayList<String>();
//			while((line = reader.readLine()) != null) {
//				statements.add(line);
//			}
//			return statements;
//		} finally {
//			try {
//				reader.close();
//			} catch(IOException e) {
//				logger.warn("error closing reader for resource: " + name);
//			}
//		}
//	}
//
//
//	public boolean refreshDatabase() {
//		dropDatabase();
//		return createDatabase();
//	}
//
//	void reverse() throws SQLException {
//		if(hasDatabase()) {
//			if(isInitialSchema()) {
//				dropDatabase();
//			} else {
//				revert();
//			}
//		}
//	}
//	
//	/**
//	 * Revert the database.
//	 * Called when migrating backward to a state where the database
//	 * still exists (the opposite of {@link #update()}).
//	 */
//	protected abstract void revert() throws SQLException;
//	
//	@Override
//	public void start(BundleContext context) throws Exception {
//		this.context = context;
//		
//		logger.setBundle(context.getBundle());
//		logger.info("MigrationService starting...");
//
//		Bundle appBundle;
//		
//		ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
//		if(ref == null) {
//			throw new IllegalStateException("Package Admin service must be present to run a migration");
//		} else {
//			String symbolicName = context.getBundle().getSymbolicName();
//			symbolicName = symbolicName.substring(0, symbolicName.lastIndexOf('.'));
//			
//			PackageAdmin admin = (PackageAdmin) context.getService(ref);
//			Bundle[] bundles = admin.getBundles(symbolicName, null);
//			if(bundles.length == 0) {
//				throw new IllegalStateException("bundle " + symbolicName + " is not present - cannot run migration");
//			} else if(bundles.length == 1) {
//				appBundle = bundles[0];
//			} else {
//				throw new IllegalStateException("no more than 2 bundles of " + symbolicName + " may be present to run a migration");
//			}
//		}
//
//		dir = Direction.parse(context.getProperty("direction"));
//		logger.info("configuring " + dir.name() + " migration in " + Mode.getSystemMode() + " mode");
//		
//		appSchema = context.getProperty("schema");
//		if(blank(appSchema)) {
//			appSchema = "ROOT";
//		}
//		logger.info("schema set to " + appSchema);
//
//		String name = (String) appBundle.getHeaders().get("Bundle-Activator");
//		Class<?> clazz = appBundle.loadClass(name);
//		appConfig = loadConfiguration(clazz.asSubclass(AppService.class));
//		initializePersistServices(appConfig);
//		
//		appName = appBundle.getSymbolicName();
//
//		Version version = appBundle.getVersion();
//		version = new Version(version.getMajor(), version.getMinor(), version.getMicro());
//		appVersion = version.toString();
//
//		DbMigrator migrator = new DbMigrator(this);
//		migrator.start();
//		logger.info("migrator thread started");
//
//		logger.info("MigrationService service started");
//	}
//	
//	@Override
//	public void stop(BundleContext context) {
//		if(persistServices != null) {
//			persistServices.close();
//			persistServices = null;
//		}
//		
//		this.context = null;
//
//		logger.info("migrator stopped");
//		logger.setBundle(null);
//	}
//	
//	/**
//	 * Update the database.
//	 * Called when migrating forward and the database already exists
//	 * (but is probably out dated).
//	 */
//	protected abstract void update() throws SQLException;
//
//	
//	
//}
