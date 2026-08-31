package io.recruitcrm.CompanyService.Filters;

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
public class TaskActivityFieldsCompanyFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    commanFunction function;
    String accountOwnerAPIKey;
    String email;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> companyKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companyIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> companySlugMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> tasksTimestampScenarios;
    Map<String, Integer> companyIdMap = new HashMap<>();
    JavaFakerTask fakerTask;
    int defaultTaskTypeId = 0;
    int followUpTaskTypeId = 0;
    int interviewSchedulingTaskTypeId = 0;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        email = ThreadManager.getAccount().getOwner().getEmail();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        fakerTask = new JavaFakerTask();
        createAssociatedEntities();
        setupTaskTypes();
        userMap = createUserMap();
        teamMap = createTeamMap();
        createTestData();
        waitForDataSync();
    }

    @DataProvider(name = "tasksDateFieldFilterSearchTestData", parallel = true)
    public Object[][] tasksDateFieldDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyTaskFilterDataProvider.json");
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
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValue_TYPE
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "tasksDateFieldFilterSearchTestData", description = "Tasks Activity Fields Company Filter Search Test")
    public void tasksDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createDateFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

        validateDateFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "tasksTaskExistsFilterSearchTestData", parallel = true)
    public Object[][] tasksTaskExistsFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyTaskFilterDataProvider.json");
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
                    testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValue_TYPE
                    });
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    @Owner("Sai Teja SG")
    @Test(groups = {"aries_service"}, dataProvider = "tasksTaskExistsFilterSearchTestData", description = "Tasks Task Exists (checkbox) Company Filter Search Test")
    public void tasksTaskExistsFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);

        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "tasksMultiselectAndDropdownFilterSearchTest", parallel = true)
    public Object[][] tasksMultiselectAndDropdownFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyTaskFilterDataProvider.json");
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
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValue_TYPE
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "tasksMultiselectAndDropdownFilterSearchTest", description = "Tasks Activity Fields Company Filter Search Test")
    public void tasksMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    @DataProvider(name = "tasksTextFieldFilterSearchTest", parallel = true)
    public Object[][] tasksTextFieldFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyTaskFilterDataProvider.json");
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
                    testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType
                    });
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "tasksTextFieldFilterSearchTest", description = "Tasks Text Fields Company Filter Search Test")
    public void tasksTextFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

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

        String[] expectedCompanies = expectedResult.split(",");
        List<Integer> expectedCompanyIds = new ArrayList<>();
        for (String companyKey : expectedCompanies) {
            String normalizedKey = companyKey.trim().toLowerCase().replace(" ", "");
            String companyIdStr = companyKeyToIdMap.get(normalizedKey);
            if (companyIdStr == null) {
                Assert.fail("Expected company key '" + companyKey + "' (normalized: '" + normalizedKey + "') not found in companyKeyToIdMap. Available keys: " + companyKeyToIdMap.keySet());
            }
            expectedCompanyIds.add(Integer.parseInt(companyIdStr));
        }

        List<Integer> actualCompanyIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject company = data.getJSONObject(i);
            actualCompanyIds.add(company.getInt("id"));
        }

        
        Assert.assertEquals(data.length(),expectedCompanyIds.size(), "All expected companies are not present in the response");
        for (int companyId : expectedCompanyIds) {
            if (!actualCompanyIds.contains(companyId)) {
                Assert.fail("Company: " + companyIdToKeyMap.get(String.valueOf(companyId)) + " is not present in the actual response but was expected to be present");
            }
        }
    }

    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (expectedResult.isEmpty() || expectedResult.trim().isEmpty()){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject company = data.getJSONObject(i);
            int companyId = company.getInt("id");
            String companySlug = company.getString("slug");
            
            try {
                Response taskResponse = getCompanyTasksBySlug(albatrossURL, albatrossAuthToken, companySlug);
                
                Assert.assertEquals(taskResponse.getStatusCode(), 200, 
                    "Failed to fetch task for company ID: " + companyId);
                
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
                    "No matching task record found for task ID: " + companyId +
                    " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
                    
            } catch (Exception e) {
                Assert.fail("Error validating task for task ID: " + companyId +
                    " - " + e.getMessage());
            }
        }
    }

    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        }
        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject company = data.getJSONObject(i);
            String companySlug = company.getString("slug");
            Response taskResponse = getCompanyTasksBySlug(albatrossURL, albatrossAuthToken, companySlug);
            String responseBody = taskResponse.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray tasks = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("tasks");
            
            if(expectedResult.equals("Empty")) {
                Assert.assertEquals(tasks.length(), 0, "Wrong company data for field: " + fieldName +  " and filterType: " + filterType + " and filterValue: " + filterValue);
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
                "No matching task record found for company ID: " + companySlug + 
                " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
        }
    }

    public Response getCompanyTasksBySlug(String albatrossURL, String authToken, String companySlug) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + authToken);
        String basePath = "expand-activity/get-activity-data";

        JSONObject payload = new JSONObject();
        payload.put("type", "1");
        payload.put("page", "detailspage");
        payload.put("relatedToSlug", companySlug);
        payload.put("relatedtotypeid", 3); // Company type ID is 3

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch company tasks data");
        return response;
    }

    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "COMPANY");
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
        payload.put("advancedSearchContext", "COMPANY");
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
        payload.put("advancedSearchContext", "COMPANY");
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
                // Handle company1, company2, etc. in filter values
                actualValue = companyKeyToIdMap.get(fieldKey.toLowerCase()).toString();
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

    public void createTestData() {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> companyIdMap = new ConcurrentHashMap<>();
        try {
            CompletableFuture.allOf(companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        int companyId = response.jsonPath().getInt("data.company.id");
                        companyIdMap.put(companyKey, companyId);
                        companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                        companySlugMap.put(companyKey, response.jsonPath().getString("data.company.slug"));
                        companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> taskIds = createTasksForCompany(companySlugMap);
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

            companyKeyToIdMap.put("associated_candidates_candidate1", String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"))));
            companyKeyToIdMap.put("associated_candidates_candidate2", String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"))));
            companyKeyToIdMap.put("associated_companies_company1", String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"))));
            companyKeyToIdMap.put("associated_companies_company2", String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"))));
            companyIdToKeyMap.put(String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"))), "associated_candidates_candidate1");
            companyIdToKeyMap.put(String.valueOf(function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"))), "associated_candidates_candidate2");
            companyIdToKeyMap.put(String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson1.getString("slug"))), "associated_companies_company1");
            companyIdToKeyMap.put(String.valueOf(function.getCompanyIdBySlug(albatrossURL, albatrossAuthToken, companyJson2.getString("slug"))), "associated_companies_company2");
        } finally {
            executor.shutdown();
        }
    }

    private List<Integer> createTasksForCompany(Map<String, String> companySlugMap) {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        LinkedHashMap<String, Integer> taskIdMap = new LinkedHashMap<>();

        List<String> sortedCompanyKeys = companySlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String companyKey : sortedCompanyKeys) {
                if (!companyJson.has(companyKey) || !companyJson.getJSONObject(companyKey).has("tasks")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    String companySlug = companySlugMap.get(companyKey);
                    JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                    JSONObject tasksData = companyEntry.getJSONObject("tasks");
                    Task task = processTaskPayload(tasksData, companySlug);
                    Response response = RestClient.doPost("JSON", baseURL, "tasks", accountOwnerAPIKey, null, true, task);
                    if (response.getStatusCode() == 200) {
                        taskIdMap.put(companyKey, response.jsonPath().getInt("id"));
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return new ArrayList<>(taskIdMap.values());
    }

    private Task processTaskPayload(JSONObject payload, String companySlug) {
        Task task = new Task();
        
        task.setTitle(payload.optString("title", "Default Task Title"));
        task.setDescription(payload.optString("description", "Task description"));
        task.setStart_date(fakerTask.getFutureDate());
        task.setReminder(payload.optInt("reminder", 30));
        
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{company")) {
            task.setRelated_to(companySlug);
        } else {
            task.setRelated_to(payload.optString("related_to", companySlug));
        }
        task.setRelated_to_type(payload.optString("related_to_type", "company"));
        
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

    public List<Integer> getTaskIdsBySlug(String companySlug) {
        try {
            Response response = getCompanyTasksBySlug(albatrossURL, albatrossAuthToken, companySlug);
            
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
        String[] completeCompanies = {"company7", "company8", "company9", "company10", "company11", "company12"};
        
        for (String companyKey : completeCompanies) {
            try {
                String companySlug = companySlugMap.get(companyKey);
                if (companySlug != null) {
                    List<Integer> taskIds = getTaskIdsBySlug(companySlug);
                    
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
}

