package io.recruitcrm.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.javafaker.JavaFakerCompany;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.javafaker.JavaFakerReport;
import io.rcrm.api.pojo.SalesPipelineStage;
import io.rcrm.api.pojo.SalesPipelineStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.commanfunctions.commanFunction;
import io.recruitcrm.report.pojo.ClientPerformanceReport;
import io.recruitcrm.report.pojo.KpiLists;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class ClientPerformanceReport_Test extends TestBase {
	commanFunction function = new commanFunction();
	JavaFakerReport javaFakerReport = new JavaFakerReport();
	JavaFakerCompany javaFakerCompany = new JavaFakerCompany();
	String companyName = javaFakerCompany.getCompanyName();
	JavaFakerContact contactFaker = new JavaFakerContact();
	SalesPipelineStages salesPipelineStages = new SalesPipelineStages();

	String customContactStage = javaFakerReport.getLabel();
	String companySlug;

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void getClientPerformanceReportKPIs_Test() {
		HashMap<String, String> queryParameters = new HashMap<>();
		queryParameters.put("report_type", "3");

		Response response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("status_message", Matchers.containsString("success"));
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetClientPerformanceReportKPIs_Test() {
		HashMap<String, String> queryParameters = new HashMap<>();
		queryParameters.put("report_type", "3");

		Response response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", ThreadManager.getOwnerAlbatrossToken()+"abcd",
				queryParameters, null, true);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.getString("error"), "Invalid authcode or unauthorized user");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void clientPerformanceReportKPIsWithEmptyQueryParameters_Test() {
		Response response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", ThreadManager.getOwnerAlbatrossToken(), null, null, true);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("report_type[0]"), "The report type field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getContactStageKPIsTestData", groups = "nightly-build")
	public void clientPerformanceReportWithDefaultContactStage_Test(String Value, String label, String companySlug) {
		ClientPerformanceReport clientPerformanceReporpayload = clientPerformanceReportPayload(Value, label,
				companySlug);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/client-performance-report",
				ThreadManager.getOwnerAlbatrossToken(), null, true, clientPerformanceReporpayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);

		String seriesName = jsonPath.getString("chart_data.series_data[0].seriesname");
		Assert.assertEquals(seriesName, label);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void clientPerformanceReportWithInvalidCompanySlug_Test() {
		String value = javaFakerReport.getRandomDigits(1);
		String label = javaFakerReport.getLabel();
		String companySlugInvalid = companySlug + "a";
		ClientPerformanceReport clientPerformanceReporpayload = clientPerformanceReportPayload(value, label,
				companySlugInvalid);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/client-performance-report",
				ThreadManager.getOwnerAlbatrossToken(), null, true, clientPerformanceReporpayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("company_slugs[0]"), "The selected company slugs is invalid.");
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void clientPerformanceReportWithEmptyCompanySlugAndKPIsList_Test() {
		ClientPerformanceReport payload = new ClientPerformanceReport();
		payload.setCompany_slugs(new String[] {});
		payload.setKpi_lists(new KpiLists[] {});
		payload.setFrom_date(javaFakerReport.getFromDate());
		payload.setTo_date(javaFakerReport.getToDate());

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/client-performance-report",
				ThreadManager.getOwnerAlbatrossToken(), null, true, payload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("kpi_lists[0]"), "Please select at least one KPI to generate report");
		Assert.assertEquals(jsonPath.getString("company_slugs[0]"), "The company slugs field is required.");
	}

	@Owner("Rahul Shibu")
	@Test(dataProvider = "getCompanySlugTestData", groups = "nightly-build")
	public void clientPerformanceReportWithCustomContactStage_Test(String companySlug) {
		List<SalesPipelineStage> stages = new ArrayList<>();
		stages.add(new SalesPipelineStage(customContactStage));
		salesPipelineStages.setSalesPipelineStages(stages);
		Response responseContactStage = RestClient.doPost("JSON", albatrossURL, "sales-pipeline", ThreadManager.getOwnerAlbatrossToken(), null, true,
				salesPipelineStages);
		
		String value = javaFakerReport.getRandomDigits(1);

		ClientPerformanceReport clientPerformanceReporpayload = clientPerformanceReportPayload(value,
				customContactStage, companySlug);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/client-performance-report",
				ThreadManager.getOwnerAlbatrossToken(), null, true, clientPerformanceReporpayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("chart_data.series_data[0].seriesname"), customContactStage);
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGenerateClientPerformanceReport_Test() {
		String value = javaFakerReport.getRandomDigits(1);
		String label = javaFakerReport.getLabel();

		ClientPerformanceReport clientPerformanceReporpayload = clientPerformanceReportPayload(value, label,
				companySlug);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/client-performance-report",
				ThreadManager.getOwnerAlbatrossToken()+"abcd", null, true, clientPerformanceReporpayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.getString("error"), "Invalid authcode or unauthorized user");
	}

	public ClientPerformanceReport clientPerformanceReportPayload(String value, String label, String companySlug) {
		KpiLists kpi1 = new KpiLists();
		kpi1.setValue(value);
		kpi1.setLabel(label);
		kpi1.setChecked(true);

		ClientPerformanceReport payload = new ClientPerformanceReport();
		payload.setCompany_slugs(new String[] { companySlug });
		payload.setKpi_lists(new KpiLists[] { kpi1 });
		payload.setFrom_date(javaFakerReport.getFromDate());
		payload.setTo_date(javaFakerReport.getToDate());
		return payload;
	}

	@DataProvider
	public Object[][] getContactStageKPIsTestData() {
		String companySlug = getCompanySlug();
		return new Object[][] { { "1", "Contact Stage - Lead", companySlug },
				{ "2", "Contact Stage - Follow Up", companySlug }, { "3", "Contact Stage - Client", companySlug } };
	}
	
	@DataProvider 
	public Object[] getCompanySlugTestData() {
		return new Object[]{getCompanySlug()};
	}
	
	public String getCompanySlug() {
		JsonPath companyJsonPath = function.createNewCompanyWithMandatoryFields(baseURL , ThreadManager.getAccountApiKey()).jsonPath();
		String companySlug = companyJsonPath.getString("slug");
		return companySlug;
	}

}
