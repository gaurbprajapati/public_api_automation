package io.recruitcrm.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.JavaFakerReport;
import io.rcrm.api.pojo.SalesPipelineStage;
import io.rcrm.api.pojo.SalesPipelineStages;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.ContactStageReport;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.TeamPerformanceReport;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class TeamPerformanceReport_Test extends TestBase{

	public TeamPerformanceReport_Test() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	String slug = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();
	JavaFakerReport javaFakerReport = new JavaFakerReport();
	SalesPipelineStages salesPipelineStages = new SalesPipelineStages();

	@Owner("Sandeep")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void teamPerformanceReportPost_Test(ArrayList<Integer> recruiterIds,String kpiListStringJson) {
		
		// get /v1/getkpilistreport
		// get /v1/collabrator
		
		ArrayList<Integer> teamIds =new ArrayList<Integer>();
		ArrayList<Object> kpiLists0 =new ArrayList<Object>();
		
		
		KpiLists KpiLists1 = new KpiLists();
		KpiLists1.setLabel("task");
		KpiLists1.setValue("Tasks Added");
		
		KpiLists KpiLists2 = new KpiLists();
		KpiLists2.setLabel("appointment");
		KpiLists2.setValue("Total Meetings Added");

		KpiLists KpiLists3 = new KpiLists();
		KpiLists3.setLabel("Sequence Created");
		KpiLists3.setValue("seqcreated");

		KpiLists KpiLists4 = new KpiLists();
		KpiLists4.setLabel("Sequence Enrollments");
		KpiLists4.setValue("seqenrollment");

		KpiLists KpiLists5 = new KpiLists();
		KpiLists5.setLabel("Sequence Open rate");
		KpiLists5.setValue("seqopenrate");

		KpiLists KpiLists6 = new KpiLists();
		KpiLists6.setLabel("Sequence Reply rate");
		KpiLists6.setValue("seqreplyrate");

		KpiLists KpiLists7 = new KpiLists();
		KpiLists7.setLabel("Sequence Unsubscribed rate");
		KpiLists7.setValue("sequnsubscriberate");
		
		
		kpiLists0.add(KpiLists1);
		kpiLists0.add(KpiLists2);
		kpiLists0.add(KpiLists3);
		kpiLists0.add(KpiLists4);
		kpiLists0.add(KpiLists5);
		kpiLists0.add(KpiLists6);
		kpiLists0.add(KpiLists7);

		
		TeamPerformanceReport teamReport = new TeamPerformanceReport();
		teamReport.setRecruiter_ids(recruiterIds);
		teamReport.setTeam_ids(teamIds);
		teamReport.setKpi_lists(kpiLists0);
		teamReport.setFrom_date("1613400447");
		teamReport.setTo_date("1644936462");
		
	
		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(), null, true, teamReport);

		response.then().statusCode(200);
		
		
	}
	
	
	@DataProvider
	public Object[][] getReportsValidTestData() {

		String generatedString = RandomStringUtils.randomAlphabetic(4);
		JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
		ArrayList<Integer> recruiterIds = jsonGetCollabrators.get("id");
		
		
		ArrayList<Object> kpiLists = new ArrayList<Object>();

		KpiLists KpiLists1 = new KpiLists();
		KpiLists1.setLabel("Tasks Added");
		KpiLists1.setValue("task");
		KpiLists1.setChecked(true);

		KpiLists KpiLists2 = new KpiLists();
		KpiLists2.setLabel("Total Meetings Added");
		KpiLists2.setValue("appointment");
		KpiLists2.setChecked(true);

		KpiLists KpiLists3 = new KpiLists();
		KpiLists3.setLabel("Sequence Created");
		KpiLists3.setValue("seqcreated");
		KpiLists3.setChecked(true);

		KpiLists KpiLists4 = new KpiLists();
		KpiLists4.setLabel("Sequence Enrollments");
		KpiLists4.setValue("seqenrollment");
		KpiLists4.setChecked(true);

		KpiLists KpiLists5 = new KpiLists();
		KpiLists5.setLabel("Sequence Open rate");
		KpiLists5.setValue("seqopenrate");
		KpiLists5.setChecked(true);

		KpiLists KpiLists6 = new KpiLists();
		KpiLists6.setLabel("Sequence Reply rate");
		KpiLists6.setValue("seqreplyrate");
		KpiLists6.setChecked(true);

		KpiLists KpiLists7 = new KpiLists();
		KpiLists7.setLabel("Sequence Unsubscribed rate");
		KpiLists7.setValue("sequnsubscriberate");
		KpiLists7.setChecked(true);

		kpiLists.add(KpiLists1);
		kpiLists.add(KpiLists2);
		kpiLists.add(KpiLists3);
		kpiLists.add(KpiLists4);
		kpiLists.add(KpiLists5);
		kpiLists.add(KpiLists6);
		kpiLists.add(KpiLists7);
		
		Gson gson = new Gson();
		String kpiListStringJson = gson.toJson(kpiLists);

		Object data[][] = {

				{ recruiterIds,kpiListStringJson } };
		return data;
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getContactStageKPIsTestData", groups = "nightly-build")
	public void teamPerformanceReportWithDefaultContactStage_Test(String Value, String label,
			List<Integer> recruiterValidIds) {
		ContactStageReport teamPerformanceReportPayload = teamPerformanceReportPayload(Value, label, recruiterValidIds);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(),
				null, true, teamPerformanceReportPayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		String seriesName = jsonPath.getString("chart_data.series_data[0].seriesname");
		Assert.assertEquals(seriesName, label);
	}
	
	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void teamPerformanceReportWithInvalidRecruiterId_Test() {
		String value = javaFakerReport.getRandomDigits(1);
		String label = javaFakerReport.getLabel();
		List<Integer> recruiterInvalidIds = javaFakerReport.getRecruiterIds();

		ContactStageReport teamPerformanceReportPayload = teamPerformanceReportPayload(value, label,
				recruiterInvalidIds);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(),
				null, true, teamPerformanceReportPayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("recruiter_ids[0]"), "The selected recruiter ids is invalid.");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void teamPerformanceReportWithEmptyRecruiterIds_Test() {
		String value = javaFakerReport.getRandomDigits(1);
		String label = javaFakerReport.getLabel();
		List<Integer> recruiterIds = new ArrayList<>();
		ContactStageReport teamPerformanceReportPayload = teamPerformanceReportPayload(value, label, recruiterIds);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(),
				null, true, teamPerformanceReportPayload);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("message[0]"),
				"Please select at least one team or team member to generate report");
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getRecruiterValidTestData", groups = "nightly-build")
	public void teamPerformanceReportWithEmptyKPIsList_Test(List<Integer> recruiterValidIds) {
		ContactStageReport contactStageReport = new ContactStageReport();
		contactStageReport.setRecruiter_ids(recruiterValidIds);
		contactStageReport.setKpi_lists(new KpiLists[] {});
		contactStageReport.setFrom_date(javaFakerReport.getPastDate(10));
		contactStageReport.setTeam_ids(new int[] {});
		contactStageReport.setTo_date(javaFakerReport.getFutureDate(10));

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(),
				null, true, contactStageReport);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 422);
		Assert.assertEquals(jsonPath.getString("kpi_lists[0]"), "Please select at least one KPI to generate report");
	}
	
	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getRecruiterValidTestData", groups = "nightly-build")
	public void teamPerformanceReportWithCustomContactStage_Test(List<Integer> recruiterValidIds) {
		String customContactStage = javaFakerReport.getLabel();
		List<SalesPipelineStage> stages = new ArrayList<>();
		stages.add(new SalesPipelineStage(customContactStage));
		salesPipelineStages.setSalesPipelineStages(stages);
		Response responseContactStage = RestClient.doPost("JSON", albatrossURL, "sales-pipeline", ThreadManager.getOwnerAlbatrossToken(), null, true,
				salesPipelineStages);
		Assert.assertEquals(responseContactStage.getStatusCode(), 200);

		ContactStageReport teamPerformanceReportPayload = teamPerformanceReportPayload("custom", customContactStage,
				recruiterValidIds);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report", ThreadManager.getOwnerAlbatrossToken(),
				null, true, teamPerformanceReportPayload);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("chart_data.series_data[0].seriesname"), customContactStage);
	}

	@Owner("Gaurav Prajapati")
	@Test(dataProvider = "getRecruiterValidTestData", groups = "nightly-build")
	public void unauthorizedUserCannotGenerateTeamPerformanceReport_Test(List<Integer> recruiterValidIds) {
		String value = javaFakerReport.getRandomDigits(1);
		String label = javaFakerReport.getLabel();

		ContactStageReport teamPerformanceReportPayload = teamPerformanceReportPayload(value, label, recruiterValidIds);

		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/team-performance-report",
				ThreadManager.getOwnerAlbatrossToken()+"abcd", null, true, teamPerformanceReportPayload);

		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.getString("error"), "Invalid authcode or unauthorized user");
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getTeamPerformanceReportKPIs_Test() {
		HashMap<String, String> queryParameters = new HashMap<>();
		queryParameters.put("report_type", "1");

		Response response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", ThreadManager.getOwnerAlbatrossToken(), queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("status_message", Matchers.containsString("success"));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetTeamPerformanceReportKPIs_Test() {
		HashMap<String, String> queryParameters = new HashMap<>();
		queryParameters.put("report_type", "1");

		Response response = RestClient.doGet("JSON", reportServiceURL, "reports-kpi", ThreadManager.getOwnerAlbatrossToken()+"abcd",
				queryParameters, null, true);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 401);
		Assert.assertEquals(jsonPath.getString("error"), "Invalid authcode or unauthorized user");
	}

	@Owner("Ajendra Singh")
	@Test(groups = "nightly-build")
	public void verfiyGlobalExportField_Test(){
		String basePath = "/global/export-fields";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
				null, true);

		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(response.getStatusCode(), 200);
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	public ContactStageReport teamPerformanceReportPayload(String value, String label, List<Integer> recruiterIds) {
		KpiLists kpiList = new KpiLists();
		kpiList.setValue(value);
		kpiList.setLabel(label);
		kpiList.setChecked(true);

		ContactStageReport payload = new ContactStageReport();
		payload.setRecruiter_ids(recruiterIds);
		payload.setKpi_lists(new KpiLists[] { kpiList });
		payload.setFrom_date(String.valueOf(DateUtil.yesterday().getTime()/1000));
		payload.setTeam_ids(new int[] {});
		payload.setTo_date(String.valueOf(DateUtil.tommarrow().getTime()/1000));
		return payload;
	}

	@DataProvider
	public Object[][] getContactStageKPIsTestData() {
		List<Integer> recruiterValidIds = getRecruiterIds();
		return new Object[][] { { "1", "Contact Stage - Lead", recruiterValidIds },
				{ "2", "Contact Stage - Follow Up", recruiterValidIds },
				{ "3", "Contact Stage - Client", recruiterValidIds } };
	}
	
	@DataProvider
	public Object[] getRecruiterValidTestData() {
		return new Object[] {getRecruiterIds()};
	}
	
	public List<Integer> getRecruiterIds() {
		HashMap<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("report", "recruiter");
		Response responseRecruiter = RestClient.doPost("JSON", albatrossURL, "global/get-users-for-rpr", ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, true, null);
		Assert.assertEquals(responseRecruiter.getStatusCode(), 200);
		JsonPath jsonPathRecruiter = responseRecruiter.jsonPath();
		List<Integer> recruiterValidIds = jsonPathRecruiter.getList("data.id");
		return recruiterValidIds;
	}

}
