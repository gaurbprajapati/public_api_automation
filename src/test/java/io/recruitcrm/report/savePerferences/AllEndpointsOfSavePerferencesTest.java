package io.recruitcrm.report.savePerferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.commanfunctions.reportService.ReportServiceFunctions;
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
public class AllEndpointsOfSavePerferencesTest extends TestBase{

	
		String reportID="";
		commanFunction function = new commanFunction();
		ListFunctions listFunctions = new ListFunctions();
	
		
		
		JavaFakerSavePerferences reportFaker = new JavaFakerSavePerferences();
		String reportName = reportFaker.getReportName();

	
		@Owner("Sai Teja SG")
		@Test
		public void saveReportPerferences_reportTest() {
	
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put("report_type", "1");
	
			String basePath = "reports-kpi";
	
			Response response = RestClient.doGet("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null,
					true);
	
	
			response.then().body("status_code", Matchers.is(200));
		}
	
		@Owner("Smit Patel")
		@Test(dataProvider = "getReportsValidTestData", groups = "nightly-build")
		public void saveReportPerferences_postReportTest(ArrayList<Integer> recruiterIds,String kpiListStringJson) {
	
			// get /v1/getkpilistreport
			// get /v1/collabrator
	
			ArrayList<Integer> teamIds = new ArrayList<Integer>();
	
			Settings settings = new Settings();
			settings.setRecruiter_ids(recruiterIds);
			settings.setTeam_ids(teamIds);
			settings.setKpi_lists(kpiListStringJson);
			settings.setFrom_date("1613400447");
			settings.setTo_date("1644936462");
			settings.setDate_format("custom_range");
	
			SavePerference savePerference = new SavePerference();
			savePerference.setName(reportName);
			savePerference.setReport_type(1);
			savePerference.setSettings(settings);
	
			Response response = RestClient.doPost("JSON", reportServiceURL, "reports-preferences/save", ThreadManager.getOwnerAlbatrossToken(), null,
					true, savePerference);
	
			
			response.then().statusCode(200);
			response.then().body("data.id", Matchers.notNullValue());
			response.then().body("status_code", Matchers.is(200));
			response.then().body("status_message", Matchers.containsString("success"));
			
			// Verify Response using Assertion and Jsonpath
			JsonPath jp = response.jsonPath();

			int ID = jp.get("data.id");
			reportID=Integer.toString(ID);
			
			
		}
		
		@Owner("Akshaya Uppala")
		@Test(dataProvider = "getReportsValidTestDataForEditSavePerferences", groups = "nightly-build")
		public void test_editSavedReportPerferencesByID(ArrayList<Integer> recruiterIds,String kpiListStringJson) {

	
			Map<String, String> pathParamters = new HashMap<String, String>();
			pathParamters.put("ID", reportID);
			String basePath = "reports-preferences/{ID}"; 
			
			ArrayList<Integer> teamIds = new ArrayList<Integer>();
			Settings settings = new Settings();
			settings.setRecruiter_ids(recruiterIds);
			settings.setTeam_ids(teamIds);
			settings.setKpi_lists(kpiListStringJson);
			settings.setFrom_date("1613400447");
			settings.setTo_date("1644936462");
			settings.setDate_format("custom_range");
	
			SavePerference savePerference = new SavePerference();
			savePerference.setName(reportName);
			savePerference.setReport_type(1);
			savePerference.setSettings(settings);
	
//			Response response = RestClient.doPost("JSON", reportServiceURL, "reports-preferences/save", authTokenMap, null,
//					true, savePerference);
//			
			
			Response response = RestClient.doPost1("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,
					pathParamters, true, savePerference);

			
	
			
			response.then().statusCode(200);
//			response.then().body("data.id", Matchers.notNullValue());
//			response.then().body("status_code", Matchers.is(200));
//			response.then().body("status_message", Matchers.containsString("success"));
//			
//			// Verify Response using Assertion and Jsonpath
//			JsonPath jp = response.jsonPath();
//
//			int ID = jp.get("data.id");
//			reportID=Integer.toString(ID);
//			
			
		}
		
		@Owner("Sai Teja SG")
		@Test(dependsOnMethods = "saveReportPerferences_postReportTest", groups = "nightly-build")
		public void test_GetAllSavedReportPerferences() {
			
			String basePath = "reports-preferences";
			Map<String, String> queryParameters = new HashMap<String, String>();
			queryParameters.put("report_type", "1");

			Response response = RestClient.doGet("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), queryParameters, null, true);
			Assert.assertEquals(response.getStatusCode(), 200);
		}
		
		@Owner("Smit Patel")
		@Test(dependsOnMethods = "saveReportPerferences_postReportTest", groups = "nightly-build")
		public void test_GetSavePerferencesByID() {
			
			Map<String, String> pathParamters = new HashMap<String, String>();
			pathParamters.put("ID", reportID);
			String basePath = "reports-preferences/{ID}";

			Response response = RestClient.doGet("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, true);

			Assert.assertEquals(response.getStatusCode(), 200);
		}
		
		@Owner("Akshaya Uppala")
		@Test(dependsOnMethods = "saveReportPerferences_postReportTest", groups = "nightly-build")
		public void test_DeleteSavePerferencesByID() {
			
			Map<String, String> pathParamters = new HashMap<String, String>();
			pathParamters.put("ID", reportID);
			String basePath = "reports-preferences/{ID}"; 

			Response response = RestClient.doDelete("JSON", reportServiceURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParamters, false);

			Assert.assertEquals(response.getStatusCode(), 200);
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
	
			kpiLists.add(KpiLists1);
			kpiLists.add(KpiLists2);
			
			Gson gson = new Gson();
			String kpiListStringJson = gson.toJson(kpiLists);
	
			Object data[][] = {
	
					{ recruiterIds,kpiListStringJson } };
			return data;
		}
		
		
		@DataProvider
		public Object[][] getReportsValidTestDataForEditSavePerferences() {
	
			String generatedString = RandomStringUtils.randomAlphabetic(4);
			JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
			ArrayList<Integer> recruiterIds = jsonGetCollabrators.get("id");
			
			// Get all KPIlist 
			ReportServiceFunctions reportServiceFunctions = new ReportServiceFunctions();		
			ArrayList<Object> kpiLists = reportServiceFunctions.getAllKPILists();
	
			Gson gson = new Gson();
			String kpiListStringJson = gson.toJson(kpiLists);
	
			Object data[][] = {
	
					{ recruiterIds,kpiListStringJson } };
			return data;
		}
		
//		kpi_lists: "[{\"value\":\"cadded\",\"label\":\"Candidates Added\",\"checked\":true},{\"value\":\"job\",\"label\":\"Jobs Added\",\"checked\":true},{\"value\":\"company\",\"label\":\"Companies Added\",\"checked\":true},{\"value\":\"contact\",\"label\":\"Contacts Added\",\"checked\":true}]"


	
}
