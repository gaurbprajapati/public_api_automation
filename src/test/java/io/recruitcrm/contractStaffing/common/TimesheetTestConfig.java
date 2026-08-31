package io.recruitcrm.contractStaffing.common;

import java.util.*;

public class TimesheetTestConfig {

    private String testId;
    private String comment;
    private String method;
    private String frequency;
    private String startOfTheWeek;
    private List<Integer> dayPattern;
    private String regularHours;
    private String breakBillable;
    private double payRate;
    private double billRate;
    private long jobStartDate;
    private long jobEndDate;
    private int timesheetFrequency;
    private int timesheetStartDay;
    private int payCurrencyId;
    private int billCurrencyId;
    private String rulesApplied;
    private String actualWorkTime;
    private String breakTime;
    private int breakTimeThreshold;
    private String acceptedPayBillRate;
    private String overtimeHours;
    private double totalOvertimeHours;
    private String weeklyOvertimeHours;
    private double expectedTotalPayRate;
    private double expectedTotalBillRate;

    private Map<String, Object> rawData;

    public static TimesheetTestConfig fromMap(Map<String, Object> data) {
        TimesheetTestConfig config = new TimesheetTestConfig();
        config.rawData = data;

        config.testId = getString(data, "testId", "");
        config.comment = getString(data, "_comment", "");
        config.method = getString(data, "method", "Shift");
        config.frequency = getString(data, "frequency", "Weekly");
        config.startOfTheWeek = getString(data, "start of the week", "[mon]");
        config.dayPattern = parseDayPattern(getString(data, "dayPattern", "[mon,tue,wed,thu,fri,sat]"));
        config.regularHours = getString(data, "regularHours", "9:00-17:00");
        config.breakBillable = getString(data, "breakBillable", "No");
        config.payRate = getDouble(data, "payRate", 5000);
        config.billRate = getDouble(data, "billRate", 6000);
        config.jobStartDate = getLong(data, "jobStartDate", 1751328000L);
        config.jobEndDate = getLong(data, "jobEndDate", 1759017600L);
        config.timesheetFrequency = getInt(data, "timesheetFrequency", 2);
        config.timesheetStartDay = getInt(data, "timesheetStartDay", 1);
        config.payCurrencyId = getInt(data, "payCurrencyId", 53);
        config.billCurrencyId = getInt(data, "billCurrencyId", 53);
        config.rulesApplied = getString(data, "rulesApplied", "");
        config.actualWorkTime = getString(data, "actualWorkTime", "");
        config.breakTime = getString(data, "breakTime", "");
        config.breakTimeThreshold = getInt(data, "breakTimeThreshold", 0);
        config.acceptedPayBillRate = getString(data, "acceptedPayBillRate", "");
        config.overtimeHours = getString(data, "overtimeHours", "");
        config.totalOvertimeHours = getDouble(data, "totalOvertimeHours", 0);
        config.weeklyOvertimeHours = getString(data, "weeklyOvertimeHours", "");
        config.expectedTotalPayRate = getDouble(data, "expectedTotalPayRate", 0);
        config.expectedTotalBillRate = getDouble(data, "expectedTotalBillRate", 0);

        return config;
    }

    public boolean isShiftBased() {
        return "Shift".equalsIgnoreCase(method) || "2".equals(method);
    }

    public boolean isHourBased() {
        return "Hours".equalsIgnoreCase(method) || "Hour".equalsIgnoreCase(method) || "1".equals(method);
    }

    public int getWorkLogType() {
        return isShiftBased() ? 2 : 1;
    }

    public List<Integer> getWorkDayIds() {
        return dayPattern != null && !dayPattern.isEmpty() ? dayPattern : Arrays.asList(1, 2, 3, 4, 5, 6);
    }

    public int getWorkStartTimeSeconds() {
        return parseTimeToSeconds(regularHours, true);
    }

    public int getWorkEndTimeSeconds() {
        return parseTimeToSeconds(regularHours, false);
    }

    public int getWorkDurationSeconds() {
        int start = getWorkStartTimeSeconds();
        int end = getWorkEndTimeSeconds();
        return end > start ? end - start : 0;
    }

    public Object getRawValue(String key) {
        return rawData != null ? rawData.get(key) : null;
    }

    private static List<Integer> parseDayPattern(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) return Arrays.asList(1, 2, 3, 4, 5, 6);
        String cleaned = pattern.replaceAll("[\\[\\]\\s]", "").toLowerCase();
        if (cleaned.isEmpty()) return Arrays.asList(1, 2, 3, 4, 5, 6);

        Map<String, Integer> dayMap = new LinkedHashMap<>();
        dayMap.put("mon", 1);
        dayMap.put("tue", 2);
        dayMap.put("wed", 3);
        dayMap.put("thu", 4);
        dayMap.put("fri", 5);
        dayMap.put("sat", 6);
        dayMap.put("sun", 7);

