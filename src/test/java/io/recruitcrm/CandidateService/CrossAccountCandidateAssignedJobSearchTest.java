package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;


@AccountType("CrossAccount")
public class CrossAccountCandidateAssignedJobSearchTest extends TestBase {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    commanFunction function = new commanFunction();

    private int testCandidateIdA;
    private int testCandidateIdB;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        createTestData("AccountA");
        createTestData("AccountB");
    }

    @Owner("Harika")
    @Test(dataProvider = "crossAccountCandidateAssignedJobSearchTestData", groups = {"candidate_service", "nightly-build"})
    public void crossAccountCandidateAssignedJobSearchOperations_Test(String testScenario, String accountType, String tokenType, 
            String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        
        Response response = null;
        Map<String, String> queryParameters = new HashMap<>();
        JSONObject searchPayload;
        
        try {
            switch (operation.toUpperCase()) {
                case "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A":
                case "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_CROSS_ACCOUNT":
                    queryParameters.put("page", "1");
                    queryParameters.put("size", "100");
                    searchPayload = createSearchPayload(testCandidateIdA, "", new ArrayList<>());
                    response = RestClient.doPost("JSON", candidatesURL, "candidate-assigned-job/search/get",
                            token, queryParameters, true, searchPayload);
                    break;

                default:
                    Assert.fail("Unsupported operation: " + operation);
            }
            
            int expectedStatus = Integer.parseInt(expectedStatusCode);
            assertThat("Response status code should match expected", response.getStatusCode(), is(equalTo(expectedStatus)));
            
            switch (expectedResponse) {
                case "success":
                    response.then().body("meta.message", Matchers.containsString("success"));
                    break;
                case "Unauthorised access":
                    response.then().body("meta.message", Matchers.is("Unauthorised access"));
                    break;
                case "not found":
                    response.then().body("errors[0].message", Matchers.containsString("not found"));
                    break;
                case "Bad Request":
                    response.then().body("error", Matchers.is("Bad Request"));
                    break;
            }
            
        } catch (Exception e) {
            if (expectedResponse.equals("Exception")) {
                // Expected exception, test passes
                return;
            } else {
                throw e;
            }
        }
    }


    @DataProvider(name = "crossAccountCandidateAssignedJobSearchTestData")
    public static Object[][] crossAccountCandidateAssignedJobSearchTestData() {
        return new Object[][] {
            // ===== SCENARIO 1: VALID CROSS-ACCOUNT OPERATIONS =====
            // Account A performs operations (should succeed)
            {"SCENARIO_1_SEARCH_OWN_CANDIDATE_A", "AccountA", "valid", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "200", "success", "Account A should search their own candidate's assigned jobs with valid token"},

            // ===== SCENARIO 2: CROSS-ACCOUNT SECURITY =====
            // Account B attempts to search Account A's data (should fail)
            {"SCENARIO_2_CROSS_ACCOUNT_SEARCH_B_TO_A", "AccountB", "valid", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_CROSS_ACCOUNT", "404", "not found", "Account B should not access Account A's candidate data"},
            
            // ===== SCENARIO 3: INVALID TOKEN OPERATIONS =====
            // Account A performs operations with invalid token (should fail)
            {"SCENARIO_3_INVALID_TOKEN_A", "AccountA", "invalid", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "401", "Unauthorised access", "Account A should be denied access with invalid token"},

            // ===== SCENARIO 4: EDGE CASES =====
            // Account C (non-existent) attempts operations
            {"SCENARIO_4_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "401", "Unauthorised access", "Non-existent account should return 401"},
            
            // Account A with expired token
            {"SCENARIO_4_EXPIRED_TOKEN", "AccountA", "expired", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "401", "Unauthorised access", "Expired token should return 401"},
            
            // Account A with malformed token
            {"SCENARIO_4_MALFORMED_TOKEN", "AccountA", "malformed", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "401", "Unauthorised access", "Malformed token should return 401"},
            
            // ===== SCENARIO 5: BOUNDARY TESTING =====
            // Account A with empty token
            {"SCENARIO_5_EMPTY_TOKEN", "AccountA", "empty", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "401", "Unauthorised access", "Empty token should return 401"},
            
            // Account A with null token
            {"SCENARIO_5_NULL_TOKEN", "AccountA", "null", "POST_CANDIDATE_ASSIGNED_JOB_SEARCH_A", "401", "Unauthorised access", "Null token should return 401"},

        };
    }


    private JSONObject createSearchPayload(Integer candidateId, String searchTerm, List<Map<String, Object>> sortPriorityList) {
        JSONObject searchPayload = new JSONObject();
        searchPayload.put("sortPriorityList", sortPriorityList);
        searchPayload.put("searchTerm", searchTerm);
        searchPayload.put("candidateId", candidateId);
        return searchPayload;
    }


    private void assignCandidateToJob(String candidateSlug, String jobSlug, String accountType) {
        try {
            // Assign candidate to job
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);

            String basePath = "candidates/{candidate}/assign";

            Map<String, String> queryParameters = new HashMap<>();
            queryParameters.put("job_slug", jobSlug);

            Response response = RestClient.doPost1("JSON", baseURL, basePath,
                    getAccountApiKey(accountType), queryParameters, pathParameters, true, null);

            assertThat("Response status code should be 200", response.getStatusCode(), is(equalTo(200)));
        } catch (Exception e) {
            Assert.fail("Failed to create test data: " + e.getMessage());
        }
    }


    public void createTestData(String accountType) {
        JsonPath candidateJsonPath = allCrudFunctions
                .createCandidate(albatrossURL, getTokenForAccount(accountType,"valid")).jsonPath();
        String jobSlug = function.getEntityResponse(baseURL, getAccountApiKey(accountType), "job");

        if (accountType.equals("AccountA")) {
            testCandidateIdA = candidateJsonPath.get("data.candidate.id");
            String testCandidateSlugA = candidateJsonPath.get("data.candidate.slug");
            assignCandidateToJob(testCandidateSlugA, jobSlug, accountType);
        }else{
            testCandidateIdB = candidateJsonPath.get("data.candidate.id");
            String testCandidateSlugB = candidateJsonPath.get("data.candidate.slug");
            assignCandidateToJob(testCandidateSlugB, jobSlug, accountType);
        }

    }
}
