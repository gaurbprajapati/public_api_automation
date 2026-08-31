package io.recruitcrm.ContactService.Filters;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.DateUtil;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CompanyDateFieldCrossEntityContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String apiKey;
    String email;
    Map<String, List<JsonPath>> companyDataMap = new HashMap<>();
    Map<String, String> contactSlugToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, String> contactIdToKeyMap = new HashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companySlugToKeyMap = new HashMap<>();
    Map<String, Map<String, String>> timestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "companyDateFieldCrossEntityFilterSearchTestData", description = "Filter Search Test for Company Date Fields Cross Entity Contact")
    public void companyDateFieldCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        JSONObject companyDataByContact = getCompaniesForContacts(data);
        logContactNameAndCompany(response, data, companyDataByContact, fieldName, dbField);
        validateDateFieldCrossEntityFilteredData(companyDataByContact, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateDateFieldCrossEntityFilteredData(JSONObject companyDataByContact, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(companyDataByContact.length(), 0, "Expected empty result but response has data for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && companyDataByContact.length() == 0) {
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (String contactSlug : companyDataByContact.keySet()) {
            JSONArray contactAssociatedCompanies = companyDataByContact.getJSONArray(contactSlug);
            boolean atLeastOneMatch = false;
            for (int i = 0; i < contactAssociatedCompanies.length(); i++) {
                JSONObject company = contactAssociatedCompanies.getJSONObject(i);
                String companyDateStr = company.optString(dbField, "").trim();
                boolean matches = validateDateFieldFilteredDataBoolean(companyDateStr, filterType, filterValue, fieldName);
                if (matches) {
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!atLeastOneMatch) {
                Assert.fail("No company matched the filter for contact still coming in the response: " + contactSlug + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
        }
    }

    private boolean validateDateFieldFilteredDataBoolean(String companyDate, String filterType, String filterValue, String fieldName) {
        if (companyDate.isEmpty() || companyDate.equals("null") || companyDate.equals("0")) {
            return filterType.equals("is_empty");
        }

        try {
            LocalDate companyParsedDate = parseDate(companyDate);
            
            switch (filterType) {
                case "is":
                case "is_equal_to":
                    if (isRelativeDatePeriod(filterValue)) {
                        return isDateInPeriod(companyParsedDate, filterValue);
                    } else {
                        LocalDate filterDate = parseDate(filterValue);
                        return companyParsedDate.equals(filterDate);
                    }
                case "is_not":
                    LocalDate filterDateNot = parseDate(filterValue);
                    return !companyParsedDate.equals(filterDateNot);
                case "is_before":
                    LocalDate filterDateBefore = parseDate(filterValue);
                    return companyParsedDate.isBefore(filterDateBefore);
                case "is_after":
                    LocalDate filterDateAfter = parseDate(filterValue);
                    return companyParsedDate.isAfter(filterDateAfter);
                case "is_between":
                    String[] dates = filterValue.split(",");
                    if (dates.length != 2) {
                        return false;
                    }
                    LocalDate startDate = parseDate(dates[0].trim());
                    LocalDate endDate = parseDate(dates[1].trim());
                    return (companyParsedDate.isEqual(startDate) || companyParsedDate.isAfter(startDate)) &&
                           (companyParsedDate.isEqual(endDate) || companyParsedDate.isBefore(endDate));
                case "is_mt":
                    int days = Integer.parseInt(filterValue);
                    LocalDate cutoffDate = LocalDate.now().minusDays(days);
                    return companyParsedDate.isBefore(cutoffDate) || companyParsedDate.isEqual(cutoffDate);
                case "is_lt":
                    int daysLt = Integer.parseInt(filterValue);
                    LocalDate cutoffDateLt = LocalDate.now().minusDays(daysLt);
                    return companyParsedDate.isAfter(cutoffDateLt) || companyParsedDate.isEqual(cutoffDateLt);
                case "has_any_value":
                    return !companyDate.isEmpty() && !companyDate.equals("null") && !companyDate.equals("0");
                case "is_empty":
                    return companyDate.isEmpty() || companyDate.equals("null") || companyDate.equals("0");
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }



    public JSONObject getCompaniesForContacts(JSONArray contactData) {
        JSONObject companyResult = new JSONObject();
        for (int i = 0; i < contactData.length(); i++) {
            JSONArray contactAssociatedCompanies = new JSONArray();
            JSONObject contact = contactData.getJSONObject(i);
            String contactSlug = contact.getString("slug");
            List<JsonPath> companyList = companyDataMap.get(contactSlug);
            if (companyList != null) {
                for (JsonPath companyJsonPath : companyList) {
                    Map<String, Object> companyMap = companyJsonPath.get("data.company");
                    JSONObject company = new JSONObject(companyMap);
                    contactAssociatedCompanies.put(company);
                }
            }
            companyResult.put(contactSlug, contactAssociatedCompanies);
        }
        return companyResult;
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/contactCompany_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> companyIdMap = new HashMap<>();
        Map<String, String> companySlugMap = new HashMap<>();

        try {
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        
                        String createdBy = companyEntry.optString("createdBy", "owner");
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        Integer companyId = jsonPath.getInt("data.company.id");
                        return Map.entry(companyKey, Map.entry(slug, companyId));
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>> future : createFutures) {
                Map.Entry<String, Map.Entry<String, Integer>> entry = future.join();
                String companyKey = entry.getKey();
                String companySlug = entry.getValue().getKey();
                Integer companyId = entry.getValue().getValue();
                companyIdMap.put(companyKey, companyId);
                companySlugMap.put(companyKey, companySlug);
                companyKeyToSlugMap.put(companyKey, companySlug);
                companySlugToKeyMap.put(companySlug, companyKey);
            }

            List<String> companySlugs = new ArrayList<>(companySlugMap.values());
            List<Integer> companyIds = new ArrayList<>(companyIdMap.values());

            createMeetingsForCompanies(companySlugs);

            addContactsToCompanies(companyJson, companyIdMap);

            updateCompaniesWithTimestampScenarios(companyIds);
            updateCompaniesWithLastActivityTimestamps(companyIds);

            refreshCompanyDataMap();
        } finally {
            executor.shutdown();
        }
    }

    private void createMeetingsForCompanies(List<String> companySlugs) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(companySlugs.stream()
                    .map(slug -> CompletableFuture.runAsync(() -> {
                        function.createNewMeetingWithEntitySlug(baseURL, apiKey, "company", slug);
                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateCompaniesWithTimestampScenarios(List<Integer> companyIds) {
        timestampScenarios = createTimestampScenarios();

        int companyIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : timestampScenarios.entrySet()) {
            if (companyIndex >= timestampScenarios.size() || companyIndex >= companyIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer companyId = companyIds.get(companyIndex);

            JSONObject fieldsAndValues = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndValues.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateCompanyFields(companyId, fieldsAndValues);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the company fields timestamps for company ID: " + companyId);
            }
            companyIndex++;
        }
    }

    private void updateCompaniesWithLastActivityTimestamps(List<Integer> companyIds) {
        Map<String, Map<String, String>> lastActivityScenarios = createLastActivityTimestampScenarios();
        int companyIndex = 0;
        for (Map.Entry<String, Map<String, String>> scenario : lastActivityScenarios.entrySet()) {
            if (companyIndex >= lastActivityScenarios.size() || companyIndex >= companyIds.size()) {
                break;
            }

            Map<String, String> timestamps = scenario.getValue();
            Integer companyId = companyIds.get(companyIndex);

            JSONObject fieldsAndTimestamps = new JSONObject();
            for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
            }

            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("company", companyId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() != 200) {
                Assert.fail("Failed to update the company last activity timestamps for company ID: " + companyId);
            }
            companyIndex++;
        }
    }

    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdon", todayEpoch);
        todayTimestamps.put("updatedon", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdon", yesterdayEpoch);
        yesterdayTimestamps.put("updatedon", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdon", thisWeekEpoch);
        thisWeekTimestamps.put("updatedon", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdon", lastWeekEpoch);
        lastWeekTimestamps.put("updatedon", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdon", thisMonthEpoch);
        thisMonthTimestamps.put("updatedon", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdon", lastMonthEpoch);
        lastMonthTimestamps.put("updatedon", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdon", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedon", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdon", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedon", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdon", thisYearEpoch);
        thisYearTimestamps.put("updatedon", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("createdon", lastYearEpoch);
        lastYearTimestamps.put("updatedon", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);

        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("createdon", "1655251200");
        staticTimestamps1.put("updatedon", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdon", "1678406400");
        staticTimestamps2.put("updatedon", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        Map<String, String> staticTimestamps3 = new HashMap<>();
        staticTimestamps3.put("createdon", "1717689600");
        staticTimestamps3.put("updatedon", "1717689600");
        scenarios.put("static_date_scenario3", staticTimestamps3);

        Map<String, String> staticTimestamps4 = new HashMap<>();
        staticTimestamps4.put("createdon", "1739491200");
        staticTimestamps4.put("updatedon", "1739491200");
        scenarios.put("static_date_scenario4", staticTimestamps4);

        return scenarios;
    }

    private Map<String, Map<String, String>> createLastActivityTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("meeting_created_on", todayEpoch);
        scenarios.put("today_last_activity", todayTimestamps);

        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("meeting_created_on", yesterdayEpoch);
        scenarios.put("yesterday_last_activity", yesterdayTimestamps);

        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("meeting_created_on", thisWeekEpoch);
        scenarios.put("this_week_last_activity", thisWeekTimestamps);

        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("meeting_created_on", lastWeekEpoch);
        scenarios.put("last_week_last_activity", lastWeekTimestamps);

        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("meeting_created_on", thisMonthEpoch);
        scenarios.put("this_month_last_activity", thisMonthTimestamps);

        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("meeting_created_on", lastMonthEpoch);
        scenarios.put("last_month_last_activity", lastMonthTimestamps);

        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("meeting_created_on", thisQuarterEpoch);
        scenarios.put("this_quarter_last_activity", thisQuarterTimestamps);

        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("meeting_created_on", lastQuarterEpoch);
        scenarios.put("last_quarter_last_activity", lastQuarterTimestamps);

        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("meeting_created_on", thisYearEpoch);
        scenarios.put("this_year_last_activity", thisYearTimestamps);

        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("meeting_created_on", lastYearEpoch);
        scenarios.put("last_year_last_activity", lastYearTimestamps);

        Map<String, String> staticLastActivityTimestamps1 = new HashMap<>();
        staticLastActivityTimestamps1.put("meeting_created_on", "1655251200");
        scenarios.put("static_last_activity_scenario1", staticLastActivityTimestamps1);

        Map<String, String> staticLastActivityTimestamps2 = new HashMap<>();
        staticLastActivityTimestamps2.put("meeting_created_on", "1678406400");
        scenarios.put("static_last_activity_scenario2", staticLastActivityTimestamps2);

        Map<String, String> staticLastActivityTimestamps3 = new HashMap<>();
        staticLastActivityTimestamps3.put("meeting_created_on", "1717689600");
        scenarios.put("static_last_activity_scenario3", staticLastActivityTimestamps3);

        Map<String, String> staticLastActivityTimestamps4 = new HashMap<>();
        staticLastActivityTimestamps4.put("meeting_created_on", "1739491200");
        scenarios.put("static_last_activity_scenario4", staticLastActivityTimestamps4);

        return scenarios;
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

    private void addContactsToCompanies(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<String>> contactSlugToCompanySlugsMap = new HashMap<>();
        
        for (int i = 1; i <= 10; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, ownerAlbatrossAuthToken);
            String contactSlug = response.jsonPath().getString("data.contact.slug");
            Integer contactId = response.jsonPath().getInt("data.contact.id");
            String contactKey = "contact" + i;
            contactKeyToSlugMap.put(contactKey, contactSlug);
            contactIdToKeyMap.put(String.valueOf(contactId), contactKey);
            contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
            contactSlugToKeyMap.put(contactSlug, contactKey);
            contactSlugToCompanySlugsMap.put(contactSlug, new ArrayList<>());
        }

        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            if (contact.isEmpty()) {
                continue;
            }
            String[] contactKeys = contact.split(",");
            String contactSlugs = Arrays.stream(contactKeys)
                    .map(String::trim)
                    .map(contactKeyToSlugMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            if (contactSlugs.isEmpty()) {
                continue;
            }

            Integer companyId = companyIdMap.get(companyKey);
            String companySlug = companyKeyToSlugMap.get(companyKey);
            
            UpdateFields updateFields = new UpdateFields();
            updateFields.setKey("contactid");
            updateFields.setValue(contactSlugs);
            updateFields.setTableFlag("company");
            updateFields.setId(Collections.singletonList(companyId));
            updateFields.setAddInValues(true);
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ownerAlbatrossAuthToken, null, true, updateFields);
            linkResponse.then().statusCode(200);
            
            for (String contactKey : contactKeys) {
                String contactSlug = contactKeyToSlugMap.get(contactKey.trim());
                if (contactSlug != null && companySlug != null) {
                    contactSlugToCompanySlugsMap.get(contactSlug).add(companySlug);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : contactSlugToCompanySlugsMap.entrySet()) {
            String contactSlug = entry.getKey();
            List<String> companySlugs = entry.getValue();
            if (!companySlugs.isEmpty()) {
                List<JsonPath> companyList = new ArrayList<>();
                for (String companySlug : companySlugs) {
                    Response companyResponse = getCompany(companySlug);
                    JsonPath companyJsonPath = companyResponse.jsonPath();
                    companyList.add(companyJsonPath);
                }
                synchronized (companyDataMap) {
                    companyDataMap.put(contactSlug, companyList);
                }
            }
        }
    }

    public Response getContact(String contactSlug) {
        String basePath = "/contacts/{contactSlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactSlug", contactSlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    public Response getCompany(String companySlug) {
        String basePath = "/companies/{companySlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companySlug", companySlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    private void refreshCompanyDataMap() {
        synchronized (companyDataMap) {
            for (Map.Entry<String, List<JsonPath>> entry : companyDataMap.entrySet()) {
                String contactSlug = entry.getKey();
                List<JsonPath> existingCompanies = entry.getValue();
                if (existingCompanies == null || existingCompanies.isEmpty()) {
                    continue;
                }

                List<JsonPath> refreshed = new ArrayList<>();
                for (JsonPath companyJsonPath : existingCompanies) {
                    try {
                        String companySlug = companyJsonPath.getString("data.company.slug");
                        if (companySlug == null || companySlug.isEmpty()) {
                            refreshed.add(companyJsonPath);
                            continue;
                        }
                        Response updatedCompanyResponse = getCompany(companySlug);
                        refreshed.add(updatedCompanyResponse.jsonPath());
                    } catch (Exception e) {
                        refreshed.add(companyJsonPath);
                    }
                }
                companyDataMap.put(contactSlug, refreshed);
            }
        }
    }

    private void logContactNameAndCompany(Response response, JSONArray contactData, JSONObject companyDataByContact, String fieldName, String dbField) {
        FilterSearchReporter.logInfo("<b>📋 Contact - Company Information:</b>");
        
        if (contactData != null && companyDataByContact != null) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
            logMessage.append("<code>");
            
            for (int i = 0; i < contactData.length(); i++) {
                JSONObject contact = contactData.getJSONObject(i);
                String contactSlug = contact.getString("slug");
                JSONArray associatedCompanies = companyDataByContact.optJSONArray(contactSlug);
                
                String firstName = contact.optString("firstname", "").trim();
                String lastName = contact.optString("lastname", "").trim();
                String contactName = (firstName + " " + lastName).trim();
                if (contactName.isEmpty()) {
                    contactName = "Unknown";
                }
                logMessage.append("Contact: ").append(contactName).append("\n");
                
                if (associatedCompanies != null && associatedCompanies.length() > 0) {
                    for (int j = 0; j < associatedCompanies.length(); j++) {
                        JSONObject company = associatedCompanies.getJSONObject(j);
                        String companyName = company.optString("companyname", "N/A");
                        String companyFieldValue = String.valueOf(company.opt(dbField)).trim();
                        if (companyFieldValue.isEmpty() || companyFieldValue.equals("null") || companyFieldValue.equals("0")) {
                            companyFieldValue = "N/A";
                        }
                        logMessage.append("  Company ").append(j + 1).append(": ").append(companyName);
                        logMessage.append(" | ").append(fieldName).append(": ").append(companyFieldValue).append("\n");
                    }
                } else {
                    logMessage.append("  No companies found\n");
                }
                logMessage.append("\n");
            }
            
            logMessage.append("</code></pre>");
            FilterSearchReporter.logInfo(logMessage.toString());
        }
    }

    @DataProvider(name = "companyDateFieldCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] companyDateFieldCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyDateTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String testCaseId = test.optString("testCaseId", "");
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), testCaseId});
            }
        }
        return testData.toArray(new Object[0][]);
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
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
        filter.put("isCrossEntity", true);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        if (fieldName.equals("Last Meeting Added")) {
            filter.put("entityType", "company_last_activities_t");
        } else {
            filter.put("entityType", "company");
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
}
