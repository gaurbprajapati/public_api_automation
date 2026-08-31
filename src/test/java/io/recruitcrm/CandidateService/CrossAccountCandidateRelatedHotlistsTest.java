package io.recruitcrm.CandidateService;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.HotlistRelated;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountCandidateRelatedHotlistsTest extends TestBase {

    private String candidateSlugAccountA = "";
    private int hotlistIdAccountA;
    private int recordIdAccountA = 0;
    private AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountCandidateRelatedHotlistsTestData", groups = "nightly-build")
    public void crossAccountCandidateRelatedHotlistsOperations_Test(String testScenario, String accountType, String tokenType,
                                                                   String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                // ===== SEARCH OPERATIONS ====
                case "POST_HOTLISTS_SEARCH_CROSS_ACCOUNT":
                    createTestDataAccountA();
                    JSONObject crossAccountSearchRequestBody = new JSONObject();
                    crossAccountSearchRequestBody.put("entityName", "candidates");
                    crossAccountSearchRequestBody.put("recordId", recordIdAccountA);

                    response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related-hotlists/search/get",
                            token, null, null, true, crossAccountSearchRequestBody.toString());
                    break;

                // ===== DELETE OPERATIONS =====
                case "POST_HOTLISTS_DELETE_CROSS_ACCOUNT":
                    createTestDataAccountA();
                    JSONObject crossAccountDeleteRequestBody = new JSONObject();
                    crossAccountDeleteRequestBody.put("recordId", recordIdAccountA);
                    crossAccountDeleteRequestBody.put("entityName", "candidates");
                    crossAccountDeleteRequestBody.put("hotlistIds", new int[] {hotlistIdAccountA});

                    response = RestClient.doPost1("JSON", candidatesURL, "hotlists/related/delete",
                            token, null, null, true, crossAccountDeleteRequestBody.toString());
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        int actualStatusCode = response.getStatusCode();
        String actualStatusCodeStr = String.valueOf(actualStatusCode);

        switch (expectedResponse) {
            case "cross_account_isolation":
                if (operation.contains("SEARCH")) {
                    // For search operations - should get empty results or unauthorized
                    if (actualStatusCode == 404) {
                        JsonPath jp = response.jsonPath();
                        // Should get empty results or no access to Account A's data
                        if (jp.get("data") != null) {
                            assertThat("Cross-account search should return empty results",
                                    (Integer) jp.get("data.size()"), equalTo(0));
                        }
                    } else {
                        assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                                actualStatusCodeStr, equalTo(expectedStatusCode));
                    }
                } else if (operation.contains("DELETE")) {
                    // For delete operations - should get unauthorized or forbidden
                    assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                            actualStatusCodeStr, equalTo(expectedStatusCode));
                }
                break;

            case "unauthorized":
                assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                        actualStatusCodeStr, equalTo(expectedStatusCode));
                break;

            case "validation_error":
                assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                        actualStatusCodeStr, equalTo(expectedStatusCode));
                break;

            default:
                assertThat("Expected status code " + expectedStatusCode + " but got " + actualStatusCodeStr,
                        actualStatusCodeStr, equalTo(expectedStatusCode));
        }
    }

    private void createTestDataAccountA() {
        // Step 1: Create a candidate using Account A
        Response candidateResponse = allCrudFunctions.createCandidate(albatrossURL, accountA_Token);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));

        JsonPath candidateJp = candidateResponse.jsonPath();
        candidateSlugAccountA = candidateJp.get("data.candidate.slug");
        recordIdAccountA = candidateJp.get("data.candidate.id");

        assertThat("Candidate slug should not be null", candidateSlugAccountA, notNullValue());
        assertThat("Record ID should not be null", recordIdAccountA, notNullValue());

        // Step 2: Create a hotlist using Account A
        commanFunction function = new commanFunction();
        Response hotlistResponse = function.createNewHotlist(baseURL, accountA_apiKey, "candidate");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));

        JsonPath hotlistJp = hotlistResponse.jsonPath();
        hotlistIdAccountA = hotlistJp.getInt("id");

        assertThat("Hotlist ID should not be null", hotlistIdAccountA, notNullValue());

        // Step 3: Add candidate to hotlist using Account A
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(candidateSlugAccountA);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistIdAccountA));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, accountA_apiKey, null, pathParameters, true, hotlistRelated);
        assertThat("Failed to add candidate to hotlist", addResponse.getStatusCode(), equalTo(200));
    }

    @DataProvider(name = "crossAccountCandidateRelatedHotlistsTestData")
    public static Object[][] crossAccountCandidateRelatedHotlistsTestData() {
        return new Object[][]{
                // ===== SCENARIO : CROSS-ACCOUNT ISOLATION =====
                // Account B tries to access Account A's data
                {"SCENARIO_2_SEARCH_CROSS_ACCOUNT", "AccountB", "valid", "POST_HOTLISTS_SEARCH_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's hotlist data"},
                {"SCENARIO_2_DELETE_CROSS_ACCOUNT", "AccountB", "valid", "POST_HOTLISTS_DELETE_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not delete Account A's hotlist"},
        };
    }
}
