package io.recruitcrm.CandidateService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
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

import com.qa.api.util.reaper.ThreadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class SkillsFieldCandidateFilterSearchTest extends FilterSearchBaseTest {


    private static final String[] SKILL_FIELDS = {"Skills", "Language"};

    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    Map<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    Map<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    int allExpectedCandidateCount;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "skillsFieldFilterSearchTestData", description = "Filter search for skill and language-skills (multiselect)")
    public void skillsFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully",
                "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult,
                candidateKeyToIdMap, candidateIdToKeyMap, allExpectedCandidateCount, "Candidate");
    }

    @DataProvider(name = "skillsFieldFilterSearchTestData", parallel = true)
    public Object[][] skillsFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateTextTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String sectionKey : SKILL_FIELDS) {
            JSONArray tests = filterData.getJSONArray(sectionKey);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{
                        sectionKey,
                        test.getString("filterType"),
                        test.getString("filterValue"),
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("filterValue_TYPE"),
                        test.optString("testCaseId", "")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        List<String> candidateKeys = candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .sorted()
                .collect(Collectors.toList());
        allExpectedCandidateCount = candidateKeys.size();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateKeys.stream()
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = candidateEntry.getJSONObject("payload");
                        Response response = function.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        int candidateId = jsonPath.getInt("data.candidate.id");
                        String normalizedKey = candidateKey.toLowerCase();
                        candidateKeyToIdMap.put(normalizedKey, String.valueOf(candidateId));
                        candidateIdToKeyMap.put(String.valueOf(candidateId), normalizedKey);
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField,
            String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj;
        if ("STRING_LIST".equals(filterValue_TYPE)) {
            filterValueObj = stringListFilterValue(filterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", filterValue);
        }

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filterClause = new JSONObject();
        filterClause.put("isCrossEntity", false);
        filterClause.put("groupType", "candidates");
        filterClause.put("searchField", dbField);
        filterClause.put("filterType", filterType);
        filterClause.put("entityType", "candidate");
        filterClause.put("fieldType", "text");
        filterClause.put("filterValue", filterValueObj);

        filtersArray.put(filterClause);
        groupFilterList.put("filters", filtersArray);

        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");

        payload.put("filterSearchList", filterSearchList);

        return payload;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "skillsFieldFilterSearchSmokeTestData", description = "[Smoke] subset of full aries run")
    public void skillsFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField,
            String expectedResult, String filterValue_TYPE, String testCaseId) {
        skillsFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "skillsFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] skillsFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(skillsFieldFilterSearchTestData());
    }
}
