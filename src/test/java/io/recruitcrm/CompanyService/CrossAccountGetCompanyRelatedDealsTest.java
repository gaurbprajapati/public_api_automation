package io.recruitcrm.CompanyService;

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
public class CrossAccountGetCompanyRelatedDealsTest extends TestBase {

    private String companySlugAccountA;
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
        // Step 1: Create a company using Account A
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        companySlugAccountA = companyJp.get("slug");
        companyNameAccountA = companyJp.get("company_name");
        assertThat("Company slug should not be null", companySlugAccountA, notNullValue());
        assertThat("Company name should not be null", companyNameAccountA, notNullValue());

        // Step 2: Create a candidate (required for deal creation)
        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, accountA_Token);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        String candidateSlug = candidateJp.get("data.candidate.slug");
        assertThat("Candidate slug should not be null", candidateSlug, notNullValue());

        // Step 3: Create a contact (required for deal creation)
        Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlugAccountA);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        String contactSlug = contactJp.get("slug");

        // Step 4: Create a job using Account A
        Response jobResponse = function.createNewJob(baseURL, accountA_apiKey, companySlugAccountA, contactSlug);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");

        // Step 5: Create a deal linked to the company for Account A
        Deal deal = new Deal();
        deal.setName("Test Deal for Account A " + companyNameAccountA);
        deal.setDeal_value(15000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlugAccountA);
        deal.setJob_slug(jobSlug);
        deal.setContact_slugs(contactSlug);
        deal.setCandidate_slug(candidateSlug);

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals",
                accountA_apiKey, null, true, deal);
        assertThat("Failed to create test deal", dealResponse.getStatusCode(), equalTo(200));
        JsonPath dealJp = dealResponse.jsonPath();
        dealIdAccountA = dealJp.get("id");
        assertThat("Deal ID should not be null", dealIdAccountA, notNullValue());
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "crossAccountCompanyRelatedDealsTestData", groups = "nightly-build")
    public void crossAccountCompanyRelatedDealsOperations_Test(String testScenario, String accountType, String tokenType,
                                                               String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_COMPANY_RELATED_DEALS_CROSS_ACCOUNT":
                    // Account B tries to access Account A's company related deals
                    response = getCompanyRelatedDeals(companySlugAccountA, token);
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getCompanyRelatedDeals(String companySlug, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("page", "1");
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
        pathParams.put("companySlug", companySlug);

        return RestClient.doPost1("JSON", companyServiceURL, "companies/{companySlug}/related-deals",
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

                    // Verify that Account B cannot access Account A's company related deals
                    assertThat("Data should be null", isolationJp.get("data"), nullValue());
                } else {
                    assertThat("Expected status code", response.getStatusCode(), equalTo(Integer.parseInt(expectedStatusCode)));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountCompanyRelatedDealsTestData")
    public static Object[][] crossAccountCompanyRelatedDealsTestData() {
        return new Object[][]{
                // SCENARIO: Cross account isolation - Account B tries to access Account A's company related deals
                {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_COMPANY_RELATED_DEALS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's company related deals"},
        };
    }
}