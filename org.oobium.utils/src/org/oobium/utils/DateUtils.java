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
package org.oobium.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	private static final SimpleDateFormat httpDate_1123		= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat httpDate_850		= new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat httpDate_ASCTIME	= new SimpleDateFormat("EEE, MM  dd HH:mm:ss yyyy", Locale.US);
	static {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		httpDate_1123.setTimeZone(tz);
		httpDate_850.setTimeZone(tz);
		httpDate_ASCTIME.setTimeZone(tz);
	}

	public static String httpDate(Date date) {
		return httpDate_1123.format(date);
	}
	
	public static String httpDate(long dateInMillis) {
		return httpDate_1123.format(new Date(dateInMillis));
	}
	
	public static Date httpDate(String date) throws ParseException {
		try {
			try {
				return httpDate_1123.parse(date);
			} catch(ParseException e1) {
				try {
					return httpDate_850.parse(date);
				} catch(ParseException e2) {
					try {
						return httpDate_ASCTIME.parse(date);
					} catch(ParseException e3) {
						throw e1;
					}
				}
			}
		} catch(Exception e) {
			throw new ParseException("could not parse http date: " + date, -1);
		}
	}
	
	public static boolean sameDay(Date date1, Date date2)  {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(date1);
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
	}
	
	public static boolean sameHour(Date date1, Date date2)  {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(date1);
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
				&& c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
				&& c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY);
	}
	
	public static boolean sameMonth(Date date1, Date date2)  {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(date1);
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
	}
	
	public static boolean sameYear(Date date1, Date date2)  {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(date1);
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
	}
	
	private DateUtils() {
		// static methods only
	}
	
}
