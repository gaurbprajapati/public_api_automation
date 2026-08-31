package io.recruitcrm.ContactService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
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
import java.util.stream.Collectors;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DateFieldContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String apiKey;
    String albatrossAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, Map<String, String>> timestampScenarios;
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        albatrossAuthToken = ownerAlbatrossAuthToken;
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createCompanies();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "dateFieldFilterContactSearchTestData", description = "Filter Search Test for Date Fields")
    public void dateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        validateEntityDateField(data, filterType, filterValue, fieldName, dbField, expectedResult, "Contact");
    }

    @DataProvider(name = "dateFieldFilterContactSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDateTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");

                testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            // Create only companies 1-5
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String companyKey = "company" + i;
                if (companyJson.has(companyKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }
                        
                        Integer companyId = Integer.parseInt(companyIdStr);
                        
                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return ownerAlbatrossAuthToken;
        }
    }

    public void createTestData() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> contactIdMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> contactSlugMap = new ConcurrentHashMap<>();
        try {
            List<CompletableFuture<Void>> createFutures = contactJson.keySet().stream()
                    .filter(key -> key.startsWith("contact"))
                    .map(contactKey -> CompletableFuture.runAsync(() -> {
                        JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                        JSONObject payload = contactEntry.getJSONObject("payload");
                        String createdBy = contactEntry.has("createdBy") ? contactEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        // Add delay to avoid contacts getting created with same ID
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        
                        // Replace company placeholders in selectedcompanies
                        if (payload.has("selectedcompanies")) {
                            JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                            for (int i = 0; i < selectedCompanies.length(); i++) {
                                JSONObject companyInfo = selectedCompanies.getJSONObject(i);
                                String slugPlaceholder = companyInfo.optString("slug", "");
                                String idPlaceholder = companyInfo.optString("id", "");
                                
                                // Extract company key from placeholder like {company1_slug} or {company1_id}
                                String companyKey = null;
                                if (slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                                    companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6); // Remove { and _slug}
                                } else if (idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                                    companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4); // Remove { and _id}
                                }
                                
                                if (companyKey != null) {
                                    String actualSlug = companyKeyToSlugMap.get(companyKey);
                                    String actualId = companyKeyToIdMap.get(companyKey);
                                    
                                    if (actualSlug != null && actualId != null) {
                                        companyInfo.put("slug", actualSlug);
                                        companyInfo.put("id", actualId);
                                    }
                                }
                            }
                        }
                        
                        Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", authToken, null, true, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        
                        if (contactIdStr == null) {
                            System.out.println("Skipping " + contactKey + " - contact ID is null");
                            return;
                        }
                        
                        Integer contactId = Integer.parseInt(contactIdStr);
                        String slug = jsonPath.getString("data.contact.slug");
                        
                        synchronized (contactIdMap) {
                            contactIdMap.put(contactKey, contactId);
                        }
                        synchronized (contactSlugMap) {
                            contactSlugMap.put(contactKey, slug);
                        }
                    }, executor))
                    .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();

            List<Integer> contactIds = new ArrayList<>(contactIdMap.values());
            List<String> contactSlugs = new ArrayList<>(contactSlugMap.values());

            createActivityForContact(contactSlugs);
            updateContactsWithTimestampScenarios(contactIds);
            updateContactsWithLastActivityTimestamps(contactIds);

        } finally {
            executor.shutdown();
        }
    }

    private void createActivityForContact(List<String> slugs) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(slugs.stream()
                    .map(slug -> CompletableFuture.runAsync(() -> {
                        function.createNewCallLogWithEntitySlug(baseURL, apiKey, "contact", slug);
                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateContactsWithTimestampScenarios(List<Integer> contactIds) {
        timestampScenarios = createTimestampScenarios();

        int contactIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : timestampScenarios.entrySet()) {
            if (contactIndex >= timestampScenarios.size() || contactIndex >= contactIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer contactId = contactIds.get(contactIndex);

            JSONObject fieldsAndValues = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndValues.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateContactFields(contactId, fieldsAndValues);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the contact fields timestamps");
            }
            contactIndex++;
        }
    }

    private void updateContactsWithLastActivityTimestamps(List<Integer> contactIds) {
        Map<String, Map<String, String>> lastActivityScenarios = createLastActivityTimestampScenarios();
        int contactIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : lastActivityScenarios.entrySet()) {
            if (contactIndex >= lastActivityScenarios.size() || contactIndex >= contactIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer contactId = contactIds.get(contactIndex);

            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("contact", contactId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the contact last activity timestamps");
            }
            contactIndex++;
        }
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdon", todayEpoch);
        todayTimestamps.put("updatedon", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdon", yesterdayEpoch);
        yesterdayTimestamps.put("updatedon", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdon", thisWeekEpoch);
        thisWeekTimestamps.put("updatedon", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdon", lastWeekEpoch);
        lastWeekTimestamps.put("updatedon", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdon", thisMonthEpoch);
        thisMonthTimestamps.put("updatedon", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdon", lastMonthEpoch);
        lastMonthTimestamps.put("updatedon", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdon", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedon", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdon", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedon", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdon", thisYearEpoch);
        thisYearTimestamps.put("updatedon", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("createdon", lastYearEpoch);
        lastYearTimestamps.put("updatedon", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("createdon", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("updatedon", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdon", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("updatedon", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("createdon", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticTimestamps3.put("updatedon", "1717689600");
        scenarios.put("static_date_scenario3", staticTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("createdon", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticTimestamps4.put("updatedon", "1739491200");
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
        lastYearTimestamps.put("message_sent_on", lastYearEpoch);
        lastYearTimestamps.put("sms_sent_on", lastYearEpoch);
        lastYearTimestamps.put("note_created_on", lastYearEpoch);
        scenarios.put("last_year_last_activity", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticLastActivityTimestamps1 = new HashMap<>();
        staticLastActivityTimestamps1.put("calllog_created_on", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticLastActivityTimestamps1.put("last_communication_timestamp", "1655251200");
        staticLastActivityTimestamps1.put("email_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("message_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("sms_sent_on", "1655251200");
        staticLastActivityTimestamps1.put("note_created_on", "1655251200");
        scenarios.put("static_last_activity_scenario1", staticLastActivityTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticLastActivityTimestamps2 = new HashMap<>();
        staticLastActivityTimestamps2.put("calllog_created_on", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticLastActivityTimestamps2.put("last_communication_timestamp", "1678406400");
        staticLastActivityTimestamps2.put("email_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("message_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("sms_sent_on", "1678406400");
        staticLastActivityTimestamps2.put("note_created_on", "1678406400");
        scenarios.put("static_last_activity_scenario2", staticLastActivityTimestamps2);

        // Static date scenario 3 - Current year data (June 6, 2024)
        Map<String, String> staticLastActivityTimestamps3 = new HashMap<>();
        staticLastActivityTimestamps3.put("calllog_created_on", "1717689600");  // 2024-06-06 00:00:00 UTC
        staticLastActivityTimestamps3.put("last_communication_timestamp", "1717689600");
        staticLastActivityTimestamps3.put("email_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("message_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("sms_sent_on", "1717689600");
        staticLastActivityTimestamps3.put("note_created_on", "1717689600");
        scenarios.put("static_last_activity_scenario3", staticLastActivityTimestamps3);

        // Static date scenario 4 - Future planning data (February 14, 2025)
        Map<String, String> staticLastActivityTimestamps4 = new HashMap<>();
        staticLastActivityTimestamps4.put("calllog_created_on", "1739491200");  // 2025-02-14 00:00:00 UTC
        staticLastActivityTimestamps4.put("last_communication_timestamp", "1739491200");
        staticLastActivityTimestamps4.put("email_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("message_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("sms_sent_on", "1739491200");
        staticLastActivityTimestamps4.put("note_created_on", "1739491200");
        scenarios.put("static_last_activity_scenario4", staticLastActivityTimestamps4);

        return scenarios;
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Create filterValue object with type and value
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
                // For date fields, has_any_value and is_empty use 0 as value with LONG type
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if (filterType.equals("is_equal_to") || filterType.equals("is_before") || filterType.equals("is_after")) {
                long epochValue = dateToEpochSeconds(filterValue);
                filterValueObj.put("value", epochValue);
            } else {
                filterValueObj.put("value", filterValue);
            }
        }   
        
        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        if (fieldName.equals("Last Call Log Added") || fieldName.equals("Last Communication") || 
            fieldName.equals("Last Linkedin Message Sent On") || fieldName.equals("Last SMS Sent On") || 
            fieldName.equals("Last Email Sent On") || fieldName.equals("Last Note Added On")) {
            filter.put("entityType", "contact_last_activities_t");
        } else {
            filter.put("entityType", "contact");
        }
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
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "dateFieldFilterContactSearchSmokeTestData", description = "[Smoke] Filter Search Test for Date Fields")
    public void dateFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        dateFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "dateFieldFilterContactSearchSmokeTestData", parallel = true)
    public Object[][] dateFieldFilterContactSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
