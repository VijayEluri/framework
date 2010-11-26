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

import static org.oobium.logging.Logger.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class LogFormatter {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public static String format(Bundle bundle, int level, String message, Throwable exception) {
		StringBuilder sb = new StringBuilder();
		sb.append(sdf.format(new Date()));
		switch(decode(level)) {
		case ERROR:		sb.append(" (ERROR) "); break;
		case WARNING: 	sb.append(" (WARN)  "); break;
		case INFO: 		sb.append(" (INFO)  "); break;
		case DEBUG:		sb.append(" (DEBUG) "); break;
		case TRACE:		sb.append(" (TRACE) "); break;
		}
		if(message != null && message.startsWith("<bundle>")) {
			int end = message.indexOf("</bundle>");
			if(end != -1) {
				sb.append(message.substring(8, end)).append(": ");
				sb.append(message.substring(end + 9));
			} else if(bundle != null) {
				sb.append(bundle.getSymbolicName()).append(": ");
				sb.append(message);
			} else {
				sb.append(message);
			}
		} else {
			if(bundle != null) {
				sb.append(bundle.getSymbolicName()).append(": ");
			}
			if(message != null) {
				sb.append(message);
			}
		}
		sb.append('\n');
		if(exception != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				exception.printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch(Exception ex) {
				// discard
			}
		}
		return sb.toString();
	}
	
	static String format(Bundle bundle, String message) {
		if(bundle == null) {
			return message;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<bundle>");
			sb.append(bundle.getSymbolicName());
			sb.append("</bundle>");
			if(message != null) {
				sb.append(message);
			}
			return sb.toString();
		}
	}

	public static String format(ServiceReference reference, int level, String message, Throwable exception) {
		Bundle bundle = (reference != null) ? reference.getBundle() : null;
		return format(bundle, level, message, exception);
	}

}
