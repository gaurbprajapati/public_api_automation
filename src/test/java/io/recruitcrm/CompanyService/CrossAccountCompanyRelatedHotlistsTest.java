package io.recruitcrm.CompanyService;

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
public class CrossAccountCompanyRelatedHotlistsTest extends TestBase {

    private String companySlugAccountA = "";
    private int hotlistIdAccountA;
    private int recordIdAccountA = 0;
    private boolean testDataCreated = false;
    private AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private commanFunction function = new commanFunction();

    @Owner("Harika")
    @Test(dataProvider = "crossAccountCompanyRelatedHotlistsTestData", groups = "nightly-build")
    public void crossAccountCompanyRelatedHotlistsOperations_Test(String testScenario, String accountType, String tokenType,
                                                                   String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                // ===== SEARCH OPERATIONS ====
                case "POST_HOTLISTS_SEARCH_OWN_ACCOUNT":
                    createTestDataAccountA();
                    Map<String, String> queryParams = new HashMap<>();
                    queryParams.put("size", "25");
                    queryParams.put("page", "1");

                    JSONObject ownAccountSearchRequestBody = new JSONObject();
                    ownAccountSearchRequestBody.put("entityName", "companies");
                    ownAccountSearchRequestBody.put("recordId", recordIdAccountA);
                    ownAccountSearchRequestBody.put("searchTerm", "");
                    ownAccountSearchRequestBody.put("sortOrder", JSONObject.NULL);

                    response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
                            token, queryParams, null, true, ownAccountSearchRequestBody.toString());
                    break;

                case "POST_HOTLISTS_SEARCH_CROSS_ACCOUNT":
                    createTestDataAccountA();
                    Map<String, String> crossAccountQueryParams = new HashMap<>();
                    crossAccountQueryParams.put("size", "25");
                    crossAccountQueryParams.put("page", "1");

                    JSONObject crossAccountSearchRequestBody = new JSONObject();
                    crossAccountSearchRequestBody.put("entityName", "companies");
                    crossAccountSearchRequestBody.put("recordId", recordIdAccountA);

