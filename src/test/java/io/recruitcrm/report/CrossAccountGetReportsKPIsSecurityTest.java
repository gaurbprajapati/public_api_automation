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

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountGetReportsKPIsSecurityTest extends TestBase {

    

    @Owner("Ajendra Singh")
    @Test(dataProvider = "crossAccountGetReportsKPIsTestData", groups = "nightly-build")
    public void crossAccountGetReportsKPIsOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("report_type", "1");
        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_REPORTS_KPI":
                    response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
                    break;
                case "GET_REPORTS_KPI_EMPTY":
                    response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, null, null, true);
                    break;
                case "GET_REPORTS_KPI_TYPE_2":
                    queryParameters.put("report_type", "2");
                    response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
                    break;
                case "GET_REPORTS_KPI_TYPE_3":
                    queryParameters.put("report_type", "3");
                    response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
                    break;
                default:
                    Assert.fail("Unsupported operation: " + operation);
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);

            switch (expectedResponse) {
                case "success":
                    response.then().body(Matchers.notNullValue());
                    break;
                case "bad_request":
                case "validation_error":
                    try {
                        response.then().body(Matchers.notNullValue());
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
                        response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
                    } catch (Exception e) {
                    }
                    break;
                case "token_expired":
                    try {
                        response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
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

    @DataProvider(name = "crossAccountGetReportsKPIsTestData")
    public static Object[][] crossAccountGetReportsKPIsTestData() {
        return new Object[][] {
                {"SCENARIO_1_GET_KPI", "AccountA", "valid", "GET_REPORTS_KPI", "200", "success", "Account A should be able to get reports KPI"},
                {"SCENARIO_1_GET_KPI_B", "AccountB", "valid", "GET_REPORTS_KPI", "200", "success", "Account B should be able to get reports KPI independently"},
                {"SCENARIO_1_GET_KPI_EMPTY", "AccountB", "valid", "GET_REPORTS_KPI_EMPTY", "422", "validation_error", "Account B should get validation error for missing report_type parameter"},
                {"SCENARIO_1_GET_KPI_TYPE_2", "AccountB", "valid", "GET_REPORTS_KPI_TYPE_2", "200", "success", "Account B should be able to get reports KPI with type 2"},
                {"SCENARIO_2_GET_KPI_INVALID", "AccountB", "invalid", "GET_REPORTS_KPI", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_KPI_EMPTY_INVALID", "AccountB", "invalid", "GET_REPORTS_KPI_EMPTY", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_KPI_TYPE_2_INVALID", "AccountB", "invalid", "GET_REPORTS_KPI_TYPE_2", "401", "unauthorized", "Account B should be denied access with invalid token"},
                {"SCENARIO_2_GET_KPI_A_INVALID", "AccountA", "invalid", "GET_REPORTS_KPI", "401", "unauthorized", "Account A should be denied access with invalid token"},
                {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "GET_REPORTS_KPI", "401", "token_expired", "Expired token should return 401"},
                {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_REPORTS_KPI_EMPTY", "401", "Unauthorized", "Malformed token should return 401"},
                {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "GET_REPORTS_KPI", "401", "unauthorized", "Empty token should return 401"},
                {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "GET_REPORTS_KPI_TYPE_2", "401", "unauthorized", "Null token should return 401"}
        };
    }


}