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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class GetCandidateWorkHistoryTest extends TestBase {
    String albatrossAuthToken;
    String accountAPIKey;
    AllCrudFunctions function = new AllCrudFunctions();
    ConcurrentHashMap<String, String> candidateIdMap = new ConcurrentHashMap<>();

    public GetCandidateWorkHistoryTest() {
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
    public void getCandidateWorkHistory_Success() {
        int candidateId = Integer.parseInt(candidateIdMap.get("candidate1"));
        String basePath = "candidates/{candidateId}/work-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));
        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, pathParams, true);

        assertThat("Response should not be null", response, is(notNullValue()));
        assertThat("API should return 200 status code", response.getStatusCode(), is(equalTo(200)));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta section should not be null", jsonPath.get("meta"), is(notNullValue()));
        assertThat("Meta message should match expected", jsonPath.get("meta.message"), is(equalTo("Work history fetched successfully")));
        assertThat("Request UUID should not be null", jsonPath.get("meta.requestUuid"), is(notNullValue()));
        assertThat("Timestamp should not be null", jsonPath.get("meta.timestamp"), is(notNullValue()));

        List<Map<String, Object>> data = jsonPath.getList("data");
        assertThat("Data section should not be null", data, is(notNullValue()));
        assertThat("Data section should not be empty", data.isEmpty(), is(false));

        Map<String, Object> companyGroup = data.get(0);
        assertThat("Company name should not be null", companyGroup.get("companyName"), is(notNullValue()));
        assertThat("Work histories should not be null", companyGroup.get("workHistories"), is(notNullValue()));

        List<Map<String, Object>> workHistories = (List<Map<String, Object>>) companyGroup.get("workHistories");
        assertThat("Work histories list should not be null", workHistories, is(notNullValue()));
        assertThat("Work histories list should not be empty", workHistories.isEmpty(), is(false));

        for (Map<String, Object> workHistory : workHistories) {
            assertThat("Work history ID should not be null", workHistory.get("id"), is(notNullValue()));
            assertThat("Candidate ID should not be null", workHistory.get("candidateId"), is(notNullValue()));
            assertThat("Title should not be null", workHistory.get("title"), is(notNullValue()));
            assertThat("Work company name should not be null", workHistory.get("workCompanyName"), is(notNullValue()));
            assertThat("Employment type should not be null", workHistory.get("employmentType"), is(notNullValue()));
            assertThat("Industry ID should not be null", workHistory.get("industryId"), is(notNullValue()));
            assertThat("Work location should not be null", workHistory.get("workLocation"), is(notNullValue()));
            assertThat("Salary should not be null", workHistory.get("salary"), is(notNullValue()));
            assertThat("Is currently working should not be null", workHistory.get("isCurrentlyWorking"), is(notNullValue()));
            assertThat("Work start date should not be null", workHistory.get("workStartDate"), is(notNullValue()));
            assertThat("Work end date should not be null", workHistory.get("workEndDate"), is(notNullValue()));
            assertThat("Work description should not be null", workHistory.get("workDescription"), is(notNullValue()));
            assertThat("Candidate slug should not be null", workHistory.get("candidateSlug"), is(notNullValue()));
            assertThat("Is manually added should not be null", workHistory.get("isManuallyAdded"), is(notNullValue()));
            assertThat("Created by should not be null", workHistory.get("createdBy"), is(notNullValue()));
            assertThat("Updated by should not be null", workHistory.get("updatedBy"), is(notNullValue()));
            assertThat("Created on should not be null", workHistory.get("createdOn"), is(notNullValue()));
            assertThat("Updated on should not be null", workHistory.get("updatedOn"), is(notNullValue()));
        }

        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/candidate/GetCandidateWorkHistory.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateWorkHistory_WithoutAuth() {
        int candidateId = Integer.parseInt(candidateIdMap.get("candidate1"));
        String basePath = "candidates/{candidateId}/work-history";
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
    public void getCandidateWorkHistory_InvalidAuth() {
        int candidateId = Integer.parseInt(candidateIdMap.get("candidate1"));
        String basePath = "candidates/{candidateId}/work-history";
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
    public void getCandidateWorkHistory_InvalidCandidateId() {
        String basePath = "candidates/999999999/work-history";
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
    public void testWorkHistorySorting(String candidateId, String testType) {
        List<Map<String, Object>> workHistory = getWorkHistoryForCandidate(candidateId);
        validateBasicDateSorting(workHistory);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "groupingScenarioData", groups = {"candidate_service", "nightly-build"})
    public void testWorkHistoryGroupingLogic(String candidateId, String testType, HistoryGroupingValidationConfig config) {
        List<Map<String, Object>> companyGroups = getGroupedWorkHistoryForCandidate(candidateId);
        validateGroupingLogic(companyGroups, testType, config);
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "dynamicUpdateScenarioData", groups = {"candidate_service", "nightly-build"})
    public void testDynamicWorkHistoryUpdates(String candidateId, String updateType, String originalCompany, String newCompany) {
        performWorkHistoryUpdate(candidateId, originalCompany, newCompany);

        List<Map<String, Object>> updatedWorkHistory = getWorkHistoryForCandidate(candidateId);

        switch (updateType) {
            case "Merge Groups": {
                validateBasicDateSorting(updatedWorkHistory);
                List<Map<String, Object>> companyGroups = getGroupedWorkHistoryForCandidate(candidateId);
                validateGroupingLogic(companyGroups, updateType, new HistoryGroupingValidationConfig(2, "MULTIPLE_GROUPS", null, Map.of("Amazon", 2, "Netflix", 2)));
                break;
            }
            case "Split Groups": {
                validateBasicDateSorting(updatedWorkHistory);
                List<Map<String, Object>> companyGroups = getGroupedWorkHistoryForCandidate(candidateId);
                validateGroupingLogic(companyGroups, updateType, new HistoryGroupingValidationConfig(3, "MULTIPLE_GROUPS", null, Map.of("Facebook", 2, "Meta", 1, "LinkedIn", 1)));
                break;
            }
            case "Merge and Split Groups": {
                validateBasicDateSorting(updatedWorkHistory);
                List<Map<String, Object>> companyGroups = getGroupedWorkHistoryForCandidate(candidateId);
                validateGroupingLogic(companyGroups, updateType, new HistoryGroupingValidationConfig(3, "MULTIPLE_GROUPS", null, Map.of("Airbnb", 1, "Uber", 3, "Slack", 1)));
                break;
            }
        }
    }


    @DataProvider(name = "allSortingScenarioData")
    public Object[][] getAllSortingScenarioData() {
        return new Object[][] {
                {candidateIdMap.get("candidate1"), "Basic Date Sorting"},
                {candidateIdMap.get("candidate2"), "Intra-Company Ordering"},
                {candidateIdMap.get("candidate3"), "Inter-Company Ordering"}
        };
    }

    @DataProvider(name = "groupingScenarioData")
    public Object[][] getGroupingScenarioData() {
        return new Object[][] {
                {candidateIdMap.get("candidate4"), "Case Sensitivity", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "capgemini", Map.of("capgemini", 3))},
                {candidateIdMap.get("candidate5"), "Special Characters", new HistoryGroupingValidationConfig(2, "MULTIPLE_GROUPS", null, Map.of("AT&T", 2, "O'Malley's Firm", 2))},
                {candidateIdMap.get("candidate6"), "Whitespace Handling", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "recruitcrm", Map.of("recruitcrm", 4))},
                {candidateIdMap.get("candidate7"), "Multiple Spaces", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", "workforce cloud", Map.of("workforce cloud", 3))},
                {candidateIdMap.get("candidate8"), "Similar Distinct", new HistoryGroupingValidationConfig(3, "MULTIPLE_GROUPS", null, Map.of("Smith Consulting", 1, "Smith Consultants", 1, "Smith's Consulting", 1))},
                {candidateIdMap.get("candidate9"), "Empty Company Names", new HistoryGroupingValidationConfig(2, "SINGLE_GROUP", null, Map.of("Not Available", 2))},
                {candidateIdMap.get("candidate10"), "Very Long Names", new HistoryGroupingValidationConfig(1, "SINGLE_GROUP", null, Map.of("Very Long Company Name", 2), 100)}
        };
    }

    @DataProvider(name = "dynamicUpdateScenarioData")
    public Object[][] getDynamicUpdateScenarioData() {
        return new Object[][] {
                {candidateIdMap.get("candidate11"), "Merge Groups", "Youtube", "Netflix"},
                {candidateIdMap.get("candidate12"), "Split Groups", "Facebook", "Meta"},
                {candidateIdMap.get("candidate13"), "Merge and Split Groups", "Airbnb", "UBER"}
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

    private List<Map<String, Object>> getWorkHistoryForCandidate(String candidateId) {
        String basePath = "candidates/{candidateId}/work-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", candidateId);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, pathParams, true);
        assertThat("Failed to get work history for candidate: " + candidateId, response.getStatusCode(), is(equalTo(200)));

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> companyGroups = jsonPath.getList("data");

        List<Map<String, Object>> workHistory = new ArrayList<>();
        for (Map<String, Object> companyGroup : companyGroups) {
            List<Map<String, Object>> workHistories = (List<Map<String, Object>>) companyGroup.get("workHistories");
            if (workHistories != null) {
                workHistory.addAll(workHistories);
            }
        }

        return workHistory;
    }

    private List<Map<String, Object>> getGroupedWorkHistoryForCandidate(String candidateId) {
        String basePath = "candidates/{candidateId}/work-history";
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", candidateId);

        Response response = RestClient.doGet("JSON", candidatesURL, basePath, albatrossAuthToken, null, pathParams, true);
        assertThat("Failed to get work history for candidate: " + candidateId, response.getStatusCode(), is(equalTo(200)));

        JsonPath jsonPath = response.jsonPath();
        return jsonPath.getList("data");
    }


    private void validateBasicDateSorting(List<Map<String, Object>> workHistory) {
        validateOverallSortingOrder(workHistory);
        validateSameStartDateSorting(workHistory);
    }

    private void validateOverallSortingOrder(List<Map<String, Object>> workHistory) {
        for (int i = 0; i < workHistory.size() - 1; i++) {
            Map<String, Object> currentEntry = workHistory.get(i);
            Map<String, Object> nextEntry = workHistory.get(i + 1);

            int currentStartDate = getStartDate(currentEntry);
            int currentEndDate = getEndDate(currentEntry);
            int nextStartDate = getStartDate(nextEntry);
            int nextEndDate = getEndDate(nextEntry);

            String currentCompany = (String) currentEntry.get("workCompanyName");
            String nextCompany = (String) nextEntry.get("workCompanyName");

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
                assertThat("Start date entries not sorted correctly. " + currentCompany +" start date: " + currentStartDate + " should be >= " + nextCompany +" start date: " + nextStartDate, currentStartDate, is(greaterThanOrEqualTo(nextStartDate)));
            } else if (currentIsStartDateEntry && nextIsEndDateOnly) {
                assertThat("Start date entry correctly positioned before end date only entry", true, is(true));
            } else if (currentIsStartDateEntry && nextIsNoDates) {
                assertThat("Start date entry correctly positioned before no dates entry", true, is(true));
            } else if (currentIsEndDateOnly && nextIsEndDateOnly) {
                assertThat("End date only entries not sorted correctly. " + currentCompany +" end date: " + currentEndDate + " should be >= " + nextCompany +" end date: " + nextEndDate, currentEndDate, is(greaterThanOrEqualTo(nextEndDate)));
            } else if (currentIsEndDateOnly && nextIsNoDates) {
                assertThat("End date only entry correctly positioned before no dates entry", true, is(true));
            } else if (currentIsNoDates && nextIsNoDates) {
                assertThat("No dates entries order maintained", true, is(true));
            } else {
                throw new AssertionError("Invalid sorting order detected. " + currentCompany +" (type: " + getEntryType(currentEntry) + ") should not come before " +nextCompany + " (type: " + getEntryType(nextEntry) + "). " +"Expected order: Start date entries → End date only entries → No dates entries");
            }
        }
    }

    private int getStartDate(Map<String, Object> entry) {
        Object startDate = entry.get("workStartDate");
        return startDate != null ? (Integer) startDate : 0;
    }

    private int getEndDate(Map<String, Object> entry) {
        Object endDate = entry.get("workEndDate");
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


    private void validateSameStartDateSorting(List<Map<String, Object>> workHistory) {
        Map<Integer, List<Map<String, Object>>> startDateGroups = new HashMap<>();

        for (Map<String, Object> entry : workHistory) {
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

                    String currentCompany = (String) currentEntry.get("workCompanyName");
                    String nextCompany = (String) nextEntry.get("workCompanyName");

                    assertThat("Entries with same start date should be sorted by created date (ascending). " + "Entry " + currentCompany + " created: " + currentCreatedDate + " should be <= Entry " + nextCompany + " created: " + nextCreatedDate, currentCreatedDate, is(lessThanOrEqualTo(nextCreatedDate)));
                }
            }
        }
    }

    private int getCreatedDate(Map<String, Object> entry) {
        Object createdDate = entry.get("createdOn");
        return createdDate != null ? (Integer) createdDate : 0;
    }

    private void validateGroupingLogic(List<Map<String, Object>> companyGroups, String testType, HistoryGroupingValidationConfig config) {
        assertThat(testType + " grouping failed - expected " + config.getExpectedGroupCount() + " groups, got " + companyGroups.size(), companyGroups.size(), is(equalTo(config.getExpectedGroupCount())));

        if ("SINGLE_GROUP".equals(config.getGroupType())) {
            validateSingleGroup(companyGroups, config, testType);
        } else if ("MULTIPLE_GROUPS".equals(config.getGroupType())) {
            validateMultipleGroups(companyGroups, config, testType);
        }

        if (config.getExpectedEntityRecords() != null) {
            for (Map<String, Object> group : companyGroups) {
                String companyName = (String) group.get("companyName");
                List<Map<String, Object>> workHistories = (List<Map<String, Object>>) group.get("workHistories");
                int actualRecordCount = workHistories != null ? workHistories.size() : 0;

                for (Map.Entry<String, Integer> expectedEntry : config.getExpectedEntityRecords().entrySet()) {
                    if (expectedEntry.getKey().equalsIgnoreCase(companyName)) {
                        int expectedRecordCount = expectedEntry.getValue();
                        assertThat(testType + " validation failed - Company '" + companyName + "' expected " + expectedRecordCount + " records, but got " + actualRecordCount, actualRecordCount, is(equalTo(expectedRecordCount)));
                        break;
                    }
                }
            }
        }
    }

    private void validateSingleGroup(List<Map<String, Object>> companyGroups, HistoryGroupingValidationConfig config, String testType) {
        String actualCompanyName = (String) companyGroups.get(0).get("companyName");

        if (config.getExpectedEntityName() != null) {
            assertThat(testType + " grouping failed - expected company name '" + config.getExpectedEntityName() + "', got '" + actualCompanyName + "'", actualCompanyName.toLowerCase(), is(equalTo(config.getExpectedEntityName().toLowerCase())));
        }

        if (config.getMinLength() > 0) {
            assertThat(testType + " grouping failed - company name should be longer than " + config.getMinLength() + " characters, got " + actualCompanyName.length(), actualCompanyName.length(), is(greaterThan(config.getMinLength())));
        }
    }

    private void validateMultipleGroups(List<Map<String, Object>> companyGroups, HistoryGroupingValidationConfig config, String testType) {
        List<String> actualCompanyNames = companyGroups.stream()
            .map(group -> (String) group.get("companyName"))
            .collect(Collectors.toList());

        if (config.getExpectedEntityRecords() != null) {
            for (String expectedName : config.getExpectedEntityRecords().keySet()) {
                boolean found = actualCompanyNames.stream()
                    .anyMatch(actualName -> actualName.equalsIgnoreCase(expectedName));
                assertThat(testType + " grouping failed - expected company '" + expectedName + "' not found in " + actualCompanyNames, found, is(true));
            }
        }
    }


    private void performWorkHistoryUpdate(String candidateId, String originalCompany, String newCompany) {
        List<Map<String, Object>> workHistory = getWorkHistoryForCandidate(candidateId);

        Map<String, Object> entryToUpdate = workHistory.stream()
            .filter(entry -> originalCompany.equals(entry.get("workCompanyName")))
            .findFirst()
            .orElse(null);

        assertThat("Work history entry with company '" + originalCompany + "' not found", entryToUpdate, is(notNullValue()));

        JSONObject updatePayload = new JSONObject();
        updatePayload.put("id", entryToUpdate.get("id"));
        updatePayload.put("candidate_id", Integer.parseInt(candidateId));
        updatePayload.put("title", entryToUpdate.get("title"));
        updatePayload.put("work_company_name", newCompany);
        updatePayload.put("employment_type", entryToUpdate.get("employmentType"));
        updatePayload.put("industry_id", entryToUpdate.get("industryId"));
        updatePayload.put("work_location", entryToUpdate.get("workLocation"));
        updatePayload.put("salary", entryToUpdate.get("salary"));
        updatePayload.put("is_currently_working", (Boolean) entryToUpdate.get("isCurrentlyWorking") ? 1 : 0);
        updatePayload.put("work_start_date", entryToUpdate.get("workStartDate"));
        updatePayload.put("work_end_date", entryToUpdate.get("workEndDate"));
        updatePayload.put("work_description", entryToUpdate.get("workDescription"));
        updatePayload.put("created_by", entryToUpdate.get("createdBy"));
        updatePayload.put("updated_by", entryToUpdate.get("updatedBy"));
        updatePayload.put("created_on", entryToUpdate.get("createdOn"));
        updatePayload.put("updated_on", entryToUpdate.get("updatedOn"));
        updatePayload.put("candidate_slug", entryToUpdate.get("candidateSlug"));
        updatePayload.put("is_manually_added", entryToUpdate.get("isManuallyAdded"));

        String basePath = "candidates/candidate-work/" + entryToUpdate.get("id");
        Map<String, String> queryParams = new HashMap<>();

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, queryParams, true, updatePayload);

        assertThat("Failed to update work history for candidate: " + candidateId, response.getStatusCode(), is(equalTo(200)));
    }


}
