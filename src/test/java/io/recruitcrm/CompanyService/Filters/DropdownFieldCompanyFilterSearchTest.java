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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DropdownFieldCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    Map<String, Integer> industryMap;
    Map<String, String> userMap;
    Map<String, String> teamMap;
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
        industryMap = createIndustryMap();
        userMap = createUserMap();
        teamMap = createTeamMap();
        createTestData();
        waitForDataSync();
    }


    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "ownerDropdownFieldFilterSearchTestData", description = "Filter Search Test for Dropdown Fields")
    public void ownerDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "ownername");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");

    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "createdByDropdownFieldFilterSearchTestData", description = "Filter Search Test for Dropdown Fields")
    public void createdByDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "creatorname");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "updatedByDropdownFieldFilterSearchTestData", description = "Filter Search Test for Dropdown Fields")
    public void updatedByDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "updatorname");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "industryDropdownFieldFilterSearchTestData", description = "Filter Search Test for Dropdown Fields")
    public void industryDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "industry");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @DataProvider(name = "ownerDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] ownerDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if(key.equals("Owner")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "createdByDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] createdByDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if(key.equals("CreatedBy")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "updatedByDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] updatedByDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if(key.equals("UpdatedBy")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "industryDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] industryDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if(key.equals("Industry")) {
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

        
        try {
            //Creating all companies according to payload and storing their IDs in a map
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
                    String createdBy = companyEntry.getString("createdBy");
                    String albatrossAuthToken = getAlbatrossAuthToken(createdBy);
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
                Integer companyId = entry.getValue().getValue();

                // Store mappings for validation
                companyKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(companyId));
                companyIdToKeyMap.put(String.valueOf(companyId), entry.getKey().toLowerCase());
            }
        } finally {
            executor.shutdown();
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedTeamMember":
                return restrictedTeamMemberAlbatrossAuthToken;
        }
        return null;
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Parse filterValue to extract hotlist IDs from placeholders like {hotlist1},{hotlist2}
        String processedFilterValue = processFilterValue(filterValue, fieldName);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("ENTITY_ASSOCIATION")) {
            filterValueObj = entityAssociationFilterValue(processedFilterValue);
        } else if (filterValue_TYPE.equals("INTEGER_LIST")) {
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

    private String processFilterValue(String filterValue, String fieldName) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;

        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0); 
            String fieldKey = matcher.group(1);

            String actualValue = null;
            if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            } else if (fieldKey.startsWith("owner") || fieldKey.startsWith("admin") || fieldKey.startsWith("restrictedTeamMember") || fieldKey.startsWith("teamMember")) {
                actualValue = userMap.get(fieldKey);
            } else if (fieldName.equals("Industry")) {
                actualValue = String.valueOf(industryMap.get(fieldKey));
            }
            
            if (actualValue != null) {
                processedValue = processedValue.replace(placeholder, actualValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder);
            }
        }

        return processedValue;
    }

    public Map<String,String> createUserMap() {
        Map<String,String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restrictedTeamMember", user.get("[2].id").toString());
        userMap.put("teamMember", user.get("[3].id").toString());
        return userMap;
    }

    public Map<String,String> createTeamMap() {
        Map<String,String> teamMap = new HashMap<>();
        ArrayList<String> userId = new ArrayList<String>();
		userId.add(String.valueOf(userMap.get("owner")));
		userId.add(String.valueOf(userMap.get("teamMember")));

		Response response = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId);
		response.then().statusCode(200);
        Response team = function.getTeams(baseURL, apiKey);
        System.out.println("Team response: "+team.prettyPrint());
        String teamId = team.jsonPath().getString("[0].team_id");
        teamMap.put("team", teamId);
        return teamMap;
    }

    public Map<String,Integer> createIndustryMap() {
        return allCrudFunctions.getIndustryIdMapWithRetry(albatrossURL, ownerAlbatrossAuthToken);
    }

    



    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "ownerDropdownFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Dropdown Fields")
    public void ownerDropdownFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        ownerDropdownFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "ownerDropdownFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] ownerDropdownFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(ownerDropdownFieldFilterSearchTestData());
    }
}