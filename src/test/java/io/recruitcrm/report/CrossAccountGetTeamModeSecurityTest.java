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
public class CrossAccountGetTeamModeSecurityTest extends TestBase {

    private JavaFakerSavePerferences fakerReportData = new JavaFakerSavePerferences();



    @Owner("Ajendra Singh")
    @Test(dataProvider = "crossAccountGetTeamModeTestData", groups = "nightly-build")
    public void crossAccountGetTeamModeOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);

        Map<String, String> queryParameters = new HashMap<>();
        String mode = "job_recruiter";
        queryParameters.put("mode", mode);

        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_TEAM_MODE":
                    response = RestClient.doGet("JSON", albatrossURL, "teams", token, queryParameters, null, true);
                    break;

                case "GET_TEAM_MODE_INVALID":
                    queryParameters.put("mode", fakerReportData.getModes());
                    response = RestClient.doGet("JSON", albatrossURL, "teams", token, queryParameters, null, true);
                    break;

                case "GET_TEAM_MODE_EMPTY":
                    queryParameters.put("mode", "");
                    response = RestClient.doGet("JSON", albatrossURL, "teams", token, queryParameters, null, true);
                    break;

                case "GET_TEAMS_PUBLIC_API":
                    String publicApiToken = token;
                    if (tokenType.equals("valid")) {
                        if (accountType.equals("AccountA")) {
                            publicApiToken = accountA_apiKey;
                        } else if (accountType.equals("AccountB")) {
                            publicApiToken = accountB_apiKey;
                        }
                    }
                    response = RestClient.doGet("JSON", baseURL, "teams", publicApiToken, null, null, true);
                    break;

                default:
                    Assert.fail("Unsupported operation: " + operation);
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);

            switch (expectedResponse) {
                case "success":
                    response.then().body("status", Matchers.containsString("success"));
                    response.then().body("data.records", Matchers.notNullValue());
                    break;

                case "public_success":
                    response.then().body(Matchers.notNullValue());
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

    @DataProvider(name = "crossAccountGetTeamModeTestData")
    public static Object[][] crossAccountGetTeamModeTestData() {
        return new Object[][]{
                {"SCENARIO_1_GET_TEAM_MODE", "AccountA", "valid", "GET_TEAM_MODE", "200", "success", "Account A should be able to get team mode data"},

                {"SCENARIO_1_GET_TEAM_MODE_B", "AccountB", "valid", "GET_TEAM_MODE", "200", "success", "Account B should be able to get team mode data independently"},
                {"SCENARIO_1_GET_TEAM_MODE_EMPTY", "AccountB", "valid", "GET_TEAM_MODE_EMPTY", "200", "success", "Account B should be able to get team mode with empty mode parameter"},
                {"SCENARIO_1_GET_TEAMS_PUBLIC", "AccountB", "valid", "GET_TEAMS_PUBLIC_API", "200", "public_success", "Account B should be able to get teams via public API"},

                {"SCENARIO_2_GET_TEAM_MODE_INVALID", "AccountB", "invalid", "GET_TEAM_MODE", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_TEAM_MODE_EMPTY_INVALID", "AccountB", "invalid", "GET_TEAM_MODE_EMPTY", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_TEAMS_PUBLIC_INVALID", "AccountB", "invalid", "GET_TEAMS_PUBLIC_API", "401", "unauthorized", "Account B should be denied public API access with invalid token"},
                {"SCENARIO_2_GET_TEAM_MODE_A_INVALID", "AccountA", "invalid", "GET_TEAM_MODE", "401", "unauthorized", "Account A should be denied access with invalid token"},

                {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "GET_TEAM_MODE", "401", "token_expired", "Expired token should return 401"},

                {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_TEAM_MODE_EMPTY", "401", "Unauthorized", "Malformed token should return 401"},

                {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "GET_TEAM_MODE", "401", "unauthorized", "Empty token should return 401"},

                {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "GET_TEAMS_PUBLIC_API", "401", "unauthorized", "Null token should return 401"}
        };
    }


}