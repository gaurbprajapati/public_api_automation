package io.recruitcrm.CandidateService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.response.Response;
import com.qa.api.util.reaper.ReaperIntegration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DateFieldCandidateFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    String apiKey;
    commanFunction commanFunction = new commanFunction();
    Map<String, Map<String, String>> timestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        apiKey = ThreadManager.getAccountApiKey();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateFieldFilterSearchTestData", description = "Filter Search Test for Date Fields")
    public void dateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("filterValue_TYPE: ", filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);

        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        validateEntityDateField(data, filterType, filterValue, fieldName, dbField, expectedResult, "Candidate");
    }

    @DataProvider(name = "dateFieldFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateDateTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");

                testData.add(new Object[]{
                        key,
                        filterType,
                        filterValue,
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.optString("fieldType", "date"),
                        test.getString("filterValue_TYPE"),
                        test.optString("testCaseId", "")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> candidateIdMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> candidateSlugMap = new ConcurrentHashMap<>();
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = candidateEntry.getJSONObject("payload");
                        Response response = function.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        candidateIdMap.put(candidateKey, response.jsonPath().getInt("data.candidate.id"));
                        candidateSlugMap.put(candidateKey, response.jsonPath().getString("data.candidate.slug"));
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> candidateIds = new ArrayList<>(candidateIdMap.values());
            List<String> candidateSlug = new ArrayList<>(candidateSlugMap.values());

            createActivityForCandidate(candidateSlug.subList(0, candidateSlug.size() - 1));
            updateCandidatesWithTimestampScenarios(candidateIds);
            updateCandidatesWithLastActivityTimestamps(candidateIds);

        } finally {
            executor.shutdown();
        }
    }

    private void createActivityForCandidate(List<String> slugs) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(slugs.stream()
                    .map(slug -> CompletableFuture.runAsync(() -> {
                        commanFunction.createNewCallLogWithEntitySlug(baseURL, apiKey, "candidate", slug);
                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }


    private void updateCandidatesWithTimestampScenarios(List<Integer> candidateIds) {
        timestampScenarios = createTimestampScenarios();

        int candidateIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : timestampScenarios.entrySet()) {
            if (candidateIndex >= timestampScenarios.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer candidateId = candidateIds.get(candidateIndex);

            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateCandidateTimestamp(candidateId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the candidate fields timestamps");
            }
            candidateIndex++;
        }

    }


    private void updateCandidatesWithLastActivityTimestamps(List<Integer> candidateIds) {
        Map<String, Map<String, String>> lastActivityScenarios = createLastActivityTimestampScenarios();
        int candidateIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : lastActivityScenarios.entrySet()) {
            if (candidateIndex >= lastActivityScenarios.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer candidateId = candidateIds.get(candidateIndex);

            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("candidate", candidateId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the candidate last activity timestamps");
            }
            candidateIndex++;
        }
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("Added On", todayEpoch);
        todayTimestamps.put("Updated On", todayEpoch);
        todayTimestamps.put("Available From", todayEpoch);
        todayTimestamps.put("Profile Request Sent On", todayEpoch);
        todayTimestamps.put("Profile Updated By Candidate On", todayEpoch);

        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("Added On", yesterdayEpoch);
        yesterdayTimestamps.put("Updated On", yesterdayEpoch);
        yesterdayTimestamps.put("Available From", yesterdayEpoch);
        yesterdayTimestamps.put("Profile Request Sent On", yesterdayEpoch);
        yesterdayTimestamps.put("Profile Updated By Candidate On", yesterdayEpoch);

        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("Added On", thisWeekEpoch);
        thisWeekTimestamps.put("Updated On", thisWeekEpoch);
        thisWeekTimestamps.put("Available From", thisWeekEpoch);
        thisWeekTimestamps.put("Profile Request Sent On", thisWeekEpoch);
        thisWeekTimestamps.put("Profile Updated By Candidate On", thisWeekEpoch);

        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("Added On", lastWeekEpoch);
        lastWeekTimestamps.put("Updated On", lastWeekEpoch);
        lastWeekTimestamps.put("Available From", lastWeekEpoch);
        lastWeekTimestamps.put("Profile Request Sent On", lastWeekEpoch);
        lastWeekTimestamps.put("Profile Updated By Candidate On", lastWeekEpoch);

        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("Added On", thisMonthEpoch);
        thisMonthTimestamps.put("Updated On", thisMonthEpoch);
        thisMonthTimestamps.put("Available From", thisMonthEpoch);
        thisMonthTimestamps.put("Profile Request Sent On", thisMonthEpoch);
        thisMonthTimestamps.put("Profile Updated By Candidate On", thisMonthEpoch);

        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("Added On", lastMonthEpoch);
        lastMonthTimestamps.put("Updated On", lastMonthEpoch);
        lastMonthTimestamps.put("Available From", lastMonthEpoch);
        lastMonthTimestamps.put("Profile Request Sent On", lastMonthEpoch);
        lastMonthTimestamps.put("Profile Updated By Candidate On", lastMonthEpoch);

        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("Added On", thisQuarterEpoch);
        thisQuarterTimestamps.put("Updated On", thisQuarterEpoch);
        thisQuarterTimestamps.put("Available From", thisQuarterEpoch);
        thisQuarterTimestamps.put("Profile Request Sent On", thisQuarterEpoch);
        thisQuarterTimestamps.put("Profile Updated By Candidate On", thisQuarterEpoch);

        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("Added On", lastQuarterEpoch);
        lastQuarterTimestamps.put("Updated On", lastQuarterEpoch);
        lastQuarterTimestamps.put("Available From", lastQuarterEpoch);
        lastQuarterTimestamps.put("Profile Request Sent On", lastQuarterEpoch);
        lastQuarterTimestamps.put("Profile Updated By Candidate On", lastQuarterEpoch);

        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("Added On", thisYearEpoch);
        thisYearTimestamps.put("Updated On", thisYearEpoch);
        thisYearTimestamps.put("Available From", thisYearEpoch);
        thisYearTimestamps.put("Profile Request Sent On", thisYearEpoch);
        thisYearTimestamps.put("Profile Updated By Candidate On", thisYearEpoch);

        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("Added On", lastYearEpoch);
        lastYearTimestamps.put("Updated On", lastYearEpoch);
        lastYearTimestamps.put("Available From", lastYearEpoch);
        lastYearTimestamps.put("Profile Request Sent On", lastYearEpoch);
        lastYearTimestamps.put("Profile Updated By Candidate On", lastYearEpoch);

        scenarios.put("last_year_scenario", lastYearTimestamps);


        //static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("Added On", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("Updated On", "1655251200");
        staticTimestamps1.put("Available From", "1655251200");
        staticTimestamps1.put("Profile Request Sent On", "1655251200");
        staticTimestamps1.put("Profile Updated By Candidate On", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        //static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("Added On", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("Updated On", "1678406400");
        staticTimestamps2.put("Available From", "1678406400");
        staticTimestamps2.put("Profile Request Sent On", "1678406400");
        staticTimestamps2.put("Profile Updated By Candidate On", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        //static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("Added On", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticTimestamps3.put("Updated On", "1717689600");
        staticTimestamps3.put("Available From", "1717689600");
        staticTimestamps3.put("Profile Request Sent On", "1717689600");
        staticTimestamps3.put("Profile Updated By Candidate On", "1717689600");
        scenarios.put("static_date_scenario3", staticTimestamps3);

        //static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("Added On", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticTimestamps4.put("Updated On", "1739491200");
        staticTimestamps4.put("Available From", "1739491200");
        staticTimestamps4.put("Profile Request Sent On", "1739491200");
        staticTimestamps4.put("Profile Updated By Candidate On", "1739491200");
        scenarios.put("static_date_scenario4", staticTimestamps4);

        return scenarios;
    }


    private Map<String, Map<String, String>> createLastActivityTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("calllog_created_on", todayEpoch);
        todayTimestamps.put("last_communication_timestamp", todayEpoch);
        todayTimestamps.put("email_sent_on", todayEpoch);
        todayTimestamps.put("meeting_created_on", todayEpoch);
        todayTimestamps.put("message_sent_on", todayEpoch);
        todayTimestamps.put("sms_sent_on", todayEpoch);
        todayTimestamps.put("note_created_on", todayEpoch);
        scenarios.put("today_last_activity", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("calllog_created_on", yesterdayEpoch);
        yesterdayTimestamps.put("last_communication_timestamp", yesterdayEpoch);
        yesterdayTimestamps.put("email_sent_on", yesterdayEpoch);
        yesterdayTimestamps.put("meeting_created_on", yesterdayEpoch);
        yesterdayTimestamps.put("message_sent_on", yesterdayEpoch);
        yesterdayTimestamps.put("sms_sent_on", yesterdayEpoch);
        yesterdayTimestamps.put("note_created_on", yesterdayEpoch);
        scenarios.put("yesterday_last_activity", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("calllog_created_on", thisWeekEpoch);
        thisWeekTimestamps.put("last_communication_timestamp", thisWeekEpoch);
        thisWeekTimestamps.put("email_sent_on", thisWeekEpoch);
        thisWeekTimestamps.put("meeting_created_on", thisWeekEpoch);
        thisWeekTimestamps.put("message_sent_on", thisWeekEpoch);
        thisWeekTimestamps.put("sms_sent_on", thisWeekEpoch);
        thisWeekTimestamps.put("note_created_on", thisWeekEpoch);
        scenarios.put("this_week_last_activity", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("calllog_created_on", lastWeekEpoch);
        lastWeekTimestamps.put("last_communication_timestamp", lastWeekEpoch);
        lastWeekTimestamps.put("email_sent_on", lastWeekEpoch);
        lastWeekTimestamps.put("meeting_created_on", lastWeekEpoch);
        lastWeekTimestamps.put("message_sent_on", lastWeekEpoch);
        lastWeekTimestamps.put("sms_sent_on", lastWeekEpoch);
        lastWeekTimestamps.put("note_created_on", lastWeekEpoch);
        scenarios.put("last_week_last_activity", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("calllog_created_on", thisMonthEpoch);
        thisMonthTimestamps.put("last_communication_timestamp", thisMonthEpoch);
        thisMonthTimestamps.put("email_sent_on", thisMonthEpoch);
        thisMonthTimestamps.put("meeting_created_on", thisMonthEpoch);
        thisMonthTimestamps.put("message_sent_on", thisMonthEpoch);
        thisMonthTimestamps.put("sms_sent_on", thisMonthEpoch);
        thisMonthTimestamps.put("note_created_on", thisMonthEpoch);
        scenarios.put("this_month_last_activity", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("calllog_created_on", lastMonthEpoch);
        lastMonthTimestamps.put("last_communication_timestamp", lastMonthEpoch);
        lastMonthTimestamps.put("email_sent_on", lastMonthEpoch);
        lastMonthTimestamps.put("meeting_created_on", lastMonthEpoch);
        lastMonthTimestamps.put("message_sent_on", lastMonthEpoch);
        lastMonthTimestamps.put("sms_sent_on", lastMonthEpoch);
        lastMonthTimestamps.put("note_created_on", lastMonthEpoch);
        scenarios.put("last_month_last_activity", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("calllog_created_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("last_communication_timestamp", thisQuarterEpoch);
        thisQuarterTimestamps.put("email_sent_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("meeting_created_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("message_sent_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("sms_sent_on", thisQuarterEpoch);
        thisQuarterTimestamps.put("note_created_on", thisQuarterEpoch);
        scenarios.put("this_quarter_last_activity", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("calllog_created_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("last_communication_timestamp", lastQuarterEpoch);
        lastQuarterTimestamps.put("email_sent_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("meeting_created_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("message_sent_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("sms_sent_on", lastQuarterEpoch);
        lastQuarterTimestamps.put("note_created_on", lastQuarterEpoch);
        scenarios.put("last_quarter_last_activity", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("calllog_created_on", thisYearEpoch);
        thisYearTimestamps.put("last_communication_timestamp", thisYearEpoch);
        thisYearTimestamps.put("email_sent_on", thisYearEpoch);
        thisYearTimestamps.put("meeting_created_on", thisYearEpoch);
        thisYearTimestamps.put("message_sent_on", thisYearEpoch);
        thisYearTimestamps.put("sms_sent_on", thisYearEpoch);
        thisYearTimestamps.put("note_created_on", thisYearEpoch);
        scenarios.put("this_year_last_activity", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("calllog_created_on", lastYearEpoch);
        lastYearTimestamps.put("last_communication_timestamp", lastYearEpoch);
        lastYearTimestamps.put("email_sent_on", lastYearEpoch);
        lastYearTimestamps.put("meeting_created_on", lastYearEpoch);
        lastYearTimestamps.put("message_sent_on", lastYearEpoch);
        lastYearTimestamps.put("sms_sent_on", lastYearEpoch);
        lastYearTimestamps.put("note_created_on", lastYearEpoch);
        scenarios.put("last_year_last_activity", lastYearTimestamps);


        //static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticLastActivityTimestamps1 = new HashMap<>();
        staticLastActivityTimestamps1.put("calllog_created_on", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticLastActivityTimestamps1.put("last_communication_timestamp", "1655251200");
        staticLastActivityTimestamps1.put("email_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("meeting_created_on", "1655251200");
        staticLastActivityTimestamps1.put("message_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("sms_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("note_created_on", "1655251200");
        scenarios.put("static_last_activity_scenario1", staticLastActivityTimestamps1);

        //static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticLastActivityTimestamps2 = new HashMap<>();
        staticLastActivityTimestamps2.put("calllog_created_on", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticLastActivityTimestamps2.put("last_communication_timestamp", "1678406400");
        staticLastActivityTimestamps2.put("email_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("meeting_created_on", "1678406400");
        staticLastActivityTimestamps2.put("message_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("sms_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("note_created_on", "1678406400");
        scenarios.put("static_last_activity_scenario2", staticLastActivityTimestamps2);

        //static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticLastActivityTimestamps3 = new HashMap<>();
        staticLastActivityTimestamps3.put("calllog_created_on", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticLastActivityTimestamps3.put("last_communication_timestamp", "1717689600");
        staticLastActivityTimestamps3.put("email_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("meeting_created_on", "1717689600");
        staticLastActivityTimestamps3.put("message_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("sms_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("note_created_on", "1717689600");
        scenarios.put("static_last_activity_scenario3", staticLastActivityTimestamps3);

        //static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticLastActivityTimestamps4 = new HashMap<>();
        staticLastActivityTimestamps4.put("calllog_created_on", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticLastActivityTimestamps4.put("last_communication_timestamp", "1739491200");
        staticLastActivityTimestamps4.put("email_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("meeting_created_on", "1739491200");
        staticLastActivityTimestamps4.put("message_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("sms_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("note_created_on", "1739491200");
        scenarios.put("static_last_activity_scenario4", staticLastActivityTimestamps4);

        return scenarios;
    }


    /**
     * Last-activity date fields on candidate search use the candidate last-activities entity (same idea as
     * {@code contact_last_activities_t} / {@code company_last_activities_t} on contact/company date tests).
     */
    private String resolveCandidateDateEntityType(String fieldName) {
        if ("Last Call Log Added".equals(fieldName)
                || "Last Email Sent On".equals(fieldName)
                || "Last Communication".equals(fieldName)
                || "Last Meeting Added".equals(fieldName)
                || "Last LinkedIn Message Sent On".equals(fieldName)
                || "Last SMS Sent On".equals(fieldName)
                || "Last Note Created On".equals(fieldName)) {
            return "candidate_last_activities_t";
        }
        return "candidate";
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj = new JSONObject();

        if (filterType.equals("is_between")) {
            filterValueObj.put("type", "LONG_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            long startEpoch = dateToEpochSeconds(startValue);
            long endEpoch = dateToEpochSeconds(endValue);
            rangeValue.put("start", startEpoch);
            rangeValue.put("end", endEpoch);
            filterValueObj.put("value", rangeValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("is_mt") || filterType.equals("is_lt")) {
                filterValueObj.put("value", Integer.parseInt(filterValue));
            } else if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if (filterType.equals("is_equal_to") || filterType.equals("is_before") || filterType.equals("is_after")) {
                long epochValue = dateToEpochSeconds(filterValue);
                filterValueObj.put("value", epochValue);
            } else {
                filterValueObj.put("value", filterValue);
            }
        }

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "candidates");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", resolveCandidateDateEntityType(fieldName));
        filter.put("fieldType", fieldType);
        filter.put("filterValue", filterValueObj);

        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);

        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");

        payload.put("filterSearchList", filterSearchList);

        return payload;
    }






    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "dateFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Date Fields")
    public void dateFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        dateFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "dateFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] dateFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
