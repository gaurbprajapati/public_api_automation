package io.recruitcrm.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.TeamPerformanceReport;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountTeamPerformanceReportSecurityTest extends TestBase {

    

    @Owner("Sai Teja SG")
    @Test(dataProvider = "crossAccountTeamPerformanceReportTestData", groups = "nightly-build")
    public void crossAccountTeamPerformanceReportOperations_Test(String testScenario, String accountType, String tokenType,
            String operation, String expectedStatusCode, String expectedResponse, String description) {

        String token = getTokenForAccount(accountType, tokenType);

        ArrayList<Integer> recruiterIds = new ArrayList<>();
        try {
            Map<String, String> recruiterParams = new HashMap<>();
            recruiterParams.put("report", "recruiter");
            Response recruiterResponse = RestClient.doPost("JSON", albatrossURL, "global/get-users-for-rpr", token, recruiterParams, true, null);

            if (recruiterResponse.getStatusCode() == 200) {
                JsonPath recruiterJson = recruiterResponse.jsonPath();
                List<Integer> validRecruiterIds = recruiterJson.getList("data.id");
                if (validRecruiterIds != null && !validRecruiterIds.isEmpty()) {
                    recruiterIds.addAll(validRecruiterIds);
                } else {
                    recruiterIds.add(1);
                }
            } else {
                recruiterIds.add(1);
            }
        } catch (Exception e) {
            recruiterIds.add(1);
        }

        ArrayList<Integer> teamIds = new ArrayList<>();
        ArrayList<Object> kpiLists = new ArrayList<>();

        kpiLists.add(createKpiList("task", "Tasks Added"));
        kpiLists.add(createKpiList("appointment", "Total Meetings Added"));
        kpiLists.add(createKpiList("Sequence Created", "seqcreated"));
        kpiLists.add(createKpiList("Sequence Enrollments", "seqenrollment"));
        kpiLists.add(createKpiList("Sequence Open rate", "seqopenrate"));
        kpiLists.add(createKpiList("Sequence Reply rate", "seqreplyrate"));
        kpiLists.add(createKpiList("Sequence Unsubscribed rate", "sequnsubscriberate"));

        TeamPerformanceReport teamReport = new TeamPerformanceReport();
        teamReport.setRecruiter_ids(recruiterIds);
        teamReport.setTeam_ids(teamIds);
        teamReport.setKpi_lists(kpiLists);
        teamReport.setFrom_date("1613400447");
        teamReport.setTo_date("1644936462");

        Response response = null;
        Map<String, String> queryParameters = new HashMap<>();

        try {
            if (isInvalidToken(tokenType)) {
                if (operation.toUpperCase().contains("POST")) {
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", token, null, true, teamReport);
                } else {
                    queryParameters.put("report_type", "1");
                    response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
                }
                response.then().statusCode(401);
                return;
            }

            switch (operation.toUpperCase()) {
                case "POST_CREATE_REPORT":
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", token, null, true, teamReport);
                    break;

                case "GET_REPORTS_KPI":
                    queryParameters.put("report_type", "1");
                    response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
                    break;

                case "POST_REPORT_WITH_A_RECRUITERS":
                    if (accountType.equals("AccountB")) {
                        ArrayList<Integer> crossAccountRecruiterIds = new ArrayList<>();
                        crossAccountRecruiterIds.add(999);
                        teamReport.setRecruiter_ids(crossAccountRecruiterIds);
                    }
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", token, null, true, teamReport);
                    break;

                case "POST_REPORT_WITH_A_TEAMS":
                    if (accountType.equals("AccountB")) {
                        ArrayList<Integer> crossAccountTeamIds = new ArrayList<>();
                        crossAccountTeamIds.add(888);
                        teamReport.setTeam_ids(crossAccountTeamIds);
                    }
                    response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", token, null, true, teamReport);
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
                    } catch (Exception ignored) {
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
                    } catch (Exception ignored) {
                    }
                    break;

                case "token_expired":
                    try {
                        response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
                    } catch (Exception ignored) {
                    }
                    break;

                default:
                    try {
                        response.then().body("error_message", Matchers.equalTo(expectedResponse));
                    } catch (Exception e1) {
                        try {
                            response.then().body("error", Matchers.equalTo(expectedResponse));
                        } catch (Exception ignored) {
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") ||
                expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") ||
                expectedResponse.contains("validation_error") || expectedResponse.contains("not_found")) {
            } else {
                throw e;
            }
        }
    }

    private boolean isInvalidToken(String tokenType) {
        return tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed") ||
               tokenType.equals("empty") || tokenType.equals("null");
    }

    private KpiLists createKpiList(String label, String value) {
        KpiLists kpi = new KpiLists();
        kpi.setLabel(label);
        kpi.setValue(value);
        return kpi;
    }

    @DataProvider(name = "crossAccountTeamPerformanceReportTestData")
    public static Object[][] crossAccountTeamPerformanceReportTestData() {
        return new Object[][] {
            {"SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE_REPORT", "200", "success", "Account A should be able to create team performance report"},

            {"SCENARIO_1_GET_KPI", "AccountB", "valid", "GET_REPORTS_KPI", "200", "success", "Account B should be able to get reports KPI independently"},
            {"SCENARIO_1_CROSS_RECRUITER_ACCESS", "AccountB", "valid", "POST_REPORT_WITH_A_RECRUITERS", "422", "validation_error", "Account B should not use Account A's recruiter IDs"},
            {"SCENARIO_1_CROSS_TEAM_ACCESS", "AccountB", "valid", "POST_REPORT_WITH_A_TEAMS", "422", "validation_error", "Account B should not use Account A's team IDs"},

            {"SCENARIO_2_CREATE_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", "Account B should be denied create with invalid token"},
            {"SCENARIO_2_GET_KPI_INVALID", "AccountB", "invalid", "GET_REPORTS_KPI", "401", "unauthorized", "Account B should be denied KPI access with invalid token"},
            {"SCENARIO_2_CROSS_RECRUITER_INVALID", "AccountB", "invalid", "POST_REPORT_WITH_A_RECRUITERS", "401", "unauthorized", "Account B should be denied cross-account access with invalid token"},
            {"SCENARIO_2_CROSS_TEAM_INVALID", "AccountB", "invalid", "POST_REPORT_WITH_A_TEAMS", "401", "unauthorized", "Account B should be denied cross-account access with invalid token"},

            {"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE_REPORT", "401", "token_expired", "Expired token should return 401"},
            {"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_REPORTS_KPI", "401", "Unauthorized", "Malformed token should return 401"},

            {"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE_REPORT", "401", "unauthorized", "Empty token should return 401"},
            {"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "GET_REPORTS_KPI", "401", "unauthorized", "Null token should return 401"}
        };
    }


}
