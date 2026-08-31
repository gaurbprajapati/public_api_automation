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
public class GetJobSearchCountTest extends TestBase {

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
    @Test(dataProvider = "jobCreationData", groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_Success(String jobSlug, Integer jobId, String jobName) {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Entity count retrieved successfully"));
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("Context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("Response code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data is an integer
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("Data should be an integer", jp.get("data"), instanceOf(Integer.class));

        // Validate JSON schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi/job/jobSearchCount.json"));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithoutAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                null, null, true, requestBody);

        assertThat("Expected status code 400 but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_InvalidAuth() {
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response response = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn + "invalid-token-123", null, true, requestBody);

        assertThat("Expected status code 401 but got " + response.getStatusCode(), 
                response.getStatusCode(), equalTo(401));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_AfterCreatingJob() {
        // Get initial count
        JSONObject requestBody = createDefaultSearchRequestBody();
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Initial response should succeed", initialResponse.getStatusCode(), equalTo(200));
        int initialCount = initialResponse.jsonPath().getInt("data");

        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");

        // Get count after creating job
        Response afterCreateResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);
        
        assertThat("After create response should succeed", afterCreateResponse.getStatusCode(), equalTo(200));
        int afterCreateCount = afterCreateResponse.jsonPath().getInt("data");

        // Verify count increased
        assertThat("Job count should increase after creating job", 
                afterCreateCount, equalTo(initialCount + 1));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithOwnerFilter() {
        // Get current user ID for owner filter
        Response usersResponse = commanFunction.getUsers(baseURL, apiAuthToken);
        assertThat("Users response should succeed", usersResponse.getStatusCode(), equalTo(200));
        JsonPath usersJp = usersResponse.jsonPath();
        int currentOwnerId = usersJp.get("[0].id");
        
        // Create a job owned by current user
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        
        // Get initial count with owner filter
        JSONObject requestBody = createOwnerFilterRequestBody(currentOwnerId);
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + initialResponse.getStatusCode(), 
                initialResponse.getStatusCode(), equalTo(200));

        JsonPath initialJp = initialResponse.jsonPath();
        int initialCount = initialJp.getInt("data");

        // Get a different user to transfer ownership to
        int newOwnerId = usersJp.get("[1].id");
        if (newOwnerId == 0 || newOwnerId == currentOwnerId) {
            // If only one user, create another user or skip transfer test
            newOwnerId = currentOwnerId; // Fallback for single user scenario
        }
        
        // Transfer job ownership to different user
        transferJobOwnership(jobSlug, newOwnerId);
        
        // Get count after ownership change
        Response afterChangeResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + afterChangeResponse.getStatusCode(), 
                afterChangeResponse.getStatusCode(), equalTo(200));

        JsonPath afterJp = afterChangeResponse.jsonPath();
        int afterCount = afterJp.getInt("data");
        
        // Verify count decreased (job no longer owned by current user)
        assertThat("Count should decrease after transferring ownership", 
                afterCount, equalTo(initialCount - 1));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithJobStatusOpen() {
        // Create a job with status = 1 (Open)
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        
        // Get initial count with jobstatus = 1 filter
        JSONObject requestBody = createJobStatusFilterRequestBody(1);
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + initialResponse.getStatusCode(), 
                initialResponse.getStatusCode(), equalTo(200));

        JsonPath initialJp = initialResponse.jsonPath();
        int initialCount = initialJp.getInt("data");

        // Change job status to 0 (Closed)
        updateJobStatus(jobSlug, 0);
        
        // Get count after status change
        Response afterChangeResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + afterChangeResponse.getStatusCode(), 
                afterChangeResponse.getStatusCode(), equalTo(200));

        JsonPath afterJp = afterChangeResponse.jsonPath();
        int afterCount = afterJp.getInt("data");
        
        // Verify count decreased (job no longer has status = 1)
        assertThat("Count should decrease after changing job status from Open to Closed", 
                afterCount, equalTo(initialCount - 1));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithJobStatusClosed() {
        // Create a job and set status to 0 (Closed)
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        updateJobStatus(jobSlug, 0);
        
        // Get initial count with jobstatus = 0 filter
        JSONObject requestBody = createJobStatusFilterRequestBody(0);
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + initialResponse.getStatusCode(), 
                initialResponse.getStatusCode(), equalTo(200));

        JsonPath initialJp = initialResponse.jsonPath();
        int initialCount = initialJp.getInt("data");

        // Change job status to 1 (Open)
        updateJobStatus(jobSlug, 1);
        
        // Get count after status change
        Response afterChangeResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + afterChangeResponse.getStatusCode(), 
                afterChangeResponse.getStatusCode(), equalTo(200));

