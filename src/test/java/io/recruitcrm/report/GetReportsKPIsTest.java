package io.recruitcrm.report;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetReportsKPIsTest extends TestBase {

	public GetReportsKPIsTest() {
		// TODO Auto-generated constructor stub
		super();
	}
	commanFunction function = new commanFunction();

	@Owner("Divya")
	@Test(dataProvider = "getReportTypeValidData", groups = "nightly-build")
	public void getReportsKPIs_Test(String reportType) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("report_type", reportType);

		String basePath = "reports-kpi";

		Response response = RestClient.doGet("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
				true);

		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.containsString("success"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("kpilist.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getReportTypeInValidData", groups = "nightly-build")
	public void getReportsKPIsWithInvalidReportType_Test(String reportType, String errorMessage) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("report_type", reportType);

		String basePath = "reports-kpi";

		Response response = RestClient.doGet("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
				true);

		response.then().statusCode(422);
		response.then().body("report_type[0]", Matchers.containsString(errorMessage));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("kpilistForInvalidValue.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getReportsKPIsWithEmptyReportType_Test() {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("report_type", "");

		String basePath = "reports-kpi";

		Response response = RestClient.doGet("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
				true);

		response.then().statusCode(422);
		response.then().body("report_type[0]", Matchers.containsString("The report type field is required."));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("kpilistForInvalidValue.json"));

	}

	@DataProvider
	public Object[][] getReportTypeInValidData() {

		Object data[][] = { { "9999", "report_type invalid type" }, { "x99x", "The report type must be an integer." } };
		return data;
	}

	@DataProvider
	public Object[][] getReportTypeValidData() {

		Object data[][] = { { "1" }, { "4" } };
		return data;
	}

}