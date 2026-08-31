package io.recruitcrm.CompanyService.Filters;

import com.qa.api.util.reaper.ThreadManager;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import io.rcrm.api.pojo.albatross.UpdateFields;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactFieldCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();
    String albatrossAuthToken;
    String email;
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> companyIdToKeyMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<Integer, String> contactIdToKeyMap = new HashMap<>();
    Map<String, Integer> contactKeyToIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactFieldFilterCompanySearchTestData", description = "Filter Search Test for Contact Fields")
    public void contactFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");
        Assert.assertEquals(response.getStatusCode(), 200," Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"),"Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @DataProvider(name = "contactFieldFilterCompanySearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyContactFilterDataProvider.json");
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

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Parse filterValue to extract hotlist IDs from placeholders like {hotlist1},{hotlist2}
        List<Integer> contactIds = parseContactIds(filterValue);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        filterValueObj.put("type", filterValue_TYPE);
        JSONArray valueArray = new JSONArray();
        for (Integer contactId : contactIds) {
            valueArray.put(contactId);
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


    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, String> companySlugMap = new HashMap<>();
        Map<String, Integer> companyIdMap = new HashMap<>();
        
        try {
            //Creating all companies according to payload and storing their slugs and IDs in maps
            List<CompletableFuture<Map.Entry<String, Map.Entry<String, Integer>>>> createFutures = companyJson.keySet().stream()
                .filter(key -> key.startsWith("company"))
                .map(companyKey -> CompletableFuture.supplyAsync(() -> {
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject payload = companyEntry.getJSONObject("payload");
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
                companySlugMap.put(entry.getKey(), entry.getValue().getKey());
                Integer companyId = entry.getValue().getValue();
                companyIdMap.put(entry.getKey(), companyId);
                // Store mappings for validation
                companyKeyToIdMap.put(entry.getKey().toLowerCase(), String.valueOf(companyId));
                companyIdToKeyMap.put(String.valueOf(companyId), entry.getKey().toLowerCase());
            }

            addContactsToCompanies(companyJson, companyIdMap);
        } finally {
            executor.shutdown();
        }    
    }

    private void addContactsToCompanies(JSONObject companyJson, Map<String, Integer> companyIdMap) {
        //Creating 4 contacts
        for (int i = 1; i <= 4; i++) {
            Response response = allCrudFunctions.createContact(albatrossURL, albatrossAuthToken);
            contactKeyToSlugMap.put("contact" + i, response.jsonPath().getString("data.contact.slug"));
            contactIdToKeyMap.put(response.jsonPath().getInt("data.contact.id"), "contact" + i);
            contactKeyToIdMap.put("contact" + i, response.jsonPath().getInt("data.contact.id"));
        }   

        //Link contacts to companies
        for (String companyKey : companyIdMap.keySet()) {
            JSONObject companyEntry = companyJson.getJSONObject(companyKey);
            String contact = companyEntry.optString("contact", "").trim();
            // Skip empty contact
            if (contact.isEmpty()) {
                continue;
            }
            //Building contact slugs
            String contactSlugs = Arrays.stream(contact.split(","))
                    .map(String::trim)
                    .map(contactKeyToSlugMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));

            // Nothing to update
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
            Response linkResponse = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
            linkResponse.then().statusCode(200);
        }
    }

    private List<Integer> parseContactIds(String filterValue) {
        List<Integer> contactIds = new ArrayList<>();
    
        //If filterValue is empty or null, return empty list
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return contactIds;
        }
    
        for (String raw : filterValue.split(",")) {
            String placeholder = raw.trim();
            if (placeholder.isEmpty()) continue;
    
            //Normalize: remove {}
            String key = (placeholder.startsWith("{") && placeholder.endsWith("}")) ? placeholder.substring(1, placeholder.length() - 1): placeholder;
    
            Integer id = contactKeyToIdMap.get(key);
            //If contact ID is not found, fail the test
            if (id == null) {
                Assert.fail("Wrong contact name provided in the filter value. Contact ID not found for: " + placeholder +". Available contacts: " + contactKeyToIdMap.keySet());
            }
            contactIds.add(id);
        }
    
        return contactIds;
    }
}
