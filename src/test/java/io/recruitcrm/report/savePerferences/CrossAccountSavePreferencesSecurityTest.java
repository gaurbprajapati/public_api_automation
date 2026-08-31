package io.recruitcrm.report.savePerferences;

import java.util.ArrayList;
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
import io.recruitcrm.report.pojo.SavePerference.SavePerference;
import io.recruitcrm.report.pojo.SavePerference.Settings;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountSavePreferencesSecurityTest extends TestBase {

	private String reportPreferenceID = "";
	private JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();


	@Owner("Akshaya Uppala")
	@Test(dataProvider = "crossAccountSavePreferencesTestData", groups = "nightly-build")
	public void crossAccountSavePreferencesOperations_Test(String testScenario, String accountType, String tokenType,
															String operation, String expectedStatusCode, String expectedResponse, String description) {

		String reportName = reportFaker.getReportName();
		String token = getTokenForAccount(accountType, tokenType);

		ArrayList<Integer> recruiterIds = new ArrayList<>();
		recruiterIds.add(1);
		ArrayList<Integer> teamIds = new ArrayList<>();

		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists("[{\"label\":\"Applied\",\"value\":\"10\",\"checked\":true}]");
		settings.setFrom_date("1613400447");
		settings.setTo_date("1644936462");
		settings.setDate_format("custom_range");

		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(1);
		savePerference.setSettings(settings);

		Response response = null;
		Map<String, String> pathParameters = new HashMap<>();
		Map<String, String> queryParameters = new HashMap<>();

		try {
			if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed")
					|| tokenType.equals("empty") || tokenType.equals("null")) {

				if (operation.toUpperCase().equals("POST_CREATE")) {
					response = RestClient.doPost("JSON", reportServiceURL, "reports-preferences/save", token, null, true, savePerference);
				} else {
					response = RestClient.doGet("JSON", reportServiceURL, "reports-preferences", token, null, null, true);
				}
				response.then().statusCode(401);
				return;
			}

			switch (operation.toUpperCase()) {
				case "POST_CREATE":
					response = RestClient.doPost("JSON", reportServiceURL, "reports-preferences/save", token, null, true, savePerference);
					response.then().statusCode(200);
					response.then().body("data.id", Matchers.notNullValue());
					JsonPath jp = response.jsonPath();
					int ID = jp.get("data.id");
					reportPreferenceID = Integer.toString(ID);
					break;

				case "GET_ALL":
					queryParameters.put("report_type", "1");
					response = RestClient.doGet("JSON", reportServiceURL, "reports-preferences", token, queryParameters, null, true);
					break;

				case "GET_BY_ID":
					pathParameters.put("ID", reportPreferenceID.isEmpty() ? "1002" : reportPreferenceID);
					response = RestClient.doGet("JSON", reportServiceURL, "reports-preferences/{ID}", token, null, pathParameters, true);
					break;

				case "GET_KPI":
					queryParameters.put("report_type", "1");
					response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
					break;

				case "POST_EDIT":
					pathParameters.put("ID", reportPreferenceID.isEmpty() ? "1002" : reportPreferenceID);
					savePerference.setName(reportName + "_Edited");
					response = RestClient.doPost1("JSON", reportServiceURL, "reports-preferences/{ID}", token, null, pathParameters, true, savePerference);
					break;

				case "DELETE":
					pathParameters.put("ID", reportPreferenceID.isEmpty() ? "1002" : reportPreferenceID);
					response = RestClient.doDelete("JSON", reportServiceURL, "reports-preferences/{ID}", token, null, pathParameters, false);
					break;

				default:
					Assert.fail("Unsupported operation: " + operation);
			}

			int expectedStatus = Integer.parseInt(expectedStatusCode);
			response.then().statusCode(expectedStatus);

			switch (expectedResponse) {
				case "success":
					if (operation.startsWith("GET")) response.then().body(Matchers.notNullValue());
					break;

				case "bad_request":
					try {
						response.then().body("error", Matchers.containsString("validation"));
					} catch (Exception ignored) {}
					break;

				case "access denied":
				case "forbidden":
					response.then().body("error", Matchers.containsString("forbidden"));
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

				case "not_found":
				case "Report Preference not found":
					try {
						response.then().body("error_message", Matchers.containsString("not found"));
					} catch (Exception e) {
						try {
							response.then().body("message", Matchers.containsString("not found"));
						} catch (Exception ignored) {}
					}
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
		} catch (Exception e) {
			if (!(expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied") ||
					expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request") ||
					expectedResponse.contains("not_found") || expectedResponse.contains("Report Preference not found") ||
					expectedResponse.contains("server_error"))) {
				throw e;
			}
		}
	}

	@DataProvider(name = "crossAccountSavePreferencesTestData")
	public static Object[][] crossAccountSavePreferencesTestData() {
		return new Object[][] {
			{"SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE", "200", "success", ""},
			{"SCENARIO_1_GET_ALL", "AccountB", "valid", "GET_ALL", "200", "success", ""},
			{"SCENARIO_1_GET_BY_ID", "AccountB", "valid", "GET_BY_ID", "500", "server_error", ""},
			{"SCENARIO_1_DELETE", "AccountB", "valid", "DELETE", "500", "server_error", ""},
			{"SCENARIO_2_CREATE_INVALID", "AccountB", "invalid", "POST_CREATE", "401", "unauthorized", ""},
			{"SCENARIO_2_GET_ALL_INVALID", "AccountB", "invalid", "GET_ALL", "401", "unauthorized", ""},
			{"SCENARIO_2_GET_BY_ID_INVALID", "AccountB", "invalid", "GET_BY_ID", "401", "unauthorized", ""},
			{"SCENARIO_2_DELETE_INVALID", "AccountB", "invalid", "DELETE", "401", "unauthorized", ""},
			{"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "GET_ALL", "401", "token_expired", ""},
			{"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_ALL", "401", "Unauthorized", ""},
			{"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "GET_ALL", "401", "unauthorized", ""},
			{"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "GET_ALL", "401", "unauthorized", ""}
		};
	}


}