        List<Integer> result = new ArrayList<>();
        for (String day : cleaned.split(",")) {
            Integer id = dayMap.get(day.trim());
            if (id != null) result.add(id);
        }
        return result.isEmpty() ? Arrays.asList(1, 2, 3, 4, 5, 6) : result;
    }

    private static int parseTimeToSeconds(String regularHours, boolean isStart) {
        if (regularHours == null || !regularHours.contains("-")) return isStart ? 32400 : 61200;
        String[] parts = regularHours.split("-");
        String timeStr = isStart ? parts[0].trim() : parts[1].trim();
        String[] hm = timeStr.split(":");
        if (hm.length == 2) {
            try {
                return Integer.parseInt(hm[0]) * 3600 + Integer.parseInt(hm[1]) * 60;
            } catch (NumberFormatException e) {
                return isStart ? 32400 : 61200;
            }
        }
        return isStart ? 32400 : 61200;
    }

    private static String getString(Map<String, Object> map, String key, String defaultVal) {
        Object v = map.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    private static int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }

    private static long getLong(Map<String, Object> map, String key, long defaultVal) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String) v); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }

    private static double getDouble(Map<String, Object> map, String key, double defaultVal) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); } catch (NumberFormatException e) { return defaultVal; }
        }
        return defaultVal;
    }

    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getStartOfTheWeek() { return startOfTheWeek; }
    public void setStartOfTheWeek(String startOfTheWeek) { this.startOfTheWeek = startOfTheWeek; }
    public List<Integer> getDayPattern() { return dayPattern; }
    public void setDayPattern(List<Integer> dayPattern) { this.dayPattern = dayPattern; }
    public String getRegularHours() { return regularHours; }
    public void setRegularHours(String regularHours) { this.regularHours = regularHours; }
    public String getBreakBillable() { return breakBillable; }
    public void setBreakBillable(String breakBillable) { this.breakBillable = breakBillable; }
    public double getPayRate() { return payRate; }
    public void setPayRate(double payRate) { this.payRate = payRate; }
    public double getBillRate() { return billRate; }
    public void setBillRate(double billRate) { this.billRate = billRate; }
    public long getJobStartDate() { return jobStartDate; }
    public void setJobStartDate(long jobStartDate) { this.jobStartDate = jobStartDate; }
    public long getJobEndDate() { return jobEndDate; }
    public void setJobEndDate(long jobEndDate) { this.jobEndDate = jobEndDate; }
    public int getTimesheetFrequency() { return timesheetFrequency; }
    public void setTimesheetFrequency(int timesheetFrequency) { this.timesheetFrequency = timesheetFrequency; }
    public int getTimesheetStartDay() { return timesheetStartDay; }
    public void setTimesheetStartDay(int timesheetStartDay) { this.timesheetStartDay = timesheetStartDay; }
    public int getPayCurrencyId() { return payCurrencyId; }
    public void setPayCurrencyId(int payCurrencyId) { this.payCurrencyId = payCurrencyId; }
    public int getBillCurrencyId() { return billCurrencyId; }
    public void setBillCurrencyId(int billCurrencyId) { this.billCurrencyId = billCurrencyId; }
    public String getRulesApplied() { return rulesApplied; }
    public void setRulesApplied(String rulesApplied) { this.rulesApplied = rulesApplied; }
    public String getActualWorkTime() { return actualWorkTime; }
    public void setActualWorkTime(String actualWorkTime) { this.actualWorkTime = actualWorkTime; }
    public String getBreakTime() { return breakTime; }
    public void setBreakTime(String breakTime) { this.breakTime = breakTime; }
    public int getBreakTimeThreshold() { return breakTimeThreshold; }
    public void setBreakTimeThreshold(int breakTimeThreshold) { this.breakTimeThreshold = breakTimeThreshold; }
    public String getAcceptedPayBillRate() { return acceptedPayBillRate; }
    public void setAcceptedPayBillRate(String acceptedPayBillRate) { this.acceptedPayBillRate = acceptedPayBillRate; }
    public String getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(String overtimeHours) { this.overtimeHours = overtimeHours; }
    public double getTotalOvertimeHours() { return totalOvertimeHours; }
    public void setTotalOvertimeHours(double totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }
    public String getWeeklyOvertimeHours() { return weeklyOvertimeHours; }
    public void setWeeklyOvertimeHours(String weeklyOvertimeHours) { this.weeklyOvertimeHours = weeklyOvertimeHours; }
    public double getExpectedTotalPayRate() { return expectedTotalPayRate; }
    public void setExpectedTotalPayRate(double expectedTotalPayRate) { this.expectedTotalPayRate = expectedTotalPayRate; }
    public double getExpectedTotalBillRate() { return expectedTotalBillRate; }
    public void setExpectedTotalBillRate(double expectedTotalBillRate) { this.expectedTotalBillRate = expectedTotalBillRate; }

    @Override
    public String toString() {
        return testId != null && !testId.isEmpty() ? testId : "TimesheetTestConfig{method=" + method + ", freq=" + timesheetFrequency + "}";
    }
}
