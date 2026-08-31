package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.CandidateAnswerRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetCandidateAnswersTest extends TestBase {

    String accountApiKey;
    String albatrossTkn;
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        accountApiKey = ThreadManager.getAccountApiKey();
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersSuccess(int candidateId, int jobId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", jobId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Candidate answers fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("All Question Answer DTO should not be null", jp.get("data.allQuestionAnswerDto"), notNullValue());
        assertThat("Question Answer DTOs should not be null", jp.get("data.allQuestionAnswerDto.questionAnswerDtos"), notNullValue());

        // Verify question answer structure
        if (jp.get("data.allQuestionAnswerDto.questionAnswerDtos.size()") != null && 
            (Integer) jp.get("data.allQuestionAnswerDto.questionAnswerDtos.size()") > 0) {
            
            assertThat("Question ID should not be null", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].questionId"), notNullValue());
            assertThat("Question should not be null", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].question"), notNullValue());
            assertThat("Read More should be boolean", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].readMore"), notNullValue());
            
            // Verify numeric values are positive when present
            if (jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].questionId") != null) {
                assertThat("Question ID should be positive", (Integer) jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].questionId"), greaterThan(0));
            }
            if (jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].answerId") != null) {
                assertThat("Answer ID should be positive", (Integer) jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].answerId"), greaterThan(0));
            }
        }

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/candidateAnswersWithJob.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutAuth(int candidateId, int jobId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", jobId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                null, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersInvalidAuth(int candidateId, int jobId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", jobId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn + "invalid_token", queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersInvalidCandidateId(int candidateId, int jobId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", "999999999");

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", jobId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Errors should not be null", jp.get("errors"), notNullValue());
        assertThat("Error message should contain candidate not found", jp.get("errors[0].message"), equalTo("Candidate id 999999999 not found."));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersInvalidJobId(int candidateId, int jobId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", 999999999);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Errors should not be null", jp.get("errors"), notNullValue());
        assertThat("Error message should contain job not found", jp.get("errors[0].message"), equalTo("Job id 999999999 not found."));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersMissingJobId(int candidateId, int jobId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        // jobId is intentionally missing

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Candidate answers fetched successfully"));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("All Question Answer DTO should not be null", jp.get("data.allQuestionAnswerDto"), notNullValue());
        assertThat("All Associated Job Question Answer DTO should not be null", jp.get("data.allAssociatedJobQuestionAnswerDto"), nullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersMissingCandidateId(int candidateId, int jobId) {
        // candidateId is intentionally missing from query params

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", jobId);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    // ========== CANDIDATE ANSWERS WITHOUT JOB ASSOCIATION TESTS ==========

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersWithoutJobTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutJobSuccess(int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Candidate answers fetched successfully"));
        assertThat("Request UUID should not be null", jp.get("meta.requestUuid"), notNullValue());
        assertThat("Timestamp should not be null", jp.get("meta.timestamp"), notNullValue());

        // Verify responseType structure
        assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
        assertThat("ResponseType code should be 103", (Integer) jp.get("meta.responseType.code"), equalTo(103));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("All Question Answer DTO should not be null", jp.get("data.allQuestionAnswerDto"), notNullValue());
        assertThat("Question Answer DTOs should not be null", jp.get("data.allQuestionAnswerDto.questionAnswerDtos"), notNullValue());
        assertThat("All Associated Job Question Answer DTO should be null", jp.get("data.allAssociatedJobQuestionAnswerDto"), nullValue());

        // Verify question answer structure
        if (jp.get("data.allQuestionAnswerDto.questionAnswerDtos.size()") != null && 
            (Integer) jp.get("data.allQuestionAnswerDto.questionAnswerDtos.size()") > 0) {
            
            assertThat("Question ID should not be null", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].questionId"), notNullValue());
            assertThat("Question should not be null", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].question"), notNullValue());
            assertThat("Read More should be boolean", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].readMore"), notNullValue());
            assertThat("Job ID should be null for non-job associated questions", jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].jobId"), nullValue());
            
            // Verify numeric values are positive when present
            if (jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].questionId") != null) {
                assertThat("Question ID should be positive", (Integer) jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].questionId"), greaterThan(0));
            }
            if (jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].answerId") != null) {
                assertThat("Answer ID should be positive", (Integer) jp.get("data.allQuestionAnswerDto.questionAnswerDtos[0].answerId"), greaterThan(0));
            }
        }

        // Validate JSON schema using existing schema
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemaValidation/privateApi/candidate/candidateAnswersWithoutJob.json"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersWithoutJobTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutJobWithoutAuth(int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                null, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersWithoutJobTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutJobInvalidAuth(int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn + "invalid_token", queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 401 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(401));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 401", (Integer) jp.get("meta.status"), equalTo(401));
        assertThat("Message should match expected", jp.get("meta.message"), equalTo("Unauthorised access"));
        assertThat("Data should contain error message", jp.get("data"), equalTo("Invalid or expired token"));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersWithoutJobTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutJobInvalidCandidateId(int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", "999999999");

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 404 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(404));

        JsonPath jp = response.jsonPath();
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 404", (Integer) jp.get("meta.status"), equalTo(404));
        assertThat("Data should be null", jp.get("data"), nullValue());
        assertThat("Errors should not be null", jp.get("errors"), notNullValue());
        assertThat("Error message should contain candidate not found", jp.get("errors[0].message"), equalTo("Candidate id 999999999 not found."));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersWithoutJobTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutJobMissingCandidateId(int candidateId) {
        // candidateId is intentionally missing from query params

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, null, null, true, requestBody.toString());

        assertThat("Expected status code 400 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(400));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "candidateAnswersWithoutJobTestData", groups = {"candidate_service", "nightly-build"})
    public void testGetCandidateAnswersWithoutJobEmptyRequestBody(int candidateId) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        // Empty request body
        JSONObject requestBody = new JSONObject();

        Response response = RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get", 
                albatrossTkn, queryParams, null, true, requestBody.toString());

        assertThat("Expected status code 200 but got " + response.getStatusCode(), response.getStatusCode(), equalTo(200));

        JsonPath jp = response.jsonPath();

        // Verify meta information
        assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
        assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(200));
        assertThat("Message should match expected value", (String) jp.get("meta.message"), equalTo("Candidate answers fetched successfully"));

        // Verify data structure exists
        assertThat("Data should not be null", jp.get("data"), notNullValue());
        assertThat("All Question Answer DTO should not be null", jp.get("data.allQuestionAnswerDto"), notNullValue());
    }

    @DataProvider(name = "candidateAnswersTestData")
    public Object[][] getCandidateAnswersTestData() {
        // Step 1: Create a candidate
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        int candidateId = candidateJp.get("data.candidate.id");
        assertThat("Candidate ID should not be null", candidateId, notNullValue());
        
        // Step 2: Create a job
        String jobSlug = function.getEntityResponse(baseURL, accountApiKey, "job");

        Response getJobResponse = albatrossFunctions.getJobResponse(albatrossURL, albatrossTkn, jobSlug);
        assertThat("Failed to get test job", getJobResponse.getStatusCode(), equalTo(200));
        JsonPath getJobJp = getJobResponse.jsonPath();
        int jobId = getJobJp.get("data.job.id");
        assertThat("Job ID should not be null", jobId, notNullValue());
        
        // Step 3: Create candidate questions
        Response questionResponse = albatrossFunctions.createCandidateQuestion(albatrossURL, albatrossTkn);
        assertThat("Failed to create candidate question", questionResponse.getStatusCode(), equalTo(200));
        JsonPath questionJp = questionResponse.jsonPath();
        int questionId = questionJp.get("data.user");
        assertThat("Question ID should not be null", questionId, notNullValue());
        
        // Step 4: Create candidate answer
        CandidateAnswerRequest answerRequest = new CandidateAnswerRequest("Test Answer for Candidate", questionId, null, candidateId);
        Response answerResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers", 
                albatrossTkn, null, true, answerRequest);
        assertThat("Failed to create candidate answer", answerResponse.getStatusCode(), equalTo(200));
        
        return new Object[][] { { candidateId, jobId } };
    }

    @DataProvider(name = "candidateAnswersWithoutJobTestData")
    public Object[][] getCandidateAnswersWithoutJobTestData() {
        // Step 1: Create a candidate
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, albatrossTkn);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        int candidateId = candidateJp.get("data.candidate.id");
        assertThat("Candidate ID should not be null", candidateId, notNullValue());
        
        // Step 2: Create candidate questions (without job association)
        Response questionResponse = albatrossFunctions.createCandidateQuestion(albatrossURL, albatrossTkn);
        assertThat("Failed to create candidate question", questionResponse.getStatusCode(), equalTo(200));
        JsonPath questionJp = questionResponse.jsonPath();
        int questionId = questionJp.get("data.user");
        assertThat("Question ID should not be null", questionId, notNullValue());
        
        // Step 3: Create candidate answer (without job association)
        CandidateAnswerRequest answerRequest = new CandidateAnswerRequest("Test Answer for Candidate", questionId, null, candidateId);
        Response answerResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers", 
                albatrossTkn, null, true, answerRequest);
        assertThat("Failed to create candidate answer", answerResponse.getStatusCode(), equalTo(200));
        
        return new Object[][] { { candidateId } };
    }
}
