package io.recruitcrm.ContactService;

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
public class CrossAccountContactV2WidgetCountTest extends TestBase {

    private static final String BASE_PATH = "widget-count";

    private String contactIdAccountA;
    private String contactSlugAccountA;
    private final AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
    private final commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        createTestDataAccountA();
    }

    @Owner("Suhel Bhadane")
    @Test(dataProvider = "crossAccountContactV2WidgetCountTestData", groups = {"contact_service", "nightly-build"})
    public void crossAccountContactV2WidgetCountOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;
        switch (operation.toUpperCase()) {
            case "GET_WIDGET_COUNT_CROSS_ACCOUNT":
                response = getWidgetCount(contactIdAccountA, contactSlugAccountA, token, null);
                break;
            case "GET_WIDGET_COUNT_WITH_WIDGET_KEYS_CROSS_ACCOUNT":
                response = getWidgetCount(contactIdAccountA, contactSlugAccountA, token, "jobs,hotlists");
                break;

            default:
                throw new RuntimeException("Unknown operation: " + operation);
        }
        int actualStatus = response.getStatusCode();
        int expectedStatus = Integer.parseInt(expectedStatusCode);
        String caseContext = "[" + testScenario + "] " + description + " - ";
        assertThat(caseContext + "Response status code should match expected (expected " + expectedStatusCode + ", got " + actualStatus + ")", actualStatus, is(equalTo(expectedStatus)));

        verifyResponse(response, expectedStatusCode, expectedResponse, operation, caseContext);
    }

    private Response getWidgetCount(String contactId, String contactSlug, String token, String widgetKeys) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("entityType", "contacts");
        queryParams.put("recordId", contactId);
        queryParams.put("recordSlug", String.valueOf(contactSlug));
        if (widgetKeys != null && !widgetKeys.isEmpty()) {
            queryParams.put("widgetKeys", widgetKeys);
        }

        return RestClient.doGet("JSON", contactServiceURL, BASE_PATH, token, queryParams, null, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation, String caseContext) {
        switch (expectedResponse) {
            case "success":
                response.then().body("meta.message", containsString("success"));
                break;
            case "cross_account_isolation":
                JsonPath isolationJp = response.jsonPath();
                assertThat(caseContext + "Meta object should not be null", isolationJp.get("meta"), notNullValue());
                assertThat(caseContext + "Meta status should be 404", isolationJp.get("meta.status"), equalTo(404));
                assertThat(caseContext + "Data should be null", isolationJp.get("data"), nullValue());
                break;
            case "Unauthorised access":
                // API returns 500 for invalid/expired/malformed/null token; no body assertion
                break;
            case "not found":
                response.then().body("errors[0].message", containsString("not found"));
                break;
            case "Bad Request":
                response.then().body("error", is("Bad Request"));
                break;
        }
    }

    @DataProvider(name = "crossAccountContactV2WidgetCountTestData")
    public static Object[][] crossAccountContactV2WidgetCountTestData() {
        return new Object[][]{
                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A", "AccountB", "valid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's contact widget count"},
                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A_WITH_WIDGET_KEYS", "AccountB", "valid", "GET_WIDGET_COUNT_WITH_WIDGET_KEYS_CROSS_ACCOUNT", "404", "cross_account_isolation", "Account B should not access Account A's contact widget count with widgetKeys"},

                {"SCENARIO_3_INVALID_TOKEN_B", "AccountB", "invalid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "500", "Unauthorised access", "Account B should be denied access with invalid token"},

                {"SCENARIO_4_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "500", "Unauthorised access", "Non-existent account should return 500"},
                {"SCENARIO_4_EXPIRED_TOKEN", "AccountB", "expired", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "500", "Unauthorised access", "Expired token should return 500"},
                {"SCENARIO_4_MALFORMED_TOKEN", "AccountB", "malformed", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "500", "Unauthorised access", "Malformed token should return 500"},

                {"SCENARIO_5_EMPTY_TOKEN", "AccountB", "empty", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "401", "Unauthorised access", "Empty token should return 401"},
                {"SCENARIO_5_NULL_TOKEN", "AccountB", "null", "GET_WIDGET_COUNT_CROSS_ACCOUNT", "500", "Unauthorised access", "Null token should return 500"},
        };
    }

    private void createTestDataAccountA() {
        try {
            Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey);
            assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
            JsonPath companyJp = companyResponse.jsonPath();
            String companySlugAccountA = companyJp.get("slug");
            assertThat("Company slug should not be null", companySlugAccountA, notNullValue());

            Response contactResponse = function.createNewContact_POST(baseURL, accountA_apiKey, companySlugAccountA);
            assertThat("Failed to create test contact", contactResponse.getStatusCode(), equalTo(200));
            JsonPath contactJp = contactResponse.jsonPath();
            contactSlugAccountA = contactJp.get("slug");
            assertThat("Contact slug should not be null", contactSlugAccountA, notNullValue());

            Response contactDetailsResponse = albatrossFunctions.getContactResponse(albatrossURL, accountA_Token, contactSlugAccountA);
            assertThat("Failed to get contact details from albatross API", contactDetailsResponse.getStatusCode(), equalTo(200));
            JsonPath contactDetailsJp = contactDetailsResponse.jsonPath();
            Object contactIdObj = contactDetailsJp.get("data.contact.id");
            contactIdAccountA = contactIdObj != null ? contactIdObj.toString() : null;
            assertThat("Contact ID should not be null", contactIdAccountA, notNullValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test data: " + e.getMessage(), e);
        }
    }
}
