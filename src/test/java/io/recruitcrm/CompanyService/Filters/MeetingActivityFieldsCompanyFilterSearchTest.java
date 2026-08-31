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
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.javafaker.JavaFakerMeeting;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MeetingActivityFieldsCompanyFilterSearchTest extends FilterSearchBaseTest {
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
    ConcurrentHashMap<Integer, String> meetingIdToCompanyKeyMap = new ConcurrentHashMap<>();
    Map<String, Map<String, String>> meetingsTimestampScenarios;
    JavaFakerMeeting fakerMeeting;
    int defaultMeetingTypeId = 0;
    int businessDevelopmentMeetingTypeId = 0;
    int candidateInternalInterviewTypeId = 0;
    int candidateExternalInterviewTypeId = 0;
    int clientMeetingTypeId = 0;
    int internalMeetingTypeId = 0;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        email = ThreadManager.getAccount().getOwner().getEmail();
        fakerMeeting = new JavaFakerMeeting();
        createAssociatedEntities();
        setupMeetingTypes();
        userMap = createUserMap();
        teamMap = createTeamMap();
        createTestData();
        waitForDataSync();
    }

    @DataProvider(name = "meetingsDateFieldFilterSearchTestData", parallel = true)
    public Object[][] meetingsDateFieldDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyMeetingFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                if ("date".equals(fieldType)) {
                    try {
                        String filterType = test.getString("filterType");
                        String filterValue = getFilterValueAsString(test);
                        String filterValueType = test.getString("filterValue_TYPE");
                        
                        testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType
                        });
                    } catch (Exception e) {
                        System.err.println("Error processing test case in section: " + key + ", index: " + i + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Date field filter test data provider returning " + testData.size() + " test cases");
        return testData.toArray(new Object[0][]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "meetingsDateFieldFilterSearchTestData", description = "Meetings Activity Fields Company Filter Search Test")
    public void meetingsDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
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

    @DataProvider(name = "meetingsMeetingExistsFilterSearchTestData", parallel = true)
    public Object[][] meetingsMeetingExistsFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyMeetingFilterDataProvider.json");
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
                    testData.add(new Object[]{
                            key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType
                    });
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    @Owner("Sai Teja SG")
    @Test(groups = {"aries_service"}, dataProvider = "meetingsMeetingExistsFilterSearchTestData", description = "Meetings Meeting Exists (checkbox) Company Filter Search Test")
    public void meetingsMeetingExistsFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType) {
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logInfo("Account: ", email);
        FilterSearchReporter.logPayload(payload);

        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

        validateMultiselectAndDropdownFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    }

    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
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
            if (dbField.equals("meetingscheduledon")) {
                dbField = "startdate";
            }
            JSONObject company = data.getJSONObject(i);
            String companySlug = company.getString("slug");
            Response meetingResponse = getCompanyMeetingsBySlug(albatrossURL, albatrossAuthToken, companySlug);
            String responseBody = meetingResponse.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray meetings = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("appointments");
            
            boolean foundMatchingRecord = false;
            if (filterType.equals("is_empty") && meetings.length() == 0) {
                foundMatchingRecord = true;
            }
            for (int j = 0; j < meetings.length(); j++) {
                JSONObject meeting = meetings.getJSONObject(j);
                String meetingDate = meeting.optString(dbField, "0");

                if (validateDateAgainstFilter(meetingDate, filterType, filterValue, fieldName)) {
                    foundMatchingRecord = true;
                    break; 
                }
                
            }
            Assert.assertTrue(foundMatchingRecord, 
                "No matching meeting record found for company ID: " + companySlug + 
                " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
        }
    }

    public Response getCompanyMeetingsBySlug(String albatrossURL, String authToken, String companySlug) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + authToken);
        String basePath = "expand-activity/get-activity-data";

        JSONObject payload = new JSONObject();
        payload.put("type", "2"); // 2 = appointments/meetings
        payload.put("page", "detailspage");
        payload.put("relatedToSlug", companySlug);
        payload.put("relatedtotypeid", 3); // Company type ID is 3

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch company meetings data");
        return response;
    }

    @DataProvider(name = "meetingsMultiselectAndDropdownFilterSearchTest", parallel = true)
    public Object[][] meetingsMultiselectAndDropdownFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyMeetingFilterDataProvider.json");
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
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), fieldType, filterValueType
                    });
                }
            }
        }
        return testData.toArray(new Object[0][]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "meetingsMultiselectAndDropdownFilterSearchTest", description = "Meetings Multiselect Fields Company Filter Search Test")
    public void meetingsMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType) {
        JSONObject payload = createMultiselectAndDropdownFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValueType);
        FilterSearchReporter.logInfo("Account: ",email);
        System.out.println("Payload: "+payload);
        FilterSearchReporter.logPayload(payload);
        
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

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
        
        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
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
        filter.put("groupType", "meeting");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "meeting");
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
            else if (fieldKey.startsWith("meeting_type_")) {
                if (fieldKey.equals("meeting_type_business_development")) {
                    actualValue = String.valueOf(businessDevelopmentMeetingTypeId);
                } else if (fieldKey.equals("meeting_type_candidate_internal_interview")) {
                    actualValue = String.valueOf(candidateInternalInterviewTypeId);
                } else if (fieldKey.equals("meeting_type_candidate_external_interview")) {
                    actualValue = String.valueOf(candidateExternalInterviewTypeId);
                } else if (fieldKey.equals("meeting_type_client_meeting")) {
                    actualValue = String.valueOf(clientMeetingTypeId);
                } else if (fieldKey.equals("meeting_type_internal_meeting")) {
                    actualValue = String.valueOf(internalMeetingTypeId);
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

    @DataProvider(name = "meetingsTextFieldFilterSearchTest", parallel = true)
    public Object[][] meetingsTextFieldFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/companyMeetingFilterDataProvider.json");
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
    @Test(groups = {"aries_service"}, dataProvider = "meetingsTextFieldFilterSearchTest", description = "Meetings Text Fields Company Filter Search Test")
    public void meetingsTextFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValue_TYPE) {
        JSONObject payload = createTextFieldFilterSearchPayload(fieldName, filterType, filterValue, dbField, fieldType, filterValue_TYPE);
        FilterSearchReporter.logInfo("Account: ",email);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "companies");

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "companyname");

        validateTextFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    } 


    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        // First validate the count of companies returned
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (expectedResult.isEmpty() || expectedResult.trim().isEmpty()){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        // If we expect data but got none, fail the test
        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 companies for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        // Validate each company's meetings
        for (int i = 0; i < data.length(); i++) {
            JSONObject company = data.getJSONObject(i);
            int companyId = company.getInt("id");
            String companySlug = company.getString("slug");
            
            try {
                Response meetingResponse = getCompanyMeetingsBySlug(albatrossURL, albatrossAuthToken, companySlug);
                
                Assert.assertEquals(meetingResponse.getStatusCode(), 200, 
                    "Failed to fetch meeting for company ID: " + companyId);
                
                String responseBody = meetingResponse.getBody().asString();
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray meetingContent = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("appointments");
                
                FilterSearchReporter.logResponse(meetingResponse, meetingContent);

                boolean foundMatchingRecord = false;
                if (meetingContent.length() == 0) {
                    if (filterType.equals("does_not_contain") || filterType.equals("is_not") || filterType.equals("is_empty")) {
                        foundMatchingRecord = true;
                        continue;
                    }
                }

                if (filterType.equals("does_not_contain") || filterType.equals("is_not")) {
                    foundMatchingRecord = true;
                    for (int j = 0; j < meetingContent.length(); j++) {
                        JSONObject meetingRecord = meetingContent.getJSONObject(j);
                        String meetingFieldValue = meetingRecord.optString("title", "");

                        if (!validateTextAgainstFilter(meetingFieldValue, filterType, filterValue, fieldName)) {
                            foundMatchingRecord = false;
                            break;
                        }
                    }
                } else {
                    for (int j = 0; j < meetingContent.length(); j++) {
                        JSONObject meetingRecord = meetingContent.getJSONObject(j);
                        String meetingFieldValue = meetingRecord.optString("title", "");
                        
                        if (validateTextAgainstFilter(meetingFieldValue, filterType, filterValue, fieldName)) {
                            foundMatchingRecord = true;
                            break; 
                        }
                    }
                }
                
                Assert.assertTrue(foundMatchingRecord, 
                    "No matching meeting record found for meeting ID: " + companyId +
                    " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
                    
            } catch (Exception e) {
                Assert.fail("Error validating meeting for meeting ID: " + companyId +
                    " - " + e.getMessage());
            }
        }
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
        filter.put("groupType", "meeting");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "meeting");
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
        filter.put("groupType", "meeting");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "meeting");
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
        try {
            CompletableFuture.allOf(companyJson.keySet().stream()
                    .filter(key -> key.startsWith("company"))
                    .map(companyKey -> CompletableFuture.runAsync(() -> {
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("payload");
                        Response response = allCrudFunctions.createCompanyWithJson(albatrossURL, albatrossAuthToken, payload);
                        int companyId = response.jsonPath().getInt("data.company.id");
                        companyIdToKeyMap.put(String.valueOf(companyId), companyKey);
                        companySlugMap.put(companyKey, response.jsonPath().getString("data.company.slug"));
                        companyKeyToIdMap.put(companyKey, String.valueOf(companyId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> meetingIds = createMeetingsForCompany(companySlugMap);
            updateMeetingsWithTimestampScenarios(meetingIds);

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
            System.out.println("associatedEntitiesIdMap: "+associatedEntitiesIdMap);

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

    private List<Integer> createMeetingsForCompany(Map<String, String> companySlugMap) {
        JSONObject companyJson = readJsonFileFromPath("src/test/resources/testData/company_data.json");
        LinkedHashMap<String, Integer> meetingIdMap = new LinkedHashMap<>();

        List<String> sortedCompanyKeys = companySlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String companyKey : sortedCompanyKeys) {
                if (!companyJson.has(companyKey) || !companyJson.getJSONObject(companyKey).has("meetings")) {
                    continue;
                }

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        String companySlug = companySlugMap.get(companyKey);
                        JSONObject companyEntry = companyJson.getJSONObject(companyKey);
                        JSONObject payload = companyEntry.getJSONObject("meetings");
                        Meeting meeting = processMeetingPayload(payload, companySlug);
                        
                        Response response = RestClient.doPost("JSON", baseURL, "meetings", accountOwnerAPIKey, null, true, meeting);
                        
                        if (response.getStatusCode() == 200) {
                            int meetingId = response.jsonPath().getInt("id");
                            meetingIdMap.put(companyKey, meetingId);
                            meetingIdToCompanyKeyMap.put(meetingId, companyKey);
                        } else {
                            System.err.println("Meeting creation failed for " + companyKey + ": " + response.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("Exception creating meeting for " + companyKey + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        List<Integer> sortedMeetingIds = sortedCompanyKeys.stream()
                .filter(meetingIdMap::containsKey)
                .map(meetingIdMap::get)
                .collect(Collectors.toList());
        
        return sortedMeetingIds;
    }

    private Meeting processMeetingPayload(JSONObject payload, String companySlug) {
        Meeting meeting = new Meeting();
        
        meeting.setTitle(payload.optString("title", "Default Meeting Title"));
        meeting.setDescription(payload.optString("description", "Meeting description"));
        meeting.setAddress(payload.optString("address", "Office Address"));

        String startDate = getVariedStartDateForCompany(companySlug);
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
        
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{company")) {
            meeting.setRelated_to(companySlug);
        } else {
            meeting.setRelated_to(payload.optString("related_to", companySlug));
        }
        meeting.setRelated_to_type(payload.optString("related_to_type", "company"));
        
        if (payload.has("meeting_type_id")) {
            String meetingTypeIdStr = payload.getString("meeting_type_id");
            meetingTypeIdStr = replacePlaceholdersInFilterValue(meetingTypeIdStr);
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
            } else {
                if (!userMap.isEmpty()) {
                    String firstUserId = userMap.values().iterator().next();
                    meeting.setOwner_id(Integer.parseInt(firstUserId));
                }
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

    public void updateMeetingsWithTimestampScenarios(List<Integer> meetingIds) {
        meetingsTimestampScenarios = createMeetingsTimestampScenarios();

        List<Map.Entry<String, Map<String, String>>> scenarios = new ArrayList<>(meetingsTimestampScenarios.entrySet());
        int limit = Math.min(meetingIds.size(), scenarios.size());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < limit; i++) {
                final int idx = i;
                futures.add(CompletableFuture.runAsync(() -> {
                    Map<String, String> timestamps = scenarios.get(idx).getValue();
                    Integer meetingId = meetingIds.get(idx);
                    
                    String companyKey = meetingIdToCompanyKeyMap.get(meetingId);

                    JSONObject fieldsAndTimestamps = new JSONObject();
                    for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                        fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
                    }

                    Response updateResponse = ReaperIntegration.updateActivityTimestamp(meetingId, fieldsAndTimestamps,"appointment");
                    if (updateResponse.getStatusCode() != 200) {
                        System.err.println("Failed to update timestamps for " + companyKey + " (Meeting ID: " + meetingId + ")");
                        System.err.println("Response: " + updateResponse.getBody().asString());
                        Assert.fail("Failed to update the meetings timestamps");
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
    }
    
    private Map<String, Map<String, String>> createMeetingsTimestampScenarios() {
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
        staticTimestamps1.put("createdOn", "1655251200"); // 2022-06-15 00:00:00
        staticTimestamps1.put("updatedOn", "1655251200"); // 2022-06-15 00:00:00
        staticTimestamps1.put("startdate", "1655251200"); // 2022-06-15 00:00:00
        scenarios.put("static_date_scenario1", staticTimestamps1);

        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdOn", "1678406400"); // 2023-03-10 00:00:00
        staticTimestamps2.put("updatedOn", "1678406400"); // 2023-03-10 00:00:00
        staticTimestamps2.put("startdate", "1678406400"); // 2023-03-10 00:00:00
        scenarios.put("static_date_scenario2", staticTimestamps2);

        return scenarios;
    }
    
    private String getVariedStartDateForCompany(String companySlug) {
        String companyKey = companySlugMap.entrySet().stream()
            .filter(entry -> entry.getValue().equals(companySlug))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse("unknown");

        switch (companyKey) {
            case "company1":
                return getDateDaysFromNow(0); // Today
            case "company2": 
                return getDateDaysFromNow(-1); // Yesterday
            case "company3":
                return getDateDaysFromNow(7); // Next week
            case "company4":
                return getDateDaysFromNow(-7); // Last week
            case "company5":
                return getDateDaysFromNow(-30); // 30 days ago
            case "company6":
                return getDateDaysFromNow(-60); // 60 days ago
            case "company7":
                return getDateDaysFromNow(-90); // 90 days ago
            case "company8":
                return getDateDaysFromNow(-180); // 180 days ago
            case "company9":
                return getDateDaysFromNow(14); // 2 weeks future
            case "company10":
                return getDateDaysFromNow(-365); // 1 year ago
            case "company11":
                return getDateDaysFromNow(30); // 1 month future
            case "company12":
                return getDateDaysFromNow(60); // 2 months future
            case "company13":
                return getDateDaysFromNow(0); // Today
            case "company14":
                return getDateDaysFromNow(-45); // 45 days ago
            case "company15":
                return getDateDaysFromNow(-120); // 120 days ago
            case "company16":
                return getDateDaysFromNow(-200); // 200 days ago
            case "company17":
                return getDateDaysFromNow(21); // 3 weeks future
            default:
                return fakerMeeting.getFutureDate(); // Default future date
        }
    }
    

    private String getDateDaysFromNow(int days) {
        java.util.Date date = new java.util.Date();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days);
        return calendar.getTime().toString();
    }
    

    private long parseDynamicDateValue(String dynamicValue) {
        if (dynamicValue.equals("DYNAMIC_180_DAYS_AGO")) {
            return System.currentTimeMillis() / 1000 - (180 * 24 * 60 * 60);
        } else if (dynamicValue.equals("DYNAMIC_90_DAYS_AGO")) {
            return System.currentTimeMillis() / 1000 - (90 * 24 * 60 * 60);
        } else if (dynamicValue.equals("DYNAMIC_60_DAYS_AGO")) {
            return System.currentTimeMillis() / 1000 - (60 * 24 * 60 * 60);
        } else if (dynamicValue.equals("DYNAMIC_60_DAYS_FUTURE")) {
            return System.currentTimeMillis() / 1000 + (60 * 24 * 60 * 60);
        }
        return System.currentTimeMillis() / 1000;
    }

    private String processDynamicBetweenValue(String jsonValue) {
        String result = jsonValue;

        if (result.contains("DYNAMIC_60_DAYS_AGO")) {
            long epoch = System.currentTimeMillis() / 1000 - (60 * 24 * 60 * 60);
            result = result.replace("\"DYNAMIC_60_DAYS_AGO\"", String.valueOf(epoch));
        }
        if (result.contains("DYNAMIC_60_DAYS_FUTURE")) {
            long epoch = System.currentTimeMillis() / 1000 + (60 * 24 * 60 * 60);
            result = result.replace("\"DYNAMIC_60_DAYS_FUTURE\"", String.valueOf(epoch));
        }
        
        return result;
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
                        } else if ("Candidate Internal Interview".equals(label)) {
                            candidateInternalInterviewTypeId = id;
                        } else if ("Candidate Interview External".equals(label)) {
                            candidateExternalInterviewTypeId = id;
                        } else if ("Client Meeting".equals(label)) {
                            clientMeetingTypeId = id;
                        } else if ("Internal Meeting".equals(label)) {
                            internalMeetingTypeId = id;
                        }
                        
                        if (defaultMeetingTypeId == 0) {
                            defaultMeetingTypeId = id;
                        }
                    }
                    
                    // Set fallback IDs for missing meeting types
                    if (businessDevelopmentMeetingTypeId == 0) businessDevelopmentMeetingTypeId = defaultMeetingTypeId;
                    if (candidateInternalInterviewTypeId == 0) candidateInternalInterviewTypeId = defaultMeetingTypeId;
                    if (candidateExternalInterviewTypeId == 0) candidateExternalInterviewTypeId = defaultMeetingTypeId;
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
        candidateInternalInterviewTypeId = 2;
        candidateExternalInterviewTypeId = 3;
        clientMeetingTypeId = 4;
        internalMeetingTypeId = 5;
    }

    public String replacePlaceholdersInFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }
        
        String result = filterValue;
        
        result = result.replace("{{meeting_type_business_development}}", String.valueOf(businessDevelopmentMeetingTypeId));
        result = result.replace("{{meeting_type_candidate_internal_interview}}", String.valueOf(candidateInternalInterviewTypeId));
        result = result.replace("{{meeting_type_candidate_external_interview}}", String.valueOf(candidateExternalInterviewTypeId));
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


