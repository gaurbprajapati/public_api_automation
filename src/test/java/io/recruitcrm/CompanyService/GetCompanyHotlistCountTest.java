package io.recruitcrm.CompanyService;

import com.qa.api.util.reaper.ReaperIntegration;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
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

import java.util.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanyHotlistCountTest extends TestBase {
    String apiAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    AllCrudFunctions function;
    commanFunction commanFunction = new commanFunction();
    Map<String, String> userMap;
    Map<String, Map<String, String>> hotlistTimestampScenarios;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Owner");
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        function = new AllCrudFunctions();
        userMap = createUserMap(apiAuthToken);
        hotlistTimestampScenarios = commanFunction.createTimestampScenarios();
        createData();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyHotlistCountTest_Success() {
        String basePath = "advanced-search/company-hotlists/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createHotlistCountPayload("updated_on", "desc");
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
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyHotlistCountTest_WithoutAuth() {
        String basePath = "advanced-search/company-hotlists/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createHotlistCountPayload("updated_on", "desc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, null, queryParameters, true, payload);
        response.then().statusCode(401);
        response.then().body("meta", Matchers.notNullValue());
        response.then().body("meta.message", Matchers.equalTo("Unauthorised access"));
        response.then().body("meta.status", Matchers.equalTo(401));
        response.then().body("meta.requestUuid", Matchers.notNullValue());
        response.then().body("meta.timestamp", Matchers.notNullValue());
        response.then().body("meta.responseType", Matchers.notNullValue());
        response.then().body("data", Matchers.equalTo("Invalid or expired token"));
        response.then().body("errors", Matchers.notNullValue());
        response.then().body("errors", Matchers.instanceOf(List.class));
        response.then().body("errors.size()", Matchers.equalTo(0));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyHotlistCountTest_InvalidAuth() {
        String basePath = "advanced-search/company-hotlists/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createHotlistCountPayload("updated_on", "desc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken + "123", queryParameters, true, payload);
        
        response.then().statusCode(401);
        response.then().body("meta", Matchers.notNullValue());
        response.then().body("meta.message", Matchers.equalTo("Unauthorised access"));
        response.then().body("meta.status", Matchers.equalTo(401));
        response.then().body("meta.requestUuid", Matchers.notNullValue());
        response.then().body("meta.timestamp", Matchers.notNullValue());
        response.then().body("meta.responseType", Matchers.notNullValue());
        response.then().body("data", Matchers.equalTo("Invalid or expired token"));
        response.then().body("errors", Matchers.notNullValue());
        response.then().body("errors", Matchers.instanceOf(List.class));
        response.then().body("errors.size()", Matchers.equalTo(0));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCompanyHotlistCountData", groups = {"company_service", "nightly-build"})
    public void getCompanyHotlistCountTest_DataProvider(String dbField, String filterType, String filterValue, String fieldType, int count, String token) {
        String basePath = "advanced-search/company-hotlists/search/count/get";
        Map<String, String> queryParameters = new HashMap<>();
        
        JSONObject payload = createHotlistFilterCountPayload(dbField, filterType, filterValue, "updated_on", "desc", fieldType);
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
        Assert.assertEquals(actualCount, count, "Hotlists count mismatch for token: " + token + "and filterValue: " + filterValue);
    }

    @DataProvider(name = "getCompanyHotlistCountData", parallel = true)
    public Object[][] getCompanyHotlistCountData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyHotlistCountDataProvider.json");
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

    private JSONObject createHotlistCountPayload(String sortField, String sortOrder) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY_HOTLIST");
        JSONArray sortPriorityList = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", sortField);
        sortItem.put("order", sortOrder);
        sortPriorityList.put(sortItem);
        payload.put("sortPriorityList", sortPriorityList);
        return payload;
    }

    private JSONObject createHotlistFilterCountPayload(String dbField, String filterType, String filterValue, String sortField, String sortOrder, String fieldType) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY_HOTLIST");
        
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
        filter.put("groupType", "hotlist");
        filter.put("entityType", "hotlist");
        
        // Convert dbField to searchField (camelCase to snake_case)
        // Special handling: userId -> userid, createdOn -> created_on, updatedOn -> updated_on
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
        // Handle both camelCase and lowercase variations
        switch (normalized) {
            case "userid":
                return "userid";  // Special case: userId/userid -> userid (not user_id)
            case "createdon":
                return "created_on";
            case "updatedon":
                return "updated_on";
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
        JSONObject hotlistData = readJsonFileFromPath("src/test/resources/company_hotlistData.json");
        
        Iterator<String> keys = hotlistData.keys();
        while (keys.hasNext()) {
            String albatrossTkn = "";
            String hotlistKey = keys.next();
            JSONObject hotlist = hotlistData.getJSONObject(hotlistKey);
            
            JSONObject payload = hotlist.getJSONObject("payload");
            String owner = hotlist.getString("owner");

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

            Response response = function.createHotlistWithJson(albatrossURL, albatrossTkn, payload);
            int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");

            String createdOn = hotlist.getString("createdon");
            String updatedOn = hotlist.getString("updatedon");
            if (!createdOn.isEmpty() && !updatedOn.isEmpty()) {
                updateHotlistTimestamps(hotlistId, createdOn, hotlistKey);
            }
        }
    }

    private void updateHotlistTimestamps(int hotlistId, String createdOn, String hotlistKey) {
        Map<String, String> timestamps = hotlistTimestampScenarios.get(createdOn);
        if (timestamps == null) {
            Assert.fail("Timestamp scenario not found for key: " + createdOn + ". Available scenarios: " + hotlistTimestampScenarios.keySet());
        }
        
        String createdOnTimestamp = timestamps.get("createdOn");
        String updatedOnTimestamp = timestamps.get("updatedOn");

        Response updateResponse = ReaperIntegration.updateHotlistTimestamp(hotlistId, createdOnTimestamp, updatedOnTimestamp);
        if (updateResponse.getStatusCode() != 200) {
            Assert.fail("Failed to update the hotlist timestamps for hotlist: " + hotlistKey + ". Status: " + updateResponse.getStatusCode() + ", Response: " + updateResponse.getBody().prettyPrint());
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

