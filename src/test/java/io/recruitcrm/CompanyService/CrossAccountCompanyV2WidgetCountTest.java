package io.recruitcrm.CompanyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

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
public class CrossAccountCompanyV2WidgetCountTest extends TestBase {

    private static final String BASE_PATH = "widget-count";

    private int companyIdAccountA;
    private String companySlugAccountA;
    private final AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private final commanFunction function = new commanFunction();

    @BeforeClass
    public void setUp() {
        // Create test data for Account A
        createTestDataAccountA();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "crossAccountCompanyV2WidgetCountTestData", groups = "nightly-build")
    public void crossAccountCompanyV2WidgetCountOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        switch (operation.toUpperCase()) {
            case "GET_WIDGET_COUNT_CROSS_ACCOUNT":
                // Account B tries to access Account A's company widget count
                response = getWidgetCount(companyIdAccountA, companySlugAccountA, token, null);
                break;
            case "GET_WIDGET_COUNT_WITH_WIDGET_KEYS_CROSS_ACCOUNT":
                // Account B tries to access Account A's company widget count with widgetKeys
                response = getWidgetCount(companyIdAccountA, companySlugAccountA, token, "jobs,hotlists");
                break;

            default:
                throw new RuntimeException("Unknown operation: " + operation);
        }
        int expectedStatus = Integer.parseInt(expectedStatusCode);
        assertThat("Response status code should match expected", response.getStatusCode(), is(equalTo(expectedStatus)));

        verifyResponse(response, expectedStatusCode, expectedResponse, operation);
    }

    private Response getWidgetCount(int companyId, String companySlug, String token, String widgetKeys) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "companies");
        queryParams.put("recordId", String.valueOf(companyId));
        queryParams.put("recordSlug", companySlug);
        if (widgetKeys != null && !widgetKeys.isEmpty()) {
            queryParams.put("widgetKeys", widgetKeys);
        }

        return RestClient.doGet("JSON", companyServiceURL, BASE_PATH, token, queryParams, null, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        switch (expectedResponse) {
            case "success":
                response.then().body("meta.message", containsString("success"));
                break;
            case "cross_account_isolation":
                // Should get 404 for cross-account access
                JsonPath isolationJp = response.jsonPath();
                assertThat("Meta object should not be null", isolationJp.get("meta"), notNullValue());
                assertThat("Meta status should be 404", isolationJp.get("meta.status"), equalTo(404));

                // Verify that Account B cannot access Account A's company widget count
                assertThat("Data should be null", isolationJp.get("data"), nullValue());
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

    @DataProvider(name = "crossAccountCompanyV2WidgetCountTestData")
    public static Object[][] crossAccountCompanyV2WidgetCountTestData() {
        return new Object[][]{
                // ===== SCENARIO 1: CROSS-ACCOUNT SECURITY =====
                // Account B attempts to access Account A's data (should fail)
                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A", "AccountB", "valid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's company widget count"},
                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A_WITH_WIDGET_KEYS", "AccountB", "valid", "GET_WIDGET_COUNT_WITH_WIDGET_KEYS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's company widget count with widgetKeys"},

                // ===== SCENARIO 2: INVALID TOKEN OPERATIONS =====
                // Account B performs operations with invalid token (should fail)
                {"SCENARIO_3_INVALID_TOKEN_B", "AccountB", "invalid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Account B should be denied access with invalid token"},

                // ===== SCENARIO 3: EDGE CASES =====
                // Account C (non-existent) attempts operations
                {"SCENARIO_4_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Non-existent account should return 401"},

                // Account B with expired token
                {"SCENARIO_4_EXPIRED_TOKEN", "AccountB", "expired", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Expired token should return 401"},

                // Account B with malformed token
                {"SCENARIO_4_MALFORMED_TOKEN", "AccountB", "malformed", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Malformed token should return 401"},

                // ===== SCENARIO 4: BOUNDARY TESTING =====
                // Account B with empty token
                {"SCENARIO_5_EMPTY_TOKEN", "AccountB", "empty", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Empty token should return 401"},

                // Account B with null token
                {"SCENARIO_5_NULL_TOKEN", "AccountB", "null", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Null token should return 401"},
        };
    }

    private void createTestDataAccountA() {
        try {
            Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
            assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
            JsonPath companyJp = companyResponse.jsonPath();
            companySlugAccountA = companyJp.get("slug");
            assertThat("Company slug should not be null", companySlugAccountA, notNullValue());

            // Get company ID from albatross API using slug (response structure: data.company.id)
            Response companyDetailsResponse = albatrossFunctions.getCompanyResponse(albatrossURL, accountA_Token, companySlugAccountA);
            assertThat("Failed to get company details from albatross API", companyDetailsResponse.getStatusCode(), equalTo(200));
            JsonPath companyDetailsJp = companyDetailsResponse.jsonPath();
            companyIdAccountA = companyDetailsJp.get("data.company.id");
            assertThat("Company ID should not be null", companyIdAccountA, notNullValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test data: " + e.getMessage(), e);
        }
    }

}

