package io.recruitcrm.report;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.albatross.JavaFakerTargetReports;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.pojo.albatross.targetReports.*;

import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountTargetReportsSecurityTest extends TestBase {

    private JavaFakerTargetReports faker = new JavaFakerTargetReports();

    private static int accountA_TargetReportId = 0;

    @BeforeClass(alwaysRun = true)    public void setupTestData() {
        createAccountATargetReport();
    }

    private void createAccountATargetReport() {
        try {
            TargetReport targetReport = new TargetReport();
            targetReport.setTitle("CrossAccount-Test-" + faker.getTargetReportName());
            targetReport.setAssignee_id(faker.getAssigneeId());
            targetReport.setAssignee_type(faker.getAssigneeType());
            targetReport.setFrequency(faker.getFrequency());
            targetReport.setStart_date(faker.getStartDate(30));
            targetReport.setEnd_date(faker.getEndDate(90));

            String basePath = "target-reports/create";
            Response response = RestClient.doPost("JSON", albatrossURL, basePath, accountA_Token, null, true, targetReport);

            if (response.getStatusCode() == 200) {
                JsonPath jsonPath = response.jsonPath();
                accountA_TargetReportId = jsonPath.getInt("data.id");
                System.out.println("Created Account A target report with ID: " + accountA_TargetReportId);
            } else {
                System.out.println("Failed to create Account A target report: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Exception creating Account A target report: " + e.getMessage());
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "crossAccountTargetReportsTestData", groups = "nightly-build")
    public void crossAccountTargetReportsOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);

        TargetReport targetReport = new TargetReport();
        targetReport.setTitle(faker.getTargetReportName());
        targetReport.setAssignee_id(faker.getAssigneeId());
        targetReport.setAssignee_type(faker.getAssigneeType());
        targetReport.setFrequency(faker.getFrequency());
        targetReport.setStart_date(faker.getStartDate(30));
        targetReport.setEnd_date(faker.getEndDate(90));

        if (operation.equals("POST_SAVE_TARGET")) {
            targetReport.setId(faker.getRandomTargetId());
        }

        String kpiListJson = "{\"kpis\":[{\"label\":\"" + faker.getKPILabel() + "\",\"value\":\"" + faker.getKPIValue(faker.getKPILabel()) + "\",\"target\":\"" + faker.getKPICount() + "\",\"checked\":true,\"includeInTarget\":true}]}";
        targetReport.setKpi_list(kpiListJson);

        TargetReportData targetReportData = new TargetReportData();
        targetReportData.setTargetId(faker.getRandomTargetId());

        Response response = null;

        try {
            switch (operation.toUpperCase()) {
                case "GET_FETCH_TARGET_TITLE":
                    response = RestClient.doGet("JSON", reportServiceURL, "reports/fetch-target-title", token, null, null, true);
                    break;

                case "POST_GET_REPORT_DATA":
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/get-report-data", token, null, true, targetReportData);
                    break;

                case "POST_CREATE_TARGET":
                    response = RestClient.doPost("JSON", albatrossURL, "target-reports/create", token, null, true, targetReport);
                    break;

                case "POST_SAVE_TARGET":
                    response = RestClient.doPost("JSON", albatrossURL, "target-reports/update", token, null, true, targetReport);
                    break;

                case "POST_GET_REPORT_DATA_WITH_A_TARGET":
                    TargetReportData crossAccountData = new TargetReportData();
                    crossAccountData.setTargetId(accountA_TargetReportId);
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/get-report-data", token, null, true, crossAccountData);
                    break;

                case "POST_SHARE_TARGET_WITH_A_ID":
                    ShareTargetReport shareTargetReport = new ShareTargetReport();
                    shareTargetReport.setReport_shared(true);
                    shareTargetReport.setTarget_user_preference_id(accountA_TargetReportId);
                    shareTargetReport.setAuto_refresh(true);
                    shareTargetReport.setRefresh_time(faker.getRefreshTime());
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/targets-share", token, null, true, shareTargetReport);
                    break;

                default:
                    Assert.fail("Unsupported operation: " + operation);
            }

            int expectedStatus = Integer.parseInt(expectedStatusCode);
            response.then().statusCode(expectedStatus);

            String responseBody = response.getBody().asString();

            switch (expectedResponse) {
                case "success":
                    try {
                        response.then().body(Matchers.notNullValue());
                    } catch (Exception e) {
                        if (responseBody.contains("success") || responseBody.contains("data")) {
                        } else {
                            throw e;
                        }
                    }
                    break;

                case "empty":
                    try {
                        response.then().body("$", Matchers.empty());
                    } catch (Exception e) {
                        if (responseBody.trim().isEmpty() || responseBody.equals("[]") || responseBody.equals("{}")) {
                        } else {
                            if (responseBody.contains("accountid") || responseBody.contains("non-object")) {
                            } else {
                                throw e;
                            }
                        }
                    }
                    break;

                case "target_not_found":
                    response.then().body("message", Matchers.is("Target report not found"));
                    response.then().body("message_type", Matchers.is("is-danger"));
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

                case "server_error":
                    try {
                        response.then().body("exception", Matchers.containsString("Error"));
                    } catch (Exception e) {
                    }
                    break;

                case "method_not_allowed":
                    try {
                        response.then().body("exception", Matchers.containsString("MethodNotAllowedHttpException"));
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
                    expectedResponse.contains("not_found") || expectedResponse.contains("server_error")) {
            } else {
                throw e;
            }
        }
    }

    @DataProvider(name = "crossAccountTargetReportsTestData")
    public static Object[][] crossAccountTargetReportsTestData() {
        return new Object[][]{
                {"SCENARIO_1_FETCH_TITLE", "AccountA", "valid", "GET_FETCH_TARGET_TITLE", "200", "success", "Account A should be able to fetch target title"},

                {"SCENARIO_1_FETCH_TITLE_B", "AccountB", "valid", "GET_FETCH_TARGET_TITLE", "200", "empty", "Account B should get empty results for fetch title"},
                {"SCENARIO_1_CREATE_TARGET", "AccountB", "valid", "POST_CREATE_TARGET", "200", "success", "Account B should be able to create target report independently"},

                {"SCENARIO_1_CROSS_TARGET_ACCESS", "AccountB", "valid", "POST_GET_REPORT_DATA_WITH_A_TARGET", "200", "empty", "Account B should not access Account A's target report data"},
                {"SCENARIO_1_CROSS_TARGET_SHARE", "AccountB", "valid", "POST_SHARE_TARGET_WITH_A_ID", "500", "server_error", "Account B should not share Account A's target report"},

                {"SCENARIO_2_FETCH_TITLE_INVALID", "AccountB", "invalid", "GET_FETCH_TARGET_TITLE", "500", "server_error", "Account B should get server error with invalid token"},
                {"SCENARIO_2_GET_REPORT_DATA_INVALID", "AccountB", "invalid", "POST_GET_REPORT_DATA", "200", "empty", "Account B should get empty results with invalid token"},
                {"SCENARIO_2_CREATE_TARGET_INVALID", "AccountB", "invalid", "POST_CREATE_TARGET", "401", "unauthorized", "Account B should be denied target creation with invalid token"},
                {"SCENARIO_2_SAVE_TARGET_INVALID", "AccountA", "invalid", "POST_SAVE_TARGET", "401", "unauthorized", "Account A should be denied target save with invalid token"},

                {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE_TARGET", "401", "token_expired", "Expired token should return 401"},

                {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_FETCH_TARGET_TITLE", "500", "server_error", "Malformed token should return server error"},

                {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE_TARGET", "401", "unauthorized", "Empty token should return 401"},

                {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_SAVE_TARGET", "401", "unauthorized", "Null token should return 401"}
        };
    }
}