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
import com.qa.api.util.reaper.ReaperIntegration;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DropdownFieldsContactFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    String email;
    Map<String, String> userMap;
    Map<String, String> teamMap;
    Map<String, String> contactKeyToIdMap = new HashMap<>(); 
    Map<String, String> contactIdToKeyMap = new HashMap<>(); 
    Map<String, Integer> stageIdMap = new HashMap<>();
    Map<String, String> lastCommsTypeMap = new HashMap<>();
    Map<String, Integer> contactSlugToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> contactSlugMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> contactIdToLastCommsTypeMap = new ConcurrentHashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    JavaFakerMeeting fakerMeeting;
    String apiKey;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        apiKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        userMap = createUserMap();
        teamMap = createTeamMap();
        stageIdMap = createStageMap();
        initializeLastCommsTypeMap();
        fakerMeeting = new JavaFakerMeeting();
        createCompanies();
        createTestData();
        waitForDataSync();
    }

    private void initializeLastCommsTypeMap() {
        lastCommsTypeMap.put("Email", "email");
        lastCommsTypeMap.put("CallLog", "calllog");
        lastCommsTypeMap.put("SMS", "sms");
        lastCommsTypeMap.put("Meeting", "meeting");
        lastCommsTypeMap.put("Message", "message");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "ownerDropdownFieldFilterSearchTestData", description = "Filter Search Test for Owner Dropdown Field")
    public void ownerDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "ownername");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "createdByDropdownFieldFilterSearchTestData", description = "Filter Search Test for CreatedBy Dropdown Field")
    public void createdByDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "creatorname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "updatedByDropdownFieldFilterSearchTestData", description = "Filter Search Test for UpdatedBy Dropdown Field")
    public void updatedByDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "updatorname");
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "stageDropdownFieldFilterSearchTestData", description = "Filter Search Test for Stage Dropdown Field")
    public void stageDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "stage", contactIdToKeyMap);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "lastCommsTypeDropdownFieldFilterSearchTestData", description = "Filter Search Test for LastCommsType Dropdown Field")
    public void lastCommsTypeDropdownFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, ownerAlbatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "last_communication_method", contactIdToKeyMap);
        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult, contactKeyToIdMap, contactIdToKeyMap, 15, "Contact");
    }

    @DataProvider(name = "ownerDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] ownerDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDropdownTypeFilterDataProvider.json");
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
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDropdownTypeFilterDataProvider.json");
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
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDropdownTypeFilterDataProvider.json");
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

    @DataProvider(name = "stageDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] stageDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if(key.equals("Stage")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    testData.add(new Object[]{key, test.getString("filterType"), test.getString("filterValue"), test.getString("dbField"), test.getString("expectedResult"), test.getString("fieldType"), test.getString("filterValue_TYPE")});
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "lastCommsTypeDropdownFieldFilterSearchTestData", parallel = true)
    public Object[][] lastCommsTypeDropdownFieldFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/contactDropdownTypeFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            if(key.equals("LastCommsType")) {
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
                        Integer companyId = jsonPath.getInt("data.company.id");
                        
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

        try {
            List<CompletableFuture<Void>> createFutures = contactJson.keySet().stream()
                .filter(key -> key.startsWith("contact"))
                .map(contactKey -> CompletableFuture.runAsync(() -> {
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject payload = contactEntry.getJSONObject("payload");
                    String createdBy = contactEntry.getString("createdBy");
                    String albatrossAuthToken = getAlbatrossAuthToken(createdBy);
                    
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
                    
                    // Update stageid based on Stage field
                    if (contactEntry.has("Stage")) {
                        String stageLabel = contactEntry.getString("Stage");
                        if (stageIdMap.containsKey(stageLabel)) {
                            Integer stageId = stageIdMap.get(stageLabel);
                            JSONObject contact = payload.getJSONObject("contact");
                            contact.put("stageid", stageId);
                        }
                    }
                    
                    Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", albatrossAuthToken, null, true, payload);
                    response.then().statusCode(200);
                    JsonPath jsonPath = response.jsonPath();
                    String slug = jsonPath.getString("data.contact.slug");
                    String contactIdStr = jsonPath.getString("data.contact.id");
                    
                    // Skip if contact ID is null (contact not created due to empty fields)
                    if (contactIdStr == null) {
                        System.out.println("Skipping " + contactKey + " - contact ID is null");
                        return;
                    }
                    
                    Integer contactId = Integer.parseInt(contactIdStr);

                    // Store mappings for validation
                    contactKeyToIdMap.put(contactKey.toLowerCase(), String.valueOf(contactId));
                    contactIdToKeyMap.put(String.valueOf(contactId), contactKey.toLowerCase());
                    
                    // Store contact slug and ID for LastCommsType update
                    synchronized (contactSlugToIdMap) {
                        contactSlugToIdMap.put(slug, contactId);
                    }
                    synchronized (contactSlugMap) {
                        contactSlugMap.put(contactKey, slug);
                    }
                    
                    // Store contact ID and LastCommsType for updating after meetings are created
                    if (contactEntry.has("LastCommsType")) {
                        String lastCommsType = contactEntry.getString("LastCommsType");
                        synchronized (contactIdToLastCommsTypeMap) {
                            contactIdToLastCommsTypeMap.put(contactId, lastCommsType);
                        }
                    }
                }, executor))
                .collect(Collectors.toList());

            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
            
            // Create meetings for contacts
            createMeetingsForContact(contactSlugMap);
            
            // Update LastCommsType for all contacts after meetings are created
            updateAllContactsLastCommsType();

        } finally {
            executor.shutdown();
        }
    }

    private void createMeetingsForContact(ConcurrentHashMap<String, String> contactSlugMap) {
        List<String> sortedContactKeys = contactSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String contactKey : sortedContactKeys) {
                String contactSlug = contactSlugMap.get(contactKey);
                if (contactSlug == null) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        Meeting meeting = createMeetingForContact(contactSlug);
                        
                        Response response = RestClient.doPost("JSON", baseURL, "meetings", apiKey, null, true, meeting);
                        
                        if (response.getStatusCode() == 200) {
                            System.out.println("Successfully created meeting for contact " + contactSlug);
                        } else {
                            System.err.println("Meeting creation failed for contact " + contactSlug + ": " + response.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("Exception creating meeting for contact " + contactSlug + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private Meeting createMeetingForContact(String contactSlug) {
        Meeting meeting = new Meeting();
        
        meeting.setTitle("Contact Meeting");
        meeting.setDescription("Meeting activity for contact");
        meeting.setAddress("Office Address");
        
        String startDate = getDateDaysFromNow(0);
        meeting.setStart_date(startDate);
        meeting.setEnd_date(fakerMeeting.getEndDateWithReferenceDate(startDate));
        meeting.setReminder(30);
        meeting.setRelated_to(contactSlug);
        meeting.setRelated_to_type("contact");
        
        return meeting;
    }

    private String getDateDaysFromNow(int days) {
        java.util.Date date = new java.util.Date();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);
        return calendar.getTime().toString();
    }

    private void updateAllContactsLastCommsType() {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> updateFutures = contactIdToLastCommsTypeMap.entrySet().stream()
                    .map(entry -> CompletableFuture.runAsync(() -> {
                        Integer contactId = entry.getKey();
                        String lastCommsType = entry.getValue();
                        updateContactLastCommsType(contactId, lastCommsType);
                    }, executor))
                    .collect(Collectors.toList());
            
            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void updateContactLastCommsType(Integer contactId, String lastCommsType) {
        try {
            String apiValue = lastCommsTypeMap.getOrDefault(lastCommsType, lastCommsType.toLowerCase());
            
            JSONObject fieldsAndTimestamps = new JSONObject();
            fieldsAndTimestamps.put("last_communication_method", apiValue);
            
            Response updateResponse = ReaperIntegration.updateLastActivityTimestamp("contact", contactId, fieldsAndTimestamps);
            if (updateResponse.getStatusCode() == 200) {
                System.out.println("Successfully updated LastCommsType for contact ID: " + contactId + " to " + apiValue);
            } else {
                System.err.println("Failed to update LastCommsType for contact ID: " + contactId + ". Status: " + updateResponse.getStatusCode() + ". Response: " + updateResponse.getBody().asString());
            }
        } catch (Exception e) {
            System.err.println("Error updating LastCommsType for contact ID: " + contactId + ": " + e.getMessage());
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
        
        String processedFilterValue = processFilterValue(filterValue, fieldName);
        
        JSONObject filterValueObj = new JSONObject();
        if (filterValue_TYPE.equals("ENTITY_ASSOCIATION")) {
            filterValueObj = entityAssociationFilterValue(processedFilterValue);
        } else if (filterValue_TYPE.equals("INTEGER_LIST")) {
            filterValueObj = integerListFilterValue(processedFilterValue);
        } else if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValue(processedFilterValue);
        } else {
            filterValueObj = emptyFilterValue(filterValue_TYPE);
        }
        
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
        // Set entityType to "contact_last_activities_t" for LastCommsType field
        String entityType = fieldName.equals("LastCommsType") ? "contact_last_activities_t" : "contact";
        filter.put("entityType", entityType);
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
            } else if (fieldName.equals("Stage")) {
                Integer stageId = stageIdMap.get(fieldKey);
                // If not found, try with space variations (e.g., "FollowUp" -> "Follow Up")
                if (stageId == null) {
                    // Try with space: "FollowUp" -> "Follow Up"
                    String withSpace = fieldKey.replaceAll("([a-z])([A-Z])", "$1 $2");
                    stageId = stageIdMap.get(withSpace);
                }
                // If still not found, try case-insensitive search
                if (stageId == null) {
                    for (Map.Entry<String, Integer> entry : stageIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(fieldKey) || 
                            entry.getKey().replaceAll("\\s+", "").equalsIgnoreCase(fieldKey)) {
                            stageId = entry.getValue();
                            break;
                        }
                    }
                }
                if (stageId != null) {
                    actualValue = String.valueOf(stageId);
                }
            } else if (fieldName.equals("LastCommsType")) {
                actualValue = lastCommsTypeMap.get(fieldKey);
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

    public Map<String,Integer> createStageMap() {
        Map<String, Integer> stageMap = new HashMap<>();
        try {
            Response response = allCrudFunctions.getContactStages(albatrossURL, ownerAlbatrossAuthToken);
            if (response.getStatusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.getBody().asString());
                JSONArray dataArray = responseJson.getJSONArray("data");
                
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject stageObj = dataArray.getJSONObject(i);
                    String stageLabel = stageObj.getString("label");
                    Integer stageId = stageObj.getInt("id");
                    stageMap.put(stageLabel, stageId);
                    
                    // Also add normalized version without spaces for easier lookup
                    // e.g., "Follow Up" -> "FollowUp"
                    String normalizedLabel = stageLabel.replaceAll("\\s+", "");
                    if (!normalizedLabel.equals(stageLabel)) {
                        stageMap.put(normalizedLabel, stageId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch contact stages: " + e.getMessage());
        }
        return stageMap;
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "ownerDropdownFieldFilterSearchSmokeTestData", description = "[Smoke] Filter Search Test for Owner Dropdown Field")
    public void ownerDropdownFieldFilterSearchSmokeTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        ownerDropdownFieldFilterSearchTest(fieldName, filterType, filterValue, dbField, expectedResult, fieldType, filterValue_TYPE);
    }

    @DataProvider(name = "ownerDropdownFieldFilterSearchSmokeTestData", parallel = true)
    public Object[][] ownerDropdownFieldFilterSearchSmokeTestData() {
        return limitSmokeRows(ownerDropdownFieldFilterSearchTestData());
    }
}
