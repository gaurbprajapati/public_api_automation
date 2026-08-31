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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;
import com.qa.api.util.reaper.ThreadManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class OffLimitStatusFieldContactFilterSearchTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    Map<String, Integer> offLimitStatusMap;
    Map<String, String> contactKeyToIdMap = new HashMap<>(); 
    Map<String, String> contactIdToKeyMap = new HashMap<>(); 
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    String apiKey;
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        offLimitStatusMap = allCrudFunctions.getOffLimitStatusMap(albatrossURL, ownerAlbatrossAuthToken);
        createCompanies();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "offLimitStatusFieldContactFilterSearchTestData", description = "Filter Search Test for Off Limit Status Field")
    public void offLimitStatusFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Email: "+email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @DataProvider(name = "offLimitStatusFieldContactFilterSearchTestData", parallel = true)
    public Object[][] offLimitStatusFieldContactFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if (key.equals("Off Limit Status")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
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
                        String albatrossAuthToken = getAlbatrossAuthToken(createdBy);
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
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

    public void createTestData() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Map<String, Integer> contactIdMap = new HashMap<>();
        
        try {
            //Creating all contacts according to payload and storing their IDs in a map
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
                    contactIdMap.put(contactKey, contactId);
                    
                    // Store mappings for validation
                    contactKeyToIdMap.put(contactKey.toLowerCase(), String.valueOf(contactId));
                    contactIdToKeyMap.put(String.valueOf(contactId), contactKey.toLowerCase());
                }, executor))
                .collect(Collectors.toList());
            
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            addContactsToOffLimitStatus(contactJson, contactIdMap);
        } finally {
            executor.shutdown();
        }
    }

    private void addContactsToOffLimitStatus(JSONObject contactJson, Map<String, Integer> contactIdMap) {
        Map<String, List<Integer>> offLimitStatusContactIdsMap = new HashMap<>();
        
        // Group contacts by their off limit status assignments
        for (String contactKey : contactIdMap.keySet()) {
            JSONObject contactEntry = contactJson.getJSONObject(contactKey);
            if (!contactEntry.has("offLimitStatus") || contactEntry.isNull("offLimitStatus")) {
                continue;
            }
            
            String contactOffLimitStatus = contactEntry.getString("offLimitStatus");
            if (contactOffLimitStatus == null || contactOffLimitStatus.isEmpty()) {
                continue;
            }
            
            Integer contactId = contactIdMap.get(contactKey);
            String[] offLimitStatusNames = contactOffLimitStatus.split(",");
            for (String offLimitStatusName : offLimitStatusNames) {
                offLimitStatusName = offLimitStatusName.trim();
                if (!offLimitStatusName.isEmpty()) {
                    offLimitStatusContactIdsMap.computeIfAbsent(offLimitStatusName, k -> new ArrayList<>()).add(contactId);
                }
            }
        }

        // Bulk mark contacts as off limit
        long nowSeconds = System.currentTimeMillis() / 1000L;
        long startDate = nowSeconds;
        long endDate = nowSeconds + 30L * 24 * 60 * 60; // +30 days
        
        String basePath = "off-limit/mark-off-limit";
        
        for (Map.Entry<String, List<Integer>> entry : offLimitStatusContactIdsMap.entrySet()) {
            String statusLabel = entry.getKey();
            List<Integer> contactIds = entry.getValue();
            
            if (contactIds.isEmpty()) {
                continue;
            }
            
            // Get status ID from the map
            Integer statusId = offLimitStatusMap.get(statusLabel);
            if (statusId == null) {
                Assert.fail("Off-limit status ID not found for status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
                continue;
            }
            
            // Create payload
            JSONObject payload = new JSONObject();
            payload.put("entity_type_id", 2); // Entity type ID 2 for contacts
            payload.put("entity_ids", new JSONArray(contactIds));
            payload.put("status_id", statusId);
            payload.put("start_date", startDate);
            payload.put("end_date", endDate);
            payload.put("reason", "");
            
            Response response = RestClient.doPost("JSON", albatrossURL, basePath, ownerAlbatrossAuthToken, null, true, payload);
            System.out.println("Marking contact as offlimit: "+ response.prettyPrint());
            Assert.assertEquals(response.getStatusCode(), 200, "Failed to mark contacts off-limit with status: " + statusLabel);
        }
    }


    private String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy.toLowerCase()) {
            case "owner":
                return ownerAlbatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teammember":
                return teamMemberAlbatrossAuthToken;
            case "restrictedteammember":
                return restrictedTeamMemberAlbatrossAuthToken;
            default:
                return ownerAlbatrossAuthToken; // Default to owner token
        }
    }

    public JSONObject createFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("offLimitBehavior", "bypass");
        payload.put("sortPriorityList", new JSONArray());
        
        // Process filterValue to convert status label placeholders to status IDs
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

    private String processFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(filterValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String statusLabel = matcher.group(1);
            
            // Get status ID from the map
            Integer statusId = offLimitStatusMap.get(statusLabel);
            if (statusId != null) {
                processedValue = processedValue.replace(placeholder, String.valueOf(statusId));
            } else {
                throw new IllegalArgumentException("Off-limit status ID not found for status label: " + statusLabel + ". Available statuses: " + offLimitStatusMap.keySet());
            }
        }

        return processedValue;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "offLimitStatusFieldContactFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Off Limit Status Field")
    public void offLimitStatusFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        offLimitStatusFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "offLimitStatusFieldContactFilterSearchSmokeTestData", parallel = true)
    public Object[][] offLimitStatusFieldContactFilterSearchSmokeTestData() {
        return limitSmokeRows(offLimitStatusFieldContactFilterSearchTestData());
    }
}
