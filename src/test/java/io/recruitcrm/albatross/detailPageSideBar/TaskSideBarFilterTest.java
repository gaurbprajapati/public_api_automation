package io.recruitcrm.albatross.detailPageSideBar;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerTask;
import io.rcrm.api.listeners.FilterSearchReporter;
import io.rcrm.api.pojo.Task;
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

import java.text.Normalizer;
import java.util.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TaskSideBarFilterTest extends FilterSearchBaseTest {

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
    Map<String, Integer> taskIdMap = new HashMap<>();
    JavaFakerTask fakerTask;
    int defaultTaskTypeId = 0;
    int followUpTaskTypeId = 0;
    int interviewSchedulingTaskTypeId = 0;


    @BeforeClass
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        fakerTask = new JavaFakerTask();
        createAssociatedEntities(function, accountOwnerAPIKey, albatrossAuthToken, associatedEntitiesSlugMap, associatedEntitiesIdMap);
        setupTaskTypes();
        userMap = createUserMap(accountOwnerAPIKey);
        teamMap = createTeamMap();
        createTestData();
        updateTasksToCompleteStatus();
    }

    @DataProvider(name = "taskSideBarFiltersTestData", parallel = true)
    public Object[][] taskSideBarFiltersDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/taskSideBarFilterDataProvider.json");
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

    @DataProvider(name = "taskSideBarCombinedFilterTestData", parallel = true)
    public Object[][] taskSideBarCombinedFilterDataProvider() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/taskSideBarFilterDataProvider.json");
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

    @DataProvider(name = "taskSearchTestData", parallel = true)
    public Object[][] taskSearchDataProvider() {
        JSONObject searchData = readJsonFileFromPath("src/test/resources/taskSearchDataProvider.json");
        List<Object[]> testData = new ArrayList<>();

        JSONArray testCases = searchData.getJSONArray("taskSearchTestCases");
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
    @Test(dataProvider = "taskSideBarFiltersTestData", description = "Task Sidebar Filter Search Test")
    public void taskSideBarFiltersTest(String fieldName, String filter, String filterValue, String expectedResult, String description) {
        String basePath = "/expand-activity/get-activity-data";
        JSONObject payload = buildPayload(filter, filterValue);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filter: " + filter + " and filterValue: " + filterValue + " is not 200");
        JSONArray tasks = getFilteredTaskData(response);

        if (filter.equals("createdon") || filter.equals("duedate")) {
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterValue));
            assertDateWithinRange(response, range[0], range[1], filter, description);
        } else {
            assertTaskFilterResults(tasks, expectedResult, taskIdMap, description);
            validateExpectedTaskCount(response, tasks, description);

            if (filter.equals("tasktype")) {
                validateTaskTypesMatch(tasks, filterValue, description);
            } else if (filter.equals("status")) {
                validateTaskStatusMatch(tasks, filterValue, description);
            }
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "taskSideBarCombinedFilterTestData", description = "Task Sidebar Combined Filter Test")
    public void taskSideBarCombinedFilterTest(String description, String testDataJson, String expectedResult) {
        JSONObject testData = new JSONObject(testDataJson);
        JSONObject payload = createCombinedFilterPayload(candidateSlug, testData);
        FilterSearchReporter.logPayload(payload);

        Response response = RestClient.doPost("JSON", albatrossURL, "expand-activity/get-activity-data", albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for " + description + " is not 200");
        Assert.assertEquals(response.jsonPath().get("status"), "success", "Status for " + description + " is not success");

        JSONArray tasks = getFilteredTaskData(response);
        FilterSearchReporter.logResponse(response, tasks);

        assertTaskFilterResults(tasks, expectedResult, taskIdMap, description);
        validateExpectedTaskCount(response, tasks, description);
        if (testData.has("task_types") && !testData.getString("task_types").equals("[]")) {
            validateTaskTypesMatch(tasks, testData.getString("task_types"), description);
        }
        if (testData.has("taskStatus") && !testData.getString("taskStatus").equals("All")) {
            validateTaskStatusMatch(tasks, testData.getString("taskStatus"), description);
        }
        if (testData.has("createdDate")) {
            String createdDateFilter = testData.getString("createdDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(createdDateFilter));
            assertDateWithinRange(response, range[0], range[1], "createdon", description + " - created date");
        }
        if (testData.has("dueDate")) {
            String dueDateFilter = testData.getString("dueDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(dueDateFilter));
            assertDateWithinRange(response, range[0], range[1], "duedate", description + " - due date");
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "taskSearchTestData", description = "Task Search Test")
    public void taskSearchTest(String testCaseId, String filter, String searchTerm, String expectedResult, String description) {
        String basePath = "/expand-activity/get-activity-data";
        JSONObject payload = buildPayload(filter, searchTerm);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200,
                "Response code for search term: '" + searchTerm + "' is not 200");

        assertTaskSearchResults(response, expectedResult, searchTerm, description);
    }

    public JSONObject buildPayload(String filter, String filterValue) {
        JSONObject payload = new JSONObject();

        // Base payload
        payload.put("relatedToSlug", candidateSlug);
        payload.put("relatedtotypeid", 5);  // Candidate entity type
        payload.put("type", 1);  // Tasks - MUST be integer, not string
        payload.put("page", "detailspage");
        payload.put("pageSize", 15);
        payload.put("relatedtocompany", JSONObject.NULL);
        payload.put("offset", 0);
        payload.put("isFilterApplied", 1);

        if (filter.equals("createdon") || filter.equals("duedate")) {
            JSONObject dateFilter = new JSONObject();
            dateFilter.put("id", filterValue);

            long[] range = DateUtil.getDateRange(Integer.parseInt(filterValue));
            dateFilter.put("startdate", range[0]);
            dateFilter.put("enddate", range[1]);

            if (filter.equals("createdon")) {
                payload.put("createdDate", dateFilter);
            } else {
                payload.put("dueDate", dateFilter);
            }
        }

        if (filter.equals("task_associated_with")) {
            JSONObject json = new JSONObject(processFilterValue(filterValue));
            payload.put("associations", json);
        }

        if (filter.equals("userFilter")) {
            JSONObject json = new JSONObject(processFilterValue(filterValue));
            payload.put("teamFilter", json.getJSONArray("teamFilter"));
            payload.put("userFilter", json.getJSONArray("userFilter"));
            payload.put("userSlugs", json.getJSONArray("userSlugs"));
        }

        if (filter.equals("tasktype")) {
            JSONArray array = new JSONArray(processFilterValue(filterValue));
            payload.put("task_types", array);
        }

        if (filter.equals("status")) {
            if ("All".equals(filterValue)) {
                payload.put("taskStatus", "2");
            } else if ("Complete".equals(filterValue)) {
                payload.put("taskStatus", "1");
            } else if ("Incomplete".equals(filterValue)) {
                payload.put("taskStatus", "0");
            }
        }

        if (filter.equals("search")) {
            payload.put("searchTerm", filterValue);
        }

        return payload;
    }

    public void assertDateWithinRange(Response response, long startEpoch, long endEpoch, String filterType, String description) {
        JSONArray tasks = getFilteredTaskData(response);

        String dateField = filterType.equals("createdon") ? "createdon" : "startdate";
        String fieldDisplayName = filterType.equals("createdon") ? "createdon" : "startdate (due date)";
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            long dateValue = task.getLong(dateField);
            Assert.assertTrue(dateValue >= startEpoch && dateValue <= endEpoch, description + " failed. Task " + fieldDisplayName + " " + dateValue + " is not within range [" + startEpoch + ", " + endEpoch + "]");
        }
    }

    public void assertTaskFilterResults(JSONArray tasksArray, String expectedItems, Map<String, Integer> taskIdMap, String description) {
        Set<Integer> actualIds = new HashSet<>();
        for (int i = 0; i < tasksArray.length(); i++) {
            actualIds.add(tasksArray.getJSONObject(i).getInt("id"));
        }
        Set<Integer> expectedIds = new HashSet<>();
        Set<String> expectedKeys = new HashSet<>();

        // Handle empty expected results
        if (expectedItems != null && !expectedItems.trim().isEmpty()) {
            for (String key : expectedItems.split(",")) {
                key = key.trim();
                if (!key.isEmpty()) {  // Skip empty keys
                    if (taskIdMap.containsKey(key)) {
                        expectedIds.add(taskIdMap.get(key));
                        expectedKeys.add(key);
                    } else {
                        throw new IllegalArgumentException("Key not found in taskIdMap: " + key);
                    }
                }
            }
        }
        Set<String> actualKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : taskIdMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }
        Assert.assertEquals(actualIds, expectedIds,
                description + " Mismatch in task IDs. Expected keys=" + expectedKeys + " (IDs=" + expectedIds + "), Actual keys=" + actualKeys + " (IDs=" + actualIds + ")");
    }

    public void assertTaskSearchResults(Response response, String expectedResult, String searchTerm, String description) {
        JSONArray tasksArray = getFilteredTaskData(response);

        // Get actual task IDs from response
        Set<Integer> actualIds = new HashSet<>();
        for (int i = 0; i < tasksArray.length(); i++) {
            actualIds.add(tasksArray.getJSONObject(i).getInt("id"));
        }

        // Get expected task IDs
        Set<Integer> expectedIds = new HashSet<>();
        Set<String> expectedKeys = new HashSet<>();

        if (expectedResult != null && !expectedResult.trim().isEmpty()) {
            for (String key : expectedResult.split(",")) {
                key = key.trim();
                if (taskIdMap.containsKey(key)) {
                    expectedIds.add(taskIdMap.get(key));
                    expectedKeys.add(key);
                } else {
                    throw new IllegalArgumentException("Expected key not found in taskIdMap: " + key);
                }
            }
        }

        // Get actual task keys for better error reporting
        Set<String> actualKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : taskIdMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }

        // Validate that search term appears in the title or description of returned tasks
        if (!searchTerm.trim().isEmpty()) {
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject item = tasksArray.getJSONObject(i);
                String titleText = item.optString("title", "").toLowerCase();
                String descriptionText = item.optString("description", "").toLowerCase();

                String searchTermLower = searchTerm.toLowerCase();

                // Handle multiple levels of escaping in search term
                String normalizedSearchTerm = searchTermLower
                        .replace("\\\\n", "\n")  // \\n -> \n
                        .replace("\\n", "\n")    // \n -> \n
                        .replace("\\\\t", "\t")  // \\t -> \t
                        .replace("\\t", "\t");   // \t -> \t

                // Check if search term appears in title or description (case-insensitive)
                // Also handle diacritics normalization (e.g., résumé should match resume)
                boolean found = titleText.contains(searchTermLower) || titleText.contains(normalizedSearchTerm) ||
                        descriptionText.contains(searchTermLower) || descriptionText.contains(normalizedSearchTerm) ||
                        containsWithDiacriticsNormalization(titleText, searchTermLower) ||
                        containsWithDiacriticsNormalization(descriptionText, searchTermLower);
                Assert.assertTrue(found,
                        description + " - Search term '" + searchTerm + "' not found in title: " + titleText + " or description: " + descriptionText);
            }
        }

        // Validate expected vs actual results
        Assert.assertEquals(actualIds, expectedIds,
                description + " - Search term: '" + searchTerm + "' - Mismatch in IDs. " +
                        "Expected keys=" + expectedKeys + " (IDs=" + expectedIds + "), " +
                        "Actual keys=" + actualKeys + " (IDs=" + actualIds + ")");
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
        // Simple diacritics normalization - remove common accents
        String normalizedText = normalizeDiacritics(text);
        String normalizedSearchTerm = normalizeDiacritics(searchTerm);
        return normalizedText.contains(normalizedSearchTerm);
    }

    private String normalizeDiacritics(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    private void validateTaskTypesMatch(JSONArray tasksArray, String filterValue, String description) {
        String processedFilter = replacePlaceholdersInFilterValue(filterValue);
        JSONArray filterArray = new JSONArray(processedFilter);

        if (filterArray.isEmpty()) {
            return;
        }
        Set<Integer> expectedTypes = new HashSet<>();
        for (int i = 0; i < filterArray.length(); i++) {
            expectedTypes.add(filterArray.getInt(i));
        }
        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject task = tasksArray.getJSONObject(i);
            int actualTaskTypeId = task.getInt("notetype");
            Assert.assertTrue(expectedTypes.contains(actualTaskTypeId), description + " - task type validation failed");
        }
    }

    private void validateTaskStatusMatch(JSONArray tasksArray, String filterValue, String description) {
        // Map string values to numeric values
        String mappedValue = filterValue;
        if ("All".equals(filterValue)) {
            mappedValue = "2";
        } else if ("Complete".equals(filterValue)) {
            mappedValue = "1";
        } else if ("Incomplete".equals(filterValue)) {
            mappedValue = "0";
        }

        int filterStatusValue = Integer.parseInt(mappedValue);
        for (int i = 0; i < tasksArray.length(); i++) {
            JSONObject task = tasksArray.getJSONObject(i);
            int actualTaskStatus = task.getInt("status");
            if (filterStatusValue == 2) {
                Assert.assertTrue(actualTaskStatus == 0 || actualTaskStatus == 1, description + " failed. Expected: 0 or 1, Actual: " + actualTaskStatus);
            } else {
                Assert.assertEquals(actualTaskStatus, filterStatusValue, description + " failed. Expected: " + filterStatusValue + ", Actual: " + actualTaskStatus);
            }
        }
    }

    private void validateExpectedTaskCount(Response response, JSONArray tasks, String description) {
        // Get the actual filtered count from API response
        int actualFilteredCount = response.jsonPath().getInt("data.filtered_count");
        int actualTasksLength = tasks.length();
        Assert.assertEquals(actualFilteredCount, actualTasksLength,
                description + " - count validation failed. filtered_count: " + actualFilteredCount + ", tasks array length: " + actualTasksLength);
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

        if (testData.has("taskStatus")) {
            String statusValue = testData.getString("taskStatus");
            if ("All".equals(statusValue)) {
                payload.put("taskStatus", "2");
            } else if ("Complete".equals(statusValue)) {
                payload.put("taskStatus", "1");
            } else if ("Incomplete".equals(statusValue)) {
                payload.put("taskStatus", "0");
            } else {
                payload.put("taskStatus", statusValue);
            }
        }

        if (testData.has("task_types")) {
            String taskTypesValue = replacePlaceholdersInFilterValue(testData.getString("task_types"));
            payload.put("task_types", new JSONArray(taskTypesValue));
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

        if (testData.has("dueDate")) {
            String filterId = testData.getString("dueDate");
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterId));
            JSONObject dueDateFilter = new JSONObject();
            dueDateFilter.put("id", filterId);
            dueDateFilter.put("startdate", range[0]);
            dueDateFilter.put("enddate", range[1]);
            payload.put("dueDate", dueDateFilter);
        }

        if (testData.has("associations")) {
            String associationsValue = replacePlaceholdersInFilterValue(testData.getString("associations"));
            if (!associationsValue.equals("{}")) {
                JSONObject associations = new JSONObject(associationsValue);
                JSONObject associationsFilter = new JSONObject();
                associationsFilter.put("2", new JSONArray()); // contacts
                associationsFilter.put("3", new JSONArray()); // companies
                associationsFilter.put("4", new JSONArray()); // jobs
                associationsFilter.put("5", new JSONArray()); // candidates
                associationsFilter.put("11", new JSONArray()); // deals
                // Merge with filter values
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

        // Base payload structure
        payload.put("relatedToSlug", candidateSlug);
        payload.put("relatedtotypeid", 5);  // Candidate entity type
        payload.put("type", 1);  // Tasks - MUST be integer, not string
        payload.put("page", "detailspage");
        payload.put("pagesize", 15);
        payload.put("relatedtocompany", JSONObject.NULL);
        payload.put("offset", 0);
        payload.put("searchTerm", "");
        payload.put("isFilterApplied", 1);

        // Default filter values
        payload.put("userFilter", new JSONArray());
        payload.put("userSlugs", new JSONArray());
        payload.put("teamFilter", new JSONArray());
        payload.put("taskStatus", JSONObject.NULL);
        payload.put("task_types", new JSONArray());

        // Create proper date filter structure with name field
        JSONObject createdDate = new JSONObject();
        createdDate.put("id", 0);
        createdDate.put("name", "All Time");
        createdDate.put("startdate", JSONObject.NULL);
        createdDate.put("enddate", JSONObject.NULL);
        payload.put("createdDate", createdDate);

        JSONObject dueDate = new JSONObject();
        dueDate.put("id", 0);
        dueDate.put("name", "All Time");
        dueDate.put("startdate", JSONObject.NULL);
        dueDate.put("enddate", JSONObject.NULL);
        payload.put("dueDate", dueDate);

        // Create empty associations object (not with empty arrays) - this is correct format when no associations are filtered
        payload.put("associations", new JSONObject());
        return payload;
    }

    public JSONArray getFilteredTaskData(Response response) {
        JSONObject json = new JSONObject(response.getBody().asString());
        return json.getJSONObject("data")
                .getJSONObject("events")
                .getJSONArray("tasks");
    }


    public void setupTaskTypes() {
        Response response = RestClient.doGet("JSON", albatrossURL, "task-types", albatrossAuthToken, null, null, true);
        assert response != null;
        if (response.getStatusCode() == 200) {
            JsonPath jp = response.jsonPath();
            List<Map<String, Object>> taskTypes = jp.getList("data");

            if (taskTypes != null && !taskTypes.isEmpty()) {
                for (Map<String, Object> taskType : taskTypes) {
                    String label = (String) taskType.get("label");
                    int id = (Integer) taskType.get("id");

                    if ("Follow up".equals(label)) {
                        followUpTaskTypeId = id;
                    } else if ("Interview scheduling".equals(label)) {
                        interviewSchedulingTaskTypeId = id;
                    }
                    if (defaultTaskTypeId == 0) {
                        defaultTaskTypeId = id;
                    }
                }
            }
        }
    }

    private Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();
        Map<String, List<String>> teamDefinitions = new HashMap<>();
        teamDefinitions.put("TaskSideBarFilterTestTeam1", Arrays.asList("owner", "teamMember"));
        teamDefinitions.put("TaskSideBarFilterTestTeam2", Arrays.asList("admin", "restricted"));

        // Create teams dynamically
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
        taskIdMap = createTasksForCandidate(candidateSlug);
        updateActivityWithTimestampScenarios(new ArrayList<>(taskIdMap.values()), "task", createTaskTimestampScenarios());
    }

    private Map<String, Integer> createTasksForCandidate(String candidateSlug) {
        JSONObject tasksJson = readJsonFileFromPath("src/test/resources/task_sidebar_data.json");
        for (String taskKey : tasksJson.keySet()) {
            JSONObject taskContainer = tasksJson.getJSONObject(taskKey);
            JSONArray tasksArray = taskContainer.getJSONArray("tasks");

            // Process the first task in the tasks array
            if (!tasksArray.isEmpty()) {
                JSONObject taskPayload = tasksArray.getJSONObject(0);
                Task task = processTaskPayload(taskPayload, candidateSlug);
                Response response = RestClient.doPost("JSON", baseURL, "tasks", accountOwnerAPIKey, null, true, task);
                if (response.getStatusCode() == 200) {
                    int taskId = response.jsonPath().getInt("id");
                    taskIdMap.put(taskKey, taskId);
                }
            }
        }
        return taskIdMap;
    }


    private Task processTaskPayload(JSONObject payload, String candidateSlug) {
        Task task = new Task();

        task.setTitle(replacePlaceholders(payload.getString("title")));
        task.setDescription(replacePlaceholders(payload.optString("description", "")));

        if (payload.has("related_to") && payload.getString("related_to").startsWith("{")) {
            task.setRelated_to(candidateSlug);  // Use the actual candidate slug for linking
        } else {
            task.setRelated_to(payload.optString("related_to", candidateSlug));
        }
        task.setRelated_to_type(payload.optString("related_to_type", "candidate"));

        // Process task type with placeholder replacement - CHECK BEFORE REPLACEMENT!
        String originalTaskTypeStr = payload.getString("task_type_id");
        if (originalTaskTypeStr.contains("follow_up")) {
            task.setTask_type_id(followUpTaskTypeId);
        } else if (originalTaskTypeStr.contains("interview_scheduling")) {
            task.setTask_type_id(interviewSchedulingTaskTypeId);
        }

        // Process dates
        String startDateStr = payload.optString("start_date", "FUTURE_DATE");
        if ("FUTURE_DATE".equals(startDateStr)) {
            task.setStart_date(fakerTask.getFutureDate());
        } else if ("PAST_DATE".equals(startDateStr)) {
            task.setStart_date(fakerTask.getPastDate());
        } else {
            task.setStart_date(fakerTask.getFutureDate());
        }

        // Process reminder
        task.setReminder(payload.optInt("reminder", 30));

        // Process owner and creator
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

        // Process associations using base class method
        processAssociatedEntityField(payload, "associated_candidates", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_companies", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_contacts", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_jobs", associatedEntitiesSlugMap);
        processAssociatedEntityField(payload, "associated_deals", associatedEntitiesSlugMap);

        // Set the processed values from payload
        task.setAssociated_candidates(payload.optString("associated_candidates", ""));
        task.setAssociated_companies(payload.optString("associated_companies", ""));
        task.setAssociated_contacts(payload.optString("associated_contacts", ""));
        task.setAssociated_jobs(payload.optString("associated_jobs", ""));
        task.setAssociated_deals(payload.optString("associated_deals", ""));

        // Process collaborators using base class methods
        processCollaboratorField(payload, "collaborators", userMap);
        processCollaboratorField(payload, "collaborator_team_ids", teamMap);

        // Set the processed values from payload
        task.setCollaborators(payload.optString("collaborators", ""));
        task.setCollaborator_team_ids(payload.optString("collaborator_team_ids", ""));

        // Process other fields
        task.setEnable_auto_populate_teams(payload.optInt("enable_auto_populate_teams", 0));

        if (payload.has("updated_by")) {
            String updatedByStr = replacePlaceholders(payload.getString("updated_by"));
            try {
                task.setUpdated_by(Integer.parseInt(updatedByStr));
            } catch (NumberFormatException e) {
                // Skip if invalid
            }
        }
        return task;
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

        result = result.replace("{{task_type_follow_up}}", String.valueOf(followUpTaskTypeId));
        result = result.replace("{{task_type_interview_scheduling}}", String.valueOf(interviewSchedulingTaskTypeId));

        result = result.replace("{main_candidate}", candidateSlug);
        return result;
    }

    private Map<String, Map<String, String>> createTaskTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        String todayEpoch = DateUtil.getEpochForDateScenario("today");
        Map<String, String> scenario1 = new HashMap<>();
        scenario1.put("createdOn", todayEpoch);
        scenario1.put("updatedOn", todayEpoch);
        scenario1.put("startdate", todayEpoch);
        scenarios.put("task1_scenario", scenario1);

        String yesterdayEpoch = DateUtil.getEpochForDateScenario("yesterday");
        Map<String, String> scenario2 = new HashMap<>();
        scenario2.put("createdOn", yesterdayEpoch);
        scenario2.put("updatedOn", yesterdayEpoch);
        scenario2.put("startdate", todayEpoch);
        scenarios.put("task2_scenario", scenario2);

        Map<String, String> scenario3 = new HashMap<>();
        scenario3.put("createdOn", todayEpoch);
        scenario3.put("updatedOn", todayEpoch);
        scenario3.put("startdate", todayEpoch);
        scenarios.put("task3_scenario", scenario3);

        String lastMonthEpoch = DateUtil.getEpochForDateScenario("last_month");
        Map<String, String> scenario4 = new HashMap<>();
        scenario4.put("createdOn", lastMonthEpoch);
        scenario4.put("updatedOn", lastMonthEpoch);
        scenario4.put("startdate", lastMonthEpoch);
        scenarios.put("task4_scenario", scenario4);

        String tomorrowEpoch = DateUtil.getEpochForDateScenario("tomorrow");
        Map<String, String> scenario5 = new HashMap<>();
        scenario5.put("createdOn", todayEpoch);
        scenario5.put("updatedOn", todayEpoch);
        scenario5.put("startdate", tomorrowEpoch);
        scenarios.put("task5_scenario", scenario5);

        String lastWeekEpoch = DateUtil.getEpochForDateScenario("last_week");
        Map<String, String> scenario6 = new HashMap<>();
        scenario6.put("createdOn", lastWeekEpoch);
        scenario6.put("updatedOn", lastWeekEpoch);
        scenario6.put("startdate", lastWeekEpoch);
        scenarios.put("task6_scenario", scenario6);

        String nextMonthEpoch = DateUtil.getEpochForDateScenario("next_month");
        Map<String, String> scenario7 = new HashMap<>();
        scenario7.put("createdOn", todayEpoch);
        scenario7.put("updatedOn", todayEpoch);
        scenario7.put("startdate", nextMonthEpoch);
        scenarios.put("task7_scenario", scenario7);

        Map<String, String> scenario8 = new HashMap<>();
        scenario8.put("createdOn", todayEpoch);
        scenario8.put("updatedOn", todayEpoch);
        scenario8.put("startdate", todayEpoch);
        scenarios.put("task8_scenario", scenario8);

        String nextWeekEpoch = DateUtil.getEpochForDateScenario("next_week");
        Map<String, String> scenario9 = new HashMap<>();
        scenario9.put("createdOn", todayEpoch);
        scenario9.put("updatedOn", todayEpoch);
        scenario9.put("startdate", nextWeekEpoch);
        scenarios.put("task9_scenario", scenario9);

        Map<String, String> scenario10 = new HashMap<>();
        scenario10.put("createdOn", yesterdayEpoch);
        scenario10.put("updatedOn", yesterdayEpoch);
        scenario10.put("startdate", todayEpoch);
        scenarios.put("task10_scenario", scenario10);

        Map<String, String> scenario11 = new HashMap<>();
        scenario11.put("createdOn", todayEpoch);
        scenario11.put("updatedOn", todayEpoch);
        scenario11.put("startdate", nextWeekEpoch);
        scenarios.put("task11_scenario", scenario11);

        Map<String, String> scenario12 = new HashMap<>();
        scenario12.put("createdOn", todayEpoch);
        scenario12.put("updatedOn", todayEpoch);
        scenario12.put("startdate", yesterdayEpoch);
        scenarios.put("task12_scenario", scenario12);

        return scenarios;
    }

    public String replacePlaceholdersInFilterValue(String filterValue) {
        if (filterValue == null || filterValue.isEmpty()) {
            return filterValue;
        }

        String result = filterValue;

        // Replace task type placeholders
        result = result.replace("{{follow_up_task_type_id}}", String.valueOf(followUpTaskTypeId));
        result = result.replace("{{interview_scheduling_task_type_id}}", String.valueOf(interviewSchedulingTaskTypeId));

        // Short format task type placeholders (used in JSON files)
        result = result.replace("{{task_type_follow_up}}", String.valueOf(followUpTaskTypeId));
        result = result.replace("{{task_type_interview_scheduling}}", String.valueOf(interviewSchedulingTaskTypeId));
        result = result.replace("{{Not Available}}", String.valueOf(defaultTaskTypeId));


        // Replace user placeholders
        if (userMap != null) {
            // Long format placeholders
            result = result.replace("{{owner_user_id}}", userMap.get("owner"));
            result = result.replace("{{admin_user_id}}", userMap.get("admin"));
            result = result.replace("{{teamMember_user_id}}", userMap.get("teamMember"));
            result = result.replace("{{restricted_user_id}}", userMap.get("restricted"));

            // Short format placeholders (used in JSON files)
            result = result.replace("{{owner}}", userMap.get("owner"));
            result = result.replace("{{admin}}", userMap.get("admin"));
            result = result.replace("{{teamMember}}", userMap.get("teamMember"));
            result = result.replace("{{restricted}}", userMap.get("restricted"));

            // User slugs (long format)
            result = result.replace("{{owner_user_slug}}", "Owner");
            result = result.replace("{{admin_user_slug}}", "Admin");
            result = result.replace("{{teamMember_user_slug}}", "TeamMember");

            // User slugs (short format used in JSON files)
            result = result.replace("{{userSlugOwner}}", "Owner");
            result = result.replace("{{userSlugAdmin}}", "Admin");
            result = result.replace("{{userSlugTeamMember}}", "TeamMember");
        }

        // Replace team placeholders
        if (teamMap != null) {
            result = result.replace("{{team1}}", teamMap.get("team1"));
            result = result.replace("{{team2}}", teamMap.get("team2"));
        }

        // Replace entity slug placeholders for associations FIRST (before IDs overwrite them)
        for (Map.Entry<String, String> entry : associatedEntitiesSlugMap.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        // Replace entity ID placeholders (for non-association fields)
        for (Map.Entry<String, Integer> entry : associatedEntitiesIdMap.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }

        // Replace test candidate slug placeholder
        result = result.replace("{{test_candidate_slug}}", candidateSlug);

        // Remove any unreplaced placeholders that would break JSON parsing
        result = result.replaceAll("\\{\\{[^}]+\\}\\}", "\"\"");

        return result;
    }

    public void updateTasksToCompleteStatus() {
        String[] completeTaskKeys = {"task1", "task4", "task6", "task8", "task11"}; // 5 tasks to complete
        for (String taskKey : completeTaskKeys) {
            Integer taskId = taskIdMap.get(taskKey);
            if (taskId != null) {
                Response updateResponse = updateTaskStatus(taskId, "1");
                Assert.assertEquals(updateResponse.getStatusCode(), 200, "Failed to update task " + taskId + " to complete status");
            }
        }
    }

    public Response updateTaskStatus(int taskId, String status) {
        JSONObject payload = new JSONObject();
        payload.put("key", "status");
        payload.put("value", status);
        payload.put("tableFlag", "task");
        payload.put("id", taskId);
        String basePath = "global/update-fields";
        return RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
    }

}