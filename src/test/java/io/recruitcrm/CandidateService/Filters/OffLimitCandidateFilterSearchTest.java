package io.recruitcrm.CandidateService.Filters;

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
public class OffLimitCandidateFilterSearchTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, Integer> offLimitStatusMap;
    Map<String, String> candidateKeyToIdMap = new HashMap<>(); 
    Map<String, String> candidateIdToKeyMap = new HashMap<>(); 
    String apiKey;
    private static final int ENTITY_TYPE_ID_CANDIDATE = 5;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        email = ThreadManager.getAccount().getOwner().getEmail();
        apiKey = ThreadManager.getAccountApiKey();
        offLimitStatusMap = allCrudFunctions.getOffLimitStatusMap(albatrossURL, ownerAlbatrossAuthToken);
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "offLimitStatusFieldCandidateFilterSearchTestData", description = "Filter Search Test for Off Limit Status Field")
    public void offLimitStatusFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "candidates");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, dbField);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, candidateKeyToIdMap, candidateIdToKeyMap, 15, "Candidate");
    }

    @DataProvider(name = "offLimitStatusFieldCandidateFilterSearchTestData", parallel = true)
    public Object[][] offLimitStatusFieldCandidateFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateOfflimitStatusFilterDataProvider.json");
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
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> candidateIdMap = new HashMap<>();
        
        try {
            //Creating all candidates according to payload and storing their IDs in a map
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .map(candidateKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                    JSONObject candidatePayload = candidateEntry.getJSONObject("payload");
                    Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, ownerAlbatrossAuthToken, candidatePayload);
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
                Integer candidateId = entry.getValue().getValue();
                candidateIdMap.put(entry.getKey(), candidateId);
                
                // Store mappings for validation
                candidateKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(candidateId));
                candidateIdToKeyMap.put(String.valueOf(candidateId), entry.getKey().toLowerCase());
            }
            addCandidatesToOffLimitStatus(candidateJson, candidateIdMap);
        } finally {
            executor.shutdown();
        }
    }

    private void addCandidatesToOffLimitStatus(JSONObject candidateJson, Map<String, Integer> candidateIdMap) {
        Map<String, List<Integer>> offLimitStatusCandidateIdsMap = new HashMap<>();
        
        // Group candidates by their off limit status assignments
        for (String candidateKey : candidateIdMap.keySet()) {
            JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
            if (!candidateEntry.has("offLimitStatus") || candidateEntry.isNull("offLimitStatus")) {
                continue;
            }
            
            String candidateOffLimitStatus = candidateEntry.getString("offLimitStatus");
            if (candidateOffLimitStatus == null || candidateOffLimitStatus.isEmpty()) {
                continue;
            }
            
            Integer candidateId = candidateIdMap.get(candidateKey);
            String[] offLimitStatusNames = candidateOffLimitStatus.split(",");
            for (String offLimitStatusName : offLimitStatusNames) {
                offLimitStatusName = offLimitStatusName.trim();
                if (!offLimitStatusName.isEmpty()) {
                    offLimitStatusCandidateIdsMap.computeIfAbsent(offLimitStatusName, k -> new ArrayList<>()).add(candidateId);
                }
            }
        }

        // Bulk mark candidates as off limit
        long nowSeconds = System.currentTimeMillis() / 1000L;
        long startDate = nowSeconds;
        long endDate = nowSeconds + 30L * 24 * 60 * 60; // +30 days
        
        String basePath = "off-limit/mark-off-limit";
        
        for (Map.Entry<String, List<Integer>> entry : offLimitStatusCandidateIdsMap.entrySet()) {
            String statusLabel = entry.getKey();
            List<Integer> candidateIds = entry.getValue();
            
            if (candidateIds.isEmpty()) {
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
            payload.put("entity_type_id", ENTITY_TYPE_ID_CANDIDATE);
            payload.put("entity_ids", new JSONArray(candidateIds));
            payload.put("status_id", statusId);
            payload.put("start_date", startDate);
            payload.put("end_date", endDate);
            payload.put("reason", "");
            
            Response response = RestClient.doPost("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, true, payload);
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to mark candidates off-limit with status: " + statusLabel);
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
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("offLimitBehavior", "inclusion");
        payload.put("sortPriorityList", new JSONArray());

        String processedFilterValue = processFilterValue(filterValue);

        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("INTEGER_LIST")) {
            filterValueObj = integerListFilterValue(processedFilterValue);
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
}
