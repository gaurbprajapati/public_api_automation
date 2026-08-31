package io.recruitcrm.CandidateService;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class UnassignJobsTest extends TestBase {

    public UnassignJobsTest() {
        super();
    }

    JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
    commanFunction function = new commanFunction();
    AllCrudFunctions albatrossFunctions1 = new AllCrudFunctions();


    @Owner("Raj Pandey")
    @Test
    public void unAssignJobsTest_200() {
        int assignmentId = getAssignmentId();

        String basePath = "assign-job-candidate/jobs/assignment";

        Map<String, Object> assignJobCandidateIds = new HashMap<>();
        assignJobCandidateIds.put("assignJobCandidateIds", Collections.singletonList(assignmentId));

        Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, assignJobCandidateIds);

        Assert.assertEquals(response.getStatusCode(), 200);
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath("privateApi/candidate/UnAssignJobs.json"));
        response.then().body("meta.message", Matchers.is("Candidates unassigned successfully."));
    }

    @Owner("Sampurn Chouksey")
    @Test
    public void unAssignJobsTest_401() {
        int assignmentId = getAssignmentId();

        String basePath = "assign-job-candidate/jobs/assignment";

        Map<String, Object> assignJobCandidateIds = new HashMap<>();
        assignJobCandidateIds.put("assignJobCandidateIds", Collections.singletonList(assignmentId));

        Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken() + fakerCandidate.getDescription(), null, null, true, assignJobCandidateIds);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Gaurav Prajapati")
    @Test
    public void unAssignJobsTest_404() {
        int assignmentId = getAssignmentId();

        String basePath = "assign-job-candidate/jobs/assignment";

        Map<String, Object> assignJobCandidateIds = new HashMap<>();
        assignJobCandidateIds.put("assignJobCandidateIds", Collections.singletonList(-assignmentId));

        Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, assignJobCandidateIds);

        Assert.assertEquals(response.getStatusCode(), 404);
        response.then().body("errors[0].message", Matchers.is("Invalid resource/s to unassign."));
    }

    @Owner("Yash Rampal")
    @Test
    public void unAssignJobsTest_400() {
        String basePath = "assign-job-candidate/jobs/assignment";

        Map<String, Object> assignJobCandidateIds = new HashMap<>();

        Response response = RestClient.doDeleteOnce("application/json", candidatesURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, null, true, assignJobCandidateIds);

        Assert.assertEquals(response.getStatusCode(), 400);
        response.then().body("errors[0].message", Matchers.is("Field idsToUnassign cannot be null."));
    }

    public int getAssignmentId() {
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

        Map<String, String> pathParamters1 = new HashMap<String, String>();
        pathParamters1.put("candidateId", String.valueOf(candidateId));

        basePath = "assign-job-candidate/candidates/{candidateId}/jobs/assigned?page=1&size=1";

        Response response2 = RestClient.doGet("JSON", candidatesURL, basePath,
                ThreadManager.getOwnerAlbatrossToken(),
                null, pathParamters1, true);

        Assert.assertEquals(response2.getStatusCode(), 200);
        return response2.jsonPath().get("data[0].id");
    }
}
