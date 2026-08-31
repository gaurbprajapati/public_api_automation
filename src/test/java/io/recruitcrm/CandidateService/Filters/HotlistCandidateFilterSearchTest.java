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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class HotlistCandidateFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    String albatrossAuthToken;
    String email;
    Map<String, Integer> hotlistIdMap = new HashMap<>();
    Map<String, String> candidateKeyToIdMap = new HashMap<>();
    Map<String, String> candidateIdToKeyMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "hotlistFieldFilterCandidateSearchTestData", description = "Filter Search Test for Hotlist Field")
    public void hotlistFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("TestCaseId: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "firstname");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 15, "Candidate");
    }

    @DataProvider(name = "hotlistFieldFilterCandidateSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateHotlistTypeFilterDataProvider.json");
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
                        test.getString("fieldType"),
                        test.optString("filterValue_TYPE", "INTEGER_LIST"),
                        test.optString("testCaseId", "")
                });
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

        List<Integer> hotlistIds = parseHotlistIds(filterValue);
        String processedIds = hotlistIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        JSONObject filterValueObj;
        if ("INTEGER_LIST".equals(filterValue_TYPE)) {
            filterValueObj = integerListFilterValue(processedIds);
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

    private List<Integer> parseHotlistIds(String filterValue) {
        List<Integer> hotlistIds = new ArrayList<>();
    
        //If filterValue is empty or null, return empty list
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return hotlistIds;
        }
    
        for (String raw : filterValue.split(",")) {
            String placeholder = raw.trim();
            if (placeholder.isEmpty()) continue;
    
            //Normalize: remove {}
            String key = (placeholder.startsWith("{") && placeholder.endsWith("}")) ? placeholder.substring(1, placeholder.length() - 1): placeholder;
    
            Integer id = hotlistIdMap.get(key);
            //If hotlist ID is not found, fail the test
            if (id == null) {
                Assert.fail("Wrong hotlist name provided in the filter value. Hotlist ID not found for: " + placeholder +". Available hotlists: " + hotlistIdMap.keySet());
            }
            hotlistIds.add(id);
        }
    
        return hotlistIds;
    }
    

    private void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, String> candidateSlugMap = new HashMap<>();
        Map<String, Integer> candidateIdMap = new HashMap<>();
        
        try {
            //Creating all candidates according to payload and storing their slugs and IDs in maps
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .map(candidateKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                    JSONObject payload = candidateEntry.getJSONObject("payload");
                    Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                    response.then().statusCode(200);
                    JsonPath jsonPath = response.jsonPath();
                    String slug = jsonPath.getString("data.candidate.slug");
                    Integer candidateId = jsonPath.getInt("data.candidate.id");
                    return Map.entry(candidateKey, Map.entry(slug, candidateId));
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            for (CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>> future : createFutures) {
                Map.Entry<String, Map.Entry<String, Integer>> entry = future.join();
                candidateSlugMap.put(entry.getKey(), entry.getValue().getKey());
                Integer candidateId = entry.getValue().getValue();
                candidateIdMap.put(entry.getKey(), candidateId);
                // Store mappings for validation
                candidateKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(candidateId));
                candidateIdToKeyMap.put(String.valueOf(candidateId), entry.getKey().toLowerCase());
            }

            addCandidatesToHotlists(candidateJson, candidateIdMap);
        } finally {
            executor.shutdown();
        }    
    }

    private void addCandidatesToHotlists(JSONObject candidateJson, Map<String, Integer> candidateIdMap) {
        Map<String, List<Integer>> hotlistCandidateIdsMap = new HashMap<>();
        
        // Group candidates by their hotlist assignments
        for (String candidateKey : candidateIdMap.keySet()) {
            JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
            if (!candidateEntry.has("hotlist") || candidateEntry.isNull("hotlist")) {
                continue;
            }
            
            String candidateHotlists = candidateEntry.getString("hotlist");
            if (candidateHotlists == null || candidateHotlists.isEmpty()) {
                continue;
            }
            
            Integer candidateId = candidateIdMap.get(candidateKey);
            String[] hotlistNames = candidateHotlists.split(",");
            for (String hotlistName : hotlistNames) {
                hotlistName = hotlistName.trim();
                if (!hotlistName.isEmpty()) {
                    hotlistCandidateIdsMap.computeIfAbsent(hotlistName, k -> new ArrayList<>()).add(candidateId);
                }
            }
        }
        
        // Create hotlists with their respective candidate IDs
        for (Map.Entry<String, List<Integer>> entry : hotlistCandidateIdsMap.entrySet()) {
            String hotlistName = entry.getKey();
            List<Integer> candidateIds = entry.getValue();
            
            if (!candidateIds.isEmpty()) {
                Response response = allCrudFunctions.createCandidateHotlist(albatrossURL, albatrossAuthToken, hotlistName, candidateIds);
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to add candidates to hotlist: " + hotlistName);
                int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
                hotlistIdMap.put(hotlistName, hotlistId);
            }
        }

        // Creating a hotlist without any candidates
        Response response = allCrudFunctions.createCandidateHotlist(albatrossURL, albatrossAuthToken, "hotlist4", new ArrayList<>());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create hotlist without any candidates");
        int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
        hotlistIdMap.put("hotlist4", hotlistId);
    }

}
