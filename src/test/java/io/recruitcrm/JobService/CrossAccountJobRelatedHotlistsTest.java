package io.recruitcrm.JobService;

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
public class CrossAccountJobRelatedHotlistsTest extends TestBase {

    String companySlugAccountA = "";
    String contactSlugAccountA = "";
    private int recordIdAccountA = 0;
    private boolean testDataCreated = false;
    private final AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
    private final commanFunction function = new commanFunction();

    @Owner("Harika")
    @Test(dataProvider = "crossAccountJobRelatedHotlistsTestData", groups = {"job_service", "nightly-build"})
    public void crossAccountJobRelatedHotlistsOperations_Test(String testScenario, String accountType, String tokenType,
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
                    ownAccountSearchRequestBody.put("entityName", "jobs");
                    ownAccountSearchRequestBody.put("recordId", recordIdAccountA);
                    ownAccountSearchRequestBody.put("searchTerm", "");
                    ownAccountSearchRequestBody.put("sortOrder", JSONObject.NULL);

                    response = RestClient.doPost1("JSON", jobServiceURL, "hotlists/related-hotlists/search/get",
                            token, queryParams, null, true, ownAccountSearchRequestBody.toString());
                    break;

                case "POST_HOTLISTS_SEARCH_CROSS_ACCOUNT":
                    createTestDataAccountA();
                    Map<String, String> crossAccountQueryParams = new HashMap<>();
                    crossAccountQueryParams.put("size", "25");
                    crossAccountQueryParams.put("page", "1");

                    JSONObject crossAccountSearchRequestBody = new JSONObject();
                    crossAccountSearchRequestBody.put("entityName", "jobs");
                    crossAccountSearchRequestBody.put("recordId", recordIdAccountA);

                    response = RestClient.doPost1("JSON", jobServiceURL, "hotlists/related-hotlists/search/get",
                            token, crossAccountQueryParams, null, true, crossAccountSearchRequestBody.toString());
                    break;

                default:
                    throw new RuntimeException("Unknown operation: " + operation);
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            if (expectedResponse.equals("Exception")) {
                return;
            } else {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        int actualStatusCode = response.getStatusCode();
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
                }
                break;

            case "cross_account_isolation":
                if (operation.contains("SEARCH")) {
                    if (actualStatusCode == 404) {
                        assertThat("Meta status should be 404", jp.get("meta.status"), equalTo(404));
                        assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
                        assertThat("ResponseType code should be 101", jp.get("meta.responseType.code"), equalTo(101));
                        assertThat("Error message should contain expected pattern", jp.get("errors[0].message"), containsString("Entity jobs with ID"));
                        assertThat("Error message should contain 'not found'", jp.get("errors[0].message"), containsString("not found"));
                        assertThat("ErrorType context should match", jp.get("errors[0].errorType.context"), equalTo("Generic Error"));
                        assertThat("ErrorType code should be 202", jp.get("errors[0].errorType.code"), equalTo(202));
                    } else if (actualStatusCode == 200) {
                        if (jp.get("data") != null) {
                            assertThat("Cross-account search should return empty results",
                                    (Integer) jp.get("data.size()"), equalTo(0));
                        }
                    }
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
                assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Error while processing request"));
                assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(101));
                break;

            default:
                assertThat("Expected status code " + expectedStatus + " but got " + actualStatusCode,
                        actualStatusCode, equalTo(expectedStatus));
        }
    }

    private void createTestDataAccountA() {
        if (testDataCreated && recordIdAccountA > 0) {
            return;
        }

        JsonPath companyJson = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey).jsonPath();
        companySlugAccountA = companyJson.get("slug");

        JsonPath contactJson = function.createNewContact_POST(baseURL, accountA_apiKey, companySlugAccountA).jsonPath();
        contactSlugAccountA = contactJson.get("slug");

        Response jobResponse = function.createNewJob(baseURL, accountA_apiKey, companySlugAccountA, contactSlugAccountA);
        assertThat("Failed to create test job", jobResponse.getStatusCode(), equalTo(200));
        JsonPath jobJp = jobResponse.jsonPath();
        String jobSlugAccountA = jobJp.get("slug");

        Response jobGetResponse = allCrudFunctions.getJobResponse(albatrossURL, accountA_Token, jobSlugAccountA);
        assertThat("Failed to get test job", jobGetResponse.getStatusCode(), equalTo(200));
        JsonPath jp = jobGetResponse.jsonPath();
        Object idObj = jp.get("data.job.id");
        recordIdAccountA = idObj instanceof Number ? ((Number) idObj).intValue() : Integer.parseInt(String.valueOf(idObj));

        assertThat("Job slug should not be null", jobSlugAccountA, notNullValue());
        assertThat("Record ID should not be null", recordIdAccountA, notNullValue());

        Response hotlistResponse = function.createNewHotlist(baseURL, accountA_apiKey, "job");
        assertThat("Failed to create test hotlist", hotlistResponse.getStatusCode(), equalTo(200));

        JsonPath hotlistJp = hotlistResponse.jsonPath();
        int hotlistIdAccountA = hotlistJp.getInt("id");

        assertThat("Hotlist ID should not be null", hotlistIdAccountA, notNullValue());

        HotlistRelated hotlistRelated = new HotlistRelated();
        hotlistRelated.setRelated(jobSlugAccountA);

        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistIdAccountA));
        String basePath = "hotlists/{hotlist}/add-record";

        Response addResponse = RestClient.doPost1("JSON", baseURL, basePath, accountA_apiKey, null, pathParameters, true, hotlistRelated);
        assertThat("Failed to add job to hotlist", addResponse.getStatusCode(), equalTo(200));

        testDataCreated = true;
    }

    @DataProvider(name = "crossAccountJobRelatedHotlistsTestData")
    public static Object[][] crossAccountJobRelatedHotlistsTestData() {
        return new Object[][]{
                {"SCENARIO_2_SEARCH_CROSS_ACCOUNT", "AccountB", "valid", "POST_HOTLISTS_SEARCH_CROSS_ACCOUNT",
                        "404", "cross_account_isolation", "Account B should not access Account A's job hotlist data"},

                {"SCENARIO_4_EXPIRED_TOKEN", "AccountA", "expired", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Expired token should return 401"},

                {"SCENARIO_5_MALFORMED_TOKEN", "AccountA", "malformed", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Malformed token should return 401"},

                {"SCENARIO_7_NULL_TOKEN", "AccountA", "null", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Null token should return 401"},

                {"SCENARIO_8_NONEXISTENT_ACCOUNT", "AccountC", "valid", "POST_HOTLISTS_SEARCH_OWN_ACCOUNT",
                        "401", "Unauthorised access", "Non-existent account should return 401"}
        };
    }
}