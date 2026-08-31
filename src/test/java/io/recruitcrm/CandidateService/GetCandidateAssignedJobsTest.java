package io.recruitcrm.CandidateService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

import io.rcrm.api.commanfunctions.commanFunction;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import static io.rcrm.api.testbase.TestBase.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateAssignedJobsTest extends TestBase {

    public GetCandidateAssignedJobsTest() {
        super();
    }

    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

    private int testCandidateId;
    private String jobName;
    private int jobId;
    String albatrossAuthToken;
    String apiAuthToken;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        apiAuthToken = ThreadManager.getAccountApiKey();
        createSingleTestData();
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_200() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("getCandidateAssignedJobs_200 - Expected status code 200", response.getStatusCode(), is(equalTo(200)));

        response.then().body("meta.message", containsString("Assigned jobs fetched successfully"));
        response.then().body("meta.status", equalTo(200));
        response.then().body("meta.requestUuid", notNullValue());
        response.then().body("meta.timestamp", notNullValue());
        response.then().body("meta.responseType.code", equalTo(103));
        response.then().body("meta.responseType.context", equalTo("Request is successful"));

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> data = jsonPath.getList("data");
        assertThat("Data should not be null", data, notNullValue());

        if (!data.isEmpty()) {
            assertThat("", response.jsonPath().get("data[0].candidatestatusid"), equalTo(1));
            assertThat("", response.jsonPath().get("data[0].candidatestatus"), equalTo("Assigned"));
            assertThat("", response.jsonPath().get("data[0].id"), equalTo(jobId));
            assertThat("", response.jsonPath().get("data[0].name"), equalTo(jobName));
        }

        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/CandidateAssignedJobSearch.json"));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_InvalidCandidateId_404() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        int invalidCandidateId = -999999;
        JSONObject searchPayload = createSearchPayload(invalidCandidateId, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_InvalidCandidateId_404 - Expected status code 404", response.getStatusCode(), is(equalTo(404)));
        response.then().body("errors[0].message", containsString("Candidate id -999999 not found."));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_InvalidPageParameters_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "0");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_InvalidPageParameters_400 - Expected status code 400", response.getStatusCode(), is(equalTo(400)));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_InvalidSizeParameters_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "0");

        JSONObject searchPayload = createSearchPayload(testCandidateId, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_InvalidSizeParameters_400 - Expected status code 400", response.getStatusCode(), is(equalTo(400)));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_UnauthorizedAccess_401() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken + "invalid",
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_UnauthorizedAccess_401 - Expected status code 401", response.getStatusCode(), is(equalTo(401)));
        response.then().body("meta.message", is("Unauthorised access"));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_MissingAuth_401() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(testCandidateId, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                null,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_MissingAuth_401 - Expected status code 401", response.getStatusCode(), is(equalTo(401)));
    }


    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_MalformedPayload_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        String malformedJson = "{\"sortPriorityList\": [], \"searchTerm\": \"\", \"candidateId\": " + testCandidateId;

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, malformedJson);

        assertThat("searchCandidateAssignedJobs_MalformedPayload_400 - Expected status code 400", response.getStatusCode(), is(equalTo(400)));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_MissingRequiredFields_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, null);

        assertThat("searchCandidateAssignedJobs_MissingRequiredFields_400 - Expected status code 400", response.getStatusCode(), is(equalTo(400)));
    }

    @Owner("Harika")
    @Test(groups = {"candidate_service", "nightly-build"})
    public void getCandidateAssignedJobs_NullCandidateId_400() {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("page", "1");
        queryParameters.put("size", "100");

        JSONObject searchPayload = createSearchPayload(null, "", new ArrayList<>());

        Response response = RestClient.doPost("JSON", candidatesURL,
                "candidate-assigned-job/search/get",
                albatrossAuthToken,
                queryParameters, true, searchPayload);

        assertThat("searchCandidateAssignedJobs_NullCandidateId_400 - Expected status code 404", response.getStatusCode(), is(equalTo(404)));
        response.then().body("errors[0].message", containsString("Candidate id null not found."));
    }

    private JSONObject createSearchPayload(Integer candidateId, String searchTerm, List<Map<String, Object>> sortPriorityList) {
        JSONObject searchPayload = new JSONObject();
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("searchTerm", searchTerm);
        searchPayload.put("candidateId", candidateId);
        return searchPayload;
    }


    private void assignCandidateToJob(String candidateSlug, String jobSlug) {
        try {
            // Assign candidate to job
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);

            String basePath = "candidates/{candidate}/assign";

            Map<String, String> queryParameters = new HashMap<>();
            queryParameters.put("job_slug", jobSlug);

            Response response = RestClient.doPost1("JSON", baseURL, basePath,
                    apiAuthToken, queryParameters, pathParameters, true, null);

            assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));
        } catch (Exception e) {
            Assert.fail("Failed to create test data: " + e.getMessage());
        }
    }


    public void createSingleTestData() {
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, albatrossAuthToken).jsonPath();

        testCandidateId = candidateJsonPath.get("data.candidate.id");
        String candidateSlug = candidateJsonPath.get("data.candidate.slug");

        String jobSlug = function.getEntityResponse(baseURL, apiAuthToken, "job");
        assignCandidateToJob(candidateSlug, jobSlug);
        JsonPath jobJsonPath = albatrossFunctions1.getJobResponse(albatrossURL, albatrossAuthToken, jobSlug).jsonPath();

        jobId = jobJsonPath.get("data.job.id");
        jobName = jobJsonPath.get("data.job.name");

    }

}

