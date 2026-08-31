package io.recruitcrm.albatross.candidate;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountCandidateHiringStageHistoryRemarkSecurityTest extends TestBase {

    AllCrudFunctions function = new AllCrudFunctions();
    commanFunction commanFunction = new commanFunction();

    private String candidateSlugForCrossAccountTesting;
    private int jobIdForCrossAccountTesting;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        setupCrossAccountTokens();
        createData();
    }

    private void createData() {
        // Use Account A token for data creation
        String token = getTokenForAccount("AccountA", "valid");
        
        Response response = function.createCandidate(albatrossURL, token);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200, but got " + response.getStatusCode());
        candidateSlugForCrossAccountTesting = response.jsonPath().getString("data.candidate.slug");

        String jobSlug = commanFunction.getEntityResponse(baseURL, accountA_apiKey, "job");
        Response jobResponse = function.getJobResponse(albatrossURL, token, jobSlug);
        Assert.assertEquals(jobResponse.getStatusCode(), 200, "Expected status code 200, but got " + jobResponse.getStatusCode());
        jobIdForCrossAccountTesting = jobResponse.jsonPath().getInt("data.job.id");

        Response assignResponse = commanFunction.assignCandidateByJobSlugAndCandidateSlug(baseURL, accountA_apiKey, jobSlug, candidateSlugForCrossAccountTesting);
        Assert.assertEquals(assignResponse.getStatusCode(), 200, "Expected status code 200, but got " + assignResponse.getStatusCode());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountCandidateHiringStageHistoryRemarkTestData", groups = "nightly-build")
    public void crossAccountCandidateHiringStageHistoryRemarkOperations_Test(String scenario, String accountType, String tokenType,
                                                                             String httpMethod, String expectedStatusCode,
                                                                             String expectedResponse, String description) {

        String basePath = "candidates/get-hiring-stage-history-remark";

        JSONObject requestBody = new JSONObject();
        requestBody.put("candidateslug", candidateSlugForCrossAccountTesting);
        requestBody.put("jobid", jobIdForCrossAccountTesting);

        String token = getTokenForAccount(accountType, tokenType);

        Response response = RestClient.doPost("JSON", albatrossURL, basePath, token, null, true, requestBody.toString());

        validateCrossAccountResponse(response, expectedResponse, expectedStatusCode, description);
    }

    private void validateCrossAccountResponse(Response response, String expectedResponse, String expectedStatusCode, String description) {
        int actualStatusCode = response.getStatusCode();
        JsonPath jp = response.jsonPath();

        if (!expectedStatusCode.equals("any")) {
            int expectedCode = Integer.parseInt(expectedStatusCode);
            Assert.assertEquals(actualStatusCode, expectedCode,
                    "Scenario: " + description + " - Expected status code " + expectedCode + ", but got " + actualStatusCode);
        }

        switch (expectedResponse) {
            case "success":
                Assert.assertEquals(jp.get("message_type"), "is-success",
                        "Scenario: " + description + " - Expected message_type to be is-success");
                Assert.assertNotNull(jp.get("data.records"),
                        "Scenario: " + description + " - Expected data to be present");
                break;

            case "unauthorized":
                Assert.assertEquals(jp.get("error"), "Unauthorized",
                        "Scenario: " + description + " - Expected error to be Unauthorized");
                break;

            case "danger":
                Assert.assertEquals(jp.get("message_type"), "is-danger",
                        "Scenario: " + description + " - Expected message_type to be is-danger");
                break;

            default:
                try {
                    response.then().body("error_message", Matchers.containsString(expectedResponse));
                } catch (Exception e) {
                    try {
                        response.then().body("error", Matchers.containsString(expectedResponse));
                    } catch (Exception e2) {
                        Assert.fail("Response validation failed due to exception:"+e.getMessage()+ " and "+ e2.getMessage());
                    }
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountCandidateHiringStageHistoryRemarkTestData")
    public static Object[][] crossAccountCandidateHiringStageHistoryRemarkTestData() {
        return new Object[][]{
                // AccountA (Primary Account) - Valid Operations
                {"SCENARIO_1_READ", "AccountA", "valid", "POST", "200", "success", "AccountA reads candidate hiring stage history remark with valid token - should succeed"},

                // AccountB (Secondary Account) - Cross-Account Access Attempts
                {"SCENARIO_2_READ", "AccountB", "valid", "POST", "200", "danger", "AccountB attempts to read candidate hiring stage history remark from AccountA - should be unauthorized"},
                {"SCENARIO_3_READ", "AccountB", "invalid", "POST", "401", "unauthorized", "AccountB attempts to read candidate hiring stage history remark with invalid token - should be unauthorized"},

                // AccountC (Non-existent Account) - Edge Cases
                {"SCENARIO_4_READ", "AccountC", "valid", "POST", "401", "unauthorized", "AccountC attempts to read candidate hiring stage history remark - should be unauthorized"},

                // Invalid Token Scenarios
                {"SCENARIO_5_READ", "AccountA", "invalid", "POST", "401", "unauthorized", "AccountA reads candidate hiring stage history remark with invalid token - should be unauthorized"},
                {"SCENARIO_6_READ", "AccountA", "expired", "POST", "401", "unauthorized", "AccountA reads candidate hiring stage history remark with expired token - should be token expired"},
                {"SCENARIO_8_READ", "AccountA", "empty", "POST", "401", "unauthorized", "AccountA reads candidate hiring stage history remark with empty token - should be unauthorized"},
                {"SCENARIO_9_READ", "AccountA", "null", "POST", "401", "unauthorized", "AccountA reads candidate hiring stage history remark with null token - should be unauthorized"},

                // Cross-Account Invalid Token Scenarios
                {"SCENARIO_10_READ", "AccountB", "expired", "POST", "401", "unauthorized", "AccountB attempts to read candidate hiring stage history remark with expired token - should be token expired"},
                {"SCENARIO_11_READ", "AccountB", "malformed", "POST", "401", "unauthorized", "AccountB attempts to read candidate hiring stage history remark with malformed token - should be unauthorized"},
                {"SCENARIO_12_READ", "AccountB", "empty", "POST", "401", "unauthorized", "AccountB attempts to read candidate hiring stage history remark with empty token - should be unauthorized"},
                {"SCENARIO_13_READ", "AccountB", "null", "POST", "401", "unauthorized", "AccountB attempts to read candidate hiring stage history remark with null token - should be unauthorized"},

                // Boundary Testing
                {"SCENARIO_14_READ", "AccountA", "valid", "POST", "200", "success", "AccountA reads candidate hiring stage history remark with valid token - boundary test for success case"},
                {"SCENARIO_15_READ", "AccountB", "valid", "POST", "200", "danger", "AccountB attempts to read candidate hiring stage history remark with valid token - boundary test for unauthorized case"}
        };
    }
}
