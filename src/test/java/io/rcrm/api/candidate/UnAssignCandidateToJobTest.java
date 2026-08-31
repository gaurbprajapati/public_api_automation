package io.rcrm.api.candidate;

import java.util.HashMap;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business")
public class UnAssignCandidateToJobTest extends TestBase {

    commanFunction function;
    Object authToken;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        function = new commanFunction();
        authToken = ThreadManager.getAccountApiKey();
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void authorizedUserCanUnAssignCandidateToJobWithValidData(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", candidateSlug);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("job_slug", jobSlug);
        String basePath = "candidates/{candidate}/unassign";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status 200 for successful unassignment.");
        Assert.assertEquals(response.jsonPath().get("slug"), candidateSlug, "Candidate slug in response doesn't match expected slug.");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void verifyUserCanReUnAssignCandidateToJob(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", candidateSlug);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("job_slug", jobSlug);
        String basePath = "candidates/{candidate}/unassign";
        Response firstResponse = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(firstResponse.getStatusCode(), 200, "First unassignment should succeed with status 200.");
        Response secondResponse = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(secondResponse.getStatusCode(), 422, "Second unassignment should fail with status 422.");
        Assert.assertTrue(secondResponse.jsonPath().getString("errorMessage").contains("Candidate is not assigned to this job"), "Error message should mention that candidate is not assigned.");
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void authorizedUserCannotUnAssignCandidateToInvalidJob(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", candidateSlug);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("job_slug", jobSlug + "1234xyz");
        String basePath = "candidates/{candidate}/unassign";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(response.getStatusCode(), 422, "Unassigning to an invalid job should return 422.");
        Assert.assertTrue(response.jsonPath().getString("job_slug[0]").contains("Invalid job slug"), "Error message should mention invalid job slug.");
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void authorizedUserCannotUnAssignInvalidCandidateToValidJob(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", candidateSlug + "1234xyz");
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("job_slug", jobSlug);
        String basePath = "candidates/{candidate}/unassign";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(response.getStatusCode(), 422, "Unassigning with an invalid candidate should return 422.");
        Assert.assertTrue(response.jsonPath().getString("errorMessage").contains("Candidate not found"), "Error message should mention that candidate was not found.");
    }

    @Owner("Yash Rampal")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void authorizedUserCannotUnAssignInvalidCandidateToInvalidJob(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", candidateSlug + "1234xyz");
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("job_slug", jobSlug + "1234xyz");
        String basePath = "candidates/{candidate}/unassign";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(response.getStatusCode(), 422, "Unassigning with both invalid candidate and job should return 422.");
        Assert.assertTrue(response.jsonPath().getString("job_slug[0]").contains("Invalid job slug"), "Error message should mention invalid job slug.");
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void authorizedUserCannotUnAssignCandidateToJobWithEmptyQueryParameter(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", candidateSlug);
        String basePath = "candidates/{candidate}/unassign";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, null, pathParams, true, null);
        Assert.assertEquals(response.getStatusCode(), 422, "Missing job_slug query param should return 422.");
        Assert.assertTrue(response.jsonPath().getString("job_slug[0]").contains("The job slug field is required."), "Error message should mention that job slug is required.");
    }

    @Owner("Sampurn Chouksey")
    @Test(dataProvider = "assignedCandidateJobData", groups = "nightly-build")
    public void authorizedUserCannotUnAssignCandidateToJobWithEmptyPathParameter(String candidateSlug, String jobSlug) {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("candidate", "");
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("job_slug", jobSlug);
        String basePath = "candidates/{candidate}/unassign";
        Response response = RestClient.doPost1("JSON", baseURL, basePath, authToken, queryParams, pathParams, true, null);
        Assert.assertEquals(response.getStatusCode(), 404, "Empty candidate path param should result in 404 Not Found.");
    }

    @DataProvider
    public Object[][] assignedCandidateJobData() {
        JsonPath json = function.assignCandidateToJob(baseURL, authToken).jsonPath();
        String candidateSlug = json.get("candidate_slug");
        String jobSlug = json.get("job_slug");
        return new Object[][]{{candidateSlug, jobSlug}};
    }
}
