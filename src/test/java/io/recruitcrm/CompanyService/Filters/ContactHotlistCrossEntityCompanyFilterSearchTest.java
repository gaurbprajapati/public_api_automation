package io.recruitcrm.CompanyService.Filters;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactHotlistCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String accountOwnerAPIKey;
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companySlugToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, List<JsonPath>> contactDataMap = new ConcurrentHashMap<>();
    Map<String, Integer> hotlistIdMap = new HashMap<>();
    Map<String, Integer> contactSlugToIdMap = new ConcurrentHashMap<>();
    Map<String, Integer> companyKeyToMainContactIdMap = new ConcurrentHashMap<>();
    Map<String, Integer> companyKeyToAdditionalContactIdMap = new ConcurrentHashMap<>();
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactHotlistCrossEntityFilterSearchTestData", description = "Filter Search Test for Contact Hotlist Cross Entity Company")
    public void contactHotlistCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/companyContact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String contactSlug = jsonPath.getString("data.contact.slug");
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        
                        // Skip if company ID is null (company creation failed)
                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }
                        
                        Integer companyId = Integer.parseInt(companyIdStr);
                        String companySlug = jsonPath.getString("data.company.slug");

                        // Store lookup maps for matching companies in response (always store company info)
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey.toLowerCase(), String.valueOf(companyId));
                        }
                        synchronized (companyIdToKeyMap) {
                            companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                        }
                        synchronized (companySlugToKeyMap) {
                            companySlugToKeyMap.put(companySlug, companyKey);
                        }

                        // Only process contact-related operations if contact ID is not null
                        if (contactIdStr != null && contactSlug != null && !contactSlug.isEmpty()) {
                            Integer contactId = Integer.parseInt(contactIdStr);

                            // Store contact slug to ID mapping for adding to hotlists
                            synchronized (contactSlugToIdMap) {
                                contactSlugToIdMap.put(contactSlug, contactId);
                            }
                            
                            // Store company key to main contact ID mapping
                            synchronized (companyKeyToMainContactIdMap) {
                                companyKeyToMainContactIdMap.put(companyKey, contactId);
                            }

                            // Get contact for the company and store JsonPath as a list
                            try {
                                Response contactResponse = getContact(contactSlug);
                                JsonPath contactJsonPath = contactResponse.jsonPath();
                                
                                synchronized (contactDataMap) {
                                    List<JsonPath> contactList = new ArrayList<>();
                                    contactList.add(contactJsonPath);
                                    contactDataMap.put(companySlug, contactList);
                                }
                            } catch (Exception e) {
                                System.out.println("Warning: Could not fetch contact for " + companyKey + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("Skipping contact operations for " + companyKey + " - contact ID or slug is null");
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();

            // Create additional contacts for companies 2, 4, and 6
            createAdditionalContacts(companyJson);

            // Add contacts to hotlists
            addContactsToHotlists(companyJson);

        } finally {
            executor.shutdown();
        }
    }

    private void createAdditionalContacts(JSONObject companyJson) {
        String[] companiesToAddContacts = {"company2", "company4", "company6"};
        
        for (String companyKey : companiesToAddContacts) {
            if (companyJson.has(companyKey)) {
                JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                if (companyEntry.has("additionalContact")) {
                    JSONObject additionalContact = companyEntry.getJSONObject("additionalContact");
                    if (additionalContact.has("contact1")) {
                        JSONObject contact1 = additionalContact.getJSONObject("contact1");
                        JSONObject payload = contact1.getJSONObject("payload");
                        
                        // Get company slug and ID from the maps
                        String companySlug = null;
                        String companyId = null;
                        
                        for (Map.Entry<String, String> entry : companySlugToKeyMap.entrySet()) {
                            if (entry.getValue().equals(companyKey)) {
                                companySlug = entry.getKey();
                                break;
                            }
                        }
                        
                        for (Map.Entry<String, String> entry : companyIdToKeyMap.entrySet()) {
                            if (entry.getValue().equals(companyKey)) {
                                companyId = entry.getKey();
                                break;
                            }
                        }
                        
                        if (companySlug != null && companyId != null) {
                            // Update payload with actual company slug and ID
                            JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                            if (selectedCompanies.length() > 0) {
                                JSONObject companyInfo = selectedCompanies.getJSONObject(0);
                                companyInfo.put("slug", companySlug);
                                companyInfo.put("id", companyId);
                            }
                            
                            // Create the additional contact
                            try {
                                Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", 
                                        albatrossAuthToken, null, true, payload);
                                response.then().statusCode(200);
                                
                                JsonPath contactJsonPath = response.jsonPath();
                                String contactSlug = contactJsonPath.getString("data.contact.slug");
                                String contactIdStr = contactJsonPath.getString("data.contact.id");
                                
                                // Skip if contact ID is null
                                if (contactIdStr == null) {
                                    System.err.println("Failed to create additional contact for " + companyKey + " - contact ID is null");
                                    return;
                                }
                                
                                Integer contactId = Integer.parseInt(contactIdStr);
                                
                                // Store additional contact ID for hotlist assignment
                                synchronized (contactSlugToIdMap) {
                                    contactSlugToIdMap.put(contactSlug, contactId);
                                }
                                
                                // Store company key to additional contact ID mapping
                                synchronized (companyKeyToAdditionalContactIdMap) {
                                    companyKeyToAdditionalContactIdMap.put(companyKey, contactId);
                                }
                                
                                // Store additional contact in contactDataMap
                                Response contactResponse = getContact(contactSlug);
                                JsonPath additionalContactJsonPath = contactResponse.jsonPath();
                                
                                synchronized (contactDataMap) {
                                    List<JsonPath> contactList = contactDataMap.get(companySlug);
                                    if (contactList != null) {
                                        contactList.add(additionalContactJsonPath);
                                    }
                                }
                                
                                System.out.println("Successfully created additional contact for " + companyKey);
                            } catch (Exception e) {
                                System.err.println("Failed to create additional contact for " + companyKey + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    private void addContactsToHotlists(JSONObject companyJson) {
        Map<String, List<Integer>> hotlistContactIdsMap = new HashMap<>();
        
        // Group contacts by their hotlist assignments from main contacts
        for (String companyKey : companyJson.keySet()) {
            if (!companyKey.startsWith("company")) {
                continue;
            }
            
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            if (!companyEntry.has("hotlist") || companyEntry.isNull("hotlist")) {
                continue;
            }
            
            String contactHotlists = companyEntry.getString("hotlist");
            if (contactHotlists == null || contactHotlists.isEmpty()) {
                continue;
            }
            
            // Get main contact ID from the map
            Integer contactId = companyKeyToMainContactIdMap.get(companyKey);
            if (contactId != null) {
                String[] hotlistNames = contactHotlists.split(",");
                for (String hotlistName : hotlistNames) {
                    hotlistName = hotlistName.trim();
                    if (!hotlistName.isEmpty()) {
                        hotlistContactIdsMap.computeIfAbsent(hotlistName, k -> new ArrayList<>()).add(contactId);
                    }
                }
            }
        }
        
        // Group additional contacts by their hotlist assignments
        String[] companiesWithAdditionalContacts = {"company2", "company4", "company6"};
        for (String companyKey : companiesWithAdditionalContacts) {
            if (companyJson.has(companyKey)) {
                JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                if (companyEntry.has("additionalContact")) {
                    JSONObject additionalContact = companyEntry.getJSONObject("additionalContact");
                    if (additionalContact.has("contact1")) {
                        JSONObject contact1 = additionalContact.getJSONObject("contact1");
                        JSONObject payload = contact1.getJSONObject("payload");
                        
                        if (payload.has("hotlist") && !payload.isNull("hotlist")) {
                            String contactHotlists = payload.getString("hotlist");
                            if (contactHotlists != null && !contactHotlists.isEmpty()) {
                                // Get additional contact ID from the map
                                Integer contactId = companyKeyToAdditionalContactIdMap.get(companyKey);
                                if (contactId != null) {
                                    String[] hotlistNames = contactHotlists.split(",");
                                    for (String hotlistName : hotlistNames) {
                                        hotlistName = hotlistName.trim();
                                        if (!hotlistName.isEmpty()) {
                                            hotlistContactIdsMap.computeIfAbsent(hotlistName, k -> new ArrayList<>()).add(contactId);
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
                hotlistIdMap.put(hotlistName, hotlistId);
            }
        }

        // Creating a hotlist without any contacts
        Response response = createContactHotlist("hotlist4", new ArrayList<>());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create hotlist without any contacts");
        int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
        hotlistIdMap.put("hotlist4", hotlistId);
    }

    private Response createContactHotlist(String hotlistName, List<Integer> contactIds) {
        JSONObject payload = new JSONObject();
        payload.put("entity_name", "contacts");
        JSONArray selectedRows = new JSONArray();
        for (Integer contactId : contactIds) {
            selectedRows.put(contactId);
        }
        payload.put("selectedrows", selectedRows);
        payload.put("shared", true);
        payload.put("name", new JSONArray().put(hotlistName));
        payload.put("updateUserObj", false);
        payload.put("from_add_to_hotlist_modal", true);
        
        Response response = RestClient.doPost("JSON", albatrossURL, "/hotlists", albatrossAuthToken, null, true, payload);
        return response;
    }

    public Response getContact(String contactSlug) {
        String basePath = "/contacts/{contactSlug}";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("contactSlug", contactSlug);
        Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParams, true);
        response.then().statusCode(200);
        return response;
    }

    @DataProvider(name = "contactHotlistCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactHotlistFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), test.optString("testCaseId", "")});
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
        filter.put("isCrossEntity", true);
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
}
