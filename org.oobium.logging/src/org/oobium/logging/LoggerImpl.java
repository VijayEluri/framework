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
package org.oobium.logging;

import static org.oobium.logging.Activator.getLogService;
import static org.oobium.logging.Activator.isBundle;
import static org.oobium.logging.LogFormatter.format;

import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

public class LoggerImpl implements Logger {

	protected static int systemConsoleLevel = getSystemConsoleLevel();
	protected static int systemFileLevel = getSystemFileLevel();
	protected static int systemEmailLevel = getSystemEmailLevel();

	/**
	 *  32bits split into 4 bytes: CONSOLE|FILE|EMAIL|LEVEL
	 *  @return the value of LEVEL
	 */
	public static int decode(int level) {
		return (level & 0x000000ff);
	}
	
	public static int getSystemConsoleLevel() {
		return parseLevel(System.getProperty(SYS_PROP_CONSOLE), NEVER);
	}
	
	public static int getSystemEmailLevel() {
		return parseLevel(System.getProperty(SYS_PROP_EMAIL), NEVER);
	}
	
	public static int getSystemFileLevel() {
		return parseLevel(System.getProperty(SYS_PROP_FILE), INFO);
	}
	
	private static boolean isLogging(int logLevel, int setLevel) {
		return setLevel != NEVER && logLevel <= setLevel;
	}
	
	public static boolean isLoggingToConsole(Bundle bundle, int level) {
		if(isBundle(bundle)) {
			return isLogging((level & 0x000000ff), ((level & 0xff000000) >> 24));
		} else {
			return isLogging(level, systemConsoleLevel);
		}
	}
	
	public static boolean isLoggingToEmail(Bundle bundle, int level) {
		if(isBundle(bundle)) {
			return isLogging((level & 0x000000ff), ((level & 0x0000ff00) >> 8));
		} else {
			return isLogging(level, systemEmailLevel);
		}
	}
	
	public static boolean isLoggingToFile(Bundle bundle, int level) {
		if(isBundle(bundle)) {
			return isLogging((level & 0x000000ff), ((level & 0x00ff0000) >> 16));
		} else {
			return isLogging(level, systemFileLevel);
		}
	}
	
	private static int parseLevel(String level, int defaultLevel) {
		if(level != null) {
			try {
				return Integer.parseInt(level);
			} catch(Exception e) {
				if("ERROR".equalsIgnoreCase(level)) {
					return ERROR;
				} else if("WARN".equalsIgnoreCase(level) || "WARNING".equalsIgnoreCase(level)) {
					return WARNING;
				} else if("INFO".equalsIgnoreCase(level) || "INFORMATION".equalsIgnoreCase(level)) {
					return INFO;
				} else if("DEBUG".equalsIgnoreCase(level)) {
					return DEBUG;
				} else if("TRACE".equalsIgnoreCase(level)) {
					return TRACE;
				} else if("NO".equalsIgnoreCase(level) || "NONE".equalsIgnoreCase(level)) {
					return NONE;
				} else if("NEVER".equalsIgnoreCase(level)) {
					return NEVER;
				} else {
					// always return a valid value, otherwise it's a major pain in the ass to debug. will confuse users - guaranteed!
					System.out.println("Unknown logging level: " + level + ". Using default.");
					return defaultLevel;
				}
			}
		}
		return defaultLevel;
	}
	
