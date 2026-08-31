package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetPitchCandidateStageHistoryTest extends TestBase {

    private String candidateSlugAccountA = "";
    private int recordIdAccountA = 0;
    private int contactIdAccountA = 0;
    private int candidateIdAccountA = 0;
    commanFunction commonFunc = new commanFunction();
    AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountPitchCandidateStageHistoryTestData", groups = "nightly-build")
    public void crossAccountPitchCandidateStageHistoryOperations_Test(String testScenario, String accountType, String tokenType,
                                                                      String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        Map<String, String> queryParams = new HashMap<>();

        try {
            switch (operation.toUpperCase()) {
                case "GET_PITCH_CANDIDATE_STAGE_HISTORY_CROSS_ACCOUNT":
                    // Account B tries to access Account A's data
                    createTestDataAccountA();
                    queryParams.put("contactId", String.valueOf(contactIdAccountA));
                    queryParams.put("candidateId", String.valueOf(candidateIdAccountA));
                    queryParams.put("recordId", String.valueOf(recordIdAccountA));

                    response = RestClient.doGet("JSON", candidatesURL, "candidates/pitch-candidate/stage-history/",
                            token, queryParams, null, true);
                    break;
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            // Handle expected exceptions for invalid operations
            if (expectedResponse.equals("unauthorized") || expectedResponse.equals("not_found")) {
                // Expected exception, test passes
                assertThat("Expected exception for " + operation, true, is(true));
            } else {
                throw new RuntimeException("Unexpected exception in " + operation + ": " + e.getMessage(), e);
            }
        }
    }

    private void createTestDataAccountA() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            // Step 1 & 2: Create candidate and company in parallel (independent operations)
            CompletableFuture<Response> candidateFuture = CompletableFuture.supplyAsync(() -> {
                Response response = commonFunc.createNewCandidateWithMandatoryFields(baseURL, accountA_apiKey);
                assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            CompletableFuture<Response> companyFuture = CompletableFuture.supplyAsync(() -> {
                Response response = commonFunc.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
                assertThat("Expected status code 200", response.getStatusCode(), equalTo(200));
                return response;
            }, executor);
            
            // Wait for both candidate and company to be created
            Response candidateResponse = candidateFuture.join();
            Response companyResponse = companyFuture.join();
            
            JsonPath candidateJp = candidateResponse.jsonPath();
            candidateSlugAccountA = candidateJp.get("slug");
            assertThat("Candidate slug should not be null", candidateSlugAccountA, notNullValue());
            
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlug = companyJp.get("slug");
            assertThat("Company slug should not be null", companySlug, notNullValue());
            
            // Step 1.1 & 3: Fetch candidate details and create contact in parallel
            CompletableFuture<Integer> candidateIdFuture = CompletableFuture.supplyAsync(() -> {
                Response fetchCandidateResponse = albatrossFunctions.getCandidateResponse(albatrossURL, accountA_Token, candidateSlugAccountA);
                assertThat("Failed to fetch candidate by slug", fetchCandidateResponse.getStatusCode(), equalTo(200));
                JsonPath fetchCandidateJp = fetchCandidateResponse.jsonPath();
                int candidateId = fetchCandidateJp.get("data.candidate.id");
                assertThat("Candidate ID should not be null", candidateId, notNullValue());
                return candidateId;
            }, executor);
            
            CompletableFuture<String> contactSlugFuture = CompletableFuture.supplyAsync(() -> {
                Response contactResponse = commonFunc.createNewContact_POST(baseURL, accountA_apiKey, companySlug);
                assertThat("Expected status code 200", contactResponse.getStatusCode(), equalTo(200));
                JsonPath contactJp = contactResponse.jsonPath();
                String contactSlug = contactJp.get("slug");
                assertThat("Contact slug should not be null", contactSlug, notNullValue());
                return contactSlug;
            }, executor);
            
            // Wait for both operations to complete
            candidateIdAccountA = candidateIdFuture.join();
            String contactSlug = contactSlugFuture.join();
            
            // Step 3.1: Fetch contact by slug to get proper contact ID
            Map<String, String> contactParams = new HashMap<>();
            contactParams.put("contact", contactSlug);
            Response fetchContactResponse = RestClient.doGet("JSON", albatrossURL, "contacts/{contact}", accountA_Token, null, contactParams, true);
            assertThat("Failed to fetch contact by slug", fetchContactResponse.getStatusCode(), equalTo(200));

            JsonPath fetchContactJp = fetchContactResponse.jsonPath();
            contactIdAccountA = Integer.parseInt(fetchContactJp.get("data.contact.id"));
            assertThat("Contact ID should not be null", contactIdAccountA, notNullValue());
            
            // Step 4: Pitch candidate to contact
            String pitchPath = "pitch/{candidate}/contact/{contact}";
            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("candidate", candidateSlugAccountA);
            pathParameters.put("contact", contactSlug);
            
            Response pitchResponse = RestClient.doPost1("JSON", baseURL, pitchPath, accountA_apiKey, null, pathParameters, true, null);
            assertThat("Failed to pitch candidate to contact", pitchResponse.getStatusCode(), equalTo(200));
            
            // Step 5: Get record ID from Albatross widget endpoint
            String widgetPath = "widgets/{candidateId}/pitch-candidate-data";
            Map<String, String> widgetParams = new HashMap<>();
            widgetParams.put("candidateId", String.valueOf(candidateIdAccountA));
            
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("entitytype", "5");
            
            Response widgetResponse = RestClient.doGet("JSON", albatrossURL, widgetPath, accountA_Token, queryParams, widgetParams, true);
            assertThat("Failed to get widget data", widgetResponse.getStatusCode(), equalTo(200));
            
            JsonPath widgetJp = widgetResponse.jsonPath();
            recordIdAccountA = widgetJp.get("data.records[0].id");
            
            assertThat("Record ID should not be null", recordIdAccountA, notNullValue());
            assertThat("Record ID should be positive", recordIdAccountA, greaterThan(0));
        } finally {
            executor.shutdown();
        }
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                // Should get empty results or unauthorized
                if (response.getStatusCode() == 200) {
                    // Verify empty results or no access to other account's data
                    JsonPath jp2 = response.jsonPath();
                    assertThat("Data should be empty array for cross account access",
                            (Integer) jp2.get("data.size()"), equalTo(0));
                } else {
                    assertThat("Expected status code", response.getStatusCode(), equalTo(Integer.parseInt(expectedStatusCode)));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountPitchCandidateStageHistoryTestData")
    public static Object[][] crossAccountPitchCandidateStageHistoryTestData() {
        return new Object[][]{
                // SCENARIO 2: Cross account isolation - Account B tries to access Account A's data
                {"SCENARIO_2_CROSS_ACCOUNT", "AccountB", "valid", "GET_PITCH_CANDIDATE_STAGE_HISTORY_CROSS_ACCOUNT", "200", "cross_account_isolation", "Account B should not access Account A's data"},
        };
    }
}
