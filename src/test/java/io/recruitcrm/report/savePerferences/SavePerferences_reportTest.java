package io.recruitcrm.report.savePerferences;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.albatross.report.JavaFakerSavePerferences;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.SavePerference.SavePerference;
import io.recruitcrm.report.pojo.SavePerference.Settings;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SavePerferences_reportTest extends TestBase {
	
	ListFunctions listFunctions = new ListFunctions();
	JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
	commanFunction function = new commanFunction();
	AllCrudFunctions allCrudFunctions = new AllCrudFunctions();
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	int accountOwnerid;
	int adminId;
	int resTeamMember;
	int teamMember;
	int team1Id;
	int fromDate = fakerCandidate.getStartDate();
	int toDate = fakerCandidate.getEndDateWithReferenceDate(fromDate);

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferences_Test(ArrayList<Integer> recruiterIds, String kpiListStringJson, int reportType,
			int teamId, int userId) {
		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(teamId);
		teamIds.add(userId);
		String reportName = reportFaker.getReportName();
		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(kpiListStringJson);
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");

		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(200);
		response.then().body("data.id", Matchers.notNullValue());
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("saveReportsPreferences.json"));

	}
	
	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithInvalidRecruiterID_Test(ArrayList<Integer> recruiterIds, String kpiListStringJson, int reportType,
			int teamId, int userId) {
		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(teamId);
		teamIds.add(userId);
		ArrayList<Integer> recruiter = new ArrayList<Integer>();

		String reportName = reportFaker.getReportName();
		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiter);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(kpiListStringJson);
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");

		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);
		response.then().statusCode(200);//Response Data will contain all reports data
		response.then().body("data.id", Matchers.notNullValue());
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("saveReportsPreferences.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithEmptyReportName_Test(ArrayList<Integer> recruiterIds, String kpiListStringJson,
			int reportType, int teamId, int userId) {

		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(teamId);
		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(kpiListStringJson);
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");

		SavePerference savePerference = new SavePerference();
		savePerference.setName("");
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(422);
		response.then().body("name[0]", Matchers.is("The name field is required."));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("saveReportsWithEmptyReportName.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithInvalidReportType_Test(ArrayList<Integer> recruiterIds,
			String kpiListStringJson, int reportType, int teamId, int userId) {
		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(teamId);
		String reportName = reportFaker.getReportName();

		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(kpiListStringJson);
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");

		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportFaker.getReportType());
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(422);
		response.then().body("report_type[0]", Matchers.is("report_type invalid type"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("saveReportsWithInvalidReportType.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithInvalidTeamUserID_Test(ArrayList<Integer> recruiterIds,
			String kpiListStringJson, int reportType, int teamId, int userId) {

		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(reportFaker.getReportType());
		String reportName = reportFaker.getReportName();
		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(kpiListStringJson);
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");

		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(200); // Response will have all Reports data for null or invalid team/user ID
		response.then().body("data.id", Matchers.notNullValue());
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("saveReportsPreferences.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithEmptyTeamUserId_Test(ArrayList<Integer> recruiterIds, String kpiListStringJson,
			int reportType, int teamId, int userId) {

		ArrayList<Integer> teamIds = new ArrayList<Integer>();

		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(kpiListStringJson);
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");
		String reportName = reportFaker.getReportName();
		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(200); // Response will have all Reports data for null or invalid team/user ID
		response.then().body("data.id", Matchers.notNullValue());
		response.then().body("status_code", Matchers.is(200));
		response.then().body("status_message", Matchers.containsString("success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("saveReportsPreferences.json"));

	}

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithInvalidKpiList_Test(ArrayList<Integer> recruiterIds, String kpiListStringJson,
			int reportType, int teamId, int userId) {

		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(teamId);

		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists(reportFaker.getReportName());
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");
		String reportName = reportFaker.getReportName();
		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(422); // Response will have all Reports data for null or invalid kpi's
		response.then().body("settings[0]", Matchers.containsString("Please select at least one KPI to generate report"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
	public void saveReportPerferencesWithEmptyKpiList_Test(ArrayList<Integer> recruiterIds, String kpiListStringJson,
			int reportType, int teamId, int userId) {

		ArrayList<Integer> teamIds = new ArrayList<Integer>();
		teamIds.add(teamId);

		Settings settings = new Settings();
		settings.setRecruiter_ids(recruiterIds);
		settings.setTeam_ids(teamIds);
		settings.setKpi_lists("");
		settings.setFrom_date(String.valueOf(fromDate));
		settings.setTo_date(String.valueOf(toDate));
		settings.setDate_format("custom_range");
		String reportName = reportFaker.getReportName();
		SavePerference savePerference = new SavePerference();
		savePerference.setName(reportName);
		savePerference.setReport_type(reportType);
		savePerference.setSettings(settings);
		String basePath = "reports-preferences/save";
		Response response = RestClient.doPost("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, true,
				savePerference);

		response.then().statusCode(422); // Response will have all Reports data for null or invalid kpi's
		response.then().body("settings[0]", Matchers.containsString("Please select at least one KPI to generate report"));

	}

	@DataProvider
	public Object[][] getReportsValidTestData() {

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
		KpiLists3.setLabel("Hiring Stage - Assigned");
		KpiLists3.setValue("1");
		KpiLists3.setChecked(true);

		KpiLists KpiLists4 = new KpiLists();
		KpiLists4.setLabel("Hiring Stage - Applied");
		KpiLists4.setValue("10");
		KpiLists4.setChecked(true);

		KpiLists KpiLists5 = new KpiLists();
		KpiLists5.setLabel("Hiring Stage - Placed");
		KpiLists5.setValue("8");
		KpiLists5.setChecked(true);

		kpiLists.add(KpiLists1);
		kpiLists.add(KpiLists2);
		kpiLists.add(KpiLists3);
		kpiLists.add(KpiLists4);
		kpiLists.add(KpiLists5);

		Gson gson = new Gson();
		String kpiListStringJson = gson.toJson(kpiLists);
		int reportType = 4;

		Response response = function.getUsers(baseURL, ThreadManager.getAccountApiKey());
		response.then().statusCode(200);
		JsonPath user = response.jsonPath();
		accountOwnerid = user.get("[0].id");
		adminId = user.get("[1].id");
		resTeamMember = user.get("[2].id");
		teamMember = user.get("[3].id");

		Response team = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team.then().statusCode(200);
		JsonPath teamJsonPath = team.jsonPath();

		int arraySize = teamJsonPath.getInt("$.size()");

		ArrayList<String> userId1 = new ArrayList<String>();
		userId1.add(String.valueOf(accountOwnerid));
		userId1.add(String.valueOf(teamMember));

		Response response1 = allCrudFunctions.createTeam(albatrossURL, ThreadManager.getOwnerAlbatrossToken(), "team1", userId1);
		response1.then().statusCode(200);

		Response team1 = function.getTeams(baseURL, ThreadManager.getAccountApiKey());
		team1.then().statusCode(200);
		JsonPath teamPath = team1.jsonPath();
		team1Id = teamPath.get("[0].team_id");

		Object data[][] = { { recruiterIds, kpiListStringJson, reportType, team1Id, accountOwnerid } };
		return data;
	}

}