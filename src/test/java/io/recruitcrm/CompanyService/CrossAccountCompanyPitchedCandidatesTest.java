package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountCompanyPitchedCandidatesTest extends TestBase {

    private int companyIdAccountA;
    private String companyNameAccountA;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @BeforeClass
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        try {
            // Step 1: Create company using Account A (public API)
            Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
            assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            companyNameAccountA = companyJp.get("company_name");
            assertThat("Company slug should not be null", companySlug, notNullValue());
            assertThat("Company name should not be null", companyNameAccountA, notNullValue());

            // Get company ID from albatross API using slug (response structure: data.company.id)
            Response companyDetailsResponse = albatrossFunctions.getCompanyResponse(albatrossURL, accountA_Token, companySlug);
            assertThat("Failed to get company details from albatross API", companyDetailsResponse.getStatusCode(), equalTo(200));
            JsonPath companyDetailsJp = companyDetailsResponse.jsonPath();
            companyIdAccountA = companyDetailsJp.get("data.company.id");
            assertThat("Company ID should not be null", companyIdAccountA, notNullValue());

            // Step 2: Create contact
            Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlug);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");

            // Step 3: Create candidate using Account A
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, accountA_Token);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");

            // Step 4: Pitch candidate to contact
            String pitchPath = "pitch/{candidate}/contact/{contact}";
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);
            pathParameters.put("contact", contactSlug);

            Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountA_apiKey, null, pathParameters, true, null);
            assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test data: " + e.getMessage(), e);
        }
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "crossAccountCompanyPitchedCandidatesTestData", groups = "nightly-build")
    public void crossAccountCompanyPitchedCandidatesOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        switch (operation.toUpperCase()) {
            case "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A":
                // Account A accesses their own company pitched candidates
                response = getCompanyPitchedCandidates(companyIdAccountA, token);
                break;
            case "GET_COMPANY_PITCHED_CANDIDATES_CROSS_ACCOUNT":
                // Account B tries to access Account A's company pitched candidates
                response = getCompanyPitchedCandidates(companyIdAccountA, token);
                break;

            default:
                throw new RuntimeException("Unknown operation: " + operation);
        }
        int expectedStatus = Integer.parseInt(expectedStatusCode);
        assertThat("Response status code should match expected", response.getStatusCode(), is(equalTo(expectedStatus)));

        verifyResponse(response, expectedStatusCode, expectedResponse, operation);
    }

    private Response getCompanyPitchedCandidates(int companyId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyId", String.valueOf(companyId));

        return RestClient.doPost1("JSON", companyServiceURL, "companies/{companyId}/pitched-candidates/get",
                token, queryParams, pathParams, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "success":
                response.then().body("meta.message", containsString("success"));
                break;
            case "cross_account_isolation":
                // Should get 404 or empty results for cross-account access
                if (response.getStatusCode() == 404) {
                    // If 404, verify the error response
                    JsonPath isolationJp = response.jsonPath();
                    assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    assertThat("Meta status should be 404", (Integer) isolationJp.get("meta.status"), equalTo(404));

                    // Verify that Account B cannot access Account A's company pitched candidates
                    assertThat("Data should be null", isolationJp.get("data"), nullValue());
                } else if (response.getStatusCode() == 200) {
                    // If 200, verify empty results
                    JsonPath isolationJp = response.jsonPath();
                    assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    List<Map<String, Object>> data = isolationJp.getList("data");
                    assertThat("Data should be empty for cross-account access", data.isEmpty(), is(true));
                }
                break;
            case "Unauthorised access":
                response.then().body("meta.message", is("Unauthorised access"));
                break;
            case "not found":
                response.then().body("errors[0].message", containsString("not found"));
                break;
            case "Bad Request":
                response.then().body("error", is("Bad Request"));
                break;
        }
    }

    @DataProvider(name = "crossAccountCompanyPitchedCandidatesTestData")
    public static Object[][] crossAccountCompanyPitchedCandidatesTestData() {
        return new Object[][]{
                // ===== SCENARIO 1: CROSS-ACCOUNT SECURITY =====
                // Account B attempts to access Account A's data (should fail)
                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A", "AccountB", "valid", "GET_COMPANY_PITCHED_CANDIDATES_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's company pitched candidates"},

                // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
                // Account A performs operations with invalid token (should fail)
                {"SCENARIO_3_INVALID_TOKEN_A", "AccountA", "invalid", "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Account A should be denied access with invalid token"},

                // ===== SCENARIO 3: EDGE CASES =====
                // Account C (non-existent) attempts operations
                {"SCENARIO_4_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Non-existent account should return 401"},

                // Account A with expired token
                {"SCENARIO_4_EXPIRED_TOKEN", "AccountA", "expired", "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Expired token should return 401"},

                // Account A with malformed token
                {"SCENARIO_4_MALFORMED_TOKEN", "AccountA", "malformed", "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Malformed token should return 401"},

                // ===== SCENARIO 4: BOUNDARY TESTING =====
                // Account A with empty token
                {"SCENARIO_5_EMPTY_TOKEN", "AccountA", "empty", "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Empty token should return 401"},

                // Account A with null token
                {"SCENARIO_5_NULL_TOKEN", "AccountA", "null", "GET_COMPANY_PITCHED_CANDIDATES_ACCOUNT_A", "401", "Unauthorised access", "Null token should return 401"},
        };
    }
}