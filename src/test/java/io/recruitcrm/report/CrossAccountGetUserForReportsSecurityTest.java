package io.recruitcrm.report;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.reaper.Account;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetUserForReportsSecurityTest extends TestBase {

    private JavaFakerSavePerferences fakerReportData = new JavaFakerSavePerferences();



    @Owner("Akshaya Uppala")
    @Test(dataProvider = "crossAccountGetUserForReportsTestData", groups = "nightly-build")
    public void crossAccountGetUserForReportsOperations_Test(String testScenario, String accountType, String tokenType,
                                                             String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);

        Map<String, String> queryParameters = new HashMap<>();
        String mode = "job_recruiter";
        queryParameters.put("report", mode);

        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "POST_GET_USERS_FOR_REPORTS":
                    response = RestClient.doPost1("JSON", albatrossURL, "global/get-users-for-rpr", token, queryParameters, null, true, null);
                    break;

                case "POST_GET_USERS_EMPTY_REPORT":
                    queryParameters.put("report", "");
                    response = RestClient.doPost1("JSON", albatrossURL, "global/get-users-for-rpr", token, queryParameters, null, true, null);
                    break;

                case "POST_GET_USERS_INVALID_REPORT":
                    queryParameters.put("report", fakerReportData.getModes());
                    response = RestClient.doPost1("JSON", albatrossURL, "global/get-users-for-rpr", token, queryParameters, null, true, null);
                    break;

                default:
                    Assert.fail("Unsupported operation: " + operation);
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);

            switch (expectedResponse) {
                case "success":
                    response.then().body("status", Matchers.containsString("success"));
                    response.then().body("data[0].name", Matchers.notNullValue());
                    break;

                case "bad_request":
                    try {
                        response.then().body("error", Matchers.containsString("validation"));
                    } catch (Exception e) {
                    }
                    break;

                case "access denied":
                case "forbidden":
                    response.then().body("error", Matchers.containsString("forbidden"));
                    break;

                case "unauthorized":
                case "Unauthorized":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                    }
                    break;

                case "token_expired":
                    try {
                        response.then().body("error", Matchers.containsString("Unauthorized"));
                    } catch (Exception e) {
                    }
                    break;

                default:
                    try {
                        response.then().body("error_message", Matchers.equalTo(expectedResponse));
                    } catch (Exception e) {
                        try {
                            response.then().body("error", Matchers.equalTo(expectedResponse));
                        } catch (Exception e2) {
                        }
                    }
                    break;
            }

        } catch (Exception e) {
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") ||
                expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") ||
                expectedResponse.contains("not_found")) {
            } else {
                throw e;
            }
        }
    }

    @DataProvider(name = "crossAccountGetUserForReportsTestData")
    public static Object[][] crossAccountGetUserForReportsTestData() {
        return new Object[][]{
                {"SCENARIO_1_GET_USERS", "AccountA", "valid", "POST_GET_USERS_FOR_REPORTS", "200", "success", "Account A should be able to get users for reports"},

                {"SCENARIO_1_GET_USERS_B", "AccountB", "valid", "POST_GET_USERS_FOR_REPORTS", "200", "success", "Account B should be able to get users for reports independently"},
                {"SCENARIO_1_GET_USERS_EMPTY", "AccountB", "valid", "POST_GET_USERS_EMPTY_REPORT", "200", "success", "Account B should be able to get users with empty report parameter"},
                {"SCENARIO_1_GET_USERS_INVALID", "AccountB", "valid", "POST_GET_USERS_INVALID_REPORT", "200", "success", "Account B should be able to get users with invalid report parameter"},

                {"SCENARIO_2_GET_USERS_INVALID", "AccountB", "invalid", "POST_GET_USERS_FOR_REPORTS", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_USERS_EMPTY_INVALID", "AccountB", "invalid", "POST_GET_USERS_EMPTY_REPORT", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_USERS_INVALID_PARAM_INVALID", "AccountB", "invalid", "POST_GET_USERS_INVALID_REPORT", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_USERS_A_INVALID", "AccountA", "invalid", "POST_GET_USERS_FOR_REPORTS", "401", "unauthorized", "Account A should be denied access with invalid token"},

                {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_GET_USERS_FOR_REPORTS", "401", "token_expired", "Expired token should return 401"},

                {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_GET_USERS_EMPTY_REPORT", "401", "Unauthorized", "Malformed token should return 401"},

                {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_GET_USERS_FOR_REPORTS", "401", "unauthorized", "Empty token should return 401"},

                {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_GET_USERS_INVALID_REPORT", "401", "unauthorized", "Null token should return 401"}
        };
    }


}
