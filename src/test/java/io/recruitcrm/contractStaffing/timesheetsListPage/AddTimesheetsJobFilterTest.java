package io.recruitcrm.contractStaffing.timesheetsListPage;

import com.github.javafaker.Faker;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.TestUtil;
import io.rcrm.api.pojo.Job;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class AddTimesheetsJobFilterTest extends ContractStaffingBaseTest {

    String albatrossAuthToken;
    String apiAuthToken;
    Faker faker = new Faker();

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createRuleEngineTemplate(albatrossAuthToken);
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "searchJobFilterData", groups = {"contract_staffing", "nightly-build"})
    public void verifySearchEntityWithJobFilterTest(String searchTerm, String filterType, int expectedJobCount,
            Map<String, Map<String, Object>> allJobsData) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("search", searchTerm);
        requestBody.put("companies", false);
        requestBody.put("jobs", true);
        requestBody.put("deals", false);
        requestBody.put("fromContractorsListPage", true);
        requestBody.put("ids_of_selected_records", new HashMap<>());

        Response response = executePost("timesheets/search-entity", albatrossAuthToken, requestBody);

        System.out.println("timesheets/search-entity "+filterType + response.prettyPrint() + "");
        assertThat("Response status should be 200", response.getStatusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta status should be 200", jsonPath.getInt("meta.status"), is(200));
        assertThat("Meta message should match", jsonPath.getString("meta.message"),
                is("Entities searched successfully"));
        assertThat("Response type context should be successful",
                jsonPath.getString("meta.responseType.context"), is("Request is successful"));
        assertThat("Request UUID should not be null", jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.getString("meta.timestamp"), notNullValue());

        // Assert data structure exists
        assertThat("Data object should not be null", jsonPath.get("data"), notNullValue());
        assertThat("Data.data object should not be null", jsonPath.get("data.data"), notNullValue());

        // Assert that data.data is a Map/Object
        Map<String, Object> dataDataMap = jsonPath.getMap("data.data");
        assertThat("Data.data should be a map/object", dataDataMap, notNullValue());
        assertThat("Data.data should contain key '4' for jobs", dataDataMap.containsKey("4"), is(true));

        // Get jobs from response (entity type 4 = jobs)
        List<Map<String, Object>> jobs = jsonPath.getList("data.data.4");
        assertThat("Jobs list should not be null", jobs, notNullValue());
        assertThat("Jobs list should be a List", jobs, instanceOf(List.class));
        assertThat("Expected job count should match", jobs.size(), is(expectedJobCount));
        assertThat("Jobs array length should be " + expectedJobCount, jobs.size(), is(expectedJobCount));

        if (filterType.equalsIgnoreCase("allData")) {
            // Extract lists from response
            List<String> titles = jsonPath.getList("data.data.'4'.title");
            List<String> cities = jsonPath.getList("data.data.'4'.location");
            List<String> companySlugs = jsonPath.getList("data.data.'4'.companyslug");
            List<String> companyNames = jsonPath.getList("data.data.'4'.companynameforjob");
            List<String> entityTypes = jsonPath.getList("data.data.'4'.entitytype");
            List<String> slugs = jsonPath.getList("data.data.'4'.slug");
            List<Integer> srnos = jsonPath.getList("data.data.'4'.srno");
            List<Integer> ids = jsonPath.getList("data.data.'4'.id");

            List<Integer> presentJobTypes = new ArrayList<>();

            // Iterate through each job in response
            for (int i = 0; i < jobs.size(); i++) {
                Integer responseId = ids.get(i);
                Integer responseSrno = srnos.get(i);
                String responseTitle = titles.get(i);
                String responseCity = cities.get(i);
                String responseCompanySlug = companySlugs.get(i);
                String responseCompanyName = companyNames.get(i);
                String responseEntityType = entityTypes.get(i);
                String responseSlug = slugs.get(i);

                Map<String, Object> matchedJobData = null;
                for (Map.Entry<String, Map<String, Object>> entry : allJobsData.entrySet()) {
                    Map<String, Object> jobData = entry.getValue();
                    Integer jobIdDB = (Integer) jobData.get("id");
                    Integer jobSrno = (Integer) jobData.get("srno");
                    if (responseId.equals(jobIdDB) || responseSrno.equals(jobSrno)) {
                        matchedJobData = jobData;
                        break;
                    }
                }

                if (matchedJobData != null) {
                    Integer jobType = (Integer) matchedJobData.get("jobType");
                    presentJobTypes.add(jobType);
                    if (jobType == 3 || jobType == 4) {
                        assertThat("Job type " + jobType + " - title", responseTitle,
                                is(matchedJobData.get("jobName")));
                        assertThat("Job type " + jobType + " - id", responseId, is(matchedJobData.get("id")));
                        assertThat("Job type " + jobType + " - srno", responseSrno, is(matchedJobData.get("srno")));
                        assertThat("Job type " + jobType + " - location", responseCity,
                                is(matchedJobData.get("jobCity")));
                        assertThat("Job type " + jobType + " - company slug", responseCompanySlug,
                                is(matchedJobData.get("companySlug")));
                        assertThat("Job type " + jobType + " - company name", responseCompanyName,
                                is(matchedJobData.get("companyName")));
                        assertThat("Job type " + jobType + " - entity type", responseEntityType, is("4"));
                        assertThat("Job type " + jobType + " - slug", responseSlug, notNullValue());
                    } else if (jobType == 2) {
                        // This should not happen - fail if type 2 is found
                        assertThat("CRITICAL: Job type 2 must NOT be present in response", false, is(true));
                    }
                }
            }
            assertThat("Job type 3 must be present", presentJobTypes, hasItem(3));
            assertThat("Job type 4 must be present", presentJobTypes, hasItem(4));
            assertThat("Job type 2 must NOT be present", presentJobTypes, not(hasItem(2)));
            for (Map.Entry<String, Map<String, Object>> entry : allJobsData.entrySet()) {
                Map<String, Object> jobData = entry.getValue();
                Integer jobType = (Integer) jobData.get("jobType");
                Integer jobIdDB = (Integer) jobData.get("id");

                if (jobType == 3 || jobType == 4) {
                    assertThat("Job type " + jobType + " with DB ID " + jobIdDB + " must be present",
                            ids, hasItem(jobIdDB));
                } else if (jobType == 2) {
                    assertThat("CRITICAL: Job type 2 with DB ID " + jobIdDB + " must NOT be present",
                            ids, not(hasItem(jobIdDB)));
                }
            }

        } else {
            if (expectedJobCount > 0) {
                Map<String, Object> job = jobs.get(0);
                Map<String, Object> expectedJobData = allJobsData.get(filterType);
                assertThat("Job title should match", job.get("title"), is(expectedJobData.get("jobName")));
                assertThat("Job id (DB ID) should match", job.get("id"), is(expectedJobData.get("id")));
                assertThat("Job srno (API response ID) should match", job.get("srno"),
                        is(expectedJobData.get("srno")));
                assertThat("Job location (city) should match", job.get("location"),
                        is(expectedJobData.get("jobCity")));
                assertThat("Company slug should match", job.get("companyslug"),
                        is(expectedJobData.get("companySlug")));
                assertThat("Company name should match", job.get("companynameforjob"),
                        is(expectedJobData.get("companyName")));
                assertThat("Entity type should be 4", job.get("entitytype"), is("4"));
                assertThat("Job slug should not be null", job.get("slug"), notNullValue());

            } else {
                assertThat("Jobs list should be empty when searching for job type 2", jobs.size(), is(0));
                assertThat("Jobs list should be empty", jobs, empty());
                List<Map<String, Object>> dataJobs = jsonPath.getList("data.data.4");
                assertThat("Data jobs list should be empty", dataJobs, notNullValue());
                assertThat("Data jobs list size should be 0", dataJobs.size(), is(0));
                assertThat("Data jobs list should be empty", dataJobs, empty());
            }
        }
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifySearchEntityWithJobFilterUnauthorizedTest() {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("search", "");
        requestBody.put("companies", false);
        requestBody.put("jobs", true);
        requestBody.put("deals", false);
        requestBody.put("ids_of_selected_records", new HashMap<>());

        String invalidToken = "invalid_token_123";
        Response response = executePost("timesheets/search-entity", invalidToken, requestBody);

        assertThat("Response status should be 401", response.getStatusCode(), is(401));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Meta status should be 401", jsonPath.getInt("meta.status"), is(401));
        assertThat("Meta message should match", jsonPath.getString("meta.message"), is("Unauthorised access"));
        assertThat("Response type context should be Warning",
                jsonPath.getString("meta.responseType.context"), is("Warning"));
        assertThat("Response type code should be 104",
                jsonPath.getInt("meta.responseType.code"), is(104));
        assertThat("Request UUID should not be null", jsonPath.getString("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jsonPath.getString("meta.timestamp"), notNullValue());
        assertThat("Data should be 'Internal Server Error'", jsonPath.getString("data"), is("Internal Server Error"));
        List<Object> errors = jsonPath.getList("errors");
        assertThat("Errors should not be null", errors, notNullValue());
        assertThat("Errors should be an empty array", errors, empty());
    }

    @DataProvider(name = "searchJobFilterData", parallel = true)
    public Object[][] getSearchJobFilterData() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            // Phase 1: Create company (required for contact)
            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.getString("slug");
            String companyName = jsonCompany.getString("company_name");

            // Phase 2: Create contact (required for jobs)
            JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            String contactSlug = jsonContact.getString("slug");

            // Phase 3: Create 3 jobs in parallel (independent operations)
            String jobName1 = "Job Type 2 - " + faker.job().title();
            String jobCity1 = faker.address().city();
            CompletableFuture<Response> job1Future = CompletableFuture
                    .supplyAsync(() -> createNewJobWithTypeAndCity(baseURL, apiAuthToken, companySlug, contactSlug,
                            jobName1, 2, jobCity1), executor);

            String jobName2 = "Job Type 3 - " + faker.job().title();
            String jobCity2 = faker.address().city();
            CompletableFuture<Response> job2Future = CompletableFuture
                    .supplyAsync(() -> createNewJobWithTypeAndCity(baseURL, apiAuthToken, companySlug, contactSlug,
                            jobName2, 3, jobCity2), executor);

            String jobName3 = "Job Type 4 - " + faker.job().title();
            String jobCity3 = faker.address().city();
            CompletableFuture<Response> job3Future = CompletableFuture
                    .supplyAsync(() -> createNewJobWithTypeAndCity(baseURL, apiAuthToken, companySlug, contactSlug,
                            jobName3, 4, jobCity3), executor);

            // Wait for all jobs to be created
            CompletableFuture.allOf(job1Future, job2Future, job3Future).join();

            Response job1Response = job1Future.join();
            Response job2Response = job2Future.join();
            Response job3Response = job3Future.join();

            JsonPath jsonJob1 = job1Response.jsonPath();
            JsonPath jsonJob2 = job2Response.jsonPath();
            JsonPath jsonJob3 = job3Response.jsonPath();

            String jobSlug1 = jsonJob1.getString("slug");
            String jobSlug2 = jsonJob2.getString("slug");
            String jobSlug3 = jsonJob3.getString("slug");

            // Phase 4: Get job details from albatross in parallel (independent operations)
            CompletableFuture<JsonPath> job1DetailsFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug1, "job");
                return response.jsonPath();
            }, executor);

            CompletableFuture<JsonPath> job2DetailsFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug2, "job");
                return response.jsonPath();
            }, executor);

            CompletableFuture<JsonPath> job3DetailsFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.getEntityDetail(albatrossURL, albatrossAuthToken, jobSlug3, "job");
                return response.jsonPath();
            }, executor);

            // Wait for all job details to be fetched
            CompletableFuture.allOf(job1DetailsFuture, job2DetailsFuture, job3DetailsFuture).join();

            JsonPath job1JsonPath = job1DetailsFuture.join();
            JsonPath job2JsonPath = job2DetailsFuture.join();
            JsonPath job3JsonPath = job3DetailsFuture.join();

            // Get DB IDs from albatross (these match the "id" field in response)
            int jobId1 = job1JsonPath.getInt("data.job.id");
            int jobId2 = job2JsonPath.getInt("data.job.id");
            int jobId3 = job3JsonPath.getInt("data.job.id");

            // Get API response IDs (these match "srno" in response and are used for
            // filtering/search)
            int srno1 = jsonJob1.getInt("id");
            int srno2 = jsonJob2.getInt("id");
            int srno3 = jsonJob3.getInt("id");

            Map<String, Map<String, Object>> allJobsData = new HashMap<>();

            Map<String, Object> job1Data = new HashMap<>();
            job1Data.put("jobName", jobName1);
            job1Data.put("srno", srno1); // API response ID - used for search/srno
            job1Data.put("id", jobId1); // DB ID from albatross - used for id field
            job1Data.put("jobCity", jobCity1);
            job1Data.put("jobType", 2);
            job1Data.put("companySlug", companySlug);
            job1Data.put("companyName", companyName);
            allJobsData.put("Job1", job1Data);

            Map<String, Object> job2Data = new HashMap<>();
            job2Data.put("jobName", jobName2);
            job2Data.put("srno", srno2);
            job2Data.put("id", jobId2);
            job2Data.put("jobCity", jobCity2);
            job2Data.put("jobType", 3);
            job2Data.put("companySlug", companySlug);
            job2Data.put("companyName", companyName);
            allJobsData.put("job2", job2Data);

            Map<String, Object> job3Data = new HashMap<>();
            job3Data.put("jobName", jobName3);
            job3Data.put("srno", srno3);
            job3Data.put("id", jobId3);
            job3Data.put("jobCity", jobCity3);
            job3Data.put("jobType", 4);
            job3Data.put("companySlug", companySlug);
            job3Data.put("companyName", companyName);
            allJobsData.put("job3", job3Data);

            return new Object[][] {
                    { "", "allData", 2, allJobsData },
                    { jobName2, "job2", 1, allJobsData },
                    { String.valueOf(srno2), "job2", 1, allJobsData },
                    { jobCity3, "job3", 1, allJobsData },
                    { jobName1, "job1", 0, allJobsData }
            };
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Helper method to create a job with custom job type and city.
     * Follows the same pattern as commanFunction.createNewJob() but allows
     * customization.
     */
    private Response createNewJobWithTypeAndCity(String baseURL, Object authToken, String companySlug,
            String contactSlug, String jobName, int jobType, String city) {
        Map<String, String> authTokenMap = function.getAuthTokenMap(authToken);
        Job job = new Job();
        job.setName(jobName);
        job.setCompany_slug(companySlug);
        job.setContact_slug(contactSlug);
        job.setNumber_of_openings(1);
        job.setJob_type(jobType);
        job.setCity(city);
        job.setJob_description_text("Sample JD");
        job.setEnable_job_application_form(1);
        Response response = RestClient.doPost("JSON", baseURL, "jobs", authTokenMap, null, true, job);
        JsonPath jp = response.jsonPath();
        assertEquals(response.getStatusCode(), 200);
        return response;
    }

    private Response executePost(String endpoint, String authToken, Object payload) {
        Object requestPayload = payload;
        if (payload instanceof Map) {
            requestPayload = TestUtil.getSerializedJSON(payload);
        }
        return RestClient.doPost("JSON", timesheetBaseURL, endpoint, authToken, null, true, requestPayload);
    }
}