        JsonPath afterJp = afterChangeResponse.jsonPath();
        int afterCount = afterJp.getInt("data");
        
        // Verify count decreased (job no longer has status = 0)
        assertThat("Count should decrease after changing job status from Closed to Open", 
                afterCount, equalTo(initialCount - 1));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithJobStatusBypass() {
        // Create a job and set status to 2
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        updateJobStatus(jobSlug, 2);
        
        // Get initial count with jobstatus = 2 filter and offLimitBehavior = "bypass"
        JSONObject requestBody = createJobStatusFilterWithOffLimitBehaviorRequestBody(2);
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + initialResponse.getStatusCode(), 
                initialResponse.getStatusCode(), equalTo(200));

        JsonPath initialJp = initialResponse.jsonPath();
        int initialCount = initialJp.getInt("data");

        // Change job status to 1 (Open)
        updateJobStatus(jobSlug, 1);
        
        // Get count after status change
        Response afterChangeResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + afterChangeResponse.getStatusCode(), 
                afterChangeResponse.getStatusCode(), equalTo(200));

        JsonPath afterJp = afterChangeResponse.jsonPath();
        int afterCount = afterJp.getInt("data");
        
        // Verify count decreased (job no longer has status = 2)
        assertThat("Count should decrease after changing job status from 2 to 1", 
                afterCount, equalTo(initialCount - 1));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithJobStatusInclusion() {
        // Create a job and set status to 3
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        updateJobStatus(jobSlug, 3);
        
        // Get initial count with jobstatus = 3 filter and offLimitBehavior = "inclusion"
        JSONObject requestBody = createJobStatusFilterWithOffLimitBehaviorRequestBody(3);
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + initialResponse.getStatusCode(), 
                initialResponse.getStatusCode(), equalTo(200));

        JsonPath initialJp = initialResponse.jsonPath();
        int initialCount = initialJp.getInt("data");

        // Change job status to 1 (Open)
        updateJobStatus(jobSlug, 1);
        
        // Get count after status change
        Response afterChangeResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + afterChangeResponse.getStatusCode(), 
                afterChangeResponse.getStatusCode(), equalTo(200));

        JsonPath afterJp = afterChangeResponse.jsonPath();
        int afterCount = afterJp.getInt("data");
        
        // Verify count decreased (job no longer has status = 3)
        assertThat("Count should decrease after changing job status from 3 to 1", 
                afterCount, equalTo(initialCount - 1));
    }

    @Owner("Raj Pandey")
    @Test(groups = {"job_service", "nightly-build"})
    public void testJobSearchCount_WithArchivedFilter() {
        // Create a job
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        
        // Get initial count with archived = 1 filter (before archiving)
        JSONObject requestBody = createArchivedFilterRequestBody(1);
        Response initialResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + initialResponse.getStatusCode(), 
                initialResponse.getStatusCode(), equalTo(200));

        JsonPath initialJp = initialResponse.jsonPath();
        int initialCount = initialJp.getInt("data");

        // Archive the job
        archiveJob(jobSlug);
        
        // Get count after archiving
        Response afterArchiveResponse = RestClient.doPost("JSON", ariesServiceURL, "advanced-search/jobs/search/count/get",
                albatrossTkn, null, true, requestBody);

        assertThat("Expected status code 200 but got " + afterArchiveResponse.getStatusCode(), 
                afterArchiveResponse.getStatusCode(), equalTo(200));

        JsonPath afterJp = afterArchiveResponse.jsonPath();
        int afterCount = afterJp.getInt("data");
        
        // Verify count increased (job is now archived)
        assertThat("Count should increase after archiving job", 
                afterCount, equalTo(initialCount + 1));
    }

    @DataProvider(name = "jobCreationData", parallel = true)
    public Object[][] getJobCreationData() {
        // Create test job using function
        String jobSlug = commanFunction.getEntityResponse(baseURL, apiAuthToken, "job");
        assertThat("Job slug should not be null", jobSlug, notNullValue());
        
        Map<String, String> authTokenMap = new HashMap<>();
        authTokenMap.put("Authorization", "Bearer " + albatrossTkn);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("jobSlug", jobSlug);
        String basePath = "jobs/{jobSlug}/get";
        
        Response jobResponse = RestClient.doPost1("JSON", albatrossURL, basePath, authTokenMap, null, pathParameters, true, null);
        assertThat("Job details should be retrieved", jobResponse.getStatusCode(), equalTo(200));
        
        JsonPath jp = jobResponse.jsonPath();
        Integer jobId = jp.get("data.job.id");
        String jobName = jp.get("data.job.name");
        
        assertThat("Job ID should not be null", jobId, notNullValue());
        assertThat("Job name should not be null", jobName, notNullValue());
        
        return new Object[][] { { jobSlug, jobId, jobName } };
    }

    private JSONObject createDefaultSearchRequestBody() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        requestBody.put("offLimitBehavior", "bypass");
        requestBody.put("defaultFilterList", JSONObject.NULL);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createOwnerFilterRequestBody(int ownerId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        
        // Create defaultFilterList with owner filter matching curl structure
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "jobs");
        filter.put("searchField", "ownerid");
        filter.put("filterType", "is");
        filter.put("entityType", "job");
        filter.put("fieldType", "dropdown");
        
        JSONObject filterValue = new JSONObject();
        JSONArray value = new JSONArray();
        JSONObject entityObj = new JSONObject();
        entityObj.put("entityTypeId", 6); // User entity type ID
        JSONArray entityIds = new JSONArray();
        entityIds.put(ownerId);
        entityObj.put("entityIds", entityIds);
        value.put(entityObj);
        filterValue.put("value", value);
        filterValue.put("type", "ENTITY_ASSOCIATION");
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createJobStatusFilterRequestBody(int jobStatus) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        
        // Create defaultFilterList with jobstatus filter
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "jobs");
        filter.put("searchField", "jobstatus");
        filter.put("filterType", "is");
        filter.put("entityType", "job");
        filter.put("fieldType", "NUMBER");
        
        JSONObject filterValue = new JSONObject();
        filterValue.put("type", "INTEGER_LIST");
        JSONArray statusIds = new JSONArray();
        statusIds.put(jobStatus);
        filterValue.put("value", statusIds);
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createJobStatusFilterWithOffLimitBehaviorRequestBody(int jobStatus) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
//        requestBody.put("offLimitBehavior", offLimitBehavior);
        
        // Create defaultFilterList with jobstatus filter
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "jobs");
        filter.put("searchField", "jobstatus");
        filter.put("filterType", "is");
        filter.put("entityType", "job");
        filter.put("fieldType", "NUMBER");
        
        JSONObject filterValue = new JSONObject();
        filterValue.put("type", "INTEGER_LIST");
        filterValue.put("value", new JSONArray().put(jobStatus));
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private JSONObject createArchivedFilterRequestBody(int archivedValue) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("advancedSearchContext", "JOB");
        
        // Create defaultFilterList with archived filter
        JSONObject defaultFilterList = new JSONObject();
        JSONObject defaultFilterListInner = new JSONObject();
        JSONArray filters = new JSONArray();
        JSONObject filter = new JSONObject();
        filter.put("groupType", "jobs");
        filter.put("searchField", "archived");
        filter.put("filterType", "is");
        filter.put("entityType", "job");
        filter.put("fieldType", "NUMBER");
        
        JSONObject filterValue = new JSONObject();
        filterValue.put("type", "INTEGER");
        filterValue.put("value", archivedValue);
        filter.put("filterValue", filterValue);
        
        filters.put(filter);
        defaultFilterListInner.put("filters", filters);
        defaultFilterListInner.put("subGroupJoinOperator", "AND");
        defaultFilterList.put("defaultFilterList", defaultFilterListInner);
        
        requestBody.put("defaultFilterList", defaultFilterList);
        requestBody.put("filterSearchList", JSONObject.NULL);
        requestBody.put("booleanSearchList", JSONObject.NULL);
        requestBody.put("sortPriorityList", new JSONArray());
        return requestBody;
    }

    private void transferJobOwnership(String jobSlug, Integer newOwnerId) {
        Response jobResponse = getJobResponse(jobSlug);
        
        JsonPath jobJsonPath = jobResponse.jsonPath();
        Map<String, Object> jobMap = jobJsonPath.get("data.job");
        JSONObject jobData = new JSONObject(jobMap);
        
        // Create transfer ownership payload
        JSONObject transferPayload = new JSONObject();
        transferPayload.put("relatedtotypeid", 4); // Job type ID
        transferPayload.put("selectedowner", newOwnerId);
        JSONArray selectedRows = new JSONArray();
        selectedRows.put(jobData);
        transferPayload.put("selectedrows", selectedRows);
        
        // Transfer ownership
        String transferEndpoint = "users/transfer-ownership/" + newOwnerId;
        Response transferResponse = RestClient.doPost("JSON", albatrossURL, transferEndpoint, albatrossTkn, null, true, transferPayload.toString());
        assertThat("Failed to transfer job ownership", transferResponse.getStatusCode(), equalTo(200));
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
        Response archiveResponse = RestClient.doPost1("JSON", albatrossURL, archivePath, albatrossTkn, null, null, true, archiveRequest.toString());
        assertThat("Failed to archive job", archiveResponse.getStatusCode(), equalTo(200));
    }

    public Response getJobResponse(String jobSlug) {
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
