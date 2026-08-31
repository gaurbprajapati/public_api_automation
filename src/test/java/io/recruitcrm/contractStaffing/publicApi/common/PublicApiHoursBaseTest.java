package io.recruitcrm.contractStaffing.publicApi.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.contractStaffing.hoursBasedRuleEngineCalculation.RuleEngineCalculationBase;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared public API assertions and helpers for hours-based timesheets.
 * Mirrors {@link PublicApiBaseTest} (shift-based) but resolves work hours from
 * hours-based test data ({@code Mon: 8 hours}) via {@link RuleEngineCalculationBase}.
 */
public abstract class PublicApiHoursBaseTest extends RuleEngineCalculationBase {

    protected static final String[] DAY_ABBREVS = { "", "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
    protected static final String NO_HOURS = "0h 0min";
    private static final String START_TIME = "startTime";
    private static final String END_TIME = "endTime";
    private static final Pattern WEEK_BREAK_PATTERN = Pattern.compile("(?i)Week(\\d+)\\s*:\\s*\\[(.+?)\\]");
    private static final Pattern DAY_BREAK_PATTERN = Pattern.compile(
            "(?i)(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s*:\\s*([\\d.]+)\\s*hours?");
    private static final Pattern CLOCK_BREAK_PATTERN = Pattern.compile("\\d{1,2}:\\d{2}");

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
            assertThat("pay.details.paid_on (" + tid + ")", response.jsonPath().get("pay.details.paid_on"), nullValue());
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

    // ========================== Hours work-time resolution ==========================

    /**
     * Resolves expected work seconds for a day from hours-based actualWorkTime test data.
     */
    protected int getHoursWorkSecondsForDay(String weekKey, String dayName,
            Map<String, Map<String, Map<String, Integer>>> multiWeekDetailed,
            Map<String, Map<String, Integer>> singleWeekPerDay) {
        Map<String, Integer> dayTimes = null;
        if (multiWeekDetailed != null && !multiWeekDetailed.isEmpty()) {
            Map<String, Map<String, Integer>> week = multiWeekDetailed.get(weekKey.toLowerCase());
            if (week == null) {
                week = multiWeekDetailed.get(weekKey);
            }
            if (week != null) {
                dayTimes = week.get(dayName.toLowerCase());
            }
        }
        if ((dayTimes == null || dayTimes.isEmpty()) && singleWeekPerDay != null) {
            dayTimes = singleWeekPerDay.get(dayName.toLowerCase());
        }
        if (dayTimes == null || dayTimes.isEmpty()) {
            return 0;
        }
        Integer start = dayTimes.get(START_TIME);
        Integer end = dayTimes.get(END_TIME);
        if (start != null && end != null && end >= start) {
            return end - start;
        }
        return 0;
    }

    protected boolean hasWorkOnDay(String weekKey, String dayName,
            Map<String, Map<String, Map<String, Integer>>> multiWeekDetailed,
            Map<String, Map<String, Integer>> singleWeekPerDay) {
        return getHoursWorkSecondsForDay(weekKey, dayName, multiWeekDetailed, singleWeekPerDay) > 0;
    }

    // ========================== Overtime & regular calculation ==========================

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

    // ========================== Daily hours assertions ==========================

    protected void assertDailyHoursForAllLogs(Response response, TestScenarioData data) {
        String tid = data.getTestId();
        double payRate = data.getPayRate();
        Map<String, Map<String, Map<String, Integer>>> multiWeekDetailed =
                parseMultiWeekDetailedWorkTimes(data.getActualWorkTime());
        Map<String, Map<String, Integer>> singleWeekPerDay =
                parsePerDayWorkTimes(data.getActualWorkTime());
        Map<String, Map<String, Double>> overtimeMap = parseOvertimeHours(data.getOvertimeHours());
        Map<String, Double> weeklyOtMap = parseWeeklyOvertimeHours(data.getWeeklyOvertimeHours());
        Map<String, Map<String, double[]>> payBillMap = parseAcceptedPayBillRate(data.getAcceptedPayBillRate());

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

            String prefix = "time_logs[" + i + "].daily_hours.";

            if (!hasWorkOnDay(weekKey, dayName, multiWeekDetailed, singleWeekPerDay)) {
                assertNoWorkDayHours(response, prefix, dayName, tid);
                continue;
            }

            int regularSecs = getExpectedRegularSeconds(payBillMap, weekKey, dayName, payRate);
            int overtimeSecs = getExpectedOvertimeSeconds(overtimeMap, weekKey, dayName);
            int totalSecs = getHoursWorkSecondsForDay(weekKey, dayName, multiWeekDetailed, singleWeekPerDay);

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

    protected void assertDailyHoursWithWorkTimeDetails(Response response, TestScenarioData data) {
        String tid = data.getTestId();
        double payRate = data.getPayRate();
        Map<String, Map<String, Map<String, Integer>>> multiWeekDetailed =
                parseMultiWeekDetailedWorkTimes(data.getActualWorkTime());
        Map<String, Map<String, Integer>> singleWeekPerDay =
                parsePerDayWorkTimes(data.getActualWorkTime());
        Map<String, Map<String, Double>> overtimeMap = parseOvertimeHours(data.getOvertimeHours());
        Map<String, Double> weeklyOtMap = parseWeeklyOvertimeHours(data.getWeeklyOvertimeHours());
        Map<String, Map<String, double[]>> payBillMap = parseAcceptedPayBillRate(data.getAcceptedPayBillRate());

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

            String logPrefix = "time_logs[" + i + "]";
            String hoursPrefix = logPrefix + ".daily_hours.";

            List<Map<String, Object>> workTimeDetails = response.jsonPath().getList(logPrefix + ".work_time_details");
            assertThat("work_time_details present for " + dayName + " (" + tid + ")", workTimeDetails, notNullValue());

            if (!hasWorkOnDay(weekKey, dayName, multiWeekDetailed, singleWeekPerDay)) {
                assertNoWorkDayHours(response, hoursPrefix, dayName, tid);
                assertEmptyWorkTimeDetails(response, logPrefix, dayName, tid, data.getComment(),
                        data.getBreakTime(), weekKey);
                continue;
            }

            int regularSecs = getExpectedRegularSeconds(payBillMap, weekKey, dayName, payRate);
            int overtimeSecs = getExpectedOvertimeSeconds(overtimeMap, weekKey, dayName);
            int totalSecs = getHoursWorkSecondsForDay(weekKey, dayName, multiWeekDetailed, singleWeekPerDay);

            DayBreakdown bd = new DayBreakdown(regularSecs, overtimeSecs, totalSecs);
            expectedTotalRegularSecs += bd.regular;
            expectedTotalOvertimeSecs += bd.overtime;
            expectedTotalSecs += bd.total;

            assertDayHoursValues(response, hoursPrefix, dayName, tid, bd);
            assertHoursWorkTimeDetails(response, logPrefix, dayName, tid, data.getComment(),
                    data.getBreakTime(), weekKey);
        }

        int weeklyOvertimeSecs = computeWeeklyOvertimeSeconds(weeklyOtMap);
        expectedTotalOvertimeSecs += weeklyOvertimeSecs;

        assertAggregatedHours(response, tid, expectedTotalRegularSecs, expectedTotalOvertimeSecs, expectedTotalSecs);
    }

    // ========================== Granular assertion helpers ==========================

    protected void assertNoWorkDayHours(Response response, String prefix, String dayName, String tid) {
        assertThat(dayName + " regular (" + tid + ")", response.jsonPath().getString(prefix + "regular"), is(NO_HOURS));
        assertThat(dayName + " overtime (" + tid + ")", response.jsonPath().getString(prefix + "overtime"), is(NO_HOURS));
    }

    protected void assertDayHoursValues(Response response, String prefix, String dayName, String tid, DayBreakdown bd) {
        assertThat(dayName + " regular (" + tid + ")", response.jsonPath().getString(prefix + "regular"),
                is(formatSecondsToHoursMin(bd.regular)));
        assertThat(dayName + " overtime (" + tid + ")", response.jsonPath().getString(prefix + "overtime"),
                is(formatSecondsToHoursMin(bd.overtime)));
    }

    protected void assertAggregatedHours(Response response, String tid,
            int expectedRegular, int expectedOvertime, int expectedTotal) {
        assertThat("Total regular hours (" + tid + ")", response.jsonPath().getString("hours.total_regular"),
                is(formatSecondsToHoursMin(expectedRegular)));
        assertThat("Total overtime hours (" + tid + ")", response.jsonPath().getString("hours.total_overtime"),
                is(formatSecondsToHoursMin(expectedOvertime)));
    }

    protected void assertEmptyWorkTimeDetails(Response response, String logPrefix, String dayName,
            String tid, String comment, String breakTime, String weekKey) {
        List<Map<String, Object>> wtd = response.jsonPath().getList(logPrefix + ".work_time_details");
        assertThat(dayName + " work_time_details size (" + tid + ")", wtd.size(), is(1));

        String dtlPrefix = logPrefix + ".work_time_details[0].";
        assertThat(dayName + " wtd[0].id (" + tid + ")", response.jsonPath().get(dtlPrefix + "id"), notNullValue());
        assertThat(dayName + " wtd[0].time.start (" + tid + ")", response.jsonPath().get(dtlPrefix + "time.start"),
                nullValue());
        assertThat(dayName + " wtd[0].time.end (" + tid + ")", response.jsonPath().get(dtlPrefix + "time.end"),
                nullValue());
        if (comment != null && !comment.isEmpty()) {
            assertThat(dayName + " wtd[0].remark (" + tid + ")",
                    response.jsonPath().getString(dtlPrefix + "remark"), is(comment));
        }
        assertThat(dayName + " wtd[0].break_time (" + tid + ")",
                response.jsonPath().getString(dtlPrefix + "break_time"),
                is(getExpectedBreakTimeDisplay(breakTime, weekKey, dayName)));
    }

    /**
     * Hours-based entries: assert work_time_details structure without shift clock times.
     */
    protected void assertHoursWorkTimeDetails(Response response, String logPrefix, String dayName,
            String tid, String comment, String breakTime, String weekKey) {
        List<Map<String, Object>> wtd = response.jsonPath().getList(logPrefix + ".work_time_details");
        assertThat(dayName + " work_time_details size (" + tid + ")", wtd.size(), greaterThan(0));

        String dtlPrefix = logPrefix + ".work_time_details[0].";
        assertThat(dayName + " wtd[0].id (" + tid + ")", response.jsonPath().get(dtlPrefix + "id"), notNullValue());

        if (comment != null && !comment.isEmpty()) {
            assertThat(dayName + " wtd[0].remark (" + tid + ")",
                    response.jsonPath().getString(dtlPrefix + "remark"), is(comment));
        }
        assertThat(dayName + " wtd[0].break_time (" + tid + ")",
                response.jsonPath().getString(dtlPrefix + "break_time"),
                is(getExpectedBreakTimeDisplay(breakTime, weekKey, dayName)));
    }

    protected String getExpectedBreakTimeDisplay(String breakTime, String weekKey, String dayName) {
        return formatSecondsToHoursMin(getExpectedBreakSeconds(breakTime, weekKey, dayName));
    }

    protected int getExpectedBreakSeconds(String breakTime, String weekKey, String dayName) {
        if (breakTime == null || breakTime.trim().isEmpty() || breakTime.equalsIgnoreCase("None")) {
            return 0;
        }

        String trimmed = breakTime.trim();
        if (CLOCK_BREAK_PATTERN.matcher(trimmed).matches()) {
            String[] parts = trimmed.split(":");
            return Integer.parseInt(parts[0]) * SECONDS_IN_HOUR + Integer.parseInt(parts[1]) * SECONDS_IN_MINUTE;
        }

        String expectedWeek = weekKey == null ? "" : weekKey.replaceAll("\\D", "");
        Matcher weekMatcher = WEEK_BREAK_PATTERN.matcher(trimmed);
        while (weekMatcher.find()) {
            if (!expectedWeek.isEmpty() && !weekMatcher.group(1).equals(expectedWeek)) {
                continue;
            }
            Matcher dayMatcher = DAY_BREAK_PATTERN.matcher(weekMatcher.group(2));
            while (dayMatcher.find()) {
                String breakDay = dayMatcher.group(1).toLowerCase().substring(0, 3);
                if (breakDay.equals(dayName.toLowerCase())) {
                    double hours = Double.parseDouble(dayMatcher.group(2));
                    return (int) Math.round(hours * SECONDS_IN_HOUR);
                }
            }
        }
        return 0;
    }

    // ========================== Formatting ==========================

    protected static String formatSecondsToHoursMin(int totalSeconds) {
        int totalMinutes = (totalSeconds + (SECONDS_IN_MINUTE / 2)) / SECONDS_IN_MINUTE;
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return hours + "h " + minutes + "min";
    }

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

    protected static Object[][] toProviderRows(List<Map<String, Object>> scenarios) {
        Object[][] data = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) {
            data[i][0] = scenarios.get(i);
        }
        return data;
    }
}
