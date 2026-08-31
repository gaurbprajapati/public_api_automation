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
import io.rcrm.api.pojo.Task;
import io.rcrm.api.javafaker.JavaFakerTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TaskActivityFieldsJobFilterSearchTest extends FilterSearchBaseTest {
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
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> jobKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> jobIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> jobSlugMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> tasksTimestampScenarios;
    JavaFakerTask fakerTask;
    int defaultTaskTypeId = 0;
    int followUpTaskTypeId = 0;
    int interviewSchedulingTaskTypeId = 0;
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
        email = ThreadManager.getAccount().getOwner().getEmail();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        fakerTask = new JavaFakerTask();
        createCompanies();
        createContacts();
        createAssociatedEntities();
        setupTaskTypes();
        userMap = createUserMap();
        teamMap = createTeamMap();
        jobStatusIdMap = createJobStatusMap();
        qualificationIdMap = createQualificationMap();
        hiringPipelineIdMap = createHiringPipelineMap();
        createTestData();
        waitForDataSync();
    }

    @DataProvider(name = "tasksDateFieldFilterSearchTestData", parallel = true)
    public Object[][] tasksDateFieldDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobTaskFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("date".equals(fieldType)) {
                    String filterType = test.getString("filterType");
                    String filterValue = getFilterValueAsString(test);
                    String filterValue_TYPE = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValue_TYPE, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "tasksDateFieldFilterSearchTestData", description = "Tasks Activity Fields Job Filter Search Test")
    public void tasksDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ",email);
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

        validateDateFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "tasksTaskExistsFilterSearchTestData", parallel = true)
    public Object[][] tasksTaskExistsFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobTaskFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("checkbox".equals(fieldType)) {
                    String filterType = test.getString("filterType");
                    String filterValue = getFilterValueAsString(test);
                    String filterValue_TYPE = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValue_TYPE, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    @Owner("Sai Teja SG")
    @Test(groups = {"aries_service"}, dataProvider = "tasksTaskExistsFilterSearchTestData", description = "Tasks Task Exists (checkbox) Job Filter Search Test")
    public void tasksTaskExistsFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        String basePath = "/jobs/search/get?page=1&size=100";
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);

        Response response = RestClient.doPost("JSON", jobServiceURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "tasksMultiselectAndDropdownFilterSearchTest", parallel = true)
    public Object[][] tasksMultiselectAndDropdownFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobTaskFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                String filterType = test.getString("filterType");
                String filterValue = getFilterValueAsString(test);
                if (fieldType.equals("multiselect") || fieldType.equals("dropdown")) {
                    String filterValue_TYPE = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValue_TYPE, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "tasksMultiselectAndDropdownFilterSearchTest", description = "Tasks Activity Fields Job Filter Search Test")
    public void tasksMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ",email);
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "tasksTextFieldFilterSearchTest", parallel = true)
    public Object[][] tasksTextFieldFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/jobTaskFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                String filterType = test.getString("filterType");
                if (fieldType.equals("text")) {
                    String filterValue = test.getString("filterValue");
                    String filterValueType = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "tasksTextFieldFilterSearchTest", description = "Tasks Text Fields Job Filter Search Test")
    public void tasksTextFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE, String testCaseId) {
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ",email);
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "jobs");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "name");

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");

        validateTextFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
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
                // Check if it's an associated entity
                if (normalizedKey.startsWith("associated_")) {
                    Integer associatedId = associatedEntitiesIdMap.get(normalizedKey);
                    if (associatedId != null) {
                        expectedJobIds.add(associatedId);
                        continue;
                    }
                }
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

    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " jobs for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (expectedResult.isEmpty() || expectedResult.trim().isEmpty()){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 jobs for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject job = data.getJSONObject(i);
            int jobId = job.getInt("id");
            String jobSlug = job.getString("slug");
            
            try {
                Response taskResponse = getJobTasksBySlug(albatrossURL, albatrossAuthToken, jobSlug);
                
                Assert.assertEquals(taskResponse.getStatusCode(), 200, 
                    "Failed to fetch task for job ID: " + jobId);
                
                String responseBody = taskResponse.getBody().asString();
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray taskContent = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("tasks");
                
                FilterSearchReporter.logResponse(taskResponse, taskContent);

                boolean foundMatchingRecord = false;
                if (taskContent.length() == 0) {
                    if (filterType.equals("does_not_contain") || filterType.equals("is_not") || filterType.equals("is_empty")) {
                        continue;
                    }
                }

                for (int j = 0; j < taskContent.length(); j++) {
                    JSONObject taskRecord = taskContent.getJSONObject(j);
                    
                    String taskFieldValue = taskRecord.optString("title", "");
                    
                    if (validateTextAgainstFilter(taskFieldValue, filterType, filterValue, fieldName)) {
                        foundMatchingRecord = true;
                        break; 
                    }
                }
                
                Assert.assertTrue(foundMatchingRecord, 
                    "No matching task record found for job ID: " + jobId +
                    " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
                    
            } catch (Exception e) {
                Assert.fail("Error validating task for job ID: " + jobId +
                    " - " + e.getMessage());
            }
        }
    }

    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " jobs for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        }
        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 jobs for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject job = data.getJSONObject(i);
            String jobSlug = job.getString("slug");
            Response taskResponse = getJobTasksBySlug(albatrossURL, albatrossAuthToken, jobSlug);
            String responseBody = taskResponse.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray tasks = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("tasks");
            
            if(expectedResult.equals("Empty")) {
                Assert.assertEquals(tasks.length(), 0, "Wrong job data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
                return;
            } else if (expectedResult.isEmpty()){
                Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            }
            boolean foundMatchingRecord = false;
            if (filterType.equals("is_empty") && tasks.length() == 0) {
                foundMatchingRecord = true;
            }
            for (int j = 0; j < tasks.length(); j++) {
                JSONObject task = tasks.getJSONObject(j);
                if (task.has("type") && task.getInt("type") == 1) {
                    // Map dbField to actual response field name
                    String actualFieldName = dbField.equals("taskdueon") ? "startdate" : dbField;
                    String taskDate = task.optString(actualFieldName, "0");

                    if (validateDateAgainstFilter(taskDate, filterType, filterValue, fieldName)) {
                        foundMatchingRecord = true;
                        break;
                    }
                }
            }
            Assert.assertTrue(foundMatchingRecord, 
                "No matching task record found for job ID: " + jobSlug + 
                " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
        }
    }

    public Response getJobTasksBySlug(String albatrossURL, String authToken, String jobSlug) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + authToken);
        String basePath = "expand-activity/get-activity-data";

        JSONObject payload = new JSONObject();
        payload.put("type", "1");
        payload.put("page", "detailspage");
        payload.put("relatedToSlug", jobSlug);
        payload.put("relatedtotypeid", 4); // Job type ID is 4

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch job tasks data");
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
        filter.put("groupType", "task");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "task");
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
        filter.put("groupType", "task");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "task");
        filter.put("fieldType", fieldType);


        String processedFilterValue = processFilterValue(filterValue, filterValue_TYPE);
        
        // Additional safety check for INTEGER_LIST: ensure brackets are removed if still present
        if (filterValue_TYPE != null && filterValue_TYPE.equals("INTEGER_LIST")) {
            String trimmed = processedFilterValue.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                // Extract content between brackets - this handles cases where JSON parsing might have failed
                processedFilterValue = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }
        
        JSONObject filterValueObj;
        if (filterValue_TYPE != null && filterValue_TYPE.equals("INTEGER_LIST")) {
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

    public JSONObject createTextFieldFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "JOB");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());
        
        // Create filterValue object with type and value
        JSONObject filterValueObj;
        if (filterValue_TYPE.equals("STRING_LIST")) {
            filterValueObj = stringListFilterValue(filterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", filterValue);
        }
        
        // Create filterSearchList structure
        JSONObject filterSearchList = new JSONObject();
        JSONArray groupFilterListArray = new JSONArray();
        
        JSONObject groupFilterList = new JSONObject();
        groupFilterList.put("groupFilterJoinOperator", "AND");
        
        JSONArray filtersArray = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("isCrossEntity", false);
        filter.put("groupType", "task");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "task");
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

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([^}]+)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(filterValue);
        
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String fieldKey = matcher.group(1);

            String actualValue = null;
            if (fieldKey.equals("task_type_follow_up")) {
                actualValue = String.valueOf(followUpTaskTypeId);
            }
            else if (fieldKey.equals("task_type_interview_scheduling")) {
                actualValue = String.valueOf(interviewSchedulingTaskTypeId);
            }
            else if (fieldKey.startsWith("associated_")) {
                actualValue = associatedEntitiesIdMap.get(fieldKey).toString();
            }
            else if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            }
            else if (fieldKey.startsWith("company") && !fieldKey.startsWith("associated_")) {
                actualValue = companyKeyToIdMap.get(fieldKey.toLowerCase());
                if (actualValue != null) {
                    actualValue = actualValue.toString();
                }
            }
            else if (fieldKey.startsWith("contact") && !fieldKey.startsWith("associated_")) {
                actualValue = contactKeyToIdMap.get(fieldKey.toLowerCase());
                if (actualValue != null) {
                    actualValue = actualValue.toString();
                }
            }
            else if (fieldKey.startsWith("job") && !fieldKey.startsWith("associated_")) {
                actualValue = jobKeyToIdMap.get(fieldKey.toLowerCase());
                if (actualValue != null) {
                    actualValue = actualValue.toString();
                }
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
        if ("INTEGER_LIST".equals(filterValueType)) {
            String trimmedValue = processedValue.trim();
            if (trimmedValue.startsWith("[") && trimmedValue.endsWith("]")) {
                try {
                    JSONArray jsonArray = new JSONArray(trimmedValue);
                    List<String> values = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Object item = jsonArray.get(i);
                        // Handle both string and number types - convert to string
                        values.add(String.valueOf(item));
                    }
                    String result = String.join(",", values);
                    return result;
                } catch (Exception e) {
                    // If JSON parsing fails, manually extract values by removing brackets
                    String withoutBrackets = trimmedValue.substring(1, trimmedValue.length() - 1).trim();
                    return withoutBrackets;
                }
            }
        }
        
        return processedValue;
    }

    public JSONArray getFilteredData(Response response) {
        String responseBody = response.getBody().asString();
        JSONObject jsonObject = new JSONObject(responseBody);
        
        if (!jsonObject.has("data")) {
            throw new RuntimeException("Response does not contain 'data' field. Full response: " + responseBody);
        }
        
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

                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        response.then().statusCode(200);
                        JsonPath jsonPath = response.jsonPath();
                        String slug = jsonPath.getString("data.company.slug");
                        String companyIdStr = jsonPath.getString("data.company.id");

                        if (companyIdStr == null) {
                            System.out.println("Skipping " + companyKey + " - company ID is null");
                            return;
                        }

                        synchronized (companyKeyToSlugMap) {
                            companyKeyToSlugMap.put(companyKey, slug);
                        }
                        synchronized (companyKeyToIdMap) {
                            companyKeyToIdMap.put(companyKey, String.valueOf(companyIdStr));
                        }
                    }, executor));
                }
            }
            CompletableFuture.allOf(createFutures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    public void createContacts() {
        JSONObject contactJson = readJsonFileFromPath("src/test/resources/testData/contact_data.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            List<CompletableFuture<Void>> createFutures = new ArrayList<>();
            for (String contactKey : contactJson.keySet()) {
                if (contactKey.startsWith("contact")) {
                    createFutures.add(CompletableFuture.runAsync(() -> {
                        JSONObject contactEntry = contactJson.getJSONObject(contactKey);
                        JSONObject payload = contactEntry.getJSONObject("payload");
                        
                        // Process selectedcompanies if present
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
                        
                        Response response = RestClient.doPost("JSON", albatrossURL, "/contacts", albatrossAuthToken, null, true, payload);
                        response.then().statusCode(200);
                        int contactId = response.jsonPath().getInt("data.contact.id");
                        String contactSlug = response.jsonPath().getString("data.contact.slug");
                        
                        synchronized (contactKeyToSlugMap) {
                            contactKeyToSlugMap.put(contactKey, contactSlug);
                        }
                        synchronized (contactKeyToIdMap) {
                            contactKeyToIdMap.put(contactKey, String.valueOf(contactId));
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
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
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
                        jobIdToKeyMap.put(String.valueOf(jobId), jobKey);
                        jobSlugMap.put(jobKey, response.jsonPath().getString("data.job.slug"));
                        jobKeyToIdMap.put(jobKey, String.valueOf(jobId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> taskIds = createTasksForJob(jobSlugMap);
            updateTasksToCompleteStatus();
            updateTasksWithTimestampScenarios(taskIds);

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

    private List<Integer> createTasksForJob(Map<String, String> jobSlugMap) {
        JSONObject jobJson = readJsonFileFromPath("src/test/resources/testData/job_data.json");
        LinkedHashMap<String, Integer> taskIdMap = new LinkedHashMap<>();

        List<String> sortedJobKeys = jobSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String jobKey : sortedJobKeys) {
                if (!jobJson.has(jobKey) || !jobJson.getJSONObject(jobKey).has("tasks")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String jobSlug = jobSlugMap.get(jobKey);
                    JSONObject jobEntry = jobJson.getJSONObject(jobKey);
                    JSONObject tasksData = jobEntry.getJSONObject("tasks");
                    Task task = processTaskPayload(tasksData, jobSlug);
                    Response response = RestClient.doPost("JSON", baseURL, "tasks", accountOwnerAPIKey, null, true, task);
                    if (response.getStatusCode() == 200) {
                        taskIdMap.put(jobKey, response.jsonPath().getInt("id"));
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(taskIdMap.values());
    }

    private Task processTaskPayload(JSONObject payload, String jobSlug) {
        Task task = new Task();
        
        task.setTitle(payload.optString("title", "Default Task Title"));
        task.setDescription(payload.optString("description", "Task description"));
        task.setStart_date(fakerTask.getFutureDate());
        task.setReminder(payload.optInt("reminder", 30));
        
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{job")) {
            task.setRelated_to(jobSlug);
        } else {
            task.setRelated_to(payload.optString("related_to", jobSlug));
        }
        task.setRelated_to_type(payload.optString("related_to_type", "job"));
        
        if (payload.has("task_type_id")) {
            String taskTypeIdStr = payload.getString("task_type_id");
            taskTypeIdStr = replacePlaceholdersInFilterValue(taskTypeIdStr);
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

    public void updateTasksWithTimestampScenarios(List<Integer> taskIds) {
        tasksTimestampScenarios = createTasksTimestampScenarios();

        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(tasksTimestampScenarios.entrySet());
        int limit = Math.min(taskIds.size(), scenarios.size());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < limit; i++) {
                final int idx = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, String> timestamps = scenarios.get(idx).getValue();
                    Integer taskId = taskIds.get(idx);

                    JSONObject fieldsAndTimestamps = new JSONObject();
                    for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                        fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
                    }

                    Response updateResponse = ReaperIntegration.updateActivityTimestamp(taskId, fieldsAndTimestamps,"task");
                    if (updateResponse.getStatusCode() != 200) {
                        Assert.fail("Failed to update the tasks timestamps");
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }

    private Map<String, Map<String, String>> createTasksTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new LinkedHashMap<>();

        String todayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getTodayDateString("yyyy-MM-dd")));
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdOn", todayEpoch);
        todayTimestamps.put("updatedOn", todayEpoch);
        todayTimestamps.put("startdate", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        String yesterdayEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getYesterdayDateString("yyyy-MM-dd")));
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdOn", yesterdayEpoch);
        yesterdayTimestamps.put("updatedOn", yesterdayEpoch);
        yesterdayTimestamps.put("startdate", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        String thisWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisWeekDateString()));
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdOn", thisWeekEpoch);
        thisWeekTimestamps.put("updatedOn", thisWeekEpoch);
        thisWeekTimestamps.put("startdate", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        String lastWeekEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastWeekDateString()));
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdOn", lastWeekEpoch);
        lastWeekTimestamps.put("updatedOn", lastWeekEpoch);
        lastWeekTimestamps.put("startdate", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        String thisMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisMonthDateString()));
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdOn", thisMonthEpoch);
        thisMonthTimestamps.put("updatedOn", thisMonthEpoch);
        thisMonthTimestamps.put("startdate", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        String lastMonthEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastMonthDateString()));
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdOn", lastMonthEpoch);
        lastMonthTimestamps.put("updatedOn", lastMonthEpoch);
        lastMonthTimestamps.put("startdate", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        String thisQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisQuarterDateString()));
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdOn", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedOn", thisQuarterEpoch);
        thisQuarterTimestamps.put("startdate", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        String lastQuarterEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastQuarterDateString()));
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdOn", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedOn", lastQuarterEpoch);
        lastQuarterTimestamps.put("startdate", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        String thisYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getThisYearDateString()));
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdOn", thisYearEpoch);
        thisYearTimestamps.put("updatedOn", thisYearEpoch);
        thisYearTimestamps.put("startdate", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        String lastYearEpoch = String.valueOf(dateToEpochSeconds(DateUtil.getLastYearDateString()));
        Map<String, String> lastYearTimestamps = new HashMap<>();
        lastYearTimestamps.put("createdOn", lastYearEpoch);
        lastYearTimestamps.put("updatedOn", lastYearEpoch);
        lastYearTimestamps.put("startdate", lastYearEpoch);
        scenarios.put("last_year_scenario", lastYearTimestamps);

        Map<String, String> staticTimestamps1 = new HashMap<>();
        staticTimestamps1.put("createdOn", "1655251200");  // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("updatedOn", "1655251200");
        staticTimestamps1.put("startdate", "1655251200");
        scenarios.put("static_date_scenario1", staticTimestamps1);

        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdOn", "1678406400"); // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("updatedOn", "1678406400");
        staticTimestamps2.put("startdate", "1678406400");
        scenarios.put("static_date_scenario2", staticTimestamps2);

        return scenarios;
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

    public String replacePlaceholdersInFilterValue(String filterValue) {
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

    public Response updateTaskStatus(int taskId, String status) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + albatrossAuthToken);
        headers.put("Content-Type", "application/json");
        
        JSONObject payload = new JSONObject();
        payload.put("key", "status");
        payload.put("value", status);
        payload.put("tableFlag", "task");
        payload.put("id", taskId);
        
        String basePath = "global/update-fields";
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
        
        return response;
    }

    public List<Integer> getTaskIdsBySlug(String jobSlug) {
        try {
            Response response = getJobTasksBySlug(albatrossURL, albatrossAuthToken, jobSlug);
            
            if (response.getStatusCode() != 200) {
                return new ArrayList<>();
            }
            
            JsonPath jp = response.jsonPath();
            List<Integer> taskIds = new ArrayList<>();
            
            List<Map<String, Object>> tasks = jp.getList("data.events.tasks");
            if (tasks != null && !tasks.isEmpty()) {
                for (Map<String, Object> task : tasks) {
                    Object idObj = task.get("id");
                    if (idObj != null) {
                        taskIds.add(Integer.parseInt(idObj.toString()));
                    }
                }
            }
            
            return taskIds;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void updateTasksToCompleteStatus() {
        String[] completeJobs = {"job7", "job8", "job9", "job10", "job11", "job12"};
        
        for (String jobKey : completeJobs) {
            try {
                String jobSlug = jobSlugMap.get(jobKey);
                if (jobSlug != null) {
                    List<Integer> taskIds = getTaskIdsBySlug(jobSlug);
                    
                    for (Integer taskId : taskIds) {
                        Response updateResponse = updateTaskStatus(taskId, "1");
                        Assert.assertEquals(updateResponse.getStatusCode(), 200, 
                            "Failed to update task " + taskId + " to complete status");
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private String getAlbatrossAuthToken(String createdBy) {
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

    private Map<String, Integer> createJobStatusMap() {
        Map<String, Integer> statusMap = new HashMap<>();
        try {
            statusMap = function.getJobStatusValues(albatrossURL, albatrossAuthToken);
        } catch (Exception e) {
        }
        return statusMap;
    }

    private Map<String, Integer> createQualificationMap() {
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

    private Map<String, Integer> createHiringPipelineMap() {
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
}
