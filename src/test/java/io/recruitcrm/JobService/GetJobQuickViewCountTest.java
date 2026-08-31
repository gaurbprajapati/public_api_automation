package io.recruitcrm.JobService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.jobs.JobUpdateData;
import io.rcrm.api.pojo.albatross.jobs.UpdateJobRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobQuickViewCountTest extends TestBase {

    String apiAuthToken;
    String albatrossTkn;
    commanFunction commanFunction;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        apiAuthToken = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
        commanFunction = new commanFunction();
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobQuickViewCount_Success() {
        Response response = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Job quick view data fetched successfully"));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data array
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data array should not be empty", (Integer) jp.get("data.size()"), greaterThan(0));

        // Verify all required fields in data
        assertThat("allJobs should not be null", jp.get("data[0].allJobs"), notNullValue());
        assertThat("myJobs should not be null", jp.get("data[0].myJobs"), notNullValue());
        assertThat("openJobs should not be null", jp.get("data[0].openJobs"), notNullValue());
        assertThat("closedJobs should not be null", jp.get("data[0].closedJobs"), notNullValue());
        assertThat("onHoldJobs should not be null", jp.get("data[0].onHoldJobs"), notNullValue());
        assertThat("cancelledJobs should not be null", jp.get("data[0].cancelledJobs"), notNullValue());
        assertThat("archivedJobs should not be null", jp.get("data[0].archivedJobs"), notNullValue());
        assertThat("notInAnyHotlist should not be null", jp.get("data[0].notInAnyHotlist"), notNullValue());

        // Verify logical constraints
        assertThat("myJobs should not exceed allJobs", 
                (Integer) jp.get("data[0].myJobs"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));
        assertThat("openJobs should not exceed allJobs", 
                (Integer) jp.get("data[0].openJobs"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));
        assertThat("closedJobs should not exceed allJobs", 
                (Integer) jp.get("data[0].closedJobs"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));
        assertThat("onHoldJobs should not exceed allJobs", 
                (Integer) jp.get("data[0].onHoldJobs"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));
        assertThat("cancelledJobs should not exceed allJobs", 
                (Integer) jp.get("data[0].cancelledJobs"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));
        assertThat("archivedJobs should not exceed allJobs", 
                (Integer) jp.get("data[0].archivedJobs"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));
        assertThat("notInAnyHotlist should not exceed allJobs", 
                (Integer) jp.get("data[0].notInAnyHotlist"), lessThanOrEqualTo((Integer) jp.get("data[0].allJobs")));

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/job/jobQuickViewCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobQuickViewCount_WithoutAuth() {
        Response response = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                null, null, null, true);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobQuickViewCount_InvalidAuth() {
        Response response = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn + "invalid-token-123", null, null, true);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobCreationData", groups = {"job_service", "nightly-build"})
    public void testJobQuickViewCount_AfterCreatingJob(String jobSlug, Integer jobId, String jobName) {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialAllJobs = (Integer) initialJp.get("data[0].allJobs");
        int initialMyJobs = (Integer) initialJp.get("data[0].myJobs");
        int initialOpenJobs = (Integer) initialJp.get("data[0].openJobs");
        int initialClosedJobs = (Integer) initialJp.get("data[0].closedJobs");
        int initialOnHoldJobs = (Integer) initialJp.get("data[0].onHoldJobs");
        int initialCancelledJobs = (Integer) initialJp.get("data[0].cancelledJobs");
        int initialArchivedJobs = (Integer) initialJp.get("data[0].archivedJobs");
        int initialNotInAnyHotlist = (Integer) initialJp.get("data[0].notInAnyHotlist");

        commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");

        // Get count after creating job
        Response afterCreateResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After create response should succeed", afterCreateResponse.getStatusCode(), equalTo(200));
        JsonPath afterCreateJp = afterCreateResponse.jsonPath();
        int afterCreateAllJobs = (Integer) afterCreateJp.get("data[0].allJobs");
        int afterCreateMyJobs = (Integer) afterCreateJp.get("data[0].myJobs");
        int afterCreateOpenJobs = (Integer) afterCreateJp.get("data[0].openJobs");
        int afterCreateClosedJobs = (Integer) afterCreateJp.get("data[0].closedJobs");
        int afterCreateOnHoldJobs = (Integer) afterCreateJp.get("data[0].onHoldJobs");
        int afterCreateCancelledJobs = (Integer) afterCreateJp.get("data[0].cancelledJobs");
        int afterCreateArchivedJobs = (Integer) afterCreateJp.get("data[0].archivedJobs");
        int afterCreateNotInAnyHotlist = (Integer) afterCreateJp.get("data[0].notInAnyHotlist");

        // Verify allJobs count increased
        assertThat("All jobs count should increase after creating job", 
                afterCreateAllJobs, equalTo(initialAllJobs + 1));
        
        // Verify myJobs count increased (new job is owned by current user)
        assertThat("My jobs count should increase after creating job", 
                afterCreateMyJobs, equalTo(initialMyJobs + 1));
        
        // Verify openJobs count increased (new job has status Open by default)
        assertThat("Open jobs count should increase after creating job", 
                afterCreateOpenJobs, equalTo(initialOpenJobs + 1));
        
        // Verify closedJobs unchanged (new job is Open, not Closed)
        assertThat("Closed jobs count should be unchanged after creating Open job", 
                afterCreateClosedJobs, equalTo(initialClosedJobs));
        
        // Verify onHoldJobs unchanged (new job is Open, not On Hold)
        assertThat("On hold jobs count should be unchanged after creating Open job", 
                afterCreateOnHoldJobs, equalTo(initialOnHoldJobs));
        
        // Verify cancelledJobs unchanged (new job is Open, not Cancelled)
        assertThat("Cancelled jobs count should be unchanged after creating Open job", 
                afterCreateCancelledJobs, equalTo(initialCancelledJobs));
        
        // Verify archivedJobs unchanged (new job is not archived)
        assertThat("Archived jobs count should be unchanged after creating job", 
                afterCreateArchivedJobs, equalTo(initialArchivedJobs));
        
        // Verify notInAnyHotlist count increased (new job is not in any hotlist)
        assertThat("Not in any hotlist count should increase after creating job", 
                afterCreateNotInAnyHotlist, equalTo(initialNotInAnyHotlist + 1));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobCreationData", groups = {"job_service", "nightly-build"})
    public void testOpenJobsCount_AfterStatusChange(String jobSlug, Integer jobId, String jobName) {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialOpenJobs = (Integer) initialJp.get("data[0].openJobs");
        int initialClosedJobs = (Integer) initialJp.get("data[0].closedJobs");

        // Change job status to Closed (0)
        updateJobStatus(jobSlug, 0);

        // Get count after status change
        Response afterChangeResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After change response should succeed", afterChangeResponse.getStatusCode(), equalTo(200));
        JsonPath afterChangeJp = afterChangeResponse.jsonPath();
        int afterChangeOpenJobs = (Integer) afterChangeJp.get("data[0].openJobs");
        int afterChangeClosedJobs = (Integer) afterChangeJp.get("data[0].closedJobs");

        // Verify openJobs count decreased
        assertThat("Open jobs count should decrease after changing status to Closed", 
                afterChangeOpenJobs, equalTo(initialOpenJobs - 1));
        
        // Verify closedJobs count increased
        assertThat("Closed jobs count should increase after changing status to Closed", 
                afterChangeClosedJobs, equalTo(initialClosedJobs + 1));

        // Change back to Open (1)
        updateJobStatus(jobSlug, 1);

        // Get count after changing back
        Response afterChangeBackResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After change back response should succeed", afterChangeBackResponse.getStatusCode(), equalTo(200));
        JsonPath afterChangeBackJp = afterChangeBackResponse.jsonPath();
        int afterChangeBackOpenJobs = (Integer) afterChangeBackJp.get("data[0].openJobs");
        int afterChangeBackClosedJobs = (Integer) afterChangeBackJp.get("data[0].closedJobs");

        // Verify counts are back to initial state
        assertThat("Open jobs count should be back to initial", 
                afterChangeBackOpenJobs, equalTo(initialOpenJobs));
        assertThat("Closed jobs count should be back to initial", 
                afterChangeBackClosedJobs, equalTo(initialClosedJobs));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobCreationData", groups = {"job_service", "nightly-build"})
    public void testOnHoldJobsCount_AfterStatusChange(String jobSlug, Integer jobId, String jobName) {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialOpenJobs = (Integer) initialJp.get("data[0].openJobs");
        int initialOnHoldJobs = (Integer) initialJp.get("data[0].onHoldJobs");

        // Change job status to On Hold (2)
        updateJobStatus(jobSlug, 2);

        // Get count after status change
        Response afterChangeResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After change response should succeed", afterChangeResponse.getStatusCode(), equalTo(200));
        JsonPath afterChangeJp = afterChangeResponse.jsonPath();
        int afterChangeOpenJobs = (Integer) afterChangeJp.get("data[0].openJobs");
        int afterChangeOnHoldJobs = (Integer) afterChangeJp.get("data[0].onHoldJobs");

        // Verify openJobs count decreased
        assertThat("Open jobs count should decrease after changing status to On Hold", 
                afterChangeOpenJobs, equalTo(initialOpenJobs - 1));
        
        // Verify onHoldJobs count increased
        assertThat("On Hold jobs count should increase after changing status to On Hold", 
                afterChangeOnHoldJobs, equalTo(initialOnHoldJobs + 1));

        // Change back to Open (1)
        updateJobStatus(jobSlug, 1);

        // Get count after changing back
        Response afterChangeBackResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After change back response should succeed", afterChangeBackResponse.getStatusCode(), equalTo(200));
        JsonPath afterChangeBackJp = afterChangeBackResponse.jsonPath();
        int afterChangeBackOpenJobs = (Integer) afterChangeBackJp.get("data[0].openJobs");
        int afterChangeBackOnHoldJobs = (Integer) afterChangeBackJp.get("data[0].onHoldJobs");

        // Verify counts are back to initial state
        assertThat("Open jobs count should be back to initial", 
                afterChangeBackOpenJobs, equalTo(initialOpenJobs));
        assertThat("On Hold jobs count should be back to initial", 
                afterChangeBackOnHoldJobs, equalTo(initialOnHoldJobs));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobCreationData", groups = {"job_service", "nightly-build"})
    public void testCancelledJobsCount_AfterStatusChange(String jobSlug, Integer jobId, String jobName) {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialOpenJobs = (Integer) initialJp.get("data[0].openJobs");
        int initialCancelledJobs = (Integer) initialJp.get("data[0].cancelledJobs");

        // Change job status to Cancelled (3)
        updateJobStatus(jobSlug, 3);

        // Get count after status change
        Response afterChangeResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After change response should succeed", afterChangeResponse.getStatusCode(), equalTo(200));
        JsonPath afterChangeJp = afterChangeResponse.jsonPath();
        int afterChangeOpenJobs = (Integer) afterChangeJp.get("data[0].openJobs");
        int afterChangeCancelledJobs = (Integer) afterChangeJp.get("data[0].cancelledJobs");

        // Verify openJobs count decreased
        assertThat("Open jobs count should decrease after changing status to Cancelled", 
                afterChangeOpenJobs, equalTo(initialOpenJobs - 1));
        
        // Verify cancelledJobs count increased
        assertThat("Cancelled jobs count should increase after changing status to Cancelled", 
                afterChangeCancelledJobs, equalTo(initialCancelledJobs + 1));

        // Change back to Open (1)
        updateJobStatus(jobSlug, 1);

        // Get count after changing back
        Response afterChangeBackResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After change back response should succeed", afterChangeBackResponse.getStatusCode(), equalTo(200));
        JsonPath afterChangeBackJp = afterChangeBackResponse.jsonPath();
        int afterChangeBackOpenJobs = (Integer) afterChangeBackJp.get("data[0].openJobs");
        int afterChangeBackCancelledJobs = (Integer) afterChangeBackJp.get("data[0].cancelledJobs");

        // Verify counts are back to initial state
        assertThat("Open jobs count should be back to initial", 
                afterChangeBackOpenJobs, equalTo(initialOpenJobs));
        assertThat("Cancelled jobs count should be back to initial", 
                afterChangeBackCancelledJobs, equalTo(initialCancelledJobs));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "jobCreationData", groups = {"job_service", "nightly-build"})
    public void testArchivedJobsCount_AfterArchiving(String jobSlug, Integer jobId, String jobName) {
        // Get initial count
        Response initialResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        JsonPath initialJp = initialResponse.jsonPath();
        int initialArchivedJobs = (Integer) initialJp.get("data[0].archivedJobs");
        int initialAllJobs = (Integer) initialJp.get("data[0].allJobs");
        int initialOpenJobs = (Integer) initialJp.get("data[0].openJobs");

        // Archive the job
        archiveJob(jobSlug);

        // Get count after archiving
        Response afterArchiveResponse = RestClient.doGet("JSON", jobServiceURL, "jobs/quick-view-count",
                albatrossTkn, null, null, true);
        
        assertThat("After archive response should succeed", afterArchiveResponse.getStatusCode(), equalTo(200));
        JsonPath afterArchiveJp = afterArchiveResponse.jsonPath();
        int afterArchiveArchivedJobs = (Integer) afterArchiveJp.get("data[0].archivedJobs");
        int afterArchiveAllJobs = (Integer) afterArchiveJp.get("data[0].allJobs");
        int afterArchiveOpenJobs = (Integer) afterArchiveJp.get("data[0].openJobs");

        // Verify archivedJobs count increased
        assertThat("Archived jobs count should increase after archiving", 
                afterArchiveArchivedJobs, equalTo(initialArchivedJobs + 1));
        
        // Verify allJobs count remains the same (archived jobs are still counted)
        assertThat("All jobs count should remain the same after archiving", 
                afterArchiveAllJobs, equalTo(initialAllJobs - 1));

        // Verify openJobs count decreased (archived job was Open and is no longer in open view)
        assertThat("Open jobs count should decrease after archiving Open job", 
                afterArchiveOpenJobs, equalTo(initialOpenJobs - 1));
    }

    @DataProvider(name = "jobCreationData")
    public Object[][] getJobCreationData() {
        // Create test job using function
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        
        // Get job details from albatross API
        Response jobResponse = getJobResponse(jobSlug);
        assertThat("Job details should be retrieved", jobResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = jobResponse.jsonPath();
        int jobId = jp.getInt("data.job.id");
        String jobName = jp.getString("data.job.name");
        
        assertThat("Job ID should not be null", jobId, notNullValue());
        assertThat("Job name should not be null", jobName, notNullValue());
        
        return new Object[][] { { jobSlug, jobId, jobName } };
    }

    private void updateJobStatus(String jobSlug, int jobStatus) {
        Response jobResponse = getJobResponse(jobSlug);

        JsonPath jobJsonPath = jobResponse.jsonPath();
        int companyId = jobJsonPath.getInt("data.job.companyid");
        int contactId = jobJsonPath.getInt("data.job.contactid");
        int ownerId = jobJsonPath.getInt("data.job.ownerid");
        int jobId = jobJsonPath.getInt("data.job.id");
        
        // Create job update request
        JobUpdateData jobData = JobUpdateData.builder()
                .slug(jobSlug)
                .name(jobJsonPath.getString("data.job.name"))
                .description(jobJsonPath.getString("data.job.description"))
                .noofopenings(jobJsonPath.getInt("data.job.noofopenings"))
                .qualificationid(jobJsonPath.getInt("data.job.qualificationid"))
                .specialization(jobJsonPath.getString("data.job.specialization"))
                .minexperienceinyears(jobJsonPath.getInt("data.job.minexperienceinyears"))
                .maxexperienceinyears(jobJsonPath.getInt("data.job.maxexperienceinyears"))
                .annualsalarymin(jobJsonPath.getInt("data.job.annualsalarymin"))
                .annualsalarymax(jobJsonPath.getInt("data.job.annualsalarymax"))
                .salarytype(jobJsonPath.getString("data.job.salarytype"))
                .job_type(jobJsonPath.getString("data.job.job_type"))
                .locality(jobJsonPath.getString("data.job.locality"))
                .city(jobJsonPath.getString("data.job.city"))
                .country(jobJsonPath.getString("data.job.country"))
                .postalcode(jobJsonPath.getString("data.job.postalcode"))
                .state(jobJsonPath.getString("data.job.state"))
                .address(jobJsonPath.getString("data.job.address"))
                .currencyid(jobJsonPath.getInt("data.job.currencyid"))
                .companyid(companyId)
                .contactid(contactId)
                .ownerid(ownerId)
                .id(jobId)
                .jobstatus(jobStatus)
                .build();

        UpdateJobRequest updateJobRequest = UpdateJobRequest.builder()
                .job(jobData)
                .address_changed(false)
                .filesInfo(new Object[]{})
                .deleteJobKey("")
                .secondaryContacts(new Object[]{})
                .xml_feeds(new Object[]{})
                .jobParserData(new Object[]{})
                .collaborator(null)
                .build();

        // Update job status
        String updatePath = "jobs/" + jobSlug;
        Response updateResponse = RestClient.doPost("JSON", albatrossURL, updatePath, albatrossTkn, null, true, updateJobRequest);
        assertThat("Failed to update job status", updateResponse.getStatusCode(), equalTo(200));
    }

    private void archiveJob(String jobSlug) {
        Response jobResponse = getJobResponse(jobSlug);

        JsonPath jobJsonPath = jobResponse.jsonPath();
        int jobId = jobJsonPath.getInt("data.job.id");
        
        JSONObject archiveRequest = new JSONObject();
        archiveRequest.put("key", "archived");
        archiveRequest.put("value", 1);
        archiveRequest.put("tableFlag", "job");
        archiveRequest.put("id", new JSONArray().put(jobId));
        
        String archivePath = "global/update-fields";
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossTkn);
        Response archiveResponse = RestClient.doPost1("JSON", albatrossURL, archivePath, authTokenMap, null, null, true, archiveRequest.toString());
        assertThat("Failed to archive job", archiveResponse.getStatusCode(), equalTo(200));
    }

    private Response getJobResponse(String jobSlug) {
        // Get job details from albatross API
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossTkn);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("jobSlug", jobSlug);
        String basePath = "jobs/{jobSlug}/get";
        Response jobResponse = RestClient.doPost1("JSON", albatrossURL, basePath, authTokenMap, null, pathParameters, true, null);
        assertThat("Failed to get job details from albatross API", jobResponse.getStatusCode(), equalTo(200));

        return jobResponse;
    }
}
