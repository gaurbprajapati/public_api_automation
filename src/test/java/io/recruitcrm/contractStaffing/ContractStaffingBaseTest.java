package io.recruitcrm.contractStaffing;

import io.rcrm.api.pojo.albatross.contractStaffing.*;
import io.rcrm.api.pojo.invoiceService.TimesheetsInvoiceDataRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;

import java.math.BigDecimal;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.json.*;

import com.qa.api.util.reaper.ReaperIntegration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ContractStaffingBaseTest extends TestBase {

        protected commanFunction function = new commanFunction();

        protected String timesheetBaseURL = "https://" + System.getProperty("envname")
                        + "contract-staffing-timesheet.recruitcrm.net/v1";
        
        protected String invoiceBaseURL = "https://" + System.getProperty("envname")
                        + "invoice.recruitcrm.net/v2";

        /** 12:00 AM (midnight), seconds from midnight. */
        protected static final int WORK_DAY_GRID_START_SECONDS = 0;
        /** 11:00 PM, seconds from midnight — inclusive upper bound for the overall “12am–11pm” work day. */
        protected static final int WORK_DAY_END_11PM_SECONDS = 23 * 3600;
        /** Each grid slot is two hours; up to eleven slots cover midnight through 10:00 PM (79200s). */
        protected static final int TWO_HOUR_SLOT_SECONDS = 2 * 3600;
        protected static final int MAX_TWO_HOUR_INTERVALS = 11;
        protected static final int MAX_BREAKS_PER_TWO_HOUR_SLOT = 6;
        protected static final int ADAPTIVE_BREAK_MIN_GAP_SECONDS = 600;

        /** Default job start / free-slots range start (Unix epoch seconds, UTC). */
        public static final int DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH = 1751328000;
        /** Default job end / free-slots range end (Unix epoch seconds, UTC). */
        public static final int DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH = 1759017600;
        /** String bounds for {@link #getTimeSheetFreeSlots} and similar APIs. */
        public static final String DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START =
                        Integer.toString(DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH);
        public static final String DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END =
                        Integer.toString(DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH);

        public Response EnablePortal(int contractorId, String firstName, String lastName, String contractorEmail, int recruiterUserId, String recruiterName, String authToken) {
        
                UpdatePortalStatusRequest request = UpdatePortalStatusRequest.builder()
                        .contractorId(contractorId)
                        .firstName(firstName)
                        .lastName(lastName)
                        .contractorEmail(contractorEmail)
                        .recruiterUserId(recruiterUserId)
                        .portalStatus(1)
                        .recruiterName(recruiterName)
                        .build();
                Response response = RestClient.doPost("JSON", albatrossURL, "contract-staffing/contractor/update-portal-status", authToken, null, true, request);
                assertThat("Portal status should be updated successfully", response.getStatusCode(), is(200));
                return response;
        }

        public Response createRuleEngineTemplate(String authToken) {
                // Generate template name with timestamp and random string
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String randomString = generateRandomString(3);
                String templateName = "Rule All work start and end time " + timestamp + "_" + randomString;

                // Create RuleEngineTemplate using setters
                RuleEngineTemplate ruleEngineTemplate = new RuleEngineTemplate();
                ruleEngineTemplate.setTemplateName(templateName);
                ruleEngineTemplate.setWorkLogType(2);
                // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
                ruleEngineTemplate.setCalculateBreakTime(0);
                ruleEngineTemplate.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5, 6));
                ruleEngineTemplate.setWorkTime(Arrays.asList(0, 0, 0, 0, 0, 0));
                ruleEngineTemplate.setWorkStartTime(Arrays.asList(32400, 32400, 32400, 32400, 32400, 32400));
                ruleEngineTemplate.setWorkEndTime(Arrays.asList(61200, 61200, 61200, 61200, 61200, 61200));
                ruleEngineTemplate.setCustomRules(createDefaultCustomRules());

                return RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/rule-template",
                                authToken, null, true, ruleEngineTemplate);
        }

        public Response createRuleEngineTemplateHourBased(String authToken) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String randomString = generateRandomString(3);
                String templateName = "All Rules Hour Based " + timestamp + "_" + randomString;

                RuleEngineTemplate ruleEngineTemplate = new RuleEngineTemplate();
                ruleEngineTemplate.setTemplateName(templateName);
                ruleEngineTemplate.setWorkLogType(1);
                // "Break Paid: Yes" option removed from the rule template — breaks are always unpaid/deducted now
                ruleEngineTemplate.setCalculateBreakTime(0);
                ruleEngineTemplate.setBreakTimeThreshold(0);
                ruleEngineTemplate.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5, 6));
                ruleEngineTemplate.setWorkTime(Arrays.asList(30600, 30600, 30600, 30600, 30600, 30600));
                ruleEngineTemplate.setWorkStartTime(Arrays.asList(0, 0, 0, 0, 0, 0));
                ruleEngineTemplate.setWorkEndTime(Arrays.asList(0, 0, 0, 0, 0, 0));
                ruleEngineTemplate.setCustomRules(createDefaultCustomRulesHourBased());

                return RestClient.doPost("JSON", timesheetBaseURL, "rule-engine/rule-template",
                                authToken, null, true, ruleEngineTemplate);
        }

        /**
         * Hour-based custom rules: Specific hours (ruleType 6), Weekly overtime (ruleType 8), Daily overtime (ruleType 7).
         */
        protected List<CustomRule> createDefaultCustomRulesHourBased() {
                CustomRule specificHoursRule = createCustomRule("Specific hours", Arrays.asList(1, 2, 3, 4, 5, 6), 6, 1,
                                0, 0, 28800, 30600, 0, 0, 1, 1, 0, 0);
                CustomRule weeklyOvertimeRule = createCustomRule("Weekly overtime", Arrays.asList(1, 2, 3, 4, 5, 6, 7), 8, 1,
                                0, 0, 0, 0, 0, 183600, 3, 3, 0, 0);
                CustomRule dailyOvertimeRule = createCustomRule("Daily overtime", Arrays.asList(1, 2, 3, 4, 5, 6), 7, 1,
                                0, 0, 0, 0, 32400, 0, 2, 2, 0, 0);

                return Arrays.asList(specificHoursRule, weeklyOvertimeRule, dailyOvertimeRule);
        }

        protected List<CustomRule> createDefaultTimesheetSettingsCustomRulesHourBased() {
                CustomRule weeklyOvertimeRule = createCustomRule("Weekly overtime",
                                Arrays.asList(1, 2, 3, 4, 5, 6, 7), 8, 1,
                                0, 0, 0, 0, 0, 183600, 2, 2, 0, 0);
                CustomRule dailyOvertimeRule = createCustomRule("Daily overtime", Arrays.asList(1, 2, 3, 4, 5, 6), 7, 1,
                                0, 0, 0, 0, 32400, 0, 2, 2, 0, 0);

                return Arrays.asList(weeklyOvertimeRule, dailyOvertimeRule);
        }

        protected List<CustomRule> createDefaultCustomRules() {
                CustomRule beforeShiftRule = createCustomRule("before shift", Arrays.asList(1, 2, 3, 4, 5, 6), 2, 1,
                                28800, 0,
                                0, 0, 0, 0, 2, 2, 0, 0);
                CustomRule afterShiftRule = createCustomRule("After shift", Arrays.asList(1, 2, 3, 4, 5, 6), 1, 1,
                                72000, 0, 0,
                                0, 0, 0, 2, 2, 0, 0);
                CustomRule specificRangesRule = createCustomRule("specific ranges", Arrays.asList(1, 2, 3, 4, 5, 6), 3,
                                1,
                                61200, 68400, 0, 0, 0, 0, 1, 1, 0, 0);
                CustomRule weeklyOvertimeRule = createCustomRule("weekly overtime ", Arrays.asList(1, 2, 3, 4, 5, 6, 7),
                                5, 1,
                                0, 0, 0, 0, 0, 172800, 3, 3, 0, 0);
                CustomRule dailyOvertimeRule = createCustomRule("Daily overtime", Arrays.asList(1, 2, 3, 4, 5, 6, 7), 4,
                                1, 0,
                                0, 0, 0, 32400, 0, 2, 2, 0, 0);

                return Arrays.asList(beforeShiftRule, afterShiftRule, specificRangesRule, weeklyOvertimeRule,
                                dailyOvertimeRule);
        }

        protected CustomRule createCustomRule(String ruleName, List<Integer> workDayId, int ruleType, int chargeMethod,
                        int startTime, int endTime, int startDuration, int endDuration, int dailyThreshold,
                        int weeklyThreshold,
                        int payRateMultiplier, int billRateMultiplier, int payRatePerHour, int billRatePerHour) {
                CustomRule rule = new CustomRule();
                rule.setId(0);
                rule.setRuleName(ruleName);
                rule.setWorkDayId(workDayId);
                rule.setRuleType(ruleType);
                rule.setChargeMethod(chargeMethod);
                rule.setStartTime(startTime);
                rule.setEndTime(endTime);
                rule.setStartDuration(startDuration);
                rule.setEndDuration(endDuration);
                rule.setDailyThreshold(dailyThreshold);
                rule.setWeeklyThreshold(weeklyThreshold);
                rule.setPayRateMultiplier(payRateMultiplier);
                rule.setBillRateMultiplier(billRateMultiplier);
                rule.setPayRatePerHour(payRatePerHour);
                rule.setBillRatePerHour(billRatePerHour);
                return rule;
        }

        protected TimesheetSettings createDefaultTimesheetSettings(int jobId, int candidateId, int agencyId,
                        int timesheetFrequency, int isReimbursementEnabled) {
                // Create Approvers
                Approvers approvers = new Approvers();
                approvers.setAgencyIds(Arrays.asList(agencyId));
                approvers.setClientIds(Arrays.asList());

                // Create TimesheetSettings
                TimesheetSettings timesheetSettings = new TimesheetSettings();
                timesheetSettings.setJobStartDate(DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH);
                timesheetSettings.setJobEndDate(DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH);
                timesheetSettings.setTimesheetFrequency(timesheetFrequency);
                timesheetSettings.setTimesheetStartDay(1);
                timesheetSettings.setApprovers(approvers);
                timesheetSettings.setPayCurrencyId(53);
                timesheetSettings.setPayRate(5000);
                timesheetSettings.setBillCurrencyId(53);
                timesheetSettings.setBillRate(6000);
                // calculateChargeBy/marginPercentage/markupPercentage are @NotNull on the API's DTO regardless of
                // mode — default every caller to Fixed Rate with 0/0 so existing enableTimesheet(...) callers
                // across the suite keep passing; tests that need Margin/Markup mode use the overload below.
                timesheetSettings.setCalculateChargeBy(1);
                timesheetSettings.setMarginPercentage(BigDecimal.ZERO);
                timesheetSettings.setMarkupPercentage(BigDecimal.ZERO);
                timesheetSettings.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5, 6));
                timesheetSettings.setWorkLogType(2);
                timesheetSettings.setCalculateBreakTime(false);
                timesheetSettings.setBreakTimeThreshold(0);
                timesheetSettings.setWorkTime(Arrays.asList(0, 0, 0, 0, 0, 0));
                timesheetSettings.setWorkStartTime(Arrays.asList(32400, 32400, 32400, 32400, 32400, 32400));
                timesheetSettings.setWorkEndTime(Arrays.asList(61200, 61200, 61200, 61200, 61200, 61200));
                timesheetSettings.setUpdatedOn(null);
                timesheetSettings.setUpdatedBy(null);
                timesheetSettings.setEnabledOn(null);
                timesheetSettings.setEnabledBy(null);
                timesheetSettings.setIsPreferencesModified(1);
                timesheetSettings.setJobId(jobId);
                timesheetSettings.setContractorIds(Arrays.asList(candidateId));
                timesheetSettings.setIsReimbursementEnabled(isReimbursementEnabled);
                timesheetSettings.setIsUnplannedHoursPayEnabled(0);
                timesheetSettings.setCustomRules(createDefaultCustomRules());

                return timesheetSettings;
        }

        /**
         * Creates default timesheet settings for hour-based logging (workLogType=1) with workTime in seconds,
         * workStartTime/workEndTime as 0, and hour-based custom rules. Matches timesheet-settings API for hour-based.
         */
        protected TimesheetSettings createDefaultTimesheetSettingsHourBased(int jobId, int candidateId, int agencyId,
                        int timesheetFrequency, int isReimbursementEnabled) {
                Approvers approvers = new Approvers();
                approvers.setAgencyIds(Arrays.asList(agencyId));
                approvers.setClientIds(Arrays.asList());

                TimesheetSettings timesheetSettings = new TimesheetSettings();
                timesheetSettings.setJobStartDate(DEFAULT_CONTRACT_STAFFING_JOB_START_DATE_EPOCH);
                timesheetSettings.setJobEndDate(DEFAULT_CONTRACT_STAFFING_JOB_END_DATE_EPOCH);
                timesheetSettings.setTimesheetFrequency(timesheetFrequency);
                timesheetSettings.setTimesheetStartDay(1);
                timesheetSettings.setApprovers(approvers);
                timesheetSettings.setPayCurrencyId(53);
                timesheetSettings.setPayRate(1);
                timesheetSettings.setBillCurrencyId(53);
                timesheetSettings.setBillRate(2);
                // See createDefaultTimesheetSettings — same @NotNull default-to-Fixed-Rate rationale.
                timesheetSettings.setCalculateChargeBy(1);
                timesheetSettings.setMarginPercentage(BigDecimal.ZERO);
                timesheetSettings.setMarkupPercentage(BigDecimal.ZERO);
                timesheetSettings.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5, 6));
                timesheetSettings.setWorkLogType(1);
                timesheetSettings.setCalculateBreakTime(false);
                timesheetSettings.setWorkTime(Arrays.asList(30600, 30600, 30600, 30600, 30600, 30600));
                timesheetSettings.setWorkStartTime(Arrays.asList(0, 0, 0, 0, 0, 0));
                timesheetSettings.setWorkEndTime(Arrays.asList(0, 0, 0, 0, 0, 0));
                int auditTimestamp = (int) Instant.now().getEpochSecond();
                timesheetSettings.setUpdatedOn(auditTimestamp);
                timesheetSettings.setUpdatedBy(agencyId);
                timesheetSettings.setUpdatedByUserTypeId(2);
                timesheetSettings.setEnabledOn(auditTimestamp);
                timesheetSettings.setEnabledBy(agencyId);
                timesheetSettings.setEnabledByUserTypeId(2);
                timesheetSettings.setIsPreferencesModified(1);
                timesheetSettings.setBreakTimeThreshold(0);
                timesheetSettings.setIsRemarkMandatory(0);
                timesheetSettings.setJobId(jobId);
                timesheetSettings.setContractorIds(Arrays.asList(candidateId));
                timesheetSettings.setIsReimbursementEnabled(isReimbursementEnabled);
                timesheetSettings.setIsUnplannedHoursPayEnabled(0);
                timesheetSettings.setCustomRules(createDefaultTimesheetSettingsCustomRulesHourBased());

                return timesheetSettings;
        }

        protected String generateRandomString(int length) {
                Random random = new Random();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < length; i++) {
                        sb.append(random.nextInt(10));
                }
                return sb.toString();
        }

        public Response enableTimesheet(int candidateId, int jobId, int agencyId, String authToken,
                        int timesheetFrequency, int expectedStatusCode, int isReimbursementEnabled) {
                // First create rule engine template
                // Create timesheet settings using common method
                TimesheetSettings timesheetSettings = createDefaultTimesheetSettings(jobId, candidateId, agencyId,
                                timesheetFrequency, isReimbursementEnabled);

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                authToken, null, true, timesheetSettings);
                assertThat("Timesheet should be enabled successfully", response.getStatusCode(),
                                is(expectedStatusCode));
                return response;
        }

        public Response enableTimesheetHourBased(int candidateId, int jobId, int agencyId, String authToken,
                        int timesheetFrequency, int expectedStatusCode, int isReimbursementEnabled) {
                // First create rule engine template
                // Create timesheet settings using common method
                TimesheetSettings timesheetSettings = createDefaultTimesheetSettingsHourBased(jobId, candidateId, agencyId,
                                timesheetFrequency, isReimbursementEnabled);

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                authToken, null, true, timesheetSettings);
                assertThat("Timesheet should be enabled successfully", response.getStatusCode(),
                                is(expectedStatusCode));
                return response;
        }

        public Response enableTimesheet(int candidateId, int jobId, int agencyId, String authToken,
                        int timesheetFrequency, String startDate, String endDate, int expectedStatusCode, int isReimbursementEnabled) {
                // First create rule engine template
                // Create timesheet settings using common method
                TimesheetSettings timesheetSettings = createCustomTimesheetSettings(jobId, candidateId, agencyId,
                                timesheetFrequency, isReimbursementEnabled, startDate, endDate);
                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                authToken, null, true, timesheetSettings);
                assertThat("Timesheet should be enabled successfully", response.getStatusCode(),
                                is(expectedStatusCode));
                return response;
        }

        /**
         * Enables timesheet settings with an explicit Calculate Charge By mode and margin/markup percentage —
         * use this (instead of the default-mode overloads above) for Margin/Markup-specific test coverage.
         */
        public Response enableTimesheetWithChargeMode(int candidateId, int jobId, int agencyId, String authToken,
                        int timesheetFrequency, int isReimbursementEnabled, int calculateChargeBy,
                        BigDecimal marginPercentage, BigDecimal markupPercentage, int expectedStatusCode) {
                TimesheetSettings timesheetSettings = createDefaultTimesheetSettings(jobId, candidateId, agencyId,
                                timesheetFrequency, isReimbursementEnabled);
                timesheetSettings.setCalculateChargeBy(calculateChargeBy);
                timesheetSettings.setMarginPercentage(marginPercentage);
                timesheetSettings.setMarkupPercentage(markupPercentage);

                Response response = RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                authToken, null, true, timesheetSettings);
                assertThat("Timesheet settings request with calculateChargeBy=" + calculateChargeBy
                                + " should return " + expectedStatusCode, response.getStatusCode(),
                                is(expectedStatusCode));
                return response;
        }

        /**
         * Same as {@link #enableTimesheetWithChargeMode} but returns the raw {@code Response} without asserting
         * the status code — use when a test needs to inspect the response body/status itself (e.g. null-field
         * validation cases where the expected code varies per scenario).
         */
        public Response buildAndSendTimesheetSettingsWithChargeMode(int candidateId, int jobId, int agencyId,
                        String authToken, int timesheetFrequency, int isReimbursementEnabled,
                        Integer calculateChargeBy, BigDecimal marginPercentage, BigDecimal markupPercentage) {
                TimesheetSettings timesheetSettings = createDefaultTimesheetSettings(jobId, candidateId, agencyId,
                                timesheetFrequency, isReimbursementEnabled);
                timesheetSettings.setCalculateChargeBy(calculateChargeBy);
                timesheetSettings.setMarginPercentage(marginPercentage);
                timesheetSettings.setMarkupPercentage(markupPercentage);

                return RestClient.doPost("JSON", timesheetBaseURL, "timesheet-settings",
                                authToken, null, true, timesheetSettings);
        }

        /** GET /v1/timesheet-settings/job/{jobId}/contractor/{contractorId} — no prior helper existed for this endpoint. */
        public Response getTimesheetSettingsByJobAndContractor(int jobId, int contractorId, String authToken) {
                return RestClient.doGet("JSON", timesheetBaseURL,
                                "timesheet-settings/job/" + jobId + "/contractor/" + contractorId,
                                authToken, null, null, true);
        }

        public Response getTimeSheetFreeSlots(int candidateId, int jobId,
                        int timesheetFrequency, String authToken, String startDate, String endDate) {
                // First enable timesheet for the candidate

                // Create FreeSlotsRequest
                FreeSlotsRequest freeSlotsRequest = new FreeSlotsRequest();
                freeSlotsRequest.setContractorIds(Arrays.asList(candidateId));
                freeSlotsRequest.setStartDate(Long.parseLong(startDate));
                freeSlotsRequest.setEndDate(Long.parseLong(endDate));
                freeSlotsRequest.setTimesheetFrequencyId(timesheetFrequency);
                freeSlotsRequest.setTimesheetStartDay(1);

                // Make API call to get free slots
                if (startDate == "0" || startDate.isEmpty() || endDate == "0" || endDate.isEmpty()) {
                        return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/free-slots",
                                        authToken, null, true, new FreeSlotsRequest(Arrays.asList(), 0, 0, 0, 0));
                }
                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/free-slots",
                                authToken, null, true, freeSlotsRequest);

        }

        public Response addTimeSheet(int jobId, List<Integer> contractorIds, List<TimesheetDate> timesheetDates,
                        String authToken) {
                // Create AddTimesheetRequest
                AddTimesheetRequest addTimesheetRequest = new AddTimesheetRequest();
                addTimesheetRequest.setContractorIds(contractorIds);
                addTimesheetRequest.setTimesheetDates(timesheetDates);

                // Make API call to add timesheet
                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/jobs/" + jobId + "/contractors",
                                authToken, null, true, addTimesheetRequest);
        }

        public Response getAllTimesheets(int jobId, int contractorId, int page, int size, String authToken) {
                // Create request payload
                GetTimesheetRequest getTimesheetRequest = GetTimesheetRequest.builder()
                                .sortPriorityList(Arrays.asList())
                                .build();

                // Create query parameters
                Map<String, String> queryParameters = new java.util.HashMap<>();
                queryParameters.put("jobId", String.valueOf(jobId));
                queryParameters.put("contractorId", String.valueOf(contractorId));
                queryParameters.put("page", String.valueOf(page));
                queryParameters.put("size", String.valueOf(size));

                // Make API call to get all timesheets
                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/job/contractor/get",
                                authToken, queryParameters, true, getTimesheetRequest);
        }

        public Object[] createContractStaffingTestData(String baseURL, String apiAuthToken, String albatrossURL,
                        String albatrossAuthToken) {
                ExecutorService executor = Executors.newFixedThreadPool(10);

                try {
                        // Phase 1: Parallel execution of independent operations
                        CompletableFuture<JsonPath> candidate1Future = CompletableFuture.supplyAsync(() ->
                                        function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(),
                                        executor);

                        CompletableFuture<JsonPath> candidate2Future = CompletableFuture.supplyAsync(() ->
                                        function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(),
                                        executor);

                        CompletableFuture<JsonPath> candidate3Future = CompletableFuture.supplyAsync(() ->
                                        function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(),
                                        executor);

                        CompletableFuture<JsonPath> companyFuture = CompletableFuture.supplyAsync(() ->
                                        function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath(),
                                        executor);

                        CompletableFuture<Response> usersFuture = CompletableFuture.supplyAsync(() ->
                                        function.getUsers(baseURL, apiAuthToken), executor);

                        // Wait for Phase 1 to complete
                        JsonPath jsonCandidate = candidate1Future.join();
                        JsonPath jsonCandidate2 = candidate2Future.join();
                        JsonPath jsonCandidate3 = candidate3Future.join();
                        JsonPath jsonCompany = companyFuture.join();
                        Response usersResponse = usersFuture.join();

                        String candidateSlug = jsonCandidate.getString("slug");
                        String candidateSlug2 = jsonCandidate2.getString("slug");
                        String candidateSlug3 = jsonCandidate3.getString("slug");
                        String companySlug = jsonCompany.getString("slug");
                        JsonPath usersJsonPath = usersResponse.jsonPath();
                        int userId = usersJsonPath.getInt("[0].id");

                        // Phase 2: Create contact (depends on companySlug)
                        JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug)
                                        .jsonPath();
                        String contactSlug = jsonContact.getString("slug");

                        // Phase 3: Create job (depends on companySlug and contactSlug)
                        JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug)
                                        .jsonPath();
                        String jobSlug = jsonJob.getString("slug");

                        // Phase 4: Create deal and assign candidates (can be parallelized)
                        HashMap<Integer, String> fieldsMap = new HashMap<>();
                        fieldsMap.put(5, companySlug);
                        fieldsMap.put(6, jobSlug);
                        fieldsMap.put(7, contactSlug);
                        fieldsMap.put(8, candidateSlug);

                        CompletableFuture<JsonPath> dealFuture = CompletableFuture.supplyAsync(() ->
                                        function.createNewDealWithSpecifiedFields(baseURL, apiAuthToken, fieldsMap)
                                                        .jsonPath(),
                                        executor);

                        CompletableFuture<Void> assign1Future = CompletableFuture.runAsync(() ->
                                        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug),
                                        executor);

                        CompletableFuture<Void> assign2Future = CompletableFuture.runAsync(() ->
                                        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug2, jobSlug),
                                        executor);

                        CompletableFuture<Void> assign3Future = CompletableFuture.runAsync(() ->
                                        function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug3, jobSlug),
                                        executor);

                        // Wait for Phase 4 to complete
                        JsonPath jsonDeal = dealFuture.join();
                        CompletableFuture.allOf(assign1Future, assign2Future, assign3Future).join();

                        // Phase 5: Get albatross IDs (can be parallelized)
                        CompletableFuture<Integer> candidateId1Future = CompletableFuture.supplyAsync(() ->
                                        function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug,
                                                        "candidate").jsonPath().getInt("data.candidate.id"),
                                        executor);

                        CompletableFuture<Integer> candidateId2Future = CompletableFuture.supplyAsync(() ->
                                        function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug2,
                                                        "candidate").jsonPath().getInt("data.candidate.id"),
                                        executor);

                        CompletableFuture<Integer> candidateId3Future = CompletableFuture.supplyAsync(() ->
                                        function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug3,
                                                        "candidate").jsonPath().getInt("data.candidate.id"),
                                        executor);

                        CompletableFuture<Integer> jobIdFuture = CompletableFuture.supplyAsync(() ->
                                        function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                                                        .jsonPath().getInt("data.job.id"),
                                        executor);

                        // Wait for Phase 5 to complete
                        int candidateId = candidateId1Future.join();
                        int candidateId2 = candidateId2Future.join();
                        int candidateId3 = candidateId3Future.join();
                        int jobId = jobIdFuture.join();

                        return new Object[] { jobId, candidateId, candidateId2, candidateId3, userId, candidateSlug,
                                        candidateSlug2, candidateSlug3, companySlug, contactSlug, jobSlug,
                                        jsonCandidate, jsonCandidate2, jsonCandidate3, jsonJob, jsonCompany,
                                        jsonContact, jsonDeal };
                } finally {
                        executor.shutdown();
                }
        }

        public Object[] createSingleCandidateTestData(String baseURL, String apiAuthToken, String albatrossURL,
                        String albatrossAuthToken) {
                // Create candidate using commanFunction
                JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken)
                                .jsonPath();
                String candidateSlug = jsonCandidate.getString("slug");

                // Create company using commanFunction
                JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
                String companySlug = jsonCompany.getString("slug");

                // Create contact using commanFunction
                JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
                String contactSlug = jsonContact.getString("slug");

                // Create job using commanFunction
                JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
                String jobSlug = jsonJob.getString("slug");

                // Assign candidate to the job
                function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug, jobSlug);

                // Get users to get userId using commanFunction
                Response usersResponse = function.getUsers(baseURL, apiAuthToken);
                JsonPath usersJsonPath = usersResponse.jsonPath();
                int userId = usersJsonPath.getInt("[0].id");

                // Get candidate ID from albatross
                int candidateId = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug, "candidate")
                                .jsonPath().getInt("data.candidate.id");

                // Get job ID from albatross
                int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job")
                                .jsonPath().getInt("data.job.id");

                return new Object[] { jobId, candidateId, userId };
        }

        /**
         * Two jobs under the same company/contact, each with one candidate: candidate1→job1, candidate2→job2.
         *
         * @return {@code [jobId1, jobId2, candidateId1, candidateId2, userId]}
         */
        public Object[] createTwoJobsTwoCandidatesTestData(String baseURL, String apiAuthToken, String albatrossURL,
                        String albatrossAuthToken) {
                JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
                String companySlug = jsonCompany.getString("slug");

                JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
                String contactSlug = jsonContact.getString("slug");

                JsonPath jsonJob1 = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
                String jobSlug1 = jsonJob1.getString("slug");
                JsonPath jsonJob2 = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
                String jobSlug2 = jsonJob2.getString("slug");

                JsonPath jsonCandidate1 = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken)
                                .jsonPath();
                String candidateSlug1 = jsonCandidate1.getString("slug");
                JsonPath jsonCandidate2 = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken)
                                .jsonPath();
                String candidateSlug2 = jsonCandidate2.getString("slug");

                function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug1, jobSlug1);
                function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug2, jobSlug2);

                Response usersResponse = function.getUsers(baseURL, apiAuthToken);
                int userId = usersResponse.jsonPath().getInt("[0].id");

                int jobId1 = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug1, "job").jsonPath()
                                .getInt("data.job.id");
                int jobId2 = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug2, "job").jsonPath()
                                .getInt("data.job.id");
                int candidateId1 = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug1, "candidate")
                                .jsonPath().getInt("data.candidate.id");
                int candidateId2 = function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug2, "candidate")
                                .jsonPath().getInt("data.candidate.id");

                return new Object[] { jobId1, jobId2, candidateId1, candidateId2, userId };
        }

        public Object[][] getTimesheetFrequencyData() {
                return new Object[][] {
                                { 2 }, // Weekly
                                { 3 }, // BiWeekly
                                { 4 } // Monthly
                };
        }

        protected List<TimesheetDate> convertFreeSlotsToTimesheetDates(List<Map<String, Object>> freeSlots,
                        int timesheetFrequency) {
                List<TimesheetDate> timesheetDates = new ArrayList<>();

                if (freeSlots == null || freeSlots.isEmpty()) {
                        return timesheetDates; // Return empty list if no free slots
                }

                for (Map<String, Object> slot : freeSlots) {
                        TimesheetDate timesheetDate = new TimesheetDate();
                        try {
                                // Use reflection to set values since Lombok setters might not be available
                                java.lang.reflect.Method setStartDate = TimesheetDate.class.getMethod("setStartDate",
                                                long.class);
                                java.lang.reflect.Method setEndDate = TimesheetDate.class.getMethod("setEndDate",
                                                long.class);

                                Long startDate = ((Number) slot.get("startDate")).longValue();
                                Long endDate = ((Number) slot.get("endDate")).longValue();

                                setStartDate.invoke(timesheetDate, startDate);
                                setEndDate.invoke(timesheetDate, endDate);

                                timesheetDates.add(timesheetDate);

                        } catch (Exception e) {
                                throw new RuntimeException("Failed to create TimesheetDate object", e);
                        }
                }

                // Only create subset if there are more than 3 slots available
                List<TimesheetDate> timesheetDatesSubset;
                if (timesheetFrequency == 4) {
                        timesheetDatesSubset = timesheetDates.subList(0, timesheetDates.size() - 1);
                } else {
                        timesheetDatesSubset = timesheetDates; // Use all available slots
                }

                return timesheetDatesSubset;
        }

        public Response getTimeSheetTimeLogs(int timesheetId, String authToken) {
                return RestClient.doGet("JSON", timesheetBaseURL, "timesheets/" + timesheetId + "/time-logs",
                                authToken, null, null, true);
        }

        public Response submitTimeLogsForTimesheet(SubmitTimeLogsRequest submitTimeLogsRequest, String authToken) {
                applyBulkTimeLogsDefaults(submitTimeLogsRequest);
                return RestClient.doPatch("application/json", timesheetBaseURL, "/timesheets/bulk/time-logs",
                                authToken, null, null, true, submitTimeLogsRequest);
        }
        
        public Response submitTimeLogsForTimesheetHourBased(Map<String, Object> submitTimeLogsRequest, String authToken) {
                Map<String, Object> normalizedRequest = normalizeHourBasedBulkTimeLogsRequest(submitTimeLogsRequest);
                return RestClient.doPatch("application/json", timesheetBaseURL, "/timesheets/bulk/time-logs",
                                authToken, null, null, true, normalizedRequest);
        }

        protected Map<String, Object> normalizeHourBasedBulkTimeLogsRequest(Map<String, Object> submitTimeLogsRequest) {
                if (submitTimeLogsRequest == null) {
                        return null;
                }
                Map<String, Object> normalizedRequest = new LinkedHashMap<>(submitTimeLogsRequest);
                normalizedRequest.putIfAbsent("isApproved", 0);
                normalizedRequest.putIfAbsent("save", 0);
                normalizedRequest.putIfAbsent("timesheetIdNoLogChanges", Collections.emptyList());

                Object timeLogs = normalizedRequest.get("timeLogs");
                if (!normalizedRequest.containsKey("timeDetails") && timeLogs instanceof List<?>) {
                        normalizedRequest.put("timeDetails", generateTimeDetailsFromTimeLogMaps((List<?>) timeLogs));
                }
                return normalizedRequest;
        }

        protected void applyBulkTimeLogsDefaults(SubmitTimeLogsRequest submitTimeLogsRequest) {
                if (submitTimeLogsRequest == null) {
                        return;
                }
                if (submitTimeLogsRequest.getIsApproved() == null) {
                        submitTimeLogsRequest.setIsApproved(0);
                }
                if (submitTimeLogsRequest.getSave() == null) {
                        submitTimeLogsRequest.setSave(0);
                }
                if (submitTimeLogsRequest.getTimesheetIdNoLogChanges() == null) {
                        submitTimeLogsRequest.setTimesheetIdNoLogChanges(Collections.emptyList());
                }
                if ((submitTimeLogsRequest.getTimeDetails() == null || submitTimeLogsRequest.getTimeDetails().isEmpty())
                                && submitTimeLogsRequest.getTimeLogs() != null) {
                        submitTimeLogsRequest.setTimeDetails(generateTimeDetailsFromTimeLogs(
                                        submitTimeLogsRequest.getTimeLogs()));
                }
        }

        protected Map<String, Object> normalizeBulkTimeLogsRequest(Map<String, Object> submitTimeLogsRequest) {
                if (submitTimeLogsRequest == null) {
                        return null;
                }
                Map<String, Object> normalizedRequest = new LinkedHashMap<>(submitTimeLogsRequest);
                normalizedRequest.putIfAbsent("isApproved", 0);
                normalizedRequest.putIfAbsent("save", 0);
                normalizedRequest.putIfAbsent("timesheetIdNoLogChanges", Collections.emptyList());

                Object timeLogs = normalizedRequest.get("timeLogs");
                if (timeLogs instanceof List<?>) {
                        List<Object> normalizedTimeLogs = ((List<?>) timeLogs).stream()
                                        .map(this::normalizeBulkTimeLog)
                                        .collect(Collectors.toList());
                        normalizedRequest.put("timeLogs", normalizedTimeLogs);
                        if (!normalizedRequest.containsKey("timeDetails")) {
                                normalizedRequest.put("timeDetails", generateTimeDetailsFromTimeLogMaps(normalizedTimeLogs));
                        }
                }
                return normalizedRequest;
        }

        @SuppressWarnings("unchecked")
        protected Object normalizeBulkTimeLog(Object timeLog) {
                if (!(timeLog instanceof Map<?, ?>)) {
                        return timeLog;
                }
                Map<String, Object> normalizedLog = new LinkedHashMap<>((Map<String, Object>) timeLog);
                Object workTimeDetails = normalizedLog.get("workTimeDetails");
                if (workTimeDetails == null) {
                        normalizedLog.put("workTimeDetails", buildDefaultBulkWorkTimeDetails());
                }
                normalizedLog.remove("workTime");
                normalizedLog.remove("breakTime");
                normalizedLog.remove("remark");
                return normalizedLog;
        }

        @SuppressWarnings("unchecked")
        protected List<TimeDetails> generateTimeDetailsFromTimeLogMaps(List<?> timeLogs) {
                Map<Integer, TimeDetails> timeDetailsByTimesheetId = new LinkedHashMap<>();
                for (Object timeLog : timeLogs) {
                        if (!(timeLog instanceof Map<?, ?>)) {
                                continue;
                        }
                        Map<String, Object> timeLogMap = (Map<String, Object>) timeLog;
                        Integer timesheetId = getIntegerValue(timeLogMap.get("timesheetId"));
                        if (timesheetId == null) {
                                continue;
                        }
                        TimeDetails timeDetails = timeDetailsByTimesheetId.computeIfAbsent(timesheetId,
                                        (key) -> TimeDetails.builder()
                                                        .timesheetId(key)
                                                        .totalWorkTime(0)
                                                        .totalOvertime(0)
                                                        .totalTime(0)
                                                        .build());
                        int totalTime = getIntegerValueOrZero(timeLogMap.get("totalTime"));
                        int overTime = calculateTotalOvertimeForBulkPayload(getIntegerValue(timeLogMap.get("overTime")));
                        timeDetails.setTotalWorkTime(timeDetails.getTotalWorkTime() + totalTime);
                        timeDetails.setTotalOvertime(timeDetails.getTotalOvertime() + overTime);
                        timeDetails.setTotalTime(timeDetails.getTotalTime() + totalTime);
                }
                return new ArrayList<>(timeDetailsByTimesheetId.values());
        }

        protected Integer getIntegerValue(Object value) {
                if (value instanceof Number) {
                        return ((Number) value).intValue();
                }
                return null;
        }

        protected int getIntegerValueOrZero(Object value) {
                Integer integerValue = getIntegerValue(value);
                return integerValue == null ? 0 : integerValue;
        }

        protected List<WorkTimeDetail> buildDefaultBulkWorkTimeDetails() {
                WorkTimeDetail detail1 = buildWorkTimeDetail(28800, 45000, null,
                                Arrays.asList(buildBreakInterval(32400, 34200, 1),
                                                buildBreakInterval(39600, 41400, 2)));
                WorkTimeDetail detail2 = buildWorkTimeDetail(50400, 64800, null,
                                Arrays.asList(buildBreakInterval(54000, 55800, 1),
                                                buildBreakInterval(59400, 61200, 2)));
                return Arrays.asList(detail1, detail2);
        }

        protected BreakInterval buildBreakInterval(int breakStartTime, int breakEndTime, int id) {
                BreakInterval breakInterval = new BreakInterval();
                breakInterval.setBreakStartTime(breakStartTime);
                breakInterval.setBreakEndTime(breakEndTime);
                breakInterval.setId(id);
                return breakInterval;
        }

        protected List<TimeDetails> generateTimeDetailsFromTimeLogs(List<TimeLog> timeLogs) {
                Map<Integer, List<TimeLog>> timeLogsByTimesheetId = new LinkedHashMap<>();
                for (TimeLog timeLog : timeLogs) {
                        if (timeLog == null || timeLog.getTimesheetId() == null) {
                                continue;
                        }
                        Integer timesheetId = timeLog.getTimesheetId();
                        timeLogsByTimesheetId.computeIfAbsent(timesheetId, (key) -> new ArrayList<>()).add(timeLog);
                }
                List<TimeDetails> timeDetails = new ArrayList<>();
                for (Map.Entry<Integer, List<TimeLog>> entry : timeLogsByTimesheetId.entrySet()) {
                        timeDetails.add(buildTimeDetailsForBulkPayload(entry.getKey(), entry.getValue()));
                }
                return timeDetails;
        }

        protected TimeDetails buildTimeDetailsForBulkPayload(Integer timesheetId, List<TimeLog> timeLogs) {
                int totalWorkTime = 0;
                int totalOvertime = 0;
                int totalTime = 0;
                for (TimeLog timeLog : timeLogs) {
                        int logTotalTime = getTotalTimeForBulkPayload(timeLog);
                        totalWorkTime = totalWorkTime + logTotalTime;
                        totalOvertime = totalOvertime + calculateTotalOvertimeForBulkPayload(timeLog);
                        totalTime = totalTime + logTotalTime;
                }
                return TimeDetails.builder()
                                .timesheetId(timesheetId)
                                .totalWorkTime(totalWorkTime)
                                .totalOvertime(totalOvertime)
                                .totalTime(totalTime)
                                .build();
        }

        protected int getTotalTimeForBulkPayload(TimeLog timeLog) {
                if (timeLog == null) {
                        return 0;
                }
                Integer totalTime = timeLog.getTotalTime();
                if (totalTime == null) {
                        return 0;
                }
                return totalTime;
        }

        @SuppressWarnings("UnnecessaryUnboxing")
        protected int calculateTotalOvertimeForBulkPayload(TimeLog timeLog) {
                if (timeLog == null) {
                        return 0;
                }
                return calculateTotalOvertimeForBulkPayload(timeLog.getOverTime());
        }

        @SuppressWarnings("UnnecessaryUnboxing")
        protected int calculateTotalOvertimeForBulkPayload(Integer overTime) {
                if (overTime == null) {
                        return 0;
                }
                if (Integer.valueOf(3600).equals(overTime)) {
                        return 7200;
                }
                return overTime;
        }
        /**
         * Builds a list of time-log maps for hour-based PATCH timesheets/bulk/time-logs.
         * Each map has workTime, overTime, breakTime, remark, totalTime, workTimeDetails, id, timesheetId.
         */
        public List<Map<String, Object>> generateTimeLogIdsforHourBased(List<Map<String, Object>> timeLogs, int timesheetId) {
                List<Map<String, Object>> timeLogsList = new ArrayList<>();
                for (int i = 0; i < Math.max(0, timeLogs.size()); i++) {
                        Map<String, Object> timeLogData = timeLogs.get(i);
                        String timesheetPeriod = timeLogData.containsKey("timesheetPeriod")
                        ? (String) timeLogData.get("timesheetPeriod")
                        : "";
                        int timeLogId = timeLogData.containsKey("id") ? ((Number) timeLogData.get("id")).intValue() : 0;
                        Map<String, Object> log = new HashMap<>();
                        log.put("workTime", 39600);
                        log.put("breakTime", 3600);
                        log.put("remark", "remark1");
                        log.put("overTime", 7200);
                        log.put("totalTime", 39600);
                        log.put("workTimeDetails", null);
                        log.put("id", timeLogId);
                        log.put("timesheetId", timesheetId);
                        log.put("timesheetPeriod", timesheetPeriod);
                        timeLogsList.add(log);
                }
                return timeLogsList;
        }

        public TimeDetails generateTimeDetailsForHourBased(List<Map<String, Object>> timeLogs, int timesheetID) {
                int size = Math.max(0, timeLogs.size());
                int totalWorkTime = size * 39600;
                int totalOvertime = size * 7200;
                int totalTime = size * 39600;
                TimeDetails timeDetails = TimeDetails.builder()
                        .timesheetId(timesheetID)
                        .totalWorkTime(totalWorkTime)
                        .totalOvertime(totalOvertime)
                        .totalTime(totalTime)
                        .build();
                return timeDetails;
        }

        public Response approveTimesheet(int timesheetId, ApproveTimesheetRequest approveTimesheetRequest,
                        String authToken) {
                return RestClient.doPost("JSON", timesheetBaseURL, "/timesheets/" + timesheetId + "/status",
                                authToken, null, true, approveTimesheetRequest);
        }

        /**
         * Status 3 = rejected (sets {@code approvalStatus} and {@code remark}); status 4 = approved (sets
         * {@code approvalStatus} only).
         */
        protected ApproveTimesheetRequest buildApproveTimesheetRequest(int approvalStatus) {
                return buildApproveTimesheetRequest(approvalStatus, "Automated test");
        }

        protected ApproveTimesheetRequest buildApproveTimesheetRequest(int approvalStatus, String rejectRemark) {
                ApproveTimesheetRequest request = new ApproveTimesheetRequest();
                request.setApprovalStatus(approvalStatus);
                if (approvalStatus == 3) {
                        request.setRemark(rejectRemark != null ? rejectRemark : "Automated test");
                }
                return request;
        }

        public Response getTimeSheetStatusHistory(int timesheetId, String authToken) {
                return RestClient.doGet("JSON", timesheetBaseURL, "/timesheets/" + timesheetId + "/status-history",
                                authToken, null, null, true);
        }

        public Response deleteTimesheet(int timesheetId, String authToken) {
                return RestClient.doDelete("JSON", timesheetBaseURL, "/timesheets/" + timesheetId,
                                authToken, null, null, true);
        }

        public Response bulkDeleteTimesheets(List<Integer> timesheetIds, String authToken) {
                // Create BulkDeleteTimesheetRequest
                BulkDeleteTimesheetRequest bulkDeleteRequest = new BulkDeleteTimesheetRequest();
                bulkDeleteRequest.setTimesheetIds(timesheetIds);

                // Make API call to bulk delete timesheets
                return RestClient.doDelete("JSON", timesheetBaseURL, "/timesheets",
                                authToken, null, null, true, bulkDeleteRequest);
        }

        public Response validateTimeLogs(List<Integer> timesheetIds, String authToken) {
                ValidateTimeLogsRequest validateTimeLogsRequest = new ValidateTimeLogsRequest();
                validateTimeLogsRequest.setTimesheetIds(timesheetIds);

                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/contractor/validate/time-logs",
                                authToken, null, true, validateTimeLogsRequest);
        }

        public Response getTimeSheetSettingPreferences(String authToken) {
                return RestClient.doGet("JSON", timesheetBaseURL, "timesheet-settings/preferences ",
                                authToken, null, null, true);
        }

        public List<Integer> createTimesheetsForValidation(int jobId, int candidateId, int timesheetFrequency,
                        String authToken) {
                List<Integer> timesheetIds = new ArrayList<>();

                try {
                        // Get free slots
                        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                        authToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START,
                                        DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

                        if (freeSlotsResponse.statusCode() == 200) {
                                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

                                if (!freeSlots.isEmpty()) {
                                        // Create timesheets for the first few available slots
                                        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots,
                                                        timesheetFrequency);

                                        // Create multiple timesheets
                                        for (int i = 0; i < Math.min(3, timesheetDates.size()); i++) {
                                                Response addTimesheetResponse = addTimeSheet(jobId,
                                                                Arrays.asList(candidateId),
                                                                Arrays.asList(timesheetDates.get(i)), authToken);

                                                if (addTimesheetResponse.statusCode() == 200) {
                                                        // Get the created timesheet ID
                                                        Response getAllTimesheetsResponse = getAllTimesheets(jobId,
                                                                        candidateId, 1, 100, authToken);
                                                        if (getAllTimesheetsResponse.statusCode() == 200) {
                                                                JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse
                                                                                .jsonPath();
                                                                List<Map<String, Object>> timesheets = getAllTimesheetsJsonPath
                                                                                .getList("data");
                                                                if (!timesheets.isEmpty()) {
                                                                        int timesheetId = ((Number) timesheets.get(0)
                                                                                        .get("id")).intValue();
                                                                        timesheetIds.add(timesheetId);
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                } catch (Exception e) {
                        throw new RuntimeException("Failed to create timesheets dynamically", e);
                }

                return timesheetIds;
        }

        public List<Integer> createSingleTimesheetForValidation(int jobId, int candidateId, int timesheetFrequency,
                        String authToken) {
                List<Integer> timesheetIds = new ArrayList<>();

                try {
                        // Get free slots
                        Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                        authToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START,
                                        DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

                        if (freeSlotsResponse.statusCode() == 200) {
                                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

                                if (!freeSlots.isEmpty()) {
                                        List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots,
                                                        timesheetFrequency);

                                        // Create a single timesheet
                                        Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId),
                                                        Arrays.asList(timesheetDates.get(0)), authToken);

                                        if (addTimesheetResponse.statusCode() == 200) {
                                                Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId,
                                                                1, 100, authToken);
                                                if (getAllTimesheetsResponse.statusCode() == 200) {
                                                        JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse
                                                                        .jsonPath();
                                                        List<Map<String, Object>> timesheets = getAllTimesheetsJsonPath
                                                                        .getList("data");
                                                        if (!timesheets.isEmpty()) {
                                                                int timesheetId = ((Number) timesheets.get(0).get("id"))
                                                                                .intValue();
                                                                timesheetIds.add(timesheetId);
                                                        }
                                                }
                                        }
                                }
                        }
                } catch (Exception e) {
                        throw new RuntimeException("Failed to create single timesheet dynamically", e);
                }

                return timesheetIds;
        }

        public Response validateDealTimeLogs(List<Integer> timesheetIds, String authToken) {
                // Create ValidateTimeLogsRequest
                ValidateTimeLogsRequest validateTimeLogsRequest = new ValidateTimeLogsRequest();
                validateTimeLogsRequest.setTimesheetIds(timesheetIds);

                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/deal/validate/time-logs",
                                authToken, null, true, validateTimeLogsRequest);
        }

        protected int getExpectedTimeLogsLength(int timesheetFrequency) {
                switch (timesheetFrequency) {
                        case 2:
                                return 20;
                        case 3:
                                return 41;
                        case 4:
                                return 62;
                        default:
                                return 20;
                }
        }

        protected void validateTimesheetPeriods(List<Map<String, Object>> timeLogs, int timesheetFrequency) {
                List<String> actualPeriods = timeLogs.stream()
                                .map(timeLog -> (String) timeLog.get("timesheetPeriod"))
                                .distinct()
                                .collect(Collectors.toList());

                assertThat(actualPeriods, not(empty()));
                for (String period : actualPeriods) {
                        assertThat(period, notNullValue());
                        assertThat(period.contains(" - "), is(true));
                        assertThat(period.matches(".*\\d{4}.*"), is(true));
                }
                int expectedMinPeriods = getExpectedMinPeriods(timesheetFrequency);
                assertThat(actualPeriods.size() >= expectedMinPeriods, is(true));
        }

        protected int getExpectedMinPeriods(int timesheetFrequency) {
                switch (timesheetFrequency) {
                        case 2:
                                return 2;
                        case 3:
                                return 2;
                        case 4:
                                return 2;
                        default:
                                return 2;
                }
        }

        protected void validateTimesheetIdsInTimeLogs(List<Map<String, Object>> timeLogs, List<Integer> timesheetIds) {
                List<Integer> actualTimesheetIds = timeLogs.stream()
                                .map(timeLog -> (Integer) timeLog.get("timesheetId"))
                                .distinct()
                                .collect(Collectors.toList());

                for (Integer requestedId : timesheetIds) {
                        assertThat(actualTimesheetIds.contains(requestedId), is(true));
                }

                for (Integer actualId : actualTimesheetIds) {
                        assertThat(timesheetIds.contains(actualId), is(true));
                }

                assertThat(actualTimesheetIds.size(), is(timesheetIds.size()));
        }

        protected void validateFrequencySpecificConditions(List<Map<String, Object>> timeLogs, int timesheetFrequency) {
                List<String> actualPeriods = timeLogs.stream()
                                .map(timeLog -> (String) timeLog.get("timesheetPeriod"))
                                .distinct()
                                .collect(Collectors.toList());

                switch (timesheetFrequency) {
                        case 2:
                                validateWeeklyConditions(timeLogs, actualPeriods);
                                break;
                        case 3:
                                validateBiweeklyConditions(timeLogs, actualPeriods);
                                break;
                        case 4:
                                validateMonthlyConditions(timeLogs, actualPeriods);
                                break;
                        default:
                                throw new AssertionError("Unsupported timesheet frequency: " + timesheetFrequency);
                }
        }

        protected void validateWeeklyConditions(List<Map<String, Object>> timeLogs, List<String> actualPeriods) {
                assertThat(timeLogs.size() >= 6 && timeLogs.size() <= 7, is(true));
                assertThat(actualPeriods.size(), is(1));
                for (String period : actualPeriods) {
                        assertThat(period.contains(" - "), is(true));
                        assertThat(period.matches(".*\\d{4}.*"), is(true));
                }
        }

        protected void validateBiweeklyConditions(List<Map<String, Object>> timeLogs, List<String> actualPeriods) {
                assertThat(timeLogs.size() >= 12 && timeLogs.size() <= 14, is(true));
                assertThat(actualPeriods.size(), is(1));
                for (String period : actualPeriods) {
                        assertThat(period.contains(" - "), is(true));
                        assertThat(period.matches(".*\\d{4}.*"), is(true));
                }
        }

        protected void validateMonthlyConditions(List<Map<String, Object>> timeLogs, List<String> actualPeriods) {
                assertThat(timeLogs.size() >= 28 && timeLogs.size() <= 31, is(true));
                assertThat(actualPeriods.size(), is(1));
                for (String period : actualPeriods) {
                        assertThat(period.contains(" - "), is(true));
                        assertThat(period.matches(".*\\d{4}.*"), is(true));
                }
        }

        protected long getExpectedPeriodEnd(int timesheetFrequency) {
                switch (timesheetFrequency) {
                        case 2:
                                return 1751846399L;
                        case 3:
                                return 1752451199L;
                        case 4:
                                return 1754006399L;
                        default:
                                return 1751846399L;
                }
        }

        protected int getExpectedTimeLogsCountForDeal(int timesheetFrequency) {
                switch (timesheetFrequency) {
                        case 2:
                                return 6;
                        case 3:
                                return 13;
                        case 4:
                                return 31;
                        default:
                                return 6;
                }
        }

        protected Response retryApiCall(java.util.function.Supplier<Response> apiCall,
                        int maxRetries,
                        int initialDelay,
                        int delayIncrement,
                        java.util.function.Predicate<Response> successCondition) {
                Response response = null;
                int currentDelay = initialDelay;
                boolean success = false;

                for (int attempt = 0; attempt < maxRetries; attempt++) {
                        response = apiCall.get();
                        if (successCondition.test(response)) {
                                success = true;
                                break;
                        }

                        if (attempt < maxRetries - 1) {
                                try {
                                        Thread.sleep(currentDelay);
                                        currentDelay += delayIncrement;
                                } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        throw new RuntimeException("Retry interrupted", e);
                                }
                        }
                }

                if (!success) {
                        throw new RuntimeException(
                                        "Failed to get successful response after " + maxRetries + " attempts");
                }

                return response;
        }

        /**
         * Builds a WorkTimeDetail in the new API format (id null, rangeBasedBreakTime 3600).
         */
        protected WorkTimeDetail buildWorkTimeDetail(int workStartTime, int workEndTime, String rangeBasedRemark, List<BreakInterval> breakIntervals) {
                return WorkTimeDetail.builder()
                        .workStartTime(workStartTime)
                        .workEndTime(workEndTime)
                        .rangeBasedRemark(rangeBasedRemark)
                        .rangeBasedBreakTime(3600)
                        .breakIntervals(breakIntervals != null ? breakIntervals : Collections.emptyList())
                        .build();
        }

        /**
         * Builds a TimeLog in the new API format with workTimeDetails (single detail with breakIntervals).
         */
        protected TimeLog buildTimeLogWithWorkTimeDetails(int startTime, int endTime, Integer id, Integer timesheetId, Long date, int breakTime, int overTime, int totalTime, String remark, List<BreakInterval> breakIntervals) {
                WorkTimeDetail detail = buildWorkTimeDetail(startTime, endTime, remark, breakIntervals);
                return TimeLog.builder()
                        .id(id)
                        .timesheetId(timesheetId)
                        .timesheetPeriod("")
                        .workTimeDetails(Collections.singletonList(detail))
                        .overTime(overTime)
                        .totalTime(totalTime)
                        .build();
        }

        /**
         * Builds a TimeLog with multiple workTimeDetails (e.g. morning and afternoon ranges).
         * Matches payload: id, timesheetId, timesheetPeriod, workTimeDetails, totalTime, overTime.
         */
        protected TimeLog buildTimeLogWithWorkTimeDetailsList(Integer id, Integer timesheetId, String timesheetPeriod,
                        List<WorkTimeDetail> workTimeDetails, int totalTime, int overTime) {
                return TimeLog.builder()
                        .id(id)
                        .timesheetId(timesheetId)
                        .timesheetPeriod(timesheetPeriod != null ? timesheetPeriod : "")
                        .workTimeDetails(workTimeDetails != null ? workTimeDetails : Collections.emptyList())
                        .totalTime(totalTime)
                        .overTime(overTime)
                        .build();
        }

        /**
         * Places {@code numberOfBreaks} breaks of equal duration inside {@code [workStartTime, workEndTime]}.
         * Each break is non-overlapping; minimum gaps apply before the first break, between breaks, and after the last.
         *
         * @param workStartTime         segment start (seconds from midnight)
         * @param workEndTime           segment end (seconds from midnight)
         * @param numberOfBreaks        how many breaks (0 returns an empty list)
         * @param breakDurationSeconds  length of each break (e.g. 1800 for 30 minutes)
         * @param minGapSeconds         minimum gap between segment edge and a break, and between consecutive breaks
         * @return ordered break intervals with ids 1..n
         * @throws IllegalArgumentException if the segment is too short for the requested breaks and gaps
         */
        protected List<BreakInterval> buildBreakIntervalsWithinWorkSegment(int workStartTime, int workEndTime,
                        int numberOfBreaks, int breakDurationSeconds, int minGapSeconds) {
                if (numberOfBreaks <= 0) {
                        return Collections.emptyList();
                }
                int span = workEndTime - workStartTime;
                if (span <= 0) {
                        throw new IllegalArgumentException("work segment end must be after start");
                }
                int minSpanRequired = numberOfBreaks * breakDurationSeconds + (numberOfBreaks + 1) * minGapSeconds;
                if (span < minSpanRequired) {
                        throw new IllegalArgumentException(String.format(
                                        "Segment [%d,%d] (span=%ds) cannot fit %d break(s) of %ds with %ds gaps (need span >= %ds)",
                                        workStartTime, workEndTime, span, numberOfBreaks, breakDurationSeconds,
                                        minGapSeconds, minSpanRequired));
                }
                int leftover = span - numberOfBreaks * breakDurationSeconds - (numberOfBreaks + 1) * minGapSeconds;
                List<Integer> gapSizes = new ArrayList<>(numberOfBreaks + 1);
                for (int i = 0; i <= numberOfBreaks; i++) {
                        gapSizes.add(minGapSeconds);
                }
                for (int k = 0; k < leftover; k++) {
                        int idx = k % (numberOfBreaks + 1);
                        gapSizes.set(idx, gapSizes.get(idx) + 1);
                }
                int cursor = workStartTime;
                List<BreakInterval> breaks = new ArrayList<>();
                for (int b = 0; b < numberOfBreaks; b++) {
                        cursor += gapSizes.get(b);
                        BreakInterval interval = new BreakInterval();
                        interval.setId(b + 1);
                        interval.setBreakStartTime(cursor);
                        interval.setBreakEndTime(cursor + breakDurationSeconds);
                        breaks.add(interval);
                        cursor += breakDurationSeconds;
                }
                cursor += gapSizes.get(numberOfBreaks);
                if (cursor != workEndTime) {
                        throw new IllegalStateException(
                                        "Break layout invariant failed: expected cursor " + workEndTime + " but got " + cursor);
                }
                return breaks;
        }

        /**
         * Same as {@link #buildBreakIntervalsWithinWorkSegment(int, int, int, int, int)} with 30-minute breaks and
         * 600s minimum gaps.
         */
        protected List<BreakInterval> buildBreakIntervalsWithinWorkSegment(int workStartTime, int workEndTime,
                        int numberOfBreaks) {
                return buildBreakIntervalsWithinWorkSegment(workStartTime, workEndTime, numberOfBreaks, 1800, 600);
        }

        /**
         * Two 30-minute breaks inside the segment (delegates to {@link #buildBreakIntervalsWithinWorkSegment} with
         * {@code numberOfBreaks = 2}).
         */
        protected List<BreakInterval> buildDefaultBreakIntervalsWithinWorkSegment(int workStartTime, int workEndTime) {
                return buildBreakIntervalsWithinWorkSegment(workStartTime, workEndTime, 2);
        }

        /**
         * Like {@link #buildBreakIntervalsWithinWorkSegment(int, int, int, int, int)} but chooses a break duration so
         * {@code numberOfBreaks} intervals fit in {@code [workStartTime, workEndTime]} with {@code minGapSeconds}
         * gaps (non-overlapping, all inside the segment).
         */
        protected List<BreakInterval> buildBreakIntervalsWithinWorkSegmentAdaptive(int workStartTime, int workEndTime,
                        int numberOfBreaks, int minGapSeconds) {
                if (numberOfBreaks <= 0) {
                        return Collections.emptyList();
                }
                int span = workEndTime - workStartTime;
                int totalMinGaps = (numberOfBreaks + 1) * minGapSeconds;
                if (span <= totalMinGaps) {
                        throw new IllegalArgumentException(String.format(
                                        "Segment [%d,%d] (span=%ds) cannot fit %d break(s) with %ds minimum gaps",
                                        workStartTime, workEndTime, span, numberOfBreaks, minGapSeconds));
                }
                int breakDuration = (span - totalMinGaps) / numberOfBreaks;
                if (breakDuration < 60) {
                        throw new IllegalArgumentException(String.format(
                                        "Segment [%d,%d] yields break duration %ds (< 60s) for %d breaks",
                                        workStartTime, workEndTime, breakDuration, numberOfBreaks));
                }
                return buildBreakIntervalsWithinWorkSegment(workStartTime, workEndTime, numberOfBreaks, breakDuration,
                                minGapSeconds);
        }

        /**
         * Uses the first {@code n} of {@value #MAX_TWO_HOUR_INTERVALS} contiguous two-hour slots starting at midnight
         * ({@value #WORK_DAY_GRID_START_SECONDS}s). Each slot is {@value #TWO_HOUR_SLOT_SECONDS}s. Up to
         * {@value #MAX_BREAKS_PER_TWO_HOUR_SLOT} breaks per slot, non-overlapping and inside that slot.
         * The grid spans 22 hours (11×2h), within a 12am–11pm day ({@value #WORK_DAY_END_11PM_SECONDS}s).
         */
        protected List<WorkTimeDetail> buildWorkTimeDetailsFromElevenTwoHourGrid(int numberOfTwoHourIntervalsToUse,
                        String rangeBasedRemark, int breaksPerTwoHourSlot) {
                int n = Math.min(Math.max(numberOfTwoHourIntervalsToUse, 1), MAX_TWO_HOUR_INTERVALS);
                int m = Math.min(Math.max(breaksPerTwoHourSlot, 0), MAX_BREAKS_PER_TWO_HOUR_SLOT);
                List<WorkTimeDetail> details = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                        int segStart = WORK_DAY_GRID_START_SECONDS + i * TWO_HOUR_SLOT_SECONDS;
                        int segEnd = segStart + TWO_HOUR_SLOT_SECONDS;
                        List<BreakInterval> breaks = m == 0
                                        ? Collections.emptyList()
                                        : buildBreakIntervalsWithinWorkSegmentAdaptive(segStart, segEnd, m,
                                                        ADAPTIVE_BREAK_MIN_GAP_SECONDS);
                        details.add(buildWorkTimeDetail(segStart, segEnd, rangeBasedRemark, breaks));
                }
                return details;
        }

        /**
         * Builds {@code numberOfWorkEntries} non-overlapping {@link WorkTimeDetail} blocks.
         * For {@code numberOfWorkEntries == 2} and {@code numberOfBreaksPerSegment == 2}, uses the historical
         * morning / afternoon windows and fixed break times. Otherwise each segment gets {@code numberOfBreaksPerSegment}
         * breaks via {@link #buildBreakIntervalsWithinWorkSegment(int, int, int)} (non-overlapping, inside the segment).
         */
        protected List<WorkTimeDetail> buildNonOverlappingWorkTimeDetailsForDay(int numberOfWorkEntries,
                        String rangeBasedRemark, int defaultWorkDayStartSeconds, int defaultWorkDayEndSeconds,
                        int numberOfBreaksPerSegment) {
                if (numberOfWorkEntries < 1) {
                        throw new IllegalArgumentException("numberOfWorkEntries must be at least 1");
                }
                if (numberOfBreaksPerSegment < 0) {
                        throw new IllegalArgumentException("numberOfBreaksPerSegment must be non-negative");
                }
                if (defaultWorkDayEndSeconds <= defaultWorkDayStartSeconds) {
                        throw new IllegalArgumentException("work day end must be after work day start");
                }
                List<WorkTimeDetail> details = new ArrayList<>();
                if (numberOfWorkEntries == 2 && numberOfBreaksPerSegment == 2) {
                        BreakInterval morningBreak1 = new BreakInterval();
                        morningBreak1.setId(1);
                        morningBreak1.setBreakStartTime(32400);
                        morningBreak1.setBreakEndTime(34200);
                        BreakInterval morningBreak2 = new BreakInterval();
                        morningBreak2.setId(2);
                        morningBreak2.setBreakStartTime(39600);
                        morningBreak2.setBreakEndTime(41400);
                        details.add(buildWorkTimeDetail(28800, 45000, rangeBasedRemark,
                                        Arrays.asList(morningBreak1, morningBreak2)));

                        BreakInterval afternoonBreak1 = new BreakInterval();
                        afternoonBreak1.setId(1);
                        afternoonBreak1.setBreakStartTime(54000);
                        afternoonBreak1.setBreakEndTime(55800);
                        BreakInterval afternoonBreak2 = new BreakInterval();
                        afternoonBreak2.setId(2);
                        afternoonBreak2.setBreakStartTime(59400);
                        afternoonBreak2.setBreakEndTime(61200);
                        details.add(buildWorkTimeDetail(50400, 64800, rangeBasedRemark,
                                        Arrays.asList(afternoonBreak1, afternoonBreak2)));
                        return details;
                }
                if (numberOfWorkEntries == 2) {
                        details.add(buildWorkTimeDetail(28800, 45000, rangeBasedRemark,
                                        buildBreakIntervalsWithinWorkSegment(28800, 45000, numberOfBreaksPerSegment)));
                        details.add(buildWorkTimeDetail(50400, 64800, rangeBasedRemark,
                                        buildBreakIntervalsWithinWorkSegment(50400, 64800, numberOfBreaksPerSegment)));
                        return details;
                }
                int daySpan = defaultWorkDayEndSeconds - defaultWorkDayStartSeconds;
                int segmentWidth = daySpan / numberOfWorkEntries;
                if (segmentWidth <= 0) {
                        throw new IllegalArgumentException("work day too short for " + numberOfWorkEntries + " entries");
                }
                for (int e = 0; e < numberOfWorkEntries; e++) {
                        int segStart = defaultWorkDayStartSeconds + e * segmentWidth;
                        int segEnd = (e == numberOfWorkEntries - 1)
                                        ? defaultWorkDayEndSeconds
                                        : defaultWorkDayStartSeconds + (e + 1) * segmentWidth;
                        List<BreakInterval> breaks = buildBreakIntervalsWithinWorkSegment(segStart, segEnd,
                                        numberOfBreaksPerSegment);
                        details.add(buildWorkTimeDetail(segStart, segEnd, rangeBasedRemark, breaks));
                }
                return details;
        }

        /**
         * Same as {@link #buildNonOverlappingWorkTimeDetailsForDay(int, String, int, int, int)} with a standard
         * 8:00–18:00 work window ({@code 28800–64800}).
         */
        protected List<WorkTimeDetail> buildNonOverlappingWorkTimeDetailsForDay(int numberOfWorkEntries,
                        String rangeBasedRemark, int defaultWorkDayStartSeconds, int defaultWorkDayEndSeconds) {
                return buildNonOverlappingWorkTimeDetailsForDay(numberOfWorkEntries, rangeBasedRemark,
                                defaultWorkDayStartSeconds, defaultWorkDayEndSeconds, 2);
        }

        /**
         * Eleven two-hour slots from midnight: uses first {@code numberOfWorkEntries} slots (max
         * {@value #MAX_TWO_HOUR_INTERVALS}) and {@code numberOfBreaksPerSegment} breaks per slot (max
         * {@value #MAX_BREAKS_PER_TWO_HOUR_SLOT}). See {@link #buildWorkTimeDetailsFromElevenTwoHourGrid}.
         */
        protected List<WorkTimeDetail> buildNonOverlappingWorkTimeDetailsForDay(int numberOfWorkEntries,
                        String rangeBasedRemark, int numberOfBreaksPerSegment) {
                return buildWorkTimeDetailsFromElevenTwoHourGrid(numberOfWorkEntries, rangeBasedRemark,
                                numberOfBreaksPerSegment);
        }

        /**
         * Same as {@link #buildNonOverlappingWorkTimeDetailsForDay(int, String, int)} with two breaks per two-hour
         * slot.
         */
        protected List<WorkTimeDetail> buildNonOverlappingWorkTimeDetailsForDay(int numberOfWorkEntries,
                        String rangeBasedRemark) {
                return buildWorkTimeDetailsFromElevenTwoHourGrid(numberOfWorkEntries, rangeBasedRemark, 2);
        }

        public List<TimeLog> generateTimelogIDLists(List<Map<String, Object>> timeLogs, int timesheetID) {
                List<TimeLog> timeLogsList = new ArrayList<>();
                for (int i = 0; i < Math.max(0, timeLogs.size() - 1); i++) {
                        Map<String, Object> timeLogData = timeLogs.get(i);
                        int timeLogId = ((Number) timeLogData.get("id")).intValue();
                        String timesheetPeriod = timeLogData.containsKey("timesheetPeriod")
                                        ? (String) timeLogData.get("timesheetPeriod")
                                        : "";

                        TimeLog timeLog = buildTimeLogWithWorkTimeDetailsList(
                                        timeLogId, timesheetID, timesheetPeriod,
                                        buildDefaultBulkWorkTimeDetails(), 30600, 3600);
                        timeLogsList.add(timeLog);
                }
                return timeLogsList;
        }

        /**
         * Like {@link #generateTimelogIDListsWithNEntry(List, int, int, int)} with two breaks per segment.
         */
        public List<TimeLog> generateTimelogIDListsWithNEntry(List<Map<String, Object>> timeLogs, int timesheetID,
                        int numberOfWorkEntriesPerLog) {
                return generateTimelogIDListsWithNEntry(timeLogs, timesheetID, numberOfWorkEntriesPerLog, 2);
        }

        /**
         * Like {@link #generateTimelogIDLists(List, int)} but uses {@link #buildWorkTimeDetailsFromElevenTwoHourGrid}:
         * up to {@value #MAX_TWO_HOUR_INTERVALS} two-hour slots from midnight, each with up to
         * {@value #MAX_BREAKS_PER_TWO_HOUR_SLOT} breaks (values are clamped).
         */
        public List<TimeLog> generateTimelogIDListsWithNEntry(List<Map<String, Object>> timeLogs, int timesheetID,
                        int numberOfWorkEntriesPerLog, int numberOfBreaksPerSegment) {
                List<TimeLog> timeLogsList = new ArrayList<>();
                for (int i = 0; i < Math.max(0, timeLogs.size() - 1); i++) {
                        Map<String, Object> timeLogData = timeLogs.get(i);
                        int timeLogId = ((Number) timeLogData.get("id")).intValue();
                        String timesheetPeriod = timeLogData.containsKey("timesheetPeriod")
                                        ? (String) timeLogData.get("timesheetPeriod")
                                        : "";
                        List<WorkTimeDetail> workTimeDetails = buildNonOverlappingWorkTimeDetailsForDay(
                                        numberOfWorkEntriesPerLog, "REMARK " + (i + 1), numberOfBreaksPerSegment);
                        int totalGrossSeconds = workTimeDetails.stream()
                                        .mapToInt(d -> d.getWorkEndTime() - d.getWorkStartTime())
                                        .sum();
                        TimeLog timeLog = buildTimeLogWithWorkTimeDetailsList(
                                        timeLogId, timesheetID, timesheetPeriod,
                                        workTimeDetails, totalGrossSeconds, 7200);
                        timeLogsList.add(timeLog);
                }
                return timeLogsList;
        }

        public TimeDetails generateTimeDetails(List<Map<String, Object>> timeLogs, int timesheetID) {
                int size = Math.max(0, timeLogs.size() - 1);
                int totalWorkTime = size * 30600;
                int totalOvertime = size * 7200;
                int totalTime = size * 30600;
                if (size > 7 && size < 15) {
                        totalOvertime = totalOvertime + 23400;
                } else if (size > 14 && size < 31) {
                        totalOvertime = totalOvertime + 70200;
                }
                TimeDetails timeDetails = TimeDetails.builder()
                        .timesheetId(timesheetID)
                        .totalWorkTime(totalWorkTime)
                        .totalOvertime(totalOvertime)
                        .totalTime(totalTime)
                        .build();
                return timeDetails;
        }

        public Response updatePayBillStatusWithInvalidData(int timesheetId, Object invalidRequest, String authToken) {
                return RestClient.doPatch("application/json", timesheetBaseURL,
                        "/timesheets/invoices/" + timesheetId + "/pay-bill-status",
                        authToken, null, null, true, invalidRequest);
        }

        public Response validateInvoiceTimesheetID(List<Integer> timesheetIds, String authToken) {
                ValidateTimeLogsRequest validateTimeLogsRequest = new ValidateTimeLogsRequest();
                validateTimeLogsRequest.setTimesheetIds(timesheetIds);

                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/invoices/validate",
                                authToken, null, true, validateTimeLogsRequest);
        }

        public Response getInvoiceTemplate(String authToken) {
                return RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates/pay-bill",
                                authToken, null, true, null);
        }

        public Response postTimesheetsInvoices(TimesheetsInvoiceDataRequest request, String authToken) {
                return RestClient.doPost("JSON", invoiceServiceURL, "timesheets/invoices", authToken, null, true,
                                request);
        }

        public void approveTimesheet(int timesheetID, String albatrossAuthToken) {
                ApproveTimesheetRequest approveRequest1 = buildApproveTimesheetRequest(4);
                Response approveResponse1 = approveTimesheet(timesheetID, approveRequest1, albatrossAuthToken);
                assertThat("Approve timesheet for job1 should return 201", approveResponse1.statusCode(), is(201));
        }

        public Response setTimesheetToApproved(int timesheetId, String authToken) {
                ApproveTimesheetRequest approveRequest = new ApproveTimesheetRequest();
                approveRequest.setApprovalStatus(4);
                approveRequest.setRemark("Approved by filter test");
                return approveTimesheet(timesheetId, approveRequest, authToken);
        }

        public Response setTimesheetToRejected(int timesheetId, String authToken) {
                ApproveTimesheetRequest rejectRequest = new ApproveTimesheetRequest();
                rejectRequest.setApprovalStatus(3);
                rejectRequest.setRemark("Rejected by filter test");
                return approveTimesheet(timesheetId, rejectRequest, authToken);
        }

        protected Map<String, Object> getDefaultFilterTestTimesheetConfig() {
                Map<String, Object> config = new HashMap<>();
                config.put("dayPattern", "[mon,tue,wed,thu,fri]");
                config.put("regularHours", "9:00-17:00");
                config.put("rulesApplied", "Regular Hours: 1x");
                config.put("payRate", 20.0);
                config.put("billRate", 30.0);
                config.put("breakBillable", "No");
                config.put("jobStartDate", 1751328000L);
                config.put("jobEndDate", 1753920000L);
                config.put("timesheetFrequency", 2);
                config.put("timesheetStartDay", 1);
                config.put("payCurrencyId", 53);
                config.put("billCurrencyId", 53);
                config.put("breakTimeThreshold", 0);
                return config;
        }

        public int createTimesheetWithTimeLogs(int jobId, int candidateId, int timesheetFrequency, String authToken) {
                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                authToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START,
                                DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);
                JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");
                List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots, timesheetFrequency);

                addTimeSheet(jobId, Arrays.asList(candidateId), timesheetDates, authToken);
                Response getAllTimesheetsResponse = getAllTimesheets(jobId, candidateId, 1, 100, authToken);
                assertThat("Get all timesheets should return 200", getAllTimesheetsResponse.statusCode(), is(200));

                JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse.jsonPath();
                int timesheetID = ((Number) getAllTimesheetsJsonPath.get("data[0].id")).intValue();

                Response timeLogsResponse = getTimeSheetTimeLogs(timesheetID, authToken);
                assertThat("Get time logs should return 200", timeLogsResponse.statusCode(), is(200));

                JsonPath timeLogsJsonPath = timeLogsResponse.jsonPath();
                List<Map<String, Object>> timeLogs = timeLogsJsonPath.getList("data.timeLogs");
                List<TimeLog> timeLogsList = generateTimelogIDLists(timeLogs, timesheetID);

                SubmitTimeLogsRequest submitRequest = new SubmitTimeLogsRequest();
                submitRequest.setTimeLogs(timeLogsList);
                submitTimeLogsForTimesheet(submitRequest, authToken);

                return timesheetID;
        }

        
        protected void validateInvoiceResponseMeta(Response response, JsonPath jsonPath,
                        boolean shouldValidateSuccess) {
                if (shouldValidateSuccess) {
                        assertThat("Status code should be 200", response.statusCode(), is(200));
                        assertThat("Meta message should match",
                                        jsonPath.getString("meta.message"),
                                        is("Timesheet validation completed successfully"));
                        assertThat("Response type context should be successful",
                                        jsonPath.getString("meta.responseType.context"),
                                        is("Request is successful"));
                }
                assertThat("Request UUID should not be null",
                                jsonPath.getString("meta.requestUuid"),
                                is(notNullValue()));
                assertThat("Timestamp should not be null",
                                jsonPath.getString("meta.timestamp"),
                                is(notNullValue()));
        }


        protected int findTimesheetIndexByID(JsonPath jsonPath, int timesheetId) {
                List<Map<String, Object>> timesheetData = jsonPath.getList("data.timesheetInvoicePreviewData");
                for (int i = 0; i < timesheetData.size(); i++) {
                        int tsId = ((Number) timesheetData.get(i).get("timesheetId")).intValue();
                        if (tsId == timesheetId) {
                                return i;
                        }
                }
                return -1;
        }

   
        protected void validateTimesheetCommonFields(JsonPath jsonPath, int index) {
                assertThat("Timesheet " + index + " should have timesheet ID",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].timesheetId"),
                                is(greaterThan(0)));
                assertThat("Timesheet " + index + " should have timesheet period with start date",
                                jsonPath.getLong("data.timesheetInvoicePreviewData[" + index
                                                + "].timesheetPeriod.timesheetStartDate"),
                                is(greaterThan(0L)));
                assertThat("Timesheet " + index + " should have timesheet period with end date",
                                jsonPath.getLong("data.timesheetInvoicePreviewData[" + index
                                                + "].timesheetPeriod.timesheetEndDate"),
                                is(greaterThan(0L)));
                validateTimesheetCurrencyFields(jsonPath, index);
                validateTimesheetContractorFields(jsonPath, index);
                validateTimesheetJobFields(jsonPath, index);
        }

 
        protected void validateTimesheetCurrencyFields(JsonPath jsonPath, int index) {
                assertThat("Timesheet " + index + " currency ID should be 53 (INR)",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].currencyId"),
                                is(53));
                assertThat("Timesheet " + index + " bill currency symbol should be ₹",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index
                                                + "].billCurrencySymbol"),
                                is("₹"));
                assertThat("Timesheet " + index + " bill currency code should be INR",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].billCurrencyCode"),
                                is("INR"));
        }

  
        protected void validateTimesheetContractorFields(JsonPath jsonPath, int index) {
                assertThat("Timesheet " + index + " contractor name should not be empty",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].contractorName"),
                                is(not(emptyOrNullString())));
                assertThat("Timesheet " + index + " contractor owner ID should be valid",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].contractorOwnerId"),
                                is(greaterThan(0)));
                assertThat("Timesheet " + index + " contractor slug should not be empty",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].contractorSlug"),
                                is(not(emptyOrNullString())));
                assertThat("Timesheet " + index + " contractor profile pic URL should not be empty",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index
                                                + "].contractorProfilePicUrl"),
                                is(not(emptyOrNullString())));
                assertThat("Timesheet " + index + " contractor serial number should be valid",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index
                                                + "].contractorSerialNumber"),
                                is(greaterThan(0)));
                assertThat("Timesheet " + index + " contractor job assignment ID should be valid",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index
                                                + "].contractorJobAssignmentId"),
                                is(greaterThan(0)));
                assertThat("Timesheet " + index + " contractor ID should be valid",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].contractorId"),
                                is(greaterThan(0)));
        }

        protected void validateTimesheetJobFields(JsonPath jsonPath, int index) {
                assertThat("Timesheet " + index + " job slug should not be empty",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].jobSlug"),
                                is(not(emptyOrNullString())));
                assertThat("Timesheet " + index + " job ID should be valid",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].jobId"),
                                is(greaterThan(0)));
        }

        protected void validateTimesheetAssociations(JsonPath jsonPath, int index) {
                // Validate associations exist
                assertThat("Timesheet " + index + " should have contractor associations (key 5)",
                                jsonPath.getList("data.timesheetInvoicePreviewData[" + index + "].associations.5"),
                                is(notNullValue()));
                assertThat("Timesheet " + index + " contractor associations should not be empty",
                                jsonPath.getList("data.timesheetInvoicePreviewData[" + index + "].associations.5")
                                                .size(),
                                is(greaterThan(0)));
                assertThat("Timesheet " + index + " should have job associations (key 4)",
                                jsonPath.getList("data.timesheetInvoicePreviewData[" + index + "].associations.4"),
                                is(notNullValue()));
                assertThat("Timesheet " + index + " job associations should not be empty",
                                jsonPath.getList("data.timesheetInvoicePreviewData[" + index + "].associations.4")
                                                .size(),
                                is(greaterThan(0)));
                assertThat("Timesheet " + index + " should have company associations (key 3)",
                                jsonPath.getList("data.timesheetInvoicePreviewData[" + index + "].associations.3"),
                                is(notNullValue()));
                assertThat("Timesheet " + index + " company associations should not be empty",
                                jsonPath.getList("data.timesheetInvoicePreviewData[" + index + "].associations.3")
                                                .size(),
                                is(greaterThan(0)));

                // Validate contractor ID in associations matches the contractorId
                List<Integer> contractorAssociations = jsonPath
                                .getList("data.timesheetInvoicePreviewData[" + index + "].associations.5");
                int contractorId = jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].contractorId");
                assertThat("Timesheet " + index + " contractor associations should contain contractorId",
                                contractorAssociations,
                                hasItem(contractorId));

                // Validate job ID in associations matches the jobId
                List<Integer> jobAssociations = jsonPath
                                .getList("data.timesheetInvoicePreviewData[" + index + "].associations.4");
                int jobIdFromResponse = jsonPath.getInt("data.timesheetInvoicePreviewData[" + index + "].jobId");
                assertThat("Timesheet " + index + " job associations should contain jobId",
                                jobAssociations,
                                hasItem(jobIdFromResponse));
        }

        protected void validateDifferentCompanyTimesheet(JsonPath jsonPath, int index) {
                assertThat("Different company timesheet should have error key 'different_company'",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].errorKey"),
                                is("different_company"));
                assertThat("Different company timesheet approval status should be 3 or 4",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index
                                                + "].timesheetApprovalStatusTypeId"),
                                anyOf(is(3), is(4)));
                assertThat("Different company timesheet bill amount should be greater than 0",
                                jsonPath.getDouble("data.timesheetInvoicePreviewData[" + index + "].billAmount"),
                                is(greaterThan(0.0)));
        }

        protected void validateNotApprovedTimesheet(JsonPath jsonPath, int index, boolean expectZeroBillAmount) {
                assertThat("Not approved timesheet should have error key 'not_approved'",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].errorKey"),
                                is("not_approved"));
                assertThat("Not approved timesheet approval status should be 1, 2, or 3",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index
                                                + "].timesheetApprovalStatusTypeId"),
                                anyOf(is(1), is(2), is(3)));
                if (expectZeroBillAmount) {
                        assertThat("Not approved timesheet bill amount should be 0.0",
                                        jsonPath.getDouble(
                                                        "data.timesheetInvoicePreviewData[" + index + "].billAmount"),
                                        is(0.0));
                } else {
                        assertThat("Not approved timesheet bill amount should be greater than 0",
                                        jsonPath.getDouble(
                                                        "data.timesheetInvoicePreviewData[" + index + "].billAmount"),
                                        is(greaterThan(0.0)));
                }
        }

        protected void validateApprovedTimesheet(JsonPath jsonPath, int index) {
                assertThat("Approved timesheet should have empty error key",
                                jsonPath.getString("data.timesheetInvoicePreviewData[" + index + "].errorKey"),
                                is(emptyOrNullString()));
                assertThat("Approved timesheet approval status should be 3 or 4",
                                jsonPath.getInt("data.timesheetInvoicePreviewData[" + index
                                                + "].timesheetApprovalStatusTypeId"),
                                anyOf(is(3), is(4)));
                assertThat("Approved timesheet bill amount should be greater than 0",
                                jsonPath.getDouble("data.timesheetInvoicePreviewData[" + index + "].billAmount"),
                                is(greaterThan(0.0)));
        }

        protected int[] findTimesheetIndicesByIDs(JsonPath jsonPath, int timesheetId1, int timesheetId2) {
                int[] indices = { -1, -1 };
                List<Map<String, Object>> timesheetData = jsonPath.getList("data.timesheetInvoicePreviewData");
                for (int i = 0; i < timesheetData.size(); i++) {
                        int tsId = ((Number) timesheetData.get(i).get("timesheetId")).intValue();
                        if (tsId == timesheetId1) {
                                indices[0] = i;
                        } else if (tsId == timesheetId2) {
                                indices[1] = i;
                        }
                }
                return indices;
        }

        protected void validateDifferentCompanies(JsonPath jsonPath, int index1, int index2) {
                List<Integer> companyIds1 = jsonPath.getList("data.timesheetInvoicePreviewData[" + index1
                                + "].associations.3");
                List<Integer> companyIds2 = jsonPath.getList("data.timesheetInvoicePreviewData[" + index2
                                + "].associations.3");
                boolean hasDifferentCompany = false;
                for (Integer companyId1 : companyIds1) {
                        if (!companyIds2.contains(companyId1)) {
                                hasDifferentCompany = true;
                                break;
                        }
                }
                assertThat("Timesheets should belong to different companies", hasDifferentCompany, is(true));
        }

        protected int[] getErrorIndices(JsonPath jsonPath, int index1, int index2) {
                int[] errorIndices = { -1, -1, -1 }; // [differentCompany, notApproved, approved]
                String errorKey1 = jsonPath.getString("data.timesheetInvoicePreviewData[" + index1 + "].errorKey");
                String errorKey2 = jsonPath.getString("data.timesheetInvoicePreviewData[" + index2 + "].errorKey");

                if ("different_company".equals(errorKey1)) {
                        errorIndices[0] = index1;
                        if ("not_approved".equals(errorKey2)) {
                                errorIndices[1] = index2;
                        } else if (errorKey2 == null || errorKey2.isEmpty()) {
                                errorIndices[2] = index2;
                        }
                } else if ("not_approved".equals(errorKey1)) {
                        errorIndices[1] = index1;
                        if ("different_company".equals(errorKey2)) {
                                errorIndices[0] = index2;
                        } else if (errorKey2 == null || errorKey2.isEmpty()) {
                                errorIndices[2] = index2;
                        }
                } else if (errorKey1 == null || errorKey1.isEmpty()) {
                        errorIndices[2] = index1;
                        if ("different_company".equals(errorKey2)) {
                                errorIndices[0] = index2;
                        } else if ("not_approved".equals(errorKey2)) {
                                errorIndices[1] = index2;
                        }
                }

                return errorIndices;
        }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadPayBillStatusPayloadFromJson() {
        try {
            String jsonContent = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get("src/test/resources/privateApi/contractStaffing/UpdatePayBillStatusPayload.json")));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(jsonContent, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON payload: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> createPayBillStatusPayload(int payBillType, int payStatusId, String payoutNumber,
            Long payoutPaidOn) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("payBillType", payBillType);
        payload.put("payStatusId", payStatusId);
        payload.put("payoutNumber", payoutNumber);
        payload.put("payoutPaidOn", payoutPaidOn);
        return payload;
    }

    protected TimesheetSettings createCustomTimesheetSettings(int jobId, int candidateId, int agencyId,
        int timesheetFrequency, int isReimbursementEnabled, String startDate, String endDate) {
        // Create Approvers
        Approvers approvers = new Approvers();
        approvers.setAgencyIds(Arrays.asList(agencyId));
        approvers.setClientIds(Arrays.asList());

        long startEpochTime = getEpochTimeForRelativeDate(startDate);
        long endEpochTime = getEpochTimeForRelativeDate(endDate);
        // Create TimesheetSettings
        TimesheetSettings timesheetSettings = new TimesheetSettings();
        timesheetSettings.setJobStartDate(startEpochTime);
        timesheetSettings.setJobEndDate(endEpochTime);
        timesheetSettings.setCustomRules(createDefaultCustomRules());
        timesheetSettings.setTimesheetFrequency(timesheetFrequency);
        timesheetSettings.setTimesheetStartDay(1);
        timesheetSettings.setApprovers(approvers);
        timesheetSettings.setPayCurrencyId(53);
        timesheetSettings.setPayRate(5000);
        timesheetSettings.setBillCurrencyId(53);
        timesheetSettings.setBillRate(6000);
        timesheetSettings.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5, 6));
        timesheetSettings.setWorkLogType(2);
        timesheetSettings.setCalculateBreakTime(false);
        timesheetSettings.setApprovers(approvers);
        timesheetSettings.setPayCurrencyId(53);
        timesheetSettings.setPayRate(5000);
        timesheetSettings.setBillCurrencyId(53);
        timesheetSettings.setBillRate(6000);
        timesheetSettings.setWorkDayIds(Arrays.asList(1, 2, 3, 4, 5, 6));
        timesheetSettings.setWorkLogType(2);
        timesheetSettings.setCalculateBreakTime(false);
        timesheetSettings.setBreakTimeThreshold(0);
        timesheetSettings.setCalculateBreakTime(true);
        // See createDefaultTimesheetSettings — same @NotNull default-to-Fixed-Rate rationale.
        timesheetSettings.setCalculateChargeBy(1);
        timesheetSettings.setMarginPercentage(BigDecimal.ZERO);
        timesheetSettings.setMarkupPercentage(BigDecimal.ZERO);
        timesheetSettings.setWorkTime(Arrays.asList(0, 0, 0, 0, 0, 0));
        timesheetSettings.setWorkStartTime(Arrays.asList(32400, 32400, 32400, 32400, 32400, 32400));
        timesheetSettings.setWorkEndTime(Arrays.asList(61200, 61200, 61200, 61200, 61200, 61200));
        timesheetSettings.setUpdatedOn(null);
        timesheetSettings.setUpdatedBy(null);
        timesheetSettings.setEnabledOn(null);
        timesheetSettings.setEnabledBy(null);
        timesheetSettings.setIsPreferencesModified(1);
        timesheetSettings.setJobId(jobId);
        timesheetSettings.setIsReimbursementEnabled(isReimbursementEnabled);
        timesheetSettings.setContractorIds(Arrays.asList(candidateId));
        return timesheetSettings;
    }

    public long getEpochTimeForRelativeDate(String relativeDateKey) {
        if (relativeDateKey == null || relativeDateKey.isEmpty()) {
                throw new IllegalArgumentException("relativeDateKey cannot be null or empty");
        }
        String key = relativeDateKey.trim();
        int days;
        if (key.startsWith("currPrev")) {
                days = Integer.parseInt(key.substring("currPrev".length()));
                return LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        }
        if (key.startsWith("currNext")) {
                days = Integer.parseInt(key.substring("currNext".length()));
                return LocalDate.now().plusDays(days).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        }
        throw new IllegalArgumentException(
                        "relativeDateKey must be currPrevN or currNextN (e.g. currPrev30, currNext30), got: "
                                        + relativeDateKey);
        }

        public Response uploadReimbursementDocument(String fileName, int timesheetId, String authToken) {
                UploadReimbursementDocumentRequest request = UploadReimbursementDocumentRequest.builder()
                                .fileName(fileName)
                                .timesheetId(timesheetId)
                                .build();
                return RestClient.doPost("JSON", timesheetBaseURL, "reimbursements/documents",
                                authToken, null, true, request);
        }

        public Response createReimbursement(int timesheetId, CreateReimbursementRequest request, String authToken) {
                return RestClient.doPostOnce("JSON", timesheetBaseURL,
                                "timesheets/" + timesheetId + "/reimbursements",
                                authToken, null, true, request);
        }

        public int createReimbursement(String description, double amount, String fileName, int timesheetId, String authToken) {
                // Response uploadReimbursementDocumentResponse = uploadReimbursementDocument("test.pdf", timesheetId, authToken);
                CreateReimbursementRequest request = CreateReimbursementRequest.builder()
                                .description(description)
                                .amount(amount)
                                // .documentToken(uploadReimbursementDocumentResponse.jsonPath().getString("data.documentToken"))
                                // .fileName(uploadReimbursementDocumentResponse.jsonPath().getString("data.documentFileName"))
                                .build();
                Response response = createReimbursement(timesheetId, request, authToken);
                System.out.println(response.jsonPath().prettyPrint());
                assertThat(response.statusCode(), is(201));
                JsonPath createReimbursementJsonPath = response.jsonPath();
                return createReimbursementJsonPath.getInt("data.id");
        }

        public Response updateReimbursementStatus(int timesheetId, int reimbursementId, String action, String remark, String authToken) {
                UpdateReimbursementStatusRequest request = null;
                int status = 0;
                if (action.equalsIgnoreCase("approve")) {
                        status = 2;
                        request = UpdateReimbursementStatusRequest.builder().status(status).build();
                } else if (action.equalsIgnoreCase("reject")) {
                        status = 3;
                        request = UpdateReimbursementStatusRequest.builder().status(status).remark(remark).build();
                } else {
                        status = 5;
                        request = UpdateReimbursementStatusRequest.builder().status(status).build();
                }
                return RestClient.doPatchOnce("JSON", timesheetBaseURL,
                                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId + "/status",
                                authToken, null, true, request);
        }

        
        public Response reopenReimbursement(int timesheetId, int reimbursementId, String remark, String authToken) {
                JSONObject body = new JSONObject();
                body.put("remark", remark);
                return RestClient.doPost("JSON", timesheetBaseURL,
                                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId + "/reopen",
                                authToken, null, true, body);
        }

        
        public Response updatePayableBillableForReimbursement(int timesheetId, int reimbursementId, String authToken,
                        Integer isPayable, Integer isBillable) {
                JSONObject body = new JSONObject();
                if (isPayable != null) {
                        body.put("isPayable", isPayable);
                }
                if (isBillable != null) {
                        body.put("isBillable", isBillable);
                }
                return RestClient.doPatchOnce("JSON", timesheetBaseURL,
                                "timesheets/" + timesheetId + "/reimbursements/" + reimbursementId + "/payable-billable",
                                authToken, null, true, body);
        }

        public void createContractorPortalAccountAndLogin(int entityId, String firstName, String lastName, String email, int rcrmAccountId) {
                Response response = ReaperIntegration.createContractorPortalAccountAndStoreContext(entityId, firstName, lastName, email, rcrmAccountId);
                assertThat(response.getStatusCode(), is(200));
                Response loginResponse = ReaperIntegration.vmsContractorPortalLoginAndStore(email);
                if (loginResponse.getStatusCode() != 200) {
                        System.out.println("********** loginResponse: " + loginResponse.prettyPrint());
                        loginResponse = ReaperIntegration.vmsContractorPortalLoginAndStore(email);
                }
                assertThat(loginResponse.getStatusCode(), is(200));
        }

        public void createClientPortalAccountAndLogin(int entityId, String firstName, String lastName, String email, int rcrmAccountId, int jobId, boolean vmsRcrmJobLink, String companyName, int rcrmCompanyId, String rcrmEmailID, int rcrmUserId) {
                Response response = ReaperIntegration.createClientPortalAccountAndStoreContext(entityId, firstName, lastName, email, rcrmAccountId, jobId, vmsRcrmJobLink, companyName, rcrmCompanyId, rcrmEmailID, rcrmUserId);
                assertThat(response.getStatusCode(), is(200));
                Response loginResponse = ReaperIntegration.vmsClientPortalLoginAndStore(email);
                assertThat(loginResponse.getStatusCode(), is(200));
        }

        public Response enableVmsLink(int id, String authToken) {
                JSONObject body = new JSONObject();
                body.put("key", "enable_vms_link");
                body.put("value", 1);
                body.put("tableFlag", "job");
                body.put("id", id);
                Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields",
                                authToken, null, true, body);
                assertThat("global/update-fields should succeed", response.getStatusCode(), is(200));
                assertThat("enable_vms_link should be 1",
                                response.jsonPath().getString("data.enable_vms_link"), is(String.valueOf(1)));
                return response;
        }

        public void submitCandidateUrl(int id, String authToken) {
                JSONObject body = new JSONObject();
                body.put("id", id);
                Response response = RestClient.doPost("JSON", albatrossURL, "jobs/submit-candidate-url", authToken, null, true, body);
                assertThat("Candidate URL should be submitted successfully", response.getStatusCode(), is(200));
                assertThat("Message should be 'Submit Candidate URL Generated'", response.jsonPath().getString("message"), is("Submit Candidate URL Generated"));
        }

        public List<Integer> createSingleTimesheetForValidationForPortal(int jobId, int candidateId, int timesheetFrequency,
                String authToken) {
        List<Integer> timesheetIds = new ArrayList<>();

        try {
                // Get free slots
                Response freeSlotsResponse = getTimeSheetFreeSlots(candidateId, jobId, timesheetFrequency,
                                authToken, DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_START,
                                DEFAULT_TIMESHEET_FREE_SLOTS_RANGE_END);

                if (freeSlotsResponse.statusCode() == 200) {
                        JsonPath freeSlotsJsonPath = freeSlotsResponse.jsonPath();
                        List<Map<String, Object>> freeSlots = freeSlotsJsonPath.getList("data");

                        if (!freeSlots.isEmpty()) {
                                List<TimesheetDate> timesheetDates = convertFreeSlotsToTimesheetDates(freeSlots,
                                                timesheetFrequency);

                                // Create a single timesheet
                                Response addTimesheetResponse = addTimeSheet(jobId, Arrays.asList(candidateId),
                                                Arrays.asList(timesheetDates.get(0)), authToken);

                                if (addTimesheetResponse.statusCode() == 200) {
                                        Response getAllTimesheetsResponse = getAllTimesheetsContractorPortal(1, 15, authToken);
                                        if (getAllTimesheetsResponse.statusCode() == 200) {
                                                JsonPath getAllTimesheetsJsonPath = getAllTimesheetsResponse
                                                                .jsonPath();
                                                List<Map<String, Object>> timesheets = getAllTimesheetsJsonPath
                                                                .getList("data");
                                                if (!timesheets.isEmpty()) {
                                                        int timesheetId = ((Number) timesheets.get(0).get("id"))
                                                                        .intValue();
                                                        timesheetIds.add(timesheetId);
                                                }
                                        }
                                }
                        }
                }
        } catch (Exception e) {
                throw new RuntimeException("Failed to create single timesheet dynamically", e);
        }

        return timesheetIds;
        }

        public Response getAllTimesheetsContractorPortal(int page, int size, String authToken) {
                // Create request payload
                GetTimesheetRequest getTimesheetRequest = GetTimesheetRequest.builder()
                                .sortPriorityList(Arrays.asList())
                                .build();

                // Create query parameters
                Map<String, String> queryParameters = new java.util.HashMap<>();
                queryParameters.put("page", String.valueOf(page));
                queryParameters.put("size", String.valueOf(size));

                // Make API call to get all timesheets
                return RestClient.doPost("JSON", timesheetBaseURL, "timesheets/entity/get",
                                authToken, queryParameters, true, getTimesheetRequest);
        }
}
