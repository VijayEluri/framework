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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	private static final SimpleDateFormat basicDate			= new SimpleDateFormat("MM/dd/yyyy");
	private static final SimpleDateFormat httpDate_1123		= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat httpDate_850		= new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z", Locale.US);
	private static final SimpleDateFormat httpDate_ASCTIME	= new SimpleDateFormat("EEE, MM  dd HH:mm:ss yyyy", Locale.US);
	static {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		httpDate_1123.setTimeZone(tz);
		httpDate_850.setTimeZone(tz);
		httpDate_ASCTIME.setTimeZone(tz);
	}

	private static Date add(Date date, int field, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(field, amount);
		return cal.getTime();
	}
	
	public static Date addDays(Date date, int amount) {
		return add(date, Calendar.DATE, amount);
	}
	
	public static Date addHours(Date date, int amount) {
		return add(date, Calendar.HOUR_OF_DAY, amount);
	}
	
	public static Date addMinutes(Date date, int amount) {
		return add(date, Calendar.MINUTE, amount);
	}
	
	public static Date addWeeks(Date date, int amount) {
		return add(date, Calendar.WEEK_OF_YEAR, amount);
	}

	public static Date dateFromNow(int amount, int calendarField) {
		Calendar cal = Calendar.getInstance();
		cal.add(calendarField, amount);
		return cal.getTime();
	}
	
	public static Date daysFrom(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, amount);
		return cal.getTime();
	}

	public static String daysFrom(Date date, int amount, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(daysFrom(date, amount));
	}

	public static Date daysFromNow(int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, amount);
		return cal.getTime();
	}

	public static String daysFromNow(int amount, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(daysFromNow(amount));
	}

	public static long duration(Date date1, Date date2) {
		return date2.getTime() - date1.getTime();
	}
	
	public static Date endOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6);
		return cal.getTime();
	}
	
	public static String endOfWeek(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(endOfWeek(date));
	}
	
	public static Date getDate(String date) throws ParseException {
		try {
			return basicDate.parse(date);
		} catch(Exception e) {
			throw new ParseException("could not parse date: " + date + " using pattern: MM/dd/yyyy", -1);
		}
	}
	
	public static Date getDate(String date, String pattern) throws ParseException {
		try {
			return new SimpleDateFormat(pattern).parse(date);
		} catch(Exception e) {
			throw new ParseException("could not parse date: " + date + " using pattern: " + pattern, -1);
		}
	}
	
	public static List<Date> getDates(Date from, Date to) {
		return getDates(from, to, Calendar.DATE, 1);
	}
	
	public static List<Date> getDates(Date from, Date to, int dateStep) {
		return getDates(from, to, Calendar.DATE, dateStep);
	}

	public static List<Date> getDates(Date from, Date to, int dateField, int dateStep) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		List<Date> dates = new ArrayList<Date>();
		while(true) {
			Date date = cal.getTime();
			dates.add(date);
			cal.add(dateField, dateStep);
			if(cal.getTime().after(to)) {
				break;
			}
		}
		return dates;
	}

	public static List<Date> getDates(Date from, int count) {
		return getDates(from, 0, count, 1);
	}
	
	public static List<Date> getDates(Date from, int count, int dateStep) {
		return getDates(from, 0, count, dateStep);
	}

	public static List<Date> getDates(Date from, int start, int end, int dateStep) {
		return getDates(from, start, end, Calendar.DATE, dateStep);
	}
	
	public static List<Date> getDates(Date from, int start, int end, int dateField, int dateStep) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		List<Date> dates = new ArrayList<Date>();
		for(int i = start; i < end; i++) {
			Date date = cal.getTime();
			dates.add(date);
			cal.add(dateField, dateStep);
		}
		return dates;
	}

	public static List<Date> getDates(int count) {
		return getDates(new Date(), 0, count, 1);
	}
	
	public static List<Date> getDates(int count, int dateStep) {
		return getDates(new Date(), 0, count, dateStep);
	}
	
	public static int getDay() {
		return getField(new Date(), Calendar.DAY_OF_MONTH);
	}

	public static int getDay(Date date) {
		return getField(date, Calendar.DAY_OF_MONTH);
	}
	
	public static int getField(Date date, int field) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(field);
	}
	
	public static int getField(int field) {
		return getField(new Date(), field);
	}
	
	public static int getMonth() {
		return getField(new Date(), Calendar.MONTH);
	}
	
	public static int getMonth(Date date) {
		return getField(date, Calendar.MONTH);
	}
	
	public static List<Object[]> getSelectDates(String format, Date from, Date to) {
		return getSelectDates(format, from, to, Calendar.DATE, 1);
	}
	
	public static List<Object[]> getSelectDates(String format, Date from, Date to, int dateStep) {
		return getSelectDates(format, from, to, Calendar.DATE, dateStep);
	}

	public static List<Object[]> getSelectDates(String format, Date from, Date to, int dateField, int dateStep) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		List<Object[]> selectDates = new ArrayList<Object[]>();
		for(Date date : getDates(from, to, dateField, dateStep)) {
			selectDates.add(new Object[] { sdf.format(date), date.getTime() });
		}
		return selectDates;
	}
	
	public static List<Object[]> getSelectDates(String format, Date from, int count) {
		return getSelectDates(format, from, 0, count, Calendar.DATE, 1);
	}

	public static List<Object[]> getSelectDates(String format, Date from, int count, int dateStep) {
		return getSelectDates(format, from, 0, count, Calendar.DATE, dateStep);
	}

	public static List<Object[]> getSelectDates(String format, Date from, int start, int end, int dateStep) {
		return getSelectDates(format, from, start, end, Calendar.DATE, dateStep);
	}

	public static List<Object[]> getSelectDates(String format, Date from, int start, int end, int dateField, int dateStep) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		List<Object[]> selectDates = new ArrayList<Object[]>();
		for(Date date : getDates(from, start, end, dateField, dateStep)) {
			selectDates.add(new Object[] { sdf.format(date), date.getTime() });
		}
		return selectDates;
	}
	
	public static List<Object[]> getSelectDates(String format, int count) {
		return getSelectDates(format, new Date(), 0, count, Calendar.DATE, 1);
	}

	public static List<Object[]> getSelectDates(String format, int count, int dateStep) {
		return getSelectDates(format, new Date(), 0, count, Calendar.DATE, dateStep);
	}

	public static int getYear() {
		return getField(new Date(), Calendar.YEAR);
	}

	public static int getYear(Date date) {
		return getField(date, Calendar.YEAR);
	}
	
	public static int hourOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static int hours(Date date1, Date date2) {
		return (int) (duration(date1, date2) / 360000);
	}

	public static Date hoursFrom(Date date, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, amount);
		return cal.getTime();
	}

	public static String hoursFrom(Date date, int amount, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(hoursFrom(date, amount));
	}

	public static Date hoursFromNow(int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, amount);
		return cal.getTime();
	}
	
	public static String hoursFromNow(int amount, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(hoursFromNow(amount));
	}
	
	public static String httpDate() {
		return httpDate(new Date());
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
	
	public static int minute(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MINUTE);
	}
	
	public static int minutes(Date date1, Date date2) {
		return (int) (duration(date1, date2) / 60000);
	}

	public static Date minutesFromNow(int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, amount);
		return cal.getTime();
	}
	
	public static Date monthsFromNow(int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, amount);
		return cal.getTime();
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
	
	public static boolean sameWeek(Date date1, Date date2)  {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(date1);
		c2.setTime(date2);
		return c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR);
	}
	
	public static boolean sameYear(Date date1, Date date2)  {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(date1);
		c2.setTime(date2);
		return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
	}

	public static int seconds(Date date1, Date date2) {
		return (int) (duration(date1, date2) / 1000);
	}
	
	public static Date startOfDay() {
		return startOfDay(new Date());
	}
	
	public static Date startOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static String startOfDay(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(startOfDay(date));
	}
	
	public static String startOfDay(String format) {
		return startOfDay(new Date(), format);
	}
	
	public static Date startOfHour() {
		return startOfHour(new Date());
	}
	
	public static Date startOfHour(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static String startOfHour(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(startOfHour(date));
	}
	
	public static String startOfHour(String format) {
		return startOfHour(new Date(), format);
	}
	
	public static Date startOfWeek() {
		return startOfWeek(new Date());
	}
	
	public static Date startOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		return cal.getTime();
	}

	public static String startOfWeek(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(startOfWeek(date));
	}
	
	public static String startOfWeek(String format) {
		return startOfWeek(new Date(), format);
	}
	
	public static Date weeksFromNow(int amount) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_YEAR, amount);
		return cal.getTime();
	}

	public static String weeksFromNow(int amount, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(weeksFromNow(amount));
	}

	private DateUtils() {
		// static methods only
	}
	
}
