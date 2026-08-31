package com.qa.api.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.time.format.*;
import java.time.*;


public class DateUtil {

	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public DateUtil() {
		// TODO Auto-generated constructor stub
	}

	//Use + or - int value to get the date in past or future
	public static String getNDateFromTodayDateString(int days, String format){
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(nDaysFromToday(days));
	}

	public static String getYesterdayDateString(String format){
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(yesterday());
	}

	public static String getTomorrowDateString(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(tommarrow());
	}

	public static String getYesterdayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, YYYY");
		return dateFormat.format(yesterday());
	}

	public static String getTomorrowDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, YYYY");
		return dateFormat.format(tommarrow());
	}

	public static String getTodayDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, YYYY");
		return dateFormat.format(today());
	}

	public static String getTodayDateString(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(today());
	}


	public static String getPastDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, YYYY");
		return dateFormat.format(past());
	}

	public static String getFutureDateString() {
		DateFormat dateFormat = new SimpleDateFormat("MMM d, YYYY");
		return dateFormat.format(future());
	}


	public static Date yesterday() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}

	//Use + or - int value to get the date in past or future
	public static Date nDaysFromToday(int days) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}


	public static Date tommarrow() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, +1);
		return cal.getTime();
	}

	public static Date addOneHourToDate(Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, 1);
		return cal.getTime();
	}

	public static Date today() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 0);
		return cal.getTime();
	}

	public static Date lastMonthDate() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH,-1);
		return cal.getTime();
	}

	public static Date lastYearDate() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR,-1);
		return cal.getTime();
	}

	public static Date thisWeekDate() {
		final Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
		cal.add(Calendar.DATE, -daysFromMonday);
		return cal.getTime();
	}

	public static Date lastWeekDate() {
		final Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
		cal.add(Calendar.DATE, -daysFromMonday);
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		return cal.getTime();
	}

	public static Date nextWeekDate() {
		final Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
		cal.add(Calendar.DATE, -daysFromMonday);
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		return cal.getTime();
	}

	public static Date thisMonthDate() {
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date nextMonthDate() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date thisQuarterDate() {
		final Calendar cal = Calendar.getInstance();
		int currentMonth = cal.get(Calendar.MONTH);
		int quarterStartMonth = (currentMonth / 3) * 3;
		cal.set(Calendar.MONTH, quarterStartMonth);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date lastQuarterDate() {
		final Calendar cal = Calendar.getInstance();
		int currentMonth = cal.get(Calendar.MONTH);
		int currentQuarter = currentMonth / 3;
		int lastQuarter = currentQuarter - 1;

		if (lastQuarter < 0) {
			lastQuarter = 3;
			cal.add(Calendar.YEAR, -1);
		}

		int quarterStartMonth = lastQuarter * 3;
		cal.set(Calendar.MONTH, quarterStartMonth);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date thisYearDate() {
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date getStartOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		return cal.getTime();
	}

	public static Date getEndOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return cal.getTime();
	}

	public static Date getStartOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	public static Date getEndOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	public static Date getStartOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		return cal.getTime();
	}

	public static Date getEndOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
		return cal.getTime();
	}

	public static long startOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTimeInMillis() / 1000;
	}

	public static long endOfDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTimeInMillis() / 1000;
	}


	public static Date past() {
		final Calendar cal = Calendar.getInstance();
		int rand = (int) ((Math.random() * (1000 - 30)) + 30);
		cal.add(Calendar.DATE, -1*rand);
		return cal.getTime();
	}

	public static Date future() {
		final Calendar cal = Calendar.getInstance();
		int rand = (int) ((Math.random() * (1000 - 30)) + 30);
		cal.add(Calendar.DATE, rand);
		return cal.getTime();
	}

	public static String currentMonth() {
		final Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("MMM");
		return dateFormat.format(cal.getTime());
	}

	public static String lastMonth() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		DateFormat dateFormat = new SimpleDateFormat("MMM");
		return dateFormat.format(cal.getTime());
	}

	public static String currentYear() {
		final Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("YYYY");
		return dateFormat.format(cal.getTime());
	}

	public static String lastYear() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		DateFormat dateFormat = new SimpleDateFormat("YYYY");
		return dateFormat.format(cal.getTime());
	}

	public static Date todayStartTime() {
		Calendar calendar = Calendar.getInstance();

		// Set start date to today's 12:00 AM
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date todayEndTime() {
		Calendar calendar = Calendar.getInstance();

		// Set start date to today's 12:00 AM
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 45);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static String getStartDayEpochTime(){
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

		// Start of the day in IST
		ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));

		// Convert to Unix timestamp
		return String.valueOf(startOfDay.toEpochSecond());
	}

	public static String getEndDayEpochTime(){
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

		// Start of the day in IST
		ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));

		// End of the day in IST
		ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

		// Convert to Unix timestamp
		return String.valueOf(endOfDay.toEpochSecond());
	}


	public static String getCurrentEpochTime(){
		ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		return String.valueOf(now.toEpochSecond());
	}

	public static String getThisWeekDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(thisWeekDate());
	}

	public static String getLastWeekDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(lastWeekDate());
	}

	public static String getNextWeekDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(nextWeekDate());
	}

	public static String getThisMonthDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(thisMonthDate());
	}

	public static String getNextMonthDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(nextMonthDate());
	}

	public static String getThisQuarterDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(thisQuarterDate());
	}

	public static String getLastQuarterDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(lastQuarterDate());
	}

	public static String getThisYearDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(thisYearDate());
	}

	public static String getLastMonthDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(lastMonthDate());
	}

	public static String getLastYearDateString() {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		return dateFormat.format(lastYearDate());
	}

	public static String get30DaysAgoString() {
		return getNDateFromTodayDateString(-30, "yyyy-MM-dd");
	}


	public static String get60DaysAgoString() {
		return getNDateFromTodayDateString(-60, "yyyy-MM-dd");
	}

	public static String get90DaysAgoString() {
		return getNDateFromTodayDateString(-90, "yyyy-MM-dd");
	}

	public static String get365DaysAgoString() {
		return getNDateFromTodayDateString(-365, "yyyy-MM-dd");
	}

	public static String getNDaysFromNowString(int days) {
		return getNDateFromTodayDateString(days, "yyyy-MM-dd");
	}

	public static Map<String, String> getAllRelativeDatesMap() {
		Map<String, String> dates = new HashMap<>();

		// Basic dates
		dates.put("today", getTodayDateString("yyyy-MM-dd"));
		dates.put("yesterday", getYesterdayDateString("yyyy-MM-dd"));
		dates.put("7_days_ago", getNDateFromTodayDateString(-7, "yyyy-MM-dd"));
		dates.put("30_days_ago", getNDateFromTodayDateString(-30,"yyyy-MM-dd"));
		dates.put("60_days_ago", getNDateFromTodayDateString(-60, "yyyy-MM-dd"));
		dates.put("90_days_ago", getNDateFromTodayDateString(-90, "yyyy-MM-dd"));
		dates.put("365_days_ago", getNDateFromTodayDateString(-365, "yyyy-MM-dd"));

		// Week dates
		dates.put("this_week", getThisWeekDateString());
		dates.put("last_week", getLastWeekDateString());

		// Month dates
		dates.put("this_month", getThisMonthDateString());
		dates.put("last_month", getLastMonthDateString());

		// Quarter dates
		dates.put("this_quarter", getThisQuarterDateString());
		dates.put("last_quarter", getLastQuarterDateString());

		// Year dates
		dates.put("this_year", getThisYearDateString());
		dates.put("last_year", getLastYearDateString());

		return dates;
	}

	public static long dateToEpochSeconds(String dateStr) {
		try {
			java.time.LocalDate date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().getEpochSecond();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse date: " + dateStr + ". Expected format: yyyy-MM-dd");
		}
	}

	public static long[] getDateRange(int filterId) {
		Calendar cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
		cal.set(Calendar.MILLISECOND, 0);

		switch (filterId) {
			case 1: // All Time
				return new long[]{-19800, cal.getTimeInMillis() / 1000 + 15L * 365 * 24 * 3600};
			case 2: // Today
				return new long[]{startOfDay(today()), endOfDay(today())};
			case 4: // Yesterday
				Date yesterday = addDays(today(), -1);
				return new long[]{startOfDay(yesterday), endOfDay(yesterday)};
			case 5: // This Week
				Date monday = getStartOfWeek(today());
				Date sunday = getEndOfWeek(today());
				return new long[]{startOfDay(monday), endOfDay(sunday)};
			case 6: // Last Week
				Date lastMonday = getStartOfWeek(addDays(today(), -7));
				Date lastSunday = getEndOfWeek(addDays(today(), -7));
				return new long[]{startOfDay(lastMonday), endOfDay(lastSunday)};
			case 7: // This Month
				Date firstDay = getStartOfMonth(today());
				Date lastDay = getEndOfMonth(today());
				return new long[]{startOfDay(firstDay), endOfDay(lastDay)};
			case 8: // Last Month
				Date firstLastMonth = getStartOfMonth(lastMonthDate());
				Date lastLastMonth = getEndOfMonth(lastMonthDate());
				return new long[]{startOfDay(firstLastMonth), endOfDay(lastLastMonth)};
			case 9: // Custom Range → example: last year
				Date firstLastYear = getStartOfYear(lastYearDate());
				Date lastLastYear = getEndOfYear(lastYearDate());
				return new long[]{startOfDay(firstLastYear), endOfDay(lastLastYear)};
			default:
				throw new IllegalArgumentException("Invalid filter id: " + filterId);
		}
	}

	public static String getEpochForDateScenario(String scenario) {
		switch (scenario) {
			case "today":
				return String.valueOf(dateToEpochSeconds(getTodayDateString("yyyy-MM-dd")));
			case "yesterday":
				return String.valueOf(dateToEpochSeconds(getYesterdayDateString("yyyy-MM-dd")));
			case "tomorrow":
				return String.valueOf(dateToEpochSeconds(getTomorrowDateString("yyyy-MM-dd")));
			case "this_week":
				return String.valueOf(dateToEpochSeconds(getThisWeekDateString()));
			case "last_week":
				return String.valueOf(dateToEpochSeconds(getLastWeekDateString()));
			case "next_week":
				return String.valueOf(dateToEpochSeconds(getNextWeekDateString()));
			case "this_month":
				return String.valueOf(dateToEpochSeconds(getThisMonthDateString()));
			case "last_month":
				return String.valueOf(dateToEpochSeconds(getLastMonthDateString()));
			case "next_month":
				return String.valueOf(dateToEpochSeconds(getNextMonthDateString()));
			case "this_quarter":
				return String.valueOf(dateToEpochSeconds(getThisQuarterDateString()));
			case "last_quarter":
				return String.valueOf(dateToEpochSeconds(getLastQuarterDateString()));
			case "this_year":
				return String.valueOf(dateToEpochSeconds(getThisYearDateString()));
			case "last_year":
				return String.valueOf(dateToEpochSeconds(getLastYearDateString()));
			default:
				return String.valueOf(dateToEpochSeconds(getTodayDateString("yyyy-MM-dd")));
		}
	}

	public static String convertToUTCFormat(String inputDate) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

		ZoneId sourceZone;
		String normalizedDate;

		if (inputDate.contains(" IST")) {
			// Backend (PHP/Laravel) resolves "IST" as Israel Standard Time (UTC+2)
			normalizedDate = inputDate.replace(" IST", "");
			sourceZone = ZoneId.of("Asia/Jerusalem");
		} else if (inputDate.contains(" UTC")) {
			normalizedDate = inputDate.replace(" UTC", "");
			sourceZone = ZoneOffset.UTC;
		} else if (inputDate.contains(" GMT")) {
			normalizedDate = inputDate.replace(" GMT", "");
			sourceZone = ZoneOffset.UTC;
		} else {
			normalizedDate = inputDate;
			sourceZone = ZoneOffset.UTC;
		}

		LocalDateTime localDateTime = LocalDateTime.parse(normalizedDate, inputFormatter);
		ZonedDateTime sourceDate = localDateTime.atZone(sourceZone);
		ZonedDateTime utcDate = sourceDate.withZoneSameInstant(ZoneOffset.UTC);
		return utcDate.format(outputFormatter);
	}

}