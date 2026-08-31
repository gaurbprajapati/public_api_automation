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
import io.restassured.RestAssured;
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
import java.io.File;
import java.io.IOException;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class BooleanSearchCandidateTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions;
    commanFunction function;
    String albatrossAuthToken;
    String accountOwnerAPIKey;
    Map<String, Integer> customFieldIds = new HashMap<>();
    Map<String, String> entityCFValueMap = new HashMap<>();
    ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();

    Map<String, Map<String, String>> timestampScenarios;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> noteTypeMap;
    Map<String, String> callTypeMap;
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> candidateSlugMap = new ConcurrentHashMap<>();

    int defaultTaskTypeId = 0;
    int followUpTaskTypeId = 0;
    int interviewSchedulingTaskTypeId = 0;
    int defaultMeetingTypeId = 0;
    int businessDevelopmentMeetingTypeId = 0;
    int clientMeetingTypeId = 0;
    int internalMeetingTypeId = 0;

    JavaFakerTask fakerTask = new JavaFakerTask();
    JavaFakerMeeting fakerMeeting = new JavaFakerMeeting();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        function = new commanFunction();
        allCrudFunctions = new AllCrudFunctions();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
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
    @Test(groups = {"aries_service"}, dataProvider = "booleanSearchCandidateTestData", description = "Boolean Search Test for Candidate")
    public void booleanSearchCandidateTest(String testCaseId, String description, String filterValue, String entities, String expectedResult, String expectedResultName, String expectedResultReason) {
        FilterSearchReporter.skipFilterCriteriaLogging();

        String basePath = ADVANCED_SEARCH_CANDIDATES_GET_PATH;
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

        Response response = RestClient.doPost("JSON", ariesServiceURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for test case: " + testCaseId + " (query: " + filterValue + ") is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully");

        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, "Name", "firstname");
        FilterSearchReporter.logResponse(response, data);

        validateBooleanSearchResults(data, testCaseId, description, filterValue, entities, expectedResult);
    }

    public JSONObject createBooleanSearchPayload(String filterValue, String entities) {
        Set<String> validEntities = new HashSet<>(Arrays.asList(
            "entity", "notes", "call_logs", "tasks", "meetings", "files"
        ));

        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
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

    @DataProvider(name = "booleanSearchCandidateTestData")
    public Object[][] booleanSearchCandidateTestData() {
        String filePath = "src/test/resources/filtersDataProvider/candidateBooleanSearchDataProvider.json";
        List<Object[]> testData = new ArrayList<>();

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray testCases = new JSONArray(jsonContent);

            for (int i = 0; i < testCases.length(); i++) {
                JSONObject testCase = testCases.getJSONObject(i);
                String testCaseId = testCase.getString("testCaseId");
                String description = testCase.getString("description");
                String query = testCase.getString("query");

                JSONArray entitiesArray = testCase.getJSONArray("entitiesInvolved");
                StringBuilder entitiesStr = new StringBuilder();
                for (int j = 0; j < entitiesArray.length(); j++) {
                    if (j > 0) {
                        entitiesStr.append(",");
                    }
                    entitiesStr.append(entitiesArray.getString(j));
                }

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
            Assert.assertEquals(data.length(), 0, "Test Case: " + testCaseId + " - Expected empty result but got " + data.length() + " candidates for query: " + filterValue);
            return;
        }

        if (data.length() == 0) {
            Assert.fail("Test Case: " + testCaseId + " - Expected results but got 0 candidates for query: " + filterValue);
        }

        String[] expectedCandidateKeys = expectedResult.split(",");
        List<Integer> expectedCandidateIds = new ArrayList<>();

        for (String candidateKey : expectedCandidateKeys) {
            String normalizedKey = candidateKey.trim().toLowerCase().replace(" ", "");
            String candidateIdStr = candidateKeyToIdMap.get(normalizedKey);
            if (candidateIdStr == null) {
                Assert.fail("Test Case: " + testCaseId + " - Expected candidate key '" + candidateKey + "' (normalized: '" + normalizedKey + "') not found in candidateKeyToIdMap. Available keys: " + candidateKeyToIdMap.keySet());
            }
            expectedCandidateIds.add(Integer.parseInt(candidateIdStr));
        }

        List<Integer> trackedActualIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            int candidateId = data.getJSONObject(i).getInt("id");
            if (candidateIdToKeyMap.containsKey(String.valueOf(candidateId))) {
                trackedActualIds.add(candidateId);
            }
        }

        for (int candidateId : expectedCandidateIds) {
            if (!trackedActualIds.contains(candidateId)) {
                String candidateKey = candidateIdToKeyMap.get(String.valueOf(candidateId));
                Assert.fail("Test Case: " + testCaseId + " - Candidate: " + candidateKey + " (ID: " + candidateId + ") is not present in the actual response but was expected. Query: " + filterValue);
            }
        }

        for (int candidateId : trackedActualIds) {
            if (!expectedCandidateIds.contains(candidateId)) {
                String candidateKey = candidateIdToKeyMap.get(String.valueOf(candidateId));
                Assert.fail("Test Case: " + testCaseId + " - Candidate: " + candidateKey + " (ID: " + candidateId + ") is present in the actual response but was not expected. Query: " + filterValue);
            }
        }

        Assert.assertEquals(trackedActualIds.size(), expectedCandidateIds.size(), "Test Case: " + testCaseId + " - Expected " + expectedCandidateIds.size() + " tracked candidates but got " + trackedActualIds.size() + " for query: " + filterValue);
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchCandidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .map(candidateKey -> CompletableFuture.runAsync(() -> {
                    JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                    JSONObject payload = candidateEntry.getJSONObject("payload");
                    JSONObject processedPayload = processPayloadPlaceholders(payload);
                    Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, albatrossAuthToken, processedPayload);
                    int candidateId = response.jsonPath().getInt("data.candidate.id");
                    String candidateSlug = response.jsonPath().getString("data.candidate.slug");
                    candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey);
                    candidateKeyToIdMap.put(candidateKey, String.valueOf(candidateId));
                    candidateSlugMap.put(candidateKey, candidateSlug);
                }, executor)).toArray(CompletableFuture[]::new)).join();

            createNotesForCandidate(candidateSlugMap);
            createTasksForCandidate(candidateSlugMap);
            createCallLogForCandidate(candidateSlugMap);
            createMeetingsForCandidate(candidateSlugMap);
            createFilesForCandidate(candidateSlugMap);
        } finally {
            executor.shutdown();
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

            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

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

            CompletableFuture<JsonPath> userJsonFuture = CompletableFuture.supplyAsync(() ->
                    function.getUsers(baseURL, accountOwnerAPIKey).jsonPath(), executor);

            CompletableFuture<JsonPath> teamJsonFuture = userJsonFuture.thenApplyAsync((userJson) -> {
                ArrayList<String> team1UserId = new ArrayList<>();
                team1UserId.add(String.valueOf(userJson.getInt("[1].id")));
                team1UserId.add(String.valueOf(userJson.getInt("[3].id")));
                ArrayList<String> team2UserId = new ArrayList<>();
                team2UserId.add(String.valueOf(userJson.getInt("[0].id")));
                team2UserId.add(String.valueOf(userJson.getInt("[2].id")));
                allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team1", team1UserId);
                allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, "team2", team2UserId);
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
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, Integer> createCustomFields() {
        Map<String, Integer> customFieldIds = new HashMap<>();
        List<String> entityTypes = new ArrayList<>(Arrays.asList("candidate", "company", "deals", "job", "contact", "user", "team", "text", "email", "phonenumber", "longtext", "number", "date", "social_profile", "dropdown", "multiselect"));
        ExecutorService executor = Executors.newFixedThreadPool(7);

        try {
            List<CompletableFuture<AbstractMap.SimpleEntry<String, Response>>> futures = entityTypes.stream()
                .map(entity -> CompletableFuture.supplyAsync(() -> {
                    String fieldName = entity + "CF";
                    Response response;
                    if (entity.equals("dropdown") || entity.equals("multiselect")) {
                        response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "candidate", fieldName, entity, "Option A, Option B, OptionC");
                    } else {
                        response = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "candidate", fieldName, entity, "");
                    }
                    return new AbstractMap.SimpleEntry<>(fieldName, response);
                }, executor))
                .collect(Collectors.toList());

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

            if (value instanceof JSONObject) {
                JSONObject nestedObject = (JSONObject) value;
                processedPayload.put(key, processPayloadPlaceholders(nestedObject));
            } else if (value instanceof JSONArray) {
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
            } else if (key.startsWith("{") && key.endsWith("}")) {
                String trimmedKey = key.substring(1, key.length() - 1);
                String lookupKey = ("dealCF".equals(trimmedKey) && customFieldIds.containsKey("dealsCF")) ? "dealsCF" : trimmedKey;
                if (customFieldIds.containsKey(lookupKey)) {
                    String newKey = "custcolumn" + customFieldIds.get(lookupKey);
                    if (value instanceof String) {
                        processedPayload.put(newKey, processEntityPlaceholders((String) value));
                    } else {
                        processedPayload.put(newKey, value);
                    }
                } else {
                    processedPayload.put(key, value);
                }
            } else {
                processedPayload.put(key, value);
            }
        }
        return processedPayload;
    }

    public String processEntityPlaceholders(String value) {
        if (value == null) return null;
        if (value.startsWith("{") && value.endsWith("}")) {
            String innerValue = value.substring(1, value.length() - 1);
            String[] entityKeys = innerValue.split(",");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < entityKeys.length; i++) {
                String entityKey = entityKeys[i].trim();
                if (entityCFValueMap.containsKey(entityKey)) {
                    if (i > 0) result.append(",");
                    result.append(entityCFValueMap.get(entityKey));
                } else {
                    if (i > 0) result.append(",");
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

            CompletableFuture<JsonPath> contactJson1Future = companyJson1Future.thenApplyAsync(companyJson1 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> contactJson2Future = companyJson2Future.thenApplyAsync(companyJson2 ->
                    function.createNewContact_POST(baseURL, accountOwnerAPIKey, companyJson2.getString("slug")).jsonPath(), executor);

            CompletableFuture<JsonPath> jobJson1Future = companyJson1Future.thenCombineAsync(contactJson1Future, (companyJson1, contactJson1) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson1.getString("slug"), contactJson1.getString("slug")).jsonPath(), executor);
            CompletableFuture<JsonPath> jobJson2Future = companyJson2Future.thenCombineAsync(contactJson2Future, (companyJson2, contactJson2) ->
                    function.createNewJob(baseURL, accountOwnerAPIKey, companyJson2.getString("slug"), contactJson2.getString("slug")).jsonPath(), executor);

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
        } finally {
            executor.shutdown();
        }
    }

    public Map<String, String> createCustomNoteType() {
        Map<String, String> noteTypeMap = new HashMap<>();
        function.createCustomNoteType(albatrossURL, albatrossAuthToken, "Custom Note Type", false);
        Response response = function.getNoteTypes(albatrossURL, albatrossAuthToken);
        for (int i = 0; i < response.jsonPath().getList("data").size(); i++) {
            noteTypeMap.put(response.jsonPath().getString("data[" + i + "].label"), response.jsonPath().getString("data[" + i + "].id"));
        }
        return noteTypeMap;
    }

    public Map<String, String> createCustomCallType() {
        Map<String, String> callTypeMap = new HashMap<>();
        function.createCustomCallType(albatrossURL, albatrossAuthToken, "Custom Call Type", false);
        Response response = function.getCallTypes(albatrossURL, albatrossAuthToken);
        for (int i = 0; i < response.jsonPath().getList("data").size(); i++) {
            callTypeMap.put(response.jsonPath().getString("data[" + i + "].label"), response.jsonPath().getString("data[" + i + "].id"));
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
        ArrayList<String> userId = new ArrayList<>();
        userId.add(String.valueOf(userMap.get("owner")));
        userId.add(String.valueOf(userMap.get("teamMember")));
        allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId);
        Response team = function.getTeams(baseURL, accountOwnerAPIKey);
        teamMap.put("team", team.jsonPath().getString("[0].team_id"));
        return teamMap;
    }

    public void setupTaskTypes() {
        try {
            Response response = RestClient.doGet("JSON", albatrossURL, "task-types", albatrossAuthToken, null, null, true);
            if (response.getStatusCode() == 200) {
                List<Map<String, Object>> taskTypes = response.jsonPath().getList("data");
                if (taskTypes != null && !taskTypes.isEmpty()) {
                    for (Map<String, Object> taskType : taskTypes) {
                        String label = (String) taskType.get("label");
                        Integer id = (Integer) taskType.get("id");
                        if ("Follow up".equals(label)) followUpTaskTypeId = id;
                        else if ("Interview scheduling".equals(label)) interviewSchedulingTaskTypeId = id;
                        if (defaultTaskTypeId == 0) defaultTaskTypeId = id;
                    }
                    if (followUpTaskTypeId == 0) followUpTaskTypeId = defaultTaskTypeId;
                    if (interviewSchedulingTaskTypeId == 0) interviewSchedulingTaskTypeId = defaultTaskTypeId;
                }
            }
        } catch (Exception e) {
            followUpTaskTypeId = 82607;
            interviewSchedulingTaskTypeId = 82608;
        }
    }

    public void setupMeetingTypes() {
        try {
            Response response = RestClient.doGet("JSON", baseURL, "meeting-types", accountOwnerAPIKey, null, null, true);
            if (response.getStatusCode() == 200) {
                List<Map<String, Object>> meetingTypes = response.jsonPath().getList("");
                if (meetingTypes != null && !meetingTypes.isEmpty()) {
                    for (Map<String, Object> meetingType : meetingTypes) {
                        String label = (String) meetingType.get("label");
                        Integer id = (Integer) meetingType.get("id");
                        if ("Business Development Meeting".equals(label)) businessDevelopmentMeetingTypeId = id;
                        else if ("Client Meeting".equals(label)) clientMeetingTypeId = id;
                        else if ("Internal Meeting".equals(label)) internalMeetingTypeId = id;
                        if (defaultMeetingTypeId == 0) defaultMeetingTypeId = id;
                    }
                    if (businessDevelopmentMeetingTypeId == 0) businessDevelopmentMeetingTypeId = defaultMeetingTypeId;
                    if (clientMeetingTypeId == 0) clientMeetingTypeId = defaultMeetingTypeId;
                    if (internalMeetingTypeId == 0) internalMeetingTypeId = defaultMeetingTypeId;
                }
            }
        } catch (Exception e) {
            defaultMeetingTypeId = 1;
            businessDevelopmentMeetingTypeId = 1;
            clientMeetingTypeId = 4;
            internalMeetingTypeId = 5;
        }
    }

    private List<Integer> createNotesForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchCandidate_data.json");
        ConcurrentMap<String, Integer> noteIdMap = new ConcurrentHashMap<>();

        List<String> sortedCandidateKeys = candidateSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String candidateKey : sortedCandidateKeys) {
                if (!candidateJson.has(candidateKey) || !candidateJson.getJSONObject(candidateKey).has("notes")) {
                    continue;
                }

                final String key = candidateKey;
                futures.add(CompletableFuture.runAsync(() -> {
                    String candidateSlug = candidateSlugMap.get(key);
                    JSONObject notesData = candidateJson.getJSONObject(key).getJSONObject("notes");
                    JSONObject processed = processNotePayload(notesData, candidateSlug);
                    Response response = function.createNotesByPayload(baseURL, accountOwnerAPIKey, processed);
                    noteIdMap.put(key, response.jsonPath().getInt("id"));
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(noteIdMap.values());
    }

    private JSONObject processNotePayload(JSONObject notesData, String candidateSlug) {
        JSONObject note = new JSONObject(notesData.toString());

        note.put("related_to", candidateSlug);
        note.put("related_to_type", "candidate");

        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            if (!note.has(field)) {
                note.put(field, "");
            }
        }

        for (String field : associatedFields) {
            if (note.has(field) && !note.getString(field).isEmpty()) {
                super.processAssociatedEntityField(note, field, associatedEntitiesSlugMap);
            }
        }

        if (note.has("note_type_id") && noteTypeMap != null) {
            String noteTypeLabel = note.getString("note_type_id").replace("{", "").replace("}", "");
            String noteTypeId = noteTypeMap.get(noteTypeLabel);
            if (noteTypeId != null) {
                note.put("note_type_id", Integer.parseInt(noteTypeId));
            }
        }

        if (note.has("created_by") && userMap != null) {
            String createdBy = note.getString("created_by").replace("{", "").replace("}", "");
            String createdById = userMap.get(createdBy);
            if (createdById != null) {
                note.put("created_by", Integer.parseInt(createdById));
            }
        }

        if (note.has("updated_by") && userMap != null) {
            String updatedBy = note.getString("updated_by").replace("{", "").replace("}", "");
            String updatedById = userMap.get(updatedBy);
            if (updatedById != null) {
                note.put("updated_by", Integer.parseInt(updatedById));
            }
        }

        if (note.has("collaborator_team_ids")) {
            super.processCollaboratorField(note, "collaborator_team_ids", teamMap);
        }
        if (note.has("collaborator_user_ids")) {
            super.processCollaboratorField(note, "collaborator_user_ids", userMap);
        }

        if (note.has("enable_auto_populate_teams")) {
            int autoPopulateTeams = note.optInt("enable_auto_populate_teams");
            note.put("enable_auto_populate_teams", autoPopulateTeams);
            if (autoPopulateTeams == 1 && !note.has("created_by") && userMap != null && userMap.containsKey("owner")) {
                note.put("created_by", Integer.parseInt(userMap.get("owner")));
            }
        }

        return note;
    }

    private List<Integer> createTasksForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchCandidate_data.json");
        LinkedHashMap<String, Integer> taskIdMap = new LinkedHashMap<>();
        List<String> sortedKeys = new ArrayList<>(candidateSlugMap.keySet());
        Collections.sort(sortedKeys);
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String candidateKey : sortedKeys) {
                if (!candidateJson.has(candidateKey) || !candidateJson.getJSONObject(candidateKey).has("tasks")) continue;
                final String key = candidateKey;
                futures.add(CompletableFuture.runAsync(() -> {
                    String candidateSlug = candidateSlugMap.get(key);
                    JSONObject tasksData = candidateJson.getJSONObject(key).getJSONObject("tasks");
                    Task task = processTaskPayload(tasksData, candidateSlug);
                    Response response = RestClient.doPost("JSON", baseURL, "tasks", accountOwnerAPIKey, null, true, task);
                    if (response.getStatusCode() == 200) taskIdMap.put(key, response.jsonPath().getInt("id"));
                }, executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
        return new ArrayList<>(taskIdMap.values());
    }

    private Task processTaskPayload(JSONObject payload, String candidateSlug) {
        Task task = new Task();
        task.setTitle(payload.optString("title", "Default Task"));
        task.setDescription(payload.optString("description", ""));
        task.setStart_date(fakerTask.getFutureDate());
        task.setReminder(payload.optInt("reminder", 30));
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{candidate")) {
            task.setRelated_to(candidateSlug);
        } else {
            task.setRelated_to(payload.optString("related_to", candidateSlug));
        }
        task.setRelated_to_type("candidate");
        String taskTypeStr = payload.optString("task_type_id", "");
        if (taskTypeStr.contains("{{task_type_follow_up}}")) taskTypeStr = String.valueOf(followUpTaskTypeId);
        else if (taskTypeStr.contains("{{task_type_interview_scheduling}}")) taskTypeStr = String.valueOf(interviewSchedulingTaskTypeId);
        try {
            task.setTask_type_id(Integer.parseInt(taskTypeStr));
        } catch (NumberFormatException e) {
            task.setTask_type_id(followUpTaskTypeId > 0 ? followUpTaskTypeId : defaultTaskTypeId);
        }
        if (payload.has("owner_id") && userMap != null) {
            String ownerId = payload.getString("owner_id").replace("{", "").replace("}", "");
            if (userMap.containsKey(ownerId)) task.setOwner_id(Integer.parseInt(userMap.get(ownerId)));
        }
        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            String value = processAssociatedEntityValue(payload.optString(field, ""), associatedEntitiesSlugMap);
            if ("associated_candidates".equals(field)) task.setAssociated_candidates(value);
            else if ("associated_companies".equals(field)) task.setAssociated_companies(value);
            else if ("associated_contacts".equals(field)) task.setAssociated_contacts(value);
            else if ("associated_jobs".equals(field)) task.setAssociated_jobs(value);
            else if ("associated_deals".equals(field)) task.setAssociated_deals(value);
        }
        if (payload.has("collaborator_team_ids")) task.setCollaborator_team_ids(processCollaboratorValue(payload.getString("collaborator_team_ids"), teamMap));
        if (payload.has("collaborators")) task.setCollaborators(processCollaboratorValue(payload.getString("collaborators"), userMap));
        return task;
    }

    private List<Integer> createCallLogForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchCandidate_data.json");
        ConcurrentMap<String, Integer> callLogIdMap = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String candidateKey : candidateSlugMap.keySet()) {
                if (!candidateJson.has(candidateKey) || !candidateJson.getJSONObject(candidateKey).has("callLogs")) continue;
                final String key = candidateKey;
                futures.add(CompletableFuture.runAsync(() -> {
                    String candidateSlug = candidateSlugMap.get(key);
                    JSONObject callLogsData = candidateJson.getJSONObject(key).getJSONObject("callLogs");
                    JSONObject processed = processCallLogPayload(callLogsData, candidateSlug);
                    Response response = function.createCallLogByPayload(baseURL, accountOwnerAPIKey, processed.getJSONArray("calls").getJSONObject(0));
                    callLogIdMap.put(key, response.jsonPath().getInt("id"));
                }, executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
        return new ArrayList<>(callLogIdMap.values());
    }

    private JSONObject processCallLogPayload(JSONObject payload, String candidateSlug) {
        if (payload.has("calls") && payload.get("calls") instanceof JSONArray) {
            JSONArray callsArray = payload.getJSONArray("calls");
            for (int i = 0; i < callsArray.length(); i++) {
                JSONObject call = callsArray.getJSONObject(i);
                if (call.has("related_to") && call.getString("related_to").startsWith("{candidate")) {
                    call.put("related_to", candidateSlug);
                }
                call.put("related_to_type", "candidate");
                String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
                for (String field : associatedFields) super.processAssociatedEntityField(call, field, associatedEntitiesSlugMap);
                if (call.has("collaborator_user_ids")) super.processCollaboratorField(call, "collaborator_user_ids", userMap);
                if (call.has("collaborator_team_ids")) super.processCollaboratorField(call, "collaborator_team_ids", teamMap);
            }
        }
        return payload;
    }

    private List<Integer> createMeetingsForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchCandidate_data.json");
        LinkedHashMap<String, Integer> meetingIdMap = new LinkedHashMap<>();
        List<String> sortedKeys = new ArrayList<>(candidateSlugMap.keySet());
        Collections.sort(sortedKeys);
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String candidateKey : sortedKeys) {
                if (!candidateJson.has(candidateKey) || !candidateJson.getJSONObject(candidateKey).has("meetings")) continue;
                final String key = candidateKey;
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        String candidateSlug = candidateSlugMap.get(key);
                        JSONObject meetingsData = candidateJson.getJSONObject(key).getJSONObject("meetings");
                        Meeting meeting = processMeetingPayload(meetingsData, candidateSlug);
                        Response response = RestClient.doPost("JSON", baseURL, "meetings", accountOwnerAPIKey, null, true, meeting);
                        if (response.getStatusCode() == 200) meetingIdMap.put(key, response.jsonPath().getInt("id"));
                    } catch (Exception e) {
                        System.err.println("Exception creating meeting for " + key + ": " + e.getMessage());
                    }
                }, executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
        return new ArrayList<>(meetingIdMap.values());
    }

    private Meeting processMeetingPayload(JSONObject payload, String candidateSlug) {
        Meeting meeting = new Meeting();
        meeting.setTitle(payload.optString("title", "Default Meeting"));
        meeting.setDescription(payload.optString("description", ""));
        meeting.setAddress("Office Address");
        String startDate = fakerMeeting.getFutureDate();
        meeting.setStart_date(startDate);
        meeting.setEnd_date(fakerMeeting.getEndDateWithReferenceDate(startDate));
        meeting.setReminder(15);
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{candidate")) {
            meeting.setRelated_to(candidateSlug);
        } else {
            meeting.setRelated_to(payload.optString("related_to", candidateSlug));
        }
        meeting.setRelated_to_type("candidate");
        String meetingTypeStr = payload.optString("meeting_type_id", "").replace("{{meeting_type_business_development}}", String.valueOf(businessDevelopmentMeetingTypeId)).replace("{{meeting_type_client_meeting}}", String.valueOf(clientMeetingTypeId)).replace("{{meeting_type_internal_meeting}}", String.valueOf(internalMeetingTypeId));
        try {
            meeting.setMeeting_type_id(Integer.parseInt(meetingTypeStr));
        } catch (NumberFormatException e) {
            meeting.setMeeting_type_id(businessDevelopmentMeetingTypeId > 0 ? businessDevelopmentMeetingTypeId : defaultMeetingTypeId);
        }
        if (payload.has("owner_id") && userMap != null) {
            String ownerId = payload.getString("owner_id").replace("{", "").replace("}", "");
            if (userMap.containsKey(ownerId)) meeting.setOwner_id(Integer.parseInt(userMap.get(ownerId)));
            else if (!userMap.isEmpty()) meeting.setOwner_id(Integer.parseInt(userMap.values().iterator().next()));
        } else if (userMap != null && !userMap.isEmpty()) {
            meeting.setOwner_id(Integer.parseInt(userMap.values().iterator().next()));
        }
        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            String value = processAssociatedEntityValue(payload.optString(field, ""), associatedEntitiesSlugMap);
            if ("associated_candidates".equals(field)) meeting.setAssociated_candidates(value);
            else if ("associated_companies".equals(field)) meeting.setAssociated_companies(value);
            else if ("associated_contacts".equals(field)) meeting.setAssociated_contacts(value);
            else if ("associated_jobs".equals(field)) meeting.setAssociated_jobs(value);
            else if ("associated_deals".equals(field)) meeting.setAssociated_deals(value);
        }
        if (payload.has("collaborator_team_ids")) meeting.setCollaborator_team_ids(processCollaboratorValue(payload.getString("collaborator_team_ids"), teamMap));
        if (payload.has("collaborator_user_ids")) meeting.setCollaborator_user_ids(processCollaboratorValue(payload.getString("collaborator_user_ids"), userMap));
        return meeting;
    }

    private String processAssociatedEntityValue(String fieldValue, Map<String, String> entityMap) {
        if (fieldValue == null || fieldValue.isEmpty()) return "";
        if (fieldValue.startsWith("{")) {
            String entityKeys = fieldValue.replace("{", "").replace("}", "");
            String[] keys = entityKeys.split(",");
            List<String> entityValues = new ArrayList<>();
            for (String key : keys) {
                String trimmedKey = key.trim();
                if (entityMap.containsKey(trimmedKey)) entityValues.add(entityMap.get(trimmedKey));
            }
            return String.join(",", entityValues);
        }
        return fieldValue;
    }

    private String processCollaboratorValue(String fieldValue, Map<String, String> entityMap) {
        if (fieldValue == null || fieldValue.isEmpty()) return "";
        if (fieldValue.startsWith("{")) {
            String entityKeys = fieldValue.replace("{", "").replace("}", "");
            String[] keys = entityKeys.split(",");
            List<String> entityValues = new ArrayList<>();
            for (String key : keys) {
                String trimmedKey = key.trim();
                if (entityMap.containsKey(trimmedKey)) entityValues.add(entityMap.get(trimmedKey));
            }
            return String.join(",", entityValues);
        }
        return fieldValue;
    }

    private void createFilesForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/booleanSearchCandidate_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            List<CompletableFuture<Void>> futures = candidateSlugMap.keySet().stream()
                    .filter(candidateKey -> candidateJson.has(candidateKey)
                            && candidateJson.getJSONObject(candidateKey).has("files"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        String candidateSlug = candidateSlugMap.get(candidateKey);
                        JSONArray filesData = candidateJson.getJSONObject(candidateKey).getJSONArray("files");
                        for (int i = 0; i < filesData.length(); i++) {
                            attachFileToCandidate(candidateSlug, filesData.getJSONObject(i));
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private void attachFileToCandidate(String candidateSlug, JSONObject fileEntry) {
        String resourceFileName = fileEntry.getString("resourceFileName");
        File file = new File("src/test/resources/testData/booleanSearchFiles/" + resourceFileName);
        Assert.assertTrue(file.exists() && file.isFile(),
                "Boolean search file fixture not found: " + file.getAbsolutePath());

        RestAssured.baseURI = baseURL;
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + accountOwnerAPIKey)
                .multiPart("related_to", candidateSlug)
                .multiPart("related_to_type", "candidate")
                .multiPart("files[]", file)
                .post("files");

        Assert.assertEquals(response.getStatusCode(), 200,
                "Failed to attach file '" + resourceFileName + "' to candidate slug: " + candidateSlug);
    }


    // ARIES_SMOKE_WRAPPERS

    @Owner("Raj Pandey")
    @Test(groups = {FilterSearchBaseTest.ARIES_SERVICE_SMOKE_GROUP, "nightly-build"}, dataProvider = "booleanSearchCandidateSmokeTestData", description = "[Smoke] Boolean Search Test for Candidate")
    public void booleanSearchCandidateSmokeTest(String testCaseId, String description, String filterValue, String entities, String expectedResult, String expectedResultName, String expectedResultReason) {
        booleanSearchCandidateTest(testCaseId, description, filterValue, entities, expectedResult, expectedResultName, expectedResultReason);
    }

    @DataProvider(name = "booleanSearchCandidateSmokeTestData", parallel = true)
    public Object[][] booleanSearchCandidateSmokeTestData() {
        return limitSmokeRows(booleanSearchCandidateTestData());
    }
}