	public static String replace(String string, Object[] values) {
		if(values == null || values.length == 0) {
			return string;
		}
		int len = string.length();
		String[] sa = new String[values.length];
		for(int i = 0; i < sa.length; i++) {
			sa[i] = String.valueOf(values[i]);
			len += sa[i].length();
		}
		StringBuilder sb = new StringBuilder(len);
		int i = 0;
		int pos = 0;
		int ix = string.indexOf("{}");
		while(ix != -1) {
			if(ix > 0) {
				if(string.charAt(ix-1) == '\\') {
					if(ix > 1 && string.charAt(ix-2) == '\\') {
						// escape is escaped :)
						sb.append(string.substring(pos, ix-1)); // skip one of the escape characters
						sb.append(sa[i++]); // print the anchor
					} else {
						// anchor is escaped
						sb.append(string.substring(pos, ix-1)); // skip the escape character
						sb.append("{}"); // print the escaped braces
					}
				} else {
					sb.append(string.substring(pos, ix));
					sb.append(sa[i++]);
				}
			} else {
				sb.append(sa[i++]);
			}
			pos = ix + 2;
			ix = string.indexOf("{}", pos);
		}
		if(pos < string.length()) {
			sb.append(string.substring(pos));
		}
		return sb.toString();
	}


	private String tag;
	private int consoleLevel;
	private int fileLevel;
	private int emailLevel;
	
	LoggerImpl() {
		consoleLevel = (systemConsoleLevel == NEVER) ? NEVER : systemConsoleLevel;
		fileLevel = (systemFileLevel == NEVER) ? NEVER : systemFileLevel;
		emailLevel = (systemEmailLevel == NEVER) ? NEVER : systemEmailLevel;
	}
	
	@Override
	public void debug(String message) {
		log(DEBUG, message, null, new Object[0]);
	}

	@Override
	public void debug(String message, Object... values) {
		log(DEBUG, message, null, values);
	}
	
	@Override
	public void debug(String message, Throwable exception) {
		log(DEBUG, message, exception, new Object[0]) ;
	}
	
	@Override
	public void debug(String message, Throwable exception, Object... values) {
		log(DEBUG, message, exception, values);
	}

	@Override
	public void debug(Throwable exception) {
		log(DEBUG, exception.getLocalizedMessage(), exception, new Object[0]) ;
	}
	
	/**
	 * 32bits split into 4 bytes: CONSOLE|FILE|EMAIL|LEVEL<br>
	 * (Could be split into nibbles to add 4 custom loggers...)
	 * @param level the level of the logging message
	 * @return the message level encoded with the system levels
	 * @see LoggerImpl#decode(int)
	 * @see #isLoggingToConsole(int)
	 * @see #isLoggingToEmail(int)
	 * @see #isLoggingToFile(int)
	 */
	private int encode(int level) {
		return 	((consoleLevel	<< 24) & 0xff000000) |
				((fileLevel		<< 16) & 0x00ff0000) |
				((emailLevel	<<  8) & 0x0000ff00) |
				(level				   & 0x000000ff);
	}
	
	@Override
	public void error(String message) {
		log(ERROR, message, null, new Object[0]);
	}
	
	@Override
	public void error(String message, Object... values) {
		log(ERROR, message, null, values);
	}
	
	@Override
	public void error(String message, Throwable exception) {
		log(ERROR, message, exception, new Object[0]) ;
	}
	
	@Override
	public void error(String message, Throwable exception, Object... values) {
		log(ERROR, message, exception, values);
	}
	
	@Override
	public void error(Throwable exception) {
		log(ERROR, exception.getLocalizedMessage(), exception, new Object[0]) ;
	}
	
	private boolean hasLogTracker() {
		try {
			return Activator.hasLogTracker();
		} catch(NoClassDefFoundError e) {
			return false; // OSGi Logging is not enabled (bundles not installed)
		}
	}
	
	@Override
	public void info(String message) {
		log(INFO, message, null, new Object[0]);
	}
	
	@Override
	public void info(String message, Object... values) {
		log(INFO, message, null, values);
	}
	
	@Override
	public void info(String message, Throwable exception) {
		log(INFO, message, exception, new Object[0]) ;
	}
	
	@Override
	public void info(String message, Throwable exception, Object... values) {
		log(INFO, message, exception, values);
	}
	
	@Override
	public void info(Throwable exception) {
		log(INFO, exception.getLocalizedMessage(), exception, new Object[0]) ;
	}
	
