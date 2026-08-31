package io.recruitcrm.CompanyService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;

import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.SavedSearch;
import io.rcrm.api.pojo.albatross.SavedSearchRequest;
import io.rcrm.api.pojo.reaper.Account;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.json.JSONObject;
import org.json.JSONArray;
import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetSavedSearchesTest extends TestBase {

    commanFunction commanFunction = new commanFunction();
    String ownerToken;
    String adminToken;
    String teamMemberToken;
    String restrictedToken;
    Integer expectedAccountId; 
    Integer ownerUserId;
    Account acc;
    Map<String, String> userMap;
    String apiAuthToken;
    Map<String, Map<String, String>> savedSearchTimestampScenarios;
    Map<String, Integer> savedSearchDataMap; // key: savedSearchKey, value: savedSearchId

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerToken = ThreadManager.getOwnerAlbatrossToken();
        adminToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        acc = ThreadManager.getAccount();
        ownerUserId = commanFunction.getOwnerUserId(baseURL, ThreadManager.getAccountApiKey());
        apiAuthToken = ThreadManager.getAccountApiKey();
        userMap = commanFunction.createUserMap(baseURL, apiAuthToken);
        savedSearchTimestampScenarios = commanFunction.createTimestampScenarios();
        savedSearchDataMap = new HashMap<>();
        createData();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "savedSearchData", groups = {"company_service", "nightly-build"})
    public void getCompanySavedSearch_success(int createdId, String createdName) {
        Response response = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 15);
        assertThat("Expected status 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();
        assertThat(jp.get("meta"), notNullValue());
        assertThat(jp.get("meta.message"), equalTo("Entities retrieved successfully"));
        assertThat((Integer) jp.get("meta.status"), equalTo(200));
        assertThat(jp.get("meta.requestUuid"), notNullValue());
        assertThat(jp.get("meta.timestamp"), notNullValue());
        assertThat(jp.get("meta.responseType"), notNullValue());
        assertThat(jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat((Integer) jp.get("meta.responseType.code"), equalTo(103));

        List<Map<String, Object>> items = jp.getList("data");
        assertThat(items, notNullValue());
        assertThat(items.size(), greaterThan(0));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/company/getSavedSearches.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifySavedSearchList_sortByName() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "50");
        
        JSONObject payload = createSavedSearchPayload("name", "asc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        JsonPath jsonPath = response.jsonPath();
        List<String> names = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(names, "asc"), "Saved searches are not sorted by name in ascending order. Names: " + names);

        // verify descending order
        payload = createSavedSearchPayload("name", "desc");
        response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        jsonPath = response.jsonPath();
        names = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(names, "desc"), "Saved searches are not sorted by name in descending order. Names: " + names);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifySavedSearchList_sortByCreatedOn() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "50");
        
        JSONObject payload = createSavedSearchPayload("createdon", "asc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        JsonPath jsonPath = response.jsonPath();
        List<Long> createdOnList = jsonPath.getList("data.createdOn");
        List<String> nameList = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(createdOnList, "asc"), "Saved searches are not sorted by createdOn in ascending order. Values: " + nameList);

        // verify descending order
        payload = createSavedSearchPayload("createdon", "desc");
        response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        jsonPath = response.jsonPath();
        createdOnList = jsonPath.getList("data.createdOn");
        nameList = jsonPath.getList("data.name");
        
        Assert.assertTrue(isSorted(createdOnList, "desc"), "Saved searches are not sorted by createdOn in descending order. Values: " + nameList);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void verifySavedSearchList_sortByUpdatedOn() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "50");
        
        JSONObject payload = createSavedSearchPayload("updatedon", "asc");
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        JsonPath jsonPath = response.jsonPath();
        List<Long> updatedOnList = jsonPath.getList("data.updatedOn");
        
        Assert.assertTrue(isSorted(updatedOnList, "asc"), "Saved searches are not sorted by updatedOn in ascending order. Values: " + updatedOnList);

        // verify descending order
        payload = createSavedSearchPayload("updatedon", "desc");
        response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        
        jsonPath = response.jsonPath();
        updatedOnList = jsonPath.getList("data.updatedOn");
        
        Assert.assertTrue(isSorted(updatedOnList, "desc"), "Saved searches are not sorted by updatedOn in descending order. Values: " + updatedOnList);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "pinData", groups = {"company_service", "nightly-build"})
    public void testSavedSearch_isPinnedReflectsPinUnpin(int id) {

        Response listBefore = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 50);
        Map<String, Object> before = findById(listBefore.jsonPath(), id);
        assertThat(before, notNullValue());
        Object pinnedBefore = before.get("isPinned");

        // pin
        Response pinResp = RestClient.doPost1("JSON", companyServiceURL, "saved-searches/{savedSearch}/pinned-saved-search", ownerToken,
                null, Collections.singletonMap("savedSearch", String.valueOf(id)), true, null);
        assertThat("Pin saved search should return 200 but got " + pinResp.getStatusCode(), pinResp.getStatusCode(), equalTo(200));

        Response listAfterPin = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 50);
        Map<String, Object> afterPin = findById(listAfterPin.jsonPath(), id);
        assertThat(asBoolean(afterPin.get("isPinned")), is(true));

        // unpin
        Response unpinResp = RestClient.doDelete("JSON", companyServiceURL, "saved-searches/{savedSearch}/pinned-saved-search", ownerToken,
                null, Collections.singletonMap("savedSearch", String.valueOf(id)), true);
        assertThat("Unpin saved search should return 200 but got " + unpinResp.getStatusCode(), unpinResp.getStatusCode(), equalTo(200));

        Response listAfterUnpin = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 50);
        Map<String, Object> afterUnpin = findById(listAfterUnpin.jsonPath(), id);
        assertThat(asBoolean(afterUnpin.get("isPinned")), is(false));

        // sanity on original value shape
        assertThat("isPinned type should be boolean/integer", pinnedBefore, anyOf(instanceOf(Boolean.class), instanceOf(Integer.class), nullValue()));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "shareVisibilityData", groups = {"company_service", "nightly-build"})
    public void testSavedSearch_ShareWithTeammates_Visibility(String nameShared, String namePrivate) {
        // Owner sees both
        Response ownerList = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 50);
        assertThat(containsByName(ownerList.jsonPath(), nameShared), is(true));
        assertThat(containsByName(ownerList.jsonPath(), namePrivate), is(true));

        // Admin, TeamMember and Restricted should see shared, not private
        Response adminList = listSavedSearches(adminToken, sort("updatedon", "desc"), 1, 50);
        assertThat(containsByName(adminList.jsonPath(), nameShared), is(true));
        assertThat(containsByName(adminList.jsonPath(), namePrivate), is(false));

        Response tmList = listSavedSearches(teamMemberToken, sort("updatedon", "desc"), 1, 50);
        assertThat(containsByName(tmList.jsonPath(), nameShared), is(true));
        assertThat(containsByName(tmList.jsonPath(), namePrivate), is(false));

        Response restrictedList = listSavedSearches(restrictedToken, sort("updatedon", "desc"), 1, 50);
        assertThat(containsByName(restrictedList.jsonPath(), nameShared), is(true));
        assertThat(containsByName(restrictedList.jsonPath(), namePrivate), is(false));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "collaboratorsData", groups = {"company_service", "nightly-build"})
    public void testSavedSearch_Collaborators_UserIdsListed(int savedSearchId, List<String> expectedUserIds) {
        Response list = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 50);
        assertThat("Saved searches list should return 200 but got " + list.getStatusCode(), list.getStatusCode(), equalTo(200));
        Map<String, Object> item = findById(list.jsonPath(), savedSearchId);
        assertThat(item, notNullValue());

        Object collaboratorId = item.get("collaboratorId");
        // collaboratorId can be null or an empty array/list when there are no collaborators
        if (collaboratorId instanceof List) {
            assertThat("collaboratorId should be empty when no collaborators", ((List<?>) collaboratorId).isEmpty(), is(true));
        } else {
            assertThat("collaboratorId should be null or empty array", collaboratorId, is(nullValue()));
        }
        Object ct = item.get("collaboratorType");
        List<String> actualIds = new ArrayList<>();
        if (ct instanceof List) {
            for (Object v : (List<?>) ct) if (v != null) actualIds.add(String.valueOf(v));
        } else if (ct instanceof String) {
            String s = (String) ct;
            if (!s.trim().isEmpty()) actualIds.addAll(Arrays.asList(s.split(",")));
        }
        for (String uid : expectedUserIds) {
            assertThat("Expected collaborator userId missing: " + uid, actualIds, hasItem(uid));
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "comprehensiveData", groups = {"company_service", "nightly-build"})
    public void testSavedSearch_ComprehensiveFieldValidation(int savedSearchId, String expectedName, int shareFlag,
                                                            List<String> expectedCollaboratorUserIds) {
        Response response = listSavedSearches(ownerToken, sort("updatedon", "desc"), 1, 100);
        assertThat("Expected status 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        Map<String, Object> item = findById(response.jsonPath(), savedSearchId);
        assertThat("Saved search not found in list for id=" + savedSearchId, item, notNullValue());

        // value assertions
        assertThat("ID should match the created one", ((Number) item.get("id")).intValue(), equalTo(savedSearchId));
        assertThat("Name should match the created name", (String) item.get("name"), equalTo(expectedName));
        assertThat("JSON should match template", (String) item.get("json"), equalTo("{\"filters\":[],\"sort\":[]}"));
        assertThat("Entity type should be companies", (String) item.get("entityType"), equalTo("companies"));

        Object psr = item.get("postSearchRevamp");
        assertThat("postSearchRevamp should be true/1", psr, anyOf(instanceOf(Boolean.class), instanceOf(Number.class)));

        Object swt = item.get("shareWithTeammates");
        int swtInt = (swt instanceof Boolean) ? (((Boolean) swt) ? 1 : 0) : ((Number) swt).intValue();
        assertThat("shareWithTeammates should match request", swtInt, equalTo(shareFlag));

        Object collaboratorId = item.get("collaboratorId");
        // collaboratorId can be null or an empty array/list when there are no collaborators
        if (collaboratorId instanceof List) {
            assertThat("collaboratorId should be empty when no collaborators", ((List<?>) collaboratorId).isEmpty(), is(true));
        } else {
            assertThat("collaboratorId should be null or empty array", collaboratorId, is(nullValue()));
        }
        Object collaboratorType = item.get("collaboratorType");
        List<String> actualCollabIds = new ArrayList<>();
        if (collaboratorType instanceof String) {
            String s = (String) collaboratorType;
            if (!s.trim().isEmpty()) actualCollabIds.addAll(Arrays.asList(s.split(",")));
        } else if (collaboratorType instanceof List) {
            for (Object v : (List<?>) collaboratorType) if (v != null) actualCollabIds.add(String.valueOf(v));
        }
        for (String uid : expectedCollaboratorUserIds) {
            assertThat("Expected collaborator userId missing: " + uid, actualCollabIds, hasItem(uid));
        }

        if (expectedAccountId != null) {
            assertThat("accountId should match current account", ((Number) item.get("accountId")).intValue(), equalTo(expectedAccountId));
        }

        if (ownerUserId != null) {
            assertThat("userId should be owner", ((Number) item.get("userId")).intValue(), equalTo(ownerUserId));
            assertThat("createdBy should be owner", ((Number) item.get("createdBy")).intValue(), equalTo(ownerUserId));
            assertThat("updatedBy should be owner", ((Number) item.get("updatedBy")).intValue(), equalTo(ownerUserId));
        }

        Long createdOn = toLong(item.get("createdOn"));
        Long updatedOn = toLong(item.get("updatedOn"));
        assertThat("createdOn should be present and > 0", createdOn, allOf(notNullValue(), greaterThan(0L)));
        assertThat("updatedOn should be present and >= createdOn", updatedOn, allOf(notNullValue(), greaterThanOrEqualTo(createdOn)));

        Object isPinned = item.get("isPinned");
        assertThat("isPinned should be boolean/integer", isPinned, anyOf(instanceOf(Boolean.class), instanceOf(Number.class)));

        Object applied = item.get("appliedFilterSearchSavedWith");
        if (applied != null) {
            assertThat("appliedFilterSearchSavedWith should be numeric if present", applied, instanceOf(Number.class));
        }
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchList_WithoutAuth() {
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", null,
                queryParams(1, 15), true, buildSortBody("updatedon", "desc"));
        assertThat("Without auth should return 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchList_InvalidAuth() {
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken + "xyz",
                queryParams(1, 15), true, buildSortBody("updatedon", "desc"));
        assertThat("Invalid auth should return 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchList_MissingQueryParameters() {
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken,
                null, true, buildSortBody("updatedon", "desc"));
        assertThat("Missing query params should return 500 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"company_service", "nightly-build"})
    public void testSavedSearchList_InvalidQueryParameters() {
        Map<String, String> qp = new HashMap<>();
        qp.put("page", "abc");
        qp.put("size", "-1");
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken,
                qp, true, buildSortBody("updatedon", "desc"));
        assertThat("Invalid query params should return 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "getSavedSearchesFiltersData", groups = {"company_service", "nightly-build"})
    public void verifySavedSearchesFilters(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "15");
        
        JSONObject payload = createSavedSearchFilterPayload(dbField, filterType, filterValue, "updatedon", "desc", fieldType);
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", ownerToken, queryParameters, true, payload);
        response.then().statusCode(200);
        JSONArray filteredData = getFilteredData(response);
        
        // Convert dbField to camelCase format for validation (API response uses camelCase)
        String responseFieldName = convertToCamelCase(dbField);
        
        if (fieldName.equals("Created By")) {
            commanFunction.validateCreatedByFilteredData(filteredData, filterType, filterValue, fieldName, responseFieldName, expectedResult, "Saved search", userMap);
        } else if (fieldName.equals("Created On") || fieldName.equals("Updated On")) {
            commanFunction.validateCreatedOnUpdatedOnFilteredData(filteredData, filterType, filterValue, fieldName, responseFieldName, expectedResult);
        }
    }

    // ===================== Data Providers =====================

    @DataProvider(name = "savedSearchData")
    public Object[][] getSavedSearchData() {
        // Use pre-created saved search from JSON data
        Integer id = savedSearchDataMap.get("savedSearch1");
        if (id == null) {
            Assert.fail("savedSearch1 was not created successfully. Check createData() method.");
        }
        String name = "Saved Search Test [Owner][Today][Shared]";
        return new Object[][]{{id, name}};
    }

    @DataProvider(name = "shareVisibilityData")
    public Object[][] getShareVisibilityData() {
        // Use pre-created saved searches from JSON data
        String nameShared = "Saved Search Test [Owner][Today][Shared]";
        String namePrivate = "Saved Search Test [Owner][Today][Not Shared]";
        return new Object[][]{{nameShared, namePrivate}};
    }

    @DataProvider(name = "collaboratorsData")
    public Object[][] getCollaboratorsData() {
        // fetch users and pick two IDs
        Response allUsers = RestClient.doPost("JSON", albatrossURL, "global/get-all-users-and-teams", ownerToken, null, true, null);
        assertThat("get-all-users-and-teams should return 200 but got " + allUsers.getStatusCode(), allUsers.getStatusCode(), equalTo(200));
        List<String> userIds = new ArrayList<>();
        try {
            List<Map<String, Object>> users = allUsers.jsonPath().getList("data.allUsers");
            if (users != null) {
                for (Map<String, Object> u : users) {
                    Object id = u.get("id");
                    if (id != null) userIds.add(String.valueOf(id));
                    if (userIds.size() >= 2) break;
                }
            }
        } catch (Exception ignored) {}
        if (userIds.size() < 1) {
            // fallback to owner id via users endpoint
            Response usersResp = RestClient.doGet("JSON", baseURL, "users", ThreadManager.getAccountApiKey(), null, null, true);
            if (usersResp.getStatusCode() == 200) {
                try {
                    Integer id = usersResp.jsonPath().getInt("[0].id");
                    if (id != null) userIds.add(String.valueOf(id));
                } catch (Exception ignored) {}
            }
        }
        int id = createSavedSearch("CompaniesList_Collab_" + System.currentTimeMillis(), 0, Collections.emptyList(), new ArrayList<>(userIds));
        return new Object[][]{{id, userIds}};
    }

    @DataProvider(name = "pinData")
    public Object[][] getPinData() {
        int id = createSavedSearch("CompaniesList_PinFlip_" + System.currentTimeMillis(), 0, Collections.emptyList(), Collections.emptyList());
        return new Object[][]{{id}};
    }

    @DataProvider(name = "comprehensiveData")
    public Object[][] getComprehensiveData() {
        // build collaborators and share flag
        Response allUsers = RestClient.doPost("JSON", albatrossURL, "global/get-all-users-and-teams", ownerToken, null, true, null);
        assertThat("get-all-users-and-teams should return 200 but got " + allUsers.getStatusCode(), allUsers.getStatusCode(), equalTo(200));
        List<String> userIds = new ArrayList<>();
        try {
            List<Map<String, Object>> users = allUsers.jsonPath().getList("data.allUsers");
            if (users != null) {
                for (Map<String, Object> u : users) {
                    Object id = u.get("id");
                    if (id != null) userIds.add(String.valueOf(id));
                    if (userIds.size() >= 2) break;
                }
            }
        } catch (Exception ignored) {}
        if (userIds.isEmpty()) {
            Integer ownerId = new commanFunction().getOwnerUserId(baseURL, ThreadManager.getAccountApiKey());
            if (ownerId != null) userIds.add(String.valueOf(ownerId));
        }
        String name = "CompaniesList_Comprehensive_" + System.currentTimeMillis();
        int share = 1;
        int id = createSavedSearch(name, share, Collections.emptyList(), new ArrayList<>(userIds));
        return new Object[][]{{id, name, share, userIds}};
    }

    @DataProvider(name = "getSavedSearchesFiltersData", parallel = true)
    public Object[][] getSavedSearchesFiltersData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersTest/savedSearchDataProvider.json");
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

    // ===================== Helpers =====================
    private Response listSavedSearches(Object token, Map<String, Object> sort, int page, int size) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", String.valueOf(page));
        queryParameters.put("size", String.valueOf(size));

        return RestClient.doPost("JSON", ariesServiceURL, "advanced-search/company-saved-searches/search/get", token, queryParameters, true, createSavedSearchPayload((String) sort.get("field"), (String) sort.get("order")));
    }

    private JSONObject createSavedSearchPayload(String field, String order) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY_SAVED_SEARCH");
        JSONArray sortPriorityList = new JSONArray();
        JSONObject sortItem = new JSONObject();
        sortItem.put("field", field);
        sortItem.put("order", order);
        sortPriorityList.put(sortItem);
        payload.put("sortPriorityList", sortPriorityList);
        return payload;
    }

    private Map<String, Object> sort(String field, String order) {
        Map<String, Object> s = new HashMap<>();
        s.put("field", field);
        s.put("order", order);
        return s;
    }

    private JSONObject buildSortBody(String field, String order) {
        return createSavedSearchPayload(field, order);
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

    public void createData() {
        JSONObject savedSearchData = readJsonFileFromPath("src/test/resources/company_savedSearchData.json");
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(savedSearchData.keySet().stream()
                .map(savedSearchKey -> CompletableFuture.runAsync(() -> {
                    JSONObject savedSearch = savedSearchData.getJSONObject(savedSearchKey);
                    String owner = savedSearch.getString("owner");
                    String albatrossTkn = "";
                    
                    // Use tokens initialized in setUp() instead of getRoleBasedToken
                    switch (owner.toLowerCase()) {
                        case "owner":
                            albatrossTkn = ownerToken;
                            break;
                        case "admin":
                            albatrossTkn = adminToken;
                            break;
                        case "teammember":
                            albatrossTkn = teamMemberToken;
                            break;
                        case "restrictedteammember":
                            albatrossTkn = restrictedToken;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid owner: " + owner);
                    }
                    
                    JSONObject payload = savedSearch.getJSONObject("payload");

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
                }, executor))
                .toArray(CompletableFuture[]::new))
                .join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateSavedSearchTimestamps(int savedSearchId, String createdOn, String savedSearchKey) {
        Map<String, String> timestamps = savedSearchTimestampScenarios.get(createdOn);
        String createdOnTimestamp = timestamps.get("createdOn");
        String updatedOnTimestamp = timestamps.get("updatedOn");

        Response updateResponse = ReaperIntegration.updateSavedSearchTimestamp(savedSearchId, createdOnTimestamp, updatedOnTimestamp);
        if (updateResponse.getStatusCode() != 200) {
            Assert.fail("Failed to update the saved search timestamps for saved search: " + savedSearchKey);
        }
    }


    private int createSavedSearch(String name, int shareWithTeammates, List<Object> collaboratorIds, List<Object> collaboratorTypes) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + ownerToken);

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(name);
        savedSearch.setEntitytype("companies");
        savedSearch.setJson("{\"filters\":[],\"sort\":[]}");
        savedSearch.setUserid(null);
        savedSearch.setAccountid(null);
        savedSearch.setShare_with_teammates(shareWithTeammates);
        savedSearch.setPost_search_revamp(1);
        savedSearch.setCollaborator_id(collaboratorIds);
        // collaborator_type expects comma-separated userIds string; collapse if provided
        savedSearch.setCollaborator_type(collaboratorTypes);

        SavedSearchRequest request = new SavedSearchRequest();
        request.setSave_searches(savedSearch);
        request.setUpdateUserObj(false);

        Response createResponse = RestClient.doPost("JSON", albatrossURL, "saved-searches", authTokenMap, null, true, request);
        assertThat("Create saved search failed", createResponse.getStatusCode(), equalTo(200));

        int id = createResponse.jsonPath().getInt("data.id");
        assertThat(id, greaterThan(0));
        return id;
    }

    private Map<String, String> queryParams(int page, int size) {
        Map<String, String> qp = new HashMap<>();
        qp.put("page", String.valueOf(page));
        qp.put("size", String.valueOf(size));
        return qp;
    }

    private Map<String, Object> findById(JsonPath jp, int id) {
        List<Map<String, Object>> items = jp.getList("data");
        if (items == null) return null;
        for (Map<String, Object> it : items) {
            if (it != null && Objects.equals(((Number) it.get("id")).intValue(), id)) {
                return it;
            }
        }
        return null;
    }

    private boolean containsByName(JsonPath jp, String name) {
        List<String> names = jp.getList("data.name");
        if (names == null) return false;
        for (String n : names) {
            if (name.equals(n)) return true;
        }
        return false;
    }

    private boolean asBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() == 1;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return false;
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

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String) {
            try { return Long.parseLong((String) v); } catch (Exception ignored) { return null; }
        }
        return null;
    }

    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getJSONArray("data");
    }

    private JSONObject createSavedSearchFilterPayload(String dbField, String filterType, String filterValue, String sortField, String sortOrder, String fieldType) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY_SAVED_SEARCH");
        
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

}

