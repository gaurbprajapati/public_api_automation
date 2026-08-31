package io.recruitcrm.CompanyService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
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
public class HotlistFieldCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String albatrossAuthToken;
    String email;
    Map<String, Integer> hotlistIdMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>(); // Maps company key (e.g., "company1") to company ID string
    Map<String, String> companyIdToKeyMap = new HashMap<>(); // Maps company ID string to company key (e.g., "company1")

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "hotlistFieldFilterCompanySearchTestData", description = "Filter Search Test for Hotlist Field")
    public void hotlistFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @DataProvider(name = "hotlistFieldFilterCompanySearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyHotlistFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Parse filterValue to extract hotlist IDs from placeholders like {hotlist1},{hotlist2}
        List<Integer> hotlistIds = parseHotlistIds(filterValue);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        JSONArray valueArray = new JSONArray();
        for (Integer hotlistId : hotlistIds) {
            valueArray.put(hotlistId);
        }
        filterValueObj.put("value", valueArray);
        
        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "companies");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "company");
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
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, String> companySlugMap = new HashMap<>();
        Map<String, Integer> companyIdMap = new HashMap<>();
        
        try {
            //Creating all companies according to payload and storing their slugs and IDs in maps
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
                    Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
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
                companySlugMap.put(entry.getKey(), entry.getValue().getKey());
                Integer companyId = entry.getValue().getValue();
                companyIdMap.put(entry.getKey(), companyId);
                // Store mappings for validation
                companyKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(companyId));
                companyIdToKeyMap.put(String.valueOf(companyId), entry.getKey().toLowerCase());
            }

            addCompaniesToHotlists(companyJson, companyIdMap);
        } finally {
            executor.shutdown();
        }    
    }

    private void addCompaniesToHotlists(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<Integer>> hotlistCompanyIdsMap = new HashMap<>();
        
        // Group companies by their hotlist assignments
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            if (!companyEntry.has("hotlist") || companyEntry.isNull("hotlist")) {
                continue;
            }
            
            String companyHotlists = companyEntry.getString("hotlist");
            if (companyHotlists == null || companyHotlists.isEmpty()) {
                continue;
            }
            
            Integer companyId = companyIdMap.get(companyKey);
            String[] hotlistNames = companyHotlists.split(",");
            for (String hotlistName : hotlistNames) {
                hotlistName = hotlistName.trim();
                if (!hotlistName.isEmpty()) {
                    hotlistCompanyIdsMap.computeIfAbsent(hotlistName, k -> new ArrayList<>()).add(companyId);
                }
            }
        }
        
        // Create hotlists with their respective company IDs
        for (Map.Entry<String, List<Integer>> entry : hotlistCompanyIdsMap.entrySet()) {
            String hotlistName = entry.getKey();
            List<Integer> companyIds = entry.getValue();
            
            if (!companyIds.isEmpty()) {
                Response response = allCrudFunctions.createCompanyHotlist(albatrossURL, albatrossAuthToken, hotlistName, companyIds);
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to add companies to hotlist: " + hotlistName);
                int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
                hotlistIdMap.put(hotlistName, hotlistId);
            }
        }

        // Creating a hotlist without any companies
        Response response = allCrudFunctions.createCompanyHotlist(albatrossURL, albatrossAuthToken, "hotlist4", new ArrayList<>());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create hotlist without any companies");
        int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
        hotlistIdMap.put("hotlist4", hotlistId);
    }



    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "hotlistFieldFilterCompanySearchSmokeTestData", description = "[Smoke] Filter Search Test for Hotlist Field")
    public void hotlistFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        hotlistFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "hotlistFieldFilterCompanySearchSmokeTestData", parallel = true)
    public Object[][] hotlistFieldFilterCompanySearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
