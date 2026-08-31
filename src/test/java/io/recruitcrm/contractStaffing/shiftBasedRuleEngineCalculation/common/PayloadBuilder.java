package io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.common;

import com.qa.api.util.TestUtil;
import io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.MultipleTimeEntryBaseTest;

import java.util.*;

/**
 * Builder class for creating API payloads for rule engine and timesheet operations.
 * Centralizes payload construction logic for cleaner test code.
 */
public class PayloadBuilder {

    // Constants matching MultipleTimeEntryBaseTest
    private static final String TEMPLATE_NAME = "templateName";
    private static final String WORK_LOG_TYPE = "workLogType";
    private static final String CALCULATE_BREAK_TIME = "calculateBreakTime";
    private static final String BREAK_TIME_THRESHOLD = "breakTimeThreshold";
    private static final String WORK_DAY_IDS = "workDayIds";
    private static final String WORK_TIME = "workTime";
    private static final String WORK_START_TIME = "workStartTime";
    private static final String WORK_END_TIME = "workEndTime";
    private static final String CUSTOM_RULES = "customRules";
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final int HOURS_METHOD = 1;
    private static final int SHIFTS_LOGGING = 2;
    private static final int DEFAULT_WORK_START_TIME = 28800; // 8:00 AM
    private static final int DEFAULT_WORK_END_TIME = 61200; // 5:00 PM

    /**
     * Builds payload for creating a rule template.
     */
    public static Map<String, Object> buildRuleTemplatePayload(
            String templateName,
            List<Integer> workDayIds,
            String regularHours,
            List<Map<String, Object>> customRules,
            String breakBillable,
            int workLogType,
            Integer breakTimeThreshold) {
        
        Map<String, Integer> workTimes = parseTimeRange(regularHours);
        Integer workStartTime = workTimes.getOrDefault(START_TIME, DEFAULT_WORK_START_TIME);
        Integer workEndTime = workTimes.getOrDefault(END_TIME, DEFAULT_WORK_END_TIME);

        Map<String, Object> payload = new HashMap<>();
        payload.put(TEMPLATE_NAME, templateName);
        payload.put(WORK_LOG_TYPE, workLogType);

        // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
        int calculateBreakTimeValue = 0;
        payload.put(CALCULATE_BREAK_TIME, calculateBreakTimeValue);

        if (calculateBreakTimeValue == 0 && breakTimeThreshold != null && breakTimeThreshold > 0) {
            payload.put(BREAK_TIME_THRESHOLD, breakTimeThreshold);
        } else {
            payload.put(BREAK_TIME_THRESHOLD, 0);
        }

        payload.put(WORK_DAY_IDS, workDayIds);

        // Build work time arrays for selected work days
        List<Integer> workTimeList = new ArrayList<>();
        List<Integer> workStartTimeList = new ArrayList<>();
        List<Integer> workEndTimeList = new ArrayList<>();

        for (int i = 0; i < workDayIds.size(); i++) {
            if (workLogType == SHIFTS_LOGGING) {
                workTimeList.add(0);
                workStartTimeList.add(workStartTime);
                workEndTimeList.add(workEndTime);
            } else if (workLogType == HOURS_METHOD) {
                Integer workDuration = workEndTime - workStartTime;
                workTimeList.add(workDuration);
                workStartTimeList.add(0);
                workEndTimeList.add(0);
            }
        }

        payload.put(WORK_TIME, workTimeList);
        payload.put(WORK_START_TIME, workStartTimeList);
        payload.put(WORK_END_TIME, workEndTimeList);
        payload.put(CUSTOM_RULES, customRules != null ? customRules : new ArrayList<>());

        return payload;
    }

