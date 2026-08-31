package io.recruitcrm.JobService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetJobDetailsPageFieldCountTest extends TestBase {

    private int accountA_AccountId;
    private int accountB_AccountId;
    private int accountA_UserId;
    private int accountB_UserId;
    commanFunction function = new commanFunction();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        setupCrossAccountTokens();
        accountA_AccountId = accountA.getAccountId();
        accountB_AccountId = accountB.getAccountId();
        setupUserIds();
    }

    private void setupUserIds() {
        Response getUsersA = function.getUsers(baseURL, accountA_apiKey);
        assertThat("Failed to get users", getUsersA.getStatusCode(), equalTo(200));
        JsonPath jp1 = getUsersA.jsonPath();
        accountA_UserId = jp1.get("[0].id");
        assertThat("Account A User ID should not be null", accountA_UserId, notNullValue());

        Response getUsersB = function.getUsers(baseURL, accountB_apiKey);
        assertThat("Failed to get users", getUsersB.getStatusCode(), equalTo(200));
        JsonPath jp2 = getUsersB.jsonPath();
        accountB_UserId = jp2.get("[0].id");
        assertThat("Account B User ID should not be null", accountB_UserId, notNullValue());
    }

    @Owner("Harika")
    @Test(dataProvider = "crossAccountJobDetailsPageFieldsCountTestData", groups = {"job_service", "nightly-build"})
    public void crossAccountJobDetailsPageFieldsCountOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_A":
                    response = getJobDetailsPageFieldsCount(accountA_AccountId, accountA_UserId, token);
                    break;

                case "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_CROSS_ACCOUNTID":
                    response = getJobDetailsPageFieldsCount(accountA_AccountId, accountB_UserId, token);
                    break;

                case "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_CROSS_USERID":
                    response = getJobDetailsPageFieldsCount(accountB_AccountId, accountA_UserId, token);
                    break;

                default:
                    assertThat("Unsupported operation: " + operation, false, is(true));
            }

            verifyResponse(response, expectedStatusCode, expectedResponse, operation);

        } catch (Exception e) {
            if (expectedResponse.equals("Exception")) {
                return;
            } else {
                throw e;
            }
        }
    }

    private Response getJobDetailsPageFieldsCount(int accountId, int userId, String token) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accountId", String.valueOf(accountId));
        queryParams.put("userId", String.valueOf(userId));
        queryParams.put("entityType", "jobs");

        return RestClient.doGet("JSON", jobServiceURL, "details-page/fields-count",
                token, queryParams, null, true);
    }

    private void verifyResponse(Response response, String expectedStatusCode, String expectedResponse, String operation) {
        int expectedStatus = Integer.parseInt(expectedStatusCode);
        assertThat("Expected status code " + expectedStatus + " but got " + response.getStatusCode(),
                response.getStatusCode(), equalTo(expectedStatus));

        JsonPath jp = response.jsonPath();

        switch (expectedResponse) {
            case "success":
                assertThat("Meta object should not be null", jp.get("meta"), notNullValue());
                assertThat("Meta status should be 200", jp.get("meta.status"), equalTo(200));
                assertThat("Message should match expected value", jp.get("meta.message"),
                        equalTo("Details page field count fetched successfully"));
                assertThat("ResponseType context should match", jp.get("meta.responseType.context"),
                        equalTo("Request is successful"));
                assertThat("ResponseType code should be 103", jp.get("meta.responseType.code"), equalTo(103));
                assertThat("Data should not be null", jp.get("data"), notNullValue());
                assertThat("MyViewCount should not be null", jp.get("data.myViewCount"), notNullValue());
                assertThat("OthersViewCount should not be null", jp.get("data.othersViewCount"), notNullValue());
                break;

            case "Unauthorised access":
                assertThat("Meta status should be 401", jp.get("meta.status"), equalTo(401));
                assertThat("Message should match expected value", jp.get("meta.message"), equalTo("Unauthorised access"));
                assertThat("ResponseType context should match", jp.get("meta.responseType.context"), equalTo("Warning"));
                assertThat("ResponseType code should be 104", jp.get("meta.responseType.code"), equalTo(104));
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
        }
    }

    @DataProvider(name = "crossAccountJobDetailsPageFieldsCountTestData", parallel = true)
    public static Object[][] crossAccountJobDetailsPageFieldsCountTestData() {
        return new Object[][]{

                {"SCENARIO_2_CROSS_ACCOUNT_ACCESS_B_TO_A", "AccountB", "valid",
                        "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_CROSS_ACCOUNTID", "401", "Unauthorized",
                        "Account B should not access Account A's job details page field count"},

                {"SCENARIO_3_CROSS_ACCOUNT_ACCESS_B_TO_A", "AccountB", "valid",
                        "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_CROSS_USERID", "401", "Unauthorized",
                        "Account B should not access Account A's job details page field count"},

                {"SCENARIO_5_NONEXISTENT_ACCOUNT", "AccountC", "valid", "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_A",
                        "401", "Unauthorised access", "Non-existent account should return 401"},

                {"SCENARIO_6_EXPIRED_TOKEN", "AccountA", "expired", "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_A",
                        "401", "Unauthorised access", "Expired token should return 401"},

                {"SCENARIO_7_MALFORMED_TOKEN", "AccountA", "malformed", "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_A",
                        "401", "Unauthorised access", "Malformed token should return 401"},

                {"SCENARIO_9_NULL_TOKEN", "AccountA", "null", "GET_JOB_DETAILS_PAGE_FIELDS_COUNT_A",
                        "401", "Unauthorised access", "Null token should return 401"},
        };
    }
}
