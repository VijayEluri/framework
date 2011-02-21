package org.oobium.logging;

import java.util.HashMap;
import java.util.Map;


public abstract class LogProvider {

	private static final Map<String, Logger> loggers = new HashMap<String, Logger>();
	private static final Class<? extends Logger> loggerClass;
	static {
		try {
			loggerClass = Class.forName("org.oobium.logging.LoggerImpl").asSubclass(Logger.class);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieve the Default Logger.
	 * If the Logger does not currently exist, it is created.<br>
	 * Is equivalent to calling <code>getLogger(null)</code>.
	 * @param key the key used to identify the Logger, or null
	 * @return the Logger; never null
	 * @see #getLogger(Class)
	 * @see #getLogger(String)
	 */
	public static Logger getLogger() {
		return getLogger((String) null);
	}

	/**
	 * Retrieve the Logger with the name of the given Class as its key (can be null).<br>
	 * This is a convenience method which calls {@link #getLogger(String)} and passes in
	 * <code>clazz.getName()</code>.<br>
	 * <b>The passed in Class object is not retained, it is only used to get its name.</b><br>
	 * Null values for clazz returns the default Logger, as with {@link #getLogger()}.
	 * @param clazz the Class to use to create the key that identifies the Logger, or null
	 * @return the Logger; never null
	 * @see #getLogger()
	 * @see #getLogger(String)
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getLogger((clazz != null) ? clazz.getName() : null);
	}
	
	/**
	 * Retrieve the Logger with the given key (can be null).<br>
	 * If the Logger does not currently exist, it is created.
	 * Null values for key returns the default Logger, as with {@link #getLogger()}.
	 * @param key the key used to identify the Logger, or null
	 * @return the Logger; never null
	 * @see #getLogger()
	 * @see #getLogger(Class)
	 */
	public static Logger getLogger(String key) {
		Logger logger = loggers.get(key);
		if(logger == null) {
			synchronized(loggers) {
				logger = loggers.get(key);
				if(logger == null) {
					try {
						loggers.put(key, logger = loggerClass.newInstance());
					} catch(Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return logger;
	}
	
}
