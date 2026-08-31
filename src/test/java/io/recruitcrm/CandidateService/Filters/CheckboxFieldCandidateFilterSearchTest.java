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
public class CheckboxFieldCandidateFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions function = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    List<Integer> createdCandidateIds = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createCustomFields();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "checkboxFieldFilterSearchTestData", description = "Filter Search Test for Checkbox Fields")
    public void checkboxFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);

        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logResponse(response, data);

        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for field: " + fieldName + ", filterType: " + filterType +
                        " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully",
                "Message for field: " + fieldName + ", filterType: " + filterType +
                        " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        validateCheckboxFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "checkboxFieldFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateCheckboxTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{
                        key,
                        test.getString("filterType"),
                        getFilterValueAsString(test),
                        test.getString("dbField"),
                        test.getString("expectedResult"),
                        test.getString("fieldType"),
                        test.getString("filterValue_TYPE")
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createCustomFields() {
        // Sequential: avoids two simultaneous POSTs to custom-fields right after token resolution (reduces flaky 401s).
        // Retries on 401 are handled in AllCrudFunctions.postCustomFieldsWithRetry.
        function.createCustomFieldsWithUserDefinedNames(albatrossURL, albatrossAuthToken, "file", 5, "Custom File", 1);
        function.createCustomFieldsWithUserDefinedNames(albatrossURL, albatrossAuthToken, "checkbox", 5, "Custom Checkbox", 2);
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/checkbox_candidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Integer>> futures = candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.supplyAsync(() -> {
                        JSONObject payload = candidateJson.getJSONObject(candidateKey);
                        Response response = function.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        int candidateId = response.jsonPath().getInt("data.candidate.id");

                        updateCustomFieldsForCandidate(candidateId, payload);
                        updateEmailOptOutForCandidate(candidateId, payload);

                        return candidateId;
                    }, executor))
                    .collect(Collectors.toList());

            List<Integer> candidateIds = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            createdCandidateIds.addAll(candidateIds);
        } finally {
            executor.shutdown();
        }
    }

    private void updateCustomFieldsForCandidate(int candidateId, JSONObject candidateData) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            String custcolumn1Value = candidateData.getString("firstname").equals("Alice") ? "1" : "0";
            String custcolumn2Value = candidateData.getString("firstname").equals("Alice") ? "1" : "0";

            CompletableFuture<Response> custcolumn1Future = CompletableFuture.supplyAsync(() -> {
                return function.updateCustomField("candidates", albatrossURL, candidateId, albatrossAuthToken, "custcolumn1", custcolumn1Value);
            }, executor);

            CompletableFuture<Response> custcolumn2Future = CompletableFuture.supplyAsync(() -> {
                return function.updateCustomField("candidates", albatrossURL, candidateId, albatrossAuthToken, "custcolumn2", custcolumn2Value);
            }, executor);

            Response response1 = custcolumn1Future.get();
            Response response2 = custcolumn2Future.get();

            Assert.assertEquals(response1.getStatusCode(), 200, "Failed to update custcolumn1 for candidate: " + candidateId);
            Assert.assertEquals(response2.getStatusCode(), 200, "Failed to update custcolumn2 for candidate: " + candidateId);

        } catch (Exception e) {
            Assert.fail("Failed to update custom fields for candidate " + candidateId + ": " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private void updateEmailOptOutForCandidate(int candidateId, JSONObject candidateData) {
        String emailOptOutValue = candidateData.getString("email_opt_out");
        if(emailOptOutValue.contains("1")) {
            Response response = function.updateEmailOptOut("candidates", albatrossURL, candidateId, albatrossAuthToken, "email_opt_out", emailOptOutValue);
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to update email_opt_out for candidate: " + candidateId);
        }
    }


    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        String groupType = "Currently Working".equals(fieldName) ? "work_history" : "candidates";

        JSONObject filterValueObj;
        if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj = integerFilterValue(filterValue);
        } else {
            throw new IllegalArgumentException("Unsupported filterValue_TYPE for checkbox filter: " + filterValue_TYPE);
        }

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", groupType);
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

    private static String getFilterValueAsString(JSONObject test) {
        Object filterValueObj = test.get("filterValue");
        if (filterValueObj == null || JSONObject.NULL.equals(filterValueObj)) {
            return "";
        }
        return String.valueOf(filterValueObj);
    }

    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getJSONArray("data");
    }

    public void validateCheckboxFilteredData(JSONArray data, String filterType, String filterValue,
                                             String fieldName, String dbField, String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0,
                    "Wrong candidate data for field: " + fieldName +
                            " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (!expectedResult.equals("Empty") && data.length() == 0){
            Assert.fail("No data found for field: " + fieldName +
                    " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        int expectedValues = getExpectedCount(fieldName, filterType);

        Assert.assertEquals(data.length(), expectedValues,
                "Wrong number of candidates returned for field: " + fieldName +
                        " and filterType: " + filterType + " and filterValue: " + filterValue +
                        " - expected " + expectedValues + " but got " + data.length());
    }

    private int getExpectedCount(String fieldName, String filterType) {

        switch (fieldName) {
            case "Custom File":
                // Alice has resume file, Bob doesn't
                return filterType.equals("yes") ? 1 : 1;
            case "Custom Checkbox":
                // Alice has checkbox checked, Bob doesn't
                return filterType.equals("yes") ? 1 : 1;
            case "Willing to Relocate":
                // Alice is willing to relocate, Bob isn't
                return filterType.equals("yes") ? 1 : 1;
            case "Opt-out":
                // Alice is not opted out, Bob is opted out
                return filterType.equals("yes") ? 1 : 1;
            case "Resume":
                // Alice has resume, Bob doesn't
                return filterType.equals("yes") ? 1 : 1;
            case "Currently Working":
                // Alice is currently working, Bob isn't
                return filterType.equals("yes") ? 1 : 1;
            default:
                return 0;
        }
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "checkboxFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Checkbox Fields")
    public void checkboxFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        checkboxFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "checkboxFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] checkboxFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}