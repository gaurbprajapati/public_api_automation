package io.recruitcrm.CompanyService;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import org.hamcrest.Matchers;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import org.testng.Assert;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCompanyHotlistTest extends TestBase{
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String apiAuthToken;
    AllCrudFunctions function;
    commanFunction commanFunction = new commanFunction();
    Map<String, Map<String, String>> hotlistTimestampScenarios;
    Map<String, String> userMap;
    

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        function = new AllCrudFunctions();
        apiAuthToken = ThreadManager.getAccountApiKey();
        hotlistTimestampScenarios = commanFunction.createTimestampScenarios();
        userMap = commanFunction.createUserMap(baseURL, apiAuthToken);
        createData();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyHotlist_Success() {
        String basePath = "advanced-search/company-hotlists/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistPayload();
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.prettyPrint();

        response.then().statusCode(200);
        response.then().body("meta", Matchers.notNullValue());
        response.then().body("meta.message", Matchers.equalTo("Entities retrieved successfully"));
        response.then().body("meta.status", Matchers.equalTo(200));
        response.then().body("meta.requestUuid", Matchers.notNullValue());
        response.then().body("meta.timestamp", Matchers.notNullValue());
        response.then().body("meta.responseType", Matchers.notNullValue());
        response.then().body("meta.responseType.context", Matchers.notNullValue());
        response.then().body("meta.responseType.code", Matchers.notNullValue());
        response.then().body("meta.responseType.code", Matchers.instanceOf(Integer.class));
        response.then().body("data", Matchers.notNullValue());
        response.then().body("data", Matchers.instanceOf(List.class));
        
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> hotlists = jsonPath.getList("data");
        
        // Validate each hotlist object in the data array
        for (int i = 0; i < hotlists.size(); i++) {
            String index = "data[" + i + "]";
            response.then().body(index + ".id", Matchers.notNullValue());
            response.then().body(index + ".name", Matchers.notNullValue());
            response.then().body(index + ".name", Matchers.not(equalTo("")));
            response.then().body(index + ".entityName", Matchers.notNullValue());
            response.then().body(index + ".entityName", Matchers.equalTo("companies"));
            response.then().body(index + ".shared", Matchers.notNullValue());
            response.then().body(index + ".shared", Matchers.instanceOf(Integer.class));
            response.then().body(index + ".userId", Matchers.notNullValue());
            response.then().body(index + ".userId", Matchers.greaterThan(0));
            response.then().body(index + ".accountId", Matchers.notNullValue());
            response.then().body(index + ".accountId", Matchers.greaterThan(0));
            response.then().body(index + ".createdOn", Matchers.notNullValue());
            response.then().body(index + ".updatedOn", Matchers.notNullValue());            
            response.then().body(index + ".updatedBy", Matchers.notNullValue());
            response.then().body(index + ".updatedBy", Matchers.greaterThan(0));
            response.then().body(index + ".userName", Matchers.anything());
            response.then().body(index + ".count", Matchers.notNullValue());
            response.then().body(index + ".count", Matchers.instanceOf(Integer.class));
            response.then().body(index + ".count", Matchers.greaterThanOrEqualTo(0));
            response.then().body(index + ".isPinned", Matchers.notNullValue());
            response.then().body(index + ".isPinned", Matchers.instanceOf(Boolean.class));
            response.then().body(index + ".createdOn", Matchers.greaterThan(0));
            response.then().body(index + ".updatedOn", Matchers.greaterThan(0));
            response.then().body(index + ".updatedBy", Matchers.greaterThan(0));
            response.then().body(index + ".userName", Matchers.anything());
            response.then().body(index + ".count", Matchers.greaterThanOrEqualTo(0));
            response.then().body(index + ".isPinned", Matchers.instanceOf(Boolean.class));
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/company/getHotlist.json"));

        List<String> expectedHotlistNames = new ArrayList<>(Arrays.asList(
            "Hotlist Test [Admin][Yesterday][Shared]",
            "Hotlist Test [Owner][Today][Not Shared]",
            "Hotlist Test [Owner][Today][Shared]",
            "Hotlist Test [Team Member][Last Month][Shared]",
            "Hotlist Test [Team Member][Last Quarter][Shared]",
            "Hotlist Test [Team Member][Last Week][Shared]",
            "Hotlist Test [Team Member][Last Year][Shared]"
        ));
        List<String> actualHotlistNames = jsonPath.getList("data.name");

        Collections.sort(expectedHotlistNames);
        Collections.sort(actualHotlistNames);

        Assert.assertEquals(actualHotlistNames, expectedHotlistNames, "Expected hotlist names do not match actual hotlist names. " +"Expected: " + expectedHotlistNames + ", Actual: " + actualHotlistNames);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifyCompanyHotlist_sortByName() {
        String basePath = "advanced-search/company-hotlists/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistPayload("name", "asc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        JsonPath jsonPath = response.jsonPath();
        List<String> names = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(names, "asc"), "Hotlists are not sorted by name in ascending order. Names: " + names);

        // verify descending order
        payload = createHotlistPayload("name", "desc");
        response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        jsonPath = response.jsonPath();
        names = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(names, "desc"), "Hotlists are not sorted by name in descending order. Names: " + names);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifyCompanyHotlist_sortByCreatedOn() {
        String basePath = "advanced-search/company-hotlists/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistPayload("created_on", "asc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        JsonPath jsonPath = response.jsonPath();
        List<Long> createdOnList = jsonPath.getList("data.createdOn");
        List<String> nameList = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(createdOnList, "asc"), "Hotlists are not sorted by createdOn in ascending order. Values: " + nameList);

        // verify descending order
        payload = createHotlistPayload("created_on", "desc");
        response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        jsonPath = response.jsonPath();
        createdOnList = jsonPath.getList("data.createdOn");
        nameList = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(createdOnList, "desc"), "Hotlists are not sorted by createdOn in descending order. Values: " + nameList);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifyCompanyHotlist_sortByUpdatedOn() {
        String basePath = "advanced-search/company-hotlists/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistPayload("updated_on", "asc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        JsonPath jsonPath = response.jsonPath();
        List<Long> updatedOnList = jsonPath.getList("data.updatedOn");
        
        Assert.assertTrue(isSorted(updatedOnList, "asc"), "Hotlists are not sorted by updatedOn in ascending order. Values: " + updatedOnList);

        // verify descending order
        payload = createHotlistPayload("updated_on", "desc");
        response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        jsonPath = response.jsonPath();
        updatedOnList = jsonPath.getList("data.updatedOn");
        
        Assert.assertTrue(isSorted(updatedOnList, "desc"), "Hotlists are not sorted by updatedOn in ascending order. Values: " + updatedOnList);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getCompanyHotlistFiltersData", groups = {"company_service", "nightly-build"})
    public void verifyCompanyHotlistFilters(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType) {
        String basePath = "advanced-search/company-hotlists/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistFilterPayload(dbField, filterType, filterValue, "updated_on", "desc", fieldType);
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, ownerAlbatrossAuthToken, queryParameters, true, payload);
        response.then().statusCode(200);
        JSONArray filteredData = getFilteredData(response);
        
        // Convert dbField to camelCase format for validation (API response uses camelCase)
        String responseFieldName = convertToCamelCase(dbField);
        
        if (fieldName.equals("Created By")) {
            commanFunction.validateCreatedByFilteredData(filteredData, filterType, filterValue, fieldName, responseFieldName, expectedResult, "Hotlist", userMap);
        } else if (fieldName.equals("Created On") || fieldName.equals("Updated On")) {
            commanFunction.validateCreatedOnUpdatedOnFilteredData(filteredData, filterType, filterValue, fieldName, responseFieldName, expectedResult);
        }
    }

    @DataProvider(name = "getCompanyHotlistFiltersData", parallel = true)
    public Object[][] getCompanyHotlistFiltersData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersTest/companyHotlistDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String filterType = test.getString("filterType");
                String filterValue = test.getString("filterValue");
                String fieldType = test.getString("fieldType");
                testData.add(new Object[]{
                    key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }



    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getJSONArray("data");
    }

    private JSONObject createHotlistFilterPayload(String dbField, String filterType, String filterValue, String sortField, String sortOrder, String fieldType) {
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
    
    private String convertToCamelCase(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName;
        }
        // Normalize to lowercase for case-insensitive comparison
        String normalized = fieldName.toLowerCase();
        
        // Map field names to camelCase format as used in API response
        switch (normalized) {
            case "userid":
            case "user_id":
                return "userId";
            case "createdon":
            case "created_on":
                return "createdOn";
            case "updatedon":
            case "updated_on":
                return "updatedOn";
            default:
                // For other fields, assume they're already in correct format or convert snake_case to camelCase
                if (fieldName.contains("_")) {
                    // Convert snake_case to camelCase
                    String[] parts = fieldName.toLowerCase().split("_");
                    StringBuilder camelCase = new StringBuilder(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        camelCase.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
                    }
                    return camelCase.toString();
                }
                return fieldName;
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

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void getCompanyHotlist_WithoutAuth() {
        String basePath = "advanced-search/companies/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistPayload();
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, null, queryParameters, true, payload);
        response.prettyPrint();
        
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
    public void getCompanyHotlist_InvalidAuth() {
        String basePath = "advanced-search/company-hotlists/search/get";
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createHotlistPayload();
        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, "invalid-api-key", queryParameters, true, payload);
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

    private JSONObject createHotlistPayload() {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY_HOTLIST");
        JSONArray sortPriorityList = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", "updated_on");
        sortItem.put("order", "desc");
        sortPriorityList.put(sortItem);
        payload.put("sortPriorityList", sortPriorityList);
        return payload;
    }

    private JSONObject createHotlistPayload(String field, String order) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY_HOTLIST");
        JSONArray sortPriorityList = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", field);
        sortItem.put("order", order);
        sortPriorityList.put(sortItem);
        payload.put("sortPriorityList", sortPriorityList);
        return payload;
    }

    public void createData() {
        JSONObject hotlistData = readJsonFileFromPath("src/test/resources/company_hotlistData.json");
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(hotlistData.keySet().stream()
                .map(hotlistKey -> CompletableFuture.runAsync(() -> {
                    JSONObject hotlist = hotlistData.getJSONObject(hotlistKey);
                    JSONObject payload = hotlist.getJSONObject("payload");
                    String owner = hotlist.getString("owner");
                    
                    String albatrossTkn = "";
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
                }, executor))
                .toArray(CompletableFuture[]::new))
                .join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateHotlistTimestamps(int hotlistId, String createdOn, String hotlistKey) {
        Map<String, String> timestamps = hotlistTimestampScenarios.get(createdOn);
        String createdOnTimestamp = timestamps.get("createdOn");
        String updatedOnTimestamp = timestamps.get("updatedOn");

        Response updateResponse = ReaperIntegration.updateHotlistTimestamp(hotlistId, createdOnTimestamp, updatedOnTimestamp);
        if (updateResponse.getStatusCode() != 200) {
            Assert.fail("Failed to update the hotlist timestamps for hotlist: " + hotlistKey);
        }
    }

    public <E extends Comparable<E>> boolean isSorted(List<E> list, String order) {
        if (list == null || list.size() <= 1) return true;
        
        for (int i = 0; i < list.size() - 1; i++) {
            E current = list.get(i);
            E next = list.get(i + 1);
            
            int cmp = current.compareTo(next);
            
            if ("asc".equalsIgnoreCase(order) && cmp > 0) {
                return false;
            } else if ("desc".equalsIgnoreCase(order) && cmp < 0) {
                return false;
            }
        }
        
        return true;
    }

}
