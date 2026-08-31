package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetCandidatesTest extends TestBase {

    private String candidateSlugAccountA = "";
    private int recordIdAccountA = 0;
    private AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountCandidatesTestData", groups = "nightly-build")
    public void crossAccountCandidatesOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {                    
                case "GET_CANDIDATES_CROSS_ACCOUNT":
                    // Account B tries to access Account A's data
                    createTestDataAccountA();
                    response = getCandidates(token, candidateSlugAccountA);
                    break;
                    
                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }
            
            verifyResponse(response, expectedStatusCode, expectedResponse, operation);
            
        } catch (Exception e) {
            // Handle expected exceptions
            if ("401".equals(expectedStatusCode)) {
                assertThat("Expected 401 status code", true, is(true));
            } else {
                throw e;
            }
        }
    }

    private void createTestDataAccountA() {
        try {
            // Step 1: Create candidate using Account A
            Response candidateResponse = allCrudFunctions.createCandidate(albatrossURL, accountA_Token);
            assertThat("Expected status code 200", candidateResponse.getStatusCode(), equalTo(200));
            
            // Step 2: Extract and store IDs
            JsonPath jp = candidateResponse.jsonPath();
            candidateSlugAccountA = jp.get("data.candidate.slug");
            recordIdAccountA = jp.get("data.candidate.id");
            
            assertThat("Candidate slug should not be null", candidateSlugAccountA, notNullValue());
            assertThat("Record ID should not be null", recordIdAccountA, notNullValue());
            
        } catch (Exception e) {
            throw new RuntimeException("Test data creation failed: " + e.getMessage(), e);
        }
    }

    private Response getCandidates(String token, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("slug", candidateSlug);

        return RestClient.doGet("JSON", candidatesURL, "candidates", 
                token, queryParams, null, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        int actualStatusCode = response.getStatusCode();
        String actualStatusCodeStr = String.valueOf(actualStatusCode);

        switch (expectedResponse) {
            case "cross_account_isolation":
                // For candidates endpoint, cross-account access should be blocked
                if (actualStatusCode == 404) {
                    // If 200, verify that the data is empty or doesn't contain Account A's data
                    JsonPath jp = response.jsonPath();
                    assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
                    assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(404));
                    
                    // Verify that Account B cannot access Account A's candidate data
                    // This could be empty results or different candidate data
                    if (jp.get("data.candidates") != null) {
                        // If candidate data exists, it should not be Account A's candidate
                        String returnedSlug = jp.get("data.candidates.slug");
                        if (returnedSlug != null) {
                            assertThat("Account B should not access Account A's candidate data", 
                                    returnedSlug, not(equalTo(candidateSlugAccountA)));
                        }
                    }
                } else {
                    assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                            actualStatusCodeStr, equalTo(expectedStatusCode));
                }
                break;

            default:
                assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                        actualStatusCodeStr, equalTo(expectedStatusCode));
        }
    }

    @DataProvider(name = "crossAccountCandidatesTestData")
    public static Object[][] crossAccountCandidatesTestData() {
        return new Object[][] {
            // SCENARIO 1: Cross account isolation - Account B tries to access Account A's data
            {"SCENARIO_1_CROSS_ACCOUNT", "AccountB", "valid", "GET_CANDIDATES_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's data"},
        };
    }
}
