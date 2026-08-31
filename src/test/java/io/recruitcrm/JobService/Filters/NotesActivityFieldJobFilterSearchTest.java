package io.recruitcrm.JobService.Filters;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.restclient.RestClient;
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
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.CreateHiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.HiringStages;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NotesActivityFieldJobFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    String ownerAlbatrossAuthToken;
    String adminAlbatrossAuthToken;
    String teamMemberAlbatrossAuthToken;
    String restrictedTeamMemberAlbatrossAuthToken;
    commanFunction function;
    String accountOwnerAPIKey;
    String email;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> noteTypeMap;
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> jobKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> jobIdToKeyMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> notesTimestampScenarios;
    Map<String, Integer> jobIdMap = new HashMap<>();
    Map<String, String> companyKeyToSlugMap = new HashMap<>();
    Map<String, String> companyKeyToIdMap = new HashMap<>();
    Map<String, String> contactKeyToSlugMap = new HashMap<>();
    Map<String, String> contactKeyToIdMap = new HashMap<>();
    Map<String, Integer> jobStatusIdMap = new HashMap<>();
    Map<String, Integer> qualificationIdMap = new HashMap<>();
    Map<String, Integer> hiringPipelineIdMap = new HashMap<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        ownerAlbatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        adminAlbatrossAuthToken = ThreadManager.getAlbatrossToken("Admin");
        teamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("TeamMember");
        restrictedTeamMemberAlbatrossAuthToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
        albatrossAuthToken = ownerAlbatrossAuthToken;
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        createCompanies();
        createAssociatedEntities();
        noteTypeMap = createCustomNoteType();
        userMap = createUserMap();
        teamMap = createTeamMap();
        jobStatusIdMap = createJobStatusMap();
        qualificationIdMap = createQualificationMap();
        hiringPipelineIdMap = createHiringPipelineMap();
        createTestData();
        waitForDataSync();
    }

    @DataProvider(name = "notesDateFieldFilterSearchTestData", parallel = true)
    public Object[][] notesDateFieldDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobNotesFilterDataProvider.json");
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
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"),fieldType, filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "notesDateFieldFilterSearchTestData", description = "Notes Activity Fields Job Filter Search Test")
    public void notesDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ",email);
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

        validateDateFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
        
    }

    @DataProvider(name = "notesNoteExistsFilterSearchTestData", parallel = true)
    public Object[][] notesNoteExistsFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobNotesFilterDataProvider.json");
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

    @Owner("Sai Teja SG")
    @Test(groups = {"aries_service"}, dataProvider = "notesNoteExistsFilterSearchTestData", description = "Notes Note Exists (checkbox) Job Filter Search Test")
    public void notesNoteExistsFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        String basePath = "/jobs/search/get?page=1&size=100";
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logPayload(payload);

        Response response = RestClient.doPost("JSON", jobServiceURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "notesMultiselectAndDropdownFilterSearchTest", parallel = true)
    public Object[][] notesMultiselectAndDropdownFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobNotesFilterDataProvider.json");
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
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"),fieldType, filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "notesMultiselectAndDropdownFilterSearchTest", description = "Notes Activity Fields Job Filter Search Test")
    public void notesMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ",email);
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        System.out.println("Payload: "+payload);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

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

        String[] expectedJobs = expectedResult.split(",");
        List<Integer> expectedJobIds = new ArrayList<>();
        for (String jobKey : expectedJobs) {
            String normalizedKey = jobKey.trim().toLowerCase().replace(" ", "");
            String jobIdStr = jobKeyToIdMap.get(normalizedKey);
            if (jobIdStr == null) {
                Assert.fail("Expected job key '" + jobKey + "' (normalized: '" + normalizedKey + "') not found in jobKeyToIdMap. Available keys: " + jobKeyToIdMap.keySet());
            }
            expectedJobIds.add(Integer.parseInt(jobIdStr));
        }

        List<Integer> actualJobIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject job = data.getJSONObject(i);
            actualJobIds.add(job.getInt("id"));
        }

        
        Assert.assertEquals(data.length(),expectedJobIds.size(), "All expected jobs are not present in the response");
        for (int jobId : expectedJobIds) {
            if (!actualJobIds.contains(jobId)) {
                Assert.fail("Job: " + jobIdToKeyMap.get(String.valueOf(jobId)) + " is not present in the actual response but was expected to be present");
            }
        }
    }


    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject job = data.getJSONObject(i);
            String jobSlug = job.getString("slug");
            Response noteResponse = getJobNotesBySlug(albatrossURL, albatrossAuthToken, jobSlug);
            String responseBody = noteResponse.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray notes = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("notes");
            
            if(expectedResult.equals("Empty")) {
                Assert.assertEquals(notes.length(), 0, "Wrong job data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
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
                "No matching notes record found for job ID: " + jobSlug + 
                " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
        }
    }

    public Response getJobNotesBySlug(String albatrossURL, String authToken, String jobSlug) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + authToken);
        String basePath = "expand-activity/get-activity-data";

        JSONObject payload = new JSONObject();
        payload.put("type", "0");
        payload.put("page", "detailspage");
        payload.put("relatedToSlug", jobSlug);
        payload.put("relatedtotypeid", 4); // Job type ID is 4

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch job notes data");
        return response;
    }


    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Create filterValue object with type and value
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
        
        // Create filterSearchList structure
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
        payload.put("advancedSearchContext", "JOB");
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
        filter.put("entityType", "NOTE");
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
        System.out.println("Processed filter value: "+filterValueObj);
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
     * Helper method to extract filterValue as String from test JSONObject
     * Handles both String and JSONObject types
     */
    private String getFilterValueAsString(JSONObject test) {
        Object filterValueObj = test.get("filterValue");
        if (filterValueObj instanceof JSONObject) {
            // Convert JSONObject to JSON string
            return filterValueObj.toString();
        } else if (filterValueObj instanceof JSONArray) {
            // Convert JSONArray to JSON string
            return filterValueObj.toString();
        } else {
            // It's already a string or other primitive type
            return String.valueOf(filterValueObj);
        }
    }

    private String processFilterValue(String filterValue, String filterValueType) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }
        
        String processedValue = filterValue;

        // Process placeholders first
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(processedValue);
        
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String fieldKey = matcher.group(1);

            String actualValue = null;
            if (fieldKey.startsWith("associated_")) {
                actualValue = associatedEntitiesIdMap.get(fieldKey).toString();
            }
            else if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            }
            else if (fieldKey.startsWith("company") && !fieldKey.startsWith("associated_")) {
                actualValue = companyKeyToIdMap.get(fieldKey.toLowerCase()).toString();
            }
            else if (fieldKey.startsWith("contact") && !fieldKey.startsWith("associated_")) {
                actualValue = contactKeyToIdMap.get(fieldKey.toLowerCase()).toString();
            }
            else if (fieldKey.startsWith("job") && !fieldKey.startsWith("associated_")) {
                actualValue = jobKeyToIdMap.get(fieldKey.toLowerCase()).toString();
            }
            else if (fieldKey.equals("Call") || fieldKey.equals("To Do") || fieldKey.equals("Custom Note Type")) {
                actualValue = noteTypeMap.get(fieldKey);
            }
            else {
                actualValue = userMap.get(fieldKey);
            }
            if (actualValue != null) {
                processedValue = processedValue.replace(placeholder, actualValue);
            } else {
                throw new IllegalArgumentException("Unable to process the payload, No value found for placeholder: " + placeholder);
            }
        }

        // For INTEGER_LIST, convert JSON array string to comma-separated string after processing placeholders
        if ("INTEGER_LIST".equals(filterValueType) && processedValue.trim().startsWith("[")) {
            try {
                JSONArray jsonArray = new JSONArray(processedValue);
                List<String> values = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    values.add(jsonArray.getString(i));
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

    public Map<String, Integer> createJobStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        try {
            statusMap = function.getJobStatusValues(albatrossURL, albatrossAuthToken);
        } catch (Exception e) {
        }
        return statusMap;
    }

    public Map<String, Integer> createQualificationMap() {
        Map<String, Integer> qualificationMap = new HashMap<>();
        try {
            Map<String, String> authTokenMap = new HashMap<>();
            authTokenMap.put("Authorization", "Bearer " + albatrossAuthToken);
            Response response = RestClient.doPost("JSON", albatrossURL, "qualifications", authTokenMap, null, true, null);
            if (response.getStatusCode() == 200) {
                JSONObject responseJson = new JSONObject(response.getBody().asString());
                JSONArray dataArray = responseJson.getJSONArray("data");
                
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject qualificationObj = dataArray.getJSONObject(i);
                    String qualificationLabel = qualificationObj.getString("label");
                    Integer qualificationId = qualificationObj.getInt("id");
                    qualificationMap.put(qualificationLabel, qualificationId);
                }
            }
        } catch (Exception e) {
        }
        return qualificationMap;
    }

    public Map<String, Integer> createHiringPipelineMap() {
        Map<String, Integer> pipelineMap = new HashMap<>();
        ListFunctions listFunctions = new ListFunctions();
        HiringPipeline hiringFaker = new HiringPipeline();
        
        try {
            JsonPath jsonGetAllCandidateHiringStages = listFunctions
                    .getAllCandidateHiringStages(baseURL, accountOwnerAPIKey).jsonPath();
            ArrayList<Integer> hiringStagesID = jsonGetAllCandidateHiringStages.get("status_id");
            
            if (hiringStagesID == null || hiringStagesID.isEmpty()) {
                return pipelineMap;
            }
            
            for (int pipelineNum = 1; pipelineNum <= 3; pipelineNum++) {
                ArrayList<Object> hiringStagesList = new ArrayList<Object>();
                
                HiringStages stage1 = new HiringStages();
                stage1.setId(10);
                stage1.setSequenceno(0);
                hiringStagesList.add(stage1);
                
                HiringStages stage2 = new HiringStages();
                stage2.setId(1);
                stage2.setSequenceno(1);
                hiringStagesList.add(stage2);
                
                HiringStages stage3 = new HiringStages();
                stage3.setId(8);
                stage3.setSequenceno(55);
                hiringStagesList.add(stage3);
                
                CreateHiringPipeline createHiringPipeline = new CreateHiringPipeline();
                createHiringPipeline.setName(hiringFaker.getHiringPipelineName() + "_Pipeline" + pipelineNum);
                createHiringPipeline.setIs_primary("0");
                createHiringPipeline.setHiring_stages(hiringStagesList);

                Response response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add",
                        albatrossAuthToken, null, true, createHiringPipeline);

                if (response.getStatusCode() == 200) {
                    JsonPath jsonPath = response.jsonPath();
                    Integer pipelineId = jsonPath.getInt("id");
                    String pipelineKey = "hiring_pipeline_" + pipelineNum;
                    pipelineMap.put(pipelineKey, pipelineId);
                }
            }
        } catch (Exception e) {
        }
        
        return pipelineMap;
    }

    /**
     * Replaces all placeholders in job payload with actual values
     */
    private void replaceJobPlaceholders(JSONObject job, JSONObject payload) {
        if (job.has("companyid")) {
            String companyIdPlaceholder = job.getString("companyid");
            if (companyIdPlaceholder != null && companyIdPlaceholder.startsWith("{") && companyIdPlaceholder.endsWith("_id}")) {
                String companyKey = companyIdPlaceholder.substring(1, companyIdPlaceholder.length() - 4);
                String actualCompanyId = companyKeyToIdMap.get(companyKey);
                if (actualCompanyId != null) {
                    job.put("companyid", actualCompanyId);
                }
            }
        }
        
        if (job.has("contactid")) {
            String contactIdPlaceholder = job.getString("contactid");
            if (contactIdPlaceholder != null && contactIdPlaceholder.startsWith("{") && contactIdPlaceholder.endsWith("_id}")) {
                String contactKey = contactIdPlaceholder.substring(1, contactIdPlaceholder.length() - 4);
                String actualContactId = contactKeyToIdMap.get(contactKey);
                if (actualContactId != null) {
                    job.put("contactid", actualContactId);
                }
            }
        }
        
        if (job.has("ownerid")) {
            String ownerPlaceholder = job.getString("ownerid");
            if (ownerPlaceholder != null && ownerPlaceholder.startsWith("{") && ownerPlaceholder.endsWith("}")) {
                String ownerKey = ownerPlaceholder.substring(1, ownerPlaceholder.length() - 1);
                String actualOwnerId = userMap.get(ownerKey);
                if (actualOwnerId != null) {
                    job.put("ownerid", Integer.parseInt(actualOwnerId));
                }
            }
        }
        
        if (job.has("qualificationid")) {
            String qualificationPlaceholder = job.getString("qualificationid");
            if (qualificationPlaceholder != null && qualificationPlaceholder.startsWith("{") && qualificationPlaceholder.endsWith("}")) {
                String qualificationLabel = qualificationPlaceholder.substring(1, qualificationPlaceholder.length() - 1);
                Integer qualificationId = qualificationIdMap.get(qualificationLabel);
                if (qualificationId == null) {
                    for (Map.Entry<String, Integer> entry : qualificationIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(qualificationLabel)) {
                            qualificationId = entry.getValue();
                            break;
                        }
                    }
                }
                if (qualificationId != null) {
                    job.put("qualificationid", qualificationId);
                }
            }
        }
        
        if (job.has("jobstatus")) {
            String jobStatusPlaceholder = job.getString("jobstatus");
            if (jobStatusPlaceholder != null && jobStatusPlaceholder.startsWith("{") && jobStatusPlaceholder.endsWith("}")) {
                String jobStatusLabel = jobStatusPlaceholder.substring(1, jobStatusPlaceholder.length() - 1);
                Integer jobStatusId = jobStatusIdMap.get(jobStatusLabel);
                if (jobStatusId == null) {
                    for (Map.Entry<String, Integer> entry : jobStatusIdMap.entrySet()) {
                        if (entry.getKey().equalsIgnoreCase(jobStatusLabel)) {
                            jobStatusId = entry.getValue();
                            break;
                        }
                    }
                }
                if (jobStatusId != null) {
                    job.put("jobstatus", jobStatusId);
                }
            }
        }
        
        if (job.has("hiring_pipeline_id")) {
            Object hiringPipelineValue = job.get("hiring_pipeline_id");
            String hiringPipelinePlaceholder = null;
            
            if (hiringPipelineValue instanceof String) {
                hiringPipelinePlaceholder = (String) hiringPipelineValue;
            } else if (hiringPipelineValue != null) {
                String valueAsString = String.valueOf(hiringPipelineValue);
                if (valueAsString.startsWith("{")) {
                    hiringPipelinePlaceholder = valueAsString;
                }
            }
            
            if (hiringPipelinePlaceholder != null && hiringPipelinePlaceholder.startsWith("{") && hiringPipelinePlaceholder.endsWith("}")) {
                if (hiringPipelinePlaceholder.equals("{default_hiring_pipeline_id}")) {
                    job.put("hiring_pipeline_id", 0);
                } else {
                    String pipelineKey = hiringPipelinePlaceholder.substring(1, hiringPipelinePlaceholder.length() - 1);
                    if (pipelineKey.endsWith("_id")) {
                        pipelineKey = pipelineKey.substring(0, pipelineKey.length() - 3);
                    }
                    
                    Integer actualPipelineId = hiringPipelineIdMap.get(pipelineKey);
                    if (actualPipelineId != null) {
                        job.put("hiring_pipeline_id", actualPipelineId);
                    }
                }
            }
        }
        
        if (payload.has("collaborator")) {
            JSONObject collaborator = payload.getJSONObject("collaborator");
            
            if (collaborator.has("user_ids")) {
                JSONArray userIds = collaborator.getJSONArray("user_ids");
                JSONArray actualUserIds = new JSONArray();
                for (int i = 0; i < userIds.length(); i++) {
                    String userIdPlaceholder = userIds.getString(i);
                    if (userIdPlaceholder != null && userIdPlaceholder.startsWith("{") && userIdPlaceholder.endsWith("}")) {
                        String userIdKey = userIdPlaceholder.substring(1, userIdPlaceholder.length() - 1);
                        String actualUserId = userMap.get(userIdKey);
                        if (actualUserId != null) {
                            actualUserIds.put(Integer.parseInt(actualUserId));
                        }
                    } else {
                        actualUserIds.put(userIdPlaceholder);
                    }
                }
                collaborator.put("user_ids", actualUserIds);
            }
            
            if (collaborator.has("team_ids")) {
                JSONArray teamIds = collaborator.getJSONArray("team_ids");
                JSONArray actualTeamIds = new JSONArray();
                for (int i = 0; i < teamIds.length(); i++) {
                    String teamIdPlaceholder = teamIds.getString(i);
                    if (teamIdPlaceholder != null && teamIdPlaceholder.startsWith("{") && teamIdPlaceholder.endsWith("}")) {
                        String teamKey = teamIdPlaceholder.substring(1, teamIdPlaceholder.length() - 1);
                        String actualTeamId = teamMap.get(teamKey);
                        if (actualTeamId != null) {
                            actualTeamIds.put(Integer.parseInt(actualTeamId));
                        }
                    } else {
                        actualTeamIds.put(teamIdPlaceholder);
                    }
                }
                collaborator.put("team_ids", actualTeamIds);
            }
        }
        
        if (payload.has("targetcompanies")) {
            JSONArray targetCompanies = payload.getJSONArray("targetcompanies");
            for (int i = 0; i < targetCompanies.length(); i++) {
                JSONObject targetCompany = targetCompanies.getJSONObject(i);
                
                if (targetCompany.has("slug")) {
                    String slugPlaceholder = targetCompany.getString("slug");
                    if (slugPlaceholder != null && slugPlaceholder.startsWith("{") && slugPlaceholder.endsWith("_slug}")) {
                        String companyKey = slugPlaceholder.substring(1, slugPlaceholder.length() - 6);
                        String actualSlug = companyKeyToSlugMap.get(companyKey);
                        if (actualSlug != null) {
                            targetCompany.put("slug", actualSlug);
                        }
                    }
                }
                
                if (targetCompany.has("id")) {
                    String idPlaceholder = targetCompany.getString("id");
                    if (idPlaceholder != null && idPlaceholder.startsWith("{") && idPlaceholder.endsWith("_id}")) {
                        String companyKey = idPlaceholder.substring(1, idPlaceholder.length() - 4);
                        String actualId = companyKeyToIdMap.get(companyKey);
                        if (actualId != null) {
                            targetCompany.put("id", actualId);
                        }
                    }
                }
                
                if (targetCompany.has("owner")) {
                    String ownerPlaceholder = targetCompany.getString("owner");
                    if (ownerPlaceholder != null && ownerPlaceholder.startsWith("{") && ownerPlaceholder.endsWith("}")) {
                        String ownerKey = ownerPlaceholder.substring(1, ownerPlaceholder.length() - 1);
                        String actualOwnerId = userMap.get(ownerKey);
                        if (actualOwnerId != null) {
                            targetCompany.put("owner", Integer.parseInt(actualOwnerId));
                        }
                    }
                }
            }
        }
    }

    public void createTestData() {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> jobIdMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> jobSlugMap = new ConcurrentHashMap<>();
        try {
            CompletableFuture.allOf(jobJson.keySet().stream()
                    .filter(key -> key.startsWith("job"))
                    .map(jobKey -> CompletableFuture.runAsync(() -> {
                        JSONObject jobEntry = jobJson.getJSONObject(jobKey);
                        JSONObject payload = jobEntry.getJSONObject("payload");
                        String createdBy = jobEntry.has("createdBy") ? jobEntry.getString("createdBy") : "admin";
                        String authToken = getAlbatrossAuthToken(createdBy);

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        JSONObject job = payload.getJSONObject("job");
                        replaceJobPlaceholders(job, payload);

                        Response response = RestClient.doPost("JSON", albatrossURL, "/jobs", authToken, null, true, payload);
                        response.then().statusCode(200);
                        int jobId = response.jsonPath().getInt("data.job.id");
                        jobIdMap.put(jobKey, jobId);
                        jobIdToKeyMap.put(String.valueOf(jobId), jobKey);
                        jobSlugMap.put(jobKey, response.jsonPath().getString("data.job.slug"));
                        jobKeyToIdMap.put(jobKey, String.valueOf(jobId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();


            List<Integer> noteIds = createNotesForJob(jobSlugMap);
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

            // Store contact info for job creation
            contactKeyToSlugMap.put("contact1", contactJson1.getString("slug"));
            contactKeyToSlugMap.put("contact2", contactJson2.getString("slug"));
            contactKeyToIdMap.put("contact1", String.valueOf(function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson1.getString("slug"))));
            contactKeyToIdMap.put("contact2", String.valueOf(function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson2.getString("slug"))));

            jobKeyToIdMap.put("associated_candidates_candidate1", String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"))));
            jobKeyToIdMap.put("associated_candidates_candidate2", String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"))));
            jobKeyToIdMap.put("associated_companies_company1", String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"))));
            jobKeyToIdMap.put("associated_companies_company2", String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"))));
            jobKeyToIdMap.put("associated_contacts_contact1", String.valueOf(function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson1.getString("slug"))));
            jobKeyToIdMap.put("associated_contacts_contact2", String.valueOf(function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson2.getString("slug"))));
            jobKeyToIdMap.put("associated_jobs_job1", String.valueOf(function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson1.getString("slug"))));
            jobKeyToIdMap.put("associated_jobs_job2", String.valueOf(function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson2.getString("slug"))));
            jobIdToKeyMap.put(String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"))), "associated_candidates_candidate1");
            jobIdToKeyMap.put(String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"))), "associated_candidates_candidate2");
            jobIdToKeyMap.put(String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"))), "associated_companies_company1");
            jobIdToKeyMap.put(String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"))), "associated_companies_company2");
            jobIdToKeyMap.put(String.valueOf(function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson1.getString("slug"))), "associated_contacts_contact1");
            jobIdToKeyMap.put(String.valueOf(function.getContactIdBySlug(albatrossURL, albatrossAuthToken, contactJson2.getString("slug"))), "associated_contacts_contact2");
            jobIdToKeyMap.put(String.valueOf(function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson1.getString("slug"))), "associated_jobs_job1");
            jobIdToKeyMap.put(String.valueOf(function.getJobIdBySlug(albatrossURL, albatrossAuthToken, jobJson2.getString("slug"))), "associated_jobs_job2");
        } finally {
            executor.shutdown();
        }
    }

    private List<Integer> createNotesForJob(Map<String, String> jobSlugMap) {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");
        ConcurrentMap<String, Integer> noteIdMap = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String jobKey : jobSlugMap.keySet()) {
                if (!jobJson.has(jobKey) || !jobJson.getJSONObject(jobKey).has("notes")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String jobSlug = jobSlugMap.get(jobKey);
                    JSONObject jobEntry = jobJson.getJSONObject(jobKey);
                    JSONObject notesData = jobEntry.getJSONObject("notes");
                    JSONObject processed = processPayload(notesData, jobSlug);
                    Response response = function.createNotesByPayload(baseURL, accountOwnerAPIKey, processed);
                    noteIdMap.put(jobKey, response.jsonPath().getInt("id"));
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(noteIdMap.values());
    }

    private JSONObject processPayload(JSONObject notesData, String jobSlug) {
        if (notesData.has("note_type_id")) {
            String noteTypeLabel = notesData.getString("note_type_id").replace("{", "").replace("}", "");
            String noteTypeId = noteTypeMap.get(noteTypeLabel);
            if (noteTypeId != null) {
                notesData.put("note_type_id", noteTypeId);
            }
        }

        notesData.put("related_to", jobSlug);
        notesData.put("related_to_type", "job");

        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            if (!notesData.has(field)) {
                notesData.put(field, "");
            }
        }

        for (String field : associatedFields) {
            if (notesData.has(field) && !notesData.getString(field).isEmpty()) {
                processAssociatedEntityField(notesData, field, associatedEntitiesSlugMap);
            }
        }

        if (notesData.has("created_by") && userMap != null) {
            String createdBy = notesData.getString("created_by").replace("{", "").replace("}", "");
            String createdById = userMap.get(createdBy);
            if (createdById != null) {
                notesData.put("created_by", createdById);
            }
        }

        if (notesData.has("updated_by") && userMap != null) {
            String updatedBy = notesData.getString("updated_by").replace("{", "").replace("}", "");
            String updatedById = userMap.get(updatedBy);
            if (updatedById != null) {
                notesData.put("updated_by", updatedById);
            }
        }

        processCollaboratorField(notesData, "collaborator_team_ids", teamMap);
        processCollaboratorField(notesData, "collaborator_user_ids", userMap);
        
        return notesData;
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
