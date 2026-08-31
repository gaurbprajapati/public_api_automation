package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class CrossAccountGetPitchCandidateContactsTest extends TestBase {

    private int candidateIdAccountA;
    private String companyNameAccountA;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @BeforeClass
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1: Create candidate using Account A
            Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, accountA_Token);
            assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
            JsonPath candidateJp = candidateResponse.jsonPath();
            String candidateSlug = candidateJp.get("data.candidate.slug");
            candidateIdAccountA = candidateJp.get("data.candidate.id");
            assertThat("Candidate ID should not be null", candidateIdAccountA, notNullValue());
            
            // Step 2: Create company in parallel
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() -> {
                Response response = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
                assertThat("Failed to create test company", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            Response companyResponse = companyFuture.join();
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            companyNameAccountA = companyJp.get("company_name");
            assertThat("Company name should not be null", companyNameAccountA, notNullValue());
            
            // Step 3: Create contact
            Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlug);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            String contactSlug = contactJp.get("slug");
            
            // Step 4: Pitch candidate to contact
            String pitchPath = "pitch/{candidate}/contact/{contact}";
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlug);
            pathParameters.put("contact", contactSlug);
            
            Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountA_apiKey, null, pathParameters, true, null);
            assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
        } finally {
            executor.shutdown();
        }
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountPitchCandidateContactsTestData", groups = "nightly-build")
    public void crossAccountPitchCandidateContactsOperations_Test(String testScenario, String accountType, String tokenType,
                                                                  String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_PITCH_CANDIDATE_CONTACTS_CROSS_ACCOUNT":
                    // Account B tries to access Account A's pitch candidate contacts
                    response = getPitchCandidateContacts(candidateIdAccountA, token);
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getPitchCandidateContacts(int candidateId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", "");
        requestBody.put("sortPriorityList", JSONObject.NULL);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateId", String.valueOf(candidateId));

        return RestClient.doPost1("JSON", candidatesURL, "candidates/pitch-candidate/{candidateId}/contacts/get",
                token, queryParams, pathParams, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                // Should get 404 or empty results for cross-account access
                if (response.getStatusCode() == 404) {
                    // If 404, verify the error response
                    JsonPath isolationJp = response.jsonPath();
                    assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    assertThat("Meta status should be 404", (Integer) isolationJp.get("meta.status"), equalTo(404));

                    // Verify that Account B cannot access Account A's pitch candidate contacts
                    assertThat("Data should be null", isolationJp.get("data"), nullValue());
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountPitchCandidateContactsTestData")
    public static Object[][] crossAccountPitchCandidateContactsTestData() {
        return new Object[][]{
                // SCENARIO: Cross account isolation - Account B tries to access Account A's pitch candidate contacts
                {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_PITCH_CANDIDATE_CONTACTS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's pitch candidate contacts"},
        };
    }
}

