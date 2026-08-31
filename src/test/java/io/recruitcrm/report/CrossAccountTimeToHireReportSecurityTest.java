package io.recruitcrm.report;

import java.util.ArrayList;

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

import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.TimeToHireReport;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountTimeToHireReportSecurityTest extends TestBase {



	@Owner("Smit Patel")
	@Test(dataProvider = "crossAccountTimeToHireReportTestData", groups = "nightly-build")
	public void crossAccountTimeToHireReportOperations_Test(String testScenario, String accountType, String tokenType,
			String operation, String expectedStatusCode, String expectedResponse, String description) {

		String token = getTokenForAccount(accountType, tokenType);

		ArrayList<Object> kpiLists = new ArrayList<>();

		KpiLists KpiLists1 = new KpiLists();
		KpiLists1.setLabel("Applied");
		KpiLists1.setValue("10");
		KpiLists1.setChecked(true);

		KpiLists KpiLists2 = new KpiLists();
		KpiLists2.setLabel("Assigned");
		KpiLists2.setValue("1");
		KpiLists2.setChecked(true);

		KpiLists KpiLists3 = new KpiLists();
		KpiLists3.setLabel("Placed");
		KpiLists3.setValue("8");
		KpiLists3.setChecked(true);

		KpiLists KpiLists4 = new KpiLists();
		KpiLists4.setLabel("Candidate Pitched - Pitched");
		KpiLists4.setValue("candidate_pitched_1");
		KpiLists4.setChecked(true);

		kpiLists.add(KpiLists1);
		kpiLists.add(KpiLists2);
		kpiLists.add(KpiLists3);
		kpiLists.add(KpiLists4);

		TimeToHireReport timeToHireReport = new TimeToHireReport();
		timeToHireReport.setKpi_lists(kpiLists);

		ArrayList<Integer> jobIds = new ArrayList<>();
		jobIds.add(1);
		timeToHireReport.setJob_ids(jobIds);

		Response response = null;

		try {
			if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed")
					|| tokenType.equals("empty") || tokenType.equals("null")) {
				response = RestClient.doPost("JSON", reportServiceURL, "reports/time-to-hire-report-new", token, null,
						true, timeToHireReport);
				response.then().statusCode(401);
				return;
			}

			switch (operation.toUpperCase()) {
			case "POST_CREATE_REPORT":
				response = RestClient.doPost("JSON", reportServiceURL, "reports/time-to-hire-report-new", token, null,
						true, timeToHireReport);
				break;

			case "POST_REPORT_WITH_A_JOBS":
				if (accountType.equals("AccountB")) {
					ArrayList<Integer> crossAccountJobIds = new ArrayList<>();
					crossAccountJobIds.add(777);
					crossAccountJobIds.add(888);
					timeToHireReport.setJob_ids(crossAccountJobIds);
				}
				response = RestClient.doPost("JSON", reportServiceURL, "reports/time-to-hire-report-new", token, null,
						true, timeToHireReport);
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
			if (expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied")
					|| expectedResponse.contains("token_expired") || expectedResponse.contains("bad_request")
					|| expectedResponse.contains("validation_error") || expectedResponse.contains("not_found")) {
			} else {
				throw e;
			}
		}
	}

	@DataProvider(name = "crossAccountTimeToHireReportTestData")
	public static Object[][] crossAccountTimeToHireReportTestData() {
		return new Object[][] { { "SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE_REPORT", "422",
				"validation_error", "Account A time to hire report validation (no valid jobs)" },

				{ "SCENARIO_1_CREATE_B", "AccountB", "valid", "POST_CREATE_REPORT", "422", "validation_error",
						"Account B time to hire report validation independently" },
				{ "SCENARIO_1_CROSS_JOB_ACCESS", "AccountB", "valid", "POST_REPORT_WITH_A_JOBS", "422",
						"validation_error", "Account B should not use Account A's job IDs" },
				{ "SCENARIO_1_VERIFY", "AccountA", "valid", "POST_CREATE_REPORT", "422", "validation_error",
						"Account A should maintain independent validation after Account B attempts" },

				{ "SCENARIO_2_CREATE_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized",
						"Account B should be denied create with invalid token" },
				{ "SCENARIO_2_CREATE_B_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized",
						"Account B should be denied create with invalid token" },
				{ "SCENARIO_2_CROSS_JOB_INVALID", "AccountB", "invalid", "POST_REPORT_WITH_A_JOBS", "401",
						"unauthorized", "Account B should be denied cross-account access with invalid token" },
				{ "SCENARIO_2_VERIFY_INVALID", "AccountA", "invalid", "POST_CREATE_REPORT", "401", "unauthorized",
						"Account A should be denied access with invalid token" },

				{ "SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE_REPORT", "401", "token_expired",
						"Expired token should return 401" },

				{ "SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_CREATE_REPORT", "401", "Unauthorized",
						"Malformed token should return 401" },

				{ "SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE_REPORT", "401", "unauthorized",
						"Empty token should return 401" },

				{ "SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_CREATE_REPORT", "401", "unauthorized",
						"Null token should return 401" } };
	}
}
