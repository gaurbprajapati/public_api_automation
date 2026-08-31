package io.recruitcrm.CandidateService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DropdownFieldsCandidateFilterSearchTest extends FilterSearchBaseTest {

    /**
     * Standard gender option ids for filter payloads (INTEGER_LIST). JSON placeholders: {Male}, {Female}, {Unknown}, {Non binary}, {Prefer Not to Say}.
     */
    private static final Map<String, String> GENDER_LABEL_TO_ID;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("Unknown", "0");
        m.put("Male", "1");
        m.put("Female", "2");
        m.put("Non binary", "3");
        m.put("Prefer Not to Say", "4");
        GENDER_LABEL_TO_ID = Collections.unmodifiableMap(m);
    }

    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String accountOwnerAPIKey;
    String email;
    Map<String, String> candidateKeyToIdMap = new HashMap<>();
    Map<String, String> candidateIdToKeyMap = new HashMap<>();
    Map<String, String> userMap = new HashMap<>();
    Map<String, String> teamMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        userMap = createUserMap();
        teamMap = createTeamMap();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "ownerDropdownFieldFilterSearchTestData", description = "Filter Search Test for Owner Dropdown Field")
    public void ownerDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "ownername");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 15, "Candidate");
    }


    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "createdByDropdownFieldFilterSearchTestData", description = "Filter Search Test for Created By Dropdown Field")
    public void createdByDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "creatorname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 15, "Candidate");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "genderDropdownFieldFilterSearchTestData", description = "Filter Search Test for Gender Dropdown Field")
    public void genderDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "genderid");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 15, "Candidate");
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> createFutures = candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = new JSONObject(candidateEntry.getJSONObject("payload").toString());
                        String createdBy = candidateEntry.has("createdBy") ? candidateEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String candidateIdStr = jsonPath.getString("data.candidate.id");
                        if (candidateIdStr == null) {
                            return;
                        }
                        int candidateId = Integer.parseInt(candidateIdStr);
                        synchronized (candidateKeyToIdMap) {
                            candidateKeyToIdMap.put(candidateKey.toLowerCase(), String.valueOf(candidateId));
                        }
                        synchronized (candidateIdToKeyMap) {
                            candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey.toLowerCase());
                        }
                    }, executor))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return albatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return albatrossAuthToken;
        }
    }

    public Map<String, String> createUserMap() {
        Map<String, String> map = new HashMap<>();
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        map.put("owner", user.get("[0].id").toString());
        map.put("admin", user.get("[1].id").toString());
        map.put("restrictedTeamMember", user.get("[2].id").toString());
        map.put("teamMember", user.get("[3].id").toString());
        return map;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> map = new HashMap<>();
        ArrayList<String> userId = new ArrayList<>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("teamMember")));
        Response response = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId);
        response.then().statusCode(200);
        Response team = function.getTeams(baseURL, accountOwnerAPIKey);
        String teamId = team.jsonPath().getString("[0].team_id");
        map.put("team", teamId);
        return map;
    }

    @DataProvider(name = "ownerDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] ownerDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Owner")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[] { key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"),
                            test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "") });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "createdByDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] createdByDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Created By")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[] { key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"),
                            test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "") });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "genderDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] genderDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Gender")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[] { key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"),
                            test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "") });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        String processedFilterValue = processFilterValue(filterValue, fieldName);
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("ENTITY_ASSOCIATION")) {
            filterValueObj = entityAssociationFilterValue(processedFilterValue);
        } else if (filterValue_TYPE.equals("INTEGER_LIST")) {
            filterValueObj = integerListFilterValue(processedFilterValue);
        } else if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValueWithIgnore(processedFilterValue);
        } else if (filterValue_TYPE.equals("STRING")) {
            filterValueObj = stringFilterValue(processedFilterValue);
        } else {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
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
        filter.put("entityType", "candidate");
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

    private String processFilterValue(String filterValue, String fieldName) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }
        String processedValue = filterValue;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String fieldKey = matcher.group(1);
            String actualValue = null;
            if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            } else if (fieldKey.equals("owner") || fieldKey.equals("admin") || fieldKey.equals("restrictedTeamMember") || fieldKey.equals("teamMember")) {
                actualValue = userMap.get(fieldKey);
            } else if (GENDER_LABEL_TO_ID.containsKey(fieldKey)) {
                actualValue = GENDER_LABEL_TO_ID.get(fieldKey);
            }
            if (actualValue != null) {
                processedValue = processedValue.replace(placeholder, actualValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder + " in field: " + fieldName);
            }
        }
        return processedValue;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "ownerDropdownFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Owner Dropdown Field")
    public void ownerDropdownFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        ownerDropdownFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "ownerDropdownFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] ownerDropdownFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(ownerDropdownFieldFilterSearchTestData());
    }
}
