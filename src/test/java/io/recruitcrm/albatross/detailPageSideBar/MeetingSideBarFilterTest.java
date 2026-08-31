package io.recruitcrm.albatross.detailPageSideBar;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.Meeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.JavaFakerMeeting;
import io.recruitcrm.Filters.FilterSearchBaseTest;

import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import com.qa.api.util.DateUtil;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class MeetingSideBarFilterTest extends FilterSearchBaseTest {

    AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    commanFunction function;
    String accountOwnerAPIKey;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> userMap;
    Map<String, String> teamMap;
    String candidateSlug;
    int candidateId;
    Map<String, Integer> meetingIdMap = new HashMap<>();
    JavaFakerMeeting fakerMeeting;
    int defaultMeetingTypeId = 0;
    int clientMeetingTypeId = 0;
    int internalMeetingTypeId = 0;

    @BeforeClass
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        fakerMeeting = new JavaFakerMeeting();
        createAssociatedEntities(function, accountOwnerAPIKey, albatrossAuthToken, associatedEntitiesSlugMap, associatedEntitiesIdMap);
        setupMeetingTypes();
        userMap = createUserMap(accountOwnerAPIKey);
        teamMap = createTeamMap();
        createTestData();
    }

    @DataProvider(name = "meetingSideBarFiltersTestData", parallel = true)
    public Object[][] meetingSideBarFiltersDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/meetingSideBarDataProvider.json");
        List<Object[]> testData = new ArrayList<>();

        for (String key : filterData.keySet()) {
            if (!key.equals("Combined Filter Scenarios")) {
                JSONArray tests = filterData.getJSONArray(key);
                for (int i = 0; i < tests.length(); i++) {
                    JSONObject test = tests.getJSONObject(i);
                    String filter = test.getString("filter");
                    String filterValue = test.getString("filterValue");
                    testData.add(new Object[]{
                            key, filter, filterValue, test.getString("expectedResult"), test.getString("description")
                    });
                }
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "meetingSideBarCombinedFilterTestData", parallel = true)
    public Object[][] meetingSideBarCombinedFilterDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/meetingSideBarDataProvider.json");
        List<Object[]> testData = new ArrayList<>();

        JSONArray tests = filterData.getJSONArray("Combined Filter Scenarios");
        for (int i = 0; i < tests.length(); i++) {
            JSONObject test = tests.getJSONObject(i);
            testData.add(new Object[]{
                    test.getString("description"),
                    test.toString(),
                    test.getString("expectedResult")
            });
        }
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "meetingSearchTestData", parallel = true)
    public Object[][] meetingSearchDataProvider() {
        JSONObject searchData = readJsonFileFromPath("src/test/resources/meetingSearchDataProvider.json");
        List<Object[]> testData = new ArrayList<>();

        JSONArray testCases = searchData.getJSONArray("meetingSearchTestCases");
        for (int i = 0; i < testCases.length(); i++) {
            JSONObject testCase = testCases.getJSONObject(i);
            String testCaseId = testCase.getString("testCaseId");
            String description = testCase.getString("description");
            JSONArray searchTerms = testCase.getJSONArray("searchTerms");

            for (int j = 0; j < searchTerms.length(); j++) {
                JSONObject searchTerm = searchTerms.getJSONObject(j);
                String term = searchTerm.getString("term");
                JSONArray expectedResults = searchTerm.getJSONArray("expectedResults");
                StringBuilder expectedResultStr = new StringBuilder();
                for (int k = 0; k < expectedResults.length(); k++) {
                    if (k > 0) expectedResultStr.append(",");
                    expectedResultStr.append(expectedResults.getString(k));
                }
                testData.add(new Object[]{
                        testCaseId + "_" + j, "search", term, expectedResultStr.toString(),
                        description + " - Search term: '" + term + "'"
                });
            }
        }
        return testData.toArray(new Object[0][0]);
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "meetingSideBarFiltersTestData", description = "Meeting Sidebar Filter Search Test")
    public void meetingSideBarFiltersTest(String fieldName, String filter, String filterValue, String expectedResult, String description) {
        String basePath = "/expand-activity/get-activity-data";
        JSONObject payload = buildPayload(filter, filterValue);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filter: " + filter + " and filterValue: " + filterValue + " is not 200");
        JSONArray meetings = getFilteredMeetingData(response);
        if (filter.equals("createdon") || filter.equals("scheduledate")) {
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterValue));
            assertDateWithinRange(response, range[0], range[1], filter, description);
        } else {
            assertMeetingFilterResults(meetings, expectedResult, meetingIdMap, description);
            validateExpectedMeetingCount(response, meetings, description);
            if (filter.equals("meetingtype")) {
                validateMeetingTypesMatch(meetings, filterValue, description);
            } else if (filter.equals("status")) {
                validateMeetingStatusMatch(meetings, filterValue, description);
            }
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "meetingSideBarCombinedFilterTestData", description = "Meeting Sidebar Combined Filter Test")
    public void meetingSideBarCombinedFilterTest(String description, String testDataJson, String expectedResult) {
        JSONObject testData = new JSONObject(testDataJson);
        JSONObject payload = createCombinedFilterPayload(candidateSlug, testData);
        FilterSearchReporter.logPayload(payload);
        Response response = RestClient.doPost("JSON", albatrossURL, "expand-activity/get-activity-data", albatrossAuthToken, null, true, payload);

        Assert.assertEquals(response.getStatusCode(), 200, "Response code for " + description + " is not 200");
        Assert.assertEquals(response.jsonPath().get("status"), "success", "Status for " + description + " is not success");
        JSONArray meetings = getFilteredMeetingData(response);
        FilterSearchReporter.logResponse(response, meetings);
        assertMeetingFilterResults(meetings, expectedResult, meetingIdMap, description);
        validateExpectedMeetingCount(response, meetings, description);
        if (testData.has("meeting_types") && !testData.getString("meeting_types").equals("[]")) {
            validateMeetingTypesMatch(meetings, testData.getString("meeting_types"), description);
        }
        if (testData.has("meetingStatus") && !testData.getString("meetingStatus").equals("All")) {
            validateMeetingStatusMatch(meetings, testData.getString("meetingStatus"), description);
        }
        if (testData.has("createdDate")) {
            String createdDateFilter = testData.getString("createdDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(createdDateFilter));
            assertDateWithinRange(response, range[0], range[1], "createdon", description + " - created date");
        }
        if (testData.has("scheduleDate")) {
            String scheduleDateFilter = testData.getString("scheduleDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(scheduleDateFilter));
            assertDateWithinRange(response, range[0], range[1], "scheduledate", description + " - schedule date");
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "meetingSearchTestData", description = "Meeting Search Test")
    public void meetingSearchTest(String testCaseId, String filter, String searchTerm, String expectedResult, String description) {
        String basePath = "/expand-activity/get-activity-data";
        JSONObject payload = buildPayload(filter, searchTerm);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for search term: '" + searchTerm + "' is not 200");

        assertMeetingSearchResults(response, expectedResult, searchTerm, description);
    }

    public JSONObject buildPayload(String filter, String filterValue) {
        JSONObject payload = new JSONObject();
        payload.put("relatedToSlug", candidateSlug);
        payload.put("relatedtotypeid", 5);
        payload.put("type", 2);
        payload.put("page", "detailspage");
        payload.put("pagesize", 15);
        payload.put("relatedtocompany", JSONObject.NULL);
        payload.put("offset", 0);
        payload.put("isFilterApplied", 1);

        if (filter.equals("createdon") || filter.equals("scheduledate")) {
            JSONObject dateFilter = new JSONObject();
            dateFilter.put("id", filterValue);
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterValue));
            dateFilter.put("startdate", range[0]);
            dateFilter.put("enddate", range[1]);
            if (filter.equals("createdon")) {
                payload.put("createdDate", dateFilter);
            } else {
                payload.put("scheduleDate", dateFilter);
            }
        }

        if (filter.equals("meeting_associated_with")) {
            JSONObject json = new JSONObject(processFilterValue(filterValue));
            payload.put("associations", json);
        }

        if (filter.equals("userFilter")) {
            JSONObject json = new JSONObject(processFilterValue(filterValue));
            payload.put("teamFilter", json.getJSONArray("teamFilter"));
            payload.put("userFilter", json.getJSONArray("userFilter"));
            payload.put("userSlugs", json.getJSONArray("userSlugs"));
        }

        if (filter.equals("meetingtype")) {
            JSONArray array = new JSONArray(processFilterValue(filterValue));
            payload.put("meetingTypes", array);
        }

        if (filter.equals("status")) {
            if ("All".equals(filterValue)) {
                payload.put("meetingStatus", "2");
            } else if ("Complete".equals(filterValue)) {
                payload.put("meetingStatus", "1");
            } else if ("Incomplete".equals(filterValue)) {
                payload.put("meetingStatus", "0");
            }
        }

        if (filter.equals("search")) {
            payload.put("searchTerm", filterValue);
        }

        return payload;
    }

    public void assertDateWithinRange(Response response, long startEpoch, long endEpoch, String filterType, String description) {
        JSONArray meetings = getFilteredMeetingData(response);

        String dateField = filterType.equals("createdon") ? "createdon" : "startdate";
        String fieldDisplayName = filterType.equals("createdon") ? "createdon" : "startdate (schedule date)";
        for (int i = 0; i < meetings.length(); i++) {
            JSONObject meeting = meetings.getJSONObject(i);
            long dateValue = meeting.getLong(dateField);
            Assert.assertTrue(dateValue >= startEpoch && dateValue <= endEpoch, description + " failed. Meeting " + fieldDisplayName + " " + dateValue + " is not within range [" + startEpoch + ", " + endEpoch + "]");
        }
    }

    public void assertMeetingFilterResults(JSONArray meetingsArray, String expectedItems, Map<String, Integer> meetingIdMap, String description) {
        Set<Integer> actualIds = new HashSet<>();
        for (int i = 0; i < meetingsArray.length(); i++) {
            actualIds.add(meetingsArray.getJSONObject(i).getInt("id"));
        }
        Set<Integer> expectedIds = new HashSet<>();
        Set<String> expectedKeys = new HashSet<>();
        if (expectedItems != null && !expectedItems.trim().isEmpty() && !expectedItems.equals("Empty")) {
            for (String key : expectedItems.split(",")) {
                key = key.trim();
                if (!key.isEmpty()) {
                    if (meetingIdMap.containsKey(key)) {
                        expectedIds.add(meetingIdMap.get(key));
                        expectedKeys.add(key);
                    } else {
                        throw new IllegalArgumentException("Key not found in meetingIdMap: " + key);
                    }
                }
            }
        }
        Set<String> actualKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : meetingIdMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }
        Assert.assertEquals(actualIds, expectedIds,
                description + " Mismatch in meeting IDs. Expected keys=" + expectedKeys + " (IDs=" + expectedIds + "), Actual keys=" + actualKeys + " (IDs=" + actualIds + ")");
    }

    public void assertMeetingSearchResults(Response response, String expectedResult, String searchTerm, String description) {
        JSONArray meetingsArray = getFilteredMeetingData(response);

        Set<Integer> actualIds = new HashSet<>();
        for (int i = 0; i < meetingsArray.length(); i++) {
            actualIds.add(meetingsArray.getJSONObject(i).getInt("id"));
        }
        Set<Integer> expectedIds = new HashSet<>();
        Set<String> expectedKeys = new HashSet<>();
        if (expectedResult != null && !expectedResult.trim().isEmpty()) {
            for (String key : expectedResult.split(",")) {
                key = key.trim();
                if (meetingIdMap.containsKey(key)) {
                    expectedIds.add(meetingIdMap.get(key));
                    expectedKeys.add(key);
                } else {
                    throw new IllegalArgumentException("Expected key not found in meetingIdMap: " + key);
                }
            }
        }
        Set<String> actualKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : meetingIdMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }
        if (!searchTerm.trim().isEmpty()) {
            for (int i = 0; i < meetingsArray.length(); i++) {
                JSONObject item = meetingsArray.getJSONObject(i);
                String titleText = item.optString("title", "").toLowerCase();
                String descriptionText = item.optString("description", "").toLowerCase();

                String searchTermLower = searchTerm.toLowerCase();
                String normalizedSearchTerm = searchTermLower
                        .replace("\\\\n", "\n")
                        .replace("\\n", "\n")
                        .replace("\\\\t", "\t")
                        .replace("\\t", "\t");
                boolean found = titleText.contains(searchTermLower) || titleText.contains(normalizedSearchTerm) ||
                        descriptionText.contains(searchTermLower) || descriptionText.contains(normalizedSearchTerm) ||
                        containsWithDiacriticsNormalization(titleText, searchTermLower) ||
                        containsWithDiacriticsNormalization(descriptionText, searchTermLower);
                Assert.assertTrue(found,
                        description + " - Search term '" + searchTerm + "' not found in title: " + titleText + " or description: " + descriptionText);
            }
        }

        Assert.assertEquals(actualIds, expectedIds, description + " - Search term: '" + searchTerm + "' - Mismatch in IDs. " + "Expected keys=" + expectedKeys + " (IDs=" + expectedIds + "), " + "Actual keys=" + actualKeys + " (IDs=" + actualIds + ")");
    }

    private String processFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }
        String result = filterValue;
        result = replacePlaceholdersInFilterValue(result);
        return result;
    }

    private boolean containsWithDiacriticsNormalization(String text, String searchTerm) {
        String normalizedText = normalizeDiacritics(text);
        String normalizedSearchTerm = normalizeDiacritics(searchTerm);
        return normalizedText.contains(normalizedSearchTerm);
    }

    private String normalizeDiacritics(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private void validateMeetingTypesMatch(JSONArray meetingsArray, String filterValue, String description) {
        String processedFilter = replacePlaceholdersInFilterValue(filterValue);
        JSONArray filterArray = new JSONArray(processedFilter);

        if (filterArray.isEmpty()) {
            return;
        }
        Set<Integer> expectedTypes = new HashSet<>();
        for (int i = 0; i < filterArray.length(); i++) {
            expectedTypes.add(filterArray.getInt(i));
        }
        for (int i = 0; i < meetingsArray.length(); i++) {
            JSONObject meeting = meetingsArray.getJSONObject(i);
            int actualMeetingTypeId = meeting.getInt("notetype");
            Assert.assertTrue(expectedTypes.contains(actualMeetingTypeId), description + " - meeting type validation failed");
        }
    }

    private void validateMeetingStatusMatch(JSONArray meetingsArray, String filterValue, String description) {
        for (int i = 0; i < meetingsArray.length(); i++) {
            JSONObject meeting = meetingsArray.getJSONObject(i);
            long endDate = meeting.getLong("enddate");
            long currentTime = System.currentTimeMillis() / 1000;
            boolean isComplete = endDate < currentTime;
            if ("Complete".equals(filterValue)) {
                Assert.assertTrue(isComplete, description + " failed. Expected completed meeting but found incomplete one. End date: " + endDate + ", Current time: " + currentTime);
            } else if ("Incomplete".equals(filterValue)) {
                Assert.assertFalse(isComplete, description + " failed. Expected incomplete meeting but found completed one. End date: " + endDate + ", Current time: " + currentTime);
            }
        }
    }

    private void validateExpectedMeetingCount(Response response, JSONArray meetings, String description) {
        int actualFilteredCount = response.jsonPath().getInt("data.filtered_count");
        int actualMeetingsLength = meetings.length();
        Assert.assertEquals(actualFilteredCount, actualMeetingsLength,
                description + " - count validation failed. filtered_count: " + actualFilteredCount + ", meetings array length: " + actualMeetingsLength);
    }

    public JSONObject createCombinedFilterPayload(String candidateSlug, JSONObject testData) {
        JSONObject payload = createBasePayload(candidateSlug);

        if (testData.has("userFilter")) {
            String userFilterValue = replacePlaceholdersInFilterValue(testData.getString("userFilter"));
            JSONObject userFilterObj = new JSONObject(userFilterValue);
            payload.put("userFilter", userFilterObj.getJSONArray("userFilter"));
            payload.put("teamFilter", userFilterObj.getJSONArray("teamFilter"));
            payload.put("userSlugs", userFilterObj.getJSONArray("userSlugs"));
        }

        if (testData.has("meetingStatus")) {
            String statusValue = testData.getString("meetingStatus");
            if ("All".equals(statusValue)) {
                payload.put("meetingStatus", "2");
            } else if ("Complete".equals(statusValue)) {
                payload.put("meetingStatus", "1");
            } else if ("Incomplete".equals(statusValue)) {
                payload.put("meetingStatus", "0");
            } else {
                payload.put("meetingStatus", statusValue);
            }
        }

        if (testData.has("meeting_types")) {
            String meetingTypesValue = replacePlaceholdersInFilterValue(testData.getString("meeting_types"));
            payload.put("meetingTypes", new JSONArray(meetingTypesValue));
        }

        if (testData.has("createdDate")) {
            String filterId = testData.getString("createdDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterId));
            JSONObject createdDateFilter = new JSONObject();
            createdDateFilter.put("id", filterId);
            createdDateFilter.put("startdate", range[0]);
            createdDateFilter.put("enddate", range[1]);
            payload.put("createdDate", createdDateFilter);
        }

        if (testData.has("scheduleDate")) {
            String filterId = testData.getString("scheduleDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterId));
            JSONObject scheduleDateFilter = new JSONObject();
            scheduleDateFilter.put("id", filterId);
            scheduleDateFilter.put("startdate", range[0]);
            scheduleDateFilter.put("enddate", range[1]);
            payload.put("scheduleDate", scheduleDateFilter);
        }

        if (testData.has("associations")) {
            String associationsValue = replacePlaceholdersInFilterValue(testData.getString("associations"));
            if (!associationsValue.equals("{}")) {
                JSONObject associations = new JSONObject(associationsValue);
                JSONObject associationsFilter = new JSONObject();
                associationsFilter.put("2", new JSONArray());
                associationsFilter.put("3", new JSONArray());
                associationsFilter.put("4", new JSONArray());
                associationsFilter.put("5", new JSONArray());
                associationsFilter.put("11", new JSONArray());
                for (String key : associations.keySet()) {
                    associationsFilter.put(key, associations.getJSONArray(key));
                }
                payload.put("associations", associationsFilter);
            }
        }
        return payload;
    }

    private JSONObject createBasePayload(String candidateSlug) {
        JSONObject payload = new JSONObject();

        payload.put("relatedToSlug", candidateSlug);
        payload.put("relatedtotypeid", 5);
        payload.put("type", 2);
        payload.put("page", "detailspage");
        payload.put("pagesize", 15);
        payload.put("relatedtocompany", JSONObject.NULL);
        payload.put("offset", 0);
        payload.put("searchTerm", "");
        payload.put("isFilterApplied", 1);

        payload.put("userFilter", new JSONArray());
        payload.put("userSlugs", new JSONArray());
        payload.put("teamFilter", new JSONArray());
        payload.put("meetingStatus", JSONObject.NULL);
        payload.put("meetingTypes", new JSONArray());
        JSONObject createdDate = new JSONObject();
        createdDate.put("id", 0);
        createdDate.put("name", "All Time");
        createdDate.put("startdate", JSONObject.NULL);
        createdDate.put("enddate", JSONObject.NULL);
        payload.put("createdDate", createdDate);

        JSONObject scheduleDate = new JSONObject();
        scheduleDate.put("id", 0);
        scheduleDate.put("name", "All Time");
        scheduleDate.put("startdate", JSONObject.NULL);
        scheduleDate.put("enddate", JSONObject.NULL);
        payload.put("scheduleDate", scheduleDate);

        payload.put("associations", new JSONObject());
        return payload;
    }

    public JSONArray getFilteredMeetingData(Response response) {
        JSONObject json = new JSONObject(response.getBody().asString());
        return json.getJSONObject("data")
                .getJSONObject("events")
                .getJSONArray("appointments");
    }

    public void setupMeetingTypes() {
        Response response = RestClient.doGet("JSON", albatrossURL, "meetings/meeting-types", albatrossAuthToken, null, null, true);
        assert response != null;
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            List<Map<String, Object>> meetingTypes = jp.getList("data");
            if (meetingTypes != null && !meetingTypes.isEmpty()) {
                for (Map<String, Object> meetingType : meetingTypes) {
                    String label = (String) meetingType.get("label");
                    int id = (Integer) meetingType.get("id");
                    if (label != null && label.toLowerCase().contains("client")) {
                        clientMeetingTypeId = id;
                    } else if (label != null && label.toLowerCase().contains("internal")) {
                        internalMeetingTypeId = id;
                    }
                    if (defaultMeetingTypeId == 0) {
                        defaultMeetingTypeId = id;
                    }
                }
            }
        }
    }

    private Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        Map<String, List<String>> teamDefinitions = new HashMap<>();
        teamDefinitions.put("MeetingSideBarFilterTestTeam1", Arrays.asList("owner", "teamMember"));
        teamDefinitions.put("MeetingSideBarFilterTestTeam2", Arrays.asList("admin", "restricted"));
        for (Map.Entry<String, List<String>> entry : teamDefinitions.entrySet()) {
            String teamName = entry.getKey();
            List<String> userKeys = entry.getValue();
            ArrayList<String> userIds = new ArrayList<>();
            for (String key : userKeys) {
                userIds.add(userMap.get(key));
            }
            Response response = allCrudFunctions.createTeam(albatrossURL, albatrossAuthToken, teamName, userIds);
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Failed to create team: " + teamName);
            }
        }
        Response teamResponse = function.getTeams(baseURL, accountOwnerAPIKey);
        if (teamResponse.getStatusCode() == 200) {
            List<Map<String, Object>> teams = teamResponse.jsonPath().getList("");
            for (String teamName : teamDefinitions.keySet()) {
                for (Map<String, Object> team : teams) {
                    if (teamName.equalsIgnoreCase((String) team.get("team_name"))) {
                        String teamId = String.valueOf(team.get("team_id"));
                        if (teamName.contains("Team1")) {
                            teamMap.put("team1", teamId);
                        } else if (teamName.contains("Team2")) {
                            teamMap.put("team2", teamId);
                        }
                        break;
                    }
                }
            }
        }
        return teamMap;
    }

    public void createTestData() {
        JsonPath candidate = function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        candidateSlug = candidate.get("slug");
        candidateId = function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateSlug);
        meetingIdMap = createMeetingsForCandidate(candidateSlug);
        updateActivityWithTimestampScenarios(new ArrayList<>(meetingIdMap.values()), "appointment", createMeetingTimestampScenarios());
    }

    private Map<String, Integer> createMeetingsForCandidate(String candidateSlug) {
        JSONObject meetingsJson = readJsonFileFromPath("src/test/resources/meeting_sidebar_data.json");
        for (String meetingKey : meetingsJson.keySet()) {
            JSONObject meetingContainer = meetingsJson.getJSONObject(meetingKey);
            JSONArray meetingsArray = meetingContainer.getJSONArray("meetings");
            if (!meetingsArray.isEmpty()) {
                JSONObject meetingPayload = meetingsArray.getJSONObject(0);
                Meeting meeting = processMeetingPayload(meetingPayload, candidateSlug);
                Response response = RestClient.doPost("JSON", baseURL, "meetings", accountOwnerAPIKey, null, true, meeting);
                if (response.getStatusCode() == 200) {
                    int meetingId = response.jsonPath().getInt("id");
                    meetingIdMap.put(meetingKey, meetingId);
                }
            }
        }
        return meetingIdMap;
    }

    private Meeting processMeetingPayload(JSONObject payload, String candidateSlug) {
        Meeting meeting = new Meeting();
        meeting.setTitle(replacePlaceholders(payload.getString("title")));
        meeting.setDescription(replacePlaceholders(payload.optString("description", "")));
        meeting.setAddress(payload.optString("address", ""));
        if (payload.has("related_to") && payload.getString("related_to").startsWith("{")) {
            meeting.setRelated_to(candidateSlug);
        } else {
            meeting.setRelated_to(payload.optString("related_to", candidateSlug));
        }
        meeting.setRelated_to_type(payload.optString("related_to_type", "candidate"));
        String originalMeetingTypeStr = payload.getString("meeting_type_id");
        if (originalMeetingTypeStr.contains("client")) {
            meeting.setMeeting_type_id(clientMeetingTypeId);
        } else if (originalMeetingTypeStr.contains("internal")) {
            meeting.setMeeting_type_id(internalMeetingTypeId);
        }
        String startDateStr = payload.optString("start_date", "Today_MeetingStartDate");
        String endDateStr = payload.optString("end_date", "Today_MeetingEndDate");
        String startDate = convertDatePlaceholderToDateString(startDateStr);
        String endDate = convertDatePlaceholderToDateString(endDateStr);
        meeting.setStart_date(startDate);
        meeting.setEnd_date(endDate);
        meeting.setReminder(payload.optInt("reminder", 30));
        if (payload.has("owner_id") && userMap != null) {
            String ownerId = payload.getString("owner_id").replace("{", "").replace("}", "");
            String ownerIdStr = userMap.get(ownerId);
            if (ownerIdStr != null) {
                meeting.setOwner_id(Integer.parseInt(ownerIdStr));
            }
        }

        if (payload.has("created_by") && userMap != null) {
            String createdBy = payload.getString("created_by").replace("{", "").replace("}", "");
            String createdByIdStr = userMap.get(createdBy);
            if (createdByIdStr != null) {
                meeting.setCreated_by(Integer.parseInt(createdByIdStr));
            }
        }

        processAssociatedEntityField(payload, "associated_candidates", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_companies", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_contacts", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_jobs", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_deals", associatedEntitiesSlugMap);

        meeting.setAssociated_candidates(payload.optString("associated_candidates", ""));
        meeting.setAssociated_companies(payload.optString("associated_companies", ""));
        meeting.setAssociated_contacts(payload.optString("associated_contacts", ""));
        meeting.setAssociated_jobs(payload.optString("associated_jobs", ""));
        meeting.setAssociated_deals(payload.optString("associated_deals", ""));

        processCollaboratorField(payload, "collaborators", userMap);
        processCollaboratorField(payload, "collaborator_team_ids", teamMap);

        meeting.setCollaborator_user_ids(payload.optString("collaborators", ""));
        meeting.setCollaborator_team_ids(payload.optString("collaborator_team_ids", ""));

        meeting.setEnable_auto_populate_teams(payload.optInt("enable_auto_populate_teams", 0));

        return meeting;
    }

    private String convertDatePlaceholderToDateString(String placeholder) {
        if (placeholder.contains("_MeetingStartDate") || placeholder.contains("_MeetingEndDate")) {
            String scenario = placeholder.split("_Meeting")[0].toLowerCase();
            boolean isEndDate = placeholder.contains("_MeetingEndDate");

            String baseDate = getDateStringForScenario(scenario);

            if (isEndDate) {
                return addMinutesToDateString(baseDate, 30);
            }

            return baseDate;
        }

        return DateUtil.getTodayDateString();
    }

    private String getDateStringForScenario(String scenario) {
        switch (scenario) {
            case "today":
                return DateUtil.getTodayDateString("yyyy-MM-dd");
            case "yesterday":
                return DateUtil.getYesterdayDateString("yyyy-MM-dd");
            case "tomorrow":
                return DateUtil.getTomorrowDateString("yyyy-MM-dd");
            case "thisweek":
                return DateUtil.getThisWeekDateString(); // Already returns "yyyy-MM-dd"
            case "lastweek":
                return DateUtil.getLastWeekDateString(); // Already returns "yyyy-MM-dd"
            case "nextweek":
                return DateUtil.getNextWeekDateString(); // Already returns "yyyy-MM-dd"
            case "thismonth":
                return DateUtil.getThisMonthDateString(); // Already returns "yyyy-MM-dd"
            case "lastmonth":
                return DateUtil.getLastMonthDateString(); // Already returns "yyyy-MM-dd"
            case "nextmonth":
                return DateUtil.getNextMonthDateString(); // Already returns "yyyy-MM-dd"
            default:
                return DateUtil.getTodayDateString("yyyy-MM-dd");
        }
    }

    private String addMinutesToDateString(String dateString, int minutes) {
        java.time.LocalDateTime dateTime;
        dateTime = java.time.LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay().plusMinutes(minutes);
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String replacePlaceholders(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String result = value;

        if (userMap != null) {
            result = result.replace("{owner}", userMap.get("owner"));
            result = result.replace("{admin}", userMap.get("admin"));
            result = result.replace("{teamMember}", userMap.get("teamMember"));
            result = result.replace("{restricted}", userMap.get("restricted"));
        }

        if (teamMap != null) {
            result = result.replace("{team}", teamMap.get("team1"));
            result = result.replace("{team2}", teamMap.get("team2"));
        }

        for (Map.Entry<String, String> entry : associatedEntitiesSlugMap.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        result = result.replace("{{meeting_type_client}}", String.valueOf(clientMeetingTypeId));
        result = result.replace("{{meeting_type_internal}}", String.valueOf(internalMeetingTypeId));

        result = result.replace("{main_candidate}", candidateSlug);
        return result;
    }

    private Map<String, Map<String, String>> createMeetingTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        String todayEpoch = DateUtil.getEpochForDateScenario("today");
        Map<String, String> scenario1 = new HashMap<>();
        scenario1.put("createdOn", todayEpoch);
        scenario1.put("updatedOn", todayEpoch);
        scenarios.put("meeting1_scenario", scenario1);

        String yesterdayEpoch = DateUtil.getEpochForDateScenario("yesterday");
        Map<String, String> scenario2 = new HashMap<>();
        scenario2.put("createdOn", yesterdayEpoch);
        scenario2.put("updatedOn", yesterdayEpoch);
        scenarios.put("meeting2_scenario", scenario2);

        Map<String, String> scenario3 = new HashMap<>();
        scenario3.put("createdOn", todayEpoch);
        scenario3.put("updatedOn", todayEpoch);
        scenarios.put("meeting3_scenario", scenario3);

        String lastMonthEpoch = DateUtil.getEpochForDateScenario("last_month");
        Map<String, String> scenario4 = new HashMap<>();
        scenario4.put("createdOn", lastMonthEpoch);
        scenario4.put("updatedOn", lastMonthEpoch);
        scenarios.put("meeting4_scenario", scenario4);

        Map<String, String> scenario5 = new HashMap<>();
        scenario5.put("createdOn", todayEpoch);
        scenario5.put("updatedOn", todayEpoch);
        scenarios.put("meeting5_scenario", scenario5);

        String lastWeekEpoch = DateUtil.getEpochForDateScenario("last_week");
        Map<String, String> scenario6 = new HashMap<>();
        scenario6.put("createdOn", lastWeekEpoch);
        scenario6.put("updatedOn", lastWeekEpoch);
        scenarios.put("meeting6_scenario", scenario6);


        Map<String, String> scenario7 = new HashMap<>();
        scenario7.put("createdOn", todayEpoch);
        scenario7.put("updatedOn", todayEpoch);
        scenarios.put("meeting7_scenario", scenario7);

        Map<String, String> scenario8 = new HashMap<>();
        scenario8.put("createdOn", todayEpoch);
        scenario8.put("updatedOn", todayEpoch);
        scenarios.put("meeting8_scenario", scenario8);

        Map<String, String> scenario9 = new HashMap<>();
        scenario9.put("createdOn", todayEpoch);
        scenario9.put("updatedOn", todayEpoch);
        scenarios.put("meeting9_scenario", scenario9);

        Map<String, String> scenario10 = new HashMap<>();
        scenario10.put("createdOn", yesterdayEpoch);
        scenario10.put("updatedOn", yesterdayEpoch);
        scenarios.put("meeting10_scenario", scenario10);

        Map<String, String> scenario11 = new HashMap<>();
        scenario11.put("createdOn", todayEpoch);
        scenario11.put("updatedOn", todayEpoch);
        scenarios.put("meeting11_scenario", scenario11);

        Map<String, String> scenario12 = new HashMap<>();
        scenario12.put("createdOn", todayEpoch);
        scenario12.put("updatedOn", todayEpoch);
        scenarios.put("meeting12_scenario", scenario12);

        return scenarios;
    }

    public String replacePlaceholdersInFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String result = filterValue;
        result = result.replace("{{meeting_type_client}}", String.valueOf(clientMeetingTypeId));
        result = result.replace("{{meeting_type_internal}}", String.valueOf(internalMeetingTypeId));
        result = result.replace("{{Not Available}}", String.valueOf(defaultMeetingTypeId));
        if (userMap != null) {
            result = result.replace("{{owner_user_id}}", userMap.get("owner"));
            result = result.replace("{{admin_user_id}}", userMap.get("admin"));
            result = result.replace("{{teamMember_user_id}}", userMap.get("teamMember"));
            result = result.replace("{{restricted_user_id}}", userMap.get("restricted"));
            result = result.replace("{{owner}}", userMap.get("owner"));
            result = result.replace("{{admin}}", userMap.get("admin"));
            result = result.replace("{{teamMember}}", userMap.get("teamMember"));
            result = result.replace("{{restricted}}", userMap.get("restricted"));
            result = result.replace("{{owner_user_slug}}", "Owner");
            result = result.replace("{{admin_user_slug}}", "Admin");
            result = result.replace("{{teamMember_user_slug}}", "TeamMember");
            result = result.replace("{{userSlugOwner}}", "Owner");
            result = result.replace("{{userSlugAdmin}}", "Admin");
            result = result.replace("{{userSlugTeamMember}}", "TeamMember");
        }
        if (teamMap != null) {
            result = result.replace("{{team1}}", teamMap.get("team1"));
            result = result.replace("{{team2}}", teamMap.get("team2"));
        }
        for (Map.Entry<String, String> entry : associatedEntitiesSlugMap.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : associatedEntitiesIdMap.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        result = result.replace("{{test_candidate_slug}}", candidateSlug);
        result = result.replaceAll("\\{\\{[^}]+\\}\\}", "\"\"");
        return result;
    }
}