	@Override
	public boolean isLogging(int level) {
		return isLogging(level, consoleLevel) || isLogging(level, fileLevel) || isLogging(level, emailLevel);
	}
	
	@Override
	public boolean isLoggingDebug() {
		return isLogging(DEBUG, consoleLevel) || isLogging(DEBUG, fileLevel) || isLogging(DEBUG, emailLevel);
	}
	
	@Override
	public boolean isLoggingError() {
		return isLogging(ERROR, consoleLevel) || isLogging(ERROR, fileLevel) || isLogging(ERROR, emailLevel);
	}

	@Override
	public boolean isLoggingInfo() {
		return isLogging(INFO, consoleLevel) || isLogging(INFO, fileLevel) || isLogging(INFO, emailLevel);
	}
	
	@Override
	public boolean isLoggingToConsole(int level) {
		return isLogging(level, consoleLevel);
	}
	
	@Override
	public boolean isLoggingToEmail(int level) {
		return isLogging(level, emailLevel);
	}
	
	@Override
	public boolean isLoggingToFile(int level) {
		return isLogging(level, fileLevel);
	}
	
	@Override
	public boolean isLoggingTrace() {
		return isLogging(TRACE, consoleLevel) || isLogging(TRACE, fileLevel) || isLogging(TRACE, emailLevel);
	}
	
	@Override
	public boolean isLoggingWarning() {
		return isLogging(WARNING, consoleLevel) || isLogging(WARNING, fileLevel) || isLogging(WARNING, emailLevel);
	}
	
	@Override
	public void log(int level, String message) {
		log(level, message, null, new Object[0]);
	}
	
	@Override
	public void log(int level, String message, Object...values) {
		log(level, message, null, values);
	}
	
	@Override
	public synchronized void log(int level, String message, Throwable exception) {
		log(level, message, exception, new Object[0]);
	}
	
	@Override
	public synchronized void log(int level, String message, Throwable exception, Object...values) {
		if(isLogging(level)) {
			
			if(hasLogTracker()) {
				LogService service = getLogService();
				if(service != null) {
					message = replace(message, values);
					service.log(encode(level), format(tag, message), exception);
					return;
				}
			}
			
			// not logging else where, make sure to log to console if correct level
			if(isLoggingToConsole(level)) {
				message = replace(message, values);
				if(level <= WARNING) {
					System.err.print(format(tag, level, message, exception));
				} else {
					System.out.print(format(tag, level, message, exception));
				}
			}
		}
	}
	
	@Override
	public void log(int level, Throwable exception) {
		log(level, null, exception) ;
	}
	
	@Override
	public void setConsoleLevel(int level) {
		consoleLevel = level;
	}
	
	@Override
	public void setEmailLevel(int level) {
		emailLevel = level;
	}

	@Override
	public void setFileLevel(int level) {
		fileLevel = level;
	}
	
	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public void trace(String message) {
		log(TRACE, message, null, new Object[0]);
	}

	@Override
	public void trace(String message, Object... values) {
		log(TRACE, message, null, values);
	}

	@Override
	public void trace(String message, Throwable exception) {
		log(TRACE, message, exception, new Object[0]) ;
	}

	@Override
	public void trace(String message, Throwable exception, Object... values) {
		log(TRACE, message, exception, values);
	}

	@Override
	public void trace(Throwable exception) {
		log(TRACE, exception.getLocalizedMessage(), exception, new Object[0]) ;
	}

	@Override
	public void warn(String message) {
		log(WARNING, message, null, new Object[0]);
	}

	@Override
	public void warn(String message, Object... values) {
		log(WARNING, message, null, values);
	}

	@Override
	public void warn(String message, Throwable exception) {
		log(WARNING, message, exception, new Object[0]) ;
	}

	@Override
	public void warn(String message, Throwable exception, Object... values) {
		log(WARNING, message, exception, values);
	}

	@Override
	public void warn(Throwable exception) {
		log(WARNING, exception.getLocalizedMessage(), exception, new Object[0]) ;
	}
	
}