    /**
     * Builds payload for enabling timesheet settings.
     */
    public static Map<String, Object> buildTimesheetSettingsPayload(
            Integer jobId,
            Integer candidateId,
            Integer userId,
            List<Map<String, Object>> templateRules,
            List<Integer> workDayIds,
            String regularHours,
            double payRate,
            double billRate,
            String breakBillable,
            Long jobStartDate,
            Long jobEndDate,
            Integer timesheetFrequency,
            Integer timesheetStartDay,
            Integer payCurrencyId,
            Integer billCurrencyId,
            Integer breakTimeThreshold,
            int workLogType) {
        
        Map<String, Integer> workTimes = parseTimeRange(regularHours);
        Integer workStartTime = workTimes.getOrDefault(START_TIME, DEFAULT_WORK_START_TIME);
        Integer workEndTime = workTimes.getOrDefault(END_TIME, DEFAULT_WORK_END_TIME);

        Map<String, Object> approvers = new HashMap<>();
        approvers.put("agencyIds", Arrays.asList(userId));
        approvers.put("clientIds", Arrays.asList());

        List<Integer> workTimeList = new ArrayList<>();
        List<Integer> workStartTimeList = new ArrayList<>();
        List<Integer> workEndTimeList = new ArrayList<>();

        for (int i = 0; i < workDayIds.size(); i++) {
            if (workLogType == SHIFTS_LOGGING) {
                workTimeList.add(0);
                workStartTimeList.add(workStartTime);
                workEndTimeList.add(workEndTime);
            } else if (workLogType == HOURS_METHOD) {
                Integer workDuration = workEndTime - workStartTime;
                workTimeList.add(workDuration);
                workStartTimeList.add(0);
                workEndTimeList.add(0);
            }
        }

        Map<String, Object> settings = new HashMap<>();
        settings.put("jobId", jobId);
        settings.put("contractorIds", Arrays.asList(candidateId));
        settings.put("jobStartDate", jobStartDate);
        settings.put("jobEndDate", jobEndDate);
        settings.put("timesheetFrequency", timesheetFrequency);
        settings.put("timesheetStartDay", timesheetStartDay);
        settings.put("approvers", approvers);
        settings.put("payCurrencyId", payCurrencyId);
        settings.put("payRate", payRate);
        settings.put("billCurrencyId", billCurrencyId);
        settings.put("billRate", billRate);
        settings.put("workDayIds", workDayIds);
        settings.put("workLogType", workLogType);

        // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
        int calculateBreakTime = 0;
        settings.put("calculateBreakTime", calculateBreakTime);

        if (calculateBreakTime == 0 && breakTimeThreshold != null && breakTimeThreshold > 0) {
            settings.put(BREAK_TIME_THRESHOLD, breakTimeThreshold);
        } else {
            settings.put(BREAK_TIME_THRESHOLD, 0);
        }

        settings.put("workTime", workTimeList);
        settings.put("workStartTime", workStartTimeList);
        settings.put("workEndTime", workEndTimeList);
        settings.put("updatedOn", null);
        settings.put("updatedBy", null);
        settings.put("enabledOn", null);
        settings.put("enabledBy", null);
        settings.put("isPreferencesModified", 1);
        settings.put("customRules", templateRules != null ? templateRules : new ArrayList<>());

        return settings;
    }

    /**
     * Builds payload for creating timesheet from free slots.
     */
    public static Map<String, Object> buildCreateTimesheetPayload(
            Integer candidateId,
            Long startDate,
            Long endDate) {
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractorIds", Arrays.asList(candidateId));
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("startDate", startDate);
        dateRange.put("endDate", endDate);
        payload.put("timesheetDates", Arrays.asList(dateRange));
        return payload;
    }

    /**
     * Builds payload for getting free slots.
     */
    public static Map<String, Object> buildFreeSlotsPayload(
            Integer candidateId,
            Long startDate,
            Long endDate,
            Integer timesheetFrequencyId,
            Integer timesheetStartDay) {
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractorIds", Arrays.asList(candidateId));
        payload.put("startDate", startDate);
        payload.put("endDate", endDate);
        payload.put("timesheetFrequencyId", timesheetFrequencyId);
        payload.put("timesheetStartDay", timesheetStartDay);
        return payload;
    }

    /**
     * Builds payload for approving timesheet.
     */
    public static Map<String, Object> buildApproveTimesheetPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("approvalStatus", 4); // TIMESHEET_APPROVED_STATUS
        return payload;
    }

    /**
     * Builds payload for evaluating timesheet.
     */
    public static Map<String, Object> buildEvaluateTimesheetPayload(Integer timesheetId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timesheetId", timesheetId);
        return payload;
    }

    /**
     * Parses time range string into start and end times in seconds.
     */
    private static Map<String, Integer> parseTimeRange(String timeRangeStr) {
        Map<String, Integer> times = new HashMap<>();
        if (timeRangeStr == null || timeRangeStr.trim().isEmpty()) return times;

        String input = timeRangeStr.trim();

        // Check for hourly duration format like "8 hours", "8:30 hours"
        if (input.toLowerCase().contains("hour") || input.toLowerCase().contains("hr")
                || input.toLowerCase().contains("h")) {
            int totalSeconds = parseHourlyDuration(input.toLowerCase());
            if (totalSeconds > 0) {
                times.put(START_TIME, DEFAULT_WORK_START_TIME);
                times.put(END_TIME, DEFAULT_WORK_START_TIME + totalSeconds);
            }
            return times;
        }

        // Time range format: "9:00-17:00"
        if (input.contains("-")) {
            String[] parts = input.split("-");
            if (parts.length == 2) {
                times.put(START_TIME, convertTimeStringToSeconds(parts[0].trim()));
                times.put(END_TIME, convertTimeStringToSeconds(parts[1].trim()));
            }
        }
        return times;
    }

    private static int parseHourlyDuration(String input) {
        // Try "8:30 hours" format
        java.util.regex.Pattern timeHour = java.util.regex.Pattern.compile("^(\\d+):(\\d+)\\s*hours?$", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = timeHour.matcher(input.trim());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1)) * 3600
                    + Integer.parseInt(matcher.group(2)) * 60;
        }
        // Try "8 hours" or "8.5 hours"
        java.util.regex.Pattern hours = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*h(?:our)?s?", java.util.regex.Pattern.CASE_INSENSITIVE);
        matcher = hours.matcher(input);
        if (matcher.find()) {
            return (int) Math.round(Double.parseDouble(matcher.group(1)) * 3600);
        }
        return 0;
    }

    private static int convertTimeStringToSeconds(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 0;
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60;
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return 0;
    }
}
