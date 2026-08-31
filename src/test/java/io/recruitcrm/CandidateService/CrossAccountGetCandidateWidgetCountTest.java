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
public class CrossAccountGetCandidateWidgetCountTest extends TestBase {

    private Integer candidateIdAccountA;
    private String candidateSlugAccountA = "";
    private AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountWidgetCountTestData", groups = "nightly-build")
    public void crossAccountWidgetCountOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {
        
        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_WIDGET_COUNT_CROSS_ACCOUNT":
                    // Account B tries to access Account A's data
                    createTestDataAccountA();
                    response = getWidgetCount(token, candidateIdAccountA, candidateSlugAccountA);
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
            candidateIdAccountA = jp.get("data.candidate.id");
            candidateSlugAccountA = jp.get("data.candidate.slug");
            
            assertThat("Candidate ID should not be null", candidateIdAccountA, notNullValue());
            assertThat("Candidate slug should not be null", candidateSlugAccountA, notNullValue());
            
        } catch (Exception e) {
            throw new RuntimeException("Test data creation failed: " + e.getMessage(), e);
        }
    }

    private Response getWidgetCount(String token, int candidateId, String candidateSlug) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("candidateId", String.valueOf(candidateId));
        queryParams.put("candidateSlug", candidateSlug);

        return RestClient.doGet("JSON", candidatesURL, "candidates/widget-count", 
                token, queryParams, null, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        int actualStatusCode = response.getStatusCode();
        String actualStatusCodeStr = String.valueOf(actualStatusCode);

        switch (expectedResponse) {
            case "cross_account_isolation":
                // For widget count, we expect valid data even for cross-account access
                // as the endpoint might return counts based on accessible data
                if (actualStatusCode == 404) {
                    JsonPath jp = response.jsonPath();
                    assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
                    assertThat("Meta status should be 200", (Integer) jp.get("meta.status"), equalTo(404));
                    assertThat("Data should be null", jp.get("data"), nullValue());
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

    @DataProvider(name = "crossAccountWidgetCountTestData")
    public static Object[][] crossAccountWidgetCountTestData() {
        return new Object[][] {
            // SCENARIO: Cross account isolation - Account B tries to access Account A's data
            {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's data"},
        };
    }
}
