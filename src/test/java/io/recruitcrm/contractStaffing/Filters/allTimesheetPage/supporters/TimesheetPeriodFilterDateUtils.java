package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters;

import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public final class TimesheetPeriodFilterDateUtils {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private TimesheetPeriodFilterDateUtils() {
    }

    public static final class DateRange {
        public final long startEpoch;
        public final long endEpoch;

        public DateRange(long startEpoch, long endEpoch) {
            this.startEpoch = startEpoch;
            this.endEpoch = endEpoch;
        }
    }

    public static long startOfDayEpoch(LocalDate date) {
        return date.atStartOfDay(ZONE).toEpochSecond();
    }

    public static long endOfDayEpoch(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZONE).toEpochSecond() - 1;
    }

    public static LocalDate toLocalDate(long epochSeconds) {
        return Instant.ofEpochSecond(epochSeconds).atZone(ZONE).toLocalDate();
    }

    public static boolean periodsOverlap(long periodStart, long periodEnd, long rangeStart, long rangeEnd) {
        return periodStart <= rangeEnd && periodEnd >= rangeStart;
    }

    public static DateRange resolveIsPresetRange(String preset, LocalDate today) {
        switch (preset) {
            case "all_time":
                return new DateRange(0, Long.MAX_VALUE / 2);
            case "today":
                return dayRange(today);
            case "yesterday":
                return dayRange(today.minusDays(1));
            case "this_week":
                LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
                LocalDate weekEnd = weekStart.plusDays(6);
                return new DateRange(startOfDayEpoch(weekStart), endOfDayEpoch(weekEnd));
            case "last_week":
                LocalDate lastWeekStart = today.minusDays(today.getDayOfWeek().getValue() + 6L);
                LocalDate lastWeekEnd = lastWeekStart.plusDays(6);
                return new DateRange(startOfDayEpoch(lastWeekStart), endOfDayEpoch(lastWeekEnd));
            case "this_month":
                return new DateRange(
                        startOfDayEpoch(today.withDayOfMonth(1)),
                        endOfDayEpoch(today.with(TemporalAdjusters.lastDayOfMonth())));
            case "last_month":
                LocalDate lastMonth = today.minusMonths(1);
                return new DateRange(
                        startOfDayEpoch(lastMonth.withDayOfMonth(1)),
                        endOfDayEpoch(lastMonth.with(TemporalAdjusters.lastDayOfMonth())));
            case "this_quarter":
                int currentQuarter = (today.getMonthValue() - 1) / 3;
                LocalDate quarterStart = LocalDate.of(today.getYear(), currentQuarter * 3 + 1, 1);
                LocalDate quarterEnd = quarterStart.plusMonths(3).minusDays(1);
                return new DateRange(startOfDayEpoch(quarterStart), endOfDayEpoch(quarterEnd));
            case "last_quarter":
                LocalDate inLastQuarter = today.minusMonths(3);
                int lastQuarter = (inLastQuarter.getMonthValue() - 1) / 3;
                LocalDate lastQuarterStart = LocalDate.of(inLastQuarter.getYear(), lastQuarter * 3 + 1, 1);
                LocalDate lastQuarterEnd = lastQuarterStart.plusMonths(3).minusDays(1);
                return new DateRange(startOfDayEpoch(lastQuarterStart), endOfDayEpoch(lastQuarterEnd));
            case "this_year":
                return new DateRange(
                        startOfDayEpoch(LocalDate.of(today.getYear(), 1, 1)),
                        endOfDayEpoch(LocalDate.of(today.getYear(), 12, 31)));
            case "last_year":
                int lastYear = today.getYear() - 1;
                return new DateRange(
                        startOfDayEpoch(LocalDate.of(lastYear, 1, 1)),
                        endOfDayEpoch(LocalDate.of(lastYear, 12, 31)));
            case "last_30":
                return rollingDaysRange(today, 30);
            case "last_60":
                return rollingDaysRange(today, 60);
            case "last_90":
                return rollingDaysRange(today, 90);
            case "last_365":
                return rollingDaysRange(today, 365);
            default:
                throw new IllegalArgumentException("Unsupported is preset: " + preset);
        }
    }

    public static DateRange parseBetweenFilterValue(String filterValue) {
        JSONObject json = new JSONObject(filterValue);
        return new DateRange(json.getLong("start"), json.getLong("end"));
    }

    public static boolean matchesIsPreset(long periodStart, long periodEnd, String preset, LocalDate today) {
        if ("all_time".equals(preset)) {
            return true;
        }
        DateRange range = resolveIsPresetRange(preset, today);
        return periodsOverlap(periodStart, periodEnd, range.startEpoch, range.endEpoch);
    }

    public static boolean matchesIsEqualTo(long periodStart, long periodEnd, long filterDateEpoch) {
        return periodStart == filterDateEpoch;
    }

    public static boolean matchesIsBefore(long periodStart, long periodEnd, long filterDateEpoch) {
        return periodStart < filterDateEpoch;
    }

    public static boolean matchesIsAfter(long periodStart, long periodEnd, long filterDateEpoch) {
        return periodStart >= filterDateEpoch;
    }

    public static boolean matchesIsBetween(long periodStart, long periodEnd, String filterValue) {
        DateRange range = parseBetweenFilterValue(filterValue);
        return periodsOverlap(periodStart, periodEnd, range.startEpoch, range.endEpoch);
    }

    public static boolean matchesIsNotBetween(long periodStart, long periodEnd, String filterValue) {
        return !matchesIsBetween(periodStart, periodEnd, filterValue);
    }

    public static boolean matchesIsMoreThan(long periodStart, long periodEnd, int days, LocalDate today) {
        LocalDate periodStartDate = toLocalDate(periodStart);
        LocalDate cutoffDate = today.minusDays(days);
        return periodStartDate.isBefore(cutoffDate) || periodStartDate.isEqual(cutoffDate);
    }

    public static boolean matchesIsLessThan(long periodStart, long periodEnd, int days, LocalDate today) {
        DateRange window = rollingDaysRange(today, days);
        return periodsOverlap(periodStart, periodEnd, window.startEpoch, window.endEpoch);
    }

    public static String buildBetweenFilterValue(long startEpoch, long endEpoch) {
        return new JSONObject().put("start", startEpoch).put("end", endEpoch).toString();
    }

    public static String formatDateRangeLabel(long startEpoch, long endEpoch) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return formatter.format(toLocalDate(startEpoch)) + " - " + formatter.format(toLocalDate(endEpoch));
    }

    private static DateRange dayRange(LocalDate date) {
        return new DateRange(startOfDayEpoch(date), endOfDayEpoch(date));
    }

    private static DateRange rollingDaysRange(LocalDate today, int days) {
        return new DateRange(startOfDayEpoch(today.minusDays(days)), endOfDayEpoch(today));
    }
}
