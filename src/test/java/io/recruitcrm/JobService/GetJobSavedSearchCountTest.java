package io.recruitcrm.JobService;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.albatross.SavedSearch;
import io.rcrm.api.pojo.albatross.SavedSearchRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobSavedSearchCountTest extends TestBase {
    String apiAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    commanFunction commanFunction = new commanFunction();
    Map<String, String> userMap;
    Map<String, Map<String, String>> savedSearchTimestampScenarios;
    Map<String, Integer> savedSearchDataMap; // key: savedSearchKey, value: savedSearchId

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Owner");
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        userMap = createUserMap(apiAuthToken);
        savedSearchTimestampScenarios = commanFunction.createTimestampScenarios();
        savedSearchDataMap = new HashMap<>();
        createData();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getSavedSearchCountTest_Success() {
        String basePath = "advanced-search/job-saved-searches/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createSavedSearchCountPayload("updatedon", "desc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        int expectedCount = 7;
        
        response.then().statusCode(200);
        response.then().body("meta", Matchers.notNullValue());
        response.then().body("meta.message", Matchers.equalTo("Entity count retrieved successfully"));
        response.then().body("meta.status", Matchers.equalTo(200));
        response.then().body("meta.requestUuid", Matchers.notNullValue());
        response.then().body("meta.timestamp", Matchers.notNullValue());
        response.then().body("meta.responseType", Matchers.notNullValue());
        response.then().body("meta.responseType.context", Matchers.equalTo("Request is successful"));
        response.then().body("meta.responseType.code", Matchers.equalTo(103));
        response.then().body("data", Matchers.notNullValue());
        response.then().body("data", Matchers.instanceOf(Integer.class));
        response.then().body("data", Matchers.equalTo(expectedCount));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getSavedSearchCountTest_WithoutAuth() {
        String basePath = "advanced-search/job-saved-searches/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createSavedSearchCountPayload("updatedon", "desc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, null, queryParameters, true, payload);
        response.then().statusCode(401);
        response.then().body("meta.message", Matchers.equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void getSavedSearchCountTest_InvalidAuth() {
        String basePath = "advanced-search/job-saved-searches/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createSavedSearchCountPayload("updatedon", "desc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken + "123", queryParameters, true, payload);
        
        response.then().statusCode(401);
        response.then().body("meta.message", Matchers.equalTo("Unauthorised access"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getSavedSearchCountData", groups = {"job_service", "nightly-build"})
    public void getSavedSearchCountTest_DataProvider(String dbField, String filterType, String filterValue, String fieldType, int count, String token) {
        String basePath = "advanced-search/job-saved-searches/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createSavedSearchFilterCountPayload(dbField, filterType, filterValue, "updatedon", "desc", fieldType);
        String authToken;
        switch (token) {
            case "owner":
                authToken = ownerAlbatrossAuthToken;
                break;
            case "admin":
                authToken = adminAlbatrossAuthToken;
                break;
            case "teamMember":
                authToken = teamMemberAlbatrossAuthToken;
                break;
            default:
                throw new IllegalArgumentException("Invalid token: " + token);
        }
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, authToken, queryParameters, true, payload);
        response.then().statusCode(200);
        int actualCount = response.jsonPath().getInt("data");
        Assert.assertEquals(actualCount, count, "Saved searches count mismatch for token: " + token + " and filterValue: " + filterValue);
    }

    @DataProvider(name = "getSavedSearchCountData", parallel = true)
    public Object[][] getSavedSearchCountData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersTest/jobSavedSearchCountDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{
                    test.getString("dbField"), test.getString("filterType"), test.getString("filterValue"), test.getString("fieldType"), test.getInt("count"), test.getString("token")
                });
            }
        }
        return testData.toArray(new Object[0][]);
    }

    private JSONObject createSavedSearchCountPayload(String sortField, String sortOrder) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB_SAVED_SEARCH");
        JSONArray sortPriorityList = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.put(sortItem);
        payload.put("sortPriorityList", sortPriorityList);
        return payload;
    }

    private JSONObject createSavedSearchFilterCountPayload(String dbField, String filterType, String filterValue, String sortField, String sortOrder, String fieldType) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB_SAVED_SEARCH");
        
        JSONArray sortPriorityList = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.put(sortItem);
        payload.put("sortPriorityList", sortPriorityList);
        
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterList = new JSONArray();
        JSONObject groupFilter = new JSONObject();
        
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "saved_search");
        filter.put("entityType", "saved_search");
        
        // Convert dbField to searchField (for saved searches: userid, createdon, updatedon - no underscores)
        String searchField = convertDbFieldToSearchField(dbField);
        filter.put("searchField", searchField);
        
        // Build filterValue object based on fieldType
        JSONObject filterValueObj = buildFilterValueObject(filterValue, filterType, fieldType);
        filter.put("filterValue", filterValueObj);
        filter.put("filterType", filterType);
        filter.put("fieldType", fieldType);
        filters.put(filter);
        
        groupFilter.put("filters", filters);
        groupFilter.put("filterJoinOperator", "AND");
        groupFilterList.put(groupFilter);
        
        filterSearchList.put("groupFilterList", groupFilterList);
        filterSearchList.put("groupJoinOperator", "AND");
        payload.put("filterSearchList", filterSearchList);
        
        return payload;
    }
    
    private String convertDbFieldToSearchField(String dbField) {
        if (dbField == null || dbField.isEmpty()) {
            return dbField;
        }
        // Normalize to lowercase for case-insensitive comparison
        String normalized = dbField.toLowerCase();
        
        // Map dbField names to searchField names as per API requirements
        // For saved searches: userid, createdon, updatedon (no underscores)
        switch (normalized) {
            case "userid":
                return "userid";  // userId/userid -> userid (no underscore)
            case "createdon":
                return "createdon";  // createdon -> createdon (no underscore for saved searches)
            case "updatedon":
                return "updatedon";  // updatedon -> updatedon (no underscore for saved searches)
            default:
                // For other fields, convert camelCase to snake_case
                return dbField.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }
    }
    
    private JSONObject buildFilterValueObject(String filterValue, String filterType, String fieldType) {
        JSONObject filterValueObj = new JSONObject();
        
        if ("text".equals(fieldType) && "contains_at_least_one".equals(filterType)) {
            // For Created By filter - entity association type
            String processedFilterValue = commanFunction.processFilterValue(filterValue, userMap);
            String[] userIds = processedFilterValue.split(",");
            
            JSONArray entityArray = new JSONArray();
            JSONArray entityIdsArray = new JSONArray();
            for (String userId : userIds) {
                entityIdsArray.put(Integer.parseInt(userId.trim()));
            }
            
            JSONObject entityObj = new JSONObject();
            entityObj.put("entityTypeId", 6); // User entity type ID
            entityObj.put("entityIds", entityIdsArray);
            entityArray.put(entityObj);
            
            filterValueObj.put("value", entityArray);
            filterValueObj.put("type", "ENTITY_ASSOCIATION");
        } else if ("date".equals(fieldType) && "is".equals(filterType)) {
            // For date filters - DATE_IS type
            filterValueObj.put("value", filterValue);
            filterValueObj.put("type", "DATE_IS");
        } else {
            // Fallback for other types
            String processedFilterValue = commanFunction.processFilterValue(filterValue, userMap);
            filterValueObj.put("value", processedFilterValue);
            filterValueObj.put("type", "TEXT");
        }
        
        return filterValueObj;
    }

    public void createData() {
        JSONObject savedSearchData = readJsonFileFromPath("src/test/resources/job_savedSearchData.json");
        
        Iterator<String> keys = savedSearchData.keys();
        while (keys.hasNext()) {
            String albatrossTkn = "";
            String savedSearchKey = keys.next();
            JSONObject savedSearch = savedSearchData.getJSONObject(savedSearchKey);
            
            JSONObject payload = savedSearch.getJSONObject("payload");
            String owner = savedSearch.getString("owner");

            switch (owner) {
                case "owner":
                    albatrossTkn = ownerAlbatrossAuthToken;
                    break;
                case "admin":
                    albatrossTkn = adminAlbatrossAuthToken;
                    break;
                case "teamMember":
                    albatrossTkn = teamMemberAlbatrossAuthToken;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid owner: " + owner);
            }

            SavedSearch savedSearchObj = new SavedSearch();
            savedSearchObj.setName(payload.getString("name"));
            savedSearchObj.setEntitytype(payload.getString("entitytype"));
            savedSearchObj.setJson(payload.getString("json"));
            savedSearchObj.setUserid(payload.isNull("userid") ? null : payload.get("userid"));
            savedSearchObj.setAccountid(payload.isNull("accountid") ? null : payload.get("accountid"));
            savedSearchObj.setShare_with_teammates(payload.getInt("share_with_teammates"));
            savedSearchObj.setPost_search_revamp(payload.getInt("post_search_revamp"));
            
            JSONArray collaboratorIdArray = payload.getJSONArray("collaborator_id");
            List<Object> collaboratorIds = new ArrayList<>();
            for (int i = 0; i < collaboratorIdArray.length(); i++) {
                collaboratorIds.add(collaboratorIdArray.get(i));
            }
            savedSearchObj.setCollaborator_id(collaboratorIds);
            
            JSONArray collaboratorTypeArray = payload.getJSONArray("collaborator_type");
            List<Object> collaboratorTypes = new ArrayList<>();
            for (int i = 0; i < collaboratorTypeArray.length(); i++) {
                collaboratorTypes.add(collaboratorTypeArray.get(i));
            }
            savedSearchObj.setCollaborator_type(collaboratorTypes);

            SavedSearchRequest request = new SavedSearchRequest();
            request.setSave_searches(savedSearchObj);
            request.setUpdateUserObj(false);

            Map<String, String> authTokenMap = new HashMap<>();
            authTokenMap.put("Authorization", "Bearer " + albatrossTkn);

            Response response = RestClient.doPost("JSON", albatrossURL, "saved-searches", authTokenMap, null, true, request);
            if (response.getStatusCode() != 200) {
                Assert.fail("Failed to create saved search: " + savedSearchKey);
            }
            
            int savedSearchId = response.jsonPath().getInt("data.id");
            savedSearchDataMap.put(savedSearchKey, savedSearchId);
            
            String createdOn = savedSearch.getString("createdon");
            String updatedOn = savedSearch.getString("updatedon");
            if (!createdOn.isEmpty() && !updatedOn.isEmpty()) {
                updateSavedSearchTimestamps(savedSearchId, createdOn, savedSearchKey);
            }
        }
    }

    private void updateSavedSearchTimestamps(int savedSearchId, String createdOn, String savedSearchKey) {
        Map<String, String> timestamps = savedSearchTimestampScenarios.get(createdOn);
        if (timestamps == null) {
            // Handle case-insensitive lookup and underscore variations
            String normalizedCreatedOn = createdOn.toLowerCase().replace("_", "");
            for (String key : savedSearchTimestampScenarios.keySet()) {
                String normalizedKey = key.toLowerCase().replace("_", "");
                if (normalizedKey.equals(normalizedCreatedOn)) {
                    timestamps = savedSearchTimestampScenarios.get(key);
                    break;
                }
            }
        }
        
        if (timestamps == null) {
            Assert.fail("Failed to find timestamp scenario for: " + createdOn + " (saved search: " + savedSearchKey + "). Available scenarios: " + savedSearchTimestampScenarios.keySet());
        }
        
        String createdOnTimestamp = timestamps.get("createdOn");
        String updatedOnTimestamp = timestamps.get("updatedOn");

        Response updateResponse = ReaperIntegration.updateSavedSearchTimestamp(savedSearchId, createdOnTimestamp, updatedOnTimestamp);
        if (updateResponse.getStatusCode() != 200) {
            Assert.fail("Failed to update the saved search timestamps for saved search: " + savedSearchKey + ". Status: " + updateResponse.getStatusCode());
        }
    }

    public Map<String,String> createUserMap(String apiKey) {
        Map<String,String> userMap = new HashMap<>();
        commanFunction function = new commanFunction();
        Response response = function.getUsers(baseURL, apiKey);
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restricted", user.get("[2].id").toString());
        userMap.put("teamMember", user.get("[3].id").toString());
        return userMap;
    }
}
