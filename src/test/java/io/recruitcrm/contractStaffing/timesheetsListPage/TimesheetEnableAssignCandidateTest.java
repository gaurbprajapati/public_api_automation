package io.recruitcrm.contractStaffing.timesheetsListPage;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.TestUtil;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.ContractStaffingBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetEnableAssignCandidateTest extends ContractStaffingBaseTest {

    String albatrossAuthToken;
    String apiAuthToken;
    int ownerAccountID;
    private final Faker faker = new Faker();

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        ownerAccountID = ThreadManager.getAccount().getAccountId();
        createRuleEngineTemplate(albatrossAuthToken);
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "timesheetEnabledAssignedCandidatesData", groups = {"contract_staffing", "nightly-build"})
    public void verifyGetTimesheetEnabledAssignedCandidatesTest(int jobId, JsonPath candidate1, JsonPath candidate4) {
        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> jobIds = new ArrayList<>();
        jobIds.add(jobId);
        requestBody.put("jobIds", jobIds);

        Response response = executePost("jobs/get-timesheet-enabled-assigned-candidates", albatrossAuthToken,
                requestBody);

        assertThat("Response status should be 200", response.getStatusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta status should be 200", jsonPath.getInt("meta.status"), is(200));
        assertThat("Meta message should match", jsonPath.getString("meta.message"),
                is("Timesheet enabled assigned candidates fetched successfully"));
        assertThat("Response type context should be successful",
                jsonPath.getString("meta.responseType.context"), is("Request is successful"));
        assertThat("Request UUID should not be null", jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.getString("meta.timestamp"), notNullValue());

        List<Map<String, Object>> data = jsonPath.getList("data");
        assertThat("Data should not be null", data, notNullValue());
        assertThat("Data should be a list", data, instanceOf(List.class));
        assertThat("Data should contain exactly 2 candidates with timesheet enabled", data.size(), is(2));

        Map<String, String> expectedCandidateTitlesBySlug = new HashMap<>();
        expectedCandidateTitlesBySlug.put(candidate1.getString("slug"),
                candidate1.getString("first_name") + " " + candidate1.getString("last_name"));
        expectedCandidateTitlesBySlug.put(candidate4.getString("slug"),
                candidate4.getString("first_name") + " " + candidate4.getString("last_name"));

        for (int i = 0; i < data.size(); i++) {
            String candidateIndex = "Candidate" + (i + 1);
            int dataIndex = i;

            int candidateJobId = jsonPath.getInt("data[" + dataIndex + "].jobId");
            assertThat(candidateIndex + " Job ID should match expected jobId", candidateJobId, is(jobId));

            String actualTitle = jsonPath.getString("data[" + dataIndex + "].title");
            String actualSlug = jsonPath.getString("data[" + dataIndex + "].slug");
            assertThat(candidateIndex + " slug should match one of the expected candidates",
                    expectedCandidateTitlesBySlug, hasKey(actualSlug));
            assertThat(candidateIndex + " title should match", actualTitle,
                    is(expectedCandidateTitlesBySlug.get(actualSlug)));

            assertThat(candidateIndex + " email should not be null",
                    jsonPath.getString("data[" + dataIndex + "].email"), notNullValue());
            assertThat(candidateIndex + " entitytype should not be null",
                    jsonPath.get("data[" + dataIndex + "].entitytype"), notNullValue());
            assertThat(candidateIndex + " id should not be null",
                    jsonPath.get("data[" + dataIndex + "].id"), notNullValue());
            assertThat(candidateIndex + " srno should not be null",
                    jsonPath.get("data[" + dataIndex + "].srno"), notNullValue());
            assertThat(candidateIndex + " timesheetFrequency should not be null",
                    jsonPath.get("data[" + dataIndex + "].timesheetFrequency"), notNullValue());
            assertThat(candidateIndex + " timesheetStartDay should not be null",
                    jsonPath.get("data[" + dataIndex + "].timesheetStartDay"), notNullValue());
            assertThat(candidateIndex + " jobStartDate should not be null",
                    jsonPath.get("data[" + dataIndex + "].jobStartDate"), notNullValue());
            assertThat(candidateIndex + " jobEndDate should not be null",
                    jsonPath.get("data[" + dataIndex + "].jobEndDate"), notNullValue());
        }
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyGetTimesheetEnabledAssignedCandidatesWithEmptyBodyTest() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jobIds", new ArrayList<>());

        Response response = executePost("jobs/get-timesheet-enabled-assigned-candidates", albatrossAuthToken,
                requestBody);

        assertThat("Response status should be 400", response.getStatusCode(), is(400));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta status should be 400", jsonPath.getInt("meta.status"), is(400));
        assertThat("Meta message should match", jsonPath.getString("meta.message"),
                is("Job IDs list cannot be empty"));
        assertThat("Response type code should be 101", jsonPath.getInt("meta.responseType.code"), is(101));
        assertThat("Response type context should match", jsonPath.getString("meta.responseType.context"),
                is("Error while processing request"));
        assertThat("Request UUID should not be null", jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.getString("meta.timestamp"), notNullValue());
        assertThat("Data should be null", jsonPath.get("data"), nullValue());
        assertThat("Errors should be empty", jsonPath.getList("errors").isEmpty(), is(true));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyGetTimesheetEnabledAssignedCandidatesWithInvalidJobIdTest() {
        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> jobIds = new ArrayList<>();
        int invalidJobId = faker.number().numberBetween(10000000, 99999999); // Invalid job ID
        jobIds.add(invalidJobId);
        requestBody.put("jobIds", jobIds);

        Response response = executePost("jobs/get-timesheet-enabled-assigned-candidates", albatrossAuthToken,
                requestBody);

        assertThat("Response status should be 404", response.getStatusCode(), is(404));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta status should be 404", jsonPath.getInt("meta.status"), is(404));
        assertThat("Meta message should be null", jsonPath.get("meta.message"), nullValue());
        assertThat("Response type code should be 101", jsonPath.getInt("meta.responseType.code"), is(101));
        assertThat("Response type context should match", jsonPath.getString("meta.responseType.context"),
                is("Error while processing request"));
        assertThat("Request UUID should not be null", jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.getString("meta.timestamp"), notNullValue());
        assertThat("Data should be null", jsonPath.get("data"), nullValue());

        List<Map<String, Object>> errors = jsonPath.getList("errors");
        assertThat("Errors should not be empty", errors.isEmpty(), is(false));
        assertThat("Error message should match", jsonPath.getString("errors[0].message"),
                is("No accessible jobs found for the provided job IDs."));
        assertThat("Error type context should match", jsonPath.getString("errors[0].errorType.context"),
                is("Generic Error"));
        assertThat("Error type code should be 202", jsonPath.getInt("errors[0].errorType.code"), is(202));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyGetTimesheetEnabledAssignedCandidatesUnauthorizedTest() {
        Map<String, Object> requestBody = new HashMap<>();
        List<Integer> jobIds = new ArrayList<>();
        int randomJobId = faker.number().numberBetween(1000000, 9999999);
        jobIds.add(randomJobId);
        requestBody.put("jobIds", jobIds);

        String invalidToken = "invalid_token_123";
        Response response = executePost("jobs/get-timesheet-enabled-assigned-candidates", invalidToken, requestBody);

        assertThat("Response status should be 401", response.getStatusCode(), is(401));
        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta status should be 401", jsonPath.getInt("meta.status"), is(401));
        assertThat("Meta message should match", jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat("Response type context should be Warning", jsonPath.getString("meta.responseType.context"),
                is("Warning"));
        assertThat("Response type code should be 104", jsonPath.getInt("meta.responseType.code"), is(104));
        assertThat("Request UUID should not be null", jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.getString("meta.timestamp"), notNullValue());
        assertThat("Data should be 'Internal Server Error'", jsonPath.getString("data"), is("Internal Server Error"));
        List<Object> errors = jsonPath.getList("errors");
        assertThat("Errors should not be null", errors, notNullValue());
        assertThat("Errors should be an empty array", errors, empty());
    }

    @DataProvider(name = "timesheetEnabledAssignedCandidatesData", parallel = true)
    public Object[][] getTimesheetEnabledAssignedCandidatesData() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            // Parallel: Create 4 candidates
            CompletableFuture<JsonPath> candidate1Future = CompletableFuture.supplyAsync(() ->
                    function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(), executor);

            CompletableFuture<JsonPath> candidate2Future = CompletableFuture.supplyAsync(() ->
                    function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(), executor);

            CompletableFuture<JsonPath> candidate3Future = CompletableFuture.supplyAsync(() ->
                    function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(), executor);

            CompletableFuture<JsonPath> candidate4Future = CompletableFuture.supplyAsync(() ->
                    function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath(), executor);

            // Wait for all candidates to be created
            CompletableFuture.allOf(candidate1Future, candidate2Future, candidate3Future, candidate4Future).join();

            JsonPath jsonCandidate1 = candidate1Future.join();
            JsonPath jsonCandidate2 = candidate2Future.join();
            JsonPath jsonCandidate3 = candidate3Future.join();
            JsonPath jsonCandidate4 = candidate4Future.join();

            String candidateSlug1 = jsonCandidate1.getString("slug");
            String candidateSlug2 = jsonCandidate2.getString("slug");
            String candidateSlug3 = jsonCandidate3.getString("slug");
            String candidateSlug4 = jsonCandidate4.getString("slug");

            // Sequential: Create company (required for contact)
            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.getString("slug");

            // Sequential: Create contact (required for job)
            JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            String contactSlug = jsonContact.getString("slug");

            // Sequential: Create job (required for assignments)
            JsonPath jsonJob = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
            String jobSlug = jsonJob.getString("slug");

            // Parallel: Assign candidates to the job
            CompletableFuture<Void> assign1Future = CompletableFuture.runAsync(() ->
                    function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug1, jobSlug), executor);

            CompletableFuture<Void> assign2Future = CompletableFuture.runAsync(() ->
                    function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug2, jobSlug), executor);

            CompletableFuture<Void> assign4Future = CompletableFuture.runAsync(() ->
                    function.assignJobToCandidate(baseURL, apiAuthToken, candidateSlug4, jobSlug), executor);

            // Wait for all assignments to complete
            CompletableFuture.allOf(assign1Future, assign2Future, assign4Future).join();
            // candidate3 is NOT assigned

            // Sequential: Get users to get userId (used as agencyId)
            Response usersResponse = function.getUsers(baseURL, apiAuthToken);
            JsonPath usersJsonPath = usersResponse.jsonPath();
            int userId = usersJsonPath.getInt("[0].id");

            // Parallel: Get candidate IDs from albatross
            CompletableFuture<Integer> candidateId1Future = CompletableFuture.supplyAsync(() ->
                    function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug1, "candidate")
                            .jsonPath().getInt("data.candidate.id"), executor);

            CompletableFuture<Integer> candidateId2Future = CompletableFuture.supplyAsync(() ->
                    function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug2, "candidate")
                            .jsonPath().getInt("data.candidate.id"), executor);

            CompletableFuture<Integer> candidateId3Future = CompletableFuture.supplyAsync(() ->
                    function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug3, "candidate")
                            .jsonPath().getInt("data.candidate.id"), executor);

            CompletableFuture<Integer> candidateId4Future = CompletableFuture.supplyAsync(() ->
                    function.getEntityDetail(albatrossURL, albatrossAuthToken, candidateSlug4, "candidate")
                            .jsonPath().getInt("data.candidate.id"), executor);

            // Sequential: Get job ID from albatross (required before enabling timesheets)
            int jobId = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug, "job").jsonPath()
                    .getInt("data.job.id");

            // Wait for all candidate IDs to be retrieved
            CompletableFuture.allOf(candidateId1Future, candidateId2Future, candidateId3Future, candidateId4Future).join();

            int candidateId1 = candidateId1Future.join();
            int candidateId2 = candidateId2Future.join();
            int candidateId3 = candidateId3Future.join();
            int candidateId4 = candidateId4Future.join();

           
            enableTimesheet(candidateId1, jobId, userId, albatrossAuthToken, 2, 200, 0);
            enableTimesheet(candidateId4, jobId, userId, albatrossAuthToken, 3, 200, 0);

            return new Object[][] { { jobId, jsonCandidate1, jsonCandidate4 } };
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private Response executePost(String endpoint, String authToken, Object payload) {
        Object requestPayload = payload;
        if (payload instanceof Map) {
            requestPayload = TestUtil.getSerializedJSON(payload);
        }
        return RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, null, true, requestPayload);
    }
}
