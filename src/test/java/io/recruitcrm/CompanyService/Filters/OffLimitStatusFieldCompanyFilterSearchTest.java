package io.recruitcrm.CompanyService.Filters;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;
import com.qa.api.util.reaper.ThreadManager;

import java.util.ArrayList;
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
public class OffLimitStatusFieldCompanyFilterSearchTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    Map<String, Integer> offLimitStatusMap;
    Map<String, String> companyKeyToIdMap = new HashMap<>(); 
    Map<String, String> companyIdToKeyMap = new HashMap<>(); 
    String apiKey;
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        offLimitStatusMap = allCrudFunctions.getOffLimitStatusMap(albatrossURL, ownerAlbatrossAuthToken);
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "offLimitStatusFieldCompanyFilterSearchTestData", description = "Filter Search Test for Off Limit Status Field")
    public void offLimitStatusFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @DataProvider(name = "offLimitStatusFieldCompanyFilterSearchTestData", parallel = true)
    public Object[][] offLimitStatusFieldCompanyFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Off Limit Status")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> companyIdMap = new HashMap<>();
        
        try {
            //Creating all companies according to payload and storing their IDs in a map
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
                    Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, ownerAlbatrossAuthToken, payload);
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
                Integer companyId = entry.getValue().getValue();
                companyIdMap.put(entry.getKey(), companyId);
                
                // Store mappings for validation
                companyKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(companyId));
                companyIdToKeyMap.put(String.valueOf(companyId), entry.getKey().toLowerCase());
            }
            addCompaniesToOffLimitStatus(companyJson, companyIdMap);
        } finally {
            executor.shutdown();
        }
    }

    private void addCompaniesToOffLimitStatus(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        Map<String, List<Integer>> offLimitStatusCompanyIdsMap = new HashMap<>();
        
        // Group companies by their off limit status assignments
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            if (!companyEntry.has("offLimitStatus") || companyEntry.isNull("offLimitStatus")) {
                continue;
            }
            
            String companyOffLimitStatus = companyEntry.getString("offLimitStatus");
            if (companyOffLimitStatus == null || companyOffLimitStatus.isEmpty()) {
                continue;
            }
            
            Integer companyId = companyIdMap.get(companyKey);
            String[] offLimitStatusNames = companyOffLimitStatus.split(",");
            for (String offLimitStatusName : offLimitStatusNames) {
                offLimitStatusName = offLimitStatusName.trim();
                if (!offLimitStatusName.isEmpty()) {
                    offLimitStatusCompanyIdsMap.computeIfAbsent(offLimitStatusName, k -> new ArrayList<>()).add(companyId);
                }
            }
        }

        // Bulk mark companies as off limit
        long nowSeconds = System.currentTimeMillis() / 1000L;
        long startDate = nowSeconds;
        long endDate = nowSeconds + 30L * 24 * 60 * 60; // +30 days
        
        String basePath = "off-limit/mark-off-limit";
        
        for (Map.Entry<String, List<Integer>> entry : offLimitStatusCompanyIdsMap.entrySet()) {
            String statusLabel = entry.getKey();
            List<Integer> companyIds = entry.getValue();
            
            if (companyIds.isEmpty()) {
                continue;
            }
            
            // Get status ID from the map
            Integer statusId = offLimitStatusMap.get(statusLabel);
            if (statusId == null) {
                Assert.fail("Off-limit status ID not found for status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
                continue;
            }
            
            // Create payload
            JSONObject payload = new JSONObject();
            payload.put("entity_type_id", 3); // Entity type ID as per expected payload
            payload.put("entity_ids", new JSONArray(companyIds));
            payload.put("status_id", statusId);
            payload.put("start_date", startDate);
            payload.put("end_date", endDate);
            payload.put("reason", "");
            
            Response response = RestClient.doPost("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, true, payload);
            System.out.println("Marking company as offlimit: "+ response.prettyPrint());
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to mark companies off-limit with status: " + statusLabel);
        }
    }


    private String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy.toLowerCase()) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teammember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedteammember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return ownerAlbatrossAuthToken; // Default to owner token
        }
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("offLimitBehavior", "bypass");
        payload.put("sortPriorityList", new JSONArray());
        
        // Process filterValue to convert status label placeholders to status IDs
        String processedFilterValue = processFilterValue(filterValue);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("INTEGER_LIST")) {
            filterValueObj = integerListFilterValue(processedFilterValue);
        } else {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        }
        
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

    private String processFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String statusLabel = matcher.group(1);
            
            // Get status ID from the map
            Integer statusId = offLimitStatusMap.get(statusLabel);
            if (statusId != null) {
                processedValue = processedValue.replace(placeholder, String.valueOf(statusId));
            } else {
                throw new IllegalArgumentException("Off-limit status ID not found for status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
            }
        }

        return processedValue;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "offLimitStatusFieldCompanyFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Off Limit Status Field")
    public void offLimitStatusFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        offLimitStatusFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "offLimitStatusFieldCompanyFilterSearchSmokeTestData", parallel = true)
    public Object[][] offLimitStatusFieldCompanyFilterSearchSmokeTestData() {
        return limitSmokeRows(offLimitStatusFieldCompanyFilterSearchTestData());
    }
}
