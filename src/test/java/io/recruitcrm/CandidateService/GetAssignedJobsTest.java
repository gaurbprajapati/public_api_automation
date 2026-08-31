package io.recruitcrm.CandidateService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.testng.Assert;

import io.rcrm.api.commanfunctions.commanFunction;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class GetAssignedJobsTest extends TestBase {

    public GetAssignedJobsTest() {
        super();
    }

    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();

    @Owner("Raj Pandey")
    @Test
    public void getAssignedJobsTest_200() {
        int candidateId = assignCandidateToJob();

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidateId", String.valueOf(candidateId));

        String basePath = "assign-job-candidate/candidates/{candidateId}/jobs/assigned?page=1&size=1";

        Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().body("meta.message", Matchers.is("Assigned jobs fetched successfully."));
        response.then().body("data[0].candidateId", Matchers.is(candidateId));
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/GetAssignedJobs.json"));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void getAssignedJobsTest_401() {
        int candidateId = assignCandidateToJob();

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidateId", String.valueOf(candidateId));

        String basePath = "assign-job-candidate/candidates/{candidateId}/jobs/assigned?page=1&size=1";
        Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken() + "1234",
                null, pathParamters, true);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Gaurav Prajapati")
    @Test
    public void getAssignedJobsTest_404() {
        int candidateId = -fakerCandidate.getIndustryId();

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidateId", String.valueOf(candidateId));

        String basePath = "assign-job-candidate/candidates/{candidateId}/jobs/assigned?page=1&size=1";

        Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true);

        Assert.assertEquals(response.getStatusCode(), 404);
        response.then().body("errors[0].message", Matchers.is("Candidate id " + candidateId + " not found."));
    }

    @Owner("Yash Rampal")
    @Test
    public void getAssignedJobsTest_400() {
        int candidateId = assignCandidateToJob();

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidateId", String.valueOf(candidateId) + fakerCandidate.getDescription());

        String basePath = "assign-job-candidate/candidates/{candidateId}/jobs/assigned?page=1&size=1";

        Response response = RestClient.doGet("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters, true);

        Assert.assertEquals(response.getStatusCode(), 400);
        response.then().body("error", Matchers.is("Bad Request"));
    }

    public int assignCandidateToJob() {
        JsonPath candidateJsonPath = albatrossFunctions1
                .createCandidate(albatrossURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();

        int candidateId = candidateJsonPath.get("data.candidate.id");
        String candidateSlug = candidateJsonPath.get("data.candidate.slug");
        String jobSlug = function.getEntityResponse(baseURL, ThreadManager.getAccountApiKey(), "job");

        Map<String, String> pathParamters = new HashMap<String, String>();
        pathParamters.put("candidate", candidateSlug);

        String basePath = "candidates/{candidate}/assign";

        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("job_slug", jobSlug);

        Response response = RestClient.doPost1("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), queryParameters, pathParamters,
                true, null);

        Assert.assertEquals(response.getStatusCode(), 200);
        return candidateId;
    }
}
