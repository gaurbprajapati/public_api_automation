package io.recruitcrm.contractStaffing.common;

import com.github.javafaker.Faker;
import io.rcrm.api.pojo.albatross.contractStaffing.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class TimesheetTestDataProcessor {

    private static final Faker faker = new Faker();

    private TimesheetTestDataProcessor() {
    }

    public static RuleEngineTemplate buildRuleEngineTemplate(TimesheetTestConfig config,
                                                              List<CustomRule> customRules) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = faker.number().digits(3);
        String prefix = config.isShiftBased() ? "Shift Rule Template" : "Hours Rule Template";

        RuleEngineTemplate template = new RuleEngineTemplate();
        template.setTemplateName(prefix + " " + timestamp + "_" + randomSuffix);
        template.setWorkLogType(config.getWorkLogType());
        template.setCalculateBreakTime(0);
        template.setBreakTimeThreshold(config.getBreakTimeThreshold());

        List<Integer> workDayIds = config.getWorkDayIds();
        template.setWorkDayIds(workDayIds);

        List<Integer> workTimeList = new ArrayList<>();
        List<Integer> workStartTimeList = new ArrayList<>();
        List<Integer> workEndTimeList = new ArrayList<>();

        for (int i = 0; i < workDayIds.size(); i++) {
            if (config.isShiftBased()) {
                workTimeList.add(0);
                workStartTimeList.add(config.getWorkStartTimeSeconds());
                workEndTimeList.add(config.getWorkEndTimeSeconds());
            } else {
                workTimeList.add(config.getWorkDurationSeconds());
                workStartTimeList.add(0);
                workEndTimeList.add(0);
            }
        }

        template.setWorkTime(workTimeList);
        template.setWorkStartTime(workStartTimeList);
        template.setWorkEndTime(workEndTimeList);
        template.setCustomRules(customRules != null ? customRules : new ArrayList<>());

        return template;
    }

    public static TimesheetSettings buildTimesheetSettings(TimesheetTestConfig config,
                                                            int jobId, int candidateId,
                                                            int agencyId,
                                                            List<CustomRule> customRules,
                                                            int isReimbursementEnabled) {
        Approvers approvers = new Approvers();
        approvers.setAgencyIds(Arrays.asList(agencyId));
        approvers.setClientIds(Arrays.asList());

        List<Integer> workDayIds = config.getWorkDayIds();

        List<Integer> workTimeList = new ArrayList<>();
        List<Integer> workStartTimeList = new ArrayList<>();
        List<Integer> workEndTimeList = new ArrayList<>();

        for (int i = 0; i < workDayIds.size(); i++) {
            if (config.isShiftBased()) {
                workTimeList.add(0);
                workStartTimeList.add(config.getWorkStartTimeSeconds());
                workEndTimeList.add(config.getWorkEndTimeSeconds());
            } else {
                workTimeList.add(config.getWorkDurationSeconds());
                workStartTimeList.add(0);
                workEndTimeList.add(0);
            }
        }

        TimesheetSettings settings = new TimesheetSettings();
        settings.setJobStartDate(config.getJobStartDate());
        settings.setJobEndDate(config.getJobEndDate());
        settings.setTimesheetFrequency(config.getTimesheetFrequency());
        settings.setTimesheetStartDay(config.getTimesheetStartDay());
        settings.setApprovers(approvers);
        settings.setPayCurrencyId(config.getPayCurrencyId());
        settings.setPayRate((int) config.getPayRate());
        settings.setBillCurrencyId(config.getBillCurrencyId());
        settings.setBillRate((int) config.getBillRate());
        settings.setWorkDayIds(workDayIds);
        settings.setWorkLogType(config.getWorkLogType());
        settings.setCalculateBreakTime(false);
        settings.setBreakTimeThreshold(config.getBreakTimeThreshold());
        settings.setWorkTime(workTimeList);
        settings.setWorkStartTime(workStartTimeList);
        settings.setWorkEndTime(workEndTimeList);
        settings.setIsPreferencesModified(1);
        settings.setJobId(jobId);
        settings.setContractorIds(Arrays.asList(candidateId));
        settings.setIsReimbursementEnabled(isReimbursementEnabled);
        settings.setIsUnplannedHoursPayEnabled(0);
        settings.setCustomRules(customRules != null ? customRules : new ArrayList<>());

        if (config.isHourBased()) {
            int auditTimestamp = (int) Instant.now().getEpochSecond();
            settings.setUpdatedOn(auditTimestamp);
            settings.setUpdatedBy(agencyId);
            settings.setUpdatedByUserTypeId(2);
            settings.setEnabledOn(auditTimestamp);
            settings.setEnabledBy(agencyId);
            settings.setEnabledByUserTypeId(2);
            settings.setIsRemarkMandatory(0);
        } else {
            settings.setUpdatedOn(null);
            settings.setUpdatedBy(null);
            settings.setEnabledOn(null);
            settings.setEnabledBy(null);
        }

        return settings;
    }

    public static List<WorkTimeDetail> buildShiftWorkTimeDetails(TimesheetTestConfig config) {
        int startTime = config.getWorkStartTimeSeconds();
        int endTime = config.getWorkEndTimeSeconds();
        int midPoint = startTime + (endTime - startTime) / 2;
        int gapSeconds = 5400;

        WorkTimeDetail segment1 = new WorkTimeDetail();
        segment1.setWorkStartTime(startTime);
        segment1.setWorkEndTime(midPoint);

        WorkTimeDetail segment2 = new WorkTimeDetail();
        segment2.setWorkStartTime(midPoint + gapSeconds);
        segment2.setWorkEndTime(endTime);

        return Arrays.asList(segment1, segment2);
    }

    public static Map<String, Object> buildHourBasedTimeLogEntry(TimesheetTestConfig config,
                                                                   int timeLogId, int timesheetId,
                                                                   String timesheetPeriod) {
        int workDuration = config.getWorkDurationSeconds();
        int breakDuration = config.getBreakTimeThreshold();
        int overtime = 0;
        if (config.getTotalOvertimeHours() > 0) {
            overtime = (int) (config.getTotalOvertimeHours() * 3600);
        }
        int totalTime = workDuration > 0 ? workDuration : 39600;

        Map<String, Object> log = new HashMap<>();
        log.put("workTime", totalTime);
        log.put("breakTime", breakDuration);
        log.put("overTime", overtime);
        log.put("totalTime", totalTime);
        log.put("remark", "remark1");
        log.put("workTimeDetails", null);
        log.put("id", timeLogId);
        log.put("timesheetId", timesheetId);
        if (timesheetPeriod != null) {
            log.put("timesheetPeriod", timesheetPeriod);
        }
        return log;
    }

    public static Map<String, Object> buildFreeSlotsPayload(TimesheetTestConfig config,
                                                              int candidateId,
                                                              long rangeStart, long rangeEnd) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("contractorIds", Arrays.asList(candidateId));
        payload.put("startDate", rangeStart);
        payload.put("endDate", rangeEnd);
        payload.put("timesheetFrequencyId", config.getTimesheetFrequency());
        payload.put("timesheetStartDay", config.getTimesheetStartDay());
        return payload;
    }

    public static int calculateTotalWorkTime(TimesheetTestConfig config, int timeLogCount) {
        int perDay = config.isShiftBased()
                ? config.getWorkDurationSeconds()
                : (config.getWorkDurationSeconds() > 0 ? config.getWorkDurationSeconds() : 39600);
        return perDay * timeLogCount;
    }

    public static int calculateTotalOvertime(TimesheetTestConfig config, int timeLogCount) {
        if (config.getTotalOvertimeHours() > 0) {
            return (int) (config.getTotalOvertimeHours() * 3600);
        }
        return 0;
    }

    public static Object[][] toDataProviderRows(List<TimesheetTestConfig> configs) {
        Object[][] rows = new Object[configs.size()][1];
        for (int i = 0; i < configs.size(); i++) {
            rows[i][0] = configs.get(i);
        }
        return rows;
    }

    public static Object[][] toDataProviderRowsWithScenarioMap(List<Map<String, Object>> scenarios) {
        Object[][] rows = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            rows[i][0] = scenarios.get(i);
        }
        return rows;
    }
}
