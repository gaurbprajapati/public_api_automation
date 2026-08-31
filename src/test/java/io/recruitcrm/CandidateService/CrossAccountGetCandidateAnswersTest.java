package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.CandidateAnswerRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.reaper.ThreadManager;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetCandidateAnswersTest extends TestBase {

    private int candidateIdAccountA;
    private int jobIdAccountA;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @BeforeClass
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        // Step 1: Create a candidate using Account A
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, accountA_Token);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));

        JsonPath candidateJp = candidateResponse.jsonPath();
        candidateIdAccountA = candidateJp.get("data.candidate.id");
        assertThat("Candidate ID should not be null", candidateIdAccountA, notNullValue());

        // Step 2: Create a company and contact first (required for job creation)
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");

        Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        // Step 3: Create a job using Account A
        Response jobResponse = function.createNewJob(baseURL, accountA_apiKey, companySlug, contactSlug);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));

        JsonPath jobJp = jobResponse.jsonPath();
        jobIdAccountA = jobJp.get("id");
        assertThat("Job ID should not be null", jobIdAccountA, notNullValue());

        // Step 4: Create candidate questions for Account A
        Response questionResponse = albatrossFunctions.createCandidateQuestion(albatrossURL, accountA_Token);
        assertThat("Failed to create candidate question", questionResponse.getStatusCode(), equalTo(200));
        JsonPath questionJp = questionResponse.jsonPath();
        int questionId = questionJp.get("data.user");
        assertThat("Question ID should not be null", questionId, notNullValue());

        // Step 5: Create candidate answer for Account A
        CandidateAnswerRequest answerRequest = new CandidateAnswerRequest("Test Answer for Account A", questionId, null, candidateIdAccountA);
        Response answerResponse = RestClient.doPost("JSON", albatrossURL, "candidate-answers",
                accountA_Token, null, true, answerRequest);
        assertThat("Failed to create candidate answer", answerResponse.getStatusCode(), equalTo(200));
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountCandidateAnswersTestData", groups = "nightly-build")
    public void crossAccountCandidateAnswersOperations_Test(String testScenario, String accountType, String tokenType,
                                                            String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_CANDIDATE_ANSWERS_CROSS_ACCOUNT":
                    response = getCandidateAnswers(candidateIdAccountA, jobIdAccountA, token);
                    break;
                case "GET_CANDIDATE_ANSWERS_WITHOUT_JOB_CROSS_ACCOUNT":
                    response = getCandidateAnswersWithoutJob(candidateIdAccountA, token);
                    break;
                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getCandidateAnswers(int candidateId, int jobId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);
        requestBody.put("jobId", jobId);

        return RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get",
                token, queryParams, null, true, requestBody.toString());
    }

    private Response getCandidateAnswersWithoutJob(int candidateId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));

        JSONObject requestBody = new JSONObject();
        requestBody.put("filterQuestions", 0);
        requestBody.put("filterUnasnwered", 0);

        return RestClient.doPost1("JSON", candidatesURL, "candidate-answers/get",
                token, queryParams, null, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                // Should get 404 or empty results for cross-account access
                if (response.getStatusCode() == 404) {
                    // If 200, verify empty results or no access to other account's data
                    JsonPath isolationJp = response.jsonPath();
                    assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    assertThat("Meta status should be 404", (Integer) isolationJp.get("meta.status"), equalTo(404));

                    // Verify that Account B cannot access Account A's candidate answers
                    // This could be empty results or filtered results
                    assertThat("Data should not be null", isolationJp.get("data"), nullValue());
                } else {
                    assertThat("Expected status code", response.getStatusCode(), equalTo(Integer.parseInt(expectedStatusCode)));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountCandidateAnswersTestData")
    public static Object[][] crossAccountCandidateAnswersTestData() {
        return new Object[][]{
                // SCENARIO: Cross account isolation - Account B tries to access Account A's candidate answers
                {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_CANDIDATE_ANSWERS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's candidate answers"},
                
                // SCENARIO: Cross account isolation - Account B tries to access Account A's candidate answers without job
                {"SCENARIO_CROSS_ACCOUNT_WITHOUT_JOB", "AccountB", "valid", "GET_CANDIDATE_ANSWERS_WITHOUT_JOB_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's candidate answers without job"},
        };
    }
}
