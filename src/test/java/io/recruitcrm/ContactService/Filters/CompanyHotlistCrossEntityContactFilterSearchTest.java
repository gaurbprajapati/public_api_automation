package io.recruitcrm.ContactService.Filters;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.albatross.UpdateFields;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CompanyHotlistCrossEntityContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String ownerAlbatrossAuthToken;
    String email;
    Map<String, Integer> hotlistIdMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> companyIdToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactIdToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "companyHotlistCrossEntityFilterSearchTestData", description = "Filter Search Test for Company Hotlist Cross Entity Contact")
    public void companyHotlistCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "firstname");
        logContactIds(data);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 10, "Contact");
    }

    @DataProvider(name = "companyHotlistCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] companyHotlistCrossEntityFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactCompanyHotlistFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Hotlist")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE"), testCaseId});
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/contactCompany_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> companyIdMap = new HashMap<>();

        try {
            // Creating all companies according to payload and storing their IDs in maps
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

            // Add companies to hotlists
            addCompaniesToHotlists(companyJson, companyIdMap);
            
            // Link contacts to companies
            addContactsToCompanies(companyJson, companyIdMap);
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
                Response response = allCrudFunctions.createCompanyHotlist(albatrossURL, ownerAlbatrossAuthToken, hotlistName, companyIds);
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to add companies to hotlist: " + hotlistName);
                int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
                hotlistIdMap.put(hotlistName, hotlistId);
            }
        }

        // Creating a hotlist without any companies
        Response response = allCrudFunctions.createCompanyHotlist(albatrossURL, ownerAlbatrossAuthToken, "hotlist4", new ArrayList<>());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to create hotlist without any companies");
        int hotlistId = response.jsonPath().getInt("data.hotlist[0].id");
        hotlistIdMap.put("hotlist4", hotlistId);
    }

    private void addContactsToCompanies(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        //Creating 10 contacts
        for (int i = 1; i <= 10; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, ownerAlbatrossAuthToken);
            String contactSlug = response.jsonPath().getString("data.contact.slug");
            Integer contactId = response.jsonPath().getInt("data.contact.id");
            String contactKey = "contact" + i;
            contactKeyToSlugMap.put(contactKey, contactSlug);
            contactIdToKeyMap.put(String.valueOf(contactId), contactKey);
            contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
        }

        //Link contacts to companies
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            if (contact.isEmpty()) {
                continue;
            }
            String contactSlugs = Arrays.stream(contact.split(","))
                    .map(String::trim)
                    .map(contactKeyToSlugMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            if (contactSlugs.isEmpty()) {
                continue;
            }

            Integer companyId = companyIdMap.get(companyKey);
            UpdateFields updateFields = new UpdateFields();
            updateFields.setKey("contactid");
            updateFields.setValue(contactSlugs);
            updateFields.setTableFlag("company");
            updateFields.setId(Collections.singletonList(companyId));
            updateFields.setAddInValues(true);
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", ownerAlbatrossAuthToken, null, true, updateFields);
            linkResponse.then().statusCode(200);
        }
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
        filter.put("isCrossEntity", true);
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
            String key = (placeholder.startsWith("{") && placeholder.endsWith("}")) ? placeholder.substring(1, placeholder.length() - 1): placeholder;
    
            Integer id = hotlistIdMap.get(key);
            if (id == null) {
                Assert.fail("Wrong hotlist name provided in the filter value. Hotlist ID not found for: " + placeholder + ". Available hotlists: " + hotlistIdMap.keySet());
            }
            hotlistIds.add(id);
        }
    
        return hotlistIds;
    }

    private void logContactIds(JSONArray data) {
        if (data == null || data.length() == 0) {
            FilterSearchReporter.logInfo("<b>📋 Contact IDs:</b> No contacts returned");
            return;
        }

        StringBuilder contactIdsLog = new StringBuilder();
        contactIdsLog.append("<b>📋 Contact IDs from Returned Records:</b><br/>");
        contactIdsLog.append("<pre style='background-color: #f8f9fa; padding: 10px; border-radius: 5px;'>");
        contactIdsLog.append("<code>");

        for (int i = 0; i < data.length(); i++) {
            JSONObject contact = data.getJSONObject(i);
            Integer contactId = contact.optInt("id", -1);
            String firstName = contact.optString("firstname", "");
            String lastName = contact.optString("lastname", "");
            String contactName = (firstName + " " + lastName).trim();
            if (contactName.isEmpty()) {
                contactName = contact.optString("name", "Unknown");
            }
            String contactKey = contactIdToKeyMap.getOrDefault(String.valueOf(contactId), "Unknown");
            
            contactIdsLog.append("Record ").append(i + 1).append(": Contact ID: ").append(contactId)
                         .append(" | Key: ").append(contactKey)
                         .append(" | Name: ").append(contactName).append("\n");
        }

        contactIdsLog.append("</code></pre>");
        FilterSearchReporter.logInfo(contactIdsLog.toString());
    }
}
