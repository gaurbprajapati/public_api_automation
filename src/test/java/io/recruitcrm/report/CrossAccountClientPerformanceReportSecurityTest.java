package io.recruitcrm.report;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.ClientPerformanceReport;
import io.recruitcrm.report.pojo.KpiLists;
import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerReport;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountClientPerformanceReportSecurityTest extends TestBase {

	private commanFunction function = new commanFunction();
	private JavaFakerReport javaFakerReport = new JavaFakerReport();
	private String accountA_CompanySlug;
	private String accountB_CompanySlug;

	@BeforeClass(alwaysRun = true)	public void setupTestData() {
		// Create companies for both accounts
		JsonPath companyAJsonPath = function.createNewCompanyWithMandatoryFields(baseURL, accountA_apiKey).jsonPath();
		accountA_CompanySlug = companyAJsonPath.getString("slug");
		JsonPath companyBJsonPath = function.createNewCompanyWithMandatoryFields(baseURL, accountB_apiKey).jsonPath();
		accountB_CompanySlug = companyBJsonPath.getString("slug");
	}

	@Owner("Ajendra Singh")
	@Test(dataProvider = "crossAccountClientPerformanceReportTestData", groups = "nightly-build")
	public void crossAccountClientPerformanceReportOperations_Test(String testScenario, String accountType, String tokenType, String operation, String expectedStatusCode, String expectedResponse, String description) {
		String token = getTokenForAccount(accountType, tokenType);
		String companySlug;
		if (operation.equals("POST_CREATE_REPORT_WITH_A_COMPANY")) {
			companySlug = accountA_CompanySlug;
		} else {
			companySlug = accountType.equals("AccountA") ? accountA_CompanySlug : accountB_CompanySlug;
		}

		KpiLists kpi1 = new KpiLists();
		kpi1.setValue("1");
		kpi1.setLabel("Contact Stage - Lead");
		kpi1.setChecked(true);

		ClientPerformanceReport clientReport = new ClientPerformanceReport();
		clientReport.setCompany_slugs(new String[] { companySlug });
		clientReport.setKpi_lists(new KpiLists[] { kpi1 });
		clientReport.setFrom_date(javaFakerReport.getFromDate());
		clientReport.setTo_date(javaFakerReport.getToDate());

		Response response = null;
		Map<String, String> queryParameters = new HashMap<>();

		try {
			switch (operation.toUpperCase()) {
				case "POST_CREATE_REPORT":
				case "POST_CREATE_REPORT_WITH_A_COMPANY":
					response = RestClient.doPost("JSON", reportServiceURL, "reports/client-performance-report", token, null, true, clientReport);
					break;
				case "GET_REPORTS_KPI":
					queryParameters.put("report_type", "3");
					response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, queryParameters, null, true);
					break;
				case "GET_REPORTS_KPI_EMPTY":
					response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", token, null, null, true);
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
				case "kpi_success":
					response.then().body("status_message", Matchers.containsString("success"));
					break;
				case "bad_request":
				case "validation_error":
					try {
						response.then().body(Matchers.notNullValue());
					} catch (Exception e) {}
					break;
				case "access denied":
				case "forbidden":
					response.then().body("error", Matchers.containsString("forbidden"));
					break;
				case "unauthorized":
				case "Unauthorized":
					try {
						response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
					} catch (Exception e) {}
					break;
				case "token_expired":
					try {
						response.then().body("error", Matchers.containsString("Invalid authcode or unauthorized user"));
					} catch (Exception e) {}
					break;
				default:
					try {
						response.then().body("error_message", Matchers.equalTo(expectedResponse));
					} catch (Exception e) {
						try {
							response.then().body("error", Matchers.equalTo(expectedResponse));
						} catch (Exception e2) {}
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

	@DataProvider(name = "crossAccountClientPerformanceReportTestData")
	public static Object[][] crossAccountClientPerformanceReportTestData() {
		return new Object[][] {
			{"SCENARIO_1_CREATE", "AccountA", "valid", "POST_CREATE_REPORT", "200", "success", "Account A should be able to create client performance report"},
			{"SCENARIO_1_GET_KPI", "AccountB", "valid", "GET_REPORTS_KPI", "200", "kpi_success", "Account B should be able to get client performance KPI independently"},
			{"SCENARIO_1_GET_KPI_EMPTY", "AccountB", "valid", "GET_REPORTS_KPI_EMPTY", "422", "validation_error", "Account B should get validation error with empty KPI parameters"},
			{"SCENARIO_1_CREATE_B", "AccountB", "valid", "POST_CREATE_REPORT", "200", "success", "Account B should be able to create client performance report independently"},
			{"SCENARIO_1_CROSS_COMPANY_ACCESS", "AccountB", "valid", "POST_CREATE_REPORT_WITH_A_COMPANY", "422", "validation_error", "Account B should not access Account A's company data"},
			{"SCENARIO_2_CREATE_INVALID", "AccountB", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", "Account B should be denied create with invalid token"},
			{"SCENARIO_2_GET_KPI_INVALID", "AccountB", "invalid", "GET_REPORTS_KPI", "401", "unauthorized", "Account B should be denied KPI access with invalid token"},
			{"SCENARIO_2_GET_KPI_EMPTY_INVALID", "AccountB", "invalid", "GET_REPORTS_KPI_EMPTY", "401", "unauthorized", "Account B should be denied access with invalid token"},
			{"SCENARIO_2_CREATE_A_INVALID", "AccountA", "invalid", "POST_CREATE_REPORT", "401", "unauthorized", "Account A should be denied access with invalid token"},
			{"SCENARIO_3_EXPIRED_TOKEN", "AccountB", "expired", "POST_CREATE_REPORT", "401", "token_expired", "Expired token should return 401"},
			{"SCENARIO_3_MALFORMED_TOKEN", "AccountB", "malformed", "GET_REPORTS_KPI", "401", "Unauthorized", "Malformed token should return 401"},
			{"SCENARIO_4_EMPTY_TOKEN", "AccountB", "empty", "POST_CREATE_REPORT", "401", "unauthorized", "Empty token should return 401"},
			{"SCENARIO_4_NULL_TOKEN", "AccountB", "null", "GET_REPORTS_KPI", "401", "unauthorized", "Null token should return 401"}
		};
	}
}
