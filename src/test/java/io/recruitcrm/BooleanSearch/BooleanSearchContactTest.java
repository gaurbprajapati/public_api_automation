package io.recruitcrm.BooleanSearch;


import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.pojo.Task;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.reaper.ThreadManager;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.AbstractMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class BooleanSearchContactTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String accountOwnerAPIKey;
    Map<String, Integer> customFieldIds = new HashMap<>();
    Map<String, String> entityCFValueMap = new HashMap<>();
    ConcurrentHashMap<String, String> contactKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> contactIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyKeyToSlugMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> timestampScenarios;
    
    // Activity-related maps
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> noteTypeMap;
    Map<String, String> callTypeMap;
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> contactSlugMap = new ConcurrentHashMap<>();
    
    // Task and Meeting type IDs
    int defaultTaskTypeId = 0;
    int followUpTaskTypeId = 0;
    int interviewSchedulingTaskTypeId = 0;
    int defaultMeetingTypeId = 0;
    int businessDevelopmentMeetingTypeId = 0;
    int clientMeetingTypeId = 0;
    int internalMeetingTypeId = 0;
    
    // Faker instances
    JavaFakerTask fakerTask = new JavaFakerTask();
    JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        createCompanies();
        customFieldIds = createCustomFields();
        createAssociatedEntities();
        noteTypeMap = createCustomNoteType();
        callTypeMap = createCustomCallType();
        userMap = createUserMap();
        teamMap = createTeamMap();
        setupTaskTypes();
        setupMeetingTypes();
        createEntityCFValueMap();
        createTestData();
        waitForDataSyncBooleanSearch();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "booleanSearchContactTestData", description = "Boolean Search Test for Contact")
    public void booleanSearchContactTest(String testCaseId, String description, String filterValue, String entities, String expectedResult, String expectedResultName, String expectedResultReason) {
        FilterSearchReporter.skipFilterCriteriaLogging();
        
        JSONObject payload = createBooleanSearchPayload(filterValue, entities);
        FilterSearchReporter.logPayload(payload);
        
        FilterSearchReporter.logInfo("Test Case ID", testCaseId);
        FilterSearchReporter.logInfo("Description", description);
        FilterSearchReporter.logInfo("Query", filterValue);
        FilterSearchReporter.logInfo("Entities", entities);
        FilterSearchReporter.logInfo("Expected Result", expectedResult);
        FilterSearchReporter.logInfo("Expected Result Name", expectedResultName);
        FilterSearchReporter.logInfo("Expected Result Reason", expectedResultReason);
        FilterSearchReporter.logInfo("Account: ", ThreadManager.getOwner().getEmail());
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "contacts");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for test case: " + testCaseId + " (query: " + filterValue + ") is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for test case: " + testCaseId + " is not 'Entities retrieved successfully'");
        
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, "Name", "firstname");
        FilterSearchReporter.logResponse(response, data);
        
        validateBooleanSearchResults(data, testCaseId, description, filterValue, entities, expectedResult);
    }

    public JSONObject createBooleanSearchPayload(String filterValue, String entities) {
        Set<String> validEntities = new HashSet<>(Arrays.asList(
            "contacts", "notes", "call_logs", "tasks", "meetings"
        ));
        
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CONTACT");
        payload.put("offLimitBehavior", "bypass");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("filterSearchList", JSONObject.NULL);

        JSONObject booleanSearchList = new JSONObject();
        booleanSearchList.put("keyword", filterValue);

        JSONArray selectedOptions = new JSONArray();
        if (entities != null && !entities.isEmpty()) {
            String[] entityArray = entities.split(",");
            for (String entity : entityArray) {
                String trimmedEntity = entity.trim();
                // Validate entity is in the allowed list
                if (!validEntities.contains(trimmedEntity)) {
                    throw new IllegalArgumentException(
                        "Invalid entity: '" + trimmedEntity + "'. Allowed entities are: " + validEntities
                    );
                }
                selectedOptions.put(trimmedEntity);
            }
        }
        booleanSearchList.put("selectedOptions", selectedOptions);
        
        payload.put("booleanSearchList", booleanSearchList);
        payload.put("sortPriorityList", new JSONArray());
        
        return payload;
    }

    @DataProvider(name = "booleanSearchContactTestData")
    public Object[][] booleanSearchContactTestData() {
        String filePath = "src/test/resources/filtersDataProvider/contactBooleanSearchDataProvider.json";
        List<Object[]> testData = new ArrayList<>();
        
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray testCases = new JSONArray(jsonContent);
            
            for (int i = 0; i < testCases.length(); i++) {
                JSONObject testCase = testCases.getJSONObject(i);
                String testCaseId = testCase.getString("testCaseId");
                String description = testCase.getString("description");
                String query = testCase.getString("query");
                
                // Convert entitiesInvolved array to comma-separated string
                JSONArray entitiesArray = testCase.getJSONArray("entitiesInvolved");
                StringBuilder entitiesStr = new StringBuilder();
                for (int j = 0; j < entitiesArray.length(); j++) {
                    if (j > 0) {
                        entitiesStr.append(",");
                    }
                    entitiesStr.append(entitiesArray.getString(j));
                }

                // Convert expectedResult array to comma-separated string, or keep "Empty" as is
                String expectedResultStr;
                Object expectedResultObj = testCase.get("expectedResult");
                if (expectedResultObj instanceof String && expectedResultObj.equals("Empty")) {
                    expectedResultStr = "Empty";
                } else if (expectedResultObj instanceof JSONArray) {
                    JSONArray expectedArray = (JSONArray) expectedResultObj;
                    StringBuilder expectedStr = new StringBuilder();
                    for (int k = 0; k < expectedArray.length(); k++) {
                        if (k > 0) {
                            expectedStr.append(",");
                        }
                        expectedStr.append(expectedArray.getString(k));
                    }
                    expectedResultStr = expectedStr.toString();
                } else {
                    expectedResultStr = expectedResultObj.toString();
                }
                
                // Get expectedResultName - convert array to comma-separated string or keep "Empty"
                String expectedResultNameStr;
                Object expectedResultNameObj = testCase.get("expectedResultName");
                if (expectedResultNameObj instanceof String && expectedResultNameObj.equals("Empty")) {
                    expectedResultNameStr = "Empty";
                } else if (expectedResultNameObj instanceof JSONArray) {
                    JSONArray expectedNameArray = (JSONArray) expectedResultNameObj;
                    StringBuilder expectedNameStr = new StringBuilder();
                    for (int k = 0; k < expectedNameArray.length(); k++) {
                        if (k > 0) {
                            expectedNameStr.append(", ");
                        }
                        expectedNameStr.append(expectedNameArray.getString(k));
                    }
                    expectedResultNameStr = expectedNameStr.toString();
                } else {
                    expectedResultNameStr = expectedResultNameObj != null ? expectedResultNameObj.toString() : "";
                }
                
                // Get expectedResultReason
                String expectedResultReasonStr = testCase.optString("expectedResultReason", "");
                
                testData.add(new Object[]{
                    testCaseId,
                    description,
                    query,
                    entitiesStr.toString(),
                    expectedResultStr,
                    expectedResultNameStr,
                    expectedResultReasonStr
                });
            }
        } catch (IOException e) {
            Assert.fail("Failed to read JSON file from path: " + filePath + ". Error: " + e.getMessage());
        }
        
        return testData.toArray(new Object[0][0]);
    }

    public void validateBooleanSearchResults(JSONArray data, String testCaseId, String description, 
                                             String filterValue, String entities, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Test Case: " + testCaseId + " - Expected empty result but got " + data.length() + " contacts for query: " + filterValue);
            return;
        }
        
        if (data.length() == 0) {
            Assert.fail("Test Case: " + testCaseId + " - Expected results but got 0 contacts for query: " + filterValue);
        }
        
        String[] expectedContactKeys = expectedResult.split(",");
        List<Integer> expectedContactIds = new ArrayList<>();
        
        for (String contactKey : expectedContactKeys) {
            String normalizedKey = contactKey.trim().toLowerCase().replace(" ", "");
            String contactIdStr = contactKeyToIdMap.get(normalizedKey);
            if (contactIdStr == null) {
                Assert.fail("Test Case: " + testCaseId + " - Expected contact key '" + contactKey + "' (normalized: '" + normalizedKey + "') not found in contactKeyToIdMap. Available keys: " + contactKeyToIdMap.keySet());
            }
            expectedContactIds.add(Integer.parseInt(contactIdStr));
        }
        
        List<Integer> actualContactIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject contact = data.getJSONObject(i);
            actualContactIds.add(contact.getInt("id"));
        }
        
        Assert.assertEquals(data.length(), expectedContactIds.size(), "Test Case: " + testCaseId + " - Expected " + expectedContactIds.size() + " contacts but got " + data.length() + " for query: " + filterValue);
        
        for (int contactId : expectedContactIds) {
            if (!actualContactIds.contains(contactId)) {
                String contactKey = contactIdToKeyMap.get(String.valueOf(contactId));
                Assert.fail("Test Case: " + testCaseId + " - Contact: " + contactKey + " (ID: " + contactId + ") is not present in the actual response but was expected. Query: " + filterValue);
            }
        }
        
        for (int contactId : actualContactIds) {
            if (!expectedContactIds.contains(contactId)) {
                String contactKey = contactIdToKeyMap.get(String.valueOf(contactId));
                Assert.fail("Test Case: " + testCaseId + " - Contact: " + contactKey + " (ID: " + contactId + ") is present in the actual response but was not expected. Query: " + filterValue);
            }
        }
    }

    public void createCompanies() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
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
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchContact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = contactJson.keySet().stream()
                .filter(key -> key.startsWith("contact"))
                .map(contactKey -> CompletableFuture.runAsync(() -> {
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject payload = contactEntry.getJSONObject("payload");
                    String createdBy = contactEntry.has("createdBy") ? contactEntry.getString("createdBy") : "admin";
                    String authToken = getAlbatrossAuthToken(createdBy);
                    if (payload.has("selectedcompanies")) {
                        JSONArray selectedCompanies = payload.getJSONArray("selectedcompanies");
                        for (int i = 0; i < selectedCompanies.length(); i++) {
                            JSONObject companyInfo = selectedCompanies.getJSONObject(i);
                            String slugPlaceholder = companyInfo.optString("slug", "");
                            String idPlaceholder = companyInfo.optString("id", "");
                            String companyKey = null;
                            if (slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                                companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6);
                            } else if (idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                                companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4);
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
                    JSONObject processedPayload = processPayloadPlaceholders(payload);
                    Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", authToken, null, true, processedPayload);
                    response.then().statusCode(200);
                    String contactIdStr = response.jsonPath().getString("data.contact.id");
                    if (contactIdStr == null) {
                        return;
                    }
                    int contactId = Integer.parseInt(contactIdStr);
                    contactIdToKeyMap.put(String.valueOf(contactId), contactKey);
                    contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
                    contactSlugMap.put(contactKey, response.jsonPath().getString("data.contact.slug"));
                }, executor))
                .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            createNotesForContact(contactSlugMap);
            createTasksForContact(contactSlugMap);
            createCallLogForContact(contactSlugMap);
            createMeetingsForContact(contactSlugMap);
        } finally {
            executor.shutdown();
        }
    }

    public String getAlbatrossAuthToken(String createdBy) {
        switch (createdBy) {
            case "owner":
                return albatrossAuthToken;
            case "admin":
                return adminAlbatrossAuthToken;
            case "teamMember":
                return teamMemberAlbatrossAuthToken;
            default:
                return albatrossAuthToken;
        }
    }



    public void createEntityCFValueMap() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            CompletableFuture<JsonPath> candidateJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> candidateJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            CompletableFuture<JsonPath> companyJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> companyJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            // Contacts depend on companies
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            // Jobs depend on company + contact
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

            // Deals depend on company + contact + job
            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(jobJson1 -> {
                String companySlug1 = companyJson1Future.join().getString("slug");
                String contactSlug1 = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug1, contactSlug1, jobJson1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(jobJson2 -> {
                String companySlug2 = companyJson2Future.join().getString("slug");
                String contactSlug2 = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug2, contactSlug2, jobJson2.getString("slug")).jsonPath();
            }, executor);

            //Get user
            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(() -> {
                return function.getUsers(baseURL, accountOwnerAPIKey).jsonPath();
            }, executor);

            //Create team depends on user
            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<String>();
                ArrayList<String> team2UserId = new ArrayList<String>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));

                Response team1Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", team1UserId);
                Response team2Response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team2", team2UserId);
                team1Response.then().statusCode(200);
                team2Response.then().statusCode(200);
                Response team = function.getTeams(baseURL, accountOwnerAPIKey);
                return team.jsonPath();
            }, executor);

            CompletableFuture.allOf(
                    candidateJson1Future, candidateJson2Future,
                    companyJson1Future, companyJson2Future,
                    contactJson1Future, contactJson2Future,
                    jobJson1Future, jobJson2Future,
                    dealJson1Future, dealJson2Future,
                    userJsonFuture, teamJsonFuture
            ).join();

            JsonPath candidateJson1 = candidateJson1Future.join();
            JsonPath candidateJson2 = candidateJson2Future.join();
            JsonPath companyJson1 = companyJson1Future.join();
            JsonPath companyJson2 = companyJson2Future.join();
            JsonPath contactJson1 = contactJson1Future.join();
            JsonPath contactJson2 = contactJson2Future.join();
            JsonPath jobJson1 = jobJson1Future.join();
            JsonPath jobJson2 = jobJson2Future.join();
            JsonPath dealJson1 = dealJson1Future.join();
            JsonPath dealJson2 = dealJson2Future.join();
            JsonPath userJson = userJsonFuture.join();
            JsonPath teamJson = teamJsonFuture.join();
            entityCFValueMap.put("candidate1", candidateJson1.getString("slug"));
            entityCFValueMap.put("candidate2", candidateJson2.getString("slug"));
            entityCFValueMap.put("company1", companyJson1.getString("slug"));
            entityCFValueMap.put("company2", companyJson2.getString("slug"));
            entityCFValueMap.put("contact1", contactJson1.getString("slug"));
            entityCFValueMap.put("contact2", contactJson2.getString("slug"));
            entityCFValueMap.put("job1", jobJson1.getString("slug"));
            entityCFValueMap.put("job2", jobJson2.getString("slug"));
            entityCFValueMap.put("deal1", dealJson1.getString("slug"));
            entityCFValueMap.put("deal2", dealJson2.getString("slug"));
            entityCFValueMap.put("owner", String.valueOf(userJson.getInt("[0].id")));
            entityCFValueMap.put("admin", String.valueOf(userJson.getInt("[1].id")));
            entityCFValueMap.put("restricted", String.valueOf(userJson.getInt("[2].id")));
            entityCFValueMap.put("teamMember", String.valueOf(userJson.getInt("[3].id")));
            entityCFValueMap.put("team1", teamJson.getString("[0].team_id"));
            entityCFValueMap.put("team2", teamJson.getString("[1].team_id"));
            int entityCompany1Id = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"));
            int entityCompany2Id = function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"));
            companyKeyToIdMap.put("entityCompany1", String.valueOf(entityCompany1Id));
            companyKeyToIdMap.put("entityCompany2", String.valueOf(entityCompany2Id));
            companyKeyToIdMap.put("entitycompany1", String.valueOf(entityCompany1Id));
            companyKeyToIdMap.put("entitycompany2", String.valueOf(entityCompany2Id));
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, Integer> createCustomFields() {
        Map<String, Integer> customFieldIds = new HashMap<>();
        int colId = 1;
        List<String> entityTypes = new ArrayList<>(Arrays.asList("candidate", "company", "deals", "job", "contact", "user", "team", "text", "email", "phonenumber", "longtext", "number", "date", "social_profile", "dropdown", "multiselect", "checkbox"));
        ExecutorService executor = Executors.newFixedThreadPool(7);
        
        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = new ArrayList<>();
            for (String entity : entityTypes) {
                final int currentColId = colId++;
                final String entityType = entity;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    String fieldName = entityType + "CF";
                    Response response;

                    if (entityType.equals("dropdown") || entityType.equals("multiselect")) {
                        response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "contact", fieldName, entityType, "Option A, Option B, OptionC");
                    } else {
                        response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "contact", fieldName, entityType, "", currentColId);
                    }
                    
                    return new AbstractMap.SimpleEntry<>(fieldName, response);
                }, executor));
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            for (CompletableFuture<AbstractMap.SimpleEntry<String, Response>> future : futures) {
                AbstractMap.SimpleEntry<String, Response> result = future.get();
                String fieldName = result.getKey();
                Response response = result.getValue();
                
                Assert.assertEquals(response.getStatusCode(), 200, "Failed to create custom field: " + fieldName);
                
                int columnId = response.jsonPath().getInt("data.custumField.columnid");
                customFieldIds.put(fieldName, columnId);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating custom fields in parallel", e);
        } finally {
            executor.shutdown();
        }
        
        return customFieldIds;
    }

    public JSONObject processPayloadPlaceholders(JSONObject payload) {
        JSONObject processedPayload = new JSONObject();
        
        for (String key : payload.keySet()) {
            Object value = payload.get(key);
            
            // Handle nested JSONObject
            if (value instanceof JSONObject) {
                JSONObject nestedObject = (JSONObject) value;
                JSONObject processedNestedObject = processPayloadPlaceholders(nestedObject);
                processedPayload.put(key, processedNestedObject);
            }
            // Handle JSONArray
            else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                JSONArray processedArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    Object arrayItem = array.get(i);
                    if (arrayItem instanceof JSONObject) {
                        processedArray.put(processPayloadPlaceholders((JSONObject) arrayItem));
                    } else {
                        processedArray.put(arrayItem);
                    }
                }
                processedPayload.put(key, processedArray);
            }
            // Handle placeholder keys (custom fields)
            else if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                String lookupKey = ("dealCF".equals(trimmedKey) && customFieldIds.containsKey("dealsCF")) ? "dealsCF" : trimmedKey;
                
                if (customFieldIds.containsKey(lookupKey)) {
                    String newKey = "custcolumn" + customFieldIds.get(lookupKey);
                    if (value instanceof String) {
                        String stringValue = (String) value;
                        String processedValue = processEntityPlaceholders(stringValue);
                        processedPayload.put(newKey, processedValue);
                    } else {
                        processedPayload.put(newKey, value);
                    }
                } else {
                    processedPayload.put(key, value);
                }
            }
            // Handle regular keys
            else {
                processedPayload.put(key, value);
            }
        }
        
        return processedPayload;
    }

    public String processEntityPlaceholders(String value) {
        if (value == null) {
            return null;
        }
        
        if (value.startsWith("{") && value.endsWith("}")) {
            String innerValue = value.substring(1, value.length() - 1);
            String[] entityKeys = innerValue.split(",");
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entityKeys.length; i++) {
                String entityKey = entityKeys[i].trim();
                if (entityCFValueMap.containsKey(entityKey)) {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(entityCFValueMap.get(entityKey));
                } else {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(entityKey);
                }
            }

            return result.toString();
        }
        return value;
    }

    private void createAssociatedEntities() {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            CompletableFuture<JsonPath> candidateJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> candidateJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            CompletableFuture<JsonPath> companyJson1Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);
            CompletableFuture<JsonPath> companyJson2Future = CompletableFuture.supplyAsync(
                    () -> function.createNewCompanyWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            // Contacts depend on companies
            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            // Jobs depend on company + contact
            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

            // Deals depend on company + contact + job
            CompletableFuture<JsonPath> dealJson1Future = jobJson1Future.thenApplyAsync(jobJson1 -> {
                String companySlug1 = companyJson1Future.join().getString("slug");
                String contactSlug1 = contactJson1Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug1, contactSlug1, jobJson1.getString("slug")).jsonPath();
            }, executor);
            CompletableFuture<JsonPath> dealJson2Future = jobJson2Future.thenApplyAsync(jobJson2 -> {
                String companySlug2 = companyJson2Future.join().getString("slug");
                String contactSlug2 = contactJson2Future.join().getString("slug");
                return function.createNewDealWithMandatoryFields(baseURL, accountOwnerAPIKey, companySlug2, contactSlug2, jobJson2.getString("slug")).jsonPath();
            }, executor);

            CompletableFuture.allOf(
                    candidateJson1Future, candidateJson2Future,
                    companyJson1Future, companyJson2Future,
                    contactJson1Future, contactJson2Future,
                    jobJson1Future, jobJson2Future,
                    dealJson1Future, dealJson2Future
            ).join();

            JsonPath candidateJson1 = candidateJson1Future.join();
            JsonPath candidateJson2 = candidateJson2Future.join();
            JsonPath companyJson1 = companyJson1Future.join();
            JsonPath companyJson2 = companyJson2Future.join();
            JsonPath contactJson1 = contactJson1Future.join();
            JsonPath contactJson2 = contactJson2Future.join();
            JsonPath jobJson1 = jobJson1Future.join();
            JsonPath jobJson2 = jobJson2Future.join();
            JsonPath dealJson1 = dealJson1Future.join();
            JsonPath dealJson2 = dealJson2Future.join();

            associatedEntitiesSlugMap.put("associated_candidates_candidate1", candidateJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_candidates_candidate2", candidateJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_companies_company1", companyJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_companies_company2", companyJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_contacts_contact1", contactJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_contacts_contact2", contactJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_jobs_job1", jobJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_jobs_job2", jobJson2.getString("slug"));
            associatedEntitiesSlugMap.put("associated_deals_deal1", dealJson1.getString("slug"));
            associatedEntitiesSlugMap.put("associated_deals_deal2", dealJson2.getString("slug"));

            associatedEntitiesIdMap.put("associated_candidates_candidate1", function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_candidates_candidate2", function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_companies_company1", function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_companies_company2", function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_contacts_contact1", function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_contacts_contact2", function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_jobs_job1", function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_jobs_job2", function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson2.getString("slug")));
            associatedEntitiesIdMap.put("associated_deals_deal1", function.getDealIdBySlug(albatrossURL, albatrossAuthToken, dealJson1.getString("slug")));
            associatedEntitiesIdMap.put("associated_deals_deal2", function.getDealIdBySlug(albatrossURL, albatrossAuthToken, dealJson2.getString("slug")));

            String assocContact1Id = String.valueOf(associatedEntitiesIdMap.get("associated_contacts_contact1"));
            String assocContact2Id = String.valueOf(associatedEntitiesIdMap.get("associated_contacts_contact2"));
            contactKeyToIdMap.put("associated_contacts_contact1", assocContact1Id);
            contactKeyToIdMap.put("associated_contacts_contact2", assocContact2Id);
            contactIdToKeyMap.put(assocContact1Id, "associated_contacts_contact1");
            contactIdToKeyMap.put(assocContact2Id, "associated_contacts_contact2");
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, String> createCustomNoteType() {
        Map<String, String> noteTypeMap = new HashMap<>();
        function.createCustomNoteType(albatrossURL, albatrossAuthToken, "Custom Note Type", false);
        Response response = function.getNoteTypes(albatrossURL, albatrossAuthToken);
        for (int i = 0; i < response.jsonPath().getList("data").size(); i++) {
            String label = response.jsonPath().getString("data[" + i + "].label");
            String id = response.jsonPath().getString("data[" + i + "].id");
            noteTypeMap.put(label, id);
        }
        return noteTypeMap;
    }

    public Map<String, String> createCustomCallType() {
        Map<String, String> callTypeMap = new HashMap<>();
        function.createCustomCallType(albatrossURL, albatrossAuthToken, "Custom Call Type", false);
        Response response = function.getCallTypes(albatrossURL, albatrossAuthToken);
        for (int i = 0; i < response.jsonPath().getList("data").size(); i++) {
            String label = response.jsonPath().getString("data[" + i + "].label");
            String id = response.jsonPath().getString("data[" + i + "].id");
            callTypeMap.put(label, id);
        }
        return callTypeMap;
    }

    public Map<String, String> createUserMap() {
        Map<String, String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
        response.then().statusCode(200);
        JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restricted", user.get("[2].id").toString());
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

    public void setupTaskTypes() {
        try {
            Response response = RestClient.doGet("JSON", albatrossURL, "task-types", albatrossAuthToken, null, null, true);
            
            if (response.getStatusCode() == 200) {
                JsonPath jp = response.jsonPath();
                List<Map<String, Object>> taskTypes = jp.getList("data");
                
                if (taskTypes != null && !taskTypes.isEmpty()) {
                    for (Map<String, Object> taskType : taskTypes) {
                        String label = (String) taskType.get("label");
                        Integer id = (Integer) taskType.get("id");
                        
                        if ("Follow up".equals(label)) {
                            followUpTaskTypeId = id;
                        } else if ("Interview scheduling".equals(label)) {
                            interviewSchedulingTaskTypeId = id;
                        }
                        
                        if (defaultTaskTypeId == 0) {
                            defaultTaskTypeId = id;
                        }
                    }
                    
                    if (followUpTaskTypeId == 0 || interviewSchedulingTaskTypeId == 0) {
                        if (followUpTaskTypeId == 0) followUpTaskTypeId = defaultTaskTypeId;
                        if (interviewSchedulingTaskTypeId == 0) interviewSchedulingTaskTypeId = defaultTaskTypeId;
                    }
                } else {
                    setFallbackTaskTypeIds("No task types in API response");
                }
            } else {
                setFallbackTaskTypeIds("API call failed with status: " + response.getStatusCode());
            }
                             
        } catch (Exception e) {
            setFallbackTaskTypeIds("Exception: " + e.getMessage());
        }
    }
    
    private void setFallbackTaskTypeIds(String reason) {
        defaultTaskTypeId = 82607;
        followUpTaskTypeId = 82607; 
        interviewSchedulingTaskTypeId = 82608;
    }

    public void setupMeetingTypes() {
        try {
            Response response = RestClient.doGet("JSON", baseURL, "meeting-types", accountOwnerAPIKey, null, null, true);
            
            if (response.getStatusCode() == 200) {
                JsonPath jp = response.jsonPath();
                List<Map<String, Object>> meetingTypes = jp.getList("");
                
                if (meetingTypes != null && !meetingTypes.isEmpty()) {
                    for (Map<String, Object> meetingType : meetingTypes) {
                        String label = (String) meetingType.get("label");
                        Integer id = (Integer) meetingType.get("id");
                        
                        if ("Business Development Meeting".equals(label)) {
                            businessDevelopmentMeetingTypeId = id;
                        } else if ("Client Meeting".equals(label)) {
                            clientMeetingTypeId = id;
                        } else if ("Internal Meeting".equals(label)) {
                            internalMeetingTypeId = id;
                        }
                        
                        if (defaultMeetingTypeId == 0) {
                            defaultMeetingTypeId = id;
                        }
                    }
                    
                    if (businessDevelopmentMeetingTypeId == 0) businessDevelopmentMeetingTypeId = defaultMeetingTypeId;
                    if (clientMeetingTypeId == 0) clientMeetingTypeId = defaultMeetingTypeId;
                    if (internalMeetingTypeId == 0) internalMeetingTypeId = defaultMeetingTypeId;
                } else {
                    setFallbackMeetingTypeIds("No meeting types in API response");
                }
            } else {
                setFallbackMeetingTypeIds("API call failed with status: " + response.getStatusCode());
            }
                             
        } catch (Exception e) {
            setFallbackMeetingTypeIds("Exception: " + e.getMessage());
        }
    }
    
    private void setFallbackMeetingTypeIds(String reason) {
        defaultMeetingTypeId = 1;
        businessDevelopmentMeetingTypeId = 1; 
        clientMeetingTypeId = 4;
        internalMeetingTypeId = 5;
    }

    private List<Integer> createNotesForContact(Map<String, String> contactSlugMap) {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchContact_data.json");
        ConcurrentMap<String, Integer> noteIdMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String contactKey : contactSlugMap.keySet()) {
                if (!contactJson.has(contactKey) || !contactJson.getJSONObject(contactKey).has("notes")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String contactSlug = contactSlugMap.get(contactKey);
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject notesData = contactEntry.getJSONObject("notes");
                    JSONObject processed = processNotePayload(notesData, contactSlug);
                    Response response = function.createNotesByPayload(baseURL, accountOwnerAPIKey, processed);
                    noteIdMap.put(contactKey, response.jsonPath().getInt("id"));
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(noteIdMap.values());
    }

    private JSONObject processNotePayload(JSONObject notesData, String contactSlug) {
        JSONObject note = new JSONObject();
        
        if (notesData.has("note_type_id")) {
            String noteTypeLabel = notesData.getString("note_type_id").replace("{", "").replace("}", "");
            String noteTypeId = noteTypeMap.get(noteTypeLabel);
            if (noteTypeId != null) {
                note.put("note_type_id", noteTypeId);
            }
        }
        
        if (notesData.has("description")) {
            note.put("description", notesData.getString("description"));
        }
        
        note.put("related_to", contactSlug);
        note.put("related_to_type", "contact");
        
        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            if (notesData.has(field)) {
                String fieldValue = notesData.getString(field);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    note.put(field, fieldValue);
                } else {
                    note.put(field, "");
                }
            } else {
                note.put(field, "");
            }
        }
        
        // Process associated entity fields after setting initial values
        for (String field : associatedFields) {
            if (note.has(field) && !note.getString(field).isEmpty()) {
                super.processAssociatedEntityField(note, field, associatedEntitiesSlugMap);
            }
        }
        
        if (notesData.has("created_by") && userMap != null) {
            String createdBy = notesData.getString("created_by").replace("{", "").replace("}", "");
            String createdById = userMap.get(createdBy);
            if (createdById != null) {
                note.put("created_by", createdById);
            }
        }

        if (notesData.has("updated_by") && userMap != null) {
            String updatedBy = notesData.getString("updated_by").replace("{", "").replace("}", "");
            String updatedById = userMap.get(updatedBy);
            if (updatedById != null) {
                note.put("updated_by", updatedById);
            }
        }

        super.processCollaboratorField(note, "collaborator_team_ids", teamMap);
        super.processCollaboratorField(note, "collaborator_user_ids", userMap);
        
        if (notesData.has("enable_auto_populate_teams")) {
            note.put("enable_auto_populate_teams", notesData.getString("enable_auto_populate_teams"));
        }
        
        return note;
    }

    private List<Integer> createTasksForContact(Map<String, String> contactSlugMap) {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchContact_data.json");
        LinkedHashMap<String, Integer> taskIdMap = new LinkedHashMap<>();

        List<String> sortedContactKeys = contactSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String contactKey : sortedContactKeys) {
                if (!contactJson.has(contactKey) || !contactJson.getJSONObject(contactKey).has("tasks")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String contactSlug = contactSlugMap.get(contactKey);
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject tasksData = contactEntry.getJSONObject("tasks");
                    Task task = processTaskPayload(tasksData, contactSlug);
                    Response response = RestClient.doPost("JSON", baseURL, "tasks", accountOwnerAPIKey, null, true, task);
                    if (response.getStatusCode() == 200) {
                        taskIdMap.put(contactKey, response.jsonPath().getInt("id"));
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(taskIdMap.values());
    }

    private Task processTaskPayload(JSONObject payload, String contactSlug) {
        Task task = new Task();
        
        task.setTitle(payload.optString("title", "Default Task Title"));
        task.setDescription(payload.optString("description", "Task description"));
        task.setStart_date(fakerTask.getFutureDate());
        task.setReminder(payload.optInt("reminder", 30));
        
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{contact")) {
            task.setRelated_to(contactSlug);
        } else {
            task.setRelated_to(payload.optString("related_to", contactSlug));
        }
        task.setRelated_to_type(payload.optString("related_to_type", "contact"));
        
        if (payload.has("task_type_id")) {
            String taskTypeIdStr = payload.getString("task_type_id");
            taskTypeIdStr = replacePlaceholdersInTaskValue(taskTypeIdStr);
            try {
                int taskTypeId = Integer.parseInt(taskTypeIdStr);
                task.setTask_type_id(taskTypeId);
            } catch (NumberFormatException e) {
                if (defaultTaskTypeId > 0) {
                    task.setTask_type_id(defaultTaskTypeId);
                }
            }
        } else if (defaultTaskTypeId > 0) {
            task.setTask_type_id(defaultTaskTypeId);
        }
        if (payload.has("enable_auto_populate_teams")) {
            task.setEnable_auto_populate_teams(payload.getInt("enable_auto_populate_teams"));
        }
        if (payload.has("owner_id") && userMap != null) {
            String ownerId = payload.getString("owner_id").replace("{", "").replace("}", "");
            String ownerIdStr = userMap.get(ownerId);
            if (ownerIdStr != null) {
                task.setOwner_id(Integer.parseInt(ownerIdStr));
            }
        }
        
        if (payload.has("created_by") && userMap != null) {
            String createdBy = payload.getString("created_by").replace("{", "").replace("}", "");
            String createdByIdStr = userMap.get(createdBy);
            if (createdByIdStr != null) {
                task.setCreated_by(Integer.parseInt(createdByIdStr));
            }
        }
        
        if (payload.has("updated_by") && userMap != null) {
            String updatedBy = payload.getString("updated_by").replace("{", "").replace("}", "");
            String updatedByIdStr = userMap.get(updatedBy);
            if (updatedByIdStr != null) {
                task.setUpdated_by(Integer.parseInt(updatedByIdStr));
            }
        }
        
        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            String value = processAssociatedEntityValue(payload.optString(field, ""), associatedEntitiesSlugMap);
            switch (field) {
                case "associated_candidates":
                    task.setAssociated_candidates(value);
                    break;
                case "associated_companies":
                    task.setAssociated_companies(value);
                    break;
                case "associated_contacts":
                    task.setAssociated_contacts(value);
                    break;
                case "associated_jobs":
                    task.setAssociated_jobs(value);
                    break;
                case "associated_deals":
                    task.setAssociated_deals(value);
                    break;
            }
        }
        if (payload.has("collaborator_team_ids")) {
            String teamIds = processCollaboratorValue(payload.getString("collaborator_team_ids"), teamMap);
            task.setCollaborator_team_ids(teamIds);
        }
        
        if (payload.has("collaborators")) {
            String collaborators = processCollaboratorValue(payload.getString("collaborators"), userMap);
            task.setCollaborators(collaborators);
        }
        
        return task;
    }

    private List<Integer> createCallLogForContact(Map<String, String> contactSlugMap) {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchContact_data.json");
        ConcurrentMap<String, Integer> callLogIdMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String contactKey : contactSlugMap.keySet()) {
                if (!contactJson.has(contactKey) || !contactJson.getJSONObject(contactKey).has("callLogs")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String contactSlug = contactSlugMap.get(contactKey);
                    JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                    JSONObject callLogsData = contactEntry.getJSONObject("callLogs");
                    JSONObject payload = processCallLogPayload(callLogsData, contactSlug);
                    Response response = function.createCallLogByPayload(baseURL, accountOwnerAPIKey, payload.getJSONArray("calls").getJSONObject(0));
                    callLogIdMap.put(contactKey, response.jsonPath().getInt("id"));
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        List<Integer> callLogIds = new ArrayList<>(callLogIdMap.values());
        return callLogIds;
    }

    private JSONObject processCallLogPayload(JSONObject payload, String contactSlug) {
        if (payload.has("calls") && payload.get("calls") instanceof org.json.JSONArray) {
            JSONArray callsArray = payload.getJSONArray("calls");

            for (int i = 0; i < callsArray.length(); i++) {
                JSONObject call = callsArray.getJSONObject(i);
                if (call.has("related_to") && call.getString("related_to").startsWith("{contact")) {
                    call.put("related_to", contactSlug);
                }
                call.put("related_to_type", "contact");

                String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
                for (String field : associatedFields) {
                    super.processAssociatedEntityField(call, field, associatedEntitiesSlugMap);
                }

                if (call.has("custom_call_type_id") && callTypeMap != null) {
                    String callTypeLabel = call.getString("custom_call_type_id").replace("{", "").replace("}", "");
                    String callTypeId = callTypeMap.get(callTypeLabel);
                    if (callTypeId != null) {
                        call.put("custom_call_type_id", callTypeId);
                    }
                }

                if (call.has("created_by") && userMap != null) {
                    String createdBy = call.getString("created_by").replace("{", "").replace("}", "");
                    String createdById = userMap.get(createdBy);
                    if (createdById != null) {
                        call.put("created_by", createdById);
                    }
                }

                if (call.has("updated_by") && userMap != null) {
                    String updatedBy = call.getString("updated_by").replace("{", "").replace("}", "");
                    String updatedById = userMap.get(updatedBy);
                    if (updatedById != null) {
                        call.put("updated_by", updatedById);
                    }
                }

                super.processCollaboratorField(call, "collaborator_team_ids", teamMap);
                super.processCollaboratorField(call, "collaborator_user_ids", userMap);
            }
        }
        return payload;
    }

    private List<Integer> createMeetingsForContact(Map<String, String> contactSlugMap) {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchContact_data.json");
        LinkedHashMap<String, Integer> meetingIdMap = new LinkedHashMap<>();

        List<String> sortedContactKeys = contactSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String contactKey : sortedContactKeys) {
                if (!contactJson.has(contactKey) || !contactJson.getJSONObject(contactKey).has("meetings")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        String contactSlug = contactSlugMap.get(contactKey);
                        JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                        JSONObject payload = contactEntry.getJSONObject("meetings");
                        Meeting meeting = processMeetingPayload(payload, contactSlug);
                        
                        Response response = RestClient.doPost("JSON", baseURL, "meetings", accountOwnerAPIKey, null, true, meeting);
                        
                        if (response.getStatusCode() == 200) {
                            int meetingId = response.jsonPath().getInt("id");
                            meetingIdMap.put(contactKey, meetingId);
                        }
                    } catch (Exception e) {
                        System.err.println("Exception creating meeting for " + contactKey + ": " + e.getMessage());
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        List<Integer> sortedMeetingIds = sortedContactKeys.stream()
                .filter(meetingIdMap::containsKey)
                .map(meetingIdMap::get)
                .collect(Collectors.toList());
        
        return sortedMeetingIds;
    }

    private Meeting processMeetingPayload(JSONObject payload, String contactSlug) {
        Meeting meeting = new Meeting();
        
        meeting.setTitle(payload.optString("title", "Default Meeting Title"));
        meeting.setDescription(payload.optString("description", "Meeting description"));
        meeting.setAddress(payload.optString("address", "Office Address"));

        String startDate = fakerMeeting.getFutureDate();
        meeting.setStart_date(startDate);
        meeting.setEnd_date(fakerMeeting.getEndDateWithReferenceDate(startDate));

        int reminderValue = payload.optInt("reminder", 30);
        int[] validReminders = {15, 30, 45, 60};
        boolean isValidReminder = false;
        for (int validReminder : validReminders) {
            if (reminderValue == validReminder) {
                isValidReminder = true;
                break;
            }
        }
        meeting.setReminder(isValidReminder ? reminderValue : 30);
        
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{contact")) {
            meeting.setRelated_to(contactSlug);
        } else {
            meeting.setRelated_to(payload.optString("related_to", contactSlug));
        }
        meeting.setRelated_to_type(payload.optString("related_to_type", "contact"));
        
        if (payload.has("meeting_type_id")) {
            String meetingTypeIdStr = payload.getString("meeting_type_id");
            meetingTypeIdStr = replacePlaceholdersInMeetingValue(meetingTypeIdStr);
            try {
                int meetingTypeId = Integer.parseInt(meetingTypeIdStr);
                meeting.setMeeting_type_id(meetingTypeId);
            } catch (NumberFormatException e) {
                if (defaultMeetingTypeId > 0) {
                    meeting.setMeeting_type_id(defaultMeetingTypeId);
                }
            }
        } else if (defaultMeetingTypeId > 0) {
            meeting.setMeeting_type_id(defaultMeetingTypeId);
        }
        
        if (payload.has("enable_auto_populate_teams")) {
            meeting.setEnable_auto_populate_teams(payload.getInt("enable_auto_populate_teams"));
        }
        if (payload.has("owner_id") && userMap != null) {
            String ownerId = payload.getString("owner_id").replace("{", "").replace("}", "");
            String ownerIdStr = userMap.get(ownerId);
            if (ownerIdStr != null) {
                meeting.setOwner_id(Integer.parseInt(ownerIdStr));
            } else if (!userMap.isEmpty()) {
                String firstUserId = userMap.values().iterator().next();
                meeting.setOwner_id(Integer.parseInt(firstUserId));
            }
        } else if (userMap != null && !userMap.isEmpty()) {
            String firstUserId = userMap.values().iterator().next();
            meeting.setOwner_id(Integer.parseInt(firstUserId));
        }
        
        if (payload.has("created_by") && userMap != null) {
            String createdBy = payload.getString("created_by").replace("{", "").replace("}", "");
            String createdByIdStr = userMap.get(createdBy);
            if (createdByIdStr != null) {
                meeting.setCreated_by(Integer.parseInt(createdByIdStr));
            }
        }
        
        if (payload.has("updated_by") && userMap != null) {
            String updatedBy = payload.getString("updated_by").replace("{", "").replace("}", "");
            String updatedByIdStr = userMap.get(updatedBy);
            if (updatedByIdStr != null) {
                meeting.setUpdated_by(Integer.parseInt(updatedByIdStr));
            }
        }
        
        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            String value = processAssociatedEntityValue(payload.optString(field, ""), associatedEntitiesSlugMap);
            switch (field) {
                case "associated_candidates":
                    meeting.setAssociated_candidates(value);
                    break;
                case "associated_companies":
                    meeting.setAssociated_companies(value);
                    break;
                case "associated_contacts":
                    meeting.setAssociated_contacts(value);
                    break;
                case "associated_jobs":
                    meeting.setAssociated_jobs(value);
                    break;
                case "associated_deals":
                    meeting.setAssociated_deals(value);
                    break;
            }
        }
        if (payload.has("collaborator_team_ids")) {
            String teamIds = processCollaboratorValue(payload.getString("collaborator_team_ids"), teamMap);
            meeting.setCollaborator_team_ids(teamIds);
        }
        
        if (payload.has("collaborator_user_ids")) {
            String collaborators = processCollaboratorValue(payload.getString("collaborator_user_ids"), userMap);
            meeting.setCollaborator_user_ids(collaborators);
        }
        
        return meeting;
    }

    private String replacePlaceholdersInTaskValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }
        
        String result = filterValue;
        
        result = result.replace("{{task_type_follow_up}}", String.valueOf(followUpTaskTypeId));
        result = result.replace("{{task_type_interview_scheduling}}", String.valueOf(interviewSchedulingTaskTypeId));
        
        result = result.replace("{{owner}}", userMap.get("owner"));
        result = result.replace("{{admin}}", userMap.get("admin"));
        result = result.replace("{{teamMember}}", userMap.get("teamMember"));
        result = result.replace("{{restricted}}", userMap.get("restricted"));
        
        result = result.replace("{{team}}", teamMap.get("team"));
        result = result.replace("{{associated_companies_company1}}", String.valueOf(associatedEntitiesIdMap.get("associated_companies_company1")));
        result = result.replace("{{associated_companies_company2}}", String.valueOf(associatedEntitiesIdMap.get("associated_companies_company2")));
        result = result.replace("{{associated_contacts_contact1}}", String.valueOf(associatedEntitiesIdMap.get("associated_contacts_contact1")));
        result = result.replace("{{associated_contacts_contact2}}", String.valueOf(associatedEntitiesIdMap.get("associated_contacts_contact2")));
        result = result.replace("{{associated_jobs_job1}}", String.valueOf(associatedEntitiesIdMap.get("associated_jobs_job1")));
        result = result.replace("{{associated_jobs_job2}}", String.valueOf(associatedEntitiesIdMap.get("associated_jobs_job2")));
        result = result.replace("{{associated_candidates_candidate1}}", String.valueOf(associatedEntitiesIdMap.get("associated_candidates_candidate1")));
        result = result.replace("{{associated_candidates_candidate2}}", String.valueOf(associatedEntitiesIdMap.get("associated_candidates_candidate2")));
        result = result.replace("{{associated_deals_deal1}}", String.valueOf(associatedEntitiesIdMap.get("associated_deals_deal1")));
        result = result.replace("{{associated_deals_deal2}}", String.valueOf(associatedEntitiesIdMap.get("associated_deals_deal2")));
        
        return result;
    }

    public String replacePlaceholdersInMeetingValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }
        
        String result = filterValue;
        
        result = result.replace("{{meeting_type_business_development}}", String.valueOf(businessDevelopmentMeetingTypeId));
        result = result.replace("{{meeting_type_client_meeting}}", String.valueOf(clientMeetingTypeId));
        result = result.replace("{{meeting_type_internal_meeting}}", String.valueOf(internalMeetingTypeId));
        result = result.replace("{{associated_companies_company1}}", String.valueOf(associatedEntitiesIdMap.get("associated_companies_company1")));
        result = result.replace("{{associated_companies_company2}}", String.valueOf(associatedEntitiesIdMap.get("associated_companies_company2")));
        result = result.replace("{{associated_contacts_contact1}}", String.valueOf(associatedEntitiesIdMap.get("associated_contacts_contact1")));
        result = result.replace("{{associated_contacts_contact2}}", String.valueOf(associatedEntitiesIdMap.get("associated_contacts_contact2")));
        result = result.replace("{{associated_jobs_job1}}", String.valueOf(associatedEntitiesIdMap.get("associated_jobs_job1")));
        result = result.replace("{{associated_jobs_job2}}", String.valueOf(associatedEntitiesIdMap.get("associated_jobs_job2")));
        result = result.replace("{{associated_candidates_candidate1}}", String.valueOf(associatedEntitiesIdMap.get("associated_candidates_candidate1")));
        result = result.replace("{{associated_candidates_candidate2}}", String.valueOf(associatedEntitiesIdMap.get("associated_candidates_candidate2")));
        result = result.replace("{{associated_deals_deal1}}", String.valueOf(associatedEntitiesIdMap.get("associated_deals_deal1")));
        result = result.replace("{{associated_deals_deal2}}", String.valueOf(associatedEntitiesIdMap.get("associated_deals_deal2")));
        
        return result;
    }

    private String processAssociatedEntityValue(String fieldValue, Map<String, String> entityMap) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return "";
        }
        
        if (fieldValue.startsWith("{")) {
            String entityKeys = fieldValue.replace("{", "").replace("}", "");
            String[] keys = entityKeys.split(",");
            List<String> entityValues = new ArrayList<>();
            
            for (String key : keys) {
                String trimmedKey = key.trim();
                String entityValue = entityMap.get(trimmedKey);
                if (entityValue != null) {
                    entityValues.add(entityValue);
                }
            }
            
            return String.join(",", entityValues);
        }
        
        return fieldValue;
    }
    
    private String processCollaboratorValue(String fieldValue, Map<String, String> entityMap) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return "";
        }
        
        if (fieldValue.startsWith("{")) {
            String entityKeys = fieldValue.replace("{", "").replace("}", "");
            String[] keys = entityKeys.split(",");
            List<String> entityValues = new ArrayList<>();
            
            for (String key : keys) {
                String trimmedKey = key.trim();
                String entityValue = entityMap.get(trimmedKey);
                if (entityValue != null) {
                    entityValues.add(entityValue);
                }
            }
            
            return String.join(",", entityValues);
        }
        
        return fieldValue;
    }




    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "booleanSearchContactSmokeTestData", description = "[Smoke] Boolean Search Test for Contact")
    public void booleanSearchContactSmokeTest(String testCaseId, String description, String filterValue, String entities, String expectedResult, String expectedResultName, String expectedResultReason) {
        booleanSearchContactTest(testCaseId, description, filterValue, entities, expectedResult, expectedResultName, expectedResultReason);
    }

    @DataProvider(name = "booleanSearchContactSmokeTestData", parallel = true)
    public Object[][] booleanSearchContactSmokeTestData() {
        return limitSmokeRows(booleanSearchContactTestData());
    }
}
