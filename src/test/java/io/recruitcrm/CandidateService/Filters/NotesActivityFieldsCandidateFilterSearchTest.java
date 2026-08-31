package io.recruitcrm.CandidateService.Filters;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.DateUtil;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NotesActivityFieldsCandidateFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    String email;
    commanFunction function;
    String accountOwnerAPIKey;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> noteTypeMap;
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> notesTimestampScenarios;
    Map<String, Integer> candidateIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        createAssociatedEntities();
        noteTypeMap = createCustomNoteType();
        userMap = createUserMap();
        teamMap = createTeamMap();
        createTestData();
        waitForDataSync();
    }

    @DataProvider(name = "notesDateFieldFilterSearchTestData", parallel = true)
    public Object[][] notesDateFieldDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateNotesFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("date".equals(fieldType)) {
                    String filterType = test.getString("filterType");
                    String filterValue = getFilterValueAsString(test);
                    String filterValueType = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "notesDateFieldFilterSearchTestData", description = "Notes Activity Fields Candidate Filter Search Test")
    public void notesDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");

        validateDateFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
        
    }

    @DataProvider(name = "notesNoteExistsFilterSearchTestData", parallel = true)
    public Object[][] notesNoteExistsFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateNotesFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("checkbox".equals(fieldType)) {
                    String filterType = test.getString("filterType");
                    String filterValue = getFilterValueAsString(test);
                    String filterValueType = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "notesNoteExistsFilterSearchTestData", description = "Notes Note Exists (checkbox) Candidate Filter Search Test")
    public void notesNoteExistsFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logPayload(payload);

        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "notesMultiselectAndDropdownFilterSearchTest", parallel = true)
    public Object[][] notesMultiselectAndDropdownFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateNotesFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                String filterType = test.getString("filterType");
                String filterValue = getFilterValueAsString(test);
                if (fieldType.equals("multiselect") || fieldType.equals("dropdown")) {
                    String filterValueType = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "notesMultiselectAndDropdownFilterSearchTest", description = "Notes Activity Fields Candidate Filter Search Test")
    public void notesMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    } 

    public void validateMultiselectAndDropdownFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected result is empty but response has data");
            return;
        }

        if(expectedResult.equals("All")) {
            Assert.assertEquals(data.length(), 17, "Expected result is all but response has data");
            return;
        }

        String[] expectedCandidates = expectedResult.split(",");
        List<Integer> expectedCandidateIds = new ArrayList<>();
        for (String candidateKey : expectedCandidates) {
            expectedCandidateIds.add(Integer.parseInt(candidateKeyToIdMap.get(candidateKey.toLowerCase().replace(" ", ""))));
        }

        List<Integer> actualCandidateIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            actualCandidateIds.add(candidate.getInt("id"));
        }

        
        Assert.assertEquals(data.length(),expectedCandidateIds.size(), "All expected candidates are not present in the response");
        for (int candidateId : expectedCandidateIds) {
            if (!actualCandidateIds.contains(candidateId)) {
                Assert.fail("Candidate: " + candidateIdToKeyMap.get(String.valueOf(candidateId)) + " is not present in the actual response but was expected to be present");
            }
        }
    }


    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            String candidateSlug = candidate.getString("slug");
            Response noteResponse = function.getCandidateNotesBySlug(albatrossURL, albatrossAuthToken, candidateSlug);
            String responseBody = noteResponse.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray notes = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("notes");
            
            if(expectedResult.equals("Empty")) {
                Assert.assertEquals(notes.length(), 0, "Wrong candidate data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
                return;
            } else if (expectedResult.isEmpty()){
                Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
            boolean foundMatchingRecord = false;
            for (int j = 0; j < notes.length(); j++) {
                JSONObject note = notes.getJSONObject(j);
                String notesDate = note.optString(dbField, "0");

                if (validateDateAgainstFilter(notesDate, filterType, filterValue, fieldName)) {
                    foundMatchingRecord = true;
                    break; 
                }
                
            }
            Assert.assertTrue(foundMatchingRecord, 
                "No matching notes record found for candidate ID: " + candidateSlug + 
                " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
        }
    }


    



    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj = new JSONObject();

        if (filterType.equals("is_between")) {
            filterValueObj.put("type", "LONG_START_END");
            JSONObject rangeValue = new JSONObject();
            String startValue = filterValue.split(",")[0].trim();
            String endValue = filterValue.split(",")[1].trim();
            long startEpoch = dateToEpochSeconds(startValue);
            long endEpoch = dateToEpochSeconds(endValue);
            rangeValue.put("start", startEpoch);
            rangeValue.put("end", endEpoch);
            filterValueObj.put("value", rangeValue);
        } else {
            filterValueObj.put("type", filterValue_TYPE);
            if (filterType.equals("is_mt") || filterType.equals("is_lt")) {
                filterValueObj.put("value", Integer.parseInt(filterValue));
            } else if (filterType.equals("has_any_value") || filterType.equals("is_empty")) {
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if (filterType.equals("is_equal_to") || filterType.equals("is_before") || filterType.equals("is_after")) {
                long epochValue = dateToEpochSeconds(filterValue);
                filterValueObj.put("value", epochValue);
            } else {
                filterValueObj.put("value", filterValue);
            }
        }

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "note");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "note");
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

    public JSONObject createMultiselectAndDropdownFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("offLimitBehavior", "bypass");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();

        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");

        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "note");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "note");
        filter.put("fieldType", fieldType);

        String processedFilterValue = processFilterValue(filterValue, filterValue_TYPE);
        JSONObject filterValueObj;
        if (filterValue_TYPE.equals("INTEGER_LIST")) {
            filterValueObj = integerListFilterValue(processedFilterValue);
        } else if ("INTEGER".equals(filterValue_TYPE)) {
            filterValueObj = integerFilterValue(processedFilterValue);
        } else {
            filterValueObj = entityAssociationFilterValue(processedFilterValue);
        }
        filter.put("filterValue", filterValueObj);

        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);

        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");

        payload.put("filterSearchList", filterSearchList);

        return payload;
    }

    /**
     * Helper method to extract filterValue as String from test JSONObject.
     * Handles both String and JSONObject/JSONArray types.
     */
    private String getFilterValueAsString(JSONObject test) {
        Object filterValueObj = test.get("filterValue");
        if (filterValueObj instanceof JSONObject) {
            return filterValueObj.toString();
        } else if (filterValueObj instanceof JSONArray) {
            return filterValueObj.toString();
        } else {
            return String.valueOf(filterValueObj);
        }
    }

    private String processFilterValue(String filterValue, String filterValueType) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String processedValue = filterValue;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(processedValue);

        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String fieldKey = matcher.group(1);

            String actualValue = null;
            if (fieldKey.startsWith("associated_")) {
                actualValue = associatedEntitiesIdMap.get(fieldKey).toString();
            } else if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            } else if (fieldKey.startsWith("candidate")) {
                actualValue = candidateKeyToIdMap.get(fieldKey);
            } else if (fieldKey.equals("Call") || fieldKey.equals("To Do") || fieldKey.equals("Custom Note Type")) {
                actualValue = noteTypeMap.get(fieldKey);
            } else {
                actualValue = userMap.get(fieldKey);
            }
            if (actualValue != null) {
                processedValue = processedValue.replace(placeholder, actualValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder);
            }
        }

        if ("INTEGER_LIST".equals(filterValueType) && processedValue.trim().startsWith("[")) {
            try {
                JSONArray jsonArray = new JSONArray(processedValue);
                List<String> values = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    values.add(String.valueOf(jsonArray.get(i)));
                }
                return String.join(",", values);
            } catch (Exception e) {
                // If parsing fails, return processed value as-is
            }
        }

        return processedValue;
    }

    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        return jsonObject.getJSONArray("data");
    }


    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> candidateIdMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> candidateSlugMap = new ConcurrentHashMap<>();
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = candidateEntry.getJSONObject("payload");
                        Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        int candidateId = response.jsonPath().getInt("data.candidate.id");
                        candidateIdMap.put(candidateKey, candidateId);
                        candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey);
                        candidateSlugMap.put(candidateKey, response.jsonPath().getString("data.candidate.slug"));
                        candidateKeyToIdMap.put(candidateKey, String.valueOf(candidateId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();


            List<Integer> noteIds = createNotesForCandidate(candidateSlugMap);
            updateNotesWithTimestampScenarios(noteIds);

        } finally {
            executor.shutdown();
        }
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
            System.out.println("associatedEntitiesIdMap: "+associatedEntitiesIdMap);

            candidateKeyToIdMap.put("associated_candidates_candidate1", String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"))));
            candidateKeyToIdMap.put("associated_candidates_candidate2", String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"))));
            candidateIdToKeyMap.put(String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"))), "associated_candidates_candidate1");
            candidateIdToKeyMap.put(String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"))), "associated_candidates_candidate2");
        } finally {
            executor.shutdown();
        }
    }

    private List<Integer> createNotesForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateDataJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        ConcurrentMap<String, Integer> noteIdMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String candidateKey : candidateSlugMap.keySet()) {
                if (!candidateDataJson.has(candidateKey)) {
                    continue;
                }
                JSONObject candidateEntry = candidateDataJson.getJSONObject(candidateKey);
                if (!candidateEntry.has("notes")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String candidateSlug = candidateSlugMap.get(candidateKey);
                    JSONArray notesArray = candidateEntry.getJSONArray("notes");
                    JSONObject payload = new JSONObject();
                    payload.put("notes", new JSONArray(notesArray.toString()));
                    JSONObject processed = processPayload(payload, candidateSlug);
                    Response response = function.createNotesByPayload(baseURL, accountOwnerAPIKey, processed.getJSONArray("notes").getJSONObject(0));
                    noteIdMap.put(candidateKey, response.jsonPath().getInt("id"));
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(noteIdMap.values());
    }

    private JSONObject processPayload(JSONObject payload, String candidateSlug) {
        if (payload.has("notes") && payload.get("notes") instanceof JSONArray) {
            JSONArray notesArray = payload.getJSONArray("notes");
            
            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject note = notesArray.getJSONObject(i);
                if (note.has("related_to") && note.getString("related_to").startsWith("{candidate")) {
                    note.put("related_to", candidateSlug);
                }
                
                String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
                for (String field : associatedFields) {
                    processAssociatedEntityField(note, field, associatedEntitiesSlugMap);
                }
                
                
                if (note.has("note_type_id") && noteTypeMap != null) {
                    String noteTypeLabel = note.getString("note_type_id").replace("{", "").replace("}", "");
                    String noteTypeId = noteTypeMap.get(noteTypeLabel);
                    if (noteTypeId != null) {
                        note.put("note_type_id", noteTypeId);
                    }
                }

                if (note.has("created_by") && userMap != null) {
                    String createdBy = note.getString("created_by").replace("{", "").replace("}", "");
                    String createdById = userMap.get(createdBy);
                    if (createdById != null) {
                        note.put("created_by", createdById);
                    }
                }

                if (note.has("updated_by") && userMap != null) {
                    String updatedBy = note.getString("updated_by").replace("{", "").replace("}", "");
                    String updatedById = userMap.get(updatedBy);
                    if (updatedById != null) {
                        note.put("updated_by", updatedById);
                    }
                }

                processCollaboratorField(note, "collaborator_team_ids", teamMap);
                processCollaboratorField(note, "collaborator_user_ids", userMap);
            }
        }
        return payload;
    }

    public void updateNotesWithTimestampScenarios(List<Integer> noteIds) {
        notesTimestampScenarios = createNotesTimestampScenarios();

        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(notesTimestampScenarios.entrySet());
        int limit = Math.min(noteIds.size(), scenarios.size());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < limit; i++) {
                final int idx = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, String> timestamps = scenarios.get(idx).getValue();
                    Integer noteId = noteIds.get(idx);

                    JSONObject fieldsAndTimestamps = new JSONObject();
                    for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                        fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
                    }

                    Response updateResponse = ReaperIntegration.updateActivityTimestamp(noteId, fieldsAndTimestamps,"note");
                    if (updateResponse.getStatusCode() != 200) {
                        Assert.fail("Failed to update the notes timestamps");
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private Map<String, Map<String, String>> createNotesTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdOn", todayEpoch);
        todayTimestamps.put("updatedOn", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdOn", yesterdayEpoch);
        yesterdayTimestamps.put("updatedOn", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdOn", thisWeekEpoch);
        thisWeekTimestamps.put("updatedOn", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdOn", lastWeekEpoch);
        lastWeekTimestamps.put("updatedOn", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdOn", thisMonthEpoch);
        thisMonthTimestamps.put("updatedOn", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdOn", lastMonthEpoch);
        lastMonthTimestamps.put("updatedOn", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdOn", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedOn", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdOn", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedOn", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdOn", thisYearEpoch);
        thisYearTimestamps.put("updatedOn", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("createdOn", lastYearEpoch);
        lastYearTimestamps.put("updatedOn", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);

        // Static date scenario 1 - Historical data (June 15, 2022)
        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("createdOn", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("updatedOn", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        // Static date scenario 2 - Previous year data (March 10, 2023)
        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdOn", "1678406400");  // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("updatedOn", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        return scenarios;
    }

    public Map<String,String> createCustomNoteType() {
        Map<String,String> noteTypeMap = new HashMap<>();
        function.createCustomNoteType(albatrossURL, albatrossAuthToken, "Custom Note Type", false);
        Response response = function.getNoteTypes(albatrossURL, albatrossAuthToken);
        for (int i = 0; i < response.jsonPath().getList("data").size(); i++) {
            String label = response.jsonPath().getString("data[" + i + "].label");
            String id = response.jsonPath().getString("data[" + i + "].id");
            noteTypeMap.put(label, id);
        }
        return noteTypeMap;
    }

    public Map<String,String> createUserMap() {
        Map<String,String> userMap = new HashMap<>();
        Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
        userMap.put("owner", user.get("[0].id").toString());
        userMap.put("admin", user.get("[1].id").toString());
        userMap.put("restricted", user.get("[2].id").toString());
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
        Response team = function.getTeams(baseURL, accountOwnerAPIKey);
        String teamId = team.jsonPath().getString("[0].team_id");
        teamMap.put("team", teamId);
        return teamMap;
    }


            
}
