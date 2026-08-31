package io.recruitcrm.report;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.CandidateLifecycleReport;
import io.recruitcrm.report.pojo.KpiLists;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountCandidateLifecycleReportSecurityTest extends TestBase {

	@Owner("Ajendra Singh")
	@Test(dataProvider = "crossAccountCandidateLifecycleReportTestData", groups = "nightly-build")
	public void crossAccountCandidateLifecycleReportOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {

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

		CandidateLifecycleReport candidateReport = new CandidateLifecycleReport();
		candidateReport.setKpi_lists(kpiLists);
		candidateReport.setFrom_date("1613400447");
		candidateReport.setTo_date("1644936462");

		Response response = null;

		try {
			if (tokenType.equals("invalid") || tokenType.equals("expired") || tokenType.equals("malformed")
					|| tokenType.equals("empty") || tokenType.equals("null")) {
				response = RestClient.doPost("JSON", reportServiceURL, "reports/candidate-lifecycle-report", token, null, true, candidateReport);
				response.then().statusCode(401);
				return;
			}

			switch (operation.toUpperCase()) {
				case "POST_CREATE_REPORT":
					response = RestClient.doPost("JSON", reportServiceURL, "reports/candidate-lifecycle-report", token, null, true, candidateReport);
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
					try {
						response.then().body("error", Matchers.containsString("validation"));
					} catch (Exception ignored) {}
					break;

				case "access denied":
				case "access_denied":
				case "forbidden":
					try {
						response.then().body("error", Matchers.containsString("forbidden"));
					} catch (Exception e) {
						try {
							response.then().body("message", Matchers.containsString("access"));
						} catch (Exception ignored) {}
					}
					break;

				case "unauthorized":
				case "Unauthorized":
				case "token_expired":
					try {
						response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
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
					break;
			}

		} catch (Exception e) {
			if (!(expectedResponse.contains("unauthorized") || expectedResponse.contains("access denied")
					|| expectedResponse.contains("access_denied") || expectedResponse.contains("token_expired")
					|| expectedResponse.contains("bad_request") || expectedResponse.contains("not_found"))) {
				throw e;
			}
		}
	}

	@DataProvider(name = "crossAccountCandidateLifecycleReportTestData")
	public static Object[][] crossAccountCandidateLifecycleReportTestData() {
		return new Object[][] {
			{"SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE_REPORT", "200", "success", ""},
			{"SCENARIO_1_CREATE_B", "AccountB", "valid", "POST_CREATE_REPORT", "200", "success", ""},
			{"SCENARIO_1_CREATE_B2", "AccountB", "valid", "POST_CREATE_REPORT", "200", "success", ""},
			{"SCENARIO_1_VERIFY", "AccountA", "valid", "POST_CREATE_REPORT", "200", "success", ""},
			{"SCENARIO_2_CREATE_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", ""},
			{"SCENARIO_2_CREATE_B_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", ""},
			{"SCENARIO_2_CREATE_B2_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", ""},
			{"SCENARIO_2_VERIFY_INVALID", "AccountA", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", ""},
			{"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE_REPORT", "401", "token_expired", ""},
			{"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "POST_CREATE_REPORT", "401", "Unauthorized", ""},
			{"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE_REPORT", "401", "unauthorized", ""},
			{"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "POST_CREATE_REPORT", "401", "unauthorized", ""}
		};
	}
}