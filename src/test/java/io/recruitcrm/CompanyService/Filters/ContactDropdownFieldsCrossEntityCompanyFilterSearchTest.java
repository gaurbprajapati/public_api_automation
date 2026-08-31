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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ContactDropdownFieldsCrossEntityCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String accountOwnerAPIKey;
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companySlugToKeyMap = new ConcurrentHashMap<>();
    Map<String, String> userMap = new HashMap<>();
    Map<String, String> teamMap = new HashMap<>();
    String email;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        userMap = createUserMap();
        teamMap = createTeamMap();
        email =  ThreadManager.getAccount().getOwner().getEmail();
        createTestData();
        waitForDataSync();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactOwnerDropdownFieldsCrossEntityFilterSearchTestData", description = "Filter Search Test for Contact Owner Dropdown Field Cross Entity Company")
    public void contactOwnerDropdownFieldsCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactCreatedByDropdownFieldsCrossEntityFilterSearchTestData", description = "Filter Search Test for Contact CreatedBy Dropdown Field Cross Entity Company")
    public void contactCreatedByDropdownFieldsCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, companyKeyToIdMap, companyIdToKeyMap, 15, "Company");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "contactUpdatedByDropdownFieldsCrossEntityFilterSearchTestData", description = "Filter Search Test for Contact UpdatedBy Dropdown Field Cross Entity Company")
    public void contactUpdatedByDropdownFieldsCrossEntityFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "companies");
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
                        
                        // Get the auth token and owner ID based on createdBy field
                        String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "owner";
                        String albatrossAuthToken = getAlbatrossAuthToken(createdBy);
                        String ownerId = userMap.get(createdBy);
                        
                        // Set ownerid in contact payload (for owner field tests)
                        JSONObject contact = payload.getJSONObject("contact");
                        if (ownerId != null) {
                            contact.put("ownerid", Integer.parseInt(ownerId));
                        }
                        
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String contactIdStr = jsonPath.getString("data.contact.id");
                        String companyIdStr = jsonPath.getString("data.company.id");
                        String contactSlug = jsonPath.getString("data.contact.slug");
                        
                        // Skip if contact or company ID is null (contact not created due to empty fields)
                        if (contactIdStr == null || companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - contact or company ID is null");
                            return;
                        }
                        
                        Integer companyId = Integer.parseInt(companyIdStr);
                        String companySlug = jsonPath.getString("data.company.slug");

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

        } finally {
            executor.shutdown();
        }
    }

    private void createAdditionalContacts(JSONObject companyJson) {
        String[] companiesToAddContacts = {"company2", "company4", "company6"};
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        try {
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
                                
                                // Get the auth token and owner ID based on createdBy field
                                String createdBy = companyEntry.has("createdBy") ? companyEntry.getString("createdBy") : "owner";
                                String albatrossAuthToken = getAlbatrossAuthToken(createdBy);
                                String ownerId = userMap.get(createdBy);
                                
                                // Set ownerid in contact payload
                                JSONObject contact = payload.getJSONObject("contact");
                                if (ownerId != null) {
                                    contact.put("ownerid", Integer.parseInt(ownerId));
                                }
                                
                                // Create the additional contact
                                try {
                                    Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", 
                                            albatrossAuthToken, null, true, payload);
                                    response.then().statusCode(200);
                                    System.out.println("Successfully created additional contact for " + companyKey);
                                } catch (Exception e) {
                                    System.err.println("Failed to create additional contact for " + companyKey + ": " + e.getMessage());
                                }
                            }
                        }
                    }
                }
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
            default:
                return ownerAlbatrossAuthToken;
        }
    }

    public Map<String, String> createUserMap() {
        Map<String, String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restrictedTeamMember", user.get("[2].id").toString());
        userMap.put("teamMember", user.get("[3].id").toString());
        return userMap;
    }

    public Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        ArrayList<String> userId = new ArrayList<String>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("teamMember")));

        Response response = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId);
        response.then().statusCode(200);
        Response team = function.getTeams(baseURL, accountOwnerAPIKey);
        String teamId = team.jsonPath().getString("[0].team_id");
        teamMap.put("team", teamId);
        return teamMap;
    }

    @DataProvider(name = "contactOwnerDropdownFieldsCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] contactOwnerDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactDropdownFieldsFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        if (filterData.has("Contact Owner")) {
            JSONArray tests = filterData.getJSONArray("Contact Owner");
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{"Contact Owner", test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "contactCreatedByDropdownFieldsCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] contactCreatedByDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactDropdownFieldsFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        if (filterData.has("Contact CreatedBy")) {
            JSONArray tests = filterData.getJSONArray("Contact CreatedBy");
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{"Contact CreatedBy", test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "contactUpdatedByDropdownFieldsCrossEntityFilterSearchTestData", parallel = true)
    public Object[][] contactUpdatedByDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyCrossEntityContactDropdownFieldsFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        if (filterData.has("Contact UpdatedBy")) {
            JSONArray tests = filterData.getJSONArray("Contact UpdatedBy");
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                testData.add(new Object[]{"Contact UpdatedBy", test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
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
        
        // Parse filterValue to extract user IDs from placeholders like {owner}, {admin}
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
            } else if (fieldKey.equals("owner") || fieldKey.equals("admin") || fieldKey.equals("restrictedTeamMember") || fieldKey.equals("teamMember")) {
                actualValue = userMap.get(fieldKey);
            }

            if (actualValue != null) {
                processedValue = processedValue.replace(placeholder, actualValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder);
            }
        }

        return processedValue;
    }
}