                    response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related-hotlists/search/get",
                            token, crossAccountQueryParams, null, true, crossAccountSearchRequestBody.toString());
                    break;

                // ===== DELETE OPERATIONS =====
                case "POST_HOTLISTS_DELETE_OWN_ACCOUNT":
                    createTestDataAccountA();
                    JSONObject ownAccountDeleteRequestBody = new JSONObject();
                    ownAccountDeleteRequestBody.put("recordId", recordIdAccountA);
                    ownAccountDeleteRequestBody.put("entityName", "companies");
                    ownAccountDeleteRequestBody.put("hotlistIds", new int[]{hotlistIdAccountA});

                    response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                            token, null, null, true, ownAccountDeleteRequestBody.toString());
                    break;

                case "POST_HOTLISTS_DELETE_CROSS_ACCOUNT":
                    createTestDataAccountA();
                    JSONObject crossAccountDeleteRequestBody = new JSONObject();
                    crossAccountDeleteRequestBody.put("recordId", recordIdAccountA);
                    crossAccountDeleteRequestBody.put("entityName", "companies");
                    crossAccountDeleteRequestBody.put("hotlistIds", new int[]{hotlistIdAccountA});

                    response = RestClient.doPost1("JSON", companyServiceURL, "hotlists/related/delete",
                            token, null, null, true, crossAccountDeleteRequestBody.toString());
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            if (expectedResponse.equals("Exception")) {
                // Expected exception, test passes
                return;
            } else {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        int actualStatusCode = response.getStatusCode();
        String actualStatusCodeStr = String.valueOf(actualStatusCode);
        int expectedStatus = Integer.parseInt(expectedStatusCode);

        assertThat("Expected status code " + expectedStatus + " but got " + actualStatusCode,
                actualStatusCode, equalTo(expectedStatus));

        JsonPath jp = response.jsonPath();

        switch (expectedResponse) {
            case "success":
                if (operation.contains("SEARCH")) {
                    assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
                    assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
                    assertThat("Message should match expected", jp.get("meta.message"), equalTo("Related hotlists fetched successfully."));
                    assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
                    assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
                    assertThat("Data should not be null", jp.get("data"), notNullValue());
                } else if (operation.contains("DELETE")) {
                    assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
                    assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
                    assertThat("Message should match expected", jp.get("meta.message"), equalTo("Related hotlists removed successfully"));
                    assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Request is successful"));
                    assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
                    assertThat("Data should be null", jp.get("data"), nullValue());
                }
                break;

            case "cross_account_isolation":
                if (operation.contains("SEARCH")) {
                    // For search operations - should get 404 or empty results
                    if (actualStatusCode == 404) {
                        assertThat("Meta status should be 404", jp.get("meta.status"), equalTo(404));
                        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
                        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
                        assertThat("Error message should contain expected pattern", jp.get("errors[0].message"), containsString("Entity companies with ID"));
                        assertThat("Error message should contain 'not found'", jp.get("errors[0].message"), containsString("not found"));
                        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
                        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
                    } else if (actualStatusCode == 200) {
                        // If 200, should return empty results
                        if (jp.get("data") != null) {
                            assertThat("Cross-account search should return empty results",
                                    (Integer) jp.get("data.size()"), equalTo(0));
                        }
                    }
                } else if (operation.contains("DELETE")) {
                    // For delete operations - should get 404 or unauthorized
                    assertThat("Expected status code " + expectedStatus + " but got " + actualStatusCode,
                            actualStatusCode, equalTo(expectedStatus));
                }
                break;

            case "Unauthorized":
                assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
                assertThat("ResponseType should not be null", jp.get("meta.responseType"), notNullValue());
                assertThat("ResponseType context should match", jp.get("meta.responseType.context"), 
                        equalTo("Error while processing request"));
                assertThat("Data should be null", jp.get("data"), nullValue());
                assertThat("Error message should match", jp.get("errors[0].message"), equalTo("Unauthorized"));
                assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), 
                        equalTo("Generic Error"));
                assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
                break;

            case "Unauthorised access":
                assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
                assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
                assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
                assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
                break;

            default:
                assertThat("Expected status code " + expectedStatus + " but got " + actualStatusCode,
                        actualStatusCode, equalTo(expectedStatus));
        }
    }

    private void createTestDataAccountA() {
        // Avoid recreating test data if already created
        if (testDataCreated && recordIdAccountA > 0) {
            return;
        }

        // Step 1: Create a company using Account A
        JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey).jsonPath();
        companySlugAccountA = json.get("slug");

        Response companyResponse = allCrudFunctions.getCompanyResponse(albatrossURL, accountA_Token, companySlugAccountA);
        assertThat("Failed to create test company", companyResponse.getStatusCode(), equalTo(200));
        JsonPath jp = companyResponse.jsonPath();
        recordIdAccountA = jp.get("data.company.id");

        assertThat("Company slug should not be null", companySlugAccountA, notNullValue());
        assertThat("Record ID should not be null", recordIdAccountA, notNullValue());

        // Step 2: Create a hotlist using Account A
        Response hotlistResponse = function.createNewHotlist(baseURL, accountA_apiKey, "company");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));

        JsonPath hotlistJp = hotlistResponse.jsonPath();
        hotlistIdAccountA = hotlistJp.getInt("id");

        assertThat("Hotlist ID should not be null", hotlistIdAccountA, notNullValue());

        // Step 3: Add company to hotlist using Account A
        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(companySlugAccountA);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistIdAccountA));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, accountA_apiKey, null, pathParameters, true, hotlistRelated);
        assertThat("Failed to add company to hotlist", addResponse.getStatusCode(), equalTo(200));

        testDataCreated = true;
    }

    @DataProvider(name = "crossAccountCompanyRelatedHotlistsTestData", parallel = true)
    public static Object[][] crossAccountCompanyRelatedHotlistsTestData() {
        return new Object[][]{
                // ===== SCENARIO : CROSS-ACCOUNT ISOLATION =====
                // Account B tries to access Account A's data
                {"SCENARIO_2_SEARCH_CROSS_ACCOUNT", "AccountB", "valid", "POST_HOTLISTS_SEARCH_CROSS_ACCOUNT", 
                    "404", "cross_account_isolation", "Account B should not access Account A's company hotlist data"},

                {"SCENARIO_2_DELETE_CROSS_ACCOUNT", "AccountB", "valid", "POST_HOTLISTS_DELETE_CROSS_ACCOUNT", 
                    "404", "cross_account_isolation", "Account B should not delete Account A's company hotlist"},

                // ===== SCENARIO : EXPIRED TOKEN =====
                {"SCENARIO_4_EXPIRED_TOKEN", "AccountA", "expired", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT", 
                    "401", "Unauthorised access", "Expired token should return 401"},

                {"SCENARIO_4_EXPIRED_TOKEN", "AccountA", "expired", "POST_HOTLISTS_DELETE_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Expired token should return 401"},

                // ===== SCENARIO : MALFORMED TOKEN =====
                {"SCENARIO_5_MALFORMED_TOKEN", "AccountA", "malformed", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT", 
                    "401", "Unauthorised access", "Malformed token should return 401"},

                {"SCENARIO_5_MALFORMED_TOKEN", "AccountA", "malformed", "POST_HOTLISTS_DELETE_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Malformed token should return 401"},

                // ===== SCENARIO : NULL TOKEN =====
                {"SCENARIO_7_NULL_TOKEN", "AccountA", "null", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT", 
                    "401", "Unauthorised access", "Null token should return 401"},

                {"SCENARIO_7_NULL_TOKEN", "AccountA", "null", "POST_HOTLISTS_DELETE_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Null token should return 401"},

                // ===== SCENARIO : NON-EXISTENT ACCOUNT =====
                {"SCENARIO_8_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT", 
                    "401", "Unauthorised access", "Non-existent account should return 401"},

                {"SCENARIO_8_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_HOTLISTS_DELETE_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Non-existent account should return 401"},
        };
    }
}

