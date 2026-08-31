package io.recruitcrm.contractStaffing.publicApi.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.shiftBasedRuleEngineCalculation.MultipleTimeEntryBaseTest;
import io.restassured.response.Response;

public abstract class PublicApiBaseTest extends MultipleTimeEntryBaseTest {

    protected static final String[] DAY_ABBREVS = { "", "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
    protected static final String NO_HOURS = "0h 0min";

    // ========================== API Calls ==========================

    protected Response fetchTimesheetById(Integer timesheetId, String apiAuthToken,
            Map<String, String> queryParams) {
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", timesheetId.toString());
        return RestClient.doGet("JSON", baseURL, "timesheets/{id}",
                apiAuthToken, queryParams, pathParams, true);
    }

    protected Response fetchTimesheetsList(String apiAuthToken, Map<String, String> queryParams) {
        return RestClient.doGet("JSON", baseURL, "timesheets",
                apiAuthToken, queryParams, null, true);
    }

    // ========================== Metadata Assertions ==========================

    protected void assertBasicResponseValid(Response response, String tid) {
        assertThat("Status should be 200 for " + tid, response.getStatusCode(), is(200));
        assertThat("Response body should not be empty for " + tid, response.getBody().asString(), not(is("")));
    }

    protected void assertTimesheetMetadata(Response response, String tid) {
        assertThat("timesheet_status.id (" + tid + ")", response.jsonPath().get("timesheet_status.id"), notNullValue());
        assertThat("timesheet_status.label (" + tid + ")", response.jsonPath().getString("timesheet_status.label"),
                is("Approved"));
        assertThat("timesheet_status.performed_by (" + tid + ")",
                response.jsonPath().get("timesheet_status.performed_by"), notNullValue());
        assertThat("timesheet_status.performed_on (" + tid + ")",
                response.jsonPath().get("timesheet_status.performed_on"), notNullValue());

        assertThat("timesheet_period.start_date (" + tid + ")", response.jsonPath().get("timesheet_period.start_date"),
                notNullValue());
        assertThat("timesheet_period.end_date (" + tid + ")", response.jsonPath().get("timesheet_period.end_date"),
                notNullValue());

        assertThat("related_entities_slug.candidate (" + tid + ")",
                response.jsonPath().get("related_entities_slug.candidate"), notNullValue());
        assertThat("related_entities_slug.job (" + tid + ")", response.jsonPath().get("related_entities_slug.job"),
                notNullValue());
        assertThat("related_entities_slug.company (" + tid + ")",
                response.jsonPath().get("related_entities_slug.company"), notNullValue());
        assertThat("related_entities_slug.deals (" + tid + ")", response.jsonPath().get("related_entities_slug.deals"),
                nullValue());
    }

    // ========================== Pay & Bill Assertions ==========================

    protected void assertPayBillStructure(Response response, String tid) {
        assertThat("pay.rate (" + tid + ")", response.jsonPath().get("pay.rate"), notNullValue());
        assertThat("pay.currency (" + tid + ")", response.jsonPath().get("pay.currency"), notNullValue());
        assertThat("pay.currency_id (" + tid + ")", response.jsonPath().get("pay.currency_id"), notNullValue());
        assertThat("pay.amount (" + tid + ")", response.jsonPath().get("pay.amount"), notNullValue());

        String timesheetStatusLabel = response.jsonPath().getString("timesheet_status.label");
        boolean isApproved = "Approved".equalsIgnoreCase(timesheetStatusLabel);
        if (isApproved) {
            assertThat("pay.details.id (" + tid + ")", response.jsonPath().get("pay.details.id"), notNullValue());
            assertThat("pay.details.label (" + tid + ")", response.jsonPath().get("pay.details.label"), notNullValue());
            assertThat("pay.details.payout_number (" + tid + ")", response.jsonPath().get("pay.details.payout_number"),
                    nullValue());
            assertThat("pay.details.paid_on (" + tid + ")", response.jsonPath().get("pay.details.paid_on"),
                    nullValue());
        } else {
            assertThat("pay.details (" + tid + ")", response.jsonPath().get("pay.details"), nullValue());
        }

        assertThat("bill.rate (" + tid + ")", response.jsonPath().get("bill.rate"), notNullValue());
        assertThat("bill.currency (" + tid + ")", response.jsonPath().get("bill.currency"), notNullValue());
        assertThat("bill.currency_id (" + tid + ")", response.jsonPath().get("bill.currency_id"), notNullValue());
        assertThat("bill.amount (" + tid + ")", response.jsonPath().get("bill.amount"), notNullValue());
        assertThat("bill.details.id (" + tid + ")", response.jsonPath().get("bill.details.id"), notNullValue());
        assertThat("bill.details.label (" + tid + ")", response.jsonPath().get("bill.details.label"), notNullValue());
        assertThat("bill.details.invoice_number (" + tid + ")", response.jsonPath().get("bill.details.invoice_number"),
                nullValue());
        assertThat("bill.details.invoice_created_on (" + tid + ")",
                response.jsonPath().get("bill.details.invoice_created_on"), nullValue());
    }

    protected void assertPayBillAmounts(Response response, TestScenarioData data) {
        String tid = data.getTestId();
        double actualPayAmount = response.jsonPath().getDouble("pay.amount");
        double actualBillAmount = response.jsonPath().getDouble("bill.amount");
        assertThat("Pay amount for " + tid, actualPayAmount, closeTo(data.getExpectedTotalPay(), AMOUNT_TOLERANCE));
        assertThat("Bill amount for " + tid, actualBillAmount, closeTo(data.getExpectedTotalBill(), AMOUNT_TOLERANCE));
    }

    // ========================== Overtime Bifurcation Assertions (PAY-748) ==========================

    /**
     * Verifies pay.regular_amount/pay.overtime_amount and bill.regular_amount/bill.overtime_amount
     * are present and reconcile to the existing pay.amount/bill.amount totals.
     */
    protected void assertAmountSplit(Response response, TestScenarioData data) {
        String tid = data.getTestId();

        Double payAmount = response.jsonPath().getDouble("pay.amount");
        Double payRegular = response.jsonPath().getDouble("pay.regular_amount");
        Double payOvertime = response.jsonPath().getDouble("pay.overtime_amount");
        assertThat("pay.regular_amount present (" + tid + ")", payRegular, notNullValue());
        assertThat("pay.overtime_amount present (" + tid + ")", payOvertime, notNullValue());
        assertThat("pay.regular_amount + pay.overtime_amount == pay.amount (" + tid + ")",
                payRegular + payOvertime, closeTo(payAmount, AMOUNT_TOLERANCE));

        Double billAmount = response.jsonPath().getDouble("bill.amount");
        Double billRegular = response.jsonPath().getDouble("bill.regular_amount");
        Double billOvertime = response.jsonPath().getDouble("bill.overtime_amount");
        assertThat("bill.regular_amount present (" + tid + ")", billRegular, notNullValue());
        assertThat("bill.overtime_amount present (" + tid + ")", billOvertime, notNullValue());
        assertThat("bill.regular_amount + bill.overtime_amount == bill.amount (" + tid + ")",
                billRegular + billOvertime, closeTo(billAmount, AMOUNT_TOLERANCE));

        boolean hasAnyOvertime = hasOvertimeSomewhere(data);
        if (!hasAnyOvertime) {
            assertThat("pay.overtime_amount is 0 when no overtime exists (" + tid + ")",
                    payOvertime, closeTo(0.0, AMOUNT_TOLERANCE));
            assertThat("bill.overtime_amount is 0 when no overtime exists (" + tid + ")",
                    billOvertime, closeTo(0.0, AMOUNT_TOLERANCE));
            assertThat("pay.regular_amount == pay.amount when no overtime exists (" + tid + ")",
                    payRegular, closeTo(payAmount, AMOUNT_TOLERANCE));
            assertThat("bill.regular_amount == bill.amount when no overtime exists (" + tid + ")",
                    billRegular, closeTo(billAmount, AMOUNT_TOLERANCE));
        }
    }

    /**
     * Verifies hours.weekly_overtime replaces the removed hours.total_weekly_overtime field —
     * either as the zero-state object (no weekly OT for this scenario) or a populated one.
     */
    protected void assertWeeklyOvertimeObject(Response response, TestScenarioData data) {
        String tid = data.getTestId();

        assertThat("hours.total_weekly_overtime should no longer be returned (" + tid + ")",
                response.jsonPath().get("hours.total_weekly_overtime"), nullValue());
        assertThat("hours.weekly_overtime present (" + tid + ")",
                response.jsonPath().get("hours.weekly_overtime"), notNullValue());

        Map<String, Double> weeklyOtMap = parseWeeklyOvertimeHours(data.getWeeklyOvertimeHours());
        int weeklyOvertimeSecs = (weeklyOtMap != null) ? computeWeeklyOvertimeSeconds(weeklyOtMap) : 0;

        assertThat("hours.weekly_overtime.hours (" + tid + ")",
                response.jsonPath().getString("hours.weekly_overtime.hours"),
                is(formatSecondsToHoursMin(weeklyOvertimeSecs)));

        if (weeklyOvertimeSecs == 0) {
            assertThat("hours.weekly_overtime.pay_rate_multiplier is null when no weekly OT (" + tid + ")",
                    response.jsonPath().get("hours.weekly_overtime.pay_rate_multiplier"), nullValue());
            assertThat("hours.weekly_overtime.bill_rate_multiplier is null when no weekly OT (" + tid + ")",
                    response.jsonPath().get("hours.weekly_overtime.bill_rate_multiplier"), nullValue());
            assertThat("hours.weekly_overtime.pay_amount is 0 when no weekly OT (" + tid + ")",
                    response.jsonPath().getDouble("hours.weekly_overtime.pay_amount"), closeTo(0.0, AMOUNT_TOLERANCE));
            assertThat("hours.weekly_overtime.bill_amount is 0 when no weekly OT (" + tid + ")",
                    response.jsonPath().getDouble("hours.weekly_overtime.bill_amount"), closeTo(0.0, AMOUNT_TOLERANCE));
        } else {
            assertThat("hours.weekly_overtime.pay_rate_multiplier present (" + tid + ")",
                    response.jsonPath().get("hours.weekly_overtime.pay_rate_multiplier"), notNullValue());
            assertThat("hours.weekly_overtime.bill_rate_multiplier present (" + tid + ")",
                    response.jsonPath().get("hours.weekly_overtime.bill_rate_multiplier"), notNullValue());
            assertThat("hours.weekly_overtime.pay_amount > 0 when weekly OT exists (" + tid + ")",
                    response.jsonPath().getDouble("hours.weekly_overtime.pay_amount"), greaterThan(0.0));
            assertThat("hours.weekly_overtime.bill_amount > 0 when weekly OT exists (" + tid + ")",
                    response.jsonPath().getDouble("hours.weekly_overtime.bill_amount"), greaterThan(0.0));
        }
    }

    /**
     * Verifies the per-day daily_hours.overtime_details array: empty on no-overtime days,
     * populated (with required fields and hours summing to the day's overtime) otherwise.
     * Weekly overtime is excluded from this array by design (Analysis Q4) — not checked here.
     */
    protected void assertOvertimeDetailsForAllLogs(Response response, TestScenarioData data) {
        String tid = data.getTestId();
        Map<String, Map<String, Double>> overtimeMap = parseOvertimeHours(data.getOvertimeHours());

        List<Map<String, Object>> timeLogs = response.jsonPath().getList("time_logs");
        assertThat("time_logs should be present for " + tid, timeLogs, notNullValue());

        for (int i = 0; i < timeLogs.size(); i++) {
            int weekNumber = (i / 7) + 1;
            String weekKey = "week" + weekNumber;
            int dayOfWeek = (i % 7) + 1;
            String dayName = (dayOfWeek < DAY_ABBREVS.length) ? DAY_ABBREVS[dayOfWeek] : "mon";

            int overtimeSecs = getExpectedOvertimeSeconds(overtimeMap, weekKey, dayName);
            String basePath = "time_logs[" + i + "].daily_hours.overtime_details";

            List<Map<String, Object>> overtimeDetails = response.jsonPath().getList(basePath);
            assertThat(dayName + " overtime_details present (" + tid + ")", overtimeDetails, notNullValue());

            if (overtimeSecs == 0) {
                assertThat(dayName + " overtime_details empty when no daily overtime (" + tid + ")",
                        overtimeDetails, empty());
                continue;
            }

            assertThat(dayName + " overtime_details not empty when overtime exists (" + tid + ")",
                    overtimeDetails, not(empty()));

            int summedEntrySeconds = 0;
            for (int e = 0; e < overtimeDetails.size(); e++) {
                String entryPrefix = basePath + "[" + e + "].";
                assertThat(dayName + " overtime_details[" + e + "].overtime_type (" + tid + ")",
                        response.jsonPath().get(entryPrefix + "overtime_type"), notNullValue());
                assertThat(dayName + " overtime_details[" + e + "].rule_name (" + tid + ")",
                        response.jsonPath().get(entryPrefix + "rule_name"), notNullValue());
                String entryHours = response.jsonPath().getString(entryPrefix + "hours");
                assertThat(dayName + " overtime_details[" + e + "].hours (" + tid + ")", entryHours, notNullValue());
                assertThat(dayName + " overtime_details[" + e + "].pay_rate_multiplier (" + tid + ")",
                        response.jsonPath().get(entryPrefix + "pay_rate_multiplier"), notNullValue());
                assertThat(dayName + " overtime_details[" + e + "].bill_rate_multiplier (" + tid + ")",
                        response.jsonPath().get(entryPrefix + "bill_rate_multiplier"), notNullValue());
                assertThat(dayName + " overtime_details[" + e + "].pay_amount (" + tid + ")",
                        response.jsonPath().get(entryPrefix + "pay_amount"), notNullValue());
                assertThat(dayName + " overtime_details[" + e + "].bill_amount (" + tid + ")",
                        response.jsonPath().get(entryPrefix + "bill_amount"), notNullValue());

                summedEntrySeconds += parseHoursMinToSeconds(entryHours);
            }

            assertThat(dayName + " overtime_details entries sum to daily_hours.overtime (" + tid + ")",
                    summedEntrySeconds, is(overtimeSecs));
        }
    }

    private boolean hasOvertimeSomewhere(TestScenarioData data) {
        Map<String, Map<String, Double>> overtimeMap = parseOvertimeHours(data.getOvertimeHours());
        Map<String, Double> weeklyOtMap = parseWeeklyOvertimeHours(data.getWeeklyOvertimeHours());
        boolean hasDailyOt = overtimeMap != null && !overtimeMap.isEmpty()
                && overtimeMap.values().stream().anyMatch(week -> !week.isEmpty());
        boolean hasWeeklyOt = weeklyOtMap != null && !weeklyOtMap.isEmpty();
        return hasDailyOt || hasWeeklyOt;
    }

    private int parseHoursMinToSeconds(String hoursMin) {
        // Format: "Xh Ymin"
        String[] parts = hoursMin.replace("min", "").split("h");
        int hours = Integer.parseInt(parts[0].trim());
        int minutes = (parts.length > 1 && !parts[1].trim().isEmpty()) ? Integer.parseInt(parts[1].trim()) : 0;
        return (hours * 3600) + (minutes * 60);
    }

    // ========================== Overtime, Regular & Total Calculation
    // ==========================

    protected int getExpectedOvertimeSeconds(Map<String, Map<String, Double>> overtimeMap,
            String weekKey, String dayName) {
        if (overtimeMap == null || overtimeMap.isEmpty()) {
            return 0;
        }
        Map<String, Double> weekOt = overtimeMap.get(weekKey);
        if (weekOt == null) {
            return 0;
        }
        Double hours = weekOt.get(dayName.toLowerCase());
        return (hours != null) ? (int) (hours * SECONDS_IN_HOUR) : 0;
    }

    protected int computeWeeklyOvertimeSeconds(Map<String, Double> weeklyOtMap) {
        int total = 0;
        for (Double hours : weeklyOtMap.values()) {
            total += (int) (hours * SECONDS_IN_HOUR);
        }
        return total;
    }

    protected int computeTotalWorkSeconds(List<WorkTimeEntry> dayWorkEntries, WeekWorkData weekBreakData,
            String dayName, boolean isBillableNo) {
        int totalWork = 0;
        int totalBreak = 0;
        for (WorkTimeEntry entry : dayWorkEntries) {
            totalWork += (entry.getWorkEndTime() - entry.getWorkStartTime());
            if (weekBreakData != null) {
                for (BreakEntry brk : weekBreakData.getBreaksForWorkEntry(dayName, entry)) {
                    totalBreak += brk.getDuration();
                }
            }
        }
        if (isBillableNo) {
            totalWork -= totalBreak;
        }
        return Math.max(0, totalWork);
    }

    protected int getExpectedRegularSeconds(Map<String, Map<String, double[]>> payBillMap,
            String weekKey, String dayName, double payRate) {
        if (payBillMap == null || payBillMap.isEmpty()) {
            return 0;
        }
        Map<String, double[]> dayRules = payBillMap.get(weekKey.toLowerCase() + ":" + dayName.toLowerCase());
        if (dayRules == null) {
            dayRules = payBillMap.get(dayName.toLowerCase());
        }
        if (dayRules == null) {
            return 0;
        }
        double regularPayAmount = 0;
        double[] regArr = dayRules.get("regularHours");
        if (regArr != null && regArr.length > 0) {
            regularPayAmount += regArr[0];
        }
        double[] unallocArr = dayRules.get("unallocatedHours");
        if (unallocArr != null && unallocArr.length > 0) {
            regularPayAmount += unallocArr[0];
        }
        double actualHours = (payRate != 0) ? regularPayAmount / payRate : regularPayAmount;
        return (int) (actualHours * SECONDS_IN_HOUR);
    }

    // ========================== Daily Hours Assertions (without work_time_details)
    // ==========================

    protected void assertDailyHoursForAllLogs(Response response, TestScenarioData data) {
        String tid = data.getTestId();
        double payRate = data.getPayRate();
        Map<String, WeekWorkData> workTimesByWeek = parseMultiEntryWorkTimes(data.getActualWorkTime());
        Map<String, WeekWorkData> breakTimesByWeek = parseMultiEntryBreakTimes(data.getBreakTime());
        Map<String, Map<String, Double>> overtimeMap = parseOvertimeHours(data.getOvertimeHours());
        Map<String, Double> weeklyOtMap = parseWeeklyOvertimeHours(data.getWeeklyOvertimeHours());
        Map<String, Map<String, double[]>> payBillMap = parseAcceptedPayBillRate(data.getAcceptedPayBillRate());
        boolean isBillableNo = data.getBreakBillable() != null
                && data.getBreakBillable().equalsIgnoreCase("No");

        List<Map<String, Object>> timeLogs = response.jsonPath().getList("time_logs");
        assertThat("time_logs should be present for " + tid, timeLogs, notNullValue());

        int expectedTotalRegularSecs = 0;
        int expectedTotalOvertimeSecs = 0;
        int expectedTotalSecs = 0;

        for (int i = 0; i < timeLogs.size(); i++) {
            int weekNumber = (i / 7) + 1;
            String weekKey = "week" + weekNumber;
            int dayOfWeek = (i % 7) + 1;
            String dayName = (dayOfWeek < DAY_ABBREVS.length) ? DAY_ABBREVS[dayOfWeek] : "mon";

            WeekWorkData weekWorkData = workTimesByWeek.get(weekKey);
            WeekWorkData weekBreakData = breakTimesByWeek != null ? breakTimesByWeek.get(weekKey) : null;

            List<WorkTimeEntry> dayWorkEntries = (weekWorkData != null)
                    ? weekWorkData.getWorkEntriesForDay(dayName)
                    : new ArrayList<>();

            String prefix = "time_logs[" + i + "].daily_hours.";

            if (dayWorkEntries.isEmpty()) {
                assertNoWorkDayHours(response, prefix, dayName, tid);
                continue;
            }

            int regularSecs = getExpectedRegularSeconds(payBillMap, weekKey, dayName, payRate);
            int overtimeSecs = getExpectedOvertimeSeconds(overtimeMap, weekKey, dayName);
            int totalSecs = computeTotalWorkSeconds(dayWorkEntries, weekBreakData, dayName, isBillableNo);

            DayBreakdown bd = new DayBreakdown(regularSecs, overtimeSecs, totalSecs);
            expectedTotalRegularSecs += bd.regular;
            expectedTotalOvertimeSecs += bd.overtime;
            expectedTotalSecs += bd.total;

            assertDayHoursValues(response, prefix, dayName, tid, bd);
        }

        int weeklyOvertimeSecs = computeWeeklyOvertimeSeconds(weeklyOtMap);
        expectedTotalOvertimeSecs += weeklyOvertimeSecs;

        assertAggregatedHours(response, tid, expectedTotalRegularSecs, expectedTotalOvertimeSecs, expectedTotalSecs);
    }

    // ========================== Daily Hours + Work Time Details Assertions
    // ==========================

    protected void assertDailyHoursWithWorkTimeDetails(Response response, TestScenarioData data) {
        String tid = data.getTestId();
        double payRate = data.getPayRate();
        Map<String, WeekWorkData> workTimesByWeek = parseMultiEntryWorkTimes(data.getActualWorkTime());
        Map<String, WeekWorkData> breakTimesByWeek = parseMultiEntryBreakTimes(data.getBreakTime());
        Map<String, Map<String, Double>> overtimeMap = parseOvertimeHours(data.getOvertimeHours());
        Map<String, Double> weeklyOtMap = parseWeeklyOvertimeHours(data.getWeeklyOvertimeHours());
        Map<String, Map<String, double[]>> payBillMap = parseAcceptedPayBillRate(data.getAcceptedPayBillRate());
        boolean isBillableNo = data.getBreakBillable() != null
                && data.getBreakBillable().equalsIgnoreCase("No");

        List<Map<String, Object>> timeLogs = response.jsonPath().getList("time_logs");
        assertThat("time_logs should be present for " + tid, timeLogs, notNullValue());

        int expectedTotalRegularSecs = 0;
        int expectedTotalOvertimeSecs = 0;
        int expectedTotalSecs = 0;

        for (int i = 0; i < timeLogs.size(); i++) {
            int weekNumber = (i / 7) + 1;
            String weekKey = "week" + weekNumber;
            int dayOfWeek = (i % 7) + 1;
            String dayName = (dayOfWeek < DAY_ABBREVS.length) ? DAY_ABBREVS[dayOfWeek] : "mon";

            WeekWorkData weekWorkData = workTimesByWeek.get(weekKey);
            WeekWorkData weekBreakData = breakTimesByWeek != null ? breakTimesByWeek.get(weekKey) : null;

            List<WorkTimeEntry> dayWorkEntries = (weekWorkData != null)
                    ? weekWorkData.getWorkEntriesForDay(dayName)
                    : new ArrayList<>();

            String logPrefix = "time_logs[" + i + "]";
            String hoursPrefix = logPrefix + ".daily_hours.";

            List<Map<String, Object>> workTimeDetails = response.jsonPath().getList(logPrefix + ".work_time_details");
            assertThat("work_time_details present for " + dayName + " (" + tid + ")", workTimeDetails, notNullValue());

            if (dayWorkEntries.isEmpty()) {
                assertNoWorkDayHours(response, hoursPrefix, dayName, tid);
                assertEmptyWorkTimeDetails(response, logPrefix, dayName, tid);
                continue;
            }

            int regularSecs = getExpectedRegularSeconds(payBillMap, weekKey, dayName, payRate);
            int overtimeSecs = getExpectedOvertimeSeconds(overtimeMap, weekKey, dayName);
            int totalSecs = computeTotalWorkSeconds(dayWorkEntries, weekBreakData, dayName, isBillableNo);

            DayBreakdown bd = new DayBreakdown(regularSecs, overtimeSecs, totalSecs);
            expectedTotalRegularSecs += bd.regular;
            expectedTotalOvertimeSecs += bd.overtime;
            expectedTotalSecs += bd.total;

            assertDayHoursValues(response, hoursPrefix, dayName, tid, bd);
            assertWorkTimeDetailsForDay(response, logPrefix, dayName, tid, dayWorkEntries, weekBreakData,
                    data.getComment());
        }

        int weeklyOvertimeSecs = computeWeeklyOvertimeSeconds(weeklyOtMap);
        expectedTotalOvertimeSecs += weeklyOvertimeSecs;

        assertAggregatedHours(response, tid, expectedTotalRegularSecs, expectedTotalOvertimeSecs, expectedTotalSecs);
    }

    // ========================== Granular Assertion Helpers
    // ==========================

    protected void assertNoWorkDayHours(Response response, String prefix, String dayName, String tid) {
        assertThat(dayName + " regular (" + tid + ")", response.jsonPath().getString(prefix + "regular"), is(NO_HOURS));
        assertThat(dayName + " overtime (" + tid + ")", response.jsonPath().getString(prefix + "overtime"),
                is(NO_HOURS));
        // assertThat(dayName + " total (" + tid + ")",
        // response.jsonPath().getString(prefix + "total"), is(NO_HOURS));
    }

    protected void assertDayHoursValues(Response response, String prefix, String dayName, String tid, DayBreakdown bd) {
        assertThat(dayName + " regular (" + tid + ")", response.jsonPath().getString(prefix + "regular"),
                is(formatSecondsToHoursMin(bd.regular)));
        assertThat(dayName + " overtime (" + tid + ")", response.jsonPath().getString(prefix + "overtime"),
                is(formatSecondsToHoursMin(bd.overtime)));
        // assertThat(dayName + " total (" + tid + ")",
        // response.jsonPath().getString(prefix + "total"),
        // is(formatSecondsToHoursMin(bd.total)));
    }

    protected void assertAggregatedHours(Response response, String tid,
            int expectedRegular, int expectedOvertime, int expectedTotal) {
        assertThat("Total regular hours (" + tid + ")", response.jsonPath().getString("hours.total_regular"),
                is(formatSecondsToHoursMin(expectedRegular)));
        assertThat("Total overtime hours (" + tid + ")", response.jsonPath().getString("hours.total_overtime"),
                is(formatSecondsToHoursMin(expectedOvertime)));
        // assertThat("Total hours (" + tid + ")",
        // response.jsonPath().getString("hours.total"),
        // is(formatSecondsToHoursMin(expectedTotal)));
    }

    protected void assertEmptyWorkTimeDetails(Response response, String logPrefix, String dayName, String tid) {
        List<Map<String, Object>> wtd = response.jsonPath().getList(logPrefix + ".work_time_details");
        assertThat(dayName + " work_time_details size (" + tid + ")", wtd.size(), is(1));

        String dtlPrefix = logPrefix + ".work_time_details[0].";
        assertThat(dayName + " wtd[0].id (" + tid + ")", response.jsonPath().get(dtlPrefix + "id"), notNullValue());
        assertThat(dayName + " wtd[0].time.start (" + tid + ")", response.jsonPath().get(dtlPrefix + "time.start"),
                nullValue());
        assertThat(dayName + " wtd[0].time.end (" + tid + ")", response.jsonPath().get(dtlPrefix + "time.end"),
                nullValue());
        assertThat(dayName + " wtd[0].remark (" + tid + ")", response.jsonPath().get(dtlPrefix + "remark"),
                nullValue());
        assertThat(dayName + " wtd[0].break_time (" + tid + ")", response.jsonPath().get(dtlPrefix + "break_time"),
                nullValue());
        assertThat(dayName + " wtd[0].break_intervals (" + tid + ")",
                response.jsonPath().getList(dtlPrefix + "break_intervals"), empty());
    }

    protected void assertWorkTimeDetailsForDay(Response response, String logPrefix, String dayName, String tid,
            List<WorkTimeEntry> dayWorkEntries, WeekWorkData weekBreakData,
            String comment) {
        List<Map<String, Object>> wtd = response.jsonPath().getList(logPrefix + ".work_time_details");
        assertThat(dayName + " work_time_details size (" + tid + ")", wtd.size(), is(dayWorkEntries.size()));

        for (int w = 0; w < dayWorkEntries.size(); w++) {
            WorkTimeEntry workEntry = dayWorkEntries.get(w);
            String dtlPrefix = logPrefix + ".work_time_details[" + w + "].";

            assertThat(dayName + " wtd[" + w + "].id (" + tid + ")", response.jsonPath().get(dtlPrefix + "id"),
                    notNullValue());

            assertThat(dayName + " wtd[" + w + "].time.start (" + tid + ")",
                    response.jsonPath().getString(dtlPrefix + "time.start"),
                    is(formatSecondsToTimeDisplay(workEntry.getWorkStartTime())));
            assertThat(dayName + " wtd[" + w + "].time.end (" + tid + ")",
                    response.jsonPath().getString(dtlPrefix + "time.end"),
                    is(formatSecondsToTimeDisplay(workEntry.getWorkEndTime())));

            if (comment != null) {
                assertThat(dayName + " wtd[" + w + "].remark (" + tid + ")",
                        response.jsonPath().getString(dtlPrefix + "remark"), is(comment));
            } else {
                assertThat(dayName + " wtd[" + w + "].remark (" + tid + ")",
                        response.jsonPath().get(dtlPrefix + "remark"), nullValue());
            }

            List<BreakEntry> entryBreaks = new ArrayList<>();
            if (weekBreakData != null) {
                entryBreaks = weekBreakData.getBreaksForWorkEntry(dayName, workEntry);
            }
            assertBreakTimeAndIntervals(response, dtlPrefix, dayName, w, tid, entryBreaks);
        }
    }

    protected void assertBreakTimeAndIntervals(Response response, String dtlPrefix, String dayName,
            int entryIdx, String tid, List<BreakEntry> entryBreaks) {
        int entryBreakSecs = 0;
        for (BreakEntry brk : entryBreaks) {
            entryBreakSecs += brk.getDuration();
        }
        assertThat(dayName + " wtd[" + entryIdx + "].break_time (" + tid + ")",
                response.jsonPath().getString(dtlPrefix + "break_time"),
                is(formatSecondsToHoursMin(entryBreakSecs)));

        List<Map<String, Object>> breakIntervals = response.jsonPath().getList(dtlPrefix + "break_intervals");

        if (entryBreaks.isEmpty()) {
            assertThat(dayName + " wtd[" + entryIdx + "].break_intervals (" + tid + ")", breakIntervals, empty());
        } else {
            assertThat(dayName + " wtd[" + entryIdx + "].break_intervals size (" + tid + ")",
                    breakIntervals.size(), is(entryBreaks.size()));

            for (int b = 0; b < entryBreaks.size(); b++) {
                BreakEntry brk = entryBreaks.get(b);
                String brkPrefix = dtlPrefix + "break_intervals[" + b + "].";

                assertThat(dayName + " wtd[" + entryIdx + "].brk[" + b + "].start_time (" + tid + ")",
                        response.jsonPath().getString(brkPrefix + "start_time"),
                        is(formatSecondsToTimeDisplay(brk.getBreakStartTime())));
                assertThat(dayName + " wtd[" + entryIdx + "].brk[" + b + "].end_time (" + tid + ")",
                        response.jsonPath().getString(brkPrefix + "end_time"),
                        is(formatSecondsToTimeDisplay(brk.getBreakEndTime())));
            }
        }
    }

    // ========================== Formatting ==========================

    protected static String formatSecondsToHoursMin(int totalSeconds) {
        int totalMinutes = (totalSeconds + (SECONDS_IN_MINUTE / 2)) / SECONDS_IN_MINUTE;
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return hours + "h " + minutes + "min";
    }

    protected static String formatSecondsToTimeDisplay(int totalSeconds) {
        int hours = totalSeconds / SECONDS_IN_HOUR;
        int minutes = (totalSeconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE;
        return hours + ":" + String.format("%02d", minutes);
    }

    // ========================== Value Objects ==========================

    protected static class DayBreakdown {
        public final int regular;
        public final int overtime;
        public final int total;

        public DayBreakdown(int regular, int overtime, int total) {
            this.regular = regular;
            this.overtime = overtime;
            this.total = total;
        }
    }

    // ========================== Data Provider Utility ==========================

    protected static Object[][] toProviderRows(List<Map<String, Object>> scenarios) {
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
