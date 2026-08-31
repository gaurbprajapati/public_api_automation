package io.recruitcrm.CandidateService.Filters;

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
import org.testng.Reporter;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.DateUtil;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.Meeting;
import io.rcrm.api.javafaker.JavaFakerMeeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MeetingActivityFieldsCandidateFilterSearchTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    commanFunction function;
    String email;
    String accountOwnerAPIKey;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> userMap;
    Map<String, String> teamMap;
    ConcurrentHashMap<String, String> candidateKeyToIdMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> candidateIdToKeyMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> candidateSlugMap = new ConcurrentHashMap<>();
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
        email = ThreadManager.getAccount().getOwner().getEmail();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        fakerMeeting = new JavaFakerMeeting();
        createAssociatedEntities();
        setupMeetingTypes();
        userMap = createUserMap(accountOwnerAPIKey);
        teamMap = createTeamMap();
        createTestData();
        waitForDataSync();
    }

    @DataProvider(name = "meetingsDateFieldFilterSearchTestData", parallel = true)
    public Object[][] meetingsDateFieldDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateMeetingFilterDataProvider.json");
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
    @Test(groups = {"aries_service"}, dataProvider = "meetingsDateFieldFilterSearchTestData", description = "Meetings Activity Fields Candidate Filter Search Test")
    public void meetingsDateFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) throws InterruptedException {
        Reporter.log("Test Case ID: " + testCaseId, true);
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

    @DataProvider(name = "meetingsMeetingExistsFilterSearchTestData", parallel = true)
    public Object[][] meetingsMeetingExistsFilterSearchTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateMeetingFilterDataProvider.json");
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
        return testData.toArray(new Object[0][]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "meetingsMeetingExistsFilterSearchTestData", description = "Meetings Meeting Exists (checkbox) Candidate Filter Search Test")
    public void meetingsMeetingExistsFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        Reporter.log("Test Case ID: " + testCaseId, true);
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

    public void validateDateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " candidates for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (expectedResult.isEmpty() || expectedResult.trim().isEmpty()){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 candidates for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        for (int i = 0; i < data.length(); i++) {
            if (dbField.equals("meetingscheduledon")) {
                dbField = "startdate";
            }
            JSONObject candidate = data.getJSONObject(i);
            String candidateSlug = candidate.getString("slug");
            Response meetingResponse = getCandidateMeetingsBySlug(albatrossURL, albatrossAuthToken, candidateSlug);
            String responseBody = meetingResponse.getBody().asString();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray meetings = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("appointments");
            
            boolean foundMatchingRecord = false;
            for (int j = 0; j < meetings.length(); j++) {
                JSONObject meeting = meetings.getJSONObject(j);
                String meetingDate = meeting.optString(dbField, "0");

                if (validateDateAgainstFilter(meetingDate, filterType, filterValue, fieldName)) {
                    foundMatchingRecord = true;
                    break; 
                }
                
            }
            Assert.assertTrue(foundMatchingRecord, 
                "No matching meeting record found for candidate ID: " + candidateSlug + 
                " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
        }
    }

    public Response getCandidateMeetingsBySlug(String albatrossURL, String authToken, String candidateSlug) {
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + authToken);
        String basePath = "expand-activity/get-activity-data";

        JSONObject payload = new JSONObject();
        payload.put("type", "2"); // 2 = appointments/meetings
        payload.put("page", "detailspage");
        payload.put("relatedToSlug", candidateSlug);
        payload.put("relatedtotypeid", 5); // Candidate type ID is 5

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, authTokenMap, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Failed to fetch candidate meetings data");
        return response;
    }

    public void validateFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        if (expectedResult.equals("Empty") || expectedResult.trim().isEmpty()) {
            Assert.assertEquals(data.length(), 0, "Expected result is empty but response has data");
            return;
        }

        if(expectedResult.equals("All")) {
            Assert.assertEquals(data.length(), 11, "Expected result is all 11 candidates but response has " + data.length() + " candidates");
            return;
        }

        try {
            int expectedCount = Integer.parseInt(expectedResult);
            Assert.assertEquals(data.length(), expectedCount, 
                "Expected " + expectedCount + " candidates for filter " + dbField + " " + filterType + " " + filterValue + 
                " but found " + data.length());
            return;
        } catch (NumberFormatException e) {
        }

        String[] expectedCandidateNames = expectedResult.split(",");
        List<Integer> expectedCandidateIds = new ArrayList<>();
        for (String candidateKey : expectedCandidateNames) {
            String cleanKey = candidateKey.toLowerCase().replace(" ", "");
            String candidateIdStr = candidateKeyToIdMap.get(cleanKey);
            if (candidateIdStr != null) {
                expectedCandidateIds.add(Integer.parseInt(candidateIdStr));
            }
        }

        List<Integer> actualCandidateIds = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            actualCandidateIds.add(candidate.getInt("id"));
        }

        StringBuilder actualCandidateIdsStr = new StringBuilder();
        for (int i = 0; i < actualCandidateIds.size(); i++) {
            if (i > 0) actualCandidateIdsStr.append(",");
            actualCandidateIdsStr.append(actualCandidateIds.get(i));
        }

        StringBuilder expectedCandidatesWithIds = new StringBuilder();
        for (int i = 0; i < expectedCandidateNames.length; i++) {
            if (i > 0) expectedCandidatesWithIds.append(",");
            String candidateKey = expectedCandidateNames[i].trim();
            String cleanKey = candidateKey.toLowerCase().replace(" ", "");
            String candidateIdStr = candidateKeyToIdMap.get(cleanKey);
            if (candidateIdStr != null) {
                expectedCandidatesWithIds.append(candidateKey).append("(").append(candidateIdStr).append(")");
            } else {
                expectedCandidatesWithIds.append(candidateKey).append("(ID_NOT_FOUND)");
            }
        }
        
        if (data.length() != expectedCandidateIds.size()) {
            Assert.assertEquals(data.length(), expectedCandidateIds.size(), "All expected candidates are not present in the response");
        } else {
            List<String> missingCandidates = expectedCandidateIds.stream()
                .filter(id -> !actualCandidateIds.contains(id))
                .map(id -> candidateIdToKeyMap.get(String.valueOf(id)) + " (ID:" + id + ")")
                .collect(java.util.stream.Collectors.toList());
            
            List<String> unexpectedCandidates = actualCandidateIds.stream()
                .filter(id -> !expectedCandidateIds.contains(id))
                .map(id -> (candidateIdToKeyMap.getOrDefault(String.valueOf(id), "Unknown") + " (ID:" + id + ")"))
                .collect(java.util.stream.Collectors.toList());
            
            if (!missingCandidates.isEmpty()) {
                Assert.fail("Missing candidate(s): " + String.join(", ", missingCandidates) + " not present in the actual response but were expected");
            } else if (!unexpectedCandidates.isEmpty()) {
                Assert.fail("Unexpected candidate(s): " + String.join(", ", unexpectedCandidates) + " found in response but not expected");
            }
        }
    }

    @DataProvider(name = "meetingsMultiselectAndDropdownFilterSearchTest", parallel = true)
    public Object[][] meetingsMultiselectAndDropdownFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateMeetingFilterDataProvider.json");
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
    @Test(groups = {"aries_service"}, dataProvider = "meetingsMultiselectAndDropdownFilterSearchTest", description = "Meetings Multiselect Fields Candidate Filter Search Test")
    public void meetingsMultiselectFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String fieldType, String filterValueType, String testCaseId) {
        Reporter.log("Test Case ID: " + testCaseId, true);
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
            String cleanKey = candidateKey.trim().toLowerCase().replace(" ", "");
            String candidateIdStr = candidateKeyToIdMap.get(cleanKey);
            if (candidateIdStr != null) {
                expectedCandidateIds.add(Integer.parseInt(candidateIdStr));
            }
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
        filter.put("groupType", "meeting");
        filter.put("searchField", dbField);
        filter.put("filterType", filterType);
        filter.put("entityType", "meeting");
        filter.put("fieldType", fieldType);

        String processedFilterValue = processFilterValue(filterValue, filterValue_TYPE);
        if ("INTEGER_LIST".equals(filterValue_TYPE)) {
            String trimmed = processedFilterValue.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                processedFilterValue = trimmed.substring(1, trimmed.length() - 1).trim();
            }
        }

        JSONObject filterValueObj;
        if ("INTEGER_LIST".equals(filterValue_TYPE)) {
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

    public JSONObject createDateFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String fieldType, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("offLimitBehavior", "bypass");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj = new JSONObject();
        if ("is_between".equals(filterType)) {
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
            if ("is_mt".equals(filterType) || "is_lt".equals(filterType)) {
                filterValueObj.put("value", Integer.parseInt(filterValue));
            } else if ("has_any_value".equals(filterType) || "is_empty".equals(filterType)) {
                filterValueObj.put("value", filterValue.isEmpty() ? 0 : Integer.parseInt(filterValue));
            } else if ("is_equal_to".equals(filterType) || "is_before".equals(filterType) || "is_after".equals(filterType) || "is_not".equals(filterType)) {
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

    private String getFilterValueAsString(JSONObject test) {
        Object filterValueObj = test.get("filterValue");
        if (filterValueObj instanceof JSONObject) {
            return filterValueObj.toString();
        }
        if (filterValueObj instanceof JSONArray) {
            return filterValueObj.toString();
        }
        return String.valueOf(filterValueObj);
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
            if (fieldKey.startsWith("meeting_type_")) {
                if ("meeting_type_business_development".equals(fieldKey)) {
                    actualValue = String.valueOf(businessDevelopmentMeetingTypeId);
                } else if ("meeting_type_candidate_internal_interview".equals(fieldKey)) {
                    actualValue = String.valueOf(candidateInternalInterviewTypeId);
                } else if ("meeting_type_candidate_external_interview".equals(fieldKey)) {
                    actualValue = String.valueOf(candidateExternalInterviewTypeId);
                } else if ("meeting_type_client_meeting".equals(fieldKey)) {
                    actualValue = String.valueOf(clientMeetingTypeId);
                } else if ("meeting_type_internal_meeting".equals(fieldKey)) {
                    actualValue = String.valueOf(internalMeetingTypeId);
                }
            } else if (fieldKey.startsWith("associated_")) {
                actualValue = associatedEntitiesIdMap.get(fieldKey).toString();
            } else if (fieldKey.startsWith("team") && !"teamMember".equals(fieldKey)) {
                actualValue = teamMap.get(fieldKey);
            } else if (fieldKey.startsWith("candidate")) {
                actualValue = candidateKeyToIdMap.get(fieldKey);
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
                String trimmed = processedValue.trim();
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    return trimmed.substring(1, trimmed.length() - 1).trim();
                }
            }
        }

        return processedValue;
    }

    @DataProvider(name = "meetingsTextFieldFilterSearchTest", parallel = true)
    public Object[][] meetingsTextFieldFilterSearchTest() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/filtersDataProvider/candidateMeetingFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
            JSONArray tests = filterData.getJSONArray(key);
            for (int i = 0; i < tests.length(); i++) {
                JSONObject test = tests.getJSONObject(i);
                String fieldType = test.getString("fieldType");
                String filterType = test.getString("filterType");
                String filterValue = getFilterValueAsString(test);
                if (fieldType.equals("text")) {
                    String filterValueType = test.getString("filterValue_TYPE");
                    String testCaseId = test.optString("testCaseId", "");
                    testData.add(new Object[]{
                        key, filterType, filterValue, test.getString("dbField"), test.getString("expectedResult"), filterValueType, testCaseId
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Raj Pandey")
    @Test(groups = {"aries_service"}, dataProvider = "meetingsTextFieldFilterSearchTest", description = "Meetings Text Fields Candidate Filter Search Test")
    public void meetingsTextFieldFilterSearchTest(String fieldName, String filterType, String filterValue, String dbField, String expectedResult, String filterValueType, String testCaseId) {
        Reporter.log("Test Case ID: " + testCaseId, true);
        FilterSearchReporter.logInfo("Test Case ID: ", testCaseId);
        FilterSearchReporter.logInfo("Account: ", email);
        JSONObject payload = createTextFilterSearchPayload(fieldName, filterType, filterValue, dbField, filterValueType);
        FilterSearchReporter.logPayload(payload);
        Response response = executeFilterSearch(payload, albatrossAuthToken, "candidates");

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 200");
        Assert.assertEquals(response.jsonPath().get("meta.message"), "Entities retrieved successfully", "Message for field: " + fieldName + ", filterType: " + filterType + " and filterValue: " + filterValue + " is not 'Entities retrieved successfully'");
        JSONArray data = getFilteredData(response);
        FilterSearchReporter.logFieldValues(response, data, fieldName, "candidatename");

        validateTextFieldFilteredData(data, filterType, filterValue, fieldName, dbField, expectedResult);
    } 

    public void validateTextFieldFilteredData(JSONArray data, String filterType, String filterValue, String fieldName, String dbField, String expectedResult) {
        // First validate the count of candidates returned
        if(expectedResult.equals("Empty")) {
            Assert.assertEquals(data.length(), 0, "Expected empty result but got " + data.length() + " candidates for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
            return;
        } else if (expectedResult.isEmpty() || expectedResult.trim().isEmpty()){
            Assert.fail("No data found for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        // If we expect data but got none, fail the test
        if(data.length() == 0) {
            Assert.fail("Expected results but got 0 candidates for field: " + fieldName + " and filterType: " + filterType + " and filterValue: " + filterValue);
        }
        
        for (int i = 0; i < data.length(); i++) {
            JSONObject candidate = data.getJSONObject(i);
            int candidateId = candidate.getInt("id");
            String candidateSlug = candidate.getString("slug");
            
            try {
                Response meetingResponse = getCandidateMeetingsBySlug(albatrossURL, albatrossAuthToken, candidateSlug);
                
                Assert.assertEquals(meetingResponse.getStatusCode(), 200, "Failed to fetch meeting for candidate ID: " + candidateId);
                
                String responseBody = meetingResponse.getBody().asString();
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray meetingContent = jsonObject.getJSONObject("data").getJSONObject("events").getJSONArray("appointments");
                
                FilterSearchReporter.logResponse(meetingResponse, meetingContent);

                boolean foundMatchingRecord = false;
                System.out.println("meetingContent.length(): " + meetingContent.length());
                if (meetingContent.length() == 0) {
                    if (filterType.equals("does_not_contain") || filterType.equals("is_not") || filterType.equals("is_empty")) {
                        foundMatchingRecord = true;
                        continue;
                    }
                }

                // For does_not_contain and is_not, we need to check ALL meetings don't contain/match the value
                // For other filters, we check if ANY meeting matches
                if (filterType.equals("does_not_contain") || filterType.equals("is_not")) {
                    foundMatchingRecord = true; // Assume all meetings match (don't contain/match the value)
                    for (int j = 0; j < meetingContent.length(); j++) {
                        JSONObject meetingRecord = meetingContent.getJSONObject(j);
                        String meetingFieldValue = meetingRecord.optString("title", "");
                        
                        // If ANY meeting contains/matches the value, the candidate should NOT be in results
                        if (!validateTextAgainstFilter(meetingFieldValue, filterType, filterValue, fieldName)) {
                            foundMatchingRecord = false;
                            break;
                        }
                    }
                } else {
                    // For other filters (contains, is, begins_with, etc.), find at least one match
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
                    "No matching meeting record found for candidate ID: " + candidateId +
                    " with field: " + fieldName + ", filterType: " + filterType + ", filterValue: " + filterValue);
                    
            } catch (Exception e) {
                Assert.fail("Error validating meeting for candidate ID: " + candidateId +
                    " - " + e.getMessage());
            }
        }
    }

    public JSONObject createTextFilterSearchPayload(String fieldName, String filterType, String filterValue, String dbField, String filterValue_TYPE) {
        JSONObject payload = new JSONObject();
        payload.put("advancedSearchContext", "CANDIDATE");
        payload.put("offLimitBehavior", "bypass");
        payload.put("defaultFilterList", JSONObject.NULL);
        payload.put("booleanSearchList", JSONObject.NULL);
        payload.put("sortPriorityList", new JSONArray());

        JSONObject filterValueObj;
        if ("STRING_LIST".equals(filterValue_TYPE)) {
            filterValueObj = stringListFilterValue(filterValue);
        } else {
            filterValueObj = new JSONObject();
            filterValueObj.put("type", filterValue_TYPE);
            filterValueObj.put("value", filterValue);
        }

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
        filter.put("fieldType", "text");
        filter.put("filterValue", filterValueObj);
        filtersArray.put(filter);
        groupFilterList.put("filters", filtersArray);
        groupFilterListArray.put(groupFilterList);
        filterSearchList.put("groupFilterList", groupFilterListArray);
        filterSearchList.put("groupJoinOperator", "AND");
        payload.put("filterSearchList", filterSearchList);
        return payload;
    }
    
    private boolean isRelativeDateValue(String filterValue) {
        return filterValue.equals("all_time") || filterValue.equals("today") || filterValue.equals("yesterday") ||
               filterValue.equals("this_week") || filterValue.equals("last_week") ||
               filterValue.equals("this_month") || filterValue.equals("last_month") ||
               filterValue.equals("this_quarter") || filterValue.equals("last_quarter") ||
               filterValue.equals("this_year") || filterValue.equals("last_year") ||
               filterValue.equals("last_30") || filterValue.equals("last_60") ||
               filterValue.equals("last_90") || filterValue.equals("last_365");
    }

    private String getFilterBarLabel(String filterValue) {
        switch (filterValue) {
            case "all_time": return "All Time";
            case "today": return "Today";
            case "yesterday": return "Yesterday";
            case "this_week": return "This Week";
            case "last_week": return "Last Week";
            case "this_month": return "This Month";
            case "last_month": return "Last Month";
            case "this_quarter": return "This Quarter";
            case "last_quarter": return "Last Quarter";
            case "this_year": return "This Year";
            case "last_year": return "Last Year";
            case "last_30": return "Last 30 Days";
            case "last_60": return "Last 60 Days";
            case "last_90": return "Last 90 Days";
            case "last_365": return "Last 365 Days";
            default: return filterValue;
        }
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
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");

        ExecutorService executor = Executors.newFixedThreadPool(5);
        ConcurrentMap<String, Integer> candidateIdMap = new ConcurrentHashMap<>();
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                    .filter(key -> key.startsWith("candidate"))
                    .map(candidateKey -> CompletableFuture.runAsync(() -> {
                        JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                        JSONObject payload = new JSONObject(candidateEntry.getJSONObject("payload").toString());
                        Response response = allCrudFunctions.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        int candidateId = response.jsonPath().getInt("data.candidate.id");
                        candidateIdMap.put(candidateKey, candidateId);
                        candidateIdToKeyMap.put(String.valueOf(candidateId), candidateKey);
                        candidateSlugMap.put(candidateKey, response.jsonPath().getString("data.candidate.slug"));
                        candidateKeyToIdMap.put(candidateKey, String.valueOf(candidateId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();

            List<Integer> meetingIds = createMeetingsForCandidate(candidateSlugMap);
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

            int uniqueId1 = function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson1.getString("slug"));
            int uniqueId2 = function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateJson2.getString("slug"));
            
            candidateKeyToIdMap.put("associated_candidates_candidate1", String.valueOf(uniqueId1));
            candidateKeyToIdMap.put("associated_candidates_candidate2", String.valueOf(uniqueId2));
            candidateIdToKeyMap.put(String.valueOf(uniqueId1), "associated_candidates_candidate1");
            candidateIdToKeyMap.put(String.valueOf(uniqueId2), "associated_candidates_candidate2");
        } finally {
            executor.shutdown();
        }
    }

    private List<Integer> createMeetingsForCandidate(Map<String, String> candidateSlugMap) {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_data.json");
        LinkedHashMap<String, Integer> meetingIdMap = new LinkedHashMap<>();

        List<String> sortedCandidateKeys = candidateSlugMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String candidateKey : sortedCandidateKeys) {
                if (!candidateJson.has(candidateKey)) {
                    continue;
                }
                JSONObject candidateEntry = candidateJson.getJSONObject(candidateKey);
                if (!candidateEntry.has("meetings")) {
                    continue;
                }

                final String ck = candidateKey;
                final JSONObject meetingPayload = candidateEntry.getJSONObject("meetings");

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        String candidateSlug = candidateSlugMap.get(ck);
                        Meeting meeting = processMeetingPayload(meetingPayload, candidateSlug);
                        
                        Response response = RestClient.doPost("JSON", baseURL, "meetings", accountOwnerAPIKey, null, true, meeting);
                        
                        if (response.getStatusCode() == 200) {
                            meetingIdMap.put(ck, response.jsonPath().getInt("id"));
                        } else {
                            System.err.println("Meeting creation failed for " + ck + ": " + response.getBody().asString());
                        }
                    } catch (Exception e) {
                        System.err.println("Exception creating meeting for " + ck + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        List<Integer> sortedMeetingIds = sortedCandidateKeys.stream()
                .filter(meetingIdMap::containsKey)
                .map(meetingIdMap::get)
                .collect(Collectors.toList());
        
        
        return sortedMeetingIds;
    }

    private Meeting processMeetingPayload(JSONObject payload, String candidateSlug) {
        Meeting meeting = new Meeting();
        
        meeting.setTitle(payload.optString("title", "Default Meeting Title"));
        meeting.setDescription(payload.optString("description", "Meeting description"));
        meeting.setAddress(payload.optString("address", "Office Address"));

        String startDate = getVariedStartDateForCandidate(candidateSlug);
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
        
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{candidate")) {
            meeting.setRelated_to(candidateSlug);
        } else {
            meeting.setRelated_to(payload.optString("related_to", candidateSlug));
        }
        meeting.setRelated_to_type(payload.optString("related_to_type", "candidate"));
        
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
                    
                    String candidateKey = candidateIdToKeyMap.get(String.valueOf(meetingId));

                    JSONObject fieldsAndTimestamps = new JSONObject();
                    for (Map.Entry<String, String> timestamp : timestamps.entrySet()) {
                        fieldsAndTimestamps.put(timestamp.getKey(), timestamp.getValue());
                    }

                    Response updateResponse = ReaperIntegration.updateActivityTimestamp(meetingId, fieldsAndTimestamps,"appointment");
                    if (updateResponse.getStatusCode() != 200) {
                        System.err.println("Failed to update timestamps for " + candidateKey + " (Meeting ID: " + meetingId + ")");
                        System.err.println("Response: " + updateResponse.getBody().asString());
                        Assert.fail("Failed to update the meetings timestamps");
                    } else {
                        
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }
        
        storeDateScenarioMapping(meetingIds, scenarios, limit);
    }
    
    private Map<String, String> dateScenarioMapping = new ConcurrentHashMap<>();
    
    
    private void storeDateScenarioMapping(List<Integer> meetingIds, List<Map.Entry<String, Map<String, String>>> scenarios, int limit) {
        dateScenarioMapping.clear();
        for (int i = 0; i < limit; i++) {
            String scenarioName = scenarios.get(i).getKey();
            Integer meetingId = meetingIds.get(i);
            String candidateKey = candidateIdToKeyMap.get(String.valueOf(meetingId));
            String filterValue = scenarioNameToFilterValue(scenarioName);
            
            if (candidateKey != null) {
                dateScenarioMapping.put(filterValue, candidateKey);
            }
        }
        
    }
    
    private String scenarioNameToFilterValue(String scenarioName) {
        switch (scenarioName) {
            case "today_scenario": return "today";
            case "yesterday_scenario": return "yesterday";
            case "this_week_scenario": return "this_week";
            case "last_week_scenario": return "last_week";
            case "this_month_scenario": return "this_month";
            case "last_month_scenario": return "last_month";
            case "this_quarter_scenario": return "this_quarter";
            case "last_quarter_scenario": return "last_quarter";
            case "this_year_scenario": return "this_year";
            case "last_year_scenario": return "last_year";
            case "last_30_scenario": return "last_30";
            case "last_60_scenario": return "last_60";
            case "last_90_scenario": return "last_90";
            case "last_365_scenario": return "last_365";
            case "static_date_scenario1": return "2022-06-15"; 
            case "static_date_scenario2": return "2023-03-10";
            default: return scenarioName;
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
        staticTimestamps1.put("createdOn", "1655251200"); // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("updatedOn", "1655251200"); // 2022-06-15 00:00:00 UTC
        staticTimestamps1.put("startdate", "1655251200"); // 2022-06-15 00:00:00 UTC
        scenarios.put("static_date_scenario1", staticTimestamps1);

        Map<String, String> staticTimestamps2 = new HashMap<>();
        staticTimestamps2.put("createdOn", "1678406400"); // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("updatedOn", "1678406400"); // 2023-03-10 00:00:00 UTC
        staticTimestamps2.put("startdate", "1678406400"); // 2023-03-10 00:00:00 UTC
        scenarios.put("static_date_scenario2", staticTimestamps2);

        return scenarios;
    }
    private String getVariedStartDateForCandidate(String candidateSlug) {
        String candidateKey = candidateSlugMap.entrySet().stream()
            .filter(entry -> entry.getValue().equals(candidateSlug))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse("unknown");

        switch (candidateKey) {
            case "candidate1":
                return getDateDaysFromNow(0); // Today
            case "candidate2": 
                return getDateDaysFromNow(-1); // Yesterday
            case "candidate3":
                return getDateDaysFromNow(7); // Next week
            case "candidate4":
                return getDateDaysFromNow(-7); // Last week
            case "candidate5":
                return getDateDaysFromNow(-30); // 30 days ago
            case "candidate6":
                return getDateDaysFromNow(-60); // 60 days ago
            case "candidate7":
                return getDateDaysFromNow(-90); // 90 days ago
            case "candidate8":
                return getDateDaysFromNow(-180); // 180 days ago
            case "candidate9":
                return getDateDaysFromNow(14); // 2 weeks future
            case "candidate10":
                return getDateDaysFromNow(-365); // 1 year ago
            case "candidate11":
                return getDateDaysFromNow(30); // 1 month future
            case "candidate12":
                return getDateDaysFromNow(60); // 2 months future
            case "associated_candidates_candidate1":
                return getDateDaysFromNow(0); // Today (same as candidate1)
            case "associated_candidates_candidate2":
                return getDateDaysFromNow(-45); // 45 days ago
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
