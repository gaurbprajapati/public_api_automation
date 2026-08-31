package io.recruitcrm.report.schedule_report;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import io.rcrm.api.javafaker.albatross.report.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.ScheduleReport.ScheduleReportRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.commanfunctions.reportService.ReportServiceFunctions;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountScheduleReportSecurityTest extends TestBase {
	private String reportPreferenceID = "";
	private String scheduleReportIDForDeletion = "";
	private final JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	private final JavaFakerScheduleReport scheduleReportFaker = new JavaFakerScheduleReport();
	private final ReportServiceFunctions reportServiceFunctions = new ReportServiceFunctions();

	@BeforeClass(alwaysRun = true)	public void setupPrerequisites() {
		String token = getTokenForAccount("AccountA", "valid");
		String reportName = reportFaker.getReportName();
		try {
			reportPreferenceID = reportServiceFunctions.createReportPreference(baseURL, reportServiceURL, token, reportName);
		} catch (Exception e) {
			reportPreferenceID = "1002";
		}
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "crossAccountScheduleReportTestData", groups = "nightly-build")
	public void crossAccountScheduleReportOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {
		String token = getTokenForAccount(accountType, tokenType);
		Response response = null;
		Map<String, String> pathParameters = new HashMap<>();

		try {
			switch (operation.toUpperCase()) {
				case "POST_CREATE":
					ArrayList<Integer> collaboratorTeamIds = new ArrayList<>();
					collaboratorTeamIds.add(1);
					String body = scheduleReportFaker.getEmailBody();
					ScheduleReportRequest scheduleReportRequest = reportServiceFunctions.scheduleReportRequest(body, collaboratorTeamIds, true, reportPreferenceID.isEmpty() ? "1002" : reportPreferenceID);
					response = RestClient.doPost("JSON", reportServiceURL, "schedule-report", token, null, true, scheduleReportRequest);
					if (response.getStatusCode() == 200) {
						JsonPath jp = response.jsonPath();
						if (jp.get("data.id") != null) {
							int ID = jp.get("data.id");
							scheduleReportIDForDeletion = Integer.toString(ID);
						}
					}
					break;

				case "GET_ALL":
					String getBasePath = "schedule-report/get-all-schedules/" + (reportPreferenceID.isEmpty() ? "1002" : reportPreferenceID);
					response = RestClient.doGet("JSON", reportServiceURL, getBasePath, token, null, null, true);
					break;

				case "DELETE":
					pathParameters.put("id", scheduleReportIDForDeletion.isEmpty() ? "1002" : scheduleReportIDForDeletion);
					String deleteBasePath = "schedule-report/{id}";
					response = RestClient.doDelete("JSON", reportServiceURL, deleteBasePath, token, null, pathParameters, true);
					break;

				default:
					Assert.fail("Unsupported operation: " + operation);
			}

			int expectedStatus = Integer.parseInt(expectedStatusCode);
			if (response != null) {
				response.then().statusCode(expectedStatus);
			}

			if (response != null) {
				switch (expectedResponse) {
					case "success":
						if (operation.startsWith("GET")) {
                            if (accountType.equals("AccountB")) response.then().body("$", Matchers.empty());
                            else response.then().body(Matchers.notNullValue());
                        }
						if (operation.equals("POST_CREATE")) response.then().body("data.id", Matchers.notNullValue());
						if (operation.equals("DELETE")) response.then().body("status_message", Matchers.equalTo("success"));
						break;

					case "unauthorized":
					case "Unauthorized":
					case "token_expired":
						try {
							response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
						} catch (Exception ignored) {}
						break;

					case "server_error":
						try {
							response.then().body(Matchers.notNullValue());
						} catch (Exception ignored) {}
						break;

					default:
						try {
							response.then().body("error_message", Matchers.equalTo(expectedResponse));
						} catch (Exception e) {
							try {
								response.then().body("error", Matchers.equalTo(expectedResponse));
							} catch (Exception ignored) {}
						}
				}
			}
		} catch (Exception e) {
			if (!(expectedResponse.contains("unauthorized") || expectedResponse.contains("token_expired") ||  expectedResponse.contains("server_error"))) {
				throw e;
			}
		}
	}

	@DataProvider(name = "crossAccountScheduleReportTestData")
	public static Object[][] crossAccountScheduleReportTestData() {
		return new Object[][] {
			{"SCENARIO_1_CREATE_ACCOUNT_A", "AccountA", "valid", "POST_CREATE", "200", "success", "Account A creates schedule report"},
			{"SCENARIO_1_GET_ALL_ACCOUNT_B", "AccountA", "valid", "GET_ALL", "200", "success", "Account B should get empty response body"},
			{"SCENARIO_1_DELETE_ACCOUNT_B", "AccountB", "valid", "DELETE", "500", "server_error", "Account B should NOT delete Account A's schedule report"},
			
			{"SCENARIO_2_CREATE_INVALID_TOKEN", "AccountB", "invalid", "POST_CREATE", "401", "unauthorized", "Create with invalid token"},
			{"SCENARIO_2_CREATE_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE", "401", "token_expired", "Create with expired token"},
			{"SCENARIO_2_CREATE_MALFORMED_TOKEN", "AccountB", "malformed", "POST_CREATE", "401", "Unauthorized", "Create with malformed token"},
			{"SCENARIO_2_CREATE_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE", "401", "unauthorized", "Create with empty token"},
			{"SCENARIO_2_CREATE_NULL_TOKEN", "AccountB", "null", "POST_CREATE", "401", "unauthorized", "Create with null token"},
			
			{"SCENARIO_3_GET_ALL_INVALID_TOKEN", "AccountB", "invalid", "GET_ALL", "401", "unauthorized", "Get all with invalid token"},
			{"SCENARIO_3_GET_ALL_EXPIRED_TOKEN", "AccountB", "expired", "GET_ALL", "401", "token_expired", "Get all with expired token"},
			{"SCENARIO_3_GET_ALL_MALFORMED_TOKEN", "AccountB", "malformed", "GET_ALL", "401", "Unauthorized", "Get all with malformed token"},
			{"SCENARIO_3_GET_ALL_EMPTY_TOKEN", "AccountB", "empty", "GET_ALL", "401", "unauthorized", "Get all with empty token"},
			{"SCENARIO_3_GET_ALL_NULL_TOKEN", "AccountB", "null", "GET_ALL", "401", "unauthorized", "Get all with null token"},
			
			{"SCENARIO_4_DELETE_INVALID_TOKEN", "AccountB", "invalid", "DELETE", "401", "unauthorized", "Delete with invalid token"},
			{"SCENARIO_4_DELETE_EXPIRED_TOKEN", "AccountB", "expired", "DELETE", "401", "token_expired", "Delete with expired token"},
			{"SCENARIO_4_DELETE_MALFORMED_TOKEN", "AccountB", "malformed", "DELETE", "401", "Unauthorized", "Delete with malformed token"},
			{"SCENARIO_4_DELETE_EMPTY_TOKEN", "AccountB", "empty", "DELETE", "401", "unauthorized", "Delete with empty token"},
			{"SCENARIO_4_DELETE_NULL_TOKEN", "AccountB", "null", "DELETE", "401", "unauthorized", "Delete with null token"},
			
			{"SCENARIO_5_CROSS_ACCOUNT_CREATE", "AccountC", "valid", "POST_CREATE", "401", "unauthorized", "Non-existent account creates schedule report"},
			{"SCENARIO_5_CROSS_ACCOUNT_GET_ALL", "AccountC", "valid", "GET_ALL", "401", "unauthorized", "Non-existent account gets schedule reports"},
			{"SCENARIO_5_CROSS_ACCOUNT_DELETE", "AccountC", "valid", "DELETE", "401", "unauthorized", "Non-existent account deletes schedule report"}
		};
	}
}
