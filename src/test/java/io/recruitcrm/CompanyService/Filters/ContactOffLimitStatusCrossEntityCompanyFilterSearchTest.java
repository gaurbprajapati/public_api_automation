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
import io.rcrm.api.pojo.albatross.offlimit.MarkOffLimit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactOffLimitStatusCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String accountOwnerAPIKey;
    String email;
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companySlugToKeyMap = new ConcurrentHashMap<>();
    Map<String, Integer> offLimitStatusMap = new HashMap<>();
    ConcurrentHashMap<String, Integer> contactIdMap = new ConcurrentHashMap<>(); // Key: companyKey, Value: contactId
    ConcurrentHashMap<String, Integer> additionalContactIdMap = new ConcurrentHashMap<>(); // Key: companyKey, Value: contactId (additional contact)

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        offLimitStatusMap = allCrudFunctions.getOffLimitStatusMap(albatrossURL, albatrossAuthToken);
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactOffLimitStatusCrossEntityFilterSearchTestData", description = "Filter Search Test for Contact Off Limit Status Cross Entity Company")
    public void contactOffLimitStatusCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
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
                        
                        // Skip if contact or company ID is null (contact not created due to empty fields)
                        if (contactIdStr == null || companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - contact or company ID is null");
                            return;
                        }
                        
                        Integer contactId = Integer.parseInt(contactIdStr);
                        Integer companyId = Integer.parseInt(companyIdStr);
                        String companySlug = jsonPath.getString("data.company.slug");

                        // Store contact ID map (companyKey -> contactId) for marking off-limit
                        synchronized (contactIdMap) {
                            contactIdMap.put(companyKey, contactId);
                        }

                        // Store lookup maps for matching companies in response
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey.toLowerCase(), String.valueOf(companyId));
                        }
                        synchronized (companyIdToKeyMap) {
                            companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                        }
                        synchronized (companySlugToKeyMap) {
                            companySlugToKeyMap.put(companySlug, companyKey);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();

            // Create additional contacts for companies 2, 4, and 6
            createAdditionalContacts(companyJson);
            
            System.out.println("Additional contacts created. Map size: " + additionalContactIdMap.size());
            System.out.println("Additional contact IDs: " + additionalContactIdMap);
            
            // Mark contacts as off-limit based on offLimitStatus field
            markContactsAsOffLimit(companyJson);

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
                                
                                // Store additional contact ID
                                synchronized (additionalContactIdMap) {
                                    additionalContactIdMap.put(companyKey, contactId);
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

    private void markContactsAsOffLimit(JSONObject companyJson) {
        System.out.println("Starting markContactsAsOffLimit. Additional contact map size: " + additionalContactIdMap.size());
        System.out.println("Additional contact map contents: " + additionalContactIdMap);
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> markFutures = new ArrayList<>();
            
            // Mark main contacts as off-limit
            for (Map.Entry<String, Integer> entry : contactIdMap.entrySet()) {
                String companyKey = entry.getKey();
                Integer contactId = entry.getValue();
                
                if (!companyJson.has(companyKey)) {
                    continue;
                }
                
                JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                if (!companyEntry.has("offLimitStatus") || companyEntry.isNull("offLimitStatus")) {
                    continue;
                }
                
                String offLimitStatusLabel = companyEntry.getString("offLimitStatus");
                if (offLimitStatusLabel == null || offLimitStatusLabel.isEmpty()) {
                    continue;
                }
                
                markFutures.add(CompletableFuture.runAsync(() -> {
                    markContactAsOffLimit(contactId, offLimitStatusLabel);
                }, executor));
            }
            
            // Mark additional contacts as off-limit
            for (Map.Entry<String, Integer> entry : additionalContactIdMap.entrySet()) {
                String companyKey = entry.getKey();
                Integer contactId = entry.getValue();
                
                System.out.println("Processing additional contact for " + companyKey + " with contactId: " + contactId);
                
                if (!companyJson.has(companyKey)) {
                    System.out.println("Company key " + companyKey + " not found in companyJson");
                    continue;
                }
                
                JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                if (!companyEntry.has("additionalContact")) {
                    System.out.println("Company " + companyKey + " does not have additionalContact");
                    continue;
                }
                
                JSONObject additionalContact = companyEntry.getJSONObject("additionalContact");
                if (!additionalContact.has("contact1")) {
                    System.out.println("Company " + companyKey + " additionalContact does not have contact1");
                    continue;
                }
                
                JSONObject contact1 = additionalContact.getJSONObject("contact1");
                JSONObject payload = contact1.getJSONObject("payload");
                
                if (!payload.has("offLimitStatus") || payload.isNull("offLimitStatus")) {
                    System.out.println("Additional contact for " + companyKey + " does not have offLimitStatus in payload");
                    continue;
                }
                
                String offLimitStatusLabel = payload.getString("offLimitStatus");
                if (offLimitStatusLabel == null || offLimitStatusLabel.isEmpty()) {
                    System.out.println("Additional contact for " + companyKey + " has empty offLimitStatus");
                    continue;
                }
                
                System.out.println("Marking additional contact " + contactId + " for " + companyKey + " as off-limit with status: " + offLimitStatusLabel);
                markFutures.add(CompletableFuture.runAsync(() -> {
                    markContactAsOffLimit(contactId, offLimitStatusLabel);
                }, executor));
            }
            
            CompletableFuture.allOf(markFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void markContactAsOffLimit(Integer contactId, String offLimitStatusLabel) {
        try {
            Integer statusId = offLimitStatusMap.get(offLimitStatusLabel);
            if (statusId == null) {
                System.err.println("Off-limit status ID not found for status label: " + offLimitStatusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
                return;
            }
            
            // Calculate start and end dates (30 days from now)
            long nowSeconds = System.currentTimeMillis() / 1000L;
            String startDate = String.valueOf(nowSeconds);
            String endDate = String.valueOf(nowSeconds + 30L * 24 * 60 * 60); // +30 days
            
            MarkOffLimit markOffLimit = new MarkOffLimit();
            markOffLimit.setEntity_type_id(2); // Entity type ID 2 for contacts
            markOffLimit.setEntity_ids(new int[]{contactId});
            markOffLimit.setStatus_id(statusId);
            markOffLimit.setStart_date(startDate);
            markOffLimit.setEnd_date(endDate);
            markOffLimit.setReason("Test off-limit status");
            
            Response response = RestClient.doPost("JSON", albatrossURL, "off-limit/mark-off-limit", albatrossAuthToken, null, true, markOffLimit);
            
            if (response.getStatusCode() == 200) {
                System.out.println("Successfully marked contact " + contactId + " as off-limit with status: " + offLimitStatusLabel);
            } else {
                System.err.println("Failed to mark contact " + contactId + " as off-limit. Status: " + response.getStatusCode() + ". Response: " + response.getBody().asString());
            }
        } catch (Exception e) {
            System.err.println("Error marking contact " + contactId + " as off-limit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @DataProvider(name = "contactOffLimitStatusCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] dataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactOffLimitStatusFilterDataProvider.json");
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
        
        // Parse filterValue to extract off-limit status labels from placeholders like {Unavailable},{Contractual Off-Limits}
        String processedFilterValue = processFilterValue(filterValue);
        
        // Create filterValue object with type and value array
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("INTEGER_LIST")) {
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

    private String processFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;

        // Pattern to match placeholders like {Unavailable}, {Contractual Off-Limits}
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0); // e.g., "{Unavailable}"
            String statusLabel = matcher.group(1); // e.g., "Unavailable"

            // Look up status ID from offLimitStatusMap
            Integer statusId = offLimitStatusMap.get(statusLabel);
            
            if (statusId != null) {
                // Replace placeholder with status ID
                processedValue = processedValue.replace(placeholder, String.valueOf(statusId));
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No status ID found for off-limit status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
            }
        }

        return processedValue;
    }
}
