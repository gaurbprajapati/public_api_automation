package io.recruitcrm.CandidateService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NumberFieldCandidateFilterSearchTest extends FilterSearchBaseTest{
    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "numberFieldFilterSearchTestData", description = "Filter Search Test for Text Fields")
    public void numberFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, filterValue_TYPE);
        
        // Log payload for the listener to capture
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        
        Assert.assertEquals(response.getStatusCode(), 200," Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"),"Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Candidates fetched successfully'");
        
        validateNumberFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, "Candidate");
    }


    @DataProvider(name = "numberFieldFilterSearchTestData",parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateNumberTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{
                        key,
                        test.getString("filterType"),
                        test.getString("filterValue"),
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("filterValue_TYPE"),
                        test.getString("testCaseId")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }


    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        JSONObject filterValueObj = new JSONObject();
        
        if (filterValue_TYPE.equals("DOUBLE_START_END")) {
            filterValueObj = doubleStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("INTEGER_START_END")) {
            filterValueObj = integerStartEndFilterValue(filterValue);
        } else if (filterValue_TYPE.equals("NUMERIC_STRING")) {
            filterValueObj = numericStringFilterValue(filterValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                filterValueObj.put("value", JSONObject.NULL);
            } else {
                if (filterValue != null && !filterValue.trim().isEmpty()) {
                    try {
                        if (filterValue.contains(".")) {
                            filterValueObj.put("value", Double.parseDouble(filterValue));
                        } else {
                            filterValueObj.put("value", Integer.parseInt(filterValue));
                        }
                    } catch (NumberFormatException e) {
                        filterValueObj.put("value", filterValue);
                    }
                } else {
                    filterValueObj.put("value", JSONObject.NULL);
                }
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
        filter.put("entityType", "candidate");
        filter.put("fieldType", "number");
        filter.put("filterValue", filterValueObj);
        
        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);
        
        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");
        
        payload.put("filterSearchList", filterSearchList);

        return payload;
    }



    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .map(candidateKey -> CompletableFuture.runAsync(() -> {
                    JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                    JSONObject payload = candidateEntry.getJSONObject("payload");
                    function.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }



    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "numberFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Text Fields")
    public void numberFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String filterValue_TYPE, String testCaseId) {
        numberFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, filterValue_TYPE, testCaseId);
    }

    @DataProvider(name = "numberFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] numberFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
