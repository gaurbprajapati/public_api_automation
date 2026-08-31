package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountEducationHistorySecurityTest extends TestBase {
    
    private String candidateIdA = "";
    private String candidateIdB = "";
    private AllCrudFunctions function = new AllCrudFunctions();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        setupCrossAccountTokens();
        createTestDataForBothAccounts();
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountEducationHistoryTestData", groups = {"candidate_service", "nightly-build"})
    public void crossAccountEducationHistoryOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        Map<String, String> pathParams = new HashMap<>();
        
        switch (operation.toUpperCase()) {
            case "GET_EDUCATION_HISTORY_A":
                pathParams.put("candidateId", candidateIdA);
                response = RestClient.doGet("JSON", candidatesURL, "candidates/{candidateId}/education-history", 
                    token, null, pathParams, true);
                break;
                
            case "GET_EDUCATION_HISTORY_B":
                pathParams.put("candidateId", candidateIdB);
                response = RestClient.doGet("JSON", candidatesURL, "candidates/{candidateId}/education-history", 
                    token, null, pathParams, true);
                break;
                
            default:
                throw new AssertionError("Unsupported operation: " + operation);
        }
        
        int expectedStatus = Integer.parseInt(expectedStatusCode);
        int actualStatus = response.getStatusCode();
        
        assertThat(String.format("Test Case: %s | Account: %s | Token: %s | Operation: %s | Expected: %d, Actual: %d",
                testScenario, accountType, tokenType, operation, expectedStatus, actualStatus), actualStatus, is(equalTo(expectedStatus)));
        
        switch (expectedResponse) {
            case "success": {
                assertThat(String.format("Test Case: %s | Meta should not be null", testScenario), response.jsonPath().get("meta"), is(notNullValue()));
                assertThat(String.format("Test Case: %s | Data should not be null", testScenario), response.jsonPath().get("data"), is(notNullValue()));
                break;
            }
                
            case "invalid_token": {
                String actualMessage = response.jsonPath().getString("meta.message");
                String actualData = response.jsonPath().getString("data");
                assertThat(String.format("Test Case: %s | Meta message should be 'Unauthorised access', got: %s", testScenario, actualMessage), actualMessage, is(equalTo("Unauthorised access")));
                assertThat(String.format("Test Case: %s | Data should be 'Invalid token', got: %s", testScenario, actualData), actualData, is(equalTo("Internal Server Error")));
                break;
            }
                
            case "access_denied": {
                String metaMessage = response.jsonPath().getString("meta.message");
                Object dataValue = response.jsonPath().get("data");
                String errorMessage = response.jsonPath().getString("errors[0].message");
                assertThat(String.format("Test Case: %s | Meta message should be null for access denied, got: %s", testScenario, metaMessage), metaMessage, is(nullValue()));
                assertThat(String.format("Test Case: %s | Data should be null for access denied, got: %s", testScenario, dataValue), dataValue, is(nullValue()));
                assertThat(String.format("Test Case: %s | Error message should contain 'not found', got: %s", testScenario, errorMessage), errorMessage, allOf(notNullValue(), containsString("not found")));
                break;
            }
                
            default: {
                String actualErrorMessage = response.jsonPath().getString("error_message");
                assertThat(String.format("Test Case: %s | Error message should be '%s', got: %s", testScenario, expectedResponse, actualErrorMessage), actualErrorMessage, is(equalTo(expectedResponse)));
                break;
            }
        }
    }

    @DataProvider(name = "crossAccountEducationHistoryTestData")
    public static Object[][] crossAccountEducationHistoryTestData() {
        return new Object[][] {
            {"ACCOUNT_A_OWN_DATA", "AccountA", "valid", "GET_EDUCATION_HISTORY_A", "200", "success", "Account A should access its own candidate's education history"},
            {"ACCOUNT_B_ACCESS_A_DATA", "AccountB", "valid", "GET_EDUCATION_HISTORY_A", "404", "access_denied", "Account B should not access Account A's candidate data"},
            {"ACCOUNT_A_INVALID_TOKEN", "AccountA", "invalid", "GET_EDUCATION_HISTORY_A", "401", "invalid_token", "Account A should be denied with invalid token"}
        };
    }

    private void createTestDataForBothAccounts() {
        JSONObject candidateJson = readJsonFileFromPath("src/test/resources/testData/candidate_dataW&E.json");
        
        String authTokenAccountA = getTokenForAccount("AccountA", "valid");
        JSONObject candidate1Payload = candidateJson.getJSONObject("candidate1");

        Response responseA = function.createCandidateWithJson(albatrossURL, authTokenAccountA, candidate1Payload);
        candidateIdA = String.valueOf(responseA.jsonPath().getInt("data.candidate.id"));
        
        String authTokenAccountB = getTokenForAccount("AccountB", "valid");
        JSONObject candidate2Payload = candidateJson.getJSONObject("candidate2");
        Response responseB = function.createCandidateWithJson(albatrossURL, authTokenAccountB, candidate2Payload);
        candidateIdB = String.valueOf(responseB.jsonPath().getInt("data.candidate.id"));
    }
}
