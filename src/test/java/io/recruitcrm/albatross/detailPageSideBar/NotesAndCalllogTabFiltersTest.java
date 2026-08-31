package io.recruitcrm.albatross.detailPageSideBar;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.recruitcrm.Filters.FilterSearchBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NotesAndCalllogTabFiltersTest extends FilterSearchBaseTest {
    AllCrudFunctions allCrudFunctions;
    String albatrossAuthToken;
    commanFunction function;
    String accountOwnerAPIKey;
    Map<String, String> associatedEntitiesSlugMap = new HashMap<>();
    Map<String, Integer> associatedEntitiesIdMap = new HashMap<>();
    Map<String, String> callTypeMap;
    Map<String, String> noteTypeMap;
    Map<String, String> userMap;
    Map<String, String> userSlugMap;
    Map<String, String> teamMap;
    Map<String, Integer> callLogMap;
    Map<String, Integer> noteMap;
    int candidateId;
    String candidateSlug;


    @BeforeClass
    public void setUp() {
        allCrudFunctions = new AllCrudFunctions();
        function = new commanFunction();
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountOwnerAPIKey = ThreadManager.getAccountApiKey();
        createAssociatedEntities(function, accountOwnerAPIKey, albatrossAuthToken, associatedEntitiesSlugMap, associatedEntitiesIdMap);
        callTypeMap = createCustomCallType();
        noteTypeMap = createCustomNoteType();
        userMap = createUserMap(accountOwnerAPIKey);
        userSlugMap = createUserSlugMap(accountOwnerAPIKey);
        teamMap = createTeamMap();
        createTestData();
    }

    @DataProvider(name = "notesAndCallLogTabFiltersTestData", parallel = true)
    public Object[][] notesAndCallLogTabFiltersTestData() {
        JSONObject filterData = readJsonFileFromPath("src/test/resources/candidateDetailNotesCallLogFilterDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        for (String key : filterData.keySet()) {
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
        return testData.toArray(new Object[0][0]);
    }

    @DataProvider(name = "noteDescriptionSearchTestData", parallel = true)
    public Object[][] noteDescriptionSearchTestData() {
        JSONObject searchData = readJsonFileFromPath("src/test/resources/noteDescriptionSearchDataProvider.json");
        List<Object[]> testData = new ArrayList<>();
        
        JSONArray testCases = searchData.getJSONArray("noteDescriptionSearchTestCases");
        for (int i = 0; i < testCases.length(); i++) {
            JSONObject testCase = testCases.getJSONObject(i);
            String testCaseId = testCase.getString("testCaseId");
            String description = testCase.getString("description");
            JSONArray searchTerms = testCase.getJSONArray("searchTerms");
            
            for (int j = 0; j < searchTerms.length(); j++) {
                JSONObject searchTerm = searchTerms.getJSONObject(j);
                String term = searchTerm.getString("term");
                JSONArray expectedResults = searchTerm.getJSONArray("expectedResults");
                
                // Convert expected results to comma-separated string
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

    @Owner("Harika")
    @Test(dataProvider = "notesAndCallLogTabFiltersTestData", description = "Notes and Call Log Activity Side Bar Filter Search Test")
    public void notesAndCallLogTabFiltersTest(String fieldName, String filter, String filterValue, String expectedResult, String description) {
        String basePath = "/expand-activity/get-activity-data";
        JSONObject payload = buildPayload(filter, filterValue);
        
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code for field: " + fieldName + ", filter: " + filter + " and filterValue: " + filterValue + " is not 200");
        if (filter.equals("createdon")) {
            long[] range = DateUtil.getDateRange(Integer.parseInt(filterValue));
            assertCreatedOnWithinRange(response, range[0], range[1], description);
        } else {
            assertNotesAndCallLogsMatch(response, expectedResult, noteMap, callLogMap, description);
        }
    }

    @Owner("Harika")
    @Test(dataProvider = "noteDescriptionSearchTestData", description = "Note Description Search Test")
    public void noteDescriptionSearchTest(String testCaseId, String filter, String searchTerm, String expectedResult, String description) {
        String basePath = "/expand-activity/get-activity-data";
        JSONObject payload = buildPayload(filter, searchTerm);
        
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, payload);
        Assert.assertEquals(response.getStatusCode(), 200, 
            "Response code for search term: '" + searchTerm + "' is not 200");
        
        // Validate search results
        assertNoteDescriptionSearchResults(response, expectedResult, searchTerm, description);
    }

    public void assertCreatedOnWithinRange(Response response, long startEpoch, long endEpoch, String description) {
        JSONObject jsonResponse = new JSONObject(response.asString());

        JSONArray notes = jsonResponse
                .getJSONObject("data")
                .getJSONObject("events")
                .getJSONArray("notes");

        for (int i = 0; i < notes.length(); i++) {
            JSONObject note = notes.getJSONObject(i);
            long createdOn = note.getLong("createdon");

            Assert.assertTrue(
                    createdOn >= startEpoch && createdOn <= endEpoch, description + "failed."
            );
        }
    }

    public static void assertNotesAndCallLogsMatch(Response response, String expectedItems, Map<String, Integer> noteMap, Map<String, Integer> callLogMap, String description) {
        JSONObject json = new JSONObject(response.getBody().asString());
        JSONArray notesArray = json.getJSONObject("data")
                .getJSONObject("events")
                .getJSONArray("notes");

        Set<Integer> actualIds = new HashSet<>();
        for (int i = 0; i < notesArray.length(); i++) {
            actualIds.add(notesArray.getJSONObject(i).getInt("id"));
        }

        Set<Integer> expectedIds = new HashSet<>();
        Set<String> expectedKeys = new HashSet<>();
        for (String key : expectedItems.split(",")) {
            key = key.trim();
            if (noteMap.containsKey(key)) {
                expectedIds.add(noteMap.get(key));
                expectedKeys.add(key);
            } else if (callLogMap.containsKey(key)) {
                expectedIds.add(callLogMap.get(key));
                expectedKeys.add(key);
            } else {
                throw new IllegalArgumentException("Key not found in maps: " + key);
            }
        }

        Set<String> actualKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : noteMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }
        for (Map.Entry<String, Integer> entry : callLogMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }

        Assert.assertEquals(actualIds, expectedIds,
                description + " Mismatch in note IDs. Expected keys=" + expectedKeys + " (IDs=" + expectedIds + "), Actual keys=" + actualKeys + " (IDs=" + actualIds + ")");
    }

    public void assertNoteDescriptionSearchResults(Response response, String expectedResult, String searchTerm, String description) {
        JSONObject json = new JSONObject(response.getBody().asString());
        JSONArray notesArray = json.getJSONObject("data")
                .getJSONObject("events")
                .getJSONArray("notes");

        // Get actual note IDs from response
        Set<Integer> actualIds = new HashSet<>();
        for (int i = 0; i < notesArray.length(); i++) {
            actualIds.add(notesArray.getJSONObject(i).getInt("id"));
        }

        // Get expected note and call log IDs
        Set<Integer> expectedIds = new HashSet<>();
        Set<String> expectedKeys = new HashSet<>();
        
        if (expectedResult != null && !expectedResult.trim().isEmpty()) {
            for (String key : expectedResult.split(",")) {
                key = key.trim();
                if (noteMap.containsKey(key)) {
                    expectedIds.add(noteMap.get(key));
                    expectedKeys.add(key);
                } else if (callLogMap.containsKey(key)) {
                    expectedIds.add(callLogMap.get(key));
                    expectedKeys.add(key);
                } else {
                    throw new IllegalArgumentException("Expected key not found in noteMap or callLogMap: " + key);
                }
            }
        }

        // Get actual note and call log keys for better error reporting
        Set<String> actualKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : noteMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }
        for (Map.Entry<String, Integer> entry : callLogMap.entrySet()) {
            if (actualIds.contains(entry.getValue())) {
                actualKeys.add(entry.getKey());
            }
        }

        // Validate that search term appears in the description of returned notes and call logs
        if (!searchTerm.trim().isEmpty()) {
            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject item = notesArray.getJSONObject(i);
                String descriptionText = "";
                
                // Check if it's a note or call log based on which field has actual content
                String noteDescription = item.optString("description", "").trim();
                String callNotes = item.optString("callnotes", "").trim();
                
                if (!noteDescription.isEmpty()) {
                    descriptionText = noteDescription.toLowerCase();
                } else if (!callNotes.isEmpty()) {
                    descriptionText = callNotes.toLowerCase();
                }
                
                String searchTermLower = searchTerm.toLowerCase();
                
                // Handle multiple levels of escaping in search term
                String normalizedSearchTerm = searchTermLower
                    .replace("\\\\n", "\n")  // \\n -> \n
                    .replace("\\n", "\n")    // \n -> \n
                    .replace("\\\\t", "\t")  // \\t -> \t
                    .replace("\\t", "\t");   // \t -> \t
                
                // Check if search term appears in description (case-insensitive)
                boolean found = descriptionText.contains(searchTermLower) || descriptionText.contains(normalizedSearchTerm);
                Assert.assertTrue(found, 
                    description + " - Search term '" + searchTerm + "' not found in description: " + descriptionText);
            }
        }

        // Validate expected vs actual results
        Assert.assertEquals(actualIds, expectedIds,
                description + " - Search term: '" + searchTerm + "' - Mismatch in IDs. " +
                "Expected keys=" + expectedKeys + " (IDs=" + expectedIds + "), " +
                "Actual keys=" + actualKeys + " (IDs=" + actualIds + ")");
    }

    public JSONObject buildPayload(String filter, String filterValue) {
        JSONObject payload = new JSONObject();

        // Base payload
        payload.put("type", "0");
        payload.put("pageSize", 30);
        payload.put("page", "detailspage");
        payload.put("relatedToSlug", candidateSlug);
        payload.put("relatedtotypeid", 5);

        if(filter.equals("createdon")){
            JSONObject createdDate = new JSONObject();
            createdDate.put("id", filterValue);

            long[] range = DateUtil.getDateRange(Integer.parseInt(filterValue));
            createdDate.put("startdate", range[0]);
            createdDate.put("enddate", range[1]);

            payload.put("createdDate", createdDate);
        }

        if(filter.equals("note_call_log_associated_with")){
            JSONObject json = new JSONObject(processFilterValue(filterValue));
            payload.put("associations", json);
        }

        if(filter.equals("createdby")){
            JSONObject json = new JSONObject(processFilterValue(filterValue));

            payload.put("teamFilter", json.getJSONArray("teamFilter"));
            payload.put("userFilter", json.getJSONArray("userFilter"));
            payload.put("userSlugs", json.getJSONArray("userSlugs"));
        }

        if (filter.equals("calltype")) {
            JSONArray array = new JSONArray(processFilterValue(filterValue));
            payload.put("callLogTypes", array);
        }

        if (filter.equals("notetype")) {
            JSONArray array = new JSONArray(processFilterValue(filterValue));
            payload.put("noteTypes", array);
        }

        if (filter.equals("search")) {
            payload.put("searchTerm", filterValue);
        }

        payload.put("isFilterApplied", 1);
        payload.put("relatedtocompany", JSONObject.NULL);
        payload.put("offset", 0);

        return payload;
    }

    private String processFilterValue(String filterValue) {
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
            if (fieldKey.startsWith("associated_")) {
                actualValue = associatedEntitiesSlugMap.get(fieldKey);
            }
            else if (fieldKey.startsWith("team") && !fieldKey.equals("teamMember")) {
                actualValue = teamMap.get(fieldKey);
            }
            else if (fieldKey.startsWith("userSlug")) {
                actualValue = userSlugMap.get(fieldKey);
            }
            else if (fieldKey.equals("Video Call") || fieldKey.equals("Inbound") || fieldKey.equals("Outbound") || fieldKey.equals("Custom Call Type")) {
                actualValue = callTypeMap.get(fieldKey);
            }
            else if (fieldKey.equals("Call") || fieldKey.equals("To Do") || fieldKey.equals("Custom Note Type")) {
                actualValue = noteTypeMap.get(fieldKey);
            }
            else if (fieldKey.equals("Not Available")) {
                actualValue = "0";
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

        return processedValue;
    }


    public void createTestData() {
        JsonPath candidate = function.createNewCandidateWithMandatoryFields(baseURL, accountOwnerAPIKey).jsonPath();
        candidateSlug = candidate.get("slug");
        candidateId = function.getCandidateIdBySlug(albatrossURL, albatrossAuthToken, candidateSlug);

        callLogMap = createCallLogsForSingleCandidate(candidateSlug);
        noteMap = createNotesForSingleCandidate(candidateSlug);

        Map<String, Map<String, String>> timestampScenarios = createTimestampScenarios();
        updateActivityWithTimestampScenarios(new ArrayList<>(noteMap.values()), "note", timestampScenarios);
        updateActivityWithTimestampScenarios(new ArrayList<>(callLogMap.values()), "calllog", timestampScenarios);
    }


    private Map<String, Integer> createCallLogsForSingleCandidate(String candidateSlug) {
        JSONObject callLogJson = readJsonFileFromPath("src/test/resources/calllog_sidebar.json");
        Map<String, Integer> callLogIdMap = new LinkedHashMap<>();

        // Loop over candidate1, candidate2 ... keys
        for (String callLogKey : callLogJson.keySet()) {
            JSONObject candidateObj = callLogJson.getJSONObject(callLogKey);
            JSONArray callsArray = candidateObj.getJSONArray("calls");

            for (int i = 0; i < callsArray.length(); i++) {
                JSONObject callPayload = callsArray.getJSONObject(i);

                // Replace placeholders like {candidate1}, {owner}, etc.
                JSONObject processedPayload = processPayload(callPayload, candidateSlug);

                // Send request for each call log
                Response response = function.createCallLogByPayload(
                        baseURL,
                        accountOwnerAPIKey,
                        processedPayload
                );

                int id = response.jsonPath().getInt("id");
                callLogIdMap.put(callLogKey, id);
            }
        }
        return callLogIdMap;
    }


    private Map<String, Integer> createNotesForSingleCandidate(String candidateSlug) {
        JSONObject notesJson = readJsonFileFromPath("src/test/resources/note_sidebar.json");
        Map<String, Integer> noteIdMap = new LinkedHashMap<>();

        for (String notesKey : notesJson.keySet()) {
            JSONObject candidateObj = notesJson.getJSONObject(notesKey);
            JSONArray notesArray = candidateObj.getJSONArray("notes");

            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject notePayload = notesArray.getJSONObject(i);

                JSONObject processedPayload = processPayload(notePayload, candidateSlug);

                Response response = function.createNotesByPayload(
                        baseURL,
                        accountOwnerAPIKey,
                        processedPayload
                );

                int id = response.jsonPath().getInt("id");
                noteIdMap.put(notesKey, id);
            }
        }
        return noteIdMap;
    }


    private JSONObject processPayload(JSONObject payload, String candidateSlug) {
        // Check if this is a single activity item (has call_type or note_type_id)
        if (payload.has("call_type") || payload.has("note_type_id")) {
            // This is a single activity item, determine type and process
            String activityType = payload.has("call_type") ? "call" : "note";
            processActivityItem(payload, candidateSlug, activityType);
        } else {
            // Process calls if present
            if (payload.has("calls") && payload.get("calls") instanceof JSONArray) {
                JSONArray callsArray = payload.getJSONArray("calls");
                for (int i = 0; i < callsArray.length(); i++) {
                    JSONObject call = callsArray.getJSONObject(i);
                    processActivityItem(call, candidateSlug, "call");
                }
            }
            
            // Process notes if present
            if (payload.has("notes") && payload.get("notes") instanceof JSONArray) {
                JSONArray notesArray = payload.getJSONArray("notes");
                for (int i = 0; i < notesArray.length(); i++) {
                    JSONObject note = notesArray.getJSONObject(i);
                    processActivityItem(note, candidateSlug, "note");
                }
            }
        }
        
        return payload;
    }

    private void processActivityItem(JSONObject activityItem, String candidateSlug, String activityType) {
        // Process related_to field
        if (activityItem.has("related_to")) {
            String relatedTo = activityItem.getString("related_to");
            if (relatedTo.startsWith("{candidate") && relatedTo.endsWith("}")) {
                activityItem.put("related_to", candidateSlug);
            }
        }

        // Process associated entity fields
        String[] associatedFields = {"associated_candidates", "associated_companies", "associated_contacts", "associated_jobs", "associated_deals"};
        for (String field : associatedFields) {
            processAssociatedEntityField(activityItem, field);
        }

        // Process type-specific fields
        if ("call".equals(activityType)) {
            processCallTypeField(activityItem);
        } else if ("note".equals(activityType)) {
            processNoteTypeField(activityItem);
        }

        // Process user fields
        processUserField(activityItem, "created_by");
        processUserField(activityItem, "updated_by");

        // Process collaborator fields
        processCollaboratorField(activityItem, "collaborator_team_ids", teamMap);
        processCollaboratorField(activityItem, "collaborator_user_ids", userMap);
    }

    private void processCallTypeField(JSONObject activityItem) {
        if (activityItem.has("custom_call_type_id") && callTypeMap != null) {
            String callTypeLabel = activityItem.getString("custom_call_type_id").replace("{", "").replace("}", "");
            String callTypeId = callTypeMap.get(callTypeLabel);
            if (callTypeId != null) {
                activityItem.put("custom_call_type_id", callTypeId);
            }
        }
    }

    private void processNoteTypeField(JSONObject activityItem) {
        if (activityItem.has("note_type_id") && noteTypeMap != null) {
            String noteTypeLabel = activityItem.getString("note_type_id").replace("{", "").replace("}", "");
            String noteTypeId = noteTypeMap.get(noteTypeLabel);
            if (noteTypeId != null) {
                activityItem.put("note_type_id", noteTypeId);
            }
        }
    }

    private void processUserField(JSONObject activityItem, String fieldName) {
        if (activityItem.has(fieldName) && userMap != null) {
            String userValue = activityItem.getString(fieldName).replace("{", "").replace("}", "");
            String userId = userMap.get(userValue);
            if (userId != null) {
                activityItem.put(fieldName, userId);
            }
        }
    }



    private Map<String, Map<String, String>> createTimestampScenarios() {
        Map<String, Map<String, String>> scenarios = new HashMap<>();

        // Today scenario
        String todayEpoch = DateUtil.getEpochForDateScenario("today");
        Map<String, String> todayTimestamps = new HashMap<>();
        todayTimestamps.put("createdOn", todayEpoch);
        todayTimestamps.put("updatedOn", todayEpoch);
        scenarios.put("today_scenario", todayTimestamps);

        // Yesterday scenario
        String yesterdayEpoch = DateUtil.getEpochForDateScenario("yesterday");
        Map<String, String> yesterdayTimestamps = new HashMap<>();
        yesterdayTimestamps.put("createdOn", yesterdayEpoch);
        yesterdayTimestamps.put("updatedOn", yesterdayEpoch);
        scenarios.put("yesterday_scenario", yesterdayTimestamps);

        // This week scenario
        String thisWeekEpoch = DateUtil.getEpochForDateScenario("this_week");
        Map<String, String> thisWeekTimestamps = new HashMap<>();
        thisWeekTimestamps.put("createdOn", thisWeekEpoch);
        thisWeekTimestamps.put("updatedOn", thisWeekEpoch);
        scenarios.put("this_week_scenario", thisWeekTimestamps);

        // Last week scenario
        String lastWeekEpoch = DateUtil.getEpochForDateScenario("last_week");
        Map<String, String> lastWeekTimestamps = new HashMap<>();
        lastWeekTimestamps.put("createdOn", lastWeekEpoch);
        lastWeekTimestamps.put("updatedOn", lastWeekEpoch);
        scenarios.put("last_week_scenario", lastWeekTimestamps);

        // This month scenario
        String thisMonthEpoch = DateUtil.getEpochForDateScenario("this_month");
        Map<String, String> thisMonthTimestamps = new HashMap<>();
        thisMonthTimestamps.put("createdOn", thisMonthEpoch);
        thisMonthTimestamps.put("updatedOn", thisMonthEpoch);
        scenarios.put("this_month_scenario", thisMonthTimestamps);

        // Last month scenario
        String lastMonthEpoch = DateUtil.getEpochForDateScenario("last_month");
        Map<String, String> lastMonthTimestamps = new HashMap<>();
        lastMonthTimestamps.put("createdOn", lastMonthEpoch);
        lastMonthTimestamps.put("updatedOn", lastMonthEpoch);
        scenarios.put("last_month_scenario", lastMonthTimestamps);

        // This quarter scenario
        String thisQuarterEpoch = DateUtil.getEpochForDateScenario("this_quarter");
        Map<String, String> thisQuarterTimestamps = new HashMap<>();
        thisQuarterTimestamps.put("createdOn", thisQuarterEpoch);
        thisQuarterTimestamps.put("updatedOn", thisQuarterEpoch);
        scenarios.put("this_quarter_scenario", thisQuarterTimestamps);

        // Last quarter scenario
        String lastQuarterEpoch = DateUtil.getEpochForDateScenario("last_quarter");
        Map<String, String> lastQuarterTimestamps = new HashMap<>();
        lastQuarterTimestamps.put("createdOn", lastQuarterEpoch);
        lastQuarterTimestamps.put("updatedOn", lastQuarterEpoch);
        scenarios.put("last_quarter_scenario", lastQuarterTimestamps);

        // This year scenario
        String thisYearEpoch = DateUtil.getEpochForDateScenario("this_year");
        Map<String, String> thisYearTimestamps = new HashMap<>();
        thisYearTimestamps.put("createdOn", thisYearEpoch);
        thisYearTimestamps.put("updatedOn", thisYearEpoch);
        scenarios.put("this_year_scenario", thisYearTimestamps);

        // Last year scenario
        String lastYearEpoch = DateUtil.getEpochForDateScenario("last_year");
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


    public Map<String, String> createCustomCallType() {
        Map<String, String> callTypeMap = new HashMap<>();

        // List of call types to create
        String[] callTypes = {"Inbound", "Outbound", "Video Call", "Custom Call Type"};

        // Create each call type
        for (String callType : callTypes) {
            function.createCustomCallType(albatrossURL, albatrossAuthToken, callType, false);
        }

        // Fetch all call types
        Response response = function.getCallTypes(albatrossURL, albatrossAuthToken);
        int size = response.jsonPath().getList("data").size();
        for (int i = 0; i < size; i++) {
            String label = response.jsonPath().getString("data[" + i + "].label");
            String id = response.jsonPath().getString("data[" + i + "].id");
            callTypeMap.put(label, id);
        }

        return callTypeMap;
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

    public Map<String, String> createTeamMap() {
        Map<String, String> teamMap = new HashMap<>();

        // Define teams: teamName -> user keys
        Map<String, List<String>> teamDefinitions = new HashMap<>();
        teamDefinitions.put("team1", Arrays.asList("admin", "teamMember"));
        teamDefinitions.put("team2", Arrays.asList("owner", "restricted"));

        // Create teams
        for (Map.Entry<String, List<String>> entry : teamDefinitions.entrySet()) {
            String teamName = entry.getKey();
            List<String> userKeys = entry.getValue();

            List<String> userIds = new ArrayList<>();
            for (String key : userKeys) {
                userIds.add(String.valueOf(userMap.get(key)));
            }

            Response response = allCrudFunctions.createTeam(
                    albatrossURL,
                    ThreadManager.getOwnerAlbatrossToken(),
                    teamName,
                    new ArrayList<>(userIds) // API expects ArrayList
            );
            response.then().statusCode(200);
        }

        // Fetch all teams
        Response teamResponse = function.getTeams(baseURL, accountOwnerAPIKey);
        List<Map<String, Object>> teams = teamResponse.jsonPath().getList("");

        // Match by team name to get teamId
        for (String teamName : teamDefinitions.keySet()) {
            for (Map<String, Object> team : teams) {
                if (teamName.equalsIgnoreCase((String) team.get("team_name"))) {
                    teamMap.put(teamName, String.valueOf(team.get("team_id")));
                    break; // found this team, break inner loop
                }
            }
        }

        return teamMap;
    }



    private void processAssociatedEntityField(JSONObject activityItem, String fieldName) {
        if (activityItem.has(fieldName)) {
            String fieldValue = activityItem.getString(fieldName);
            if (fieldValue.startsWith("{" + fieldName + "_") && fieldValue.endsWith("}")) {
                String entityKeys = fieldValue.replace("{", "").replace("}", "");
                String[] keys = entityKeys.split(",");
                List<String> entityValues = new ArrayList<>();

                for (String key : keys) {
                    String trimmedKey = key.trim();
                    String entityValue = associatedEntitiesSlugMap.get(trimmedKey);
                    if (entityValue != null) {
                        entityValues.add(entityValue);
                    }
                }

                if (!entityValues.isEmpty()) {
                    activityItem.put(fieldName, String.join(",", entityValues));
                } else {
                    activityItem.put(fieldName, "");
                }
            }
        }
    }

    public void processCollaboratorField(JSONObject activityItem, String fieldName, Map<String, String> collaboratorMap) {
        if (activityItem.has(fieldName) && collaboratorMap != null) {
            String collaboratorValue = activityItem.getString(fieldName);
            if (collaboratorValue.startsWith("{") && collaboratorValue.endsWith("}")) {
                String collaboratorKeys = collaboratorValue.replace("{", "").replace("}", "");
                String[] keys = collaboratorKeys.split(",");
                List<String> collaboratorValues = new ArrayList<>();

                for (String key : keys) {
                    String trimmedKey = key.trim();
                    String collaboratorId = collaboratorMap.get(trimmedKey);
                    if (collaboratorId != null) {
                        collaboratorValues.add(collaboratorId);
                    }
                }

                if (!collaboratorValues.isEmpty()) {
                    activityItem.put(fieldName, String.join(",", collaboratorValues));
                } else {
                    activityItem.put(fieldName, "");
                }
            }
        }
    }
}
