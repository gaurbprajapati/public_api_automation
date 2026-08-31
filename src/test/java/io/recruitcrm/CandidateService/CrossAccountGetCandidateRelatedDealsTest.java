package io.recruitcrm.CandidateService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.Deal;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetCandidateRelatedDealsTest extends TestBase {

    private String candidateSlugAccountA;
    private int dealIdAccountA;
    private String companyNameAccountA;
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
        candidateSlugAccountA = candidateJp.get("data.candidate.slug");
        assertThat("Candidate slug should not be null", candidateSlugAccountA, notNullValue());

        // Step 2: Create a company and contact first (required for deal creation)
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlug = companyJp.get("slug");
        companyNameAccountA = companyJp.get("company_name");
        assertThat("Company name should not be null", companyNameAccountA, notNullValue());

        Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlug);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        // Step 3: Create a job using Account A
        Response jobResponse = function.createNewJob(baseURL, accountA_apiKey, companySlug, contactSlug);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");

        // Step 4: Create a deal linked to the candidate for Account A
        Deal deal = new Deal();
        deal.setName("Test Deal for Account A " + companyNameAccountA);
        deal.setDeal_value(15000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlug);
        deal.setJob_slug(jobSlug);
        deal.setContact_slugs(contactSlug);
        deal.setCandidate_slug(candidateSlugAccountA);

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals",
                accountA_apiKey, null, true, deal);
        assertThat("Failed to create test deal", dealResponse.getStatusCode(), equalTo(200));
        JsonPath dealJp = dealResponse.jsonPath();
        dealIdAccountA = dealJp.get("id");
        assertThat("Deal ID should not be null", dealIdAccountA, notNullValue());
    }

    @Owner("Raj Pandey")
    @Test(dataProvider = "crossAccountCandidateRelatedDealsTestData", groups = "nightly-build")
    public void crossAccountCandidateRelatedDealsOperations_Test(String testScenario, String accountType, String tokenType,
                                                                 String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_CANDIDATE_RELATED_DEALS_CROSS_ACCOUNT":
                    // Account B tries to access Account A's candidate related deals
                    response = getCandidateRelatedDeals(candidateSlugAccountA, token);
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getCandidateRelatedDeals(String candidateSlug, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "0");
        queryParams.put("size", "25");

        JSONObject requestBody = new JSONObject();
        requestBody.put("searchTerm", companyNameAccountA);

        JSONArray sortOrder = new JSONArray();
        JSONObject sort1 = new JSONObject();
        sort1.put("field", "updatedon");
        sort1.put("order", "desc");
        JSONObject sort2 = new JSONObject();
        sort2.put("field", "dealvalue");
        sort2.put("order", "asc");
        sortOrder.put(sort1);
        sortOrder.put(sort2);
        requestBody.put("sortOrder", sortOrder);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("candidateSlug", candidateSlug);

        return RestClient.doPost1("JSON", candidatesURL, "candidates/{candidateSlug}/related-deals",
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

                    // Verify that Account B cannot access Account A's candidate related deals
                    assertThat("Data should be null", isolationJp.get("data"), nullValue());
                } else {
                    assertThat("Expected status code", response.getStatusCode(), equalTo(Integer.parseInt(expectedStatusCode)));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountCandidateRelatedDealsTestData")
    public static Object[][] crossAccountCandidateRelatedDealsTestData() {
        return new Object[][]{
                // SCENARIO: Cross account isolation - Account B tries to access Account A's candidate related deals
                {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_CANDIDATE_RELATED_DEALS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's candidate related deals"},
        };
    }
}
