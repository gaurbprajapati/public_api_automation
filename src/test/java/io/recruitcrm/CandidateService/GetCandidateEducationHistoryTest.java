package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;

import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import org.json.JSONObject;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class GetCandidateEducationHistoryTest extends TestBase {
    String albatrossAuthToken;
    String accountAPIKey;
    AllCrudFunctions function = new AllCrudFunctions();
    ConcurrentHashMap<String, String> candidateIdMap = new ConcurrentHashMap<>();

    public GetCandidateEducationHistoryTest() {
        super();
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        accountAPIKey = ThreadManager.getAccountApiKey();
        createTestData();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateEducationHistory_Success() {
        int candidateId = Integer.parseInt(candidateIdMap.get("candidate1"));
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, pathParams, true);

        assertThat("Response should not be null", response, is(notNullValue()));
        assertThat("API should return 200 status code", response.getStatusCode(), is(equalTo(200)));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta section should not be null", jsonPath.get("meta"), is(notNullValue()));
        assertThat("Meta message should match expected", jsonPath.get("meta.message"), is(equalTo("Education history fetched successfully.")));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), is(notNullValue()));
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), is(notNullValue()));

        List<Map<String, Object>> data = jsonPath.getList("data");
        assertThat("Data section should not be null", data, is(notNullValue()));
        assertThat("Data section should not be empty", data.isEmpty(), is(false));

        Map<String, Object> collegeGroup = data.get(0);
        assertThat("College name should not be null", collegeGroup.get("instituteName"), is(notNullValue()));
        assertThat("Education histories should not be null", collegeGroup.get("educationHistories"), is(notNullValue()));

        List<Map<String, Object>> educationHistories = (List<Map<String, Object>>) collegeGroup.get("educationHistories");
        assertThat("Education histories list should not be null", educationHistories, is(notNullValue()));
        assertThat("Education histories list should not be empty", educationHistories.isEmpty(), is(false));

        for (Map<String, Object> educationHistory : educationHistories) {
            assertThat("Education history ID should not be null", educationHistory.get("id"), is(notNullValue()));
            assertThat("Candidate ID should not be null", educationHistory.get("candidateId"), is(notNullValue()));
            assertThat("Institute name should not be null", educationHistory.get("instituteName"), is(notNullValue()));
            assertThat("Educational qualification should not be null", educationHistory.get("educationalQualification"), is(notNullValue()));
            assertThat("Educational specialization should not be null", educationHistory.get("educationalSpecialization"), is(notNullValue()));
            assertThat("Grade should not be null", educationHistory.get("grade"), is(notNullValue()));
            assertThat("Education location should not be null", educationHistory.get("educationLocation"), is(notNullValue()));
            assertThat("Education start date should not be null", educationHistory.get("educationStartDate"), is(notNullValue()));
            assertThat("Education end date should not be null", educationHistory.get("educationEndDate"), is(notNullValue()));
            assertThat("Education description should not be null", educationHistory.get("educationDescription"), is(notNullValue()));
            assertThat("Candidate slug should not be null", educationHistory.get("candidateSlug"), is(notNullValue()));
            assertThat("Is manually added should not be null", educationHistory.get("isManuallyAdded"), is(notNullValue()));
            assertThat("Created by should not be null", educationHistory.get("createdBy"), is(notNullValue()));
            assertThat("Updated by should not be null", educationHistory.get("updatedBy"), is(notNullValue()));
            assertThat("Created on should not be null", educationHistory.get("createdOn"), is(notNullValue()));
            assertThat("Updated on should not be null", educationHistory.get("updatedOn"), is(notNullValue()));
        }
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetCandidateEducationHistoryTest.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateEducationHistory_WithoutAuth() {
        int candidateId = Integer.parseInt(candidateIdMap.get("candidate1"));
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, null, null, pathParams, true);
        assertThat("Response should not be null", response, is(notNullValue()));
        assertThat("API should return 401 status code for unauthorized access", response.getStatusCode(), is(equalTo(401)));
        JsonPath jsonPath = response.jsonPath();
        
        assertThat("Meta section should not be null", jsonPath.get("meta"), is(notNullValue()));
        assertThat("Meta message should match expected", jsonPath.get("meta.message"), is(equalTo("Unauthorised access")));
        assertThat("Response type context should be 'Warning'", jsonPath.get("meta.responseType.context"), is(equalTo("Warning")));
        assertThat("Data should contain the expected error message", jsonPath.get("data"), is(equalTo("Internal Server Error")));
        
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateEducationHistory_InvalidAuth() {
        int candidateId = Integer.parseInt(candidateIdMap.get("candidate1"));
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken + "12345", null, pathParams, true);
        
        assertThat("Response should not be null", response, is(notNullValue()));
        assertThat("API should return 401 status code for invalid authentication", response.getStatusCode(), is(equalTo(401)));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta section should not be null", jsonPath.get("meta"), is(notNullValue()));
        assertThat("Meta message should match expected", jsonPath.get("meta.message"), is(equalTo("Unauthorised access")));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), is(notNullValue()));
        assertThat("Response type context should be 'Warning'", jsonPath.get("meta.responseType.context"), is(equalTo("Warning")));
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), is(notNullValue()));
        assertThat("Data should contain the expected error message", jsonPath.get("data"), is(equalTo("Invalid or expired token")));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateEducationHistory_InvalidCandidateId() {
        String basePath = "candidates/999999999/education-history";
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, null, true);
        assertThat("Response should not be null", response, is(notNullValue()));
        assertThat("API should return 404 status code for invalid candidate ID", response.getStatusCode(), is(equalTo(404)));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta section should not be null", jsonPath.get("meta"), is(notNullValue()));
        assertThat("Meta message should be null for 404 error", jsonPath.get("meta.message"), is(nullValue()));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), is(notNullValue()));
        assertThat("Response type context should match expected", jsonPath.get("meta.responseType.context"), is(equalTo("Error while processing request")));
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), is(notNullValue()));
        assertThat("Data should be null for 404 error", jsonPath.get("data"), is(nullValue()));
        assertThat("Error message should match expected", jsonPath.get("errors[0].message"), is(equalTo("Candidate id 999999999 not found.")));
    }


    @Owner("Raj Pandey")
    @Test(dataProvider = "allSortingScenarioData", groups = {"candidate_service", "nightly-build"})
    public void testEducationHistorySorting(String candidateId, String testType) {
        List<Map<String, Object>> educationHistory = getEducationHistoryForCandidate(candidateId);
        validateBasicDateSorting(educationHistory);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "groupingScenarioData", groups = {"candidate_service", "nightly-build"})
    public void testEducationHistoryGroupingLogic(String candidateId, String testType, HistoryGroupingValidationConfig config) {
        List<Map<String, Object>> collegeGroups = getGroupedEducationHistoryForCandidate(candidateId);
        validateGroupingLogic(collegeGroups, testType, config);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "dynamicUpdateScenarioData", groups = {"candidate_service", "nightly-build"})
    public void testDynamicEducationHistoryUpdates(String candidateId, String updateType, String originalCollege, String newCollege) {
        performEducationHistoryUpdate(candidateId, originalCollege, newCollege);

        List<Map<String, Object>> updatedEducationHistory = getEducationHistoryForCandidate(candidateId);

        switch (updateType) {
            case "Merge Groups": {
                validateBasicDateSorting(updatedEducationHistory);
                List<Map<String, Object>> collegeGroups = getGroupedEducationHistoryForCandidate(candidateId);
                validateGroupingLogic(collegeGroups, updateType, new HistoryGroupingValidationConfig(1, "SINGLE_GROUPS", null, Map.of("MIT", 3)));
                break;
            }
            case "Split Groups": {
                validateBasicDateSorting(updatedEducationHistory);
                List<Map<String, Object>> collegeGroups = getGroupedEducationHistoryForCandidate(candidateId);
                validateGroupingLogic(collegeGroups, updateType, new HistoryGroupingValidationConfig(4, "MULTIPLE_GROUPS", null, Map.of("Harvard University", 1, "MIT", 1, "Stanford University", 1,"Cambridge University",1)));
                break;
            }
            case "Merge and Split Groups": {
                validateBasicDateSorting(updatedEducationHistory);
                List<Map<String, Object>> collegeGroups = getGroupedEducationHistoryForCandidate(candidateId);
                validateGroupingLogic(collegeGroups, updateType, new HistoryGroupingValidationConfig(2, "MULTIPLE_GROUPS", null, Map.of("Berkeley", 1, "UCLA", 4)));
                break;
            }
        }
    }


    @DataProvider(name = "allSortingScenarioData")
    public Object[][] getAllSortingScenarioData() {
        return new Object[][] {
                {candidateIdMap.get("candidate1"), "Basic Date Sorting"},
                {candidateIdMap.get("candidate2"), "Intra-College Ordering"},
                {candidateIdMap.get("candidate3"), "Inter-College Ordering"}
        };
    }

    @DataProvider(name = "groupingScenarioData")
    public Object[][] getGroupingScenarioData() {
        return new Object[][] {
                {candidateIdMap.get("candidate4"), "Case Sensitivity", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "galgotia college", Map.of("galgotia college", 3))},
                {candidateIdMap.get("candidate5"), "Special Characters", new HistoryGroupingValidationConfig(2, "MULTIPLE_GROUPS", null, Map.of("Trinity College Dublin", 2, "O'Malley's University", 2))},
                {candidateIdMap.get("candidate6"), "Whitespace Handling", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "georgetown university", Map.of("georgetown university", 4))},
                {candidateIdMap.get("candidate7"), "Multiple Spaces", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "university of california", Map.of("university of california", 3))},
                {candidateIdMap.get("candidate8"), "Similar Distinct", new HistoryGroupingValidationConfig(3, "MULTIPLE_GROUPS", null, Map.of("Smith University", 1, "Smith College", 1, "Smith's University", 1))},
                {candidateIdMap.get("candidate9"), "Empty College Names", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "not available", Map.of("Not Available", 2))},
                {candidateIdMap.get("candidate10"), "Very Long Names", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", null, Map.of("Very Long University Name", 2), 100)}
        };
    }

    @DataProvider(name = "dynamicUpdateScenarioData")
    public Object[][] getDynamicUpdateScenarioData() {
        return new Object[][] {
                {candidateIdMap.get("candidate11"), "Merge Groups", "Stanford University", "MIT"},
                {candidateIdMap.get("candidate12"), "Split Groups", "Harvard University", "Cambridge University"},
                {candidateIdMap.get("candidate13"), "Merge and Split Groups", "Berkeley", "UCLA"}
        };
    }

    public void createTestData() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_dataW&E.json");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            CompletableFuture.allOf(candidateJson.keySet().stream()
                .filter(key -> key.startsWith("candidate"))
                .map(candidateKey -> CompletableFuture.runAsync(() -> {
                    JSONObject payload = candidateJson.getJSONObject(candidateKey);
                        Response response = function.createCandidateWithJson(albatrossURL, albatrossAuthToken, payload);
                        int candidateId = response.jsonPath().getInt("data.candidate.id");
                        candidateIdMap.put(candidateKey, String.valueOf(candidateId));
                    }, executor)).toArray(CompletableFuture[]::new)).join();
        } finally {
            executor.shutdown();
        }
    }

    private List<Map<String, Object>> getEducationHistoryForCandidate(String candidateId) {
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", candidateId);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, pathParams, true);
        assertThat("Failed to get education history for candidate: " + candidateId, response.getStatusCode(), is(equalTo(200)));

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> collegeGroups = jsonPath.getList("data");

        List<Map<String, Object>> educationHistory = new ArrayList<>();
        for (Map<String, Object> collegeGroup : collegeGroups) {
            List<Map<String, Object>> educationHistories = (List<Map<String, Object>>) collegeGroup.get("educationHistories");
            if (educationHistories != null) {
                educationHistory.addAll(educationHistories);
            }
        }

        return educationHistory;
    }

    private List<Map<String, Object>> getGroupedEducationHistoryForCandidate(String candidateId) {
        String basePath = "candidates/{candidateId}/education-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", candidateId);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, pathParams, true);
        assertThat("Failed to get education history for candidate: " + candidateId, response.getStatusCode(), is(equalTo(200)));

        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getList("data");
    }


    private void validateBasicDateSorting(List<Map<String, Object>> educationHistory) {
        validateOverallSortingOrder(educationHistory);
        validateSameStartDateSorting(educationHistory);
    }

    private void validateOverallSortingOrder(List<Map<String, Object>> educationHistory) {
        for (int i = 0; i < educationHistory.size() - 1; i++) {
            Map<String, Object> currentEntry = educationHistory.get(i);
            Map<String, Object> nextEntry = educationHistory.get(i + 1);

            int currentStartDate = getStartDate(currentEntry);
            int currentEndDate = getEndDate(currentEntry);
            int nextStartDate = getStartDate(nextEntry);
            int nextEndDate = getEndDate(nextEntry);

            String currentCollege = (String) currentEntry.get("instituteName");
            String nextCollege = (String) nextEntry.get("instituteName");

            boolean currentHasStart = currentStartDate > 0;
            boolean currentHasEnd = currentEndDate > 0;
            boolean nextHasStart = nextStartDate > 0;
            boolean nextHasEnd = nextEndDate > 0;

            boolean currentIsStartDateEntry = currentHasStart;
            boolean currentIsEndDateOnly = !currentHasStart && currentHasEnd;
            boolean currentIsNoDates = !currentHasStart && !currentHasEnd;

            boolean nextIsStartDateEntry = nextHasStart;
            boolean nextIsEndDateOnly = !nextHasStart && nextHasEnd;
            boolean nextIsNoDates = !nextHasStart && !nextHasEnd;

            if (currentIsStartDateEntry && nextIsStartDateEntry) {
                assertThat("Start date entries not sorted correctly. " + currentCollege +" start date: " + currentStartDate + " should be >= " + nextCollege +" start date: " + nextStartDate, currentStartDate, is(greaterThanOrEqualTo(nextStartDate)));
            } else if (currentIsStartDateEntry && nextIsEndDateOnly) {
                assertThat("Start date entry correctly positioned before end date only entry", true, is(true));
            } else if (currentIsStartDateEntry && nextIsNoDates) {
                assertThat("Start date entry correctly positioned before no dates entry", true, is(true));
            } else if (currentIsEndDateOnly && nextIsEndDateOnly) {
                assertThat("End date only entries not sorted correctly. " + currentCollege +" end date: " + currentEndDate + " should be >= " + nextCollege +" end date: " + nextEndDate, currentEndDate, is(greaterThanOrEqualTo(nextEndDate)));
            } else if (currentIsEndDateOnly && nextIsNoDates) {
                assertThat("End date only entry correctly positioned before no dates entry", true, is(true));
            } else if (currentIsNoDates && nextIsNoDates) {
                assertThat("No dates entries order maintained", true, is(true));
            } else {
                throw new AssertionError("Invalid sorting order detected. " + currentCollege +" (type: " + getEntryType(currentEntry) + ") should not come before " +nextCollege + " (type: " + getEntryType(nextEntry) + "). " +"Expected order: Start date entries → End date only entries → No dates entries");
            }
        }
    }

    private int getStartDate(Map<String, Object> entry) {
        Object startDate = entry.get("educationStartDate");
        return startDate != null ? (Integer) startDate : 0;
    }

    private int getEndDate(Map<String, Object> entry) {
        Object endDate = entry.get("educationEndDate");
        return endDate != null ? (Integer) endDate : 0;
    }

    private String getEntryType(Map<String, Object> entry) {
        int startDate = getStartDate(entry);
        int endDate = getEndDate(entry);

        if (startDate > 0) {
            return "Start date entry";
        } else if (endDate > 0) {
            return "End date only entry";
        } else {
            return "No dates entry";
        }
    }


    private void validateSameStartDateSorting(List<Map<String, Object>> educationHistory) {
        Map<Integer, List<Map<String, Object>>> startDateGroups = new HashMap<>();

        for (Map<String, Object> entry : educationHistory) {
            int startDate = getStartDate(entry);
            if (startDate > 0) {
                startDateGroups.computeIfAbsent(startDate, k -> new ArrayList<>()).add(entry);
            }
        }

        for (Map.Entry<Integer, List<Map<String, Object>>> entry : startDateGroups.entrySet()) {
            List<Map<String, Object>> sameStartDateEntries = entry.getValue();

            if (sameStartDateEntries.size() > 1) {
                for (int i = 0; i < sameStartDateEntries.size() - 1; i++) {
                    Map<String, Object> currentEntry = sameStartDateEntries.get(i);
                    Map<String, Object> nextEntry = sameStartDateEntries.get(i + 1);

                    int currentCreatedDate = getCreatedDate(currentEntry);
                    int nextCreatedDate = getCreatedDate(nextEntry);

                    String currentCollege = (String) currentEntry.get("instituteName");
                    String nextCollege = (String) nextEntry.get("instituteName");
                    assertThat("Entries with same start date should be sorted by created date (ascending). " + "Entry " + currentCollege + " created: " + currentCreatedDate + " should be <= Entry " + nextCollege + " created: " + nextCreatedDate, currentCreatedDate, is(lessThanOrEqualTo(nextCreatedDate)));
                }
            }
        }
    }

    private int getCreatedDate(Map<String, Object> entry) {
        Object createdDate = entry.get("createdOn");
        return createdDate != null ? (Integer) createdDate : 0;
    }

    private void validateGroupingLogic(List<Map<String, Object>> collegeGroups, String testType, HistoryGroupingValidationConfig config) {
        assertThat(testType + " grouping failed - expected " + config.getExpectedGroupCount() + " groups, got " + collegeGroups.size(), collegeGroups.size(), is(equalTo(config.getExpectedGroupCount())));

        if ("SINGLE_GROUP".equals(config.getGroupType())) {
            validateSingleGroup(collegeGroups, config, testType);
        } else if ("MULTIPLE_GROUPS".equals(config.getGroupType())) {
            validateMultipleGroups(collegeGroups, config, testType);
        }

        if (config.getExpectedEntityRecords() != null) {
            for (Map<String, Object> group : collegeGroups) {
                String collegeName = (String) group.get("instituteName");
                List<Map<String, Object>> educationHistories = (List<Map<String, Object>>) group.get("educationHistories");
                int actualRecordCount = educationHistories != null ? educationHistories.size() : 0;
                for (Map.Entry<String, Integer> expectedEntry : config.getExpectedEntityRecords().entrySet()) {
                    if (expectedEntry.getKey().equalsIgnoreCase(collegeName)) {
                        int expectedRecordCount = expectedEntry.getValue();
                        assertThat(testType + " validation failed - College '" + collegeName + "' expected " + expectedRecordCount + " records, but got " + actualRecordCount, actualRecordCount, is(equalTo(expectedRecordCount)));
                        break;
                    }
                }
            }
        }
    }

    private void validateSingleGroup(List<Map<String, Object>> collegeGroups, HistoryGroupingValidationConfig config, String testType) {
        String actualCollegeName = (String) collegeGroups.get(0).get("instituteName");

        if (config.getExpectedEntityName() != null) {
            assertThat(testType + " grouping failed - expected college name '" + config.getExpectedEntityName() + "', got '" + actualCollegeName + "'", actualCollegeName.toLowerCase(), is(equalTo(config.getExpectedEntityName().toLowerCase())));
        }

        if (config.getMinLength() > 0) {
            assertThat(testType + " grouping failed - college name should be longer than " + config.getMinLength() + " characters, got " + actualCollegeName.length(), actualCollegeName.length(), is(greaterThan(config.getMinLength())));
        }
    }

    private void validateMultipleGroups(List<Map<String, Object>> collegeGroups, HistoryGroupingValidationConfig config, String testType) {
        List<String> actualCollegeNames = collegeGroups.stream()
            .map(group -> (String) group.get("instituteName"))
            .collect(Collectors.toList());

        if (config.getExpectedEntityRecords() != null) {
            for (String expectedName : config.getExpectedEntityRecords().keySet()) {
                boolean found = actualCollegeNames.stream()
                    .anyMatch(actualName -> actualName.equalsIgnoreCase(expectedName));
                assertThat(testType + " grouping failed - expected college '" + expectedName + "' not found in " + actualCollegeNames, found, is(true));
            }
        }
    }


    private void performEducationHistoryUpdate(String candidateId, String originalCollege, String newCollege) {
        List<Map<String, Object>> educationHistory = getEducationHistoryForCandidate(candidateId);

        Map<String, Object> entryToUpdate = educationHistory.stream()
            .filter(entry -> originalCollege.equals(entry.get("instituteName")))
            .findFirst()
            .orElse(null);

        assertThat("Education history entry with college '" + originalCollege + "' not found", entryToUpdate, is(notNullValue()));

        JSONObject updatePayload = new JSONObject();
        updatePayload.put("id", entryToUpdate.get("id"));
        updatePayload.put("candidate_id", Integer.parseInt(candidateId));
        updatePayload.put("institute_name", newCollege);
        updatePayload.put("educational_qualification", entryToUpdate.get("educationalQualification"));
        updatePayload.put("educational_specialization", entryToUpdate.get("educationalSpecialization"));
        updatePayload.put("grade", entryToUpdate.get("grade"));
        updatePayload.put("education_location", entryToUpdate.get("educationLocation"));
        updatePayload.put("education_start_date", entryToUpdate.get("educationStartDate"));
        updatePayload.put("education_end_date", entryToUpdate.get("educationEndDate"));
        updatePayload.put("education_description", entryToUpdate.get("educationDescription"));
        updatePayload.put("created_by", entryToUpdate.get("createdBy"));
        updatePayload.put("updated_by", entryToUpdate.get("updatedBy"));
        updatePayload.put("created_on", entryToUpdate.get("createdOn"));
        updatePayload.put("updated_on", entryToUpdate.get("updatedOn"));
        updatePayload.put("candidate_slug", entryToUpdate.get("candidateSlug"));
        updatePayload.put("is_manually_added", entryToUpdate.get("isManuallyAdded"));

        String basePath = "candidates/candidate-education/" + entryToUpdate.get("id");
        Map<String, String> queryParams = new HashMap<>();

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken,
            queryParams, true, updatePayload);

        assertThat("Failed to update education history for candidate: " + candidateId, response.getStatusCode(), is(equalTo(200)));
    }
}
