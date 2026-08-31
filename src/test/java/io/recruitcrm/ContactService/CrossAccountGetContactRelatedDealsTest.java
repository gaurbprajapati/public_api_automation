package io.recruitcrm.ContactService;

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
public class CrossAccountGetContactRelatedDealsTest extends TestBase {

    private String contactSlugAccountA;
    private int dealIdAccountA;
    private String contactNameAccountA;
    private String companyNameAccountA;
    private AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        createTestDataAccountA();
    }

    private void createTestDataAccountA() {
        Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath companyJp = companyResponse.jsonPath();
        String companySlugAccountA = companyJp.get("slug");
        companyNameAccountA = companyJp.get("company_name");
        assertThat("Company slug should not be null", companySlugAccountA, notNullValue());
        assertThat("Company name should not be null", companyNameAccountA, notNullValue());

        Response candidateResponse = albatrossFunctions.createCandidate(albatrossURL, accountA_Token);
        assertThat("Failed to create test candidate", candidateResponse.getStatusCode(), equalTo(200));
        JsonPath candidateJp = candidateResponse.jsonPath();
        String candidateSlug = candidateJp.get("data.candidate.slug");
        assertThat("Candidate slug should not be null", candidateSlug, notNullValue());

        Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlugAccountA);
        assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
        JsonPath contactJp = contactResponse.jsonPath();
        contactSlugAccountA = contactJp.get("slug");
        String contactFirstName = contactJp.get("first_name");
        String contactLastName = contactJp.get("last_name");
        contactNameAccountA = (contactFirstName != null ? contactFirstName : "") + " " + (contactLastName != null ? contactLastName : "");
        contactNameAccountA = contactNameAccountA.trim();
        assertThat("Contact slug should not be null", contactSlugAccountA, notNullValue());

        Response jobResponse = function.createNewJob(baseURL, accountA_apiKey, companySlugAccountA, contactSlugAccountA);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlug = jobJp.get("slug");

        Deal deal = new Deal();
        deal.setName("Test Deal for Account A Contact " + contactNameAccountA);
        deal.setDeal_value(15000);
        deal.setClose_date("2025-12-31");
        deal.setDeal_stage("1");
        deal.setDeal_type("1");
        deal.setCompany_slug(companySlugAccountA);
        deal.setJob_slug(jobSlug);
        deal.setContact_slugs(contactSlugAccountA);
        deal.setCandidate_slug(candidateSlug);

        Response dealResponse = RestClient.doPost("JSON", baseURL, "deals",
                accountA_apiKey, null, true, deal);
        assertThat("Failed to create test deal", dealResponse.getStatusCode(), equalTo(200));
        JsonPath dealJp = dealResponse.jsonPath();
        dealIdAccountA = dealJp.get("id");
        assertThat("Deal ID should not be null", dealIdAccountA, notNullValue());
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "crossAccountContactRelatedDealsTestData", groups = {"contact_service", "nightly-build"})
    public void crossAccountContactRelatedDealsOperations_Test(String testScenario, String accountType, String tokenType,
                                                               String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_CONTACT_RELATED_DEALS_CROSS_ACCOUNT":
                    response = getContactRelatedDeals(contactSlugAccountA, token);
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            assertThat("Response status code should match expected", response.getStatusCode(), is(equalTo(expectedStatus)));
            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            assertThat("Unexpected exception: " + e.getMessage(), false, is(true));
        }
    }

    private Response getContactRelatedDeals(String contactSlug, String token) {
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
        pathParams.put("contactSlug", contactSlug);

        return RestClient.doPost1("JSON", contactServiceURL, "contacts/{contactSlug}/related-deals",
                token, queryParams, pathParams, true, requestBody.toString());
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "cross_account_isolation":
                if (response.getStatusCode() == 404) {
                    JsonPath isolationJp = response.jsonPath();
                    assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                    assertThat("Meta status should be 404", (Integer) isolationJp.get("meta.status"), equalTo(404));
                    assertThat("Data should be null", isolationJp.get("data"), nullValue());
                } else {
                    assertThat("Expected status code", response.getStatusCode(), equalTo(Integer.parseInt(expectedStatusCode)));
                }
                break;
        }
    }

    @DataProvider(name = "crossAccountContactRelatedDealsTestData")
    public static Object[][] crossAccountContactRelatedDealsTestData() {
        return new Object[][]{
                {"SCENARIO_CROSS_ACCOUNT", "AccountB", "valid", "GET_CONTACT_RELATED_DEALS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's contact related deals"},
        };
    }
}
