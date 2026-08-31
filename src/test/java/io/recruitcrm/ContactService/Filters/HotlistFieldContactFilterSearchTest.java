package io.recruitcrm.ContactService.Filters;

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
import io.rcrm.api.pojo.candidateService.AddToHotlistRequest;
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
public class HotlistFieldContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String albatrossAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    Map<String, Integer> hotlistIdMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>(); // Maps contact key (e.g., "contact1") to contact ID string
    Map<String, String> contactIdToKeyMap = new HashMap<>(); // Maps contact ID string to contact key (e.g., "contact1")
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        String apiKey = ThreadManager.getAccountApiKey();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        albatrossAuthToken = ownerAlbatrossAuthToken;
        createCompanies();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "hotlistFieldFilterContactSearchTestData", description = "Filter Search Test for Hotlist Field")
    public void hotlistFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @DataProvider(name = "hotlistFieldFilterContactSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactHotlistFilterDataProvider.json");
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

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            // Create only companies 1-5
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String companyKey = "company" + i;
                if (companyJson.has(companyKey)) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, authToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }
                        
                        Integer companyId = Integer.parseInt(companyIdStr);
                        
                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                        }
                    }, executor));
                }
            }

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
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
        return ownerAlbatrossAuthToken;
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
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
        filter.put("groupType", "contacts");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "contact");
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
    
        // If filterValue is empty or null, return empty list
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return hotlistIds;
        }
    
        for (String raw : filterValue.split(",")) {
            String placeholder = raw.trim();
            if (placeholder.isEmpty()) continue;
    
            // Normalize: remove {}
            String key = (placeholder.startsWith("{") && placeholder.endsWith("}")) ? placeholder.substring(1, placeholder.length() - 1) : placeholder;
    
            Integer id = hotlistIdMap.get(key);
            // If hotlist ID is not found, fail the test
            if (id == null) {
                Assert.fail("Wrong hotlist name provided in the filter value. Hotlist ID not found for: " + placeholder + ". Available hotlists: " + hotlistIdMap.keySet());
            }
            hotlistIds.add(id);
        }
    
        return hotlistIds;
    }

    private void createTestData() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> contactIdMap = new HashMap<>();
        
        try {
            // Creating all contacts according to payload and storing their IDs in maps
            List<CompletableFuture<Void>> createFutures = contactJson.keySet().stream()
                .filter(key -> key.startsWith("contact"))
                .map(contactKey -> CompletableFuture.runAsync(() -> {
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject payload = contactEntry.getJSONObject("payload");
                    String createdBy = contactEntry.getString("createdBy");
                    String authToken = getAlbatrossAuthToken(createdBy);
                    
                    // Replace company placeholders in selectedcompanies
                    if (payload.has("selectedcompanies")) {
                        JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                        for (int i = 0; i < selectedCompanies.length(); i++) {
                            JSONObject companyInfo = selectedCompanies.getJSONObject(i);
                            String slugPlaceholder = companyInfo.optString("slug", "");
                            String idPlaceholder = companyInfo.optString("id", "");
                            
                            // Extract company key from placeholder like {company1_slug} or {company1_id}
                            String companyKey = null;
                            if (slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                                companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6); // Remove { and _slug}
                            } else if (idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                                companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4); // Remove { and _id}
                            }
                            
                            if (companyKey != null) {
                                String actualSlug = companyKeyToSlugMap.get(companyKey);
                                String actualId = companyKeyToIdMap.get(companyKey);
                                
                                if (actualSlug != null && actualId != null) {
                                    companyInfo.put("slug", actualSlug);
                                    companyInfo.put("id", actualId);
                                }
                            }
                        }
                    }
                    
                    Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", authToken, null, true, payload);
                    response.then().statusCode(200);
                    JsonPath jsonPath = response.jsonPath();
                    String slug = jsonPath.getString("data.contact.slug");
                    String contactIdStr = jsonPath.getString("data.contact.id");
                    
                    // Skip if contact ID is null
                    if (contactIdStr == null) {
                        System.out.println("Skipping " + contactKey + " - contact ID is null");
                        return;
                    }
                    
                    Integer contactId = Integer.parseInt(contactIdStr);
                    
                    // Store mappings for validation
                    contactKeyToIdMap.put(contactKey.toLowerCase(), String.valueOf(contactId));
                    contactIdToKeyMap.put(String.valueOf(contactId), contactKey.toLowerCase());
                    contactIdMap.put(contactKey, contactId);
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            
            addContactsToHotlists(contactJson, contactIdMap);
        } finally {
            executor.shutdown();
        }    
    }

    private void addContactsToHotlists(JSONObject contactJson, Map<String, Integer> contactIdMap) {
        Map<String, List<Integer>> hotlistContactIdsMap = new HashMap<>();
        
        // Group contacts by their hotlist assignments
        for (String contactKey : contactIdMap.keySet()) {
            JSONObject contactEntry = contactJson.getJSONObject(contactKey);
            if (!contactEntry.has("hotlist") || contactEntry.isNull("hotlist")) {
                continue;
            }
            
            String contactHotlists = contactEntry.getString("hotlist");
            if (contactHotlists == null || contactHotlists.isEmpty()) {
                continue;
            }
            
            Integer contactId = contactIdMap.get(contactKey);
            String[] hotlistNames = contactHotlists.split(",");
            for (String hotlistName : hotlistNames) {
                hotlistName = hotlistName.trim();
                if (!hotlistName.isEmpty()) {
                    hotlistContactIdsMap.computeIfAbsent(hotlistName, k -> new ArrayList<>()).add(contactId);
                }
            }
        }
        // Create hotlists with their respective contact IDs
        for (Map.Entry<String, List<Integer>> entry : hotlistContactIdsMap.entrySet()) {
            String hotlistName = entry.getKey();
            List<Integer> contactIds = entry.getValue();
            
            if (!contactIds.isEmpty()) {
                Response response = createContactHotlist(hotlistName, contactIds);
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to add contacts to hotlist: " + hotlistName);
                JsonPath jsonPath = response.jsonPath();
                String hotlistIdStr = jsonPath.getString("data.hotlist[0].id");
                if (hotlistIdStr != null) {
                    int hotlistId = Integer.parseInt(hotlistIdStr);
                    hotlistIdMap.put(hotlistName, hotlistId);
                } else {
                    Assert.fail("Failed to extract hotlist ID from response for hotlist: " + hotlistName + ". Response: " + response.getBody().asString());
                }
            }
        }

        // Creating a hotlist without any contacts
        Response response = createContactHotlist("hotlist4", new ArrayList<>());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create hotlist without any contacts");
        JsonPath jsonPath = response.jsonPath();
        String hotlistIdStr = jsonPath.getString("data.hotlist[0].id");
        if (hotlistIdStr != null) {
            int hotlistId = Integer.parseInt(hotlistIdStr);
            hotlistIdMap.put("hotlist4", hotlistId);
        } else {
            Assert.fail("Failed to extract hotlist ID from response for hotlist4. Response: " + response.getBody().asString());
        }
    }

    private Response createContactHotlist(String hotlistName, List<Integer> contactIds) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("authorization", "Bearer " + albatrossAuthToken);
        String basePath = "hotlists";
        
        AddToHotlistRequest requestBody = new AddToHotlistRequest();
        requestBody.setEntity_name("contacts");
        requestBody.setSelectedrows(contactIds.stream().mapToInt(Integer::intValue).toArray());
        requestBody.setShared(true);
        requestBody.setName(new String[]{hotlistName});
        requestBody.setUpdateUserObj(false);
        requestBody.setFrom_add_to_hotlist_modal(true);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, requestBody);
        return response;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "hotlistFieldFilterContactSearchSmokeTestData", description = "[Smoke] Filter Search Test for Hotlist Field")
    public void hotlistFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        hotlistFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "hotlistFieldFilterContactSearchSmokeTestData", parallel = true)
    public Object[][] hotlistFieldFilterContactSearchSmokeTestData() {
        return limitSmokeRows(dataProvider());
    }
}
